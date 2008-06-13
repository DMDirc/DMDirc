program DMDircUpdater;
{$MODE Delphi}
{$APPTYPE GUI}

{$R UAC.rc}

uses Windows, SysUtils, classes, StrUtils, Vista;

function askQuestion(Question: String): boolean;
begin
	Result := TaskDialog(0, 'DMDirc Setup', 'Question', Question, TD_ICON_QUESTION, TD_BUTTON_YES + TD_BUTTON_NO) = mrYes;
end;

procedure showError(ErrorMessage: String; addFooter: boolean = true);
begin
	if addFooter then begin
		ErrorMessage := ErrorMessage+#13#10;
		ErrorMessage := ErrorMessage+#13#10+'If you feel this is incorrect, or you require some further assistance,';
		ErrorMessage := ErrorMessage+#13#10+'please feel free to contact us.';
	end;
	
	TaskDialog(0, 'DMDirc Setup', 'Sorry, setup is unable to continue', ErrorMessage, TD_ICON_ERROR, TD_BUTTON_OK, true);
end;

procedure showmessage(message: String; context:String = 'Information');
begin
	TaskDialog(0, 'DMDirc Setup', context, message, TD_ICON_INFORMATION, TD_BUTTON_OK);
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
	// MessageBox(0, pchar('Foo: '+sProgramToRun), 'Update Failed', MB_ICONSTOP);
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
					showmessage('Unable to overwrite launcher', 'Update Failed');
				end;
			end
			else begin
				launcherUpdate := true;
				if FileExists(pchar(thisDir+'\DMDirc.exe')) then begin
					if not DeleteFile(pchar(thisDir+'\DMDirc.exe')) then begin
						showmessage('Unable to delete DMDirc.exe', 'Launcher Update Failed');
					end;
				end;
				
				if not FileExists(pchar(thisDir+'\DMDirc.exe')) and MoveFile(pchar(sourceDir+'\.DMDirc.exe'), pchar(thisDir+'\DMDirc.exe')) then begin
					if FileExists(pchar(thisDir+'\DMDircUpdater.exe')) then begin
						if not DeleteFile(pchar(thisDir+'\DMDircUpdater.exe')) then begin
							showmessage('Unable to delete DMDircUpdater.exe', 'Launcher Update Failed');
						end;
					end;
					if not FileExists(pchar(thisDir+'\DMDircUpdater.exe')) and MoveFile(pchar(sourceDir+'\.DMDircUpdater.exe'), pchar(thisDir+'\DMDircUpdater.exe')) then begin
						showmessage('Launcher update was successful');
					end
					else begin
						showmessage('Unable to update DMDircUpdater.exe', 'Launcher Update Failed');
					end;
				end
				else begin
					showmessage('Unable to update DMDirc.exe', 'Launcher Update Failed');
				end;
			end;
		end;
		
		// Look for client update
		if canDoMore then begin
			if FileExists(pchar(sourceDir+'\.DMDirc.jar')) then begin
				if FileExists(pchar(jarName)) then begin
					if not DeleteFile(pchar(jarName)) then begin
						showmessage('Unable to update DMDirc.jar', 'Launcher Update Failed');
					end;
				end;
				
				if MoveFile(pchar(sourceDir+'\.DMDirc.jar'), pchar(jarName)) then begin
					showmessage('Client update was successful');
				end
				else begin
					showmessage('Unable to move '''+sourceDir+'\.DMDirc.jar'' to '+jarName, 'Update Failed');
				end;
			end;
			
			if launcherUpdate then begin
				showmessage('The DMDirc launcher has been updated, to complete the update please relaunch DMDirc.', 'Restart Required');
			end;
		end;
	end
	else begin
		showError('This program can not be run on its own.');
	end;
end.