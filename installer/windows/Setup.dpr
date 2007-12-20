{*
 * This application launches the dmdirc java-based installer.
 * 
 * DMDirc - Open Source IRC Client
 * Copyright (c) 2006-2007 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

uses Windows, SysUtils, classes, registry;

const
{$I SetupConsts.inc}
// This is also part of the above work-around.
{$IFDEF APP_CONSOLE}
	IsConsole: boolean = true;
{$ELSE}
	IsConsole: boolean = false;
{$ENDIF}

// Run an application and wait for it to finish.
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

procedure dowriteln(line: String);
begin
	if IsConsole then writeln(line);
end;

procedure dowrite(line: String);
begin
	if IsConsole then write(line);
end;

procedure showError(ErrorMessage: String);
begin
	if IsConsole then begin
		writeln('');
		writeln('-----------------------------------------------------------------------');
		writeln('Sorry, setup is unable to continue.!');
		writeln('-----------------------------------------------------------------------');
		writeln('Reason:');
		writeln('----------');
		writeln(ErrorMessage);
		writeln('-----------------------------------------------------------------------');
		writeln('If you feel this is incorrect, or you require some further assistance,');
		writeln('please feel free to contact us.');
		writeln('-----------------------------------------------------------------------');
		readln();
	end
	else begin
		ErrorMessage := ErrorMessage+#13#10;
		ErrorMessage := ErrorMessage+#13#10+'If you feel this is incorrect, or you require some further assistance,';
		ErrorMessage := ErrorMessage+#13#10+'please feel free to contact us.';
		
		MessageBox(0, PChar(ErrorMessage), 'Sorry, setup is unable to continue', MB_OK + MB_ICONSTOP);
	end;
end;

var
	errorMessage: String;
	javaCommand: String = 'javaw.exe';
	params: String = '';
	dir: String = '';
	Reg: TRegistry;
begin
	// Nice and simple
		
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
	dowrite('Checking for installer.jar.. ');
	if FileExists('installer.jar') then begin
		dowriteln('Success!');
		dowrite('Checking for JVM.. ');
		if (ExecAndWait(javaCommand+' -version') <> 0) then begin
			dowriteln('Failed!');
			errorMessage := errorMessage+'No JVM is currently installed.';
			errorMessage := errorMessage+#13#10;
			errorMessage := errorMessage+#13#10+'DMDirc requires a 1.6.0 compatible JVM, you can get one from:';
			errorMessage := errorMessage+#13#10+'http://jdl.sun.com/webapps/getjava/BrowserRedirect';
		end
		else begin
			if IsConsole then begin
				writeln('Success!');
				write('Starting installer.jar.. ');
				javaCommand := 'java.exe';
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
			if (ExecAndWait(javaCommand+' -jar installer.jar'+params) <> 0) then begin
				dowriteln('Failed!');
				errorMessage := errorMessage+'The currently installed version of java is not compatible with DMDirc.';
				errorMessage := errorMessage+#13#10;
				errorMessage := errorMessage+#13#10+'DMDirc requires a 1.6.0 compatible JVM, you can get one from:';
				errorMessage := errorMessage+#13#10+'http://jdl.sun.com/webapps/getjava/BrowserRedirect';
				showError(errorMessage);
			end;
		end;
	end
	else begin
		dowriteln('Failed!');
		errorMessage := errorMessage+'installer.jar was not found.';
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
