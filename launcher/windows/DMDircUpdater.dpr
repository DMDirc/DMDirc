program DMDircUpdater;
{$MODE Delphi}
{$APPTYPE GUI}

{$R UAC.rc}

uses Windows, SysUtils, classes;

var
	updateFile: String = '';
	i: integer;
begin
	if ParamCount > 0 then begin
		for i := 1 to ParamCount do begin
			updateFile := updateFile+paramstr(i);
		end;

		if FileExists('DMDirc.jar') then begin
			if not DeleteFile('DMDirc.jar') then begin
				MessageBox(0, 'Unable to delete DMDirc.jar', 'Update Failed', MB_ICONSTOP);
			end;
		end;
		if MoveFile(pchar(updateFile), 'DMDirc.jar') then begin
			MessageBox(0, 'Client update was successful.', 'Update Completed', MB_OK);
		end
		else begin
			MessageBox(0, pchar('Unable to move '''+updateFile+''' to DMDirc.jar'), 'Update Failed', MB_ICONSTOP);
		end;
	end;
end.