{ Copyright (C) 2005 Bas Steendijk and Peter Green
  For conditions of distribution and use, see copyright notice in zlib_license.txt
  which is included in the package
  ----------------------------------------------------------------------------- }

//FIXME: this code only ever seems to use one dns server for a request and does
//not seem to have any form of retry code.

unit dnsasync;

interface

uses
  {$ifdef win32}
    dnswin,
  {$endif}
  lsocket,lcore,
  classes,binipstuff,dnscore,btime,lcorernd;

{$include lcoreconfig.inc}

const
  numsock=1{$ifdef ipv6}+1{$endif};

type

  //after completion or cancelation a dnswinasync may be reused
  tdnsasync=class(tcomponent)

  private
    //made a load of stuff private that does not appear to be part of the main
    //public interface. If you make any of it public again please consider the
    //consequences when using windows dns. --plugwash.
    sockets: array[0..numsock-1] of tlsocket;

    states: array[0..numsock-1] of tdnsstate;

    destinations: array[0..numsock-1] of tbinip;

    dnsserverids : array[0..numsock-1] of integer;
    startts:double;
    {$ifdef win32}
      dwas : tdnswinasync;
    {$endif}

    numsockused : integer;
    fresultlist : tbiniplist;
    requestaf : integer;
    procedure asyncprocess(socketno:integer);
    procedure receivehandler(sender:tobject;error:word);
    function sendquery(socketno:integer;const packet:tdnspacket;len:integer):boolean;
    {$ifdef win32}
      procedure winrequestdone(sender:tobject;error:word);
    {$endif}

  public
    onrequestdone:tsocketevent;

    //addr and port allow the application to specify a dns server specifically
    //for this dnsasync object. This is not a reccomended mode of operation
    //because it limits the app to one dns server but is kept for compatibility
    //and special uses.
    addr,port:string;

    overrideaf : integer;

    procedure cancel;//cancel an outstanding dns request
    function dnsresult:string; //get result of dnslookup as a string
    procedure dnsresultbin(var binip:tbinip); //get result of dnslookup as a tbinip
    property dnsresultlist : tbiniplist read fresultlist;
    procedure forwardlookup(const name:string); //start forward lookup,
                                                //preffering ipv4
    procedure reverselookup(const binip:tbinip); //start reverse lookup
    procedure customlookup(const name:string;querytype:integer); //start custom type lookup

    constructor create(aowner:tcomponent); override;
    destructor destroy; override;

  end;

implementation

uses sysutils;

constructor tdnsasync.create;
begin
  inherited create(aowner);
  dnsserverids[0] := -1;
  sockets[0] := twsocket.create(self);
  sockets[0].tag := 0;
  {$ifdef ipv6}
    dnsserverids[1] := -1;
    sockets[1] := twsocket.Create(self);
    sockets[1].tag := 1;
  {$endif}
end;

destructor tdnsasync.destroy;
var
  socketno : integer;
begin
  for socketno := 0 to numsock -1 do begin
    if dnsserverids[socketno] >= 0 then begin
      reportlag(dnsserverids[socketno],-1);
      dnsserverids[socketno] := -1;
    end;
    sockets[socketno].release;
    setstate_request_init('',states[socketno]);
  end;
  inherited destroy;
end;

procedure tdnsasync.receivehandler(sender:tobject;error:word);
var
  socketno : integer;
  Src    : TInetSockAddrV;
  SrcLen : Integer;
  fromip:tbinip;
  fromport:string;
