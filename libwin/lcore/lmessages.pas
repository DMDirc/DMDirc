{ Copyright (C) 2005 Bas Steendijk and Peter Green
  For conditions of distribution and use, see copyright notice in zlib_license.txt
  which is included in the package
  ----------------------------------------------------------------------------- }

//this unit provides a rough approximation of windows messages on linux
//it is usefull for multithreaded applications on linux to communicate back to
//the main lcore thread
//This unit is *nix only, on windows you should use the real thing

unit lmessages;
//windows messages like system based on lcore tasks
interface

uses pgtypes,sysutils,bsearchtree,strings,syncobjs;


{$if (fpc_version < 2) or ((fpc_version=2) and ((fpc_release < 2) or ((fpc_release = 2) and (fpc_patch < 2)) ))}
  {$error this code is only supported under fpc 2.2.2 and above due to bugs in the eventobject code in older versions}
{$endif}

type
  lparam=taddrint;
  wparam=taddrint;
  thinstance=pointer;
  hicon=pointer;
  hcursor=pointer;
  hbrush=pointer;
  hwnd=qword; //window handles are monotonically increasing 64 bit integers,
              //this should allow for a million windows per second for over half
              //a million years!

  twndproc=function(ahWnd:HWND; auMsg:Integer; awParam:WPARAM; alParam:LPARAM):Integer; stdcall;


  twndclass=record
    style : dword;
    lpfnwndproc : twndproc;
    cbclsextra : integer;
    cbwndextra : integer;
    hinstance : thinstance;
    hicon : hicon;
    hcursor : hcursor;
    hbrbackground : hbrush;
    lpszmenuname : pchar;
    lpszclassname : pchar;
  end;
  PWNDCLASS=^twndclass;
  
  UINT=dword;
  WINBOOL = longbool;
  tTIMERPROC = procedure (ahwnd:HWND; umsg:integer; idevent:taddrint;dwtime:taddrint);stdcall;
  ATOM = pointer;
  LPCSTR = pchar;
  LPVOID = pointer;
  HMENU = pointer;
  HINST = pointer;

  TPOINT = record 
    x : LONGint; 
    y : LONGint; 
  end; 
  
  TMSG = record 
    hwnd : HWND; 
    message : UINT; 
    wParam : WPARAM; 
    lParam : LPARAM; 
    time : DWORD; 
    pt : TPOINT;
  end; 
  THevent=TEventObject;
const
  WS_EX_TOOLWINDOW = $80;
  WS_POPUP = longint($80000000);
  hinstance=nil;
  PM_REMOVE = 1;
  WM_USER = 1024;
  WM_TIMER = 275;
  INFINITE = syncobjs.infinite;
function getwindowlongptr(ahwnd:hwnd;nindex:integer) : taddrint;
function setwindowlongptr(ahwnd:hwnd;nindex:integer;dwNewLong : taddrint) : taddrint;
function DefWindowProc(ahWnd:HWND; auMsg:Integer; awParam:WPARAM; alParam:LPARAM):Integer; stdcall;
function RegisterClass(const lpWndClass:TWNDCLASS):ATOM;
function CreateWindowEx(dwExStyle:DWORD; lpClassName:LPCSTR; lpWindowName:LPCSTR; dwStyle:DWORD; X:longint;Y:longint; nWidth:longint; nHeight:longint; hWndParent:HWND; hMenu:HMENU;hInstance:HINST; lpParam:LPVOID):HWND;
function DestroyWindow(ahWnd:HWND):WINBOOL;
function PostMessage(hWnd:HWND; Msg:UINT; wParam:WPARAM; lParam:LPARAM):WINBOOL;
function PeekMessage(var lpMsg: TMsg; hWnd: HWND; wMsgFilterMin, wMsgFilterMax, wRemoveMsg: UINT): WINBOOL;
function DispatchMessage(const lpMsg: TMsg): Longint;
function GetMessage(var lpMsg: TMsg; hWnd: HWND; wMsgFilterMin, wMsgFilterMax: UINT): WINBOOL;
function SetEvent(hEvent:THevent):WINBOOL;
function CreateEvent(lpEventAttributes:PSECURITYATTRIBUTES; bManualReset:WINBOOL; bInitialState:WINBOOL; lpName:pchar):tHevent;
function terminatethread(threadhandle : tthreadid;dummy:integer) : boolean;
function waitforsingleevent(event:thevent;timeout:cardinal) : twaitresult;
function SetTimer(ahWnd:HWND; nIDEvent:taddrint; uElapse:UINT; lpTimerFunc:tTIMERPROC):UINT;
function KillTimer(ahWnd:HWND; uIDEvent:taddrint):WINBOOL;

