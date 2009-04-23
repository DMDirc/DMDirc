program Uninstaller;
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

uses Windows, SysUtils, registry, Vista;

{$R uninstall.res}

procedure dowriteln(line: String);
begin
	if IsConsole then writeln(line);
end;

procedure dowrite(line: String);
begin
	if IsConsole then write(line);
end;

function askQuestion(Question: String): boolean;
begin
	Result := TaskDialog(0, 'DMDirc Uninstaller', 'Question', Question, TD_ICON_QUESTION, TD_BUTTON_YES + TD_BUTTON_NO) = mrYes;
end;

procedure showError(context: String; ErrorMessage: String; addFooter: boolean = true);
begin
	if addFooter then begin
		ErrorMessage := ErrorMessage+#13#10;
		ErrorMessage := ErrorMessage+#13#10+' If you feel this is incorrect, or you require some further assistance, ';
		ErrorMessage := ErrorMessage+#13#10+'please feel free to contact us.';
	end;
	
	TaskDialog(0, 'DMDirc Setup', context, ErrorMessage, TD_ICON_ERROR, TD_BUTTON_OK, true);
end;

procedure showmessage(message: String; context:String = 'Information');
begin
	TaskDialog(0, 'DMDirc Uninstaller', context, message, TD_ICON_INFORMATION, TD_BUTTON_OK);
end;

function GetTempDirectory(): String;
var
	buf: array[0..MAX_PATH] of Char;
	wintemp, temp: String;
begin
	GetTempPath(SizeOf(buf)-1, buf);
	wintemp := StrPas(buf);
	Randomize;
	temp := '\DMDirc-uninstaller-'+inttostr(1000 + Random(1000));
	while (DirectoryExists(wintemp+temp+'\')) do begin
		temp := temp+'-'+inttostr(1+Random(1000));
	end;
	MkDir(wintemp+temp+'\');
	result := wintemp+temp+'\';
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

function KillDir(Dir: string): Integer;
var
	searchResult: TSearchRec;
begin
	Result := 0;
	if FindFirst(Dir+'\*', faDirectory + faHidden  + faReadOnly + faSysfile + faAnyFile, searchResult) = 0 then
	begin
		repeat
			if (searchResult.attr and faDirectory) <> faDirectory then begin
				Try
					DeleteFile(Dir+'\'+searchResult.name);
				Except
					MessageBox(0, PChar('Unable to delete "'+Dir+'\'+searchResult.name+'" - is DMDirc still running?.'), 'DMDirc Uninstaller', MB_OK);
				end;
			end
			else begin
				if (searchResult.name <> '.') and (searchResult.name <> '..') then begin
					KillDir(Dir+'\'+searchResult.name);
				end;
			end;
		until FindNext(searchResult) <> 0;
		FindClose(searchResult);
	end;
	Try
		RmDir(Dir);
	Except
	end;
end;

var
	TempDir: String;
	InstallDir: String = '';
	i: Integer;
	Reg: TRegistry;
	handlerInfo: String;
	profileDir: String;
	deleteProtocol: boolean;
begin
	if (ParamCount > 0) then begin
		for i := 1 to ParamCount do begin
			InstallDir := InstallDir+' '+paramstr(i);
		end;
		InstallDir := trim(InstallDir);
		KillDir(InstallDir);
		profileDir := GetEnvironmentVariable('USERPROFILE');
		
		if IsWindowsVista then begin
			// Vista
			KillDir(GetEnvironmentVariable('APPDATA')+'\Microsoft\Windows\Start Menu\Programs\DMDirc');
			DeleteFile(GetEnvironmentVariable('USERPROFILE')+'\AppData\Roaming\Microsoft\Internet Explorer\Quick Launch\DMDirc.lnk');
			DeleteFile(GetEnvironmentVariable('USERPROFILE')+'\Desktop\DMDirc.lnk');
			profileDir := profileDir+'\AppData\Roaming\DMDirc';
		end
		else begin
			// Not Vista
			KillDir(GetEnvironmentVariable('USERPROFILE')+'\Microsoft\Windows\Start Menu\Programs\DMDirc');
			DeleteFile(GetEnvironmentVariable('USERPROFILE')+'\Application Data\Microsoft\Internet Explorer\Quick Launch\DMDirc.lnk');
			DeleteFile(GetEnvironmentVariable('USERPROFILE')+'\Desktop\DMDirc.lnk');
			profileDir := profileDir+'\Application Data\DMDirc';
		end;
		// Remove irc:// handler if it is us.
		deleteProtocol := false;
		Reg := TRegistry.Create;
		Reg.RootKey := HKEY_CLASSES_ROOT;
		if Reg.OpenKey('irc\Shell\open\command', false) then begin
			handlerInfo := Reg.ReadString('');
			if (handlerInfo = '"'+InstallDir+'DMDirc.exe" -c %1') then begin
				deleteProtocol := true;
			end
		end;
		Reg.CloseKey;
		Reg.Free;
		
		if deleteProtocol then begin
			Reg := TRegistry.Create;
			Reg.RootKey := HKEY_CLASSES_ROOT;
			Reg.DeleteKey('irc\Shell\open\command');
			Reg.DeleteKey('irc\Shell\open');
			Reg.DeleteKey('irc\Shell');
			Reg.DeleteKey('irc\DefaultIcon');
			Reg.DeleteKey('irc');
			Reg.CloseKey;
			Reg.Free;
		end;
			
		Reg := TRegistry.Create;
		Reg.RootKey := HKEY_LOCAL_MACHINE;
		Reg.DeleteKey('SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\DMDirc');
		Reg.CloseKey;
		Reg.Free;
		
		if (FileExists(profileDir+'\dmdirc.config')) then begin
			if MessageBox(0, PChar('A dmdirc profile has been detected ('+profileDir+') '+#13#10+'Do you want to delete it aswell?'), 'DMDirc Uninstaller', MB_YESNO) = 
IDYES then begin
				KillDir(profileDir);
			end;
		end;
		
		showmessage('DMDirc has been uninstalled from "'+InstallDir+'".', 'Uninstall Successful');
	end
	else if askQuestion('This will uninstall DMDirc. '+#13#10+#13#10+'Do you want to continue?') then begin
		if (ExecAndWait('java -jar "' + ExtractFileDir(paramstr(0)) + '\DMDirc.jar" -k', true) <> 0) then begin
			TempDir := GetTempDirectory;
			CopyFile(pchar(paramstr(0)), pchar(TempDir+'/uninstall.exe'), false);
			Launch('"'+TempDir+'/uninstall.exe" '+ExtractFileDir(paramstr(0))+'\');
		end else begin
			showError('Uninstall Aborted - DMDirc is still running.', 'Please close DMDirc before continuing')
		end;
	end;
end.
