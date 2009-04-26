{$ifdef read_interface}
type
  PIniFile = ^TIniFile;

  { TIniFile }

  TIniFile = object( TObj )
  private
    procedure SetMode(const AValue: TIniFileMode);
  protected
    fMode: TIniFileMode;
    fFileName: KOLString;
    fSection: KOLString;
    fData: PStrList;
  protected
    function FindSection(var First, Last: Integer): Boolean;
    function FindKey(Key: String; First, Last: Integer; var Value: String): Integer;
  public
    destructor Destroy; virtual;
    property Mode: TIniFileMode read fMode write SetMode;
    property FileName: KOLString read fFileName;
    property Section: KOLString read fSection write fSection;
    function ValueInteger( const Key: KOLString; Value: Integer ): Integer;
    function ValueString( const Key: KOLString; const Value: KOLString ): KOLString;
    function ValueBoolean( const Key: KOLString; Value: Boolean ): Boolean;
    function ValueData( const Key: KOLString; Value: Pointer; Count: Integer ): Boolean;
    procedure ClearAll;
    procedure ClearSection;
    procedure ClearKey( const Key: KOLString );
    procedure GetSectionNames(Names: {$IFNDEF UNICODE_CTRLS}PStrList{$ELSE}PWStrList{$ENDIF});
    procedure SectionData(Names: {$IFNDEF UNICODE_CTRLS}PStrList{$ELSE}PWStrList{$ENDIF});
    procedure UpdateFile;
  end;
{$endif read_interface}

{$ifdef read_implementation}
function OpenIniFile( const FileName: KOLString ): PIniFile;
begin
  New(Result, Create);
  Result.fFileName := FileName;
  Result.fData:= NewStrList;
  Result.fMode:= ifmRead;
  Result.fData.LoadFromFile(FileName);
end;

function GetSection(var S: String): Boolean;
var L: Integer;
begin
  S:= Trim(S);
  L:= Length(S);
  Result:= (L > 2) and (S[1] = '[') and (S[L] = ']');
  if Result then begin
    Delete(S, L, 1); Delete(S, 1, 1);
    S:= Trim(S);
//      Result:= (S <> '');
  end;
end;

procedure TIniFile.ClearAll;
begin
  fData.Clear;
  fData.SaveToFile(fFileName);
end;

procedure TIniFile.ClearKey(const Key: KOLString);
var k, F, L: Integer;
    S: String;
begin
  if FindSection(F, L) then begin
    k:= FindKey(Key, F, L, S);
    if k >= 0 then fData.Delete(k);
    fData.SaveToFile(fFileName);
  end;
end;

procedure TIniFile.ClearSection;
var i, F, L: Integer;
begin
  if FindSection(F, L) then begin
    for i := L downto F do begin
      fData.Delete(i);
    end;
    fData.SaveToFile(fFileName);
  end;
end;

destructor TIniFile.Destroy;
begin
  if fMode = ifmWrite then fData.SaveToFile(fFileName);
  fData.Free;
  fFileName := '';
  fSection := '';
  inherited;
end;

procedure TIniFile.GetSectionNames(Names: {$IFNDEF UNICODE_CTRLS}PStrList{$ELSE}PWStrList{$ENDIF});
var i: Integer;
    S: String;
begin
  for i:= 0 to fData.Count-1 do begin
    S:= fData.Items[i];
    if GetSection(S) then Names.Add(S)
  end;
end;

procedure TIniFile.SectionData(Names: {$IFNDEF UNICODE_CTRLS}PStrList{$ELSE}PWStrList{$ENDIF});
var i, F, L: Integer;
    S: String;
begin
  if fMode = ifmRead then begin
    if FindSection(F, L) then
      for i := F+1 to L do begin
        S:= Trim(fData.Items[i]);
        if S <> '' then Names.Add(S);
      end;
  end
  else begin
    ClearSection;
    fData.Add('[' + fSection + ']');
    for i:= 0 to Names.Count-1 do begin
      S:= Trim(Names.Items[i]);
      if S <> '' then fData.Add(S);
    end;
  end;
