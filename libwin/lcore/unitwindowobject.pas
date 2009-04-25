{ Copyright (C) 2005 Bas Steendijk and Peter Green
  For conditions of distribution and use, see copyright notice in zlib_license.txt
  which is included in the package
  ----------------------------------------------------------------------------- }

unit unitwindowobject;

interface

uses
  classes,
  {$ifdef win32}
    windows,messages,wmessages,
  {$else}
    lmessages,
    {$macro on}
    {$define windows := lmessages}
  {$endif}
  sysutils,
  pgtypes;

type
  twindowobject=class(tobject)
    hwndmain:hwnd;
    onmsg:function(msg,wparam,lparam:taddrint):boolean of object;
    exitloopflag:boolean;
    function settimer(id,timeout:taddrint):integer;
    function killtimer(id:taddrint):boolean;
    procedure postmessage(msg,wparam,lparam:taddrint);
    procedure messageloop;
    {$ifdef win32}
      procedure processmessages;
      function processmessage:boolean;
    {$endif}
    constructor create;
    destructor destroy; override;
  end;

implementation

//uses safewriteln;

function WindowProc(ahWnd:HWND; auMsg:Integer; awParam:WPARAM; alParam:LPARAM):Integer; stdcall;
var
  i:taddrint;
begin
  ////swriteln('in unitwindowobject.windowproc');
  Result := 0;  // This means we handled the message
  if ahwnd <> hwnd(0) then i := getwindowlongptr(ahwnd,0) else i := 0;
  if i <> 0 then begin
    if assigned(twindowobject(i).onmsg) then begin
      if not twindowobject(i).onmsg(aumsg,awparam,alparam) then i := 0;
    end else i := 0
  end;
  if i = 0 then Result := DefWindowProc(ahWnd, auMsg, awParam, alParam)
end;

var
  twindowobject_Class : TWndClass = (style:0; lpfnWndProc:@WindowProc;
  cbClsExtra:0; cbWndExtra:sizeof(pointer); hInstance:thinstance(0); hIcon:hicon(0); hCursor:hcursor(0);
  hbrBackground:hbrush(0);lpszMenuName:nil; lpszClassName:'twindowobject_class');

function twindowobject.settimer;
begin
  result := windows.settimer(hwndmain,id,timeout,nil);
end;

function twindowobject.killtimer;
begin
  result := windows.killtimer(hwndmain,id);
end;

constructor twindowobject.create;
begin
  inherited;
  //swriteln('in twindowobject.create, about to call registerclass');
  Windows.RegisterClass(twindowobject_Class);
  //swriteln('about to call createwindowex');
  hWndMain := CreateWindowEx(WS_EX_TOOLWINDOW, twindowobject_Class.lpszClassName,
    '', WS_POPUP, 0, 0,0, 0, hwnd(0), 0, HInstance, nil);
  //swriteln('about to check result of createwindowex');
  if hWndMain = hwnd(0) then raise exception.create('CreateWindowEx failed');
  //swriteln('about to store reference to self in extra windo memory');
  setwindowlongptr(hwndmain,0,taddrint(self));
  //swriteln('finished twindowobject.create , hwndmain='+inttohex(taddrint(hwndmain),16));
end;

destructor twindowobject.destroy;
begin
  if hWndMain <> hwnd(0) then DestroyWindow(hwndmain);
  inherited;
end;

procedure twindowobject.postmessage;
begin
  windows.postmessage(hwndmain,msg,wparam,lparam);
end;

{$ifdef win32}
  function twindowobject.ProcessMessage : Boolean;
  var
    Msg : TMsg;
  begin
    Result := FALSE;
    if PeekMessage(Msg, hwndmain, 0, 0, PM_REMOVE) then begin
      Result := TRUE;
      DispatchMessage(Msg);
    end;
  end;

  procedure twindowobject.processmessages;
  begin
    while processmessage do;
  end;
{$endif}

procedure twindowobject.messageloop;
var
  MsgRec : TMsg;
begin
  while GetMessage(MsgRec, hwnd(0), 0, 0) do begin
    DispatchMessage(MsgRec);
    if exitloopflag then exit;
    {if not peekmessage(msgrec,0,0,0,PM_NOREMOVE) then onidle}
  end;
end;

end.