procedure init;

implementation
uses
  baseunix,unix,lcore,unixutil;//,safewriteln;
{$i unixstuff.inc}

type
  tmessageintransit = class
    msg : tmsg;
    next : tmessageintransit;
  end;

  tthreaddata = class
    messagequeue : tmessageintransit;
    messageevent : teventobject;
    waiting : boolean;
    lcorethread : boolean;
    nexttimer : ttimeval;
    threadid : integer;
  end;
  twindow=class
    hwnd : hwnd;
    extrawindowmemory : pointer;
    threadid : tthreadid;
    windowproc : twndproc;
  end;

var
  structurelock : tcriticalsection;
  threaddata : thashtable;
  windowclasses : thashtable;
  lcorelinkpipesend : integer;
  lcorelinkpiperecv : tlasio;
  windows : thashtable;
  //I would rather things crash immediately
  //if they use an insufficiant size type
  //than crash after over four billion
  //windows have been made ;)
  nextwindowhandle : qword = $100000000;
{$i ltimevalstuff.inc}

//findthreaddata should only be called while holding the structurelock
function findthreaddata(threadid : integer) : tthreaddata;
begin
  result := tthreaddata(findtree(@threaddata,inttostr(threadid)));
  if result = nil then begin
    result := tthreaddata.create;
    result.messageevent := teventobject.create(nil,false,false,inttostr(taddrint(result)));
    result.nexttimer := tv_invalidtimebig;
    result.threadid := threadid;
    addtree(@threaddata,inttostr(threadid),result);
  end;
end;

//deletethreaddataifunused should only be called while holding the structurelock
procedure deletethreaddataifunused(athreaddata : tthreaddata);
begin
  //writeln('in deletethreaddataifunused');
  if (athreaddata <> nil) then if (athreaddata.waiting=false) and (athreaddata.messagequeue=nil) and (athreaddata.lcorethread=false) and (athreaddata.nexttimer.tv_sec=tv_invalidtimebig.tv_sec) and (athreaddata.nexttimer.tv_usec=tv_invalidtimebig.tv_usec) then begin
    //writeln('threaddata is unused, freeing messageevent');
    athreaddata.messageevent.free;
    //writeln('freeing thread data object');
    athreaddata.free;
    //writeln('deleting thread data object from hashtable');
    deltree(@threaddata,inttostr(athreaddata.threadid));
    //writeln('finished deleting thread data');
  end else begin
    //writeln('thread data is not unused');
  end;
end;

function getwindowlongptr(ahwnd:hwnd;nindex:integer) : taddrint;
var
  window : twindow;
begin
  structurelock.acquire;
  try
    window := findtree(@windows,inttostr(ahwnd));
    if window <> nil then begin
      result := paddrint(taddrint(window.extrawindowmemory)+nindex)^;
    end else begin
      result := 0;
    end;
  finally
    structurelock.release;
  end;
end;

function setwindowlongptr(ahwnd:hwnd;nindex:integer;dwNewLong : taddrint) : taddrint;
var
  window : twindow;
begin
  structurelock.acquire;
  try
    window := findtree(@windows,inttostr(ahwnd));
    if window <> nil then begin
      result := paddrint(taddrint(window.extrawindowmemory)+nindex)^;
      paddrint(taddrint(window.extrawindowmemory)+nindex)^ := dwnewlong;
    end else begin
      result := 0;
    end;
  finally
    structurelock.release;
  end;

end;


function DefWindowProc(ahWnd:HWND; auMsg:Integer; awParam:WPARAM; alParam:LPARAM):Integer; stdcall;
begin
  result := 0;
