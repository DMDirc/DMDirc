{ Copyright (C) 2005 Bas Steendijk and Peter Green
  For conditions of distribution and use, see copyright notice in zlib_license.txt
  which is included in the package
  ----------------------------------------------------------------------------- }
unit dnssync;
{$ifdef fpc}
  {$mode delphi}
{$endif}

{$include lcoreconfig.inc}

interface
  uses
    dnscore,
    binipstuff,
    {$ifdef win32}
      winsock,
      windows,
    {$else}
      {$ifdef VER1_0}
        linux,
      {$else}
        baseunix,unix,unixutil,
      {$endif}
      sockets,
      fd_utils,
    {$endif}
    lcorernd,
    sysutils;

//convert a name to an IP
//will return v4 or v6 depending on what seems favorable, or manual preference setting
//on error the binip will have a family of 0 (other fiels are also currently
//zeroed out but may be used for further error information in future)
//timeout is in miliseconds, it is ignored when using windows dns
function forwardlookup(name:string;timeout:integer):tbinip;

//convert a name to a list of all IP's returned
//this returns both v4 and v6 IP's, or possibly only v4 or v6, depending on settings
//on error, returns an empty list
function forwardlookuplist(name:string;timeout:integer):tbiniplist;


//convert an IP to a name, on error a null string will be returned, other
//details as above
function reverselookup(ip:tbinip;timeout:integer):string;



const
  tswrap=$4000;
  tsmask=tswrap-1;

  numsock=1{$ifdef ipv6}+1{$endif};
  defaulttimeout=10000;
  const mintimeout=16;

  toport='53';

var
  id:integer;

  sendquerytime:array[0..numsock-1] of integer;
implementation

{$ifdef win32}
  uses dnswin;
{$endif}


{$ifndef win32}
{$define syncdnscore}
{$endif}

{$i unixstuff.inc}
{$i ltimevalstuff.inc}

var
  numsockused:integer;
  fd:array[0..numsock-1] of integer;
  state:array[0..numsock-1] of tdnsstate;
  toaddr:array[0..numsock-1] of tbinip;

{$ifdef syncdnscore}

{$ifdef win32}
  const
    winsocket = 'wsock32.dll';
  function sendto(s: TSocket; const Buf; len, flags: Integer; var addrto: TinetSockAddrV; tolen: Integer): Integer; stdcall; external    winsocket name 'sendto';
  function bind(s: TSocket; var addr: TinetSockAddrV; namelen: Integer): Longbool; stdcall; external    winsocket name 'bind';
  type
    fdset=tfdset;
{$endif}


function getts:integer;
{$ifdef win32}
begin
  result := GetTickCount and tsmask;
{$else}
var
  temp:ttimeval;
begin
  gettimeofday(temp);
  result := ((temp.tv_usec div 1000) + (temp.tv_sec * 1000)) and tsmask;
{$endif}
end;


function sendquery(socknum:integer;const packet:tdnspacket;len:integer):boolean;
var
  a:integer;
  addr       : string;
  port       : string;
  inaddr     : TInetSockAddrV;
begin
{  writeln('sendquery ',decodename(state.packet,state.packetlen,12,0,a),' ',state.requesttype);}
  result := false;
  if len = 0 then exit; {no packet}

  if overridednsserver <> '' then addr := overridednsserver else addr := getcurrentsystemnameserver(id);

  {$ifdef ipv6}{$ifdef win32}
  if toaddr[socknum].family = AF_INET6 then if (useaf = 0) then useaf := useaf_preferv6;
  {$endif}{$endif}

  port := toport;
  toaddr[socknum] := ipstrtobinf(addr);
  makeinaddrv(toaddr[socknum],port,inaddr);

  sendto(fd[socknum],packet,len,0,inaddr,inaddrsize(inaddr));
  sendquerytime[socknum] := getts;
  result := true;
end;

procedure setupsocket;
var
  inAddrtemp : TInetSockAddrV;
  a:integer;
  biniptemp:tbinip;
  addr:string;
