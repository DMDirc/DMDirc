{*
 * Vista image improvements from http://www.installationexcellence.com/articles/VistaWithDelphi/Original/Index.html
 * and http://www.installationexcellence.com/articles/VistaWithDelphi/Index.html
 *}
unit Vista;

//{$IFDEF LAZARUS}
//	{$DEFINE MESSAGEDLG}
// {$ENDIF}

interface

uses {$IFDEF LAZARUS}Forms, Graphics, Controls,{$IFDEF MESSAGEDLG} Dialogs,{$ENDIF}{$ENDIF} Windows, SysUtils;

function IsWindowsVista: Boolean;
function TaskDialog(const AHandle: THandle; const ATitle, ADescription, AContent: WideString; const Icon, Buttons: integer; includeDescInXP: boolean = false; stripLineFeed: boolean = true): Integer;
{$IFDEF LAZARUS}
procedure SetVistaFonts(const AForm: TCustomForm);
{$ENDIF}

const
	VistaFont = 'Segoe UI'; 
	VistaContentFont = 'Calibri';
	XPContentFont = 'Verdana';
	XPFont = 'Tahoma';

	TD_ICON_BLANK = 0;
	TD_ICON_WARNING = 84;
	TD_ICON_QUESTION = 99;
	TD_ICON_ERROR = 98;
	TD_ICON_INFORMATION = 81;
	TD_ICON_SHIELD_QUESTION = 104;
	TD_ICON_SHIELD_ERROR = 105;
	TD_ICON_SHIELD_OK = 106;
	TD_ICON_SHIELD_WARNING = 107;

	TD_BUTTON_OK = 1;
	TD_BUTTON_YES = 2;
	TD_BUTTON_NO = 4;
	TD_BUTTON_CANCEL = 8;
	TD_BUTTON_RETRY = 16;
	TD_BUTTON_CLOSE = 32;

	TD_RESULT_OK = 1;
	TD_RESULT_CANCEL = 2;
	TD_RESULT_RETRY = 4;
	TD_RESULT_YES = 6;
	TD_RESULT_NO = 7;
	TD_RESULT_CLOSE = 8;
	
	{$IFNDEF LAZARUS}
		mrNone = 0;
		mrOK = mrNone + 1;
		mrCancel = mrNone + 2;
		mrAbort = mrNone + 3;
		mrRetry = mrNone + 4;
		mrYes = mrNone + 6;
		mrNo = mrNone + 7;
	{$ENDIF}

implementation

{$IFDEF LAZARUS}
procedure SetVistaFonts(const AForm: TCustomForm);
begin
	if IsWindowsVista and not SameText(AForm.Font.Name, VistaFont) and (Screen.Fonts.IndexOf(VistaFont) >= 0) then
	begin
		AForm.Font.Size := AForm.Font.Size + 1;
		AForm.Font.Name := VistaFont;
	end;
end;
{$ENDIF}

function IsWindowsVista: Boolean;
var
	VerInfo: TOSVersioninfo;
begin
	VerInfo.dwOSVersionInfoSize := SizeOf(TOSVersionInfo);
	GetVersionEx(VerInfo);
	Result := VerInfo.dwMajorVersion >= 6;
end;

// http://www.swissdelphicenter.ch/en/showcode.php?id=1692
{:Converts Unicode string to Ansi string using specified code page.
  @param   ws       Unicode string.
  @param   codePage Code page to be used in conversion.
  @returns Converted ansi string.
}
function WideStringToString(const ws: WideString; codePage: Word): AnsiString;
var
  l: integer;
begin
	if ws = '' then begin
		Result := ''
	end
	else begin
		l := WideCharToMultiByte(codePage, WC_COMPOSITECHECK or WC_DISCARDNS or WC_SEPCHARS or WC_DEFAULTCHAR, @ws[1], - 1, nil, 0, nil, nil);
		SetLength(Result, l - 1);
		if l > 1 then begin
			WideCharToMultiByte(codePage, WC_COMPOSITECHECK or WC_DISCARDNS or WC_SEPCHARS or WC_DEFAULTCHAR, @ws[1], - 1, @Result[1], l - 1, nil, nil);
		end;
	end;
end;


//from http://www.tmssoftware.com/atbdev5.htm
function TaskDialog(const AHandle: THandle; const ATitle, ADescription, AContent: WideString; const Icon, Buttons: integer; includeDescInXP: boolean = false; stripLineFeed: boolean = true): Integer;
type
        tTaskDialogProc = function(HWND: THandle; hInstance: THandle; cTitle, cDescription, cContent: pwidechar; Buttons: Integer; Icon: integer; ResButton: pinteger): integer; stdcall;
