{lsocket.pas}

{io and timer code by plugwash}

{ Copyright (C) 2005 Bas Steendijk and Peter Green
  For conditions of distribution and use, see copyright notice in zlib_license.txt
  which is included in the package
  ----------------------------------------------------------------------------- }

{note: you must use the @ in the last param to tltask.create not doing so will
 compile without error but will cause an access violation -pg}

//note: events after release are normal and are the apps responsibility to deal with safely

unit lcore;
{$ifdef fpc}
  {$mode delphi}
{$endif}
{$ifdef win32}
  {$define nosignal}
{$endif}
interface
  uses
    sysutils,
    {$ifndef win32}
      {$ifdef VER1_0}
        linux,
      {$else}
        baseunix,unix,unixutil,
      {$endif}
      fd_utils,
    {$endif}
    classes,pgtypes,bfifo;
  procedure processtasks;


  const
    {how this number is made up:
    - ethernet: MTU 1500
    - be safe for either "ethernet v1" or "PPPoE", both take 8 bytes
    - IPv6 header: 40 bytes (IPv4 is 20)
    - TCP/UDP header: 20 bytes
    }
    packetbasesize = 1432;
    receivebufsize=packetbasesize*8;

  var
    absoloutemaxs:integer=0;

  type
    {$ifdef ver1_0}
      sigset= array[0..31] of longint;
    {$endif}

    ESocketException   = class(Exception);
    TBgExceptionEvent  = procedure (Sender : TObject;
                                  E : Exception;
                                  var CanClose : Boolean) of object;

    // note : tsocketstate is defined in the same way as it is in François PIETTE's twsocket
    // however tlsocket currently only uses wsClosed wsConnecting wsconnected and wsListening
    TSocketState       = (wsInvalidState,
                        wsOpened,     wsBound,
                        wsConnecting, wsConnected,
                        wsAccepting,  wsListening,
                        wsClosed);

    TWSocketOption       = (wsoNoReceiveLoop, wsoTcpNoDelay);
    TWSocketOptions      = set of TWSocketOption;

    TSocketevent     = procedure(Sender: TObject; Error: word) of object;
    //Tdataavailevent  = procedure(data : string);
    TSendData          = procedure (Sender: TObject; BytesSent: Integer) of object;

    tlcomponent = class(tcomponent)
    private
      procedure releasetaskhandler(wparam,lparam:longint);
    public
      procedure release; virtual;
      destructor destroy; override;
    end;

    tlasio = class(tlcomponent)
    public
      state              : tsocketstate      ;
      ComponentOptions   : TWSocketOptions;
      fdhandlein         : Longint           ;  {file discriptor}
      fdhandleout        : Longint           ;  {file discriptor}

      onsessionclosed    : tsocketevent      ;
      ondataAvailable    : tsocketevent      ;
      onsessionAvailable : tsocketevent      ;

      onsessionconnected : tsocketevent      ;
      onsenddata         : tsenddata      ;
      ondatasent         : tsocketevent      ;
      //connected          : boolean         ;

      recvq              : tfifo;
      OnBgException      : TBgExceptionEvent ;
      //connectread        : boolean           ;
      sendq              : tfifo;
      closehandles       : boolean           ;
      writtenthiscycle   : boolean           ;
      onfdwrite           : procedure (Sender: TObject; Error: word) of object; //added for bewarehttpd
      lasterror:integer;
      destroying:boolean;
      recvbufsize:integer;
      function receivestr:string; virtual;
      procedure close;
      procedure abort;
      procedure internalclose(error:word); virtual;
      constructor Create(AOwner: TComponent); override;

      destructor destroy; override;
      procedure fdcleanup;
      procedure HandleBackGroundException(E: Exception);
      procedure handlefdtrigger(readtrigger,writetrigger:boolean); virtual;
      procedure dup(invalue:longint);

      function sendflush : integer;
      procedure sendstr(const str : string);virtual;
      procedure putstringinsendbuffer(const newstring : string);
      function send(data:pointer;len:integer):integer;virtual;
      procedure putdatainsendbuffer(data:pointer;len:integer); virtual;
      procedure deletebuffereddata;

      //procedure messageloop;
      function Receive(Buf:Pointer;BufSize:integer):integer; virtual;
      procedure flush;virtual;
      procedure dodatasent(wparam,lparam:longint);
      procedure doreceiveloop(wparam,lparam:longint);
      procedure sinkdata(sender:tobject;error:word);

      procedure release; override; {test -beware}

      function RealSend(Data : Pointer; Len : Integer) : Integer; //added for bewarehttpd

      procedure myfdclose(fd : integer); virtual;{$ifdef win32}abstract;{$endif}
      function myfdwrite(fd: LongInt;const buf;size: LongInt):LongInt; virtual;{$ifdef win32}abstract;{$endif}
      function myfdread(fd: LongInt;var buf;size: LongInt):LongInt; virtual;{$ifdef win32}abstract;{$endif}
    protected
      procedure dupnowatch(invalue:longint);
    end;
    ttimerwrapperinterface=class(tlcomponent)
    public
      function createwrappedtimer : tobject;virtual;abstract;
