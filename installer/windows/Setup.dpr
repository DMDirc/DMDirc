{*
 * This application launches the dmdirc java-based installer.
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

(* Current DMDirc windows setup flow:
 *
 * 1) Outer wrapper EXE that extracts a 7zip SFX to windows temp dir
 * 2) 7zip SFX unpacks
 * 3) Wrapper EXE starts Setup.exe (this program)
 * 4) Setup checks for existence of the JRE and offers to download/install it
 * 5) Setup starts the java portion of the DMDirc installer
 *)
program Setup;

// Resource file - icon, versioninfo, manifest
{$R most.res}

{ ---------------------------------------------------------------------------- }
{$IFDEF FPC}
  {$MODE Delphi}
{$ENDIF}

// Use this instead of {$APPTYPE XXX}
// APP_XXX is the same as {$APPTYPE XXX}
// Defaults to console
// This is a work-around for a bug in FPC Cross Compiling to windows in delphi
// mode (IsConsole is always true)
{$DEFINE APP_GUI}

// This block actually does the work for the above work-around
{$IFDEF APP_GUI}
  {$APPTYPE GUI}
{$ELSE}
  {$IFDEF APP_FS}
    {$APPTYPE FS}
  {$ELSE}
    {$IFDEF APP_TOOL}
      {$DEFINE APP_CONSOLE}
      {$APPTYPE TOOL}
    {$ELSE}
      {$DEFINE APP_CONSOLE}
      {$APPTYPE CONSOLE}
    {$ENDIF}
  {$ENDIF}
{$ENDIF}

{ ----------------------------------------------------------------------------
  Debugging specific compiler directives
  ---------------------------------------------------------------------------- }

// If defined the JRE will always be downloaded as if it didn't exist. Used for
// testing the JRE download dialog.
//{$DEFINE FORCEJREDOWNLOAD}

uses
  kol, Vista, Windows, SysUtils, classes, registry;

const
  // SetupConsts holds build information for this release
  {$I SetupConsts.inc}
  // This is also part of the above IsConsole workaround.
  {$IFDEF APP_CONSOLE}
    IsConsole: boolean = true;
  {$ELSE}
    IsConsole: boolean = false;
  {$ENDIF}

var
  { --------------------------------------------------------------------------
    KOL form objects
    -------------------------------------------------------------------------- }
  frmmain: pcontrol;
  progressbar, btncancel: pcontrol;
  label1, label2, label3, label4, labelurl, labelspeed, labelprogress: pcontrol;

  { --------------------------------------------------------------------------
    Other globals
    -------------------------------------------------------------------------- }
  terminateDownload: boolean = false;

{ ----------------------------------------------------------------------------
  Main form: Cancel button clicked event
  ---------------------------------------------------------------------------- }
procedure btnCancel_Click(Dummy: Pointer; Sender: PControl);
begin
  terminateDownload := true;
end;

{ ----------------------------------------------------------------------------
  Main form: Set progress percentage to <value> and display in label <msg>
  ---------------------------------------------------------------------------- }
procedure setProgress(value: integer; msg: string);
begin
  ProgressBar.progress := value;
  labelprogress.Caption := msg;
  //self.Caption := pChar('DMDirc Setup - '+CaptionLabel.Caption);
  //Application.Title := self.Caption;
  applet.processmessages;
end;

{ ----------------------------------------------------------------------------
  Initialise KOL and create the main window
  ---------------------------------------------------------------------------- }
procedure CreateMainWindow;
var
  screenw, screenh: longint;
