{ Copyright (C) 2005 Bas Steendijk and Peter Green
  For conditions of distribution and use, see copyright notice in zlib_license.txt
  which is included in the package
  ----------------------------------------------------------------------------- } 
      
unit wmessages;
//this unit contains varions functions and types to make it easier to write
//code that works with both real windows messages and lmessages

interface
uses windows,messages,pgtypes;
type
  thinstance=thandle;
  thevent=thandle;

//according to MS you are supposed to use get/setwindowlongptr to get/set
//pointers in extra window memory so your program can be built for win64, this
//is also the only interface to window memory that lmessages offers but delphi
//doesn't define it so alias it to getwindowlong here for win32.
{$ifndef win64} //future proofing ;)
  function getwindowlongptr(ahwnd:hwnd;nindex:integer) : taddrint;
  procedure setwindowlongptr(ahwnd:hwnd;nindex:integer;dwNewLong : taddrint);
{$endif}
function WaitForSingleEvent(hHandle: THandle; dwMilliseconds: DWORD): DWORD; stdcall;
implementation
{$ifndef win64}
  function getwindowlongptr(ahwnd:hwnd;nindex:integer) : taddrint;
  begin
    result := getwindowlong(ahwnd,nindex);
  end;
  procedure setwindowlongptr(ahwnd:hwnd;nindex:integer;dwNewLong : taddrint);
  begin
    setwindowlong(ahwnd,nindex,dwnewlong);
  end;
{$endif}
function WaitForSingleEvent(hHandle: THandle; dwMilliseconds: DWORD): DWORD; stdcall;
begin
  result := waitforsingleobject(hhandle,dwmilliseconds);
end;
end.