//      procedure setinitialevent(wrappedtimer : tobject;newvalue : boolean);virtual;abstract;
      procedure setontimer(wrappedtimer : tobject;newvalue:tnotifyevent);virtual;abstract;
      procedure setenabled(wrappedtimer : tobject;newvalue : boolean);virtual;abstract;
      procedure setinterval(wrappedtimer : tobject;newvalue : integer);virtual;abstract;
    end;

  var
    timerwrapperinterface : ttimerwrapperinterface;
  type
    {$ifdef win32}
      ttimeval = record
        tv_sec : longint;
        tv_usec : longint;
      end;
    {$endif}
    tltimer=class(tlcomponent)
    protected


      wrappedtimer : tobject;


//      finitialevent       : boolean           ;
      fontimer            : tnotifyevent      ;
      fenabled            : boolean           ;
      finterval	          : integer	     ; {miliseconds, default 1000}
      {$ifndef win32}
        procedure resettimes;
      {$endif}
//      procedure setinitialevent(newvalue : boolean);
      procedure setontimer(newvalue:tnotifyevent);
      procedure setenabled(newvalue : boolean);
      procedure setinterval(newvalue : integer);
    public
      //making theese public for now, this code should probablly be restructured later though
      prevtimer          : tltimer           ;
      nexttimer          : tltimer           ;
      nextts	         : ttimeval          ;

      constructor create(aowner:tcomponent);override;
      destructor destroy;override;
//      property initialevent : boolean read finitialevent write setinitialevent;
      property ontimer : tnotifyevent read fontimer write setontimer;
      property enabled : boolean read fenabled write setenabled;
      property interval	: integer read finterval write setinterval;

    end;

    ttaskevent=procedure(wparam,lparam:longint) of object;

    tltask=class(tobject)
    public
      handler  : ttaskevent;
      obj      : tobject;
      wparam   : longint;
      lparam   : longint;
      nexttask : tltask;
      constructor create(ahandler:ttaskevent;aobj:tobject;awparam,alparam:longint);
    end;



    teventcore=class
    public
      procedure processmessages; virtual;abstract;
      procedure messageloop; virtual;abstract;
      procedure exitmessageloop; virtual;abstract;
      procedure setfdreverse(fd : integer;reverseto : tlasio);virtual;abstract;
      procedure rmasterset(fd : integer;islistensocket : boolean);  virtual;abstract;
      procedure rmasterclr(fd: integer);  virtual;abstract;
      procedure wmasterset(fd : integer); virtual;abstract;
      procedure wmasterclr(fd: integer);  virtual;abstract;
    end;
var
    eventcore : teventcore;

procedure processmessages;
procedure messageloop;
procedure exitmessageloop;

var
  firsttimer                            : tltimer    ;
  firsttask  , lasttask   , currenttask : tltask     ;

  numread                               : integer    ;
  mustrefreshfds                        : boolean    ;
{  lcoretestcount:integer;}

  asinreleaseflag:boolean;


procedure disconnecttasks(aobj:tobject);
procedure addtask(ahandler:ttaskevent;aobj:tobject;awparam,alparam:longint);
type
  tonaddtask = procedure(ahandler:ttaskevent;aobj:tobject;awparam,alparam:longint);
var
  onaddtask : tonaddtask;


procedure sleep(i:integer);
{$ifndef nosignal}
  procedure prepsigpipe;{$ifndef ver1_0}inline;{$endif}
{$endif}


implementation
{$ifndef nosignal}
  uses {sockets,}lloopback,lsignal;
{$endif}
{$ifdef win32}
  uses windows,winsock;
{$endif}
{$ifndef win32}
  {$include unixstuff.inc}
{$endif}
{$include ltimevalstuff.inc}


{!!! added sleep call -beware}
procedure sleep(i:integer);
var
  tv:ttimeval;