begin
  //init both sockets smultaneously, always, so they get succesive fd's
  if fd[0] > 0 then exit;

  if overridednsserver <> '' then addr := overridednsserver else addr := getcurrentsystemnameserver(id);
  //must get the DNS server here so we know to init v4 or v6

  ipstrtobin(addr,biniptemp);

  if biniptemp.family = AF_INET6 then biniptemp := ipstrtobinf('::') else biniptemp := ipstrtobinf('0.0.0.0');


  for a := 0 to numsockused-1 do begin
    makeinaddrv(biniptemp,inttostr( 1024 + randominteger(65536 - 1024) ),inaddrtemp);

    fd[a] := Socket(biniptemp.family,SOCK_DGRAM,0);

    If {$ifndef win32}Not{$endif} Bind(fd[a],inAddrtemp,inaddrsize(inaddrtemp)) Then begin
      {$ifdef win32}
        raise Exception.create('unable to bind '+inttostr(WSAGetLastError));
      {$else}
        raise Exception.create('unable to bind '+inttostr(socketError));
      {$endif}
    end;
  end;
end;

procedure resolveloop(timeout:integer);
var
  selectresult   : integer;
  fds            : fdset;

  endtime      : longint;
  starttime    : longint;
  wrapmode     : boolean;
  currenttime  : integer;

  lag            : ttimeval;
  currenttimeout : ttimeval;
  selecttimeout	 : ttimeval;
  socknum:integer;
  needprocessing:array[0..numsock-1] of boolean;
  finished:array[0..numsock-1] of boolean;
  a,b:integer;

  Src    : TInetSockAddrV;
  Srcx   : {$ifdef win32}sockaddr_in{$else}TInetSockAddrV{$endif} absolute Src;
  SrcLen : Integer;
  fromip:tbinip;
  fromport:string;

begin
  if timeout < mintimeout then timeout := defaulttimeout;

    starttime := getts;
    endtime := starttime + timeout;
    if (endtime and tswrap)=0 then begin
      wrapmode := false;
    end else begin
      wrapmode := true;
    end;
    endtime := endtime and tsmask;

  setupsocket;
  for socknum := 0 to numsockused-1 do begin
    needprocessing[socknum] := true;
    finished[socknum] := false;
  end;

  repeat
    for socknum := numsockused-1 downto 0 do if needprocessing[socknum] then begin
      state_process(state[socknum]);
      case state[socknum].resultaction of
        action_ignore: begin
          {do nothing}
        end;
        action_done: begin
          finished[socknum] := true;
          //exit if all resolvers are finished
          b := 0;
          for a := 0 to numsockused-1 do begin
            if finished[a] then inc(b);
          end;
          if (b = numsockused) then begin
            exit;
          end;
          //onrequestdone(self,0);
        end;
        action_sendquery:begin
{        writeln('send query');}
          sendquery(socknum,state[socknum].sendpacket,state[socknum].sendpacketlen);
        end;
      end;
      needprocessing[socknum] := false;
    end;

    currenttime := getts;
    msectotimeval(selecttimeout, (endtime-currenttime) and tsmask);

    fd_zero(fds);
    for socknum := numsockused-1 downto 0 do if not finished[socknum] then fd_set(fd[socknum],fds);
    if (selecttimeout.tv_sec > 0) or (selecttimeout.tv_usec > retryafter) then begin
      selecttimeout.tv_sec := 0;
      selecttimeout.tv_usec := retryafter;
    end;
    //find the highest of the used fd's
    b := 0;
    for socknum := numsockused-1 downto 0 do if fd[socknum] > b then b := fd[socknum];
    selectresult := select(b+1,@fds,nil,nil,@selecttimeout);
    if selectresult > 0 then begin
      currenttime := getts;
      for socknum := numsockused-1 downto 0 do if fd_isset(fd[socknum],fds) then begin
  {      writeln('selectresult>0');}
        //why the hell are we zeroing out the packet buffer before reading into it?! --plugwash

        fillchar(state[socknum].recvpacket,sizeof(state[socknum].recvpacket),0);
        msectotimeval(lag,(currenttime-sendquerytime[socknum]) and tsmask);

        if overridednsserver = '' then reportlag(id,(lag.tv_sec*1000000)+lag.tv_usec);

        SrcLen := SizeOf(Src);
        state[socknum].recvpacketlen := recvfrom(fd[socknum],state[socknum].recvpacket, SizeOf(state[socknum].recvpacket),0,Srcx,SrcLen);

        if (state[socknum].recvpacketlen > 0) then begin
          fromip := inaddrvtobinip(Src);
          fromport := inttostr(htons(src.InAddr.port));
          if ((not comparebinip(toaddr[socknum],fromip)) or (fromport <> toport)) then begin
