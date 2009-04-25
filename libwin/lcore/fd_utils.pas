// this file contains code copied from linux.pp in the free pascal rtl
// i had to copy them because i use a different definition of fdset to them
// the copyright block from the file in question is shown below
{
   $Id: fd_utils.pas,v 1.2 2004/08/19 23:12:09 plugwash Exp $
   This file is part of the Free Pascal run time library.
   Copyright (c) 1999-2000 by Michael Van Canneyt,
   BSD parts (c) 2000 by Marco van de Voort
   members of the Free Pascal development team.

   See the file COPYING.FPC, included in this distribution,
   for details about the copyright.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY;without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

**********************************************************************}
{$ifdef fpc}
  {$mode delphi}
  {$inlining on}
{$endif}
unit fd_utils;
interface

type
    FDSet= Array [0..255] of longint; {31}
    PFDSet= ^FDSet;

Procedure FD_Clr(fd:longint;var fds:fdSet);
Procedure FD_Zero(var fds:fdSet);
Procedure FD_Set(fd:longint;var fds:fdSet);
Function FD_IsSet(fd:longint;var fds:fdSet):boolean;

{$ifdef fpc}
  {$ifndef ver1_0}
    {$define useinline}
  {$endif}
{$endif}

implementation  
uses sysutils;
Procedure FD_Clr(fd:longint;var fds:fdSet);{$ifdef useinline}inline;{$endif}
{ Remove fd from the set of filedescriptors}
begin
  if (fd < 0) then raise exception.create('FD_Clr fd out of range: '+inttostr(fd));
  fds[fd shr 5]:=fds[fd shr 5] and (not (1 shl (fd and 31)));
end;

Procedure FD_Zero(var fds:fdSet);
{ Clear the set of filedescriptors }
begin
  FillChar(fds,sizeof(fdSet),0);
end;

Procedure FD_Set(fd:longint;var fds:fdSet);{$ifdef useinline}inline;{$endif}
{ Add fd to the set of filedescriptors }
begin
  if (fd < 0) then raise exception.create('FD_set fd out of range: '+inttostr(fd));
  fds[fd shr 5]:=fds[fd shr 5] or (1 shl (fd and 31));
end;

Function FD_IsSet(fd:longint;var fds:fdSet):boolean;{$ifdef useinline}inline;{$endif}
{ Test if fd is part of the set of filedescriptors }
begin
  if (fd < 0) then begin
    result := false;
    exit;
  end;
  FD_IsSet:=((fds[fd shr 5] and (1 shl (fd and 31)))<>0);
end;
end.