begin
  {$ifdef win32}
    windows.sleep(i);
  {$else}
    tv.tv_sec := i div 1000;
    tv.tv_usec := (i mod 1000) * 1000;
    select(0,nil,nil,nil,@tv);
  {$endif}
end;

destructor tlcomponent.destroy;
begin
  disconnecttasks(self);
  inherited destroy;
end;

procedure tlcomponent.releasetaskhandler(wparam,lparam:longint);
begin
  free;
end;


procedure tlcomponent.release;
begin
  addtask(releasetaskhandler,self,0,0);
end;

procedure tlasio.release;
begin
  asinreleaseflag := true;
  inherited release;
end;

procedure tlasio.doreceiveloop;
begin
  if recvq.size = 0 then exit;
  if assigned(ondataavailable) then ondataavailable(self,0);
  if not (wsonoreceiveloop in componentoptions) then
  if recvq.size > 0 then tltask.create(self.doreceiveloop,self,0,0);
end;

function tlasio.receivestr;
begin
  setlength(result,recvq.size);
  receive(@result[1],length(result));
end;

function tlasio.receive(Buf:Pointer;BufSize:integer):integer;
var
  i,a,b:integer;
  p:pointer;
begin
  i := bufsize;
  if recvq.size < i then i := recvq.size;
  a := 0;
  while (a < i) do begin
    b := recvq.get(p,i-a);
    move(p^,buf^,b);
    inc(taddrint(buf),b);
    recvq.del(b);
    inc(a,b);
  end;
  result := i;
  if wsonoreceiveloop in componentoptions then begin
    if recvq.size = 0 then eventcore.rmasterset(fdhandlein,false);
  end;
end;

constructor tlasio.create;
begin
  inherited create(AOwner);
  if not assigned(eventcore) then raise exception.create('no event core');
  sendq := tfifo.create;
  recvq := tfifo.create;
  state := wsclosed;
  fdhandlein := -1;
  fdhandleout := -1;
end;

destructor tlasio.destroy;
begin
  destroying := true;
  if state <> wsclosed then close;
  recvq.free;
  sendq.free;
  inherited destroy;
end;

procedure tlasio.close;
begin
  internalclose(0);
end;

procedure tlasio.abort;
begin
  close;
end;

procedure tlasio.fdcleanup;
begin
  if fdhandlein <> -1 then begin
    eventcore.rmasterclr(fdhandlein); //fd_clr(fdhandlein,fdsrmaster)
  end;
  if fdhandleout <> -1 then begin
    eventcore.wmasterclr(fdhandleout);//fd_clr(fdhandleout,fdswmaster)
  end;
  if fdhandlein=fdhandleout then begin
    if fdhandlein <> -1 then begin
      myfdclose(fdhandlein);
    end;
  end else begin
    if fdhandlein <> -1 then begin
      myfdclose(fdhandlein);
    end;
    if fdhandleout <> -1 then begin
      myfdclose(fdhandleout);
    end;
  end;
  fdhandlein := -1;
  fdhandleout := -1;
end;

procedure tlasio.internalclose(error:word);
begin
  if (state<>wsclosed) and (state<>wsinvalidstate) then begin
    // -2 is a special indication that we should just exist silently
    // (used for connect failure handling when socket creation fails)
    if (fdhandlein = -2) and (fdhandleout = -2) then exit;
    if (fdhandlein < 0) or (fdhandleout < 0) then raise exception.create('internalclose called with invalid fd handles');
    eventcore.rmasterclr(fdhandlein);//fd_clr(fdhandlein,fdsrmaster);
    eventcore.wmasterclr(fdhandleout);//fd_clr(fdhandleout,fdswmaster);

    if closehandles then begin
      {$ifndef win32}
        //anyone remember why this is here? --plugwash
        fcntl(fdhandlein,F_SETFL,0);
      {$endif}
      myfdclose(fdhandlein);
      if fdhandleout <> fdhandlein then begin
        {$ifndef win32}
          fcntl(fdhandleout,F_SETFL,0);
        {$endif}
        myfdclose(fdhandleout);
      end;
      eventcore.setfdreverse(fdhandlein,nil);
      eventcore.setfdreverse(fdhandleout,nil);

      fdhandlein := -1;
      fdhandleout := -1;
    end;
    state := wsclosed;

    if assigned(onsessionclosed) then if not destroying then onsessionclosed(self,error);
  end;
  if assigned(sendq) then sendq.del(maxlongint);