end;

function strdup(s:pchar) : pchar;
begin
  //swriteln('in strdup, about to allocate memory');
  result := getmem(strlen(s)+1);
  //swriteln('about to copy string');
  strcopy(s,result);
  //swriteln('leaving strdup');
end;

function RegisterClass(const lpWndClass:TWNDCLASS):ATOM;
var
  storedwindowclass:pwndclass;
begin
  structurelock.acquire;
  try
    //swriteln('in registerclass, about to check for duplicate window class');
    storedwindowclass := findtree(@windowclasses, lpwndclass.lpszclassname);
    if storedwindowclass <> nil then begin

      if comparebyte(storedwindowclass^,lpwndclass,sizeof(twndclass)-sizeof(pchar)-sizeof(pchar)) <> 0 then begin
        //swriteln('duplicate window class registered with different settings');
        raise exception.create('duplicate window class registered with different settings');
      end else begin
        //swriteln('duplicate window class registered with same settings, tollerated');
      end;
    end else begin
      //swriteln('about to allocate memory for new windowclass');
      storedwindowclass := getmem(sizeof(twndclass));
      //swriteln('about to copy windowclass from parameter');
      move(lpwndclass,storedwindowclass^,sizeof(twndclass));
      //swriteln('about to copy strings');
      if lpwndclass.lpszmenuname <> nil then storedwindowclass.lpszmenuname := strdup(lpwndclass.lpszmenuname);
      if lpwndclass.lpszclassname <> nil then storedwindowclass.lpszclassname := strdup(lpwndclass.lpszclassname);
      //swriteln('about to add result to list of windowclasses');
      addtree(@windowclasses,lpwndclass.lpszclassname,storedwindowclass);
    end;
    //swriteln('about to return result');
    result := storedwindowclass;
    //swriteln('leaving registerclass');
  finally
    structurelock.release;
  end;
end;

function CreateWindowEx(dwExStyle:DWORD; lpClassName:LPCSTR; lpWindowName:LPCSTR; dwStyle:DWORD; X:longint;Y:longint; nWidth:longint; nHeight:longint; hWndParent:HWND; hMenu:HMENU;hInstance:HINST; lpParam:LPVOID):HWND;
var
  wndclass : pwndclass;
  tm : tthreadmanager;
  window : twindow;
begin
  structurelock.acquire;
  try
    window := twindow.create;
    window.hwnd := nextwindowhandle;
    result := window.hwnd;
    nextwindowhandle := nextwindowhandle + 1;
    addtree(@windows,inttostr(window.hwnd),window);
    wndclass := findtree(@windowclasses,lpclassname);
    window.extrawindowmemory := getmem(wndclass.cbwndextra);

    getthreadmanager(tm);
    window.threadid := tm.GetCurrentThreadId;
    window.windowproc := wndclass.lpfnwndproc;
  finally
    structurelock.release;
  end;
end;
function DestroyWindow(ahWnd:HWND):WINBOOL;
var
  window : twindow;
  windowthreaddata : tthreaddata;
  currentmessage : tmessageintransit;
  prevmessage : tmessageintransit;
begin
  //writeln('started to destroy window');
  structurelock.acquire;
  try
    window := twindow(findtree(@windows,inttostr(ahwnd)));
    if window <> nil then begin
      freemem(window.extrawindowmemory);
      //writeln('aboute to delete window from windows structure');
      deltree(@windows,inttostr(ahwnd));
      //writeln('deleted window from windows structure');
      windowthreaddata := tthreaddata(findtree(@threaddata,inttostr(window.threadid)));

      if windowthreaddata <> nil then begin
        //writeln('found thread data scanning for messages to clean up');
        currentmessage := windowthreaddata.messagequeue;
        prevmessage := nil;
        while currentmessage <> nil do begin
          while (currentmessage <> nil) and (currentmessage.msg.hwnd = ahwnd) do begin
            if prevmessage = nil then begin
              windowthreaddata.messagequeue := currentmessage.next;
            end else begin
              prevmessage.next := currentmessage.next;
            end;
            currentmessage.free;
            if prevmessage = nil then begin
              currentmessage := windowthreaddata.messagequeue;
            end else begin
              currentmessage := prevmessage.next;
            end;
          end;
          if currentmessage <> nil then begin
            prevmessage := currentmessage;
            currentmessage := currentmessage.next;
          end;
        end;
        //writeln('deleting thread data structure if it is unused');
        deletethreaddataifunused(windowthreaddata);
      end else begin
        //writeln('there is no thread data to search for messages to cleanup');
      end;
      //writeln('freeing window');
      window.free;
      result := true;
    end else begin
      result := false;
    end;
  finally
    structurelock.release;
  end;
  //writeln('window destroyed');
