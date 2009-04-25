{ Copyright (C) 2005 Bas Steendijk and Peter Green
  For conditions of distribution and use, see copyright notice in zlib_license.txt
  which is included in the package
  ----------------------------------------------------------------------------- }

unit wcore;

{
lcore compatible interface for windows

- messageloop

- tltimer

}
//note: events after release are normal and are the apps responsibility to deal with safely
interface

  uses
    classes,windows,mmsystem;

  type
    float=double;

    tlcomponent = class(tcomponent)
    public
      released:boolean;
      procedure release;
      destructor destroy; override;
    end;

    tltimer=class(tlcomponent)
    private
      fenabled : boolean;
      procedure setenabled(newvalue : boolean);
    public
      ontimer:tnotifyevent;
      initialevent:boolean;
      initialdone:boolean;
      prevtimer:tltimer;
      nexttimer:tltimer;
      interval:integer;        {miliseconds, default 1000}
      nextts:integer;
      property enabled:boolean read fenabled write setenabled;
      constructor create(aowner:tcomponent);override;
      destructor destroy;override;
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

procedure messageloop;
procedure addtask(ahandler:ttaskevent;aobj:tobject;awparam,alparam:longint);
procedure disconnecttasks(aobj:tobject);
procedure exitmessageloop;
procedure processmessages;

var
  onshutdown:procedure(s:string);

implementation

uses
  {$ifdef fpc}
  bmessages;
  {$else}
  messages;
  {$endif}


const
  WINMSG_TASK=WM_USER;

var
  hwndwcore:hwnd;
  firsttimer:tltimer;
  timesubstract:integer;
  firsttask,lasttask,currenttask:tltask;

procedure tlcomponent.release;
begin
  released := true;
end;

destructor tlcomponent.destroy;
begin
  disconnecttasks(self);
  inherited destroy;
end;

{------------------------------------------------------------------------------}

procedure tltimer.setenabled(newvalue : boolean);
begin
  fenabled := newvalue;
  nextts := 0;
  initialdone := false;
end;

constructor tltimer.create;
begin
  inherited create(AOwner);
  nexttimer := firsttimer;
  prevtimer := nil;

  if assigned(nexttimer) then nexttimer.prevtimer := self;
  firsttimer := self;

  interval := 1000;
  enabled := true;
  released := false;
end;

destructor tltimer.destroy;
begin
  if prevtimer <> nil then begin
    prevtimer.nexttimer := nexttimer;
  end else begin
    firsttimer := nexttimer;
  end;
  if nexttimer <> nil then begin
    nexttimer.prevtimer := prevtimer;
  end;
  inherited destroy;
end;

{------------------------------------------------------------------------------}

function wcore_timehandler:integer;
const
  rollover_bits=30;
var
  tv,tvnow:integer;
  currenttimer,temptimer:tltimer;
begin
  if not assigned(firsttimer) then begin
    result := 1000;
    exit;
  end;

  tvnow := timegettime;
  if (tvnow and ((-1) shl rollover_bits)) <> timesubstract then begin
    currenttimer := firsttimer;
    while assigned(currenttimer) do begin
      dec(currenttimer.nextts,(1 shl rollover_bits));
      currenttimer := currenttimer.nexttimer;
    end;
    timesubstract := tvnow and ((-1) shl rollover_bits);
  end;
  tvnow := tvnow and ((1 shl rollover_bits)-1);

  currenttimer := firsttimer;
  while assigned(currenttimer) do begin
    if tvnow >= currenttimer.nextts then begin
      if assigned(currenttimer.ontimer) then begin
        if currenttimer.enabled then begin
          if currenttimer.initialevent or currenttimer.initialdone then currenttimer.ontimer(currenttimer);
          currenttimer.initialdone := true;
        end;
      end;
      currenttimer.nextts := tvnow+currenttimer.interval;
    end;
    temptimer := currenttimer;
    currenttimer := currenttimer.nexttimer;
    if temptimer.released then temptimer.free;
  end;

  tv := maxlongint;
  currenttimer := firsttimer;
  while assigned(currenttimer) do begin
    if currenttimer.nextts < tv then tv := currenttimer.nextts;
    currenttimer := currenttimer.nexttimer;
  end;
  result := tv-tvnow;
  if result < 15 then result := 15;
