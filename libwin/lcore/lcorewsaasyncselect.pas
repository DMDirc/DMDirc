unit lcorewsaasyncselect;

interface

procedure lcoreinit;

implementation

uses wcore,lcore,bsearchtree,sysutils,windows,winsock,pgtypes,messages,classes,lsocket;
type
  twineventcore=class(teventcore)
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
const
  wm_dotasks=wm_user+1;
type
  twintimerwrapperinterface=class(ttimerwrapperinterface)
  public
    function createwrappedtimer : tobject;override;
//    procedure setinitialevent(wrappedtimer : tobject;newvalue : boolean);override;
    procedure setontimer(wrappedtimer : tobject;newvalue:tnotifyevent);override;
    procedure setenabled(wrappedtimer : tobject;newvalue : boolean);override;
    procedure setinterval(wrappedtimer : tobject;newvalue : integer);override;
  end;

procedure twineventcore.processmessages;
begin
  wcore.processmessages;//pass off to wcore
end;
procedure twineventcore.messageloop;
begin
  wcore.messageloop; //pass off to wcore
end;
procedure twineventcore.exitmessageloop;
begin
  wcore.exitmessageloop;
end;
var
  fdreverse : thashtable;
  fdwatches : thashtable;

procedure twineventcore.setfdreverse(fd : integer;reverseto : tlasio);
begin
  if findtree(@fdreverse,inttostr(fd)) <> nil then deltree(@fdreverse,inttostr(fd));
  if reverseto <> nil then addtree(@fdreverse,inttostr(fd),reverseto);
end;

var
  hwndlcore : hwnd;
procedure dowsaasyncselect(fd:integer; leventadd: integer; leventremove : integer);
var
  leventold : integer;
  leventnew : integer;
  wsaaresult : integer;
begin
  leventold := taddrint(findtree(@fdwatches,inttostr(fd)));
  leventnew := leventold or leventadd;
  leventnew := leventnew and not leventremove;
  if leventold <> leventnew then begin
    if leventold <> 0 then deltree(@fdwatches,inttostr(fd));
    if leventnew <> 0 then addtree(@fdwatches,inttostr(fd),pointer(leventnew));
  end;
  wsaaresult := wsaasyncselect(fd,hwndlcore,wm_user,leventnew);

end;


//to allow detection of errors:
//if we are asked to monitor for read or accept we also monitor for close
//if we are asked to monitor for write we also monitor for connect


procedure twineventcore.rmasterset(fd : integer;islistensocket : boolean);
begin
  if islistensocket then begin
//    writeln('setting accept watch for socket number ',fd);
    dowsaasyncselect(fd,FD_ACCEPT or FD_CLOSE,0);
  end else begin
//    writeln('setting read watch for socket number',fd);
    dowsaasyncselect(fd,FD_READ or FD_CLOSE,0);
  end;
end;
procedure twineventcore.rmasterclr(fd: integer);
begin
  //writeln('clearing read of accept watch for socket number ',fd);
  dowsaasyncselect(fd,0,FD_ACCEPT or FD_READ or FD_CLOSE);
end;
procedure twineventcore.wmasterset(fd : integer);
begin
  dowsaasyncselect(fd,FD_WRITE or FD_CONNECT,0);
end;

procedure twineventcore.wmasterclr(fd: integer);
begin
  dowsaasyncselect(fd,0,FD_WRITE or FD_CONNECT);
end;

var
  tasksoutstanding : boolean;

function MyWindowProc(
    ahWnd   : HWND;
    auMsg   : Integer;
    awParam : WPARAM;
    alParam : LPARAM): Integer; stdcall;
var
  socket : integer;
  event : integer;
  error : integer;
  readtrigger : boolean;
  writetrigger : boolean;
  lasio : tlasio;
begin
//  writeln('got a message');
  Result := 0;  // This means we handled the message
  if (ahwnd=hwndlcore) and (aumsg=wm_user) then begin
//    writeln('it appears to be a response to our wsaasyncselect');
    socket := awparam;
    event := alparam and $FFFF;
    error := alparam shr 16;