end;



function PostMessage(hWnd:HWND; Msg:UINT; wParam:WPARAM; lParam:LPARAM):WINBOOL;
var
  threaddata : tthreaddata;
  message : tmessageintransit;
  messagequeueend : tmessageintransit;
  window : twindow;
begin
  structurelock.acquire;
  try
    window := findtree(@windows,inttostr(hwnd));
    if window <> nil then begin
      threaddata := findthreaddata(window.threadid);
      message := tmessageintransit.create;
      message.msg.hwnd := hwnd;
      message.msg.message := msg;
      message.msg.wparam := wparam;
      message.msg.lparam := lparam;
      if threaddata.lcorethread then begin
        //swriteln('posting message to lcore thread');
        fdwrite(lcorelinkpipesend,message,sizeof(message));
      end else begin
        //writeln('posting message to non lcore thread');
        if threaddata.messagequeue = nil then begin
          threaddata.messagequeue := message;
        end else begin
          messagequeueend := threaddata.messagequeue;
          while messagequeueend.next <> nil do begin
            messagequeueend := messagequeueend.next;
          end;
          messagequeueend.next := message;
        end;

        //writeln('message added to queue');
        if threaddata.waiting then threaddata.messageevent.setevent;
      end;
      result := true;
    end else begin
      result := false;
    end;
  finally
    structurelock.release;
  end;

end;

function gettickcount : dword;
var
  result64: integer;
  tv : ttimeval;
begin
  gettimeofday(tv);
  result64 := (tv.tv_sec*1000)+(tv.tv_usec div 1000);
  result := result64;
end;

function DispatchMessage(const lpMsg: TMsg): Longint;
var
  timerproc : ttimerproc;
  window : twindow;
  windowproc : twndproc;
begin
  ////swriteln('in dispatchmessage, msg.hwnd='+inttohex(taddrint(lpmsg.hwnd),16));
  if (lpmsg.lparam <> 0) and (lpmsg.message = WM_TIMER) then begin
    timerproc := ttimerproc(lpmsg.lparam);
    timerproc(lpmsg.hwnd,lpmsg.message,lpmsg.wparam,gettickcount);
    result := 0;
  end else begin
    structurelock.acquire;
    try
      window := findtree(@windows,inttostr(lpmsg.hwnd));
      //we have to get the window procedure while the structurelock
      //is still held as the window could be destroyed from another thread
      //otherwise.
      if window <> nil then begin
        windowproc := window.windowproc;
      end else begin
        windowproc := nil;
      end;
    finally
      structurelock.release;
    end;
    if assigned(windowproc) then begin
      result := windowproc(lpmsg.hwnd,lpmsg.message,lpmsg.wparam,lpmsg.lparam);
    end else begin
      result := -1;
    end;
  end;
end;

procedure processtimers;
begin
end;

function GetMessageinternal(var lpMsg: TMsg; hWnd: HWND; wMsgFilterMin, wMsgFilterMax: UINT; wremovemsg : UINT;peek:boolean): WINBOOL;
var
  tm : tthreadmanager;
  threaddata : tthreaddata;
  message : tmessageintransit;
  nowtv : ttimeval;
  timeouttv : ttimeval;
  timeoutms : int64;

