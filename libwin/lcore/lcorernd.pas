{ Copyright (C) 2005 Bas Steendijk and Peter Green
  For conditions of distribution and use, see copyright notice in zlib_license.txt
  which is included in the package
  ----------------------------------------------------------------------------- }

unit lcorernd;

interface

{$include lcoreconfig.inc}

{
written by Bas Steendijk (beware)

the aim of this unit is to provide randomness in a consistent way, using OS specific methods for seeding

this unit uses MD5 for performance and code size, but it is made so it is easy to use a different hash,
as long as it is atleat 128 bits, and a multiple of the "word size" (32 bits)

goals:

- for the code to be:
 - relatively simple and small
 - reasonably fast

- for the numbers to be
 - random: pass diehard and similar tests
 - unique: generate UUID's
 - secure: difficult for a remote attacker to guess the internal state, even
   when given some output

typical intended uses:
 - anything that needs random numbers without extreme demands on security or
   speed should be able to use this
 - seeding other (faster) RNG's
 - generation of passwords, UUID's, cookies, and session keys
 - randomizing protocol fields to protect against spoofing attacks
 - randomness for games

this is not intended to be directly used for:
- high securirity purposes (generating RSA root keys etc)
- needing random numbers at very high rates (disk wiping, some simulations, etc)

performance:
- 24 MB/s on 2.2 GHz athlon64 core on windows XP 32 bits
- 6.4 MB/s on 1 GHz p3 on linux

exe size:
- fpc 2.2, linux: fastmd5: 12 kb; lcorernd: 6 kb.
- delphi 6: fastmd5: 3 kb; lcorernd: 2 kb

reasoning behind the security of this RNG:

- seeding:
1: i assume that any attacker has no local access to the machine. if one gained
  this, then there are more seriousness weaknesses to consider.
2: i attempt to use enough seeding to be difficult to guess.
  on windows: GUID, various readouts of hi res timestamps, heap stats, cursor
  position
  on *nix: i assume /dev/(u)random output is secure and difficult to guess. if
  it misses, i use /dev/wtmp, which typically has as lot of entropy in it. i also use hi res timestamps.
3: on a state compromise, one can easily get up to the hash size worth of previous output, beyond that one has
  to invert the hash operation.

- mixing/expansion: a secure hash random walk on a buffer with a constant secret and a changing exposed part,
  the big secret part serves to make it difficult for an attacker to predict next and previous output.
  the secret part is changed during a reseed.


                                       OS randomness
                                             v
                              <wwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwww>
 ____________________________  ________________________________________________
[            pool            ][                    seed                        ]
[hashsize][hashsize][hashsize]
          <rrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrr>
                bighash()             seeding
                   v
          <wwwwwwwwwwwwwwwwww>
<rrrrrrrrrrrrrrrrrrrrrrrrrrrr>
  hash()                            random walk
    v
<wwwwwwww>
[ output ][      secret      ]


this needs testing on platforms other than i386


these routines are called by everything else in lcore, and if the app coder desires, by the app.
because one may want to use their own random number source, the PRNG here can be excluded from linking,
and the routines here can be hooked.
}

{$include uint32.inc}

{return a dword with 32 random bits}
type
  wordtype=uint32;

var
  randomdword:function:wordtype;

{fill a buffer with random bytes}
procedure fillrandom(var buf;length:integer);

{generate an integer of 0 <= N < i}
function randominteger(i:longint):longint;

{generate an integer with the lowest b bits being random}
function randombits(b:integer):longint;

{generate a version 4 random uuid}
function generate_uuid:string;

{$ifndef nolcorernd}

{call this to mix seeding into the pool. is normally done automatically and does not have to be called
but can be done if one desires more security, for example for key generation}
procedure seedpool;

{get some raw OS specific randomness. the output is not mixed so it should not be used directly as random numbers}
function collect_seeding(var output;const bufsize:integer):integer;

function internalrandomdword:wordtype;

var
  reseedinterval:integer=64;
{$endif}

implementation

{$ifndef nolcorernd}
uses
  {$ifdef win32}windows,activex,{$endif}
  {$ifdef unix}
    {$ifdef ver1_0}
      linux,
    {$else}
      baseunix,unix,unixutil,
    {$endif}
  {$endif}
  fastmd5,sysutils;

