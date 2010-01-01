{*
 * DMDirc - Open Source IRC Client
 * Copyright (c) 2006-2010 Chris Smith, Shane Mc Cormack, Gregory Holmes
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
 *
 *
 * This application attempts to load a dll for media source information.
 * It can be run from the commandline like so:
 *  - GetMediaInfo.exe winamp getArtist
 * and it will load the winamp dll and attempt to execute the function "artist"
 * Any output will be echoed to the command line with an exit code of 0.
 * An exit code of 1 means there was an error from the DLL
 * An exit code of 2 means there was an error before the DLL
 *
 * Methods are very simple:
 *   Method: function (data: PChar): integer; stdcall;
 * A pchar of size 1024 is passed, resulting data should be written to this
 * pchar inside the dll.
 * The method should then return either 0 to indicate success, or 1 to indicate
 * an error. (This will be used as the exit code, and the contents of Data is 
 * printed to stdout - any returned value > 1 will still give an exitcode of 1)
 *}
program GetMediaInfo;
{$MODE Delphi}

uses Windows, sysutils;
var
	I: Integer;
	Player: String;
	MethodName: String;
	DLL: HINST;
	
	Method: function (data: PChar): integer; stdcall;
	Data: PChar;
begin
	// By default, assume fail.
	ExitCode := 2;

	if ParamCount > 1 then begin
		// Params
		Player := paramstr(1);
		MethodName := paramstr(2);
		
		// Load the DLL
		DLL := LoadLibrary(PChar(Player+'.dll'));
		
		if DLL <> HINST(0) then begin
			// DLL Exists and is loaded
			// Now the actual method we want!
			Method := GetProcAddress(DLL, PChar(MethodName));
			if @Method <> nil then begin
				try
					GetMem(Data, 1024);
					I := Method(Data);
					if I > 1 then I := 1;
					writeln(Data);
					ExitCode := I;
				finally
					FreeMem(Data);
				end;
			end
			else begin
				writeln('No Method ('+MethodName+') Found');
			end;
		end
		else begin
			writeln('No DLL ('+Player+'.dll) Found');
		end;
	end
	else begin
		writeln('Usage: GetMediaInfo.exe player function');
		writeln('Example: GetMediaInfo.exe winamp getArtist');
	end;
end.
