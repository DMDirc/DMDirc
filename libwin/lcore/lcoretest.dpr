program lcoretest;

uses
  lcore,
  lsocket,
  {$ifdef win32}
    lcorewsaasyncselect in 'lcorewsaasyncselect.pas',
  {$else}
    lcoreselect,
  {$endif}
  dnsasync,
  binipstuff,
  sysutils,
  dnssync
  //we don't actually make any use of the units below in this app, we just
  //include it to check if it compiles ok ;)
  {$ifndef win32}
    ,
    {$ifndef nomessages}
      lmessages,
      unitwindowobject,
    {$endif}
    unitfork
  {$endif}
  ;
{$ifdef win32}
  {$R *.RES}
{$endif}

type
  tsc=class
    procedure sessionavailable(sender: tobject;error : word);
    procedure dataavailable(sender: tobject;error : word);
    procedure sessionconnected(sender: tobject;error : word);
    procedure taskrun(wparam,lparam:longint);
    procedure timehandler(sender:tobject);
    procedure dnsrequestdone(sender:tobject;error : word);
    procedure sessionclosed(sender:tobject;error : word);
  end;
  treleasetest=class(tlcomponent)
    destructor destroy; override;
  end;
var
  listensocket : tlsocket;
  serversocket : tlsocket;
  clientsocket : tlsocket;
  sc : tsc;
  task : tltask;
  firststage : boolean;
procedure tsc.sessionavailable(sender: tobject;error : word);
begin
  writeln('received connection');
  serversocket.dup(listensocket.accept);
end;

var
  receivebuf : string;
  receivecount : integer;
procedure tsc.dataavailable(sender: tobject;error : word);
var
  receiveddata : string;
  receivedon : string;
  line : string;
begin
  receiveddata := tlsocket(sender).receivestr;
  if sender=clientsocket then begin
    receivedon := 'client socket';
  end else begin
    receivedon := 'server socket';
  end;
  writeln('received data '+receiveddata+' on '+receivedon);

  receivebuf := receivebuf+receiveddata;

  if receivebuf = 'hello world' then begin
    receivebuf := '';
    writeln('received hello world creating task');
    task := tltask.create(sc.taskrun,nil,0,0);
  end;
  receivecount := receivecount +1;
  if receivecount >50 then begin
    writeln('received over 50 bits of data, pausing to let the operator take a look');
    
    receivecount := 0;
  end;
  while pos(#10,receivebuf) > 0 do begin
    line := receivebuf;
    setlength(line,pos(#10,receivebuf)-1);
    receivebuf := copy(receivebuf,pos(#10,receivebuf)+1,1000000);
    if uppercase(copy(line,1,4))='PING' then begin
      line[2] := 'o';
      writeln('send pong:'+line);
      clientsocket.sendstr(line+#10);
    end;
  end;
end;

procedure tsc.sessionconnected(sender: tobject;error : word);
begin

  if error=0 then begin
    writeln('session is connected, local address is'+clientsocket.getxaddr);

    if firststage then begin
      clientsocket.sendstr('hello world');
    end else begin
      clientsocket.sendstr('nick test'#13#10'user x x x x'#13#10);
    end;
  end else begin
    writeln('connect failed');
  end;
end;

var
  das : tdnsasync;

procedure tsc.taskrun(wparam,lparam:longint);
var
  tempbinip : tbinip;
  dummy : integer;
begin
  writeln('task ran');
  writeln('closing client socket');
  clientsocket.close;

  writeln('looking up irc.p10link.net using dnsasync');
  das := tdnsasync.Create(nil);
  das.onrequestdone := sc.dnsrequestdone;
  //das.forwardfamily := af_inet6;
  das.forwardlookup('irc.p10link.net');

end;

procedure tsc.dnsrequestdone(sender:tobject;error : word);
var
  tempbinip : tbinip;
  tempbiniplist : tbiniplist;
begin
  writeln('irc.p10link.net resolved to '+das.dnsresult+' connecting client socket there');
  das.dnsresultbin(tempbinip);
  tempbiniplist := biniplist_new;
  biniplist_add(tempbiniplist,tempbinip);
  clientsocket.addr := tempbiniplist;
  clientsocket.port := '6667';
  firststage := false;
  clientsocket.connect;
  //writeln(clientsocket.getxaddr);
  das.free;
end;

procedure tsc.timehandler(sender:tobject);
begin
  //writeln('got timer event');
end;

destructor treleasetest.destroy;
begin
  writeln('releasetest.destroy called');
  inherited destroy;
end;

procedure tsc.sessionclosed(sender:tobject;error : word);
begin
  Writeln('session closed with error ',error);
end;
var
  timer : tltimer;
  ipbin : tbinip;
  dummy : integer;
  iplist : tbiniplist;
  releasetest : treleasetest;
begin
  lcoreinit;
  releasetest := treleasetest.create(nil);
  releasetest.release;
  
  ipbin := forwardlookup('invalid.domain',5);
  writeln(ipbintostr(ipbin));

  ipbin := forwardlookup('p10link.net',5);
  writeln(ipbintostr(ipbin));

  ipstrtobin('80.68.89.68',ipbin);
  writeln('80.68.89.68 reverses to '+reverselookup(ipbin,5));

  ipstrtobin('2001:200::8002:203:47ff:fea5:3085',ipbin);
  writeln('2001:200::8002:203:47ff:fea5:3085 reverses to '+reverselookup(ipbin,5));
  writeln('creating and setting up listen socket');
  listensocket := tlsocket.create(nil);
  listensocket.addr := '';
  listensocket.port := '12345';
  listensocket.onsessionavailable := sc.sessionavailable;
  writeln('listening');
  listensocket.listen;
  writeln(listensocket.getxport);
  writeln('listen socket is number ', listensocket.fdhandlein);
  writeln('creating and setting up server socket');
  serversocket := tlsocket.create(nil);
  serversocket.ondataavailable := sc.dataavailable;
  writeln('creating and setting up client socket');
  clientsocket := tlsocket.create(nil);
  //try connecting to ::1 first and if that fails try 127.0.0.1
  iplist := biniplist_new;
  ipstrtobin('::1',ipbin);
  biniplist_add(iplist,ipbin);
  ipstrtobin('127.0.0.1',ipbin);
  biniplist_add(iplist,ipbin);
  clientsocket.addr := iplist;
  clientsocket.port := '12345';
  clientsocket.onsessionconnected := sc.sessionconnected;
  clientsocket.ondataAvailable := sc.dataavailable;
  clientsocket.onsessionclosed := sc.sessionclosed;
  writeln('connecting');
  firststage := true;
  clientsocket.connect;
  writeln('client socket is number ',clientsocket.fdhandlein);
  writeln('creating and setting up timer');
  timer := tltimer.create(nil);
  timer.interval := 1000;
  timer.ontimer := sc.timehandler;
  timer.enabled := true;
  writeln('entering message loop');
  messageloop;
  writeln('exiting cleanly');
end.