begin
  { This call is required for common control 6 DLL to be correctly imported.
    Without it strange things happen on windows XP }
  InitCommonControls;

  { We need the screen size to centre the window later }
  screenw := GetSystemMetrics(SM_CXSCREEN);
  screenh := GetSystemMetrics(SM_CYSCREEN);

  { KOL programs ideally need an Applet created }
  Applet := NewApplet('DMDirc Setup');
  Applet.Visible := true;

  { Currently this icon does not work }
  Applet.Icon := THandle(-1);

  { Create main form and set sane defaults. If we don't set the font here then
    all child objects will have a rubbish font as a holdover from Windows 3.1! }
  frmmain := NewForm(Applet, 'DMDirc Setup').SetClientSize(400, 184);
  frmmain.CreateVisible := True;
  frmmain.CanResize := False;
  frmmain.Style := frmmain.style and (not WS_MAXIMIZEBOX);
  frmmain.Font.FontName := 'Ms Sans Serif';
  frmmain.Font.FontHeight := 8;
  frmmain.SetPosition((screenw div 2) - (frmmain.Width div 2), (screenh div 2) - (frmmain.height div 2));

  { Currently this icon does not work }
  frmmain.Icon := THandle(-1);

  progressbar := NewProgressBar(frmmain).SetPosition(16, 114);
  progressbar.SetSize(frmmain.clientWidth - (progressbar.Left * 2), 16);
  progressbar.MaxProgress := 100;
  progressbar.Progress := 0;
  progressbar.Visible := true;

  btncancel := NewButton(frmmain, 'Cancel').SetPosition(progressbar.Left +
    progressbar.width - 60, progressbar.Top + progressbar.Height + 14);
  btncancel.SetSize(60, 24);

  label1 := NewLabel(frmmain, 'Downloading Java Runtime Environment').SetPosition(16, 16);
  label1.SetSize(frmmain.ClientWidth - 32, 16);
  label1.Font.FontStyle := [fsBold];

  label2 := NewLabel(frmmain, 'Address:').SetPosition(16, label1.top + 28);
  label2.SetSize(frmmain.ClientWidth - 32, 16);

  label3 := NewLabel(frmmain, 'Speed:').SetPosition(16, label2.top + 20);
  label3.SetSize(frmmain.ClientWidth - 32, 16);

  label4 := NewLabel(frmmain, 'Progress:').SetPosition(16, label3.top + 20);
  label4.SetSize(frmmain.ClientWidth - 32, 16);

  { BringToFront calls are needed on the following labels because the labels
    created earlier are as wide as the form and cover them as they are not
    transparent. It seems windows creates controls in a backwards order so newer
    controls are behind older ones. I could rearrange this order in the code but
    it would look messy. }

  labelurl := NewLabel(frmmain, '').SetPosition(70, label1.top + 28);
  labelurl.SetSize(frmmain.ClientWidth - 32, 16);
  labelurl.BringToFront;

  labelspeed := NewLabel(frmmain, '').SetPosition(70, label2.top + 20);
  labelspeed.SetSize(frmmain.ClientWidth - 32, 16);
  labelspeed.BringToFront;

  labelprogress := NewLabel(frmmain, '').SetPosition(70, label3.top + 20);
  labelprogress.SetSize(frmmain.ClientWidth - 32, 16);
  labelprogress.BringToFront;

  { Assign UI methods }
  btncancel.OnClick := TOnEvent(MakeMethod(nil, @btnCancel_Click ));

  { The window will not appear until the messageloop is started with Run() but
    this means we must yield this thread to the UI. This is unacceptable for
    such a simple program. Calling CreateWindow here will cause the window to
    appear but the message loop does not run; consequently the app must service
    messages by hand at a timely interval to avoid windows from marking the
    program as unresponsive. This is a hack but acceptable here. }

  { /!\ WARNING /!\ Run() can no longer be used to enter the message loop after
    this call or a nasty crash will occur. }
  applet.createwindow;
end;

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
function askQuestion(Question: String): boolean;
begin
  Result := TaskDialog(0, 'DMDirc Setup', 'Question', Question, TD_ICON_QUESTION, TD_BUTTON_YES + TD_BUTTON_NO) = mrYes;
end;

{ ----------------------------------------------------------------------------
  Show an error message
  Uses nifty vista task dialog if available
  ---------------------------------------------------------------------------- }
procedure showError(ErrorMessage: String; addFooter: boolean = true; includeDescInXP: boolean = true);
begin
  if addFooter then begin
    ErrorMessage := ErrorMessage+#13#10;
    ErrorMessage := ErrorMessage+#13#10+'If you feel this is incorrect, or you require some further assistance,';
    if not IsWindowsVista then ErrorMessage := ErrorMessage+#13#10;
    ErrorMessage := ErrorMessage+'please feel free to contact us.';
  end;
  TaskDialog(0, 'DMDirc Setup', 'Sorry, setup is unable to continue.', ErrorMessage, TD_ICON_ERROR, TD_BUTTON_OK, includeDescInXP, false);
end;

{ ----------------------------------------------------------------------------
  Show a message box (information)
  Uses nifty vista task dialog if available
  ---------------------------------------------------------------------------- }