begin
  if hwnd <> 0 then raise exception.create('getting messages for an individual window is not supported');
  if (wmsgfiltermin <> 0) or (wmsgfiltermax <> 0) then raise exception.create('message filtering is not supported');
  structurelock.acquire;
  result := true;
  try
    getthreadmanager(tm);
    threaddata := findthreaddata(tm.GetCurrentThreadId);
    if threaddata.lcorethread then raise exception.create('get/peek message cannot be used in the lcore thread');
    message := threaddata.messagequeue;
    gettimeofday(nowtv);
    while (not peek) and (message=nil) and (not tv_compare(nowtv,threaddata.nexttimer)) do begin
      threaddata.waiting := true;
      structurelock.release;
      if (threaddata.nexttimer.tv_sec = TV_invalidtimebig.tv_sec) and (threaddata.nexttimer.tv_usec = TV_invalidtimebig.tv_usec) then begin
        threaddata.messageevent.waitfor(INFINITE);
      end else begin

        timeouttv := threaddata.nexttimer;
        timeoutms := (timeouttv.tv_sec * 1000)+(timeouttv.tv_usec div 1000);
        //i'm assuming the timeout is in milliseconds
        if (timeoutms > maxlongint) then timeoutms := maxlongint;
        threaddata.messageevent.waitfor(timeoutms);

      end;
      structurelock.acquire;
      threaddata.waiting := false;
      message := threaddata.messagequeue;
      gettimeofday(nowtv);
    end;
    if (message=nil) and tv_compare(nowtv,threaddata.nexttimer) then begin
      processtimers;
    end;
    message := threaddata.messagequeue;
    if message <> nil then begin
      lpmsg := message.msg;
      if wremovemsg=PM_REMOVE then begin
        threaddata.messagequeue := message.next;
        message.free;
      end;
    end else begin
      result :=false;
    end;
    deletethreaddataifunused(threaddata);
  finally
    structurelock.release;
  end;
end;

function GetMessage(var lpMsg: TMsg; hWnd: HWND; wMsgFilterMin, wMsgFilterMax: UINT): WINBOOL;
begin
  result := getmessageinternal(lpmsg,hwnd,wmsgfiltermin,wmsgfiltermax,PM_REMOVE,false);
end;

function PeekMessage(var lpMsg: TMsg; hWnd: HWND; wMsgFilterMin, wMsgFilterMax, wRemoveMsg: UINT): WINBOOL;
begin
  result := getmessageinternal(lpmsg,hwnd,wmsgfiltermin,wmsgfiltermax,wRemoveMsg,true);
end;

function SetEvent(hEvent:THevent):WINBOOL;
begin
  hevent.setevent;
  result := true;
end;

function CreateEvent(lpEventAttributes:PSECURITYATTRIBUTES; bManualReset:WINBOOL; bInitialState:WINBOOL; lpName:pchar):tHevent;
begin
  result := teventobject.create(lpeventattributes,bmanualreset,binitialstate,lpname);
end;

function terminatethread(threadhandle:tthreadid;dummy : integer) : boolean;
var
  tm : tthreadmanager;
begin
  getthreadmanager(tm);
  tm.killthread(threadhandle);
  result := true;
end;

function waitforsingleevent(event:thevent;timeout:cardinal) : twaitresult;
begin
  result := event.waitfor(timeout);
end;

procedure removefrombuffer(n : integer; var buffer:string);
begin
  if n=length(buffer) then begin
    buffer := '';
  end else begin
    uniquestring(buffer);
    move(buffer[n+1],buffer[1],length(buffer)-n);
    setlength(buffer,length(buffer)-n);
  end;
end;

type
  tsc=class
    procedure available(sender:tobject;error:word);
  end;

var
  recvbuf : string;

procedure tsc.available(sender:tobject;error:word);
var
  message : tmessageintransit;
  messagebytes : array[1..sizeof(tmessageintransit)] of char absolute  message;
  i : integer;
begin
  //swriteln('received data on lcorelinkpipe');
  recvbuf := recvbuf + lcorelinkpiperecv.receivestr;
  while length(recvbuf) >= sizeof(tmessageintransit) do begin
    for i := 1 to sizeof(tmessageintransit) do begin
      messagebytes[i] := recvbuf[i];
    end;
    dispatchmessage(message.msg);
    message.free;
    removefrombuffer(sizeof(tmessageintransit),recvbuf);
  end;
