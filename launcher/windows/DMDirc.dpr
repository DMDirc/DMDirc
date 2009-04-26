{*
 * This application launches DMDirc on windows and passes control to the
 * update engine as necessary.
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
program DMDirc;
{$MODE Delphi}
{$APPTYPE GUI}

{$R comctl.rc}
{$R icon.rc}
{$R version.rc}

uses Vista, shared, Windows, SysUtils, classes, StrUtils;
procedure InitCommonControls; stdcall; External 'comctl32.dll' name 'InitCommonControls';
{ ---------------------------------------------------------------------------- }

{ ----------------------------------------------------------------------------
  MAIN PROGRAM
  ---------------------------------------------------------------------------- }
const
  launcherVersion: String = '2';
var
  errorMessage: String;
  javaCommand: String = 'javaw.exe';
  cliParams: String = '';
  directory: String = '';
  i: integer;
  jarName: String;
  launcherUpdate: boolean = false;
  result: integer;
  s: string;
  handle: thandle;
begin
  InitCommonControls;

  { Check for a mutex created by the DMDirc update process. Wait until it no
    longer exists before we continue. This prevents us from proceeding if any
    previous instance of the launcher is shutting down. }
  handle := OpenMutex(SYNCHRONIZE, False, 'DMDirc_Launcher_Restart_Wait');
  if handle <> 0 then begin
    { The mutex exists. Wait for it for up to 5 seconds. }
    if WaitForSingleObject(handle, 5000) = WAIT_TIMEOUT then begin
      { Timed out - we cannot continue, there is some kind of serious issue? }
      showError('Internal error: Timed out waiting for previous instance of launcher to stop during restart upgrade', 'DMDirc', true, true);
      exit;
    end;
    { If we get to here, the mutex has been released and we can continue }
    CloseHandle(handle);
  end;

  jarName := ExtractFileDir(paramstr(0))+'\DMDirc.jar';

  directory := GetEnvironmentVariable('APPDATA')+'\DMDirc';
  if ParamCount > 0 then begin
    for i := 1 to ParamCount do begin
      if AnsiContainsStr(cliParams, ' ') then cliParams := cliParams+' "'+paramstr(i)+'"'
      else cliParams := cliParams+' '+paramstr(i);
      if (paramstr(i) = '-d') or (paramstr(i) = '--directory') then begin
        if ParamCount > i then begin
          directory := paramstr(i+1);
        end;
      end
    end;
  end;

  // Update if needed.
  launcherUpdate := FileExists(directory+'\.DMDirc.exe') and FileExists(directory+'\.DMDircUpdater.exe');
  if FileExists(directory+'\.DMDirc.jar') or launcherUpdate then begin
    // Vista Sucks.
    if IsWindowsVista then begin
      // Vista >.<
      // Try and delete the old file, if it fails then the user needs to give
      // us permission to delete the file (UAC), otherwise we can just go ahead
      // and run the updater.
      if not DeleteFile(pchar(jarName)) then begin
        errorMessage := 'An update to DMDirc has been previously downloaded. ';
        errorMessage := errorMessage+#13#10;
        errorMessage := errorMessage+#13#10+'As you are running Windows Vista, DMDirc requires administrator access to ';
        errorMessage := errorMessage+#13#10+'complete the update. ';
        errorMessage := errorMessage+#13#10;
        errorMessage := errorMessage+#13#10+'Please click ''Allow'' on the UAC prompt to complete the update, or click no ';
        errorMessage := errorMessage+#13#10+'here to continue without updating. ';
        if askQuestion(errorMessage, 'DMDirc') then begin
          RunProgram('"'+ExtractFileDir(paramstr(0))+'\DMDircUpdater.exe" --UpdateSourceDir "'+directory+'"', not launcherUpdate);
        end;
      end
      else RunProgram('"'+ExtractFileDir(paramstr(0))+'\DMDircUpdater.exe" --UpdateSourceDir "'+directory+'"', not launcherUpdate);
    end
    else RunProgram('"'+ExtractFileDir(paramstr(0))+'\DMDircUpdater.exe" --UpdateSourceDir "'+directory+'"', not launcherUpdate);
  end;

  if not launcherUpdate then begin
    // Check JVM
    if (ExecAndWait(javaCommand+' -version') <> 0) then begin
      errorMessage := 'No JVM is currently installed.';
      errorMessage := errorMessage+#13#10;
      errorMessage := errorMessage+#13#10+'DMDirc requires a 1.6.0 compatible JVM, you can get one from:';
      errorMessage := errorMessage+#13#10+'http://java.com/';
      showError(errorMessage, 'DMDirc', true);
    end
    // Else try and run client. (This only asks for help output to check that client
    // runs on this OS, otherwise later segfaults or so would cause the error to
    // appear
    else if FileExists(jarName) then begin
      if (ExecAndWait(javaCommand+' -jar "'+jarName+'" --help') <> 0) then begin
        errorMessage := 'The currently installed version of java is not compatible with DMDirc.';
        errorMessage := errorMessage+#13#10;
        errorMessage := errorMessage+#13#10+'DMDirc requires a 1.6.0 compatible JVM, you can get one from:';
        errorMessage := errorMessage+#13#10+'http://java.com/';
        showError(errorMessage, 'DMDirc', True);
      end
      else begin
        //Launch(javaCommand+' -ea -jar "'+jarName+'"'+' -l windows-'+launcherVersion+' '+cliParams)
        { Need to wait so we can deal with exit code 42 to restart }
        result := ExecAndWait(javaCommand+' -ea -jar "'+jarName+'"'+' -l windows-'+launcherVersion+' '+cliParams);
        if result = 42 then begin
          { Need to restart DMDirc launcher
            We can't just rerun the EXE because it is possible for the new
            process to launch the updater and try to replace this EXE before
            the current thread ends - remote but possible. We deal with this
            case by creating a mutex that this program will spin on when it
            restarts until this instance ends. }
          handle := CreateMutex(nil, True, 'DMDirc_Launcher_Restart_Wait');
          if handle = 0 then begin
            showError('Internal error: Failed to create restart mutex', 'DMDirc', true, true);
            exit;
          end;

          { Build new command line }
          s := '';
          for i := 1 to paramcount do begin
            if pos(' ', paramstr(i)) <> 0 then s := s + '"' + paramstr(i) + '"' else
              s := s + paramstr(i);
            if i < paramcount then s := s + ' ';
          end;

          { Launch self }
          Launch('"' + paramstr(0)+ '" ' + s);
        end;
      end;
    end
    else begin
      errorMessage := 'A file required to start DMDirc is missing. Please re-install DMDirc to rectify the problem.';
      errorMessage := errorMessage+#13#10;
      errorMessage := errorMessage+#13#10 + 'File: DMDirc.jar';
      showError(errorMessage, 'DMDirc');
    end;
  end;
end.