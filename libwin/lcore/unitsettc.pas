{ Copyright (C) 2005 Bas Steendijk and Peter Green
  For conditions of distribution and use, see copyright notice in zlib_license.txt
  which is included in the package
  ----------------------------------------------------------------------------- }

unit Unitsettc;

interface

procedure settc;
procedure unsettc;

implementation

uses
  windows,
  sysutils;

var
  classpriority,threadpriority:integer;
  refcount:integer=0;

procedure settc;
var
  hprocess,hthread:integer;
begin
  if (refcount = 0) then begin
    hProcess := GetCurrentProcess;
    hThread := GetCurrentThread;
    ClassPriority := GetPriorityClass(hProcess);
    ThreadPriority := GetThreadPriority(hThread);
    SetPriorityClass(hProcess, REALTIME_PRIORITY_CLASS);
    SetThreadPriority(hThread, THREAD_PRIORITY_TIME_CRITICAL);
  end;
  inc(refcount);
end;

procedure unsettc;
var
  hprocess,hthread:integer;
begin
  dec(refcount);
  if (refcount < 0) then refcount := 0;
  if (refcount = 0) then begin
    hProcess := GetCurrentProcess;
    hThread := GetCurrentThread;
    SetPriorityClass(hProcess, ClassPriority);
    SetThreadPriority(hThread,  ThreadPriority);
  end;
end;

end.