//            writeln('dnssync received from wrong IP:port ',ipbintostr(fromip),'#',fromport);
            state[socknum].recvpacketlen := 0;
          end else begin
            state[socknum].parsepacket := true;
            needprocessing[socknum] := true;
          end;
        end;
      end;
    end;
    if selectresult < 0 then exit;
    if selectresult = 0 then begin

      currenttime := getts;

      if overridednsserver = '' then reportlag(id,-1);
      if (currenttime >= endtime) and ((not wrapmode) or (currenttime < starttime)) then begin
        exit;
      end else begin
        //resend
        for socknum := numsockused-1 downto 0 do begin
          sendquery(socknum,state[socknum].sendpacket,state[socknum].sendpacketlen);
        end;
      end;
    end;
  until false;
end;
{$endif}



function forwardlookuplist(name:string;timeout:integer):tbiniplist;
var
  dummy : integer;
  a,b:integer;
  biniptemp:tbinip;
  l:tbiniplist;
begin
  ipstrtobin(name,biniptemp);
  if biniptemp.family <> 0 then begin
    result := biniplist_new;
    biniplist_add(result,biniptemp);
    exit; //it was an IP address, no need for dns
  end;

  {$ifdef win32}
  if usewindns then begin
    if (useaf = useaf_v4) then a := af_inet else if (useaf = useaf_v6) then a := af_inet6 else a := 0;
    result := winforwardlookuplist(name,a,dummy);
    {$ifdef ipv6}
    if (useaf = useaf_preferv4) then begin
      {prefer mode: sort the IP's}
      l := biniplist_new;
      addipsoffamily(l,result,af_inet);
      addipsoffamily(l,result,af_inet6);
      result := l;
    end;
    if (useaf = useaf_preferv6) then begin
      {prefer mode: sort the IP's}
      l := biniplist_new;
      addipsoffamily(l,result,af_inet6);
      addipsoffamily(l,result,af_inet);
      result := l;
    end;
    {$endif}
  end else
  {$endif}
  begin
  {$ifdef syncdnscore}
    {$ifdef ipv6}initpreferredmode;{$endif}

    numsockused := 0;

    result := biniplist_new;
    if (useaf <> useaf_v6) then begin
      setstate_forward(name,state[numsockused],af_inet);
      inc(numsockused);
    end;
    {$ifdef ipv6}
    if (useaf <> useaf_v4) then begin
      setstate_forward(name,state[numsockused],af_inet6);
      inc(numsockused);
    end;
    {$endif}

    resolveloop(timeout);

    if (numsockused = 1) then begin
      biniplist_addlist(result,state[0].resultlist);
    {$ifdef ipv6}
    end else if (useaf = useaf_preferv6) then begin
      biniplist_addlist(result,state[1].resultlist);
      biniplist_addlist(result,state[0].resultlist);
    end else begin
      biniplist_addlist(result,state[0].resultlist);
      biniplist_addlist(result,state[1].resultlist);
    {$endif}
    end;
    {$endif}
  end;
end;

function forwardlookup(name:string;timeout:integer):tbinip;
var
  listtemp:tbiniplist;
begin
  listtemp := forwardlookuplist(name,timeout);
  result := biniplist_get(listtemp,0);
end;

function reverselookup(ip:tbinip;timeout:integer):string;
var
  dummy : integer;
begin
  {$ifdef win32}
    if usewindns then begin
      result := winreverselookup(ip,dummy);
      exit;
    end;
  {$endif}
  {$ifdef syncdnscore}
  setstate_reverse(ip,state[0]);
  numsockused := 1;
  resolveloop(timeout);
  result := state[0].resultstr;
  {$endif}
end;

{$ifdef win32}
  var
    wsadata : twsadata;

  initialization
    WSAStartUp($2,wsadata);
  finalization
    WSACleanUp;
{$endif}
end.


