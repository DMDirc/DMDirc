{*
 * winamp.dpr -> Code for winamp.dll for DMDirc
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
 *}

// References:
// http://krizzprograming.blogspot.com/2007/08/controlling-winamp-programmatically.html
// http://forums.winamp.com/showthread.php?threadid=85389

library winamp;
uses
  windows, messages, SysUtils, Classes;
  
function VirtualFreeEx(hProcess:HANDLE; lpAddress:LPVOID; dwSize:DWORD ; dwFreeType : DWORD):WINBOOL; external 'kernel32' name 'VirtualFreeEx';
function VirtualAllocEx(hProcess:HANDLE; lpAddress:LPVOID; dwSize:SIZE_T ; flAllocationType : DWORD;  flProtect : DWORD):POINTER; external 'kernel32' name 'VirtualAllocEx';

type
	extendedFileInfoStruct = packed record
		filename: pchar;
		metadata: pchar;
		ret: pchar;
		retlen: integer;
	end;


const
	WM_WINAMP = WM_USER;

function getPlayState(data: PChar):integer; stdcall;
var
	hand: THandle;
	res: LongInt;
	B: array[0..255] of char;
begin
	result := 1;
	B := 'Error finding window';
	hand := FindWindow('Winamp v1.x', nil);
	if hand <> 0 then begin
		B := 'Error getting data';
		result := 0;
		res := SendMessage(hand, WM_WINAMP, 0, 104);
		if res = 1 then B := 'Playing'
		else if res = 3 then B := 'Paused'
		else B := 'Stopped';
		StrCopy(data,B);
	end;
end;

function getArtistTitle(data: PChar):integer; stdcall;
var
	hand: THandle;
	B: array[0..255] of char;
	tempHand: THandle;
	memLoc: LongInt;
	temp: LongWord;
begin
	result := 1;
	B := 'Error finding window';
	hand := FindWindow('Winamp v1.x', nil);
	if hand <> 0 then begin
		B := 'Error getting data';
		result := 0;
		memLoc := SendMessage(hand, WM_WINAMP, 0, 125); // Get number of current track
		memLoc := SendMessage(hand, WM_WINAMP, memLoc, 212); // And now its name
		GetWindowThreadProcessId(hand, @tempHand);
		hand := OpenProcess(PROCESS_ALL_ACCESS, False, tempHand);
		ReadProcessMemory(hand, Pointer(memLoc), @B, sizeof(B)-1, temp);
		CloseHandle(hand);
		StrCopy(data,B);
	end;
end;

function getMetaData(metadata: PChar): PChar;
var
	hand: THandle;
	readhand: THandle;
	B: array[0..255] of char;
	C: array[0..255] of char;
	tempHand: THandle;
	memPtr: Pointer;
	memLoc: LongInt;
	temp: LongWord;
	extinfo: extendedFileInfoStruct;
begin
	B := '';
	C := '';
	Result := '';
	hand := FindWindow('Winamp v1.x', nil);
	if hand <> 0 then begin
		memLoc := SendMessage(hand, WM_WINAMP, 0, 125); // Get number of current track
		memLoc := SendMessage(hand, WM_WINAMP, memLoc, 211); // And now its filename
		GetWindowThreadProcessId(hand, @tempHand);
		
		readhand := OpenProcess(PROCESS_ALL_ACCESS, False, tempHand);
		ReadProcessMemory(readhand, Pointer(memLoc), @C, sizeof(C)-1, temp);
		
		extinfo.filename := C;
		extinfo.metadata := metadata;
		extinfo.ret := B;
		extinfo.retlen := sizeof(B)-1;

		// This doesn't actually work yet :/
		// memPtr is nil
		memPtr := VirtualAllocEx(tempHand, nil, 2048, MEM_COMMIT or MEM_RESERVE, PAGE_READWRITE);
		if memPtr <> nil then begin
			WriteProcessMemory(readhand, memPtr, (@extinfo), sizeof(extendedFileInfoStruct), temp);
			SendMessage(readhand, WM_WINAMP, LongInt(memPtr), 290);
		end;
		
		StrCopy(Result, B);
		
		if memPtr <> nil then begin
			VirtualFreeEx(readhand, memPtr, 0, MEM_RELEASE);
		end;
		CloseHandle(readhand);
	end;
end;

function getArtist(data: PChar):integer; stdcall;
var
	B: PChar;
begin
	B := getMetaData('artist');
	StrCopy(data,B);
	if (B <> '') then Result := 0 else Result := 1;
end;

function getTitle(data: PChar):integer; stdcall;
var
	B: PChar;
begin
	B := getMetaData('title');
	StrCopy(data,B);
	if (B <> '') then Result := 0 else Result := 1;
end;

function getAlbum(data: PChar):integer; stdcall;
var
	B: PChar;
begin
	B := getMetaData('album');
	StrCopy(data,B);
	if (B <> '') then Result := 0 else Result := 1;
end;

function getLength(data: PChar):integer; stdcall;
var
	hand: THandle;
	res: LongInt;
begin
	result := 1;
	StrCopy(data, 'Error finding window');
	hand := FindWindow('Winamp v1.x', nil);
	if hand <> 0 then begin
		result := 0;
		res := SendMessage(hand, WM_WINAMP, 1, 105);
		StrCopy(data, PChar(inttostr(res)));
	end;
end;

function getTime(data: PChar):integer; stdcall;
var
	hand: THandle;
	res: LongInt;
begin
	result := 1;
	StrCopy(data, 'Error finding window');
	hand := FindWindow('Winamp v1.x', nil);
	if hand <> 0 then begin
		result := 0;
		res := SendMessage(hand, WM_WINAMP, 0, 105);
		StrCopy(data, PChar(inttostr(res div 1000)));
	end;
end;

function getFormat(data: PChar):integer; stdcall;
var
	B: array[0..255] of char;
begin
	Result := 0;
	B := 'Unknown';
	StrCopy(data,B);
end;

function getBitrate(data: PChar):integer; stdcall;
var
	hand: THandle;
	res: LongInt;
begin
	result := 1;
	StrCopy(data, 'Error finding window');
	hand := FindWindow('Winamp v1.x', nil);
	if hand <> 0 then begin
		result := 0;
		res := SendMessage(hand, WM_WINAMP, 1, 126);
		StrCopy(data, PChar(inttostr(res)));
	end;
end;

exports getPlayState, getArtistTitle, getArtist, getTitle, getAlbum, getLength, getTime, getFormat, getBitrate;

begin
end.