begin
  socketno := tlsocket(sender).tag;
  //writeln('got a reply on socket number ',socketno);
  fillchar(states[socketno].recvpacket,sizeof(states[socketno].recvpacket),0);

  SrcLen := SizeOf(Src);
  states[socketno].recvpacketlen := twsocket(sender).ReceiveFrom(@(states[socketno].recvpacket), SizeOf(states[socketno].recvpacket), Src, SrcLen);

  fromip := inaddrvtobinip(Src);
  fromport := inttostr(htons(src.InAddr.port));

  if ((not comparebinip(fromip,destinations[socketno])) or (fromport <> port)) then begin
   // writeln('dnsasync received from wrong IP:port ',ipbintostr(fromip),'#',fromport,', expected ',ipbintostr(destinations[socketno]),'#',port);
    exit;
  end;

  states[socketno].parsepacket := true;
  if states[socketno].resultaction <> action_done then begin
    //we ignore packets that come after we are done
    if dnsserverids[socketno] >= 0 then begin
      reportlag(dnsserverids[socketno],trunc((unixtimefloat-startts)*1000000));
      dnsserverids[socketno] := -1;
    end;
  {  writeln('received reply');}

    asyncprocess(socketno);
    //writeln('processed it');
  end else begin
    //writeln('ignored it because request is done');
  end;
end;

function tdnsasync.sendquery(socketno:integer;const packet:tdnspacket;len:integer):boolean;
var
  destination : string;
  inaddr : tinetsockaddrv;
  trytolisten:integer;
