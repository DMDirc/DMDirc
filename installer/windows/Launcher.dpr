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
program Launcher;
{$MODE Delphi}
{$APPTYPE GUI}

uses Windows, SysUtils, classes, MD5;
procedure InitCommonControls; stdcall; External 'comctl32.dll' name 'InitCommonControls';

//{$R files.res}
//{$R version.res}
//{$R icon.res}
{$R all.res}

{$I consts.inc}

function GetTempDirectory(): String;
var
	buf: array[0..MAX_PATH] of Char;
	wintemp, temp: String;
begin
	GetTempPath(SizeOf(buf)-1, buf);
	wintemp := StrPas(buf);
	Randomize;
	temp := '\DMDirc-installer-'+inttostr(1000 + Random(1000));
	while (DirectoryExists(wintemp+temp+'\')) do begin
		temp := temp+'-'+inttostr(1+Random(1000));
	end;
	MkDir(wintemp+temp+'\');
	result := wintemp+temp+'\';
end;

procedure ExtractResource(name: string; filename: string; dir: string = '');
var
	rStream: TResourceStream;
	fStream: TFileStream;
	fname: string;
begin
	if (dir = '') or (not DirectoryExists(dir)) then dir := IncludeTrailingPathDelimiter(ExtractFileDir(paramstr(0)));
	fname := dir+filename;
	if FileExists(fname) then DeleteFile(fname);
	
	rStream := TResourceStream.Create(hInstance, name, RT_RCDATA);
	try
		fStream := TFileStream.Create(fname, fmCreate);
		try
			fStream.CopyFrom(rStream, 0);
		finally
			fStream.Free;
		end;
	finally
		rStream.Free;
	end;
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

function checkMD5(filename: String): boolean;
var
	hash: String;
begin
	hash := MD5Print(MD5File(filename));
	result := (hash = MD5SUM);
//	if not result then begin
//		MessageBox(0, PChar('MD5Hash Result:'+#10+'Got: '+hash+#10+'Exp: '+MD5SUM+#10+'Res:'+booltostr(hash = MD5SUM)), 'Test', MB_OK + MB_ICONSTOP);
//	end;

// Uncomment this to disable MD5 Check
//	result := true;
end;

var
	ErrorMessage: String;
	TempDir: String;
begin
        InitCommonControls;
	TempDir := GetTempDirectory;
	ErrorMessage := '';
	{$I ExtractCode.inc}
end.