{$ifdef unix}{$include unixstuff.inc}{$endif}

type
  {hashtype must be array of bytes}
  hashtype=tmd5;

const
  wordsizeshift=2;
  wordsize=1 shl wordsizeshift;
  //wordsize check commented out for d3 compatibility
  //{ $if (wordsize <> sizeof(wordtype))}'wordsizeshift must be setcorrectly'{ $ifend}
  hashsize=sizeof(hashtype);
  halfhashsize=hashsize div 2;
  hashdwords=hashsize div wordsize;
  pooldwords=3*hashdwords;
  seeddwords=32;
  hashpasssize=48; {this number has to be small enough that hashing this size uses only one block transform}

var
  {the seed part of this buffer must be atleast as big as the OS seed (windows: 104 bytes, unix: 36 bytes)}
  pool:array[0..(pooldwords+seeddwords-1)] of wordtype;
  reseedcountdown:integer;

{$ifdef win32}
function collect_seeding(var output;const bufsize:integer):integer;
var
  l:packed record
    guid:array[0..3] of longint;
    qpcbuf:array[0..1] of longint;
    rdtscbuf:array[0..1] of longint;
    systemtimebuf:array[0..3] of longint;
    pid:longint;
    tid:longint;
    cursor:tpoint;
    hs:theapstatus;
  end absolute output;
  rdtsc_0,rdtsc_1:integer;
begin
  result := 0;
  if (bufsize < sizeof(l)) then exit;
  result := sizeof(l);
  {PID}
  l.pid := GetCurrentProcessId;
  l.tid := GetCurrentThreadId;

  {COCREATEGUID}
  cocreateguid(tguid(l.guid));

  {QUERYPERFORMANCECOUNTER}
  queryperformancecounter(tlargeinteger(l.qpcbuf));

  {RDTSC}
  {$ifdef cpu386}
  asm
    db $0F; db $31
    mov rdtsc_0,eax
    mov rdtsc_1,edx
  end;
  l.rdtscbuf[0] := rdtsc_0;
  l.rdtscbuf[1] := rdtsc_1;
  {$endif}
  {GETSYSTEMTIME}
  getsystemtime(tsystemtime(l.systemtimebuf));

  {cursor position}
  getcursorpos(l.cursor);

  l.hs := getheapstatus;
end;
{$endif}

{$ifdef unix}

var
  wtmpinited:boolean;
  wtmpcached:hashtype;

procedure wtmphash;
var
  f:file;
  buf:array[0..4095] of byte;
  numread:integer;
  state:tmd5state;
begin
  if wtmpinited then exit;

  assignfile(f,'/var/log/wtmp');
  filemode := 0;
  {$i-}reset(f,1);{$i+}
  if (ioresult <> 0) then exit;
  md5init(state);
  while not eof(f) do begin
    blockread(f,buf,sizeof(buf),numread);
    md5process(state,buf,numread);
  end;
  closefile(f);
  md5finish(state,wtmpcached);
  wtmpinited := true;
end;


function collect_seeding(var output;const bufsize:integer):integer;
var
  f:file;
  a:integer;
  l:packed record
    devrnd:array[0..3] of integer;
    rdtscbuf:array[0..1] of integer;
    tv:ttimeval;
    pid:integer;
  end absolute output;
  rdtsc_0,rdtsc_1:integer;