var
	DLLHandle: THandle;
	res: integer;
	wS: WideString;
	S: String;
	{$IFDEF MESSAGEDLG}
	Btns: TMsgDlgButtons;
	DlgType: TMsgDlgType;
	{$ELSE}
	Btns: Integer;
	myIcon: Integer;
	{$ENDIF}
	TaskDialogFound: boolean;
	TaskDialogProc: tTaskDialogProc;
begin
	TaskDialogFound := false;
	Result := 0;
	if IsWindowsVista then begin
		DLLHandle := LoadLibrary('comctl32.dll');
		if DLLHandle >= 32 then begin
			TaskDialogProc := tTaskDialogProc(GetProcAddress(DLLHandle,'TaskDialog'));
			
			if Assigned(TaskDialogProc) then begin
				
				if stripLineFeed then begin
					wS := StringReplace(AContent, #10, '', [rfReplaceAll]);
					wS := StringReplace(wS, #13, '', [rfReplaceAll]);
				end
				else begin
					wS := AContent;
				end;

				TaskDialogProc(AHandle, 0, PWideChar(ATitle), PWideChar(ADescription), PWideChar(wS), Buttons, Icon, @res);
				TaskDialogFound := true;
				Result := mrOK;
				case res of
					TD_RESULT_CANCEL : Result := mrCancel;
					TD_RESULT_RETRY : Result := mrRetry;
					TD_RESULT_YES : Result := mrYes;
					TD_RESULT_NO : Result := mrNo;
					TD_RESULT_CLOSE : Result := mrAbort;
				end;
			end;
			FreeLibrary(DLLHandle);
		end;
	end;
	
	if not TaskDialogFound then begin
		S := '';
		if includeDescInXP then S := ADescription + #10#13 + #10#13 + AContent else S := AContent;
		
		{$IFDEF MESSAGEDLG}
			Btns := [];
			if Buttons and TD_BUTTON_OK = TD_BUTTON_OK then Btns := Btns + [MBOK];
			if Buttons and TD_BUTTON_YES = TD_BUTTON_YES then Btns := Btns + [MBYES];
			if Buttons and TD_BUTTON_NO = TD_BUTTON_NO then Btns := Btns + [MBNO];
			if Buttons and TD_BUTTON_CANCEL = TD_BUTTON_CANCEL then Btns := Btns + [MBCANCEL];
			if Buttons and TD_BUTTON_RETRY = TD_BUTTON_RETRY then Btns := Btns + [MBRETRY];
	
			if Buttons and TD_BUTTON_CLOSE = TD_BUTTON_CLOSE then Btns := Btns + [MBABORT];
	
			DlgType := mtCustom;
	
			case Icon of
				TD_ICON_WARNING : DlgType := mtWarning;
				TD_ICON_QUESTION : DlgType := mtConfirmation;
				TD_ICON_ERROR : DlgType := mtError;
				TD_ICON_INFORMATION: DlgType := mtInformation;
			end;
	
			Result := MessageDlg(S, DlgType, Btns, 0);
		{$ELSE}
			Btns := 0;
			if Buttons and TD_BUTTON_OK = TD_BUTTON_OK then Btns := MB_OK;
			if (Buttons and TD_BUTTON_YES = TD_BUTTON_YES) and (Buttons and TD_BUTTON_NO = TD_BUTTON_NO) then Btns := MB_YESNO;
			if (Buttons and TD_BUTTON_CANCEL = TD_BUTTON_CANCEL) and (Buttons and TD_BUTTON_YES = TD_BUTTON_YES) and (Buttons and TD_BUTTON_NO = TD_BUTTON_NO) then Btns := MB_YESNOCANCEL;
			if (Buttons and TD_BUTTON_CANCEL = TD_BUTTON_CANCEL) and (Buttons and TD_BUTTON_OK = TD_BUTTON_OK) then Btns := MB_OKCANCEL;
			if (Buttons and TD_BUTTON_CANCEL = TD_BUTTON_CANCEL) and (Buttons and TD_BUTTON_RETRY = TD_BUTTON_RETRY) then Btns := MB_RETRYCANCEL;
			
			myIcon := 0;
			case Icon of
				TD_ICON_QUESTION : myIcon := MB_ICONQUESTION;
				TD_ICON_ERROR : myIcon := MB_ICONSTOP;
				TD_ICON_INFORMATION: myIcon := MB_ICONINFORMATION;
			end;

                        Result := MessageBox(0, pchar(S), pchar(String(ATitle)), Btns + myIcon);
		{$ENDIF}
	end;
end;

end.

