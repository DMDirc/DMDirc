{*
 * DMDirc Uninstaller
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

uses shared, Windows, SysUtils, registry, Vista;
procedure InitCommonControls; stdcall; External 'comctl32.dll' name 'InitCommonControls';

{$R uninstall.res}
{ ---------------------------------------------------------------------------- }

{ ----------------------------------------------------------------------------
  Create a temp directory and return the path to it
  ---------------------------------------------------------------------------- }
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

{ ----------------------------------------------------------------------------
  Delete a directory and all files it contains
  ---------------------------------------------------------------------------- }
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

{ ----------------------------------------------------------------------------
  MAIN PROGRAM
  ---------------------------------------------------------------------------- }
var
	TempDir: String;
	InstallDir: String = '';
	i: Integer;
	Reg: TRegistry;
	handlerInfo: String;
	profileDir: String;
	deleteProtocol: boolean;
begin
        InitCommonControls;
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
			KillDir(GetEnvironmentVariable('USERPROFILE')+'\Start Menu\Programs\DMDirc');
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
			if MessageBox(0, PChar('A dmdirc profile has been detected ('+profileDir+') '+#13#10+'Do you want to delete it as well?'), 'DMDirc Uninstaller', MB_YESNO) = IDYES then begin
				KillDir(profileDir);
			end;
		end;
		
		showmessage('DMDirc has been uninstalled from "'+InstallDir+'".', 'DMDirc Uninstaller', 'Uninstall Successful');
	end
	else if askQuestion('This will uninstall DMDirc. '+#13#10+#13#10+'Do you want to continue?', 'DMDirc Uninstaller') then begin
		if (ExecAndWait('java -jar "' + ExtractFileDir(paramstr(0)) + '\DMDirc.jar" -k', true) <> 0) then begin
			TempDir := GetTempDirectory;
			CopyFile(pchar(paramstr(0)), pchar(TempDir+'/uninstall.exe'), false);
			Launch('"'+TempDir+'/uninstall.exe" '+ExtractFileDir(paramstr(0))+'\');
		end else begin
			showError('Uninstall Aborted - DMDirc is still running.' +
                #13#10 + 'Please close DMDirc before continuing',
                'DMDirc Uninstaller', False, False);
		end;
	end;
end.