procedure showmessage(message: String; context:String = 'Information');
begin
  TaskDialog(0, 'DMDirc Setup', context, message, TD_ICON_INFORMATION, TD_BUTTON_OK);
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
  Return the size in bytes of the file specified by <name>
  Returns -1 on error
  ---------------------------------------------------------------------------- }
function GetFileSizeByName(name: String): Integer;
var
  hand: THandle;
begin
  hand := 0;
  Result := 0;
  if FileExists(name) then begin
    try
      hand := CreateFile(PChar(name), GENERIC_READ, FILE_SHARE_WRITE or FILE_SHARE_READ, nil, OPEN_EXISTING, FILE_ATTRIBUTE_NORMAL, 0);
      Result := GetFileSize(hand, nil);
    finally
      try
        if (hand <> 0) then CloseHandle(hand);
      except
        Result := -1;
      end;
    end;
  end;
end;

{$IFNDEF VER150}
{ ----------------------------------------------------------------------------
  Return part of a string
  ---------------------------------------------------------------------------- }
function AnsiMidStr(Source: String; Start: Integer; Count: Integer): String;
begin
  // Not perfectly accurate, but does the job
  { ^ What does that mean? // Zipplet }
  Result := Copy(Source, Start, Count);
end;
{$ENDIF}

{ ----------------------------------------------------------------------------
  Downloads the JRE. Returns TRUE if the user installed it. False otherwise
  ---------------------------------------------------------------------------- }
function downloadJRE(message: String = 'Would you like to download the java JRE?'): boolean;
var
  ProcessInfo: TProcessInformation;
  processResult: Longword;
  url: String;
  dir: String;
  line: String;
  f: TextFile;
  bits: TStringList;
  match: boolean;
  wantedsize: double;
  currentsize: double;
   lastsize: double;
   i: double;
   c: longint;
begin
  dir := IncludeTrailingPathDelimiter(ExtractFileDir(paramstr(0)));
  url := 'http://www.dmdirc.com/getjava/windows/all';
  Result := false;

  { First we will determine the approximate size of the download.
    In my opinion we should not do this until we have asked the user if they
    would like to download the JRE. Might change this later.
    We obtain the size by asking wget to find out. }
  ExecAndWait('wget.exe -o "'+dir+'wgetoutput" --spider '+url, true);

  { Just incase wget fails ... }
  if not fileexists(dir+'wgetoutput') then begin
    showerror('Internal error: wget returned no output.');
    result := false;
    exit;
  end;

  { Parse the output and grab the approximate size }
  AssignFile(f, dir+'wgetoutput');
  Reset(f);
  line := '';
  match := false;
  while not Eof(f) do begin
    ReadLn(f, line);
    if length(line) > 8 then begin
      if copy(line, 1, 7) = 'Length:' then begin
        match := true;
        break;
      end;
    end;
  end;
  if match then begin
    bits := TStringList.create;
    try
      bits.Clear;
      bits.Delimiter := ' ';
      bits.DelimitedText := line;
      try
        wantedsize := strtoint(StringReplace(bits[1], ',', '', [rfReplaceAll]))
      except
        wantedsize := 0;
      end;

      { We ask the user if they wish to download the JRE }
      if askQuestion(message+' (Download Size: '+AnsiMidStr(bits[2], 2, length(bits[2])-2)+')') then begin
        { Create progress window and show it }
        CreateMainWindow;
        { Get wget to start the download }
        ProcessInfo := Launch('wget.exe '+url+' -O jre.exe', true);
        labelurl.caption := url;
        labelspeed.caption := 'Connecting to site...';

        { Why is this case needed ?! }
        if wantedsize <= 0 then begin
          progressbar.progress := 50;
        end;
        getExitCodeProcess(ProcessInfo.hProcess, processResult);

        lastsize := 0;
        c := 0;
        i := 0;
        while (processResult = STILL_ACTIVE) and (not terminateDownload) do begin
          if wantedsize > 0 then begin
            currentsize := GetFileSizeByName(dir + 'jre.exe');
            inc(c);
            if (c >= 5) then begin
              i := (i + currentsize - lastsize) / 2;
              labelspeed.caption := nicesize(round(i * 2)) + '/sec';
              lastsize := currentsize;
              c := 0;
            end;
            if (currentsize > 0) then setProgress(Round((currentsize/wantedsize)*100),
              nicesize(currentsize) + ' of ' + nicesize(wantedsize) +
              ' (' + inttostr(Round((currentsize/wantedsize)*100)) + '%)');
          end;
          { We must process the message loop or the window wont respond to the user }
          applet.ProcessMessages;
          { Sleep to prevent 100% CPU usage }
          sleep(100);
          GetExitCodeProcess(ProcessInfo.hProcess, processResult);
        end;
        frmmain.visible := false;
        applet.visible := false;
        if (terminateDownload) then begin
          Result := false;
          TerminateProcess(ProcessInfo.hProcess, 0);
          showError('JRE Download was aborted', false);
        end
        else Result := processResult = 0;
        if not Result then begin
          if not terminateDownload then begin
            showError('JRE Download Failed', false);
          end
          else begin
            // If the download was cancelled by the form, this error will already
            // have been given.
            { No action needed here anymore }
          end;
        end;
      end;
    finally
      bits.free;
    end;
  end;
end;

{ ----------------------------------------------------------------------------
  Begin the JRE download/install.
  ---------------------------------------------------------------------------- }
function installJRE(isUpgrade: boolean): boolean;
var
  question: String;
  needDownload: boolean;
  canContinue: boolean;
begin
  Result := false;
  needDownload := not FileExists(IncludeTrailingPathDelimiter(ExtractFileDir(paramstr(0)))+'jre.exe');
  if needDownload then begin
    if not isUpgrade then question := 'Java was not detected on your machine. Would you like to download and install it now?'
    else question := 'The version of java detected on your machine is not compatible with DMDirc. Would you like to download and install a compatible version now?';
  end
  else begin
    if not isUpgrade then question := 'Java was not detected on your machine. Would you like to install it now?'
    else question := 'The version of java detected on your machine is not compatible with DMDirc. Would you like to install a compatible version now?';
  end;

  canContinue := true;
  if (needDownload) then begin
    canContinue := downloadJRE(question);
  end;

  if canContinue then begin
    // Final result of this function is the return value of installing java.
    if needDownload or askQuestion(question) then begin
      showmessage('The Java installer will now run. Please follow the instructions given. '+#13#10+'The DMDirc installation will continue afterwards.');
      Result := (ExecAndWait('jre.exe') = 0);
    end;
  end
end;

var
  errorMessage: String;
  javaCommand: String = 'javaw.exe';
  params: String = '';
  dir: String = '';
  Reg: TRegistry;
  result: Integer;
begin

  errorMessage := '';
  if FileExists('DMDirc.jar') then begin
    {$IFDEF FORCEJREDOWNLOAD}if (1 <> 0) then begin{$ELSE}if (ExecAndWait(javaCommand+' -version') <> 0) then begin{$ENDIF}
      if not installJRE(false) then begin
        showError('DMDirc setup can not continue without Java. Please install Java and try again.', false, false);
        exit;
      end;
    end;

    Reg := TRegistry.Create;
    Reg.RootKey := HKEY_LOCAL_MACHINE;
    if Reg.OpenKey('SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\DMDirc', false) then begin
      dir := Reg.ReadString('InstallDir');
      if (dir <> '') then begin
        params := params+' --directory "'+dir+'"';
      end;
    end;
    Reg.CloseKey;
    Reg.Free;
    if (ReleaseNumber <> '') then begin
      params := params+' --release '+ReleaseNumber;
    end;
    // Check if the installer runs
    if (ExecAndWait(javaCommand+' -cp DMDirc.jar com.dmdirc.installer.Main --help') <> 0) then begin
      if not installJRE(true) then begin
        showError('Sorry, DMDirc setup can not continue without an updated version of java.', false, false);
        exit;
      end
      else begin
        // Try again now that java is installed.
        result := ExecAndWait(javaCommand+' -cp DMDirc.jar com.dmdirc.installer.Main '+params);
      end;
    end
    else begin
      // Java is the right version, run the installer
      result := ExecAndWait(javaCommand+' -cp DMDirc.jar com.dmdirc.installer.Main '+params);
    end;
  end
  else begin
    errorMessage := errorMessage+'DMDirc.jar was not found.';
    errorMessage := errorMessage+#13#10;
    errorMessage := errorMessage+#13#10+'This is likely because of a corrupt installer build.';
    errorMessage := errorMessage+#13#10+'Please check http://www.dmdirc.com/ for an updated build.';
    showError(errorMessage);
  end;
end.