end;

{------------------------------------------------------------------------------}

constructor tltask.create(ahandler:ttaskevent;aobj:tobject;awparam,alparam:longint);
begin
  inherited create;
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
    postmessage(hwndwcore,WINMSG_TASK,0,0);
  end;
  lasttask := self;
  //ahandler(wparam,lparam);
end;

procedure addtask(ahandler:ttaskevent;aobj:tobject;awparam,alparam:longint);
begin
  tltask.create(ahandler,aobj,awparam,alparam);
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

procedure dotasks;
var
  temptask:tltask;
begin
  if firsttask = nil then exit;

  currenttask := firsttask;
  firsttask := nil;
  lasttask  := nil;
  while assigned(currenttask) do begin
    if assigned(currenttask.handler) then currenttask.handler(currenttask.wparam,currenttask.lparam);
    temptask := currenttask;
    currenttask := currenttask.nexttask;
    temptask.free;
  end;
  currenttask := nil;
end;

{------------------------------------------------------------------------------}

procedure exitmessageloop;
begin
  postmessage(hwndwcore,WM_QUIT,0,0);
end;

  {$ifdef threadtimer}
  'thread timer'
  {$else}
const timerid_wcore=$1000;
  {$endif}

function MyWindowProc(
    ahWnd   : HWND;
    auMsg   : Integer;
    awParam : WPARAM;
    alParam : LPARAM): Integer; stdcall;
var
    MsgRec : TMessage;
    a:integer;
begin
  Result := 0;  // This means we handled the message

  {MsgRec.hwnd    := ahWnd;}
  MsgRec.wParam  := awParam;
  MsgRec.lParam  := alParam;

  dotasks;
  case auMsg of
    {$ifndef threadtimer}
    WM_TIMER: begin
      if msgrec.wparam = timerid_wcore then begin
        a := wcore_timehandler;
        killtimer(hwndwcore,timerid_wcore);
        settimer(hwndwcore,timerid_wcore,a,nil);
      end;
    end;
    {$endif}

    {WINMSG_TASK:dotasks;}

    WM_CLOSE: begin
      {}
    end;
    WM_DESTROY: begin
      {}
    end;
  else
      Result := DefWindowProc(ahWnd, auMsg, awParam, alParam)
  end;
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
                                 lpszClassName : 'wcoreClass');

procedure messageloop;
var
  MsgRec : TMsg;
begin

  if Windows.RegisterClass(MyWindowClass) = 0 then halt;
  //writeln('about to create wcore handle, hinstance=',hinstance);
  hwndwcore := CreateWindowEx(WS_EX_TOOLWINDOW,
                               MyWindowClass.lpszClassName,
                               '',        { Window name   }
                               WS_POPUP,  { Window Style  }
                               0, 0,      { X, Y          }
                               0, 0,      { Width, Height }
                               0,         { hWndParent    }
                               0,         { hMenu         }
                               HInstance, { hInstance     }
                               nil);      { CreateParam   }

  if hwndwcore = 0 then halt;

  {$ifdef threadtimer}
  'thread timer'
  {$else}
  if settimer(hwndwcore,timerid_wcore,15,nil) = 0 then halt;
  {$endif}


  while GetMessage(MsgRec, 0, 0, 0) do begin
    TranslateMessage(MsgRec);
    DispatchMessage(MsgRec);
    {if not peekmessage(msgrec,0,0,0,PM_NOREMOVE) then onidle}
  end;

  if hWndwcore <> 0 then begin
    DestroyWindow(hwndwcore);
    hWndwcore := 0;
  end;

  {$ifdef threadtimer}
  'thread timer'
  {$else}
  killtimer(hwndwcore,timerid_wcore);
  {$endif}
end;

function ProcessMessage : Boolean;
var
    Msg : TMsg;
begin
    Result := FALSE;
    if PeekMessage(Msg, hwndwcore, 0, 0, PM_REMOVE) then begin
      Result := TRUE;
      DispatchMessage(Msg);
    end;
end;

procedure processmessages;
begin
  while processmessage do;
end;


end.