end;

procedure init;
var
  tm : tthreadmanager;
  threaddata : tthreaddata;
  pipeends : tfildes;
  sc : tsc;
begin
  structurelock := tcriticalsection.create;
  getthreadmanager(tm);
  threaddata := findthreaddata(tm.GetCurrentThreadId);
  threaddata.lcorethread := true;
  fppipe(pipeends);
  lcorelinkpipesend := pipeends[1];
  lcorelinkpiperecv := tlasio.create(nil);
  lcorelinkpiperecv.dup(pipeends[0]);
  lcorelinkpiperecv.ondataavailable := sc.available;
  recvbuf := '';
end;

var
  lcorethreadtimers : thashtable;
type
  tltimerformsg = class(tltimer)
  public
    hwnd : hwnd;
    id : taddrint;
    procedure timer(sender : tobject);
  end;

procedure tltimerformsg.timer(sender : tobject);
var
  msg : tmsg;
begin
  ////swriteln('in tltimerformsg.timer');
  fillchar(msg,sizeof(msg),0);
  msg.message := WM_TIMER;
  msg.hwnd := hwnd;
  msg.wparam := ID;
  msg.lparam := 0;
  dispatchmessage(msg);
end;

function SetTimer(ahWnd:HWND; nIDEvent:taddrint; uElapse:UINT; lpTimerFunc:tTIMERPROC):UINT;
var
  threaddata : tthreaddata;
  ltimer : tltimerformsg;
  tm : tthreadmanager;
  window : twindow;
begin
  structurelock.acquire;
  try
    window := findtree(@windows,inttostr(ahwnd));
    if window= nil then raise exception.create('invalid window');
    threaddata := findthreaddata(window.threadid);
  finally
    structurelock.release;
  end;
  if threaddata.lcorethread then begin
    getthreadmanager(tm);
    if tm.GetCurrentThreadId <> window.threadid then raise exception.create('timers on the lcore thread may only be added and removed from the lcore thread');
    if ahwnd = 0 then raise exception.create('timers on the lcore thread must be associated with a window handle');
    if @lptimerfunc <> nil then raise exception.create('seperate timer functions are not supported');

    //remove preexisting timer with same ID
    killtimer(ahwnd,nIDEvent);

    ltimer := tltimerformsg.create(nil);
    ltimer.interval := uelapse;
    ltimer.id := nidevent;
    ltimer.hwnd := ahwnd;
    ltimer.enabled := true;
    ltimer.ontimer := ltimer.timer;

    addtree(@lcorethreadtimers,inttostr(taddrint(ahwnd))+' '+inttostr(nIDEvent),ltimer);

    result := nidevent;
  end else begin
    raise exception.create('settimer not implemented for threads other than the lcore thread');
  end;
end;

function KillTimer(ahWnd:HWND; uIDEvent:taddrint):WINBOOL;
var
  threaddata : tthreaddata;
  ltimer : tltimerformsg;
  tm : tthreadmanager;
  window : twindow;
begin
  structurelock.acquire;
  try
    window := findtree(@windows,inttostr(ahwnd));
    if window= nil then raise exception.create('invalid window');
    threaddata := findthreaddata(window.threadid);
  finally
    structurelock.release;
  end;
  if threaddata.lcorethread then begin
    getthreadmanager(tm);
    if tm.GetCurrentThreadId <> window.threadid then raise exception.create('timers on the lcore thread may only be added and remove from the lcore thread');
    if ahwnd = 0 then raise exception.create('timers on the lcore thread must be associated with a window handle');
    ltimer := tltimerformsg(findtree(@lcorethreadtimers,inttostr(taddrint(ahwnd))+' '+inttostr(uIDEvent)));
    if ltimer <> nil then begin
      deltree(@lcorethreadtimers,inttostr(taddrint(ahwnd))+' '+inttostr(uIDEvent));
      ltimer.free;
      result := true;
    end else begin
      result := false;
    end;
  end else begin
    raise exception.create('settimer not implemented for threads other than the lcore thread');
  end;
end;

end.
