program DMDirc;
{$MODE Delphi}
{$APPTYPE GUI}

uses Windows, SysUtils, classes;

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

// Run an application and don't wait for it to finish.
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

var
	errorMessage: String;
	javaCommand: String = 'javaw.exe';
	cliParams: String = '';
	directory: String = '';
	i: integer;
	hK32: THandle;
begin
	directory := GetEnvironmentVariable('APPDATA')+'\DMDirc';
	if ParamCount > 0 then begin
		for i := 1 to ParamCount do begin
			cliParams := cliParams+paramstr(i);
			if (paramstr(i) = '-d') or (paramstr(i) = '--directory') then begin
				if ParamCount > i then begin
					directory := paramstr(i+1);
				end;
			end
		end;
	end;

	// Update if needed.
	if FileExists(directory+'\.DMDirc.jar') then begin
		// Vista Sucks.
		hK32 := GetModuleHandle('kernel32');
		if GetProcAddress(hK32, 'GetLocaleInfoEx') <> nil then begin
			// Vista >.<
			// Try and delete the old file, if it fails then the user needs to give
			// us permission to delete the file (UAC), otherwise we can just go ahead
			// and run the updater.
			if not DeleteFile('DMDirc.jar') then begin
				errorMessage := 'An update to DMDirc has been previously downloaded.';
				errorMessage := errorMessage+#13#10;
				errorMessage := errorMessage+#13#10+'As you are running Windows Vista, DMDirc requires administer access to';
				errorMessage := errorMessage+#13#10+'complete the update.';
				errorMessage := errorMessage+#13#10;
				errorMessage := errorMessage+#13#10+'Please click ''Allow'' on the UAC prompt to complete the update, or click no';
				errorMessage := errorMessage+#13#10+'here to continue without updating.';
				if MessageBox(0, PChar(errorMessage), 'Windows Vista', MB_YESNO) = IDYES then begin
					ExecAndWait('DMDircUpdater.exe '+directory+'\.DMDirc.jar');
				end;
			end
			else ExecAndWait('DMDircUpdater.exe '+directory+'\.DMDirc.jar');
		end
		else ExecAndWait('DMDircUpdater.exe '+directory+'\.DMDirc.jar');
	end;
	
	// Check JVM
	if (ExecAndWait(javaCommand+' -version') <> 0) then begin
		errorMessage := 'No JVM is currently installed.';
		errorMessage := errorMessage+#13#10;
		errorMessage := errorMessage+#13#10+'DMDirc requires a 1.6.0 compatible JVM, you can get one from:';
		errorMessage := errorMessage+#13#10+'http://jdl.sun.com/webapps/getjava/BrowserRedirect';
		errorMessage := errorMessage+#13#10;
		errorMessage := errorMessage+#13#10+'If you feel this is incorrect, or you require some further assistance,';
		errorMessage := errorMessage+#13#10+'please feel free to contact us.';
		
		MessageBox(0, PChar(errorMessage), 'Sorry, DMDirc is unable to continue', MB_OK + MB_ICONSTOP);
	end
	// Else try and run client. (This only asks for help output to check that client
	// runs on this OS, otherwise later segfaults or so would cause the error to
	// appear
	else if FileExists('DMDirc.jar') then begin
		if (ExecAndWait(javaCommand+' -jar DMDirc.jar --help') <> 0) then begin
			errorMessage := 'The currently installed version of java is not compatible with DMDirc.';
			errorMessage := errorMessage+#13#10;
			errorMessage := errorMessage+#13#10+'DMDirc requires a 1.6.0 compatible JVM, you can get one from:';
			errorMessage := errorMessage+#13#10+'http://jdl.sun.com/webapps/getjava/BrowserRedirect';
			errorMessage := errorMessage+#13#10;
			errorMessage := errorMessage+#13#10+'If you feel this is incorrect, or you require some further assistance,';
			errorMessage := errorMessage+#13#10+'please feel free to contact us.';
			
			MessageBox(0, PChar(errorMessage), 'Sorry, DMDirc is unable to continue', MB_OK + MB_ICONSTOP);
		end
		else begin
			Launch(javaCommand+' -jar DMDirc.jar '+cliParams)
		end;
	end
	else begin
		errorMessage := 'Your DMDirc installation has been broken. DMDirc.jar no longer exists.';
		MessageBox(0, PChar(errorMessage), 'Sorry, DMDirc is unable to continue', MB_OK + MB_ICONSTOP);
	end;
end.