end;


{* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *}
{ All exceptions *MUST* be handled. If an exception is not handled, the     }
{ application will most likely be shut down !                               }
procedure tlasio.HandleBackGroundException(E: Exception);
var
  CanAbort : Boolean;
begin
  CanAbort := TRUE;
  { First call the error event handler, if any }
  if Assigned(OnBgException) then begin
    try
      OnBgException(Self, E, CanAbort);
    except
    end;
  end;
  { Then abort the socket }
  if CanAbort then begin
    try
      close;
    except
    end;
  end;
end;

procedure tlasio.sendstr(const str : string);
begin
  putstringinsendbuffer(str);
  sendflush;
end;

procedure tlasio.putstringinsendbuffer(const newstring : string);
begin
  if newstring <> '' then putdatainsendbuffer(@newstring[1],length(newstring));
end;

function tlasio.send(data:pointer;len:integer):integer;
begin
  if state <> wsconnected then begin
    result := -1;
    exit;
  end;
  if len < 0 then len := 0;
  result := len;
  putdatainsendbuffer(data,len);
  sendflush;
end;


procedure tlasio.putdatainsendbuffer(data:pointer;len:integer);
begin
  sendq.add(data,len);
end;

function tlasio.sendflush : integer;
var
  lensent : integer;
  data:pointer;
//  fdstestr : fdset;
//  fdstestw : fdset;
begin
  if state <> wsconnected then begin
    result := -1;
    exit;
  end;

  lensent := sendq.get(data,packetbasesize*2);
  if assigned(data) then result := myfdwrite(fdhandleout,data^,lensent) else result := 0;

  if result = -1 then lensent := 0 else lensent := result;

  //sendq := copy(sendq,lensent+1,length(sendq)-lensent);
  sendq.del(lensent);

  //fd_clr(fdhandleout,fdsw); // this prevents the socket being closed by a write
                            // that sends nothing because a previous socket has
                            // slready flushed this socket when the message loop
                            // reaches it
//  if sendq.size > 0 then begin
    eventcore.wmasterset(fdhandleout);//fd_set(fdhandleout,fdswmaster);
//  end else begin
//    wmasterclr(fdhandleout);//fd_clr(fdhandleout,fdswmaster);
//  end;
  if result > 0 then begin
    if assigned(onsenddata) then onsenddata(self,result);
//    if sendq.size=0 then if assigned(ondatasent) then begin
//      tltask.create(self.dodatasent,self,0,0);
//      //begin test code
//      fd_zero(fdstestr);
//      fd_zero(fdstestw);
//      fd_set(fdhandlein,fdstestr);
//      fd_set(fdhandleout,fdstestw);
//      select(maxs,@fdstestr,@fdstestw,nil,0);
//      writeln(fd_isset(fdhandlein,fdstestr),' ',fd_isset(fdhandleout,fdstestw));
//      //end test code
//    
//    end;
    writtenthiscycle := true;
  end;
end;

procedure tlasio.dupnowatch(invalue:longint);
begin
  {  debugout('invalue='+inttostr(invalue));}
  //readln;
  if state<> wsclosed then close;
  fdhandlein := invalue;
  fdhandleout := invalue;
  eventcore.setfdreverse(fdhandlein,self);
  {$ifndef win32}
    fcntl(fdhandlein,F_SETFL,OPEN_NONBLOCK);
  {$endif}
  state := wsconnected;

end;


procedure tlasio.dup(invalue:longint);
begin
  dupnowatch(invalue);
  eventcore.rmasterset(fdhandlein,false);//fd_set(fdhandlein,fdsrmaster);
  eventcore.wmasterclr(fdhandleout);//fd_clr(fdhandleout,fdswmaster);
end;


procedure tlasio.handlefdtrigger(readtrigger,writetrigger:boolean);
var
  sendflushresult : integer;
  tempbuf:array[0..receivebufsize-1] of byte;
  a:integer;
