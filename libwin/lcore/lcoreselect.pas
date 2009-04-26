{lsocket.pas}

{io and timer code by plugwash}

{ Copyright (C) 2005 Bas Steendijk and Peter Green
  For conditions of distribution and use, see copyright notice in zlib_license.txt
  which is included in the package
  ----------------------------------------------------------------------------- }

{$ifdef fpc}
  {$ifndef ver1_0}
    {$define useinline}
  {$endif}
{$endif}

unit lcoreselect;


interface
uses
  {$ifdef VER1_0}
    linux,
  {$else}
    baseunix,unix,unixutil,
  {$endif}
  fd_utils;
var
  maxs                                  : longint    ;
  exitloopflag                          : boolean    ; {if set by app, exit mainloop}

function getfdsrmaster : fdset; {$ifdef useinline}inline;{$endif}
function getfdswmaster : fdset; {$ifdef useinline}inline;{$endif}

procedure lcoreinit;

implementation
uses
  lcore,sysutils,
  classes,pgtypes,bfifo,
  {$ifndef nosignal}
    lsignal;
  {$endif}

{$include unixstuff.inc}
{$include ltimevalstuff.inc}

const
  absoloutemaxs_select = (sizeof(fdset)*8)-1;

var
  fdreverse:array[0..absoloutemaxs_select] of tlasio;
type
  tselecteventcore=class(teventcore)
    public
      procedure processmessages; override;
      procedure messageloop; override;
      procedure exitmessageloop;override;
      procedure setfdreverse(fd : integer;reverseto : tlasio); override;
      procedure rmasterset(fd : integer;islistensocket : boolean); override;
      procedure rmasterclr(fd: integer); override;
      procedure wmasterset(fd : integer); override;
      procedure wmasterclr(fd: integer); override;
    end;

procedure processtimers;inline;
var
  tv           ,tvnow     : ttimeval ;
  currenttimer            : tltimer   ;
  temptimer               : tltimer  ;

begin
  gettimeofday(tvnow);
  currenttimer := firsttimer;
  while assigned(currenttimer) do begin
    //writeln(currenttimer.enabled);
    if tv_compare(tvnow,ttimeval(currenttimer.nextts)) and currenttimer.enabled then begin
      //if assigned(currenttimer.ontimer) then begin
      //  if currenttimer.enabled then if currenttimer.initialevent or currenttimer.initialdone then currenttimer.ontimer(currenttimer);
      //  currenttimer.initialdone := true;
      //end;
      if assigned(currenttimer.ontimer) then currenttimer.ontimer(currenttimer);
      currenttimer.nextts := timeval(tvnow);
      tv_add(ttimeval(currenttimer.nextts),currenttimer.interval);
    end;
    temptimer := currenttimer;
    currenttimer := currenttimer.nexttimer;
  end;
end;

procedure processasios(var fdsr,fdsw:fdset);//inline;
var
  currentsocket : tlasio  ;
  tempsocket    : tlasio  ;
  socketcount   : integer ; // for debugging perposes :)
  dw,bt:integer;
