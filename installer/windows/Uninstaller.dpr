program Uninstaller;
{$MODE Delphi}
{$APPTYPE GUI}

uses Windows, SysUtils, classes, registry;

{$R uninstall.res}

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

function KillDir(Dir: string): Integer;
var
	searchResult: TSearchRec;
begin
	Result := 0;
	if FindFirst(Dir+'\*', faDirectory + faHidden  + faReadOnly + faSysfile + faAnyFile, searchResult) = 0 then
	begin
		repeat
			if (searchResult.attr and faDirectory) <> faDirectory then begin
				DeleteFile(Dir+'\'+searchResult.name);
			end
			else begin
				if (searchResult.name <> '.') and (searchResult.name <> '..') then begin
					KillDir(Dir+'\'+searchResult.name);
				end;
			end;
		until FindNext(searchResult) <> 0;
		FindClose(searchResult);
	end;
	RmDir(Dir);
end;

var
	TempDir: String;
	InstallDir: String = '';
	hK32: THandle;
	i: Integer;
	Reg: TRegistry;
	handlerInfo: String;
	deleteProtocol: boolean;
begin
	if (ParamCount > 0) then begin
		for i := 1 to ParamCount do begin
			InstallDir := InstallDir+' '+paramstr(i);
		end;
		InstallDir := trim(InstallDir);
		KillDir(InstallDir);
		hK32 := GetModuleHandle('kernel32');
		if GetProcAddress(hK32, 'GetLocaleInfoEx') <> nil then begin
			// Vista
			KillDir(GetEnvironmentVariable('APPDATA')+'\Microsoft\Windows\Start Menu\Programs\DMDirc');
			DeleteFile(GetEnvironmentVariable('USERPROFILE')+'\AppData\Roaming\Microsoft\Internet Explorer\Quick Launch\DMDirc.lnk');
			DeleteFile(GetEnvironmentVariable('USERPROFILE')+'\Desktop\DMDirc.lnk');
		end
		else begin
			// Not Vista
			KillDir(GetEnvironmentVariable('USERPROFILE')+'\Microsoft\Windows\Start Menu\Programs\DMDirc');
			DeleteFile(GetEnvironmentVariable('USERPROFILE')+'\Application DataMicrosoft\Internet Explorer\Quick Launch\DMDirc.lnk');
			DeleteFile(GetEnvironmentVariable('USERPROFILE')+'\Desktop\DMDirc.lnk');
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
		
		MessageBox(0, PChar('DMDirc has been uninstalled from "'+InstallDir+'".'), 'DMDirc Uninstaller', MB_OK);
	end
	else begin
		TempDir := GetTempDirectory;
		if MessageBox(0, PChar('This will uninstall DMDirc.'+#13#10+'Do you want to continue?'), 'DMDirc Uninstaller', MB_YESNO) = IDYES then begin
			CopyFile(pchar(paramstr(0)), pchar(TempDir+'/uninstall.exe'), false);
			Launch(TempDir+'/uninstall.exe '+ExtractFileDir(paramstr(0))+'\');
		end;
	end;
end.