begin
  result := 0;
  if (bufsize < sizeof(l)) then exit;
  result := sizeof(l);

  {/DEV/URANDOM}
  a := 1;
  assignfile(f,'/dev/urandom');
  filemode := 0;
  {$i-}reset(f,1);{$i+}
  a := ioresult;
  if (a <> 0) then begin
    assignfile(f,'/dev/random');
    {$i-}reset(f,1);{$i+}
    a := ioresult;
  end;
  if (a = 0) then begin
    blockread(f,l.devrnd,sizeof(l.devrnd));
    closefile(f);
  end else begin
    {the OS we are on has no /dev/random or /dev/urandom, get a hash from /var/log/wtmp}
    wtmphash;
    move(wtmpcached,l.devrnd,sizeof(l.devrnd));
  end;
  {get more randomness in case there's no /dev/random}
  {$ifdef cpu386}{$ASMMODE intel}
  asm
    db $0F; db $31
    mov rdtsc_0,eax
    mov rdtsc_1,edx
  end;
  l.rdtscbuf[0] := rdtsc_0;
  l.rdtscbuf[1] := rdtsc_1;
  {$endif}

  gettimeofday(l.tv);
  l.pid := getpid;
end;
{$endif}

{this produces a hash which is twice the native hash size (32 bytes for MD5)}
procedure bighash(const input;len:integer;var output);
var
  inarr:array[0..65535] of byte absolute input;
  outarr:array[0..65535] of byte absolute output;

  h1,h2,h3,h4:hashtype;
  a:integer;
begin
  a := len div 2;
  {first hash round}
  getmd5(inarr[0],a,h1);
  getmd5(inarr[a],len-a,h2);

  move(h1[0],h3[0],halfhashsize);
  move(h2[0],h3[halfhashsize],halfhashsize);
  move(h1[halfhashsize],h4[0],halfhashsize);
  move(h2[halfhashsize],h4[halfhashsize],halfhashsize);

  getmd5(h3,hashsize,outarr[0]);
  getmd5(h4,hashsize,outarr[hashsize]);
end;

procedure seedpool;
var
  a:integer;
begin
  a := collect_seeding(pool[pooldwords],seeddwords*wordsize);
  if (a = 0) then halt;
  bighash(pool[hashdwords],(2*hashsize)+a,pool[hashdwords]);
  getmd5(pool[0],hashpasssize,pool[0]);
end;

function internalrandomdword;
begin
  if (reseedcountdown <= 0) then begin
    seedpool;
    reseedcountdown := reseedinterval * hashdwords;
  end else if ((reseedcountdown mod hashdwords) = 0) then begin;
    getmd5(pool[0],hashpasssize,pool[0]);
  end;
  dec(reseedcountdown);

  result := pool[reseedcountdown mod hashdwords];
end;
{$endif}

procedure fillrandom(var buf;length:integer);
var
  a,b:integer;
  buf_:array[0..16383] of uint32 absolute buf;

begin
  b := 0;
  for a := (length shr wordsizeshift)-1 downto 0 do begin
    buf_[b] := randomdword;
    inc(b);
  end;
  length := length and (wordsize-1);
  if length <> 0 then begin
    a := randomdword;
    move(a,buf_[b],length);
  end;
end;

const
  wordsizebits=32;

function randombits(b:integer):longint;
begin
  result := randomdword;
  result := result and (-1 shr (wordsizebits-b));
  if (b = 0) then result := 0;
end;

function randominteger(i:longint):longint;
var
  a,b:integer;
  j:integer;
begin
  //bitscounter := bitscounter + numofbitsininteger(i);
  if (i = 0) then begin
    result := 0;
    exit;
  end;
  {find number of bits needed}
  j := i-1;
  if (j < 0) then begin
    result := randombits(wordsizebits);
    exit
  end else if (j >= (1 shl (wordsizebits-2))) then begin
    b := wordsizebits-1
  end else begin
    b := -1;
    for a := 0 to (wordsizebits-2) do begin
      if j < 1 shl a then begin
        b := a;
        break;
      end;
    end;
  end;
  repeat
    result := randombits(b);
  until result < i;
end;

const
  ch:array[0..15] of char='0123456789abcdef';

function generate_uuid:string;
var
  buf:array[0..7] of word;
function inttohex(w:word):string;
begin
  result := ch[w shr 12] + ch[(w shr 8) and $f] + ch[(w shr 4) and $f] + ch[w and $f];
end;
begin
  fillrandom(buf,sizeof(buf));

  {uuid version 4}
  buf[3] := (buf[3] and $fff) or $4000;

  {uuid version 4}
  buf[4] := (buf[4] and $3fff) or $8000;

  result := inttohex(buf[0]) + inttohex(buf[1]) + '-' + inttohex(buf[2]) +'-'+ inttohex(buf[3]) + '-' + inttohex(buf[4])
  + '-' + inttohex(buf[5]) + inttohex(buf[6]) + inttohex(buf[7]);
end;

{$ifndef nolcorernd}
initialization randomdword := @internalrandomdword;
{$endif}

end.

