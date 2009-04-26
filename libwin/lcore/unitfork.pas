{ Copyright (C) 2005 Bas Steendijk and Peter Green
  For conditions of distribution and use, see copyright notice in zlib_license.txt
    which is included in the package
      ----------------------------------------------------------------------------- }
unit unitfork;

interface

procedure dofork(const programname:string);
procedure writepid;
function checkpid(const filename:string):boolean;
procedure deletepid;

implementation

uses
  {$ifdef VER1_0}
    linux,
  {$else}
    baseunix,unix,unixutil,
  {$endif}
  sysutils;

{$include unixstuff.inc}

const
  F_WRLCK=2;

var
  pidfilename:string;
  pidfile:text;

procedure dofork(const programname:string);
var
  a:integer;
begin
  //writeln('dofork entered');
  //if (paramstr(1) = 'foreground') or (paramstr(1)='debug') then exit; {no fork}
  a := fork;
  if a = 0 then exit; {i'm the child}
  if a < 0 then begin
    writeln('failed to run in background, try "'+programname+' foreground" if it doesnt work otherwise');
    halt; {failed}
  end;

  halt; {i'm the parent}
end;

function checkpid;
var
  handle:thandle;

begin
  result := false;
  pidfilename := '';
  //debugout(filename);
  assignfile(pidfile,filename);
  filemode := 2;
  {opening file to get a fd for it. can't rewrite because a lock appears to allow the rewrite}
  {$i-}reset(pidfile);{$i+}
  if ioresult <> 0 then begin
    {$i-}rewrite(pidfile);{$i+}
    if ioresult <> 0 then exit;
  end;

  handle := getfs(pidfile);

  //debugout('got handle');
  {check if locking is possible: it's not if other process still runs}
  {$ifdef VER1_0}
  if not flock(handle,LOCK_EX or LOCK_NB)
  {$else}
  if flock(handle,LOCK_EX or LOCK_NB) <> 0
  {$endif}
  then begin
    //debugout('failed to lock pid file');
    close(pidfile);
    exit;
  end;
  rewrite(pidfile);
  {lock again because the rewrite removes the lock}
  {$ifdef VER1_0}
  if not flock(handle,LOCK_EX or LOCK_NB)
  {$else}
  if flock(handle,LOCK_EX or LOCK_NB) <> 0
  {$endif}
  then raise exception.create('flock failed '+inttostr(linuxerror));
  pidfilename := filename;
  result := true;
end;


procedure writepid;
begin
  writeln(pidfile,getpid);
  flush(pidfile);
end;

procedure deletepid;
begin
  if pidfilename = '' then exit;
  try
    {$i-}
    closefile(pidfile);
    erase(pidfile);
    {$i+}
    ioresult;
  except
    {}
  end;
  pidfilename := '';
end;

end.
