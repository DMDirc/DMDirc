{*
 * This application attempts to load mirc-compatible dlls for media source information
 * This will fail with anything that actually relies on knowing the mirc window
 * handles
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
program GetMediaInfo;
{$MODE Delphi}

uses Windows, sysutils;

type
	TLoadInfo = packed record
		mVersion: DWORD;
		mHwnd: HWND;
		mKeep: Boolean;
	end;
	PLoadInfo = ^TLoadInfo;

var
	I: Integer;
	Player: String;
	MethodName: String;
	DLL: HINST;
	
	Method: function (mWnd: HWND; aWnd: HWND; data: PChar; parms: PChar; show: boolean; nopause: boolean): integer; stdcall;
	LoadDLLMethod: procedure (LoadInfo: PLoadInfo); stdcall;
	UnloadDLLMethod: function (mTimeOut: integer): integer; stdcall;
	LoadInfo: PLoadInfo;
	Data: PChar;
	Params: PChar;
begin
	// By default, assume fail.
	ExitCode := 1;

	if ParamCount > 1 then MethodName := paramstr(2) else MethodName := 'GetTrackName';
	
	if ParamCount > 0 then begin
		Player := paramstr(1);
		DLL := LoadLibrary(PChar(Player+'.dll'));
		
		if DLL <> HINST(0) then begin
			// DLL Exists and is loaded, we need to try to call LoadDLL first
			LoadInfo := new(PLoadInfo);
			LoadInfo.mVersion := MakeLong(6, 30); // Pretend to be mirc 6.3
			LoadInfo.mHwnd := HWND(nil);
			LoadInfo.mKeep := false;
			LoadDLLMethod := GetProcAddress(DLL, 'LoadDll');
			if @LoadDLLMethod <> nil then LoadDLLMethod(LoadInfo);
			
			// Now the actual method we want!
			Method := GetProcAddress(DLL, PChar(MethodName));
			if @Method <> nil then begin
				// mirc help says the maximum size for these are 900
				// I use 1024 mostly cos its a nicer number, but also if for some reason
				// more than the amount allocated here gets used by the DLL, we run into
				// all sorts of horrible nasty memory issues!
				// We don't actually use Params, but some DLLs might write to it, so
				// we have to allocate it aswell to prevent previously mentioned memory
				// issues!
				try
					GetMem(Data, 1024);
					GetMem(Params, 1024);
					I := Method(HWND(nil), HWND(nil), Data, Params, true, true);
					if I = 3 then begin
						writeln(Data);
						ExitCode := 0;
					end
					else begin
						writeln('Method Exists, but incompatible');
						writeln('I: '+inttostr(I));
						writeln('Data: '+Data);
						writeln('Params: '+Params);
					end;
				finally
					FreeMem(Data);
					FreeMem(Params);
				end;
			end
			else begin
				writeln('No Method ('+MethodName+') Found');
			end;
			
			// Now attempt to unload
			UnloadDLLMethod := GetProcAddress(DLL, 'UnloadDll');
			if @UnloadDLLMethod <> nil then UnloadDLLMethod(0);
		end
		else begin
			writeln('No DLL ('+Player+'.dll) Found');
		end;
	end
	else begin
		writeln('No Player Given');
	end;
end.
