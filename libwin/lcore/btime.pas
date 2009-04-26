{ Copyright (C) 2005 Bas Steendijk and Peter Green
  For conditions of distribution and use, see copyright notice in zlib_license.txt
  which is included in the package
  ----------------------------------------------------------------------------- }
{
this unit returns unix timestamp with seconds and microseconds (as float)
works on windows/delphi, and on freepascal on unix.
}


unit btime;

interface

type
  float=extended;

const
  colorburst=39375000/11;  {3579545.4545....}

var
  timezone:integer;
  timezonestr:string;
  irctime,unixtime:integer;
  tickcount:integer;
  settimebias:integer;
  performancecountfreq:extended;

function irctimefloat:float;
function irctimeint:integer;

function unixtimefloat:float;
function unixtimeint:integer;

function wintimefloat:float;

procedure settime(newtime:integer);
procedure gettimezone;
procedure timehandler;
procedure init;

function timestring(i:integer):string;
function timestrshort(i:integer):string;

{$ifdef win32}
function unixtimefloat_systemtime:float;
{$endif}

function oletounixfloat(t:float):float;
function oletounix(t:tdatetime):integer;
function unixtoole(i:integer):tdatetime;

{$ifdef win32}
function mmtimefloat:float;
function qpctimefloat:float;
{$endif}

const
  mmtime_driftavgsize=32;
  mmtime_warmupnum=4;
  mmtime_warmupcyclelength=15;
var
  //this flag is to be set when btime has been running long enough to stabilise
  warmup_finished:boolean;

  timefloatbias:float;
  ticks_freq:float=0;
  ticks_freq2:float=0;
  ticks_freq_known:boolean=false;
  lastunixtimefloat:float=0;
  lastsynctime:float=0;
  lastsyncbias:float=0;

  mmtime_last:integer=0;
  mmtime_wrapadd:float;
  mmtime_lastsyncmm:float=0;
  mmtime_lastsyncqpc:float=0;
  mmtime_drift:float=1;
  mmtime_lastresult:float;
  mmtime_nextdriftcorrection:float;
  mmtime_driftavg:array[0..mmtime_driftavgsize] of float;
  mmtime_synchedqpc:boolean;

  mmtime_prev_drift:float;
  mmtime_prev_lastsyncmm:float;
  mmtime_prev_lastsyncqpc:float;

implementation

{$ifdef fpc}
  {$mode delphi}
{$endif}

uses
  {$ifdef UNIX}
    {$ifdef VER1_0}
      linux,
    {$else}
      baseunix,unix,unixutil, {needed for 2.0.2}
    {$endif}
  {$else}
    windows,unitsettc,mmsystem,
  {$endif}
  sysutils;

  {$include unixstuff.inc}


const
  daysdifference=25569;

function oletounixfloat(t:float):float;
begin
  t := (t - daysdifference) * 86400;
  result := t;
end;

function oletounix(t:tdatetime):integer;
begin
  result := trunc(oletounixfloat(t));
end;

function unixtoole(i:integer):tdatetime;
begin
  result := ((i)/86400)+daysdifference;
end;

const
  highdwordconst=65536.0 * 65536.0;

function utrunc(f:float):integer;
{converts float to integer, in 32 bits unsigned range}
begin
  if f >= (highdwordconst/2) then f := f - highdwordconst;
  result := trunc(f);
end;

function uinttofloat(i:integer):float;
{converts 32 bits unsigned integer to float}
begin
  result := i;
  if result < 0 then result := result + highdwordconst;
end;

{$ifdef unix}
{-----------------------------------------*nix/freepascal code to read time }

function unixtimefloat:float;
var
  tv:ttimeval;
begin
  gettimeofday(tv);
  result := tv.tv_sec+(tv.tv_usec/1000000);
end;

function wintimefloat:extended;
begin
  result := unixtimefloat;
end;

function unixtimeint:integer;
var
  tv:ttimeval;
begin
  gettimeofday(tv);
  result := tv.tv_sec;
end;

{$else} {delphi 3}
{------------------------------ windows/delphi code to read time}

{
time float: gettickcount
resolution: 9x: ~55 ms NT: 1/64th of a second
guarantees: continuous without any jumps
frequency base: same as system clock.
epoch: system boot
note: if called more than once per 49.7 days, 32 bits wrapping is compensated for and it keeps going on.
note: i handle the timestamp as signed integer, but with the wrap compensation that works as well, and is faster
}

function mmtimefloat:float;
const
  wrapduration=highdwordconst * 0.001;
var
  i:integer;
