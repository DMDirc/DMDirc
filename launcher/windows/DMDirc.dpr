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
  Launch a process (hidden if requested) and wait for it to finish
  ---------------------------------------------------------------------------- }
function ExecAndWait(sProgramToRun: String): Longword;
var
  StartupInfo: TStartupInfo;
  ProcessInfo: TProcessInformation;
begin
  FillChar(StartupInfo, SizeOf(TStartupInfo), 0);
  with StartupInfo do begin
    cb := SizeOf(TStartupInfo);
    dwFlags := STARTF_USESHOWWINDOW;
    wShowWindow := SW_SHOWNORMAL;
  end;

  CreateProcess(nil, PChar(sProgramToRun), nil, nil, False, NORMAL_PRIORITY_CLASS, nil, nil, StartupInfo, ProcessInfo);
  getExitCodeProcess(ProcessInfo.hProcess, Result);

  while Result=STILL_ACTIVE do begin
    sleep(1000);
    GetExitCodeProcess(ProcessInfo.hProcess, Result);
  end;
end;

{ ----------------------------------------------------------------------------
  Launch a process (hidden if requested) and immediately return control to
  the current thread
  ---------------------------------------------------------------------------- }
procedure Launch(sProgramToRun: String);
var
  StartupInfo: TStartupInfo;
  ProcessInfo: TProcessInformation;
begin
  FillChar(StartupInfo, SizeOf(TStartupInfo), 0);
  with StartupInfo do begin
    cb := SizeOf(TStartupInfo);
    dwFlags := STARTF_USESHOWWINDOW;
    wShowWindow := SW_SHOWNORMAL;
  end;

  CreateProcess(nil, PChar(sProgramToRun), nil, nil, False, NORMAL_PRIORITY_CLASS, nil, nil, StartupInfo, ProcessInfo);
end;

{ ----------------------------------------------------------------------------
  Launch a process and either waits for it or returns control immediately
  ---------------------------------------------------------------------------- }
procedure RunProgram(sProgramToRun: String; wait: boolean);
begin
  if wait then ExecAndWait(sProgramToRun)
  else Launch(sProgramToRun);
end;

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
begin
  InitCommonControls;

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
        Launch(javaCommand+' -ea -jar "'+jarName+'"'+' -l windows-'+launcherVersion+' '+cliParams)
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