begin
{  writeln('sendquery ',decodename(state.packet,state.packetlen,12,0,a),' ',state.requesttype);}
  //writeln('trying to send query on socket number ',socketno);
  result := false;
  if len = 0 then exit; {no packet}
  if sockets[socketno].state <> wsconnected then begin
    startts := unixtimefloat;
    if port = '' then port := '53';
    sockets[socketno].Proto := 'udp';
    sockets[socketno].ondataavailable := receivehandler;

    {we are going to bind on a random local port for the DNS request, against the kaminsky attack
    there is a small chance that we're trying to bind on an already used port, so retry a few times}
    for trytolisten := 3 downto 0 do begin
      try
        sockets[socketno].port := inttostr(1024 + randominteger(65536 - 1024));
        sockets[socketno].listen;
      except
        {writeln('failed to listen ',sockets[socketno].localport,' ',trytolisten);}
        if (trytolisten = 0) then begin
          result := false;
          exit;
        end;
      end;
    end;

  end;
  if addr <> '' then begin
    dnsserverids[socketno] := -1;
    destination := addr
  end else begin
    destination := getcurrentsystemnameserver(dnsserverids[socketno]);
  end;
  destinations[socketno] := ipstrtobinf(destination);

  {$ifdef ipv6}{$ifdef win32}
  if destinations[socketno].family = AF_INET6 then if (requestaf = useaf_default) then requestaf := useaf_preferv6;
  {$endif}{$endif}

  makeinaddrv(destinations[socketno],port,inaddr);
  sockets[socketno].sendto(inaddr,sizeof(inaddr), @packet,len);
  result := true;


end;

procedure tdnsasync.asyncprocess(socketno:integer);
begin
  state_process(states[socketno]);
  case states[socketno].resultaction of
    action_ignore: begin {do nothing} end;
    action_done: begin
      {$ifdef ipv6}
      if (numsockused = 1) or (states[socketno xor 1].resultaction=action_done) then
      //if using two sockets we need to wait until both sockets are in the done
      //state before firing the event
      {$endif}
      begin
        fresultlist := biniplist_new;
        if (numsockused = 1) then begin
          //writeln('processing for one state');
          biniplist_addlist(fresultlist,states[0].resultlist);
        {$ifdef ipv6}
        end else if (requestaf = useaf_preferv6) then begin
          //writeln('processing for two states, ipv6 preference');
          //writeln('merging lists '+biniplist_tostr(states[1].resultlist)+' and '+biniplist_tostr(states[0].resultlist));
          biniplist_addlist(fresultlist,states[1].resultlist);
          biniplist_addlist(fresultlist,states[0].resultlist);
        end else begin
          //writeln('processing for two states, ipv4 preference');
          biniplist_addlist(fresultlist,states[0].resultlist);
          biniplist_addlist(fresultlist,states[1].resultlist);
        {$endif}
        end;
        //writeln(biniplist_tostr(fresultlist));
        onrequestdone(self,0);
      end;
    end;
    action_sendquery:begin
      sendquery(socketno,states[socketno].sendpacket,states[socketno].sendpacketlen);
    end;
  end;
end;

procedure tdnsasync.forwardlookup;
var
  bip : tbinip;
  i : integer;
begin
  ipstrtobin(name,bip);

  if bip.family <> 0 then begin
    // it was an IP address
    fresultlist := biniplist_new;
    biniplist_add(fresultlist,bip);
    onrequestdone(self,0);
    exit;
  end;

  if (overridednsserver <> '') and (addr = '') then addr := overridednsserver;

  if overrideaf = useaf_default then begin
    {$ifdef ipv6}
      {$ifdef win32}if not (usewindns and (addr = '')) then{$endif}
      initpreferredmode;
    {$endif}
    requestaf := useaf;
  end else begin
    requestaf := overrideaf;
  end;

  {$ifdef win32}
    if usewindns and (addr = '') then begin
      dwas := tdnswinasync.create;
      dwas.onrequestdone := winrequestdone;

      dwas.forwardlookup(name);

      exit;
    end;
  {$endif}

  numsockused := 0;
  fresultlist := biniplist_new;
  if (requestaf <> useaf_v6) then begin
    setstate_forward(name,states[numsockused],af_inet);
    inc(numsockused);
  end;

  {$ifdef ipv6}
    if (requestaf <> useaf_v4) then begin
      setstate_forward(name,states[numsockused],af_inet6);
      inc(numsockused);
    end;
  {$endif}
  for i := 0 to numsockused-1 do begin
    asyncprocess(i);
  end;

end;

procedure tdnsasync.reverselookup;
begin
  if (overridednsserver <> '') and (addr = '') then addr := overridednsserver;
  {$ifdef win32}
    if usewindns and (addr = '') then begin
      dwas := tdnswinasync.create;
      dwas.onrequestdone := winrequestdone;
      dwas.reverselookup(binip);
      exit;
    end;
  {$endif}

  setstate_reverse(binip,states[0]);
  numsockused := 1;
  asyncprocess(0);
end;

procedure tdnsasync.customlookup;
begin
  if (overridednsserver <> '') and (addr = '') then addr := overridednsserver;
  setstate_custom(name,querytype,states[0]);
  numsockused := 1;
  asyncprocess(0);
end;

function tdnsasync.dnsresult;
begin
  if states[0].resultstr <> '' then result := states[0].resultstr else begin
    result := ipbintostr(biniplist_get(fresultlist,0));
  end;
end;

procedure tdnsasync.dnsresultbin(var binip:tbinip);
begin
  binip := biniplist_get(fresultlist,0);
end;

procedure tdnsasync.cancel;
var
  socketno : integer;
begin
  {$ifdef win32}
    if assigned(dwas) then begin
      dwas.release;
      dwas := nil;
    end else
  {$endif}
  begin
    for socketno := 0 to numsock-1 do begin
      reportlag(dnsserverids[socketno],-1);
      dnsserverids[socketno] := -1;

      sockets[socketno].close;
    end;

  end;
  for socketno := 0 to numsock-1 do begin
    setstate_failure(states[socketno]);

  end;
  fresultlist := biniplist_new;
  onrequestdone(self,0);
end;

{$ifdef win32}
  procedure tdnsasync.winrequestdone(sender:tobject;error:word);
 
  begin
    if dwas.reverse then begin
      states[0].resultstr := dwas.name;
    end else begin 

      {$ifdef ipv6}
      if (requestaf = useaf_preferv4) then begin
        {prefer mode: sort the IP's}
        fresultlist := biniplist_new;
        addipsoffamily(fresultlist,dwas.iplist,af_inet);
        addipsoffamily(fresultlist,dwas.iplist,af_inet6);

      end else if (requestaf = useaf_preferv6) then begin
        {prefer mode: sort the IP's}
        fresultlist := biniplist_new;
        addipsoffamily(fresultlist,dwas.iplist,af_inet6);
        addipsoffamily(fresultlist,dwas.iplist,af_inet);
        
      end else
      {$endif}
      begin
        fresultlist := dwas.iplist;
      end;

    end;
    dwas.release;
    onrequestdone(self,error);
  end;
{$endif}
end.