begin
{  inc(lcoretestcount);}

    //the message loop will exit if all lasio's and ltimer's and lsignal's are destroyed
    //if (not assigned(firstasin)) and (not assigned(firsttimer)) and (not assigned(firstsignal)) then exit;


  {------- test optimised loop}
  socketcount := 0;
  for dw := (maxs shr 5) downto 0 do if (fdsr[dw] or fdsw[dw]) <> 0 then begin
    for bt := 0 to 31 do if (fdsr[dw] or fdsw[dw]) and (1 shl bt) <> 0 then begin
      inc(socketcount);
      currentsocket := fdreverse[dw shl 5 or bt];
      {if not assigned(currentsocket) then raise exception.create('currentsocket not assigned');
      if currentsocket.fdhandlein < 0 then raise exception.create('currentsocket.fdhandlein out of range');}
      {i've seen the out of range case actually happening, so it can happen. test: just close the fd - beware}
      if not assigned(currentsocket) then begin
        fdclose(dw shl 5 or bt);
        continue
      end;
      if currentsocket.fdhandlein < 0 then begin
        fdclose(dw shl 5 or bt);
        continue
      end;
      try
        currentsocket.handlefdtrigger(fd_isset(currentsocket.fdhandlein,fdsr),fd_isset(currentsocket.fdhandleout,fdsw));
      except
        on E: exception do begin
          currentsocket.HandleBackGroundException(e);
        end;
      end;

      if mustrefreshfds then begin
        if select(maxs+1,@fdsr,@fdsw,nil,0) <= 0 then begin
          fd_zero(fdsr);
          fd_zero(fdsw);
        end;
      end;
    end;
  end;

  {
  !!! issues:
  - sockets which are released may not be freed because theyre never processed by the loop
  made new code for handling this, using asinreleaseflag

  - when/why does the mustrefreshfds select apply, sheck if i did it correctly?

  - what happens if calling handlefdtrigger for a socket which does not have an event
  }
  {------- original loop}

  (*
  currentsocket := firstasin;
  socketcount := 0;
  while assigned(currentsocket) do begin
    if mustrefreshfds then begin
      if select(maxs,@fdsr,@fdsw,nil,0) <= 0 then begin
        fd_zero(fdsr);
        fd_zero(fdsw);
      end;
    end;
    try
      if fd_isset(currentsocket.fdhandlein,fdsr) or fd_isset(currentsocket.fdhandleout,fdsw) then begin
        currentsocket.handlefdtrigger(fd_isset(currentsocket.fdhandlein,fdsr),fd_isset(currentsocket.fdhandleout,fdsw));
      end;
    except
      on E: exception do begin
        currentsocket.HandleBackGroundException(e);
      end;
    end;
    tempsocket := currentsocket;
    currentsocket := currentsocket.nextasin;
    inc(socketcount);
    if tempsocket.released then begin
      tempsocket.free;
    end;
  end; *)
{  debugout('socketcount='+inttostr(socketcount));}
end;

procedure tselecteventcore.processmessages;
var
  fdsr         , fdsw : fdset   ;
  selectresult        : longint ;
begin
  mustrefreshfds := false;
  {$ifndef nosignal}
    prepsigpipe;
  {$endif}
  selectresult := select(maxs+1,@fdsr,@fdsw,nil,0);
  while (selectresult>0) or assigned(firsttask) or assigned(currenttask) do begin;

    processtasks;
    processtimers;
    if selectresult > 0 then begin
      processasios(fdsr,fdsw);
    end;
    selectresult := select(maxs+1,@fdsr,@fdsw,nil,0);

  end;
  mustrefreshfds := true;
end;


var
  FDSR , FDSW : fdset;

var
  fdsrmaster , fdswmaster               : fdset      ;

function getfdsrmaster : fdset; {$ifdef fpc}inline;{$endif}
begin
  result := fdsrmaster;
end;
function getfdswmaster : fdset; {$ifdef fpc}inline;{$endif}
begin
  result := fdswmaster;
end;


Function  doSelect(timeOut:PTimeVal):longint;//inline;
var
  localtimeval : ttimeval;
  maxslocal    : integer;
begin
  //unblock signals
  //zeromemory(@sset,sizeof(sset));
  //sset[0] := ;
  fdsr := getfdsrmaster;
  fdsw := getfdswmaster;

  if assigned(firsttask) then begin
    localtimeval.tv_sec  := 0;
    localtimeval.tv_usec := 0;
    timeout := @localtimeval;
  end;

  maxslocal := maxs;
  mustrefreshfds := false;
{  debugout('about to call select');}
  {$ifndef nosignal}
    sigprocmask(SIG_UNBLOCK,@blockset,nil);
  {$endif}
  result := select(maxslocal+1,@FDSR,@FDSW,nil,timeout);
  if result <= 0 then begin
    fd_zero(FDSR);
    fd_zero(FDSW);
    if result=-1 then begin
      if linuxerror = SYS_EINTR then begin
        // we received a signal it's not a problem
      end else begin
        raise esocketexception.create('select returned error '+inttostr(linuxerror));
      end;
    end;
  end;
  {$ifndef nosignal}
    sigprocmask(SIG_BLOCK,@blockset,nil);
  {$endif}
{  debugout('select complete');}
end;

procedure tselecteventcore.exitmessageloop;
begin
  exitloopflag := true
end;



procedure tselecteventcore.messageloop;
var
  tv           ,tvnow     : ttimeval ;
  currenttimer            : tltimer  ;
  selectresult:integer;
begin
  {$ifndef nosignal}
    prepsigpipe;
  {$endif}
  {currentsocket := firstasin;
  if not assigned(currentsocket) then exit; //the message loop will exit if all lsockets are destroyed
  repeat

    if currentsocket.state = wsconnected then currentsocket.sendflush;
    currentsocket := currentsocket.nextasin;
  until not assigned(currentsocket);}


  repeat

    //the message loop will exit if all lasio's and ltimer's and lsignal's are destroyed
    processtasks;
    //currenttask := nil;
    {beware}
    //if assigned(firsttimer) then begin
    //  tv.tv_sec := maxlongint;
    tv := tv_invalidtimebig;
    currenttimer := firsttimer;
    while assigned(currenttimer) do begin
      if tv_compare(tv,currenttimer.nextts) and currenttimer.enabled then tv := currenttimer.nextts;
      currenttimer := currenttimer.nexttimer;
    end;


    if tv_compare(tv,tv_invalidtimebig) then begin    
      //writeln('no timers active');
      if exitloopflag then break;
{    sleep(10);}
      selectresult := doselect(nil);

    end else begin
      gettimeofday(tvnow);
      tv_substract(tv,tvnow);

      //writeln('timers active');
      if tv.tv_sec < 0 then begin
        tv.tv_sec := 0;
        tv.tv_usec := 0; {0.1 sec}
      end;
      if exitloopflag then break;
{    sleep(10);}
      selectresult := doselect(@tv);
      processtimers;

    end;
    if selectresult > 0 then processasios(fdsr,fdsw);
    {!!!only call processasios if select has asio events -beware}

    {artificial delay to throttle the number of processasios per second possible and reduce cpu usage}
  until false;
end;


procedure tselecteventcore.rmasterset(fd : integer;islistensocket : boolean);
begin
  if fd > absoloutemaxs then raise esocketexception.create('file discriptor out of range');
  if fd > maxs then maxs := fd;
  if fd_isset(fd,fdsrmaster) then exit;
  fd_set(fd,fdsrmaster);

end;

procedure tselecteventcore.rmasterclr(fd: integer);
begin
  if not fd_isset(fd,fdsrmaster) then exit;
  fd_clr(fd,fdsrmaster);

end;


procedure tselecteventcore.wmasterset(fd : integer);
begin
  if fd > absoloutemaxs then raise esocketexception.create('file discriptor out of range');
  if fd > maxs then maxs := fd;

  if fd_isset(fd,fdswmaster) then exit;
  fd_set(fd,fdswmaster);

end;

procedure tselecteventcore.wmasterclr(fd: integer);
begin
  if not fd_isset(fd,fdswmaster) then exit;
  fd_clr(fd,fdswmaster);
end;

procedure tselecteventcore.setfdreverse(fd : integer;reverseto : tlasio);
begin
  fdreverse[fd] := reverseto;
end;

var
  inited:boolean;

procedure lcoreinit;
begin
  if inited then exit;
  inited := true;
  eventcore := tselecteventcore.create;

  absoloutemaxs := absoloutemaxs_select;

  maxs := 0;
  fd_zero(fdsrmaster);
  fd_zero(fdswmaster);
end;

end.
