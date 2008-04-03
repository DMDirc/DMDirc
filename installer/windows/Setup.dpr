{*
 * This application launches the dmdirc java-based installer.
 * 
 * DMDirc - Open Source IRC Client
 * Copyright (c) 2006-2008 Chris Smith, Shane Mc Cormack, Gregory Holmes
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
{$MODE Delphi}
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

uses 
	{$IFDEF LAZARUS}Interfaces, Forms, ComCtrls, Buttons, Messages, Controls, StdCtrls,{$ENDIF}
	Windows, SysUtils, classes, registry, strutils;

{$IFDEF LAZARUS}
	type
		TProgressForm = class(TForm)
			ProgressBar: TProgressBar;
			CancelButton: TButton;
			CaptionLabel: TLabel;
			constructor Create(AOwner: TComponent); override;
		private
			procedure onButtonClick(Sender: TObject);
		public
			procedure setProgress(value: integer);
		end;
{$ENDIF}

const
{$I SetupConsts.inc}
// This is also part of the above work-around.
{$IFDEF APP_CONSOLE}
	IsConsole: boolean = true;
{$ELSE}
	IsConsole: boolean = false;
{$ENDIF}

var
{$IFDEF LAZARUS} form: TProgressForm; {$ENDIF}
	terminateDownload: boolean = false;
	
{$IFDEF LAZARUS}
	constructor TProgressForm.Create(AOwner: TComponent);
	begin
		inherited;
		self.Width := 500;
		self.Height := 80;
		self.Position := poScreenCenter;
		self.BorderStyle := bsSingle;
		CaptionLabel := TLabel.create(self);
		CaptionLabel.Parent := self;
		CaptionLabel.Width := 490;
		CaptionLabel.Height := 15;
		CaptionLabel.Top := 5;
		CaptionLabel.Left := 5;
		CaptionLabel.Caption := 'Downloading JRE - 0%';
		
		ProgressBar := TProgressBar.create(self);
		ProgressBar.Parent := self;
		ProgressBar.Width := 490;
		ProgressBar.Height := 20;
		ProgressBar.Top := CaptionLabel.Top+CaptionLabel.Height+5;
		ProgressBar.Left := 5;
		ProgressBar.Visible := true;
		ProgressBar.Max := 100;
		ProgressBar.Position := 0;
		
		CancelButton := TButton.create(self);
		CancelButton.Parent := self;
		CancelButton.Width := 80;
		CancelButton.Height := 25;
		CancelButton.Top := ProgressBar.Top+ProgressBar.Height+5;
		CancelButton.Left := Round((self.Width/2) - (CancelButton.Width/2));
		CancelButton.Visible := true;
		CancelButton.Caption := 'Cancel';
		CancelButton.onClick := self.onButtonClick;
		
		self.Caption := pChar('DMDirc Setup - '+CaptionLabel.Caption);;
		Application.Title := self.Caption;
	end;
	
	procedure TProgressForm.onButtonClick(Sender: TObject);
	begin
		terminateDownload := true;
	end;
	
	procedure TProgressForm.setProgress(value: integer);
	begin
		ProgressBar.Position := value;
		CaptionLabel.Caption := pchar('Downloading JRE - '+inttostr(value)+'%');
		self.Caption := pChar('DMDirc Setup - '+CaptionLabel.Caption);;
		Application.Title := self.Caption;
	end;
{$ENDIF}

function askQuestion(Question: String): boolean;
begin
	Result := MessageBox(0, PChar(Question), 'DMDirc Setup', MB_YESNO or MB_ICONQUESTION) = IDYES;
end;

procedure showError(ErrorMessage: String; addFooter: boolean = true);
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
		readln();
	end
	else begin
		if addFooter then begin
			ErrorMessage := ErrorMessage+#13#10;
			ErrorMessage := ErrorMessage+#13#10+'If you feel this is incorrect, or you require some further assistance,';
			ErrorMessage := ErrorMessage+#13#10+'please feel free to contact us.';
		end;
		
		MessageBox(0, PChar(ErrorMessage), 'Sorry, setup is unable to continue', MB_OK + MB_ICONSTOP);
	end;
end;

procedure showmessage(message: String);
begin
	if IsConsole then begin
		writeln('');
		writeln('-----------------------------------------------------------------------');
		writeln('Information:!');
		writeln('-----------------------------------------------------------------------');
		writeln(message);
		writeln('-----------------------------------------------------------------------');
		readln();
	end
	else begin
		MessageBox(0, PChar(message), 'DMDirc Setup', MB_OK + MB_ICONINFORMATION);
	end;
end;

// Run an application and wait for it to finish.
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
	Result := 0;
	if FileExists(name) then begin
		try
			hand := CreateFile(PChar(name), GENERIC_READ, FILE_SHARE_WRITE or FILE_SHARE_READ, nil, OPEN_EXISTING, FILE_ATTRIBUTE_NORMAL, 0);
			Result := GetFileSize(hand, nil);
		finally
			try
				CloseHandle(hand);
			except
				Result := -1;
			end;
		end;
	end;
end;

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
	{$IFDEF LAZARUS}
		wantedsize: double;
		currentsize: double;
	{$ENDIF}
begin
	dir := IncludeTrailingPathDelimiter(ExtractFileDir(paramstr(0)));
	url := 'http://www.dmdirc.com/getjava/windows/all';
	Result := false;
	ExecAndWait('wget.exe -o '+dir+'wgetoutput --spider '+url, true);
	
	AssignFile(f, dir+'wgetoutput');
	Reset(f);
	line := '';
	match := false;
	while not Eof(f) do begin
		ReadLn(f, line);
		match := IsWild(line,'Length:*',True);
		if match then break;
	end;
	if match then begin
		bits := TStringList.create;
		try
			bits.Clear;
			bits.Delimiter := ' ';
			bits.DelimitedText := line;
			{$IFDEF LAZARUS}
				try
					wantedsize := strtoint(StringReplace(bits[1], ',', '', [rfReplaceAll]))
				except
					wantedsize := 0;
				end;
			{$ENDIF}
			if askQuestion(message+' (Download Size: '+AnsiMidStr(bits[2], 2, length(bits[2])-2)+')') then begin
				{$IFDEF LAZARUS}
					ProcessInfo := Launch('wget.exe '+url+' -O jre.exe', true);
					form.show();
					if wantedsize <= 0 then begin
						form.setProgress(50);
					end;
				{$ELSE}
					ProcessInfo := Launch('wget.exe '+url+' -O jre.exe');
				{$ENDIF}
				getExitCodeProcess(ProcessInfo.hProcess, processResult);
			
				while (processResult=STILL_ACTIVE) and (not terminateDownload) do begin
					// Update progress bar.
					{$IFDEF LAZARUS}
						if wantedsize > 0 then begin
							currentsize := GetFileSizeByName('jre.exe');
							if (currentsize > 0) then form.setProgress(Round((currentsize/wantedsize)*100));
						end;
						Application.ProcessMessages;
					{$ENDIF}
					sleep(10);
					GetExitCodeProcess(ProcessInfo.hProcess, processResult);
				end;
				{$IFDEF LAZARUS}form.hide();{$ENDIF}
				if (terminateDownload) then begin
					Result := false;
					{$IFDEF LAZARUS}
						TerminateProcess(ProcessInfo.hProcess, 0);
						showError('JRE Download was aborted', false);
					{$ENDIF}
				end
				else Result := processResult = 0;
				if not Result then showError('JRE Download Failed', false);
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
			showmessage('The Java installer will now run. Please follow the instructions given.'+#13#10+'The DMDirc installation will continue afterwards.');
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
begin
	{$IFDEF LAZARUS}
		Application.Initialize;
		Application.CreateForm(TProgressForm, form);
	{$ENDIF}
		
	if IsConsole then begin
		writeln('-----------------------------------------------------------------------');
		writeln('Welcome to the DMDirc installer.');
		writeln('-----------------------------------------------------------------------');
		writeln('This will install DMDirc on your computer.');
		writeln('');
		writeln('Please wait whilst the GUI part of the installer loads...');
		writeln('-----------------------------------------------------------------------');
//	end
//	else begin
//		errorMessage := 'This will install DMDirc on your computer, please click OK to continue, or Cancel to abort.';
//		if (MessageBox(0, PChar(errorMessage), 'DMDirc Installer', MB_OKCANCEL + MB_ICONINFORMATION) <> IDOK) then begin
//			exit;
//		end;
	end;
	errorMessage := '';
	dowrite('Checking for DMDirc.jar.. ');
	if FileExists('DMDirc.jar') then begin
		dowriteln('Success!');
		dowrite('Checking for JVM.. ');
		if (ExecAndWait(javaCommand+' -version') <> 0) then begin
			dowriteln('Failed!');
			if not installJRE(false) then begin
				showError('Sorry, DMDirc setup can not continue without java', false);
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
		if (ExecAndWait(javaCommand+' -cp DMDirc.jar com.dmdirc.installer.Main '+params) <> 0) then begin
			dowriteln('Failed!');
			if not installJRE(true) then begin
				showError('Sorry, DMDirc setup can not continue without an updated version of java', false);
				exit;
			end
			else begin
				// Try again now that java is installed.
				ExecAndWait(javaCommand+' -cp DMDirc.jar com.dmdirc.installer.Main '+params);
			end;
		end;
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