begin
  if (state=wsconnected) and writetrigger then begin
    //writeln('write trigger');

    if (sendq.size >0) then begin

      sendflushresult := sendflush;
      if (sendflushresult <= 0) and (not writtenthiscycle) then begin
        if sendflushresult=0 then begin // linuxerror := 0;
          internalclose(0);

        end else begin
          {$ifdef win32}
          if getlasterror=WSAEWOULDBLOCK then begin
            //the asynchronous nature of windows messages means we sometimes
            //get here with the buffer full
            //so do nothing in that case
          end else
          {$endif}
          begin
            internalclose({$ifdef win32}getlasterror{$else}linuxerror{$endif});
          end  
        end;
      end;

    end else begin
      //everything is sent fire off ondatasent event
      if fdhandleout >= 0 then eventcore.wmasterclr(fdhandleout);//fd_clr(fdhandleout,fdswmaster);
      if assigned(ondatasent) then tltask.create(self.dodatasent,self,0,0);
    end;
    if assigned(onfdwrite) then onfdwrite(self,0);
  end;
  writtenthiscycle := false;
  if (state =wsconnected) and readtrigger then begin
    if recvq.size=0 then begin
      a := recvbufsize;
      if (a <= 0) or (a > sizeof(tempbuf)) then a := sizeof(tempbuf);
      numread := myfdread(fdhandlein,tempbuf,a);
      if (numread=0) and (not mustrefreshfds) then begin
        {if i remember correctly numread=0 is caused by eof
        if this isn't dealt with then you get a cpu eating infinite loop
        however if onsessionconencted has called processmessages that could
        cause us to drop to here with an empty recvq and nothing left to read
        and we don't want that to cause the socket to close}

        internalclose(0);
      end else if (numread=-1) then begin
        {$ifdef win32}
          //sometimes on windows we get stale messages due to the inherent delays
          //in the windows message queue
          if WSAGetLastError = wsaewouldblock then begin
            //do nothing
          end else
        {$endif}
        begin
          numread := 0;
          internalclose({$ifdef win32}wsagetlasterror{$else}linuxerror{$endif});
        end;
      end else if numread > 0 then recvq.add(@tempbuf,numread);
    end;

    if recvq.size > 0 then begin
      if wsonoreceiveloop in componentoptions then eventcore.rmasterclr(fdhandlein); //fd_clr(fdhandlein,fdsrmaster);
      if assigned(ondataavailable) then ondataAvailable(self,0);
      if not (wsonoreceiveloop in componentoptions) then if recvq.size > 0 then
      tltask.create(self.doreceiveloop,self,0,0);
    end;
    //until (numread = 0) or (currentsocket.state<>wsconnected);
{    debugout('inner loop complete');}
  end;
end;

procedure tlasio.flush;
{$ifdef win32}
type fdset = tfdset;
{$endif}
var
  fds : fdset;
begin
  fd_zero(fds);
  fd_set(fdhandleout,fds);
  while sendq.size>0 do begin
    select(fdhandleout+1,nil,@fds,nil,nil);
    if sendflush <= 0 then exit;
  end;
end;

procedure tlasio.dodatasent(wparam,lparam:longint);
begin
  if assigned(ondatasent) then ondatasent(self,lparam);
end;

procedure tlasio.deletebuffereddata;
begin
  sendq.del(maxlongint);
end;

procedure tlasio.sinkdata(sender:tobject;error:word);
begin
  tlasio(sender).recvq.del(maxlongint);
end;

{$ifndef win32}
  procedure tltimer.resettimes;
  begin
    gettimeofday(nextts);
    {if not initialevent then} tv_add(nextts,interval);
  end;
{$endif}

{procedure tltimer.setinitialevent(newvalue : boolean);
begin
  if newvalue <> finitialevent then begin
    finitialevent := newvalue;
    if assigned(timerwrapperinterface) then begin
      timerwrapperinterface.setinitialevent(wrappedtimer,newvalue);
    end else begin
      resettimes;
    end;
  end;
end;}

procedure tltimer.setontimer(newvalue:tnotifyevent);
begin
  if @newvalue <> @fontimer then begin
    fontimer := newvalue;
    if assigned(timerwrapperinterface) then begin
      timerwrapperinterface.setontimer(wrappedtimer,newvalue);
    end else begin

    end;
  end;

end;


procedure tltimer.setenabled(newvalue : boolean);
begin
  if newvalue <> fenabled then begin
    fenabled := newvalue;
    if assigned(timerwrapperinterface) then begin
      timerwrapperinterface.setenabled(wrappedtimer,newvalue);
    end else begin
      {$ifdef win32}
        raise exception.create('non wrapper timers are not permitted on windows');
      {$else}
        resettimes;
      {$endif}
    end;
  end;
end;

procedure tltimer.setinterval(newvalue:integer);
begin
  if newvalue <> finterval then begin
    finterval := newvalue;
    if assigned(timerwrapperinterface) then begin
      timerwrapperinterface.setinterval(wrappedtimer,newvalue);
    end else begin
      {$ifdef win32}
        raise exception.create('non wrapper timers are not permitted on windows');
      {$else}
        resettimes;
      {$endif}
    end;
  end;

end;




constructor tltimer.create;
begin
  inherited create(AOwner);
  if assigned(timerwrapperinterface) then begin
    wrappedtimer := timerwrapperinterface.createwrappedtimer;
  end else begin


    nexttimer := firsttimer;
    prevtimer := nil;

    if assigned(nexttimer) then nexttimer.prevtimer := self;
    firsttimer := self;
  end;
  interval := 1000;
  enabled := true;
end;

destructor tltimer.destroy;
begin
  if assigned(timerwrapperinterface) then begin
    wrappedtimer.free;
  end else begin
    if prevtimer <> nil then begin
      prevtimer.nexttimer := nexttimer;
    end else begin
      firsttimer := nexttimer;
    end;
    if nexttimer <> nil then begin
      nexttimer.prevtimer := prevtimer;
    end;
    
  end;
  inherited destroy;
end;

constructor tltask.create(ahandler:ttaskevent;aobj:tobject;awparam,alparam:longint);
begin
  inherited create;
  if assigned(onaddtask) then onaddtask(ahandler,aobj,awparam,alparam);
  handler   := ahandler;
  obj       := aobj;
  wparam    := awparam;
  lparam    := alparam;
  {nexttask  := firsttask;
  firsttask := self;}
  if assigned(lasttask) then begin
    lasttask.nexttask := self;
  end else begin
    firsttask := self;
  end;
  lasttask := self;
  //ahandler(wparam,lparam);
end;

procedure addtask(ahandler:ttaskevent;aobj:tobject;awparam,alparam:longint);
begin

  tltask.create(ahandler,aobj,awparam,alparam);
end;

{$ifndef nosignal}
  procedure prepsigpipe;{$ifndef ver1_0}inline;
{$endif}
  begin
    starthandlesignal(sigpipe);
    if not assigned(signalloopback) then begin
      signalloopback := tlloopback.create(nil);
      signalloopback.ondataAvailable := signalloopback.sinkdata;

    end;

  end;
{$endif}

procedure processtasks;//inline;
var
  temptask                : tltask   ;

begin

  if not assigned(currenttask) then begin
    currenttask := firsttask;
    firsttask := nil;
    lasttask  := nil;
  end;
  while assigned(currenttask) do begin

    if assigned(currenttask.handler) then currenttask.handler(currenttask.wparam,currenttask.lparam);
    if assigned(currenttask) then begin
      temptask := currenttask;
      currenttask := currenttask.nexttask;
      temptask.free;
    end;
    //writeln('processed a task');
  end;

end;




procedure disconnecttasks(aobj:tobject);
var
  currenttasklocal : tltask ;
  counter          : byte   ;
begin
  for counter := 0 to 1 do begin
    if counter = 0 then begin
      currenttasklocal := firsttask; //main list of tasks
    end else begin
      currenttasklocal := currenttask; //needed in case called from a task
    end;
    // note i don't bother to sestroy the links here as that will happen when
    // the list of tasks is processed anyway
    while assigned(currenttasklocal) do begin
      if currenttasklocal.obj = aobj then begin
        currenttasklocal.obj := nil;
        currenttasklocal.handler := nil;
      end;
      currenttasklocal := currenttasklocal.nexttask;
    end;
  end;
end;


procedure processmessages;
begin
  eventcore.processmessages;
end;
procedure messageloop;
begin
  eventcore.messageloop;
end;

procedure exitmessageloop;
begin
  eventcore.exitmessageloop;
end;

function tlasio.RealSend(Data : Pointer; Len : Integer) : Integer;
begin
  result := myfdwrite(fdhandleout,data^,len);
  if (result > 0) and assigned(onsenddata) then onsenddata(self,result);
  eventcore.wmasterset(fdhandleout);
end;
{$ifndef win32}
  procedure tlasio.myfdclose(fd : integer);
  begin
    fdclose(fd);
  end;
  function tlasio.myfdwrite(fd: LongInt;const buf;size: LongInt):LongInt;
  begin
    result := fdwrite(fd,buf,size);
  end;

  function tlasio.myfdread(fd: LongInt;var buf;size: LongInt):LongInt;
  begin
    result := fdread(fd,buf,size);
  end;


{$endif}


begin
  firsttask := nil;
  

  {$ifndef nosignal}
    signalloopback := nil;
  {$endif}
end.





