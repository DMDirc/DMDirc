{*
 * This application launches the dmdirc java-based installer.
 *
 * DMDirc - Open Source IRC Client
 * Copyright (c) 2006-2009 Chris Smith, Shane Mc Cormack, Gregory Holmes
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
program Setup;

{$R most.res}

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

// If this is defined, lazarus-specific code (gui progress bar) will be compiled
// without it, a wget console window will be used for progress instead.
// This is automatically set by the build script when lazarus is detected in /usr/lib/lazarus
// You can forcibly define or undefine it here.
// {$DEFINE LAZARUS}
// {$UNDEF LAZARUS}

//{$DEFINE FORCEJREDOWNLOAD}

uses
	{$IFDEF KOL}kol,{$ENDIF}
	Vista, Windows, SysUtils, classes, registry, strutils {$IFNDEF FPC},masks{$ENDIF};


const
{$I SetupConsts.inc}
// This is also part of the above work-around.
{$IFDEF APP_CONSOLE}
	IsConsole: boolean = true;
{$ELSE}
	IsConsole: boolean = false;
{$ENDIF}

var
  {$IFDEF KOL}
    frmmain: pcontrol;
    progressbar, btncancel: pcontrol;
    label1, label2, label3, label4, label5, label6, label7: pcontrol;
  {$ENDIF}
	terminateDownload: boolean = false;

{$IFDEF KOL}
procedure btnCancel_Click(Dummy: Pointer; Sender: PControl);
begin
  { Button clicked }
  terminateDownload := true;
end;

procedure setProgress(value: integer);
begin
  ProgressBar.progress := value;
  //CaptionLabel.Caption := pchar('Downloading JRE - '+inttostr(value)+'%');
  //self.Caption := pChar('DMDirc Setup - '+CaptionLabel.Caption);
  //Application.Title := self.Caption;
  applet.processmessages;
end;

procedure CreateMainWindow;
var
  screenw, screenh: longint;
begin
  InitCommonControls;

  screenw := GetSystemMetrics(SM_CXSCREEN);
  screenh := GetSystemMetrics(SM_CYSCREEN);

  Applet := NewApplet('DMDirc Setup');
  Applet.Visible := true;
  Applet.Icon := THandle(-1);

  frmmain := NewForm( Applet, 'DMDirc Setup').SetClientSize(400, 184);
  frmmain.CreateVisible := True;
  frmmain.CanResize := False;
  frmmain.Style := frmmain.style and (not WS_MAXIMIZEBOX);
  frmmain.Font.FontName := 'Ms Sans Serif';
  frmmain.Font.FontHeight := 8;
  frmmain.SetPosition((screenw div 2) - (frmmain.Width div 2), (screenh div 2) - (frmmain.height div 2));
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

  label5 := NewLabel(frmmain, 'http://java.ftw.com/foo/').SetPosition(70, label1.top + 28);
  label5.SetSize(frmmain.ClientWidth - 32, 16);
  label5.BringToFront;

  label6 := NewLabel(frmmain, '328KByte/sec').SetPosition(70, label2.top + 20);
  label6.SetSize(frmmain.ClientWidth - 32, 16);
  label6.BringToFront;

  label7 := NewLabel(frmmain, '500KByte of 10.4MByte (50%)').SetPosition(70, label3.top + 20);
  label7.SetSize(frmmain.ClientWidth - 32, 16);
  label7.BringToFront;

  btncancel.OnClick := TOnEvent(MakeMethod(nil, @btnCancel_Click ));
end;
{$ENDIF}

function askQuestion(Question: String): boolean;
begin
	Result := TaskDialog(0, 'DMDirc Setup', 'Question', Question, TD_ICON_QUESTION, TD_BUTTON_YES + TD_BUTTON_NO) = mrYes;
end;

procedure showError(ErrorMessage: String; addFooter: boolean = true; includeDescInXP: boolean = true);
begin
	if IsConsole then begin
		writeln('');
		writeln('-----------------------------------------------------------------------');
		writeln('Sorry, setup is unable to continue.!');
		writeln('-----------------------------------------------------------------------');
		writeln('Reason:');
		writeln('----------');
		writeln(ErrorMessage);
		if addFooter then begin
			writeln('-----------------------------------------------------------------------');
			writeln('If you feel this is incorrect, or you require some further assistance,');
			writeln('please feel free to contact us.');
		end;
		writeln('-----------------------------------------------------------------------');
		readln;
	end
	else begin
		if addFooter then begin
			ErrorMessage := ErrorMessage+#13#10;
			ErrorMessage := ErrorMessage+#13#10+'If you feel this is incorrect, or you require some further assistance,';
			if not IsWindowsVista then ErrorMessage := ErrorMessage+#13#10;
			ErrorMessage := ErrorMessage+'please feel free to contact us.';
		end;
		
		TaskDialog(0, 'DMDirc Setup', 'Sorry, setup is unable to continue.', ErrorMessage, TD_ICON_ERROR, TD_BUTTON_OK, includeDescInXP, false);
	end;
end;

procedure showmessage(message: String; context:String = 'Information');
begin
	if IsConsole then begin
		writeln('');
		writeln('-----------------------------------------------------------------------');
		writeln(context+':');
		writeln('-----------------------------------------------------------------------');
		writeln(message);
		writeln('-----------------------------------------------------------------------');
		readln;
	end
	else begin
		TaskDialog(0, 'DMDirc Setup', context, message, TD_ICON_INFORMATION, TD_BUTTON_OK);
	end;
end;

// Run an application and don't wait for it to finish.
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

// Run an application and wait for it to finish.
function ExecAndWait(sProgramToRun: String; hide: boolean = false): Longword;
var
	ProcessInfo: TProcessInformation;
begin
	ProcessInfo := Launch(sProgramToRun, hide);
	getExitCodeProcess(ProcessInfo.hProcess, Result);

	while Result=STILL_ACTIVE do begin
		sleep(1000);
		GetExitCodeProcess(ProcessInfo.hProcess, Result);
	end;
end;

procedure dowriteln(line: String);
begin
	if IsConsole then writeln(line);
end;

procedure dowrite(line: String);
begin
	if IsConsole then write(line);
end;

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

function DoMatch(Input: String; Wildcard: String): boolean;
begin
	{$ifdef FPC}
		Result := IsWild(Input,Wildcard,True);
	{$else}
		Result := MatchesMask(Input,Wildcard);
	{$endif}
end;

{$IFNDEF VER150}
function AnsiMidStr(Source: String; Start: Integer; Count: Integer): String;
begin
	// Not perfectly accurate, but does the job
	Result := Copy(Source, Start, Count);
end;
{$ENDIF}

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
	{$IFDEF KOL}
		wantedsize: double;
		currentsize: double;
	{$ENDIF}
begin
	dir := IncludeTrailingPathDelimiter(ExtractFileDir(paramstr(0)));
	url := 'http://www.dmdirc.com/getjava/windows/all';
	Result := false;
	ExecAndWait('wget.exe -o "'+dir+'wgetoutput" --spider '+url, true);

  if not fileexists(dir+'wgetoutput') then begin
    showerror('Internal error: wget returned no output.');
    result := false;
    exit;
  end;
	AssignFile(f, dir+'wgetoutput');
	Reset(f);
	line := '';
	match := false;
	while not Eof(f) do begin
		ReadLn(f, line);
		match := DoMatch(line,'Length:*');
		if match then break;
	end;
	if match then begin
		bits := TStringList.create;
		try
			bits.Clear;
			bits.Delimiter := ' ';
			bits.DelimitedText := line;
			{$IFDEF KOL}
				try
					wantedsize := strtoint(StringReplace(bits[1], ',', '', [rfReplaceAll]))
				except
					wantedsize := 0;
				end;
			{$ENDIF}
			if askQuestion(message+' (Download Size: '+AnsiMidStr(bits[2], 2, length(bits[2])-2)+')') then begin
				{$IFDEF KOL}
					ProcessInfo := Launch('wget.exe '+url+' -O jre.exe', true);
          CreateMainWindow;
					if wantedsize <= 0 then begin
            progressbar.progress := 50;
					end;
				{$ELSE}
					ProcessInfo := Launch('wget.exe '+url+' -O jre.exe');
				{$ENDIF}
				getExitCodeProcess(ProcessInfo.hProcess, processResult);

				while (processResult=STILL_ACTIVE) and (not terminateDownload) do begin
					// Update progress bar.
					{$IFDEF KOL}
						if wantedsize > 0 then begin
							currentsize := GetFileSizeByName('jre.exe');
							if (currentsize > 0) then setProgress(Round((currentsize/wantedsize)*100));
						end;
						applet.ProcessMessages;
					{$ENDIF}
					sleep(10);
					GetExitCodeProcess(ProcessInfo.hProcess, processResult);
				end;
				{$IFDEF KOL}frmmain.visible := false;{$ENDIF}
				if (terminateDownload) then begin
					Result := false;
					{$IFDEF KOL}
						TerminateProcess(ProcessInfo.hProcess, 0);
						showError('JRE Download was aborted', false);
					{$ENDIF}
				end
				else Result := processResult = 0;
				if not Result then begin
					if not terminateDownload then begin
						showError('JRE Download Failed', false);
					end
					else begin
						// If the download was cancelled by the form, this error will already
						// have been given.
						{$IFNDEF KOL}
							showError('JRE Download was aborted', false);
						{$ENDIF}
					end;
				end;
			end;
		finally
			bits.free;
		end;
	end;
end;

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

	if IsConsole then begin
		writeln('-----------------------------------------------------------------------');
		writeln('Welcome to the DMDirc installer.');
		writeln('-----------------------------------------------------------------------');
		writeln('This will install DMDirc on your computer.');
		writeln('');
		writeln('Please wait whilst the GUI part of the installer loads...');
		writeln('-----------------------------------------------------------------------');
	//end
	//else begin
	//	errorMessage := 'This will install DMDirc on your computer, please click OK to continue, or Cancel to abort.';
	//	if (MessageBox(0, PChar(errorMessage), 'DMDirc Installer', MB_OKCANCEL + MB_ICONINFORMATION) <> IDOK) then begin
	//		exit;
	//	end;
	end;

	errorMessage := '';
	dowrite('Checking for DMDirc.jar.. ');
	if FileExists('DMDirc.jar') then begin
		dowriteln('Success!');
		dowrite('Checking for JVM.. ');
    {$IFDEF FORCEJREDOWNLOAD}
		if (1 <> 0) then begin
    {$ELSE}
		if (ExecAndWait(javaCommand+' -version') <> 0) then begin
    {$ENDIF}
			dowriteln('Failed!');
			if not installJRE(false) then begin
				showError('DMDirc setup can not continue without java. Please install java and try again', false, false);
				exit;
			end;
		end
		else begin
			if IsConsole then begin
				writeln('Success!');
				write('Starting installer.. ');
				javaCommand := 'java.exe';
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
			dowriteln('Failed!');
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
		if result = 0 then dowriteln('Installation completed.')
		else dowriteln('Installation did not complete.')
	end
	else begin
		dowriteln('Failed!');
		errorMessage := errorMessage+'DMDirc.jar was not found.';
		errorMessage := errorMessage+#13#10;
		errorMessage := errorMessage+#13#10+'This is likely because of a corrupt installer build.';
		errorMessage := errorMessage+#13#10+'Please check http://www.dmdirc.com/ for an updated build.';
		showError(errorMessage);
	end;
	if IsConsole then begin
		writeln('');
		writeln('-----------------------------------------------------------------------');
		writeln('Installation Completed. Thank you for choosing DMDirc');
		writeln('-----------------------------------------------------------------------');	
	end;
end.