begin
  i := gettickcount; {timegettime}
  if i < mmtime_last then begin
    mmtime_wrapadd := mmtime_wrapadd + wrapduration;
  end;
  mmtime_last := i;
  result := mmtime_wrapadd + i * 0.001;

  if (ticks_freq <> 0) and ticks_freq_known then result := int((result / ticks_freq)+0.5) * ticks_freq; //turn the float into an exact multiple of 1/64th sec to improve accuracy of things using this
end;

procedure measure_ticks_freq;
var
  f,g:float;
  o:tosversioninfo;
  isnt:boolean;
  is9x:boolean;
begin
  if (performancecountfreq = 0) then qpctimefloat;
  ticks_freq_known := false;
  settc;
  f := mmtimefloat;
  repeat g := mmtimefloat until g > f;
  unsettc;
  f := g - f;
  fillchar(o,sizeof(o),0);
  o.dwOSVersionInfoSize := sizeof(o);
  getversionex(o);
  isnt := o.dwPlatformId = VER_PLATFORM_WIN32_NT;
  is9x := o.dwPlatformId = VER_PLATFORM_WIN32_WINDOWS;

  ticks_freq2 := f;
  mmtime_synchedqpc := false;
  {
  NT 64 Hz
  identify mode as: nt64
  QPC rate: either 3579545 or TSC freq
  QPC synched to gettickcount: no
  duration between 2 ticks is constant: yes
  gettickcount tick duration: 64 Hz
  }
  if (f >= 0.014) and (f <= 0.018) and isnt then begin
    ticks_freq_known := true;
    ticks_freq := 1/64;
    mmtime_synchedqpc := false;
  end;

  {
  NT 100 Hz
  identify mode as: nt100
  QPC rate: 1193182
  QPC synched to gettickcount: yes
  duration between 2 ticks is constant: no?
  gettickcount tick duration: ~99.85 Hz
  }
  if (performancecountfreq = 1193182) and (f >= 0.008) and (f <= 0.012) and isnt then begin
    ticks_freq_known := true;
    ticks_freq2 := 11949 / (colorburst / 3);
   //  ticks_freq2 := 11949 / 1193182;
    ticks_freq := 0;
    {the ticks freq should be very close to the real one but if it's not exact, it will cause drift and correction jumps}
    mmtime_synchedqpc := true;
  end;

  {9x}
  if (performancecountfreq = 1193182) and (g >= 0.050) and (g <= 0.060) then begin
    ticks_freq_known := true;
    ticks_freq := 65536 / (colorburst / 3);
    mmtime_synchedqpc := true;
  end;
  ticks_freq_known := true;
  if ticks_freq <> 0 then ticks_freq2 := ticks_freq;
//  writeln(formatfloat('0.000000',ticks_freq));
end;

{
time float: QueryPerformanceCounter
resolution: <1us
guarantees: can have forward jumps depending on hardware. can have forward and backwards jitter on dual core.
frequency base: on NT, not the system clock, drifts compared to it.
epoch: system boot
}
function qpctimefloat:extended;
var
  p:packed record
    lowpart:longint;
    highpart:longint
  end;
  p2:tlargeinteger absolute p;
  e:extended;
begin
  if performancecountfreq = 0 then begin
    QueryPerformancefrequency(p2);
    e := p.lowpart;
    if e < 0 then e := e + highdwordconst;
    performancecountfreq := ((p.highpart*highdwordconst)+e);
  end;
  queryperformancecounter(p2);
  e := p.lowpart;
  if e < 0 then e := e + highdwordconst;

  result := ((p.highpart*highdwordconst)+e)/performancecountfreq;
end;

{
time float: QPC locked to gettickcount
resolution: <1us
guarantees: continuous without any jumps
frequency base: same as system clock.
epoch: system boot
}

function mmqpctimefloat:float;
const
  maxretries=5;
  margin=0.002;
var
  jump:float;
  mm,f,qpc,newdrift,f1,f2:float;
  qpcjumped:boolean;
  a,b,c:integer;
  retrycount:integer;
begin
  if not ticks_freq_known then measure_ticks_freq;
  retrycount := maxretries;

  qpc := qpctimefloat;
  mm := mmtimefloat;
  f := (qpc - mmtime_lastsyncqpc) * mmtime_drift + mmtime_lastsyncmm;
  //writeln('XXXX ',formatfloat('0.000000',qpc-mm));
  qpcjumped := ((f-mm) > ticks_freq2+margin) or ((f-mm) < -margin);
//  if qpcjumped then writeln('qpc jumped ',(f-mm));
  if ((qpc > mmtime_nextdriftcorrection) and not mmtime_synchedqpc) or qpcjumped then begin

    mmtime_nextdriftcorrection := qpc + 1;
    repeat
      mmtime_prev_drift := mmtime_drift;
      mmtime_prev_lastsyncmm := mmtime_lastsyncmm;
      mmtime_prev_lastsyncqpc := mmtime_lastsyncqpc;

      mm := mmtimefloat;
      dec(retrycount);
      settc;
      result := qpctimefloat;
      f := mmtimefloat;
      repeat
        if f = mm then result := qpctimefloat;
        f := mmtimefloat
      until f > mm;
      qpc := qpctimefloat;

      unsettc;
      if (qpc > result + 0.0001) then begin
        continue;
      end;
      mm := f;

      if (mmtime_lastsyncqpc <> 0) and not qpcjumped then begin
        newdrift := (mm - mmtime_lastsyncmm) / (qpc - mmtime_lastsyncqpc);
        mmtime_drift := newdrift;
     {   writeln('raw drift: ',formatfloat('0.00000000',mmtime_drift));}
        move(mmtime_driftavg[0],mmtime_driftavg[1],sizeof(mmtime_driftavg[0])*high(mmtime_driftavg));
        mmtime_driftavg[0] := mmtime_drift;

{        write('averaging drift ',formatfloat('0.00000000',mmtime_drift),' -> ');}
{        mmtime_drift := 0;}
        b := 0;
        for a := 0 to high(mmtime_driftavg) do begin
          if mmtime_driftavg[a] <> 0 then inc(b);
{          mmtime_drift := mmtime_drift + mmtime_driftavg[a];}
        end;
{        mmtime_drift := mmtime_drift / b;}
        if (b = 1) then a := 5 else if (b = 2) then a := 15 else if (b = 3) then a := 30 else if (b = 4) then a := 60 else if (b = 5) then a := 120 else if (b >= 5) then a := 120;
        mmtime_nextdriftcorrection := qpc + a;
        if (b >= 2) then warmup_finished := true;
{        writeln(formatfloat('0.00000000',mmtime_drift));}
       if mmtime_synchedqpc then mmtime_drift := 1;
      end;

      mmtime_lastsyncqpc := qpc;
      mmtime_lastsyncmm := mm;
  {   writeln(formatfloat('0.00000000',mmtime_drift));}
      break;
    until false;


    qpc := qpctimefloat;

    result := (qpc - mmtime_lastsyncqpc) * mmtime_drift + mmtime_lastsyncmm;
    f := (qpc - mmtime_prev_lastsyncqpc) * mmtime_prev_drift + mmtime_prev_lastsyncmm;

    jump := result-f;
    {writeln('jump ',formatfloat('0.000000',jump),'   drift ',formatfloat('0.00000000',mmtime_drift),' duration ',formatfloat('0.000',(mmtime_lastsyncqpc-mmtime_prev_lastsyncqpc)),' ',formatfloat('0.00000000',jump/(mmtime_lastsyncqpc-mmtime_prev_lastsyncqpc)));}

    f := result;
  end;

  result := f;

  if (result < mmtime_lastresult) then result := mmtime_lastresult + 0.000001;
  mmtime_lastresult := result;
end;

{ free pascals tsystemtime is incomaptible with windows api calls
 so we declare it ourselves - plugwash
}
{$ifdef fpc}
type
  TSystemTime = record
     wYear: Word;
     wMonth: Word;
     wDayOfWeek: Word;
     wDay: Word;
     wHour: Word;
     wMinute: Word;
     wSecond: Word;
     wMilliseconds: Word;
  end;
 {$endif}
function Date_utc: extended;
var
  SystemTime: TSystemTime;
begin
  {$ifdef fpc}
    GetsystemTime(@SystemTime);
  {$else}
    GetsystemTime(SystemTime);
  {$endif}
  with SystemTime do Result := EncodeDate(wYear, wMonth, wDay);
end;

function Time_utc: extended;
var
  SystemTime: TSystemTime;
begin
  {$ifdef fpc}
    GetsystemTime(@SystemTime);
  {$else}
    GetsystemTime(SystemTime);
  {$endif}
  with SystemTime do
    Result := EncodeTime(wHour, wMinute, wSecond, wMilliSeconds);
end;

function Now_utc: extended;
begin
  Result := round(Date_utc) + Time_utc;
end;

function unixtimefloat_systemtime:float;
begin
  {result := oletounixfloat(now_utc);}

  {this method gives exactly the same result with extended precision, but is less sensitive to float rounding in theory}
  result := oletounixfloat(int(date_utc+0.5))+time_utc*86400;
end;

function wintimefloat:extended;
begin
  result := mmqpctimefloat;
end;

function unixtimefloat:float;
const
  margin = 0.0012;
var
  f,g,h:float;
begin
  result := wintimefloat+timefloatbias;
  f := result-unixtimefloat_systemtime;
  if ((f > ticks_freq2+margin) or (f < -margin)) or (timefloatbias = 0) then begin
//    writeln('unixtimefloat init');
    f := unixtimefloat_systemtime;
    settc;
    repeat g := unixtimefloat_systemtime; h := wintimefloat until g > f;
    unsettc;
    timefloatbias := g-h;
    result := unixtimefloat;
  end;

  {for small changes backwards, guarantee no steps backwards}
  if (result <= lastunixtimefloat) and (result > lastunixtimefloat-1.5) then result := lastunixtimefloat + 0.0000001;
  lastunixtimefloat := result;
end;

function unixtimeint:integer;
begin
  result := trunc(unixtimefloat);
end;

{$endif}
{-----------------------------------------------end of platform specific}

function irctimefloat:float;
begin
  result := unixtimefloat+settimebias;
end;

function irctimeint:integer;
begin
  result := unixtimeint+settimebias;
end;


procedure settime(newtime:integer);
var
  a:integer;
begin
  a := irctimeint-settimebias;
  if newtime = 0 then settimebias := 0 else settimebias := newtime-a;

  irctime := irctimeint;
end;

procedure timehandler;
begin
  if unixtime = 0 then init;
  unixtime := unixtimeint;
  irctime := irctimeint;
  if unixtime and 63 = 0 then begin
    {update everything, apply timezone changes, clock changes, etc}
    gettimezone;
    timefloatbias := 0;
    unixtime := unixtimeint;
    irctime := irctimeint;
  end;
end;


procedure gettimezone;
var
  {$ifdef UNIX}
    {$ifndef ver1_9_4}
      {$ifndef ver1_0}
        {$define above194}
      {$endif}
    {$endif}
    {$ifndef above194}
      hh,mm,ss:word;
    {$endif}
  {$endif}
  l:integer;
begin
  {$ifdef UNIX}
    {$ifdef above194}
      timezone := tzseconds;
    {$else}
      gettime(hh,mm,ss);
      timezone := (longint(hh) * 3600 + mm * 60 + ss) - (unixtimeint mod 86400);
    {$endif}
  {$else}
  timezone := round((now-now_utc)*86400);
  {$endif}

  while timezone > 43200 do dec(timezone,86400);
  while timezone < -43200 do inc(timezone,86400);

  if timezone >= 0 then timezonestr := '+' else timezonestr := '-';
  l := abs(timezone) div 60;
  timezonestr := timezonestr + char(l div 600 mod 10+48)+char(l div 60 mod 10+48)+':'+char(l div 10 mod 6+48)+char(l mod 10+48);
end;

function timestrshort(i:integer):string;
const
  weekday:array[0..6] of string[4]=('Thu','Fri','Sat','Sun','Mon','Tue','Wed');
  month:array[0..11] of string[4]=('Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec');
var
  y,m,d,h,min,sec,ms:word;
  t:tdatetime;
begin
  t := unixtoole(i+timezone);
  decodedate(t,y,m,d);
  decodetime(t,h,min,sec,ms);
  result := weekday[(i+timezone) div 86400 mod 7]+' '+month[m-1]+' '+inttostr(d)+' '+
  inttostr(h div 10)+inttostr(h mod 10)+':'+inttostr(min div 10)+inttostr(min mod 10)+':'+inttostr(sec div 10)+inttostr(sec mod 10)+' '+
  inttostr(y);
end;

function timestring(i:integer):string;
const
  weekday:array[0..6] of string[10]=('Thursday','Friday','Saturday','Sunday','Monday','Tuesday','Wednesday');
  month:array[0..11] of string[10]=('January','February','March','April','May','June','July','August','September','October','November','December');
var
  y,m,d,h,min,sec,ms:word;
  t:tdatetime;
begin
  t := unixtoole(i+timezone);
  decodedate(t,y,m,d);
  decodetime(t,h,min,sec,ms);
  result := weekday[(i+timezone) div 86400 mod 7]+' '+month[m-1]+' '+inttostr(d)+' '+inttostr(y)+' -- '+
  inttostr(h div 10)+inttostr(h mod 10)+':'+inttostr(min div 10)+inttostr(min mod 10)+':'+inttostr(sec div 10)+inttostr(sec mod 10)+' '+
  timezonestr;
end;

procedure init;
begin
  {$ifdef win32}timebeginperiod(1);{$endif} //ensure stable unchanging clock
  fillchar(mmtime_driftavg,sizeof(mmtime_driftavg),0);
  settimebias := 0;
  gettimezone;
  unixtime := unixtimeint;
  irctime := irctimeint;
end;

initialization init;

end.
