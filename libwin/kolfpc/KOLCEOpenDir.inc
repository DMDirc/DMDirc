{$ifdef read_interface}
//[OpenDirectory Object]
{ ----------------------------------------------------------------------
                               TOpenDirDialog
----------------------------------------------------------------------- }
//[TOpenDirDialog DEFINITION]
  TOpenDirDialog = object( TObj )
  {* Dialog for open directories, uses SHBrowseForFolder. }
  private
    FWnd: HWnd;
    function GetDialogWnd: HWnd;
    function GetTitle: KOLString;
    procedure SetTitle(const AValue: KOLString);
  protected
    FDlg: PObj;
    FOptions: TOpenDirOptions;
    FCenterOnScreen: Boolean;
    FOnSelChanged: TOnODSelChange;
    function GetPath: KOLString;
    procedure SetInitialPath(const Value: KOLString);
    procedure SetCenterOnScreen(const Value: Boolean);
    procedure SetOnSelChanged(const Value: TOnODSelChange);
    function GetInitialPath: KOLString;
  public
    destructor Destroy; {-}virtual;{+}{++}(*override;*){--}
    {* destructor }
    function Execute : Boolean;
    {* Call it to select directory by user. Returns True, if operation was
       not cancelled by user. }
    property Title : KOLString read GetTitle write SetTitle;
    {* Title for a dialog. }
    property Options : TOpenDirOptions read FOptions write FOptions;
    {* Option flags. }
    property Path : KOLString read GetPath;
    {* Resulting (selected by user) path. }
    property InitialPath: KOLString read GetInitialPath write SetInitialPath;
    {* Set this property to a path of directory to be selected initially
       in a dialog. }
    property CenterOnScreen: Boolean read FCenterOnScreen write SetCenterOnScreen;
    {* Set it to True to center dialog on screen. }
    property OnSelChanged: TOnODSelChange read FOnSelChanged write SetOnSelChanged;
    {* This event is called every time, when user selects another directory.
       It is possible to enable/disable OK button in dialog and/or change
       dialog status text in responce to event. }
    property WndOwner: HWnd read FWnd write FWnd;
    {* Owner window. If you want to provide your dialog visible over stay-on-top
       form, fire it as a child of the form, assigning the handle of form window
       to this property first. }
    property DialogWnd: HWnd read GetDialogWnd;
    {* Handle to the open directory dialog itself, become available on the
       first call of callback procedure (i.e. on the first call to OnSelChanged).
    }
  end;
//[END OF TOpenDirDialog DEFINITION]
{$endif read_interface}

{$ifdef read_implementation}
{$I KOLDirDlgEx.pas}

function NewOpenDirDialog( const Title: KOLString; Options: TOpenDirOptions ):
         POpenDirDialog;
begin
  New( Result, Create );
  Result.FDlg:=NewOpenDirDialogEx;
  Result.FOptions := [ odOnlySystemDirs ];
  if Options <> [] then
    Result.FOptions := Options;
  Result.Title := Title;
end;

{ TOpenDirDialog }

function TOpenDirDialog.GetTitle: KOLString;
begin
  Result:=POpenDirDialogEx(FDlg).Title;
end;

function TOpenDirDialog.GetDialogWnd: HWnd;
begin
  Result:=POpenDirDialogEx(FDlg).Form.GetWindowHandle;
end;

procedure TOpenDirDialog.SetTitle(const AValue: KOLString);
begin
  POpenDirDialogEx(FDlg).Title:=AValue;
end;

function TOpenDirDialog.GetPath: KOLString;
begin
  Result:=ExcludeTrailingPathDelimiter(POpenDirDialogEx(FDlg).Path);
  if Result = '' then
    Result:='\';
end;

procedure TOpenDirDialog.SetInitialPath(const Value: KOLString);
begin
  POpenDirDialogEx(FDlg).InitialPath:=Value;
end;

procedure TOpenDirDialog.SetCenterOnScreen(const Value: Boolean);
begin
end;

procedure TOpenDirDialog.SetOnSelChanged(const Value: TOnODSelChange);
begin
end;

function TOpenDirDialog.GetInitialPath: KOLString;
begin
  Result:=POpenDirDialogEx(FDlg).InitialPath;
end;

destructor TOpenDirDialog.Destroy;
begin
  POpenDirDialogEx(FDlg).Free;
  inherited;
end;

function TOpenDirDialog.Execute: Boolean;
begin
  Result:=POpenDirDialogEx(FDlg).Execute;
end;

{$endif read_implementation}

