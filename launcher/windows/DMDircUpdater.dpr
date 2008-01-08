program DMDircUpdater;
{$MODE Delphi}
{$APPTYPE GUI}

{$R UAC.rc}

uses Windows, SysUtils, classes, StrUtils;

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
	MessageBox(0, pchar('Foo: '+sProgramToRun), 'Update Failed', MB_ICONSTOP);
	CreateProcess(nil, PChar(sProgramToRun), nil, nil, False, NORMAL_PRIORITY_CLASS, nil, nil, StartupInfo, ProcessInfo);
end;

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
					MessageBox(0, 'Unable to overwrite launcher', 'Launcher Update Failed', MB_ICONSTOP);
				end;
			end
			else begin
				launcherUpdate := true;
				if FileExists(pchar(thisDir+'\DMDirc.exe')) then begin
					if not DeleteFile(pchar(thisDir+'\DMDirc.exe')) then begin
						MessageBox(0, 'Unable to delete DMDirc.exe', 'Update Failed', MB_ICONSTOP);
					end;
				end;
				
				if not FileExists(pchar(thisDir+'\DMDirc.exe')) and MoveFile(pchar(sourceDir+'\.DMDirc.exe'), pchar(thisDir+'\DMDirc.exe')) then begin
					if FileExists(pchar(thisDir+'\DMDircUpdater.exe')) then begin
						if not DeleteFile(pchar(thisDir+'\DMDircUpdater.exe')) then begin
							MessageBox(0, 'Unable to delete DMDircUpdater.exe', 'Update Failed', MB_ICONSTOP);
						end;
					end;
					if not FileExists(pchar(thisDir+'\DMDircUpdater.exe')) and MoveFile(pchar(sourceDir+'\.DMDircUpdater.exe'), pchar(thisDir+'\DMDircUpdater.exe')) then begin
						MessageBox(0, 'Launcher update was successful.', 'Launcher Update Completed', MB_OK);
					end
					else begin
						MessageBox(0, pchar('Unable to update DMDircUpdater.exe'), 'Launcher Update Failed', MB_ICONSTOP);
					end;
				end
				else begin
					MessageBox(0, pchar('Unable to update DMDirc.exe'), 'Launcher Update Failed', MB_ICONSTOP);
				end;
			end;
		end;
		
		// Look for client update
		if canDoMore then begin
			if FileExists(pchar(sourceDir+'\.DMDirc.jar')) then begin
				if FileExists(pchar(jarName)) then begin
					if not DeleteFile(pchar(jarName)) then begin
						MessageBox(0, 'Unable to delete DMDirc.jar', 'Update Failed', MB_ICONSTOP);
					end;
				end;
				
				if MoveFile(pchar(sourceDir+'\.DMDirc.jar'), pchar(jarName)) then begin
					MessageBox(0, 'Client update was successful.', 'Update Completed', MB_OK);
				end
				else begin
					MessageBox(0, pchar('Unable to move '''+sourceDir+'\.DMDirc.jar'' to '+jarName), 'Update Failed', MB_ICONSTOP);
				end;
			end;
			
			if launcherUpdate then begin
				MessageBox(0, 'The DMDirc launcher has been updated, to complete the update please relaunch DMDirc.', 'Restart Required', MB_OK);
			end;
		end;
	end
	else begin
		MessageBox(0, 'This program can not be run on its own.', 'Error', MB_ICONSTOP);
	end;
end.