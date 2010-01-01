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
// http://www.informit.com/articles/article.aspx?p=130494&seqNum=3
// http://developer.apple.com/sdk/itunescomsdk.html

library winamp;
uses
	windows, messages, SysUtils, Classes, ComObj, ActiveX;

var
	oldExitProc: Pointer;
	
function getITunes(): Variant;
begin
	if FindWindow('iTunes', nil) <> 0 then begin
		Result := CreateOLEObject('iTunes.Application');
	end
	else Raise Exception.Create('iTunes is not running');
end;

// Freepascal sucks.
// In Delphi I could just do V.Foo to call function Foo, but FPC hasn't added
// this functionality yet, so yay for horrible callOLEFunction(V, 'Foo'); to
// do the exact same thing!
function callOLEFunction(V: IDispatch; S: WideString): Variant;
const
	GUID_NULL: TGUID = '{00000000-0000-0000-0000-000000000000}';
	DEFAULT_PARAMS: TDispParams = ();
var
	mres: HRESULT;
	invres: Variant;
	id: Integer;
begin
	mres := V.GetIDsOfNames(GUID_NULL, @S, 1, locale_system_default, @id);
	olecheck(mres);
	mres := V.Invoke(id, GUID_NULL, locale_system_default, DISPATCH_PROPERTYGET, DEFAULT_PARAMS, @invres, nil, nil);
	olecheck(mres);
	Result := invres;
end;

function getPlayState(data: PChar):integer; stdcall;
var
	V: Variant;
	B: array[0..255] of char;
	state: Integer;
begin
  result := 1;
	try
		V := getITunes();
		state := callOLEFunction(V, 'PlayerState');
		if state = 0 then begin
			try
				callOLEFunction(V, 'PlayerPosition');
				B := 'Paused';
			except
				B := 'Stopped';
			end
		end
		else if state = 1 then B := 'Playing';
		result := 0;
	except
		on E : Exception do B:= 'Unable to find iTunes Application ('+E.ClassName+'/'+E.Message+')';
	end;
	StrCopy(data,B);
end;

function getArtist(data: PChar):integer; stdcall;
var
	V: Variant;
	B: array[0..255] of char;
begin
  result := 1;
	try
		V := getITunes();
		V := callOLEFunction(V, 'CurrentTrack');
		B := String(callOLEFunction(V, 'Artist'));
		result := 0;
	except
		on E : Exception do B:= 'Unable to find iTunes Application ('+E.ClassName+'/'+E.Message+')';
	end;
	StrCopy(data,B);
end;

function getTitle(data: PChar):integer; stdcall;
var
	V: Variant;
	B: array[0..255] of char;
	kind: Integer;
begin
  result := 1;
	try
		V := getITunes();
		V := callOLEFunction(V, 'CurrentTrack');
		kind := callOLEFunction(V, 'Kind');
		if kind = 3 then begin
			V := getITunes();
			B := String(callOLEFunction(V, 'CurrentStreamTitle'));
		end
		else begin
			B := String(callOLEFunction(V, 'Name'));
		end;
		result := 0;
	except
		on E : Exception do B:= 'Unable to find iTunes Application ('+E.ClassName+'/'+E.Message+')';
	end;
	StrCopy(data,B);
end;

function getAlbum(data: PChar):integer; stdcall;
var
	V: Variant;
	B: array[0..255] of char;
begin
  result := 1;
	try
		V := getITunes();
		V := callOLEFunction(V, 'CurrentTrack');
		B := String(callOLEFunction(V, 'Album'));
		result := 0;
	except
		on E : Exception do B:= 'Unable to find iTunes Application ('+E.ClassName+'/'+E.Message+')';
	end;
	StrCopy(data,B);
end;

function getLength(data: PChar):integer; stdcall;
var
	V: Variant;
	B: array[0..255] of char;
begin
  result := 1;
	try
		V := getITunes();
		V := callOLEFunction(V, 'CurrentTrack');
		B := String(callOLEFunction(V, 'Duration'));
		result := 0;
	except
		on E : Exception do B:= 'Unable to find iTunes Application ('+E.ClassName+'/'+E.Message+')';
	end;
	StrCopy(data,B);
end;

function getTime(data: PChar):integer; stdcall;
var
	V: Variant;
	B: array[0..255] of char;
begin
  result := 1;
	try
		V := getITunes();
		B := String(callOLEFunction(V, 'PlayerPosition'));
		result := 0;
	except
		on E : Exception do B:= 'Unable to find iTunes Application ('+E.ClassName+'/'+E.Message+')';
	end;
	StrCopy(data,B);
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
	V: Variant;
	B: array[0..255] of char;
begin
  result := 1;
	try
		V := getITunes();
		V := callOLEFunction(V, 'CurrentTrack');
		B := String(callOLEFunction(V, 'BitRate'));
		result := 0;
	except
		on E : Exception do B:= 'Unable to find iTunes Application ('+E.ClassName+'/'+E.Message+')';
	end;
	StrCopy(data,B);
end;

exports getPlayState, getArtist, getTitle, getAlbum, getLength, getTime, getFormat, getBitrate;

procedure myExitProc;
begin
	CoUnInitialize();
	ExitProc := oldExitProc;
end; 

begin
	CoInitialize(nil);
	oldExitProc := ExitProc;
	ExitProc := @myExitProc;
end.