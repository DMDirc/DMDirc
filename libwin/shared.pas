{*
 * Shared methods / classes / functions between Windows programs
 *
 * This application launches DMDirc on windows and passes control to the
 * update engine as necessary.
 *
 * DMDirc - Open Source IRC Client
 * Copyright (c) 2006-2010 Chris Smith, Shane Mc Cormack, Gregory Holmes,
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
unit shared;

interface

uses Windows, SysUtils, Vista;

function nicesize(dsize: extended): string;
function askQuestion(Question: String; Title: string): boolean;
procedure showmessage(message: String; Title: string; context:String = 'Information');
procedure showError(ErrorMessage: String; Title: string; addFooter: boolean = true; includeDescInXP: boolean = true);
function Launch(sProgramToRun: String; hide: boolean = false): TProcessInformation;
function ExecAndWait(sProgramToRun: String; hide: boolean = false): Longword;
procedure RunProgram(sProgramToRun: String; wait: boolean);
{ ---------------------------------------------------------------------------- }

implementation

{ ----------------------------------------------------------------------------
  Takes a size, <dsize> in bytes, and converts it a human readable string with
  a suffix (MB or GB).
  ---------------------------------------------------------------------------- }
function nicesize(dsize: extended): string;
var
  kbytes: single;
  mbytes: single;
  gbytes: single;
begin
  kbytes := dsize / 1024;
  mbytes := kbytes / 1024;
  gbytes := mbytes / 1024;

  if kbytes < 1024 then begin
    result := FloatToStrF(kbytes, ffFixed, 10, 2) + ' kB';
    exit;
  end;

  if mbytes < 1024 then begin
    result := FloatToStrF(mbytes, ffFixed, 10, 2) + ' MB';
    exit;
  end;

  result := FloatToStrF(gbytes, ffFixed, 10, 2) + ' GB';
  exit;
end;

{ ----------------------------------------------------------------------------
  Ask a question and return True for YES and False for NO
  Uses nifty vista task dialog if available
  ---------------------------------------------------------------------------- }
function askQuestion(Question: String; Title: string): boolean;
begin
  Result := TaskDialog(0, Title, 'Question', Question, TD_ICON_QUESTION, TD_BUTTON_YES + TD_BUTTON_NO) = mrYes;
end;

{ ----------------------------------------------------------------------------
  Show a message box (information)
  Uses nifty vista task dialog if available
  ---------------------------------------------------------------------------- }
procedure showmessage(message: String; Title: string; context:String = 'Information');
begin
  TaskDialog(0, Title, context, message, TD_ICON_INFORMATION, TD_BUTTON_OK);
end;

{ ----------------------------------------------------------------------------
  Show an error message
  Uses nifty vista task dialog if available
  ---------------------------------------------------------------------------- }
procedure showError(ErrorMessage: String; Title: string; addFooter: boolean = true; includeDescInXP: boolean = true);
begin
  if addFooter then begin
    ErrorMessage := ErrorMessage+#13#10;
    ErrorMessage := ErrorMessage+#13#10+'If you feel this is incorrect, or you require some further assistance,';
    if not IsWindowsVista then ErrorMessage := ErrorMessage+#13#10;
    ErrorMessage := ErrorMessage+'please feel free to contact us.';
  end;
  TaskDialog(0, Title, 'Sorry, ' + Title + ' is unable to continue.', ErrorMessage, TD_ICON_ERROR, TD_BUTTON_OK, includeDescInXP, false);
end;

{ ----------------------------------------------------------------------------
  Launch a process (hidden if requested) and immediately return control to
  the current thread
  ---------------------------------------------------------------------------- }
function Launch(sProgramToRun: String; hide: boolean = false): TProcessInformation;
var
  StartupInfo: TStartupInfo;
begin
  FillChar(StartupInfo, SizeOf(TStartupInfo), 0);
  with StartupInfo do begin
    cb := SizeOf(TStartupInfo);
    dwFlags := STARTF_USESHOWWINDOW;
    if hide then wShowWindow := SW_HIDE
    else wShowWindow := SW_SHOWNORMAL;
  end;

  CreateProcess(nil, PChar(sProgramToRun), nil, nil, False, NORMAL_PRIORITY_CLASS, nil, nil, StartupInfo, Result);
end;

{ ----------------------------------------------------------------------------
  Launch a process (hidden if requested) and wait for it to finish
  ---------------------------------------------------------------------------- }
function ExecAndWait(sProgramToRun: String; hide: boolean = false): Longword;
var
  ProcessInfo: TProcessInformation;
begin
  ProcessInfo := Launch(sProgramToRun, hide);
  getExitCodeProcess(ProcessInfo.hProcess, Result);

  while Result = STILL_ACTIVE do begin
    sleep(1000);
    GetExitCodeProcess(ProcessInfo.hProcess, Result);
  end;
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
  ---------------------------------------------------------------------------- }
end.