end;

procedure TIniFile.UpdateFile;
begin
    if fMode = ifmRead then fData.LoadFromFile(fFileName)
    else fData.SaveToFile(fFileName);
end;

function TIniFile.ValueBoolean(const Key: KOLString; Value: Boolean): Boolean;
begin
  Result:= ValueInteger(Key, Ord(Value)) <> 0;
end;

function TIniFile.ValueData(const Key: KOLString; Value: Pointer; Count: Integer): Boolean;
var i: Integer;
    S: String;
begin
  if fMode = ifmRead then begin
    S:= ValueString(Key, '');
    for i:= 0 to Min(Length(S) div 2, Count) - 1 do begin
      PByte(Value)^:= Hex2Int(Copy(S, 1+i*2, 2));
      cardinal(Value):= cardinal(Value) + 1;
      Result:= True;
    end;
  end
  else begin
    S:= '';
    for i:= 0 to Count - 1 do begin
      S:= S + Int2Hex(PByte(Value)^, 2);
      cardinal(Value):= cardinal(Value) + 1;
      Result:= True;
    end;
    ValueString(Key, S);
  end;
end;

function TIniFile.ValueInteger(const Key: KOLString; Value: Integer): Integer;
begin
  Result:= Str2Int(ValueString(Key, Int2Str(Value)));
end;

function TIniFile.ValueString( const Key: KOLString; const Value: KOLString ): KOLString;
var k, F, L: Integer;
    S: String;
    fSect: Boolean;
begin
  fSect:= FindSection(F, L);
  if fSect then k:= FindKey(Key, F, L, S) else k:= -1;
  if fMode = ifmRead then begin
    if fSect and (k > 0) then Result:= S else Result := Value;
  end
  else begin
    if not fSect then begin
      fData.Add('[' + fSection + ']');
      fData.Add(''); k:= fData.Count-1;
    end;
    if k < 0 then begin k:= L+1; fData.Insert(k, ''); end;
    fData.Items[k]:= Key+'='+Value;
  end;
end;

procedure TIniFile.SetMode(const AValue: TIniFileMode);
begin
    if fMode = AValue then Exit;
    if fMode = ifmWrite then fData.SaveToFile(fFileName)
    else fData.LoadFromFile(fFileName);
    fMode:= AValue;
end;

function TIniFile.FindSection(var First, Last: Integer): Boolean;
var i: Integer;
    Sec, S: String;
begin
  Result:= False;
  Sec := Trim(fSection);
  for i:= 0 to fData.Count-1 do begin
    S:= fData.Items[i];
    if GetSection(S) and (AnsiCompareStrNoCase(S, Sec) = 0) then begin
      First:= i; Result:= True; Break;
    end;
  end;
  if Result then begin
    for i:= First+1 to fData.Count-1 do begin
      S:= fData.Items[i];
      if GetSection(S) then begin
        Last:= i-1; Exit;
      end;
    end;
    Last:= fData.Count-1;
  end;
end;

function TIniFile.FindKey(Key: String; First, Last: Integer; var Value: String): Integer;
var i, k: Integer;
    S: String;
begin
  Result:= -1;
  Key:= Trim(Key);
  for i:= First to Last do begin
    S:= fData.Items[i];
    k:= Pos('=', S);
    if k > 0 then begin
      if AnsiCompareStrNoCase(Key, Trim(Copy(S, 1, k-1))) = 0 then Result:= i;
      if Result >= 0 then begin
        Delete(S, 1, k);
        Value:= Trim(S);
        k:= Length(Value);
        if (k > 0) and (Value[1] = '"') and (Value[k] = '"') then begin
          Delete(Value, k, 1); Delete(Value, 1, 1);
        end;
        Exit;
      end;
    end;
  end;
end;
{$endif read_implementation}
