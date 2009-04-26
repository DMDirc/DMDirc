{*
 * Updates DMDirc windows components
 *
 * DMDirc - Open Source IRC Client
 * Copyright (c) 2006-2009 Chris Smith, Shane Mc Cormack, Gregory Holmes,
 * Michael Nixon
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *}
program DMDircUpdater;
{$MODE Delphi}
{$APPTYPE GUI}

{$R UAC.rc}

uses Vista, shared, Windows, SysUtils, classes, StrUtils;
procedure InitCommonControls; stdcall; External 'comctl32.dll' name 'InitCommonControls';
{ ---------------------------------------------------------------------------- }

{ ----------------------------------------------------------------------------
  MAIN PROGRAM
  ---------------------------------------------------------------------------- }
var
  sourceDir: String = '';
  thisDir: String;
  cliParams: String = '';
  i: integer;
  jarName: String;
  launcherUpdate: boolean = false;
  myName: String;
  canDoMore: boolean = true;
begin
  InitCommonControls;

  myName := ExtractFileName(paramstr(0));
  thisDir := ExtractFileDir(paramstr(0));
  jarName := thisDir+'\DMDirc.jar';

  if ParamCount > 0 then begin
    for i := 1 to ParamCount do begin
      if AnsiContainsStr(cliParams, ' ') then cliParams := cliParams+' "'+paramstr(i)+'"'
      else cliParams := cliParams+' '+paramstr(i);
      if (paramstr(i) = '--UpdateSourceDir') then begin // Source Directory
        if ParamCount > i then begin
          sourceDir := paramstr(i+1);
        end;
      end
    end;

    // Look for a launcher update.
    if FileExists(pchar(sourceDir+'\.DMDirc.exe')) and FileExists(pchar(sourceDir+'\.DMDircUpdater.exe')) then begin
      if myName = 'DMDircUpdater.exe' then begin
        // Windows won't let us overwrite ourself, so we need to copy ourself
        // to another name, and run the new us.
        if CopyFile(pchar(thisDir+'\DMDircUpdater.exe'), pchar(thisDir+'\DMDircLauncherUpdater.exe'), False) then begin
          canDoMore := false;
          Launch('"'+thisDir+'\DMDircLauncherUpdater.exe" '+cliParams);
        end
        else begin
          showmessage('Unable to overwrite launcher', 'DMDirc', 'Update Failed');
        end;
      end
      else begin
        launcherUpdate := true;
        if FileExists(pchar(thisDir+'\DMDirc.exe')) then begin
          if not DeleteFile(pchar(thisDir+'\DMDirc.exe')) then begin
            showmessage('Unable to delete DMDirc.exe', 'DMDirc', 'Launcher Update Failed');
          end;
        end;
        
        if not FileExists(pchar(thisDir+'\DMDirc.exe')) and MoveFile(pchar(sourceDir+'\.DMDirc.exe'), pchar(thisDir+'\DMDirc.exe')) then begin
          if FileExists(pchar(thisDir+'\DMDircUpdater.exe')) then begin
            if not DeleteFile(pchar(thisDir+'\DMDircUpdater.exe')) then begin
              showmessage('Unable to delete DMDircUpdater.exe', 'DMDirc', 'Launcher Update Failed');
            end;
          end;
          if not FileExists(pchar(thisDir+'\DMDircUpdater.exe')) and MoveFile(pchar(sourceDir+'\.DMDircUpdater.exe'), pchar(thisDir+'\DMDircUpdater.exe')) then begin
            showmessage('Launcher update was successful', 'DMDirc');
          end
          else begin
            showmessage('Unable to update DMDircUpdater.exe', 'DMDirc', 'Launcher Update Failed');
          end;
        end
        else begin
          showmessage('Unable to update DMDirc.exe', 'DMDirc', 'Launcher Update Failed');
        end;
      end;
    end;

    // Look for client update
    if canDoMore then begin
      if FileExists(pchar(sourceDir+'\.DMDirc.jar')) then begin
        if FileExists(pchar(jarName)) then begin
          if not DeleteFile(pchar(jarName)) then begin
            showmessage('Unable to update DMDirc.jar', 'DMDirc', 'Launcher Update Failed');
          end;
        end;

        if MoveFile(pchar(sourceDir+'\.DMDirc.jar'), pchar(jarName)) then begin
          showmessage('Client update was successful', 'DMDirc');
        end
        else begin
          showmessage('Unable to move '''+sourceDir+'\.DMDirc.jar'' to '+jarName, 'DMDirc', 'Update Failed');
        end;
      end;
      
      if launcherUpdate then begin
        showmessage('The DMDirc launcher has been updated, to complete the update please relaunch DMDirc.', 'DMDirc', 'Restart Required');
      end;
    end;
  end
  else begin
    showError('This program can not be run on its own.', 'DMDirc');
  end;
end.