//    writeln('socket=',socket,' event=',event,' error=',error);
    readtrigger := false;
    writetrigger := false;
    lasio := findtree(@fdreverse,inttostr(socket));
    if assigned(lasio) then begin
      if (error <> 0) or ((event and FD_CLOSE) <> 0) then begin
        if (lasio.state = wsconnecting) and (error <> 0) then begin
          if lasio is tlsocket then tlsocket(lasio).connectionfailedhandler(error)
        end else begin
          lasio.internalclose(error);
        end;
      end else begin
        if (event and (FD_READ or FD_ACCEPT)) <> 0 then readtrigger := true;
        if (event and (FD_WRITE)) <> 0 then writetrigger := true;

        if readtrigger or writetrigger then lasio.handlefdtrigger(readtrigger,writetrigger);
      end;
      // don't reset the event manually for listen sockets to avoid unwanted
      // extra onsessionavailible events
      if (taddrint(findtree(@fdwatches,inttostr(socket))) and (FD_ACCEPT)) = 0 then dowsaasyncselect(socket,0,0); // if not a listen socket reset watches
    end;
  end else if (ahwnd=hwndlcore) and (aumsg=wm_dotasks) then begin
      //writeln('processing tasks');
      tasksoutstanding := false;
      processtasks;
  end else begin
      //writeln('passing unknown message to defwindowproc');
      //not passing unknown messages on to defwindowproc will cause window
      //creation to fail! --plugwash
      Result := DefWindowProc(ahWnd, auMsg, awParam, alParam)
  end;

end;

procedure winaddtask(ahandler:ttaskevent;aobj:tobject;awparam,alparam:longint);
begin
  if not tasksoutstanding then PostMessage(hwndlcore,wm_dotasks,0,0);
end;
type
  twcoretimer = wcore.tltimer;

function twintimerwrapperinterface.createwrappedtimer : tobject;
begin
  result := twcoretimer.create(nil);
end;
procedure twintimerwrapperinterface.setontimer(wrappedtimer : tobject;newvalue:tnotifyevent);
begin
  twcoretimer(wrappedtimer).ontimer := newvalue;
end;
procedure twintimerwrapperinterface.setenabled(wrappedtimer : tobject;newvalue : boolean);
begin
  twcoretimer(wrappedtimer).enabled := newvalue;
end;


procedure twintimerwrapperinterface.setinterval(wrappedtimer : tobject;newvalue : integer);
begin
  twcoretimer(wrappedtimer).interval := newvalue;
end;

var
  MyWindowClass : TWndClass = (style         : 0;
                                 lpfnWndProc   : @MyWindowProc;
                                 cbClsExtra    : 0;
                                 cbWndExtra    : 0;
                                 hInstance     : 0;
                                 hIcon         : 0;
                                 hCursor       : 0;
                                 hbrBackground : 0;
                                 lpszMenuName  : nil;
                                 lpszClassName : 'lcoreClass');
  GInitData: TWSAData;

var
  inited:boolean;
procedure lcoreinit;
begin
  if (inited) then exit;

  eventcore := twineventcore.create;
  if Windows.RegisterClass(MyWindowClass) = 0 then halt;
  //writeln('about to create lcore handle, hinstance=',hinstance);
  hwndlcore := CreateWindowEx(WS_EX_TOOLWINDOW,
                               MyWindowClass.lpszClassName,
                               '',        { Window name   }
                               WS_POPUP,  { Window Style  }
                               0, 0,      { X, Y          }
                               0, 0,      { Width, Height }
                               0,         { hWndParent    }
                               0,         { hMenu         }
                               HInstance, { hInstance     }
                               nil);      { CreateParam   }
  //writeln('lcore hwnd is ',hwndlcore);
  //writeln('last error is ',GetLastError);
  onaddtask := winaddtask;
  timerwrapperinterface := twintimerwrapperinterface.create(nil);

  WSAStartup(2, GInitData);
  absoloutemaxs := maxlongint;


  inited := true;
end;

end.
