{$ifndef read_implementation}
{$ifdef FPC} {$mode delphi} {$endif FPC}
unit KOLDirDlgEx;

interface

uses Windows, Messages, KOL {$IFDEF USE_GRUSH}, ToGrush, KOLGRushControls {$ENDIF}
     {$ifdef FPC}{$if not defined(VER2_2_0) and defined(wince)},commctrl{$endif}{$endif};

{$endif read_implementation}

{$IFDEF EXTERNAL_DEFINES}
        {$INCLUDE EXTERNAL_DEFINES.INC}
{$ENDIF EXTERNAL_DEFINES}

//{$DEFINE NO_DEFAULT_FASTSCAN}
           { if you add such option, all icons for removable and remote drives
             are obtained by default accessing these drives, slow. }
//{$DEFINE DIRDLGEX_NO_DBLCLK_ON_NODE_OK}
           { if this option is on, double clicks in the directories tree
             is used only to expand/collapse children nodes. By default,
             double click on a node w/o children selects it and
             finishes the dialog as in case when OK is clicked. }

//{$DEFINE DIRDLGEX_LINKSPANEL}
           { if this option is on, links panel is available (on the left side).
             Also USE_MENU_CURCTL symbol must be added! }
//{$DEFINE DIRDLGEX_STDFONT}
           { add this option to use standard font size in a links panel
             (otherwise Arial, 14 is used) }
{$DEFINE DIRDLGEX_BIGGERPANEL}
           { a bit bigger links panel with bigger buttons on it }

{ ----------------------------------------------------------------------

                TOpenDirDialogEx

----------------------------------------------------------------------- }
{ TOpenDirDialogEx - ������������ ������������ ������� ������ �����.
  (c) by Vladimir Kladov, 2005, 14 Dec.
  �������������: ����������� ������ �������� ��������, ��� ������ ��������
                 ������ ����� ������������� ����������� ������, ����� ������
                 ������ �����, ��� ���� ������: ��������� ��������� ��������,
                 ����� ������������ ���� ��� �������������� ����� ����� ��������
                 ������ ������� ��������� �����, ���� ��� ���� ��� �����.
                 �� ������ ��� � ������ ���������, ����� ���, ��� ������� ����
                 � ������ �������� ����� ������ ��������� ��� ���� ������, ���
                 ���, ��� � ����������� ������� �������� ����������� �� ������
                 ������ �����, � ������ �� (��� ���-�� ������, ��� ������������
                 ������ ������ ��� ����, ����� ������ ��, �� ������� �����?).
  ����������� TOpenDirDialogEx: �������� ������. ��� ��������� ����������
  Execute ��������������� ������ �� ����� ������, ������� ���� ������� �����
  (�.�. ��������� �������� ����������� ����������� ���������). �������������
  ����������� ������ ����� ������ �����, ������� ����������, ����� ��������
  ������� ��� ����� �� ���� �� ����� ����� �� ������� �����. ������ �����
  ����������� � ��������������� ��� �� �������� (������������). ���������
  ������������ ����� ������������� �� (��� ������������ ���������������
  ����������, ������� �� ����������� - ���� � ������ ������������� � ����������
  �� �������� ���, � ����� �������������� �������� �� ������� ����� �� ������
  �������� ���� �� �����).

  �������������� ���������:
  ���� ����������� �������� ������� �� �������, ��������� �������.
  ���� ����������� ������������� ���������� �� ��������� (FilterAttrs), ��������,
  ���� ��������� FILE_ATTRIBUTE_HIDDEN, �� ������� ����� � ������ �������� �� �����.
}
const
  WM_USER_RESCANTREE = WM_USER;

{$ifdef FPC}
  {$if not defined(UNICODE_CTRLS) and not defined(wince)}
    {$I fpc_unicode_add.inc}
  {$endif}
{$endif FPC}

{$ifdef wince}
type
  TFindexInfoLevels = FINDEX_INFO_LEVELS;
  TFindexSearchOps = FINDEX_SEARCH_OPS;
{$endif wince}

type
  TFindFirstFileEx = function(lpFileName: PKOLChar; fInfoLevelId: TFindexInfoLevels;
    lpFindFileData: Pointer; fSearchOp: TFindexSearchOps; lpSearchFilter: Pointer;
    dwAdditionalFlags: DWORD): THandle; stdcall;

  POpenDirDialogEx = ^TOpenDirDialogEx;
  TOpenDirDialogEx = object( TObj )
  protected
    FFastScan: Boolean;
    DlgClient: PControl;
    DirTree: PControl;
    BtnPanel: PControl;
    RescanningNode, RescanningTree: Boolean;
    FPath, FRecycledName: KOLString;
    FRemoteIconSysIdx: Integer;
    FFindFirstFileEx: TFindFirstFileEx;
    k32: THandle;
    DialogForm: PControl;
    function GetFindFirstFileEx: TFindFirstFileEx;
    procedure SetPath(const Value: KOLString);
    function GetDialogForm: PControl;
    procedure DoOK( Sender: PObj );
    procedure DoCancel( Sender: PObj );
    procedure DoNotClose( Sender: PObj; var Accept: Boolean );
    procedure DoShow( Sender: PObj );
    function DoMsg( var Msg: TMsg; var Rslt: Integer ): Boolean;
    function DoExpanding( Sender: PControl; Item: THandle; Expand: Boolean )
                 : Boolean;
    function DoFilterAttrs( Attrs: DWORD; const APath: KOLString ): Boolean;
    procedure Rescantree;
    procedure RescanNode( node: Integer );
    procedure RescanDisks;
    function RemoteIconSysIdx: Integer;
    procedure CheckNodeHasChildren( node: Integer );
    procedure CreateDialogForm;
    property _FindFirstFileEx: TFindFirstFileEx read GetFindFirstFileEx;
    procedure DeleteNode( node: Integer );
    procedure DestroyingForm( Sender: PObj );
    function GetNodePath(N: THandle): KOLString;
  public
    OKCaption, CancelCaption: KOLString;
    FilterAttrs: DWORD;
    FilterRecycled: Boolean;
    Title: KOLString;
    property Form: PControl read GetDialogForm;
    {* DialogForm object. Though it is possible to do anything since it is
       in public section, do this only if you understand possible consequences.
       E.g., use it to change DialogForm bounding rectangle on screen or to
       add your own controls, event handlers and so on. }
    destructor Destroy; virtual;
    function Execute: Boolean;
    property InitialPath: KOLString read FPath write SetPath;
    property Path: KOLString read FPath write SetPath;
    property FastScan: Boolean read FFastScan write FFastScan;
    procedure DoubleClick( Sender: PControl; var M: TMouseEventData );
  {$IFDEF DIRDLGEX_LINKSPANEL}
  protected
    LinksPanel, LinksBox, LinksTape: PControl;
    LinksUp, LinksDn, LinksAdd: PControl;
    LinksList: PStrListEx;
    LinksImgList: PImageList;
    LinksRollTimer: PTimer;
    LinksPopupMenu: PMenu;
    procedure CreateLinksPanel;
    function GetLinksPanelOn: Boolean;
    procedure SetLinksPanelOn( const Value: Boolean );
    function GetLinksCount: Integer;
    function GetLinks(idx: Integer): KOLString;
    procedure SetLinks(idx: Integer; const Value: KOLString);
    procedure SetupLinksTapeHeight;
    procedure SetUpTaborders;
    procedure LinksUpClick( Sender: PControl; var Mouse: TMouseEventData );
    procedure LinksDnClick( Sender: PControl; var Mouse: TMouseEventData );
    procedure LinksUpDnStop( Sender: PControl; var Mouse: TMouseEventData );
    procedure LinksAddClick( Sender: PObj );
    procedure LinkClick( Sender: PObj );
    procedure LinksRollTimerTimer( Sender: PObj );
    //procedure LinksPanelShowEvent( Sender: PObj );
    procedure RemoveLinkClick( Sender: PMenu; Item: Integer );
  public
    property LinksPanelOn: Boolean read GetLinksPanelOn write SetLinksPanelOn;
    property LinksCount: Integer read GetLinksCount;
    property Links[ idx: Integer ]: KOLString read GetLinks write SetLinks;
    procedure AddLinks( SL: PStrList );
    function CollectLinks: PStrList;
    function LinkPresent( const s: KOLString ): Boolean;
    procedure RemoveLink( const s: KOLString );
    procedure ClearLinks;
  {$ENDIF DIRDLGEX_LINKSPANEL}
  end;

{$ifndef read_implementation}
function NewOpenDirDialogEx: POpenDirDialogEx;

{$IFDEF KOL_MCK}
{$IFNDEF DIRDLGEX_OPTIONAL} { add this symbol if you want use
         both types of the open directory dialog in your application
         (and in such case, call a constructor of the TOpenDirDialogEx
         object manually). }
type TKOLOpenDirDialog = POpenDirDialogEx;
{$ENDIF}
{$ENDIF}

implementation
{$endif read_implementation}

function NewOpenDirDialogEx: POpenDirDialogEx;
begin
  new( Result, Create );
  {$IFNDEF NO_DEFAULT_FASTSCAN}
  Result.FastScan := TRUE;
  {$ENDIF}
end;

procedure NewPanelWithSingleButtonToolbar( AParent: PControl; W, H: Integer;
  A: TControlAlign; Bmp: PBitmap; const C, T: KOLString; var Pn, Bar: PControl;
  const ClickEvent: TOnEvent; DownEvent, ReleaseEvent: TOnMouse;
  P: PMenu );
var i: Integer;
    Buffer: PKOLChar;
begin
  Pn := NewPanel( AParent, esNone ).SetSize( 0, H ).SetAlign( A );
  Pn.Border := 0;
{$ifdef wince}
  pn.Color:=clBtnFace;
  pn.HasBorder:=True;
{$endif wince}
  Bar := NewToolbar( Pn, caClient, [
      tboNoDivider, tboTextBottom {, tboFlat} ],
    Bmp.ReleaseHandle,
    [ PKOLChar( {$IFDEF TOOLBAR_DOT_NOAUTOSIZE_BUTTON} '.' + {$ENDIF} C ) ], [ 0 ] );
  Buffer := AllocMem( (Length( T ) + 1)*SizeOf(KOLChar) );
  if T <> '' then
    Move(T[1], Buffer^, Length( T )*SizeOf(KOLChar));
  {$IFDEF USE_GRUSH}
  i := 0;
  {$IFDEF TOGRUSH_OPTIONAL}
  if NoGrush then
  begin
    i := Bar.TBIndex2Item(0);
    Bar.Perform( TB_SETBITMAPSIZE, 0, MakeLong( Bmp.Width, Bmp.Height ) );
  end;
  if not NoGrush then
  {$ENDIF}
  begin
    PGRushControl( Bar.Children[0] ).All_GlyphVAlign := vaTop;
    if C = '' then
    begin
      PGRushControl( Bar.Children[0] ).All_GlyphVAlign := vaCenter;
      PGRushControl( Bar.Children[0] ).All_ContentOffsets := MakeRect( -4, -4, 4, 4 );
    end;
    PGRushControl( Bar.Children[0] ).All_GlyphHAlign := haCenter;
    PGRushControl( Bar.Children[0] ).All_GlyphWidth := Bmp.Width;
    PGRushControl( Bar.Children[0] ).All_Spacing := 2;
    PGRushControl( Bar.Children[0] ).Width := W;
    if not Assigned( DownEvent ) then
      PGRushControl( Bar.Children[0] ).OnClick := ClickEvent
    else
    begin
      PGRushControl( Bar.Children[0] ).OnMouseDown := DownEvent;
      PGRushControl( Bar.Children[0] ).OnMouseUp := ReleaseEvent;
    end;
    PGRushControl( Bar.Children[0] ).CustomData := Buffer;
    PGRushControl( Bar.Children[0] ).All_ColorOuter := AParent.Color;
    //PGRushControl( Bar ).All_ColorFrom := AParent.Color;
    //PGRushControl( Bar ).All_ColorTo   := AParent.Color;
  end
  {$IFDEF TOGRUSH_OPTIONAL}
    else
  begin
    i := Bar.TBIndex2Item(0);
    Bar.TBButtonWidth[ i ] := W;
    Bar.Perform( TB_SETBITMAPSIZE, 0, MakeLong( Bmp.Width, Bmp.Height ) );
    if not Assigned( ReleaseEvent ) then
      Bar.OnClick := ClickEvent
    else
    begin
      Bar.OnMouseDown := DownEvent;
      Bar.OnMouseUp := ReleaseEvent;
    end;
    Bar.CustomData := Buffer;
  end
  {$ENDIF TOGRUSH_OPTIONAL}
  ;
  {$ELSE}
  i := Bar.TBIndex2Item(0);
  Bar.TBButtonWidth[ i ] := W;
  Bar.Perform( TB_SETBITMAPSIZE, 0, MakeLong( Bmp.Width, Bmp.Height ) );
  if not Assigned( ReleaseEvent ) then
    Bar.OnClick := ClickEvent
  else
  begin
    Bar.OnMouseDown := DownEvent;
    Bar.OnMouseUp := ReleaseEvent;
  end;
  Bar.CustomData := Buffer;
  {$ENDIF USE_GRUSH}
  ToolbarSetTooltips( Bar, i, [ PKOLChar( T ) ] );
  if P <> nil then
  begin
    Pn.SetAutoPopupMenu( P );
  end;
  Bmp.Free;
end;

{ TOpenDirDialogEx }

{$IFDEF DIRDLGEX_LINKSPANEL}
procedure TOpenDirDialogEx.AddLinks(SL: PStrList);
var i: Integer;
begin
  for i := 0 to SL.Count-1 do
    if not LinkPresent( SL.Items[ i ] ) then
      Links[ LinksCount ] := SL.Items[ i ];
end;

{$ENDIF DIRDLGEX_LINKSPANEL}

procedure TOpenDirDialogEx.CheckNodeHasChildren(node: Integer);
var HasSubDirs: Boolean;
{$ifndef wince}
    txt: KOLString;
{$endif wince}
    F: THandle;
    Find32: TWin32FindData;
    ii, n: Integer;
begin
  HasSubDirs := FALSE;
{$ifndef wince}
  txt := DirTree.TVItemText[ node ];
  if (Length( txt ) = 2) then
    if (txt[ 2 ] = ':') then
    begin
      ii := GetDriveType( PKOLChar( txt + '\' ) );
      if IntIn( ii, [ DRIVE_REMOVABLE, DRIVE_REMOTE, DRIVE_CDROM ] ) then
        HasSubDirs := TRUE;
    end;
  if not HasSubDirs then
  begin
    if WinVer >= wvNT then
    begin
{$endif wince}
      _FindFirstFileEx;
      F := FFindFirstFileEx( PKOLChar( GetNodePath( node ) + '\*.*' ),
        FindExInfoStandard, @ Find32, FindExSearchLimitToDirectories, nil, 0 );
      if F <> INVALID_HANDLE_VALUE then
      begin
        while TRUE do
        begin
          if Find32.dwFileAttributes and FILE_ATTRIBUTE_DIRECTORY <> 0 then
          if (Find32.cFileName <> KOLString( '.' )) and (Find32.cFileName <> '..') then
          if DoFilterAttrs( Find32.dwFileAttributes, Find32.{$ifdef wince}cFileName{$else}cAlternateFileName{$endif} ) then
          begin
            HasSubDirs := TRUE;
            break;
          end;
          if not FindNextFile( F, Find32 ) then break;
        end;
        if not FindClose( F ) then
        {begin
          asm
            nop
          end;
        end};
      end;
{$ifndef wince}
    end
      else
    begin
      F := FindFirstFile( PKOLChar( GetNodePath( node ) + '\*.*' ), Find32 );
      if F <> INVALID_HANDLE_VALUE then
      begin
        while TRUE do
        begin
          if Find32.dwFileAttributes and FILE_ATTRIBUTE_DIRECTORY <> 0 then
          if (Find32.cFileName <> KOLString( '.' )) and (Find32.cFileName <> '..') then
          begin
            HasSubDirs := TRUE;
            break;
          end;
          if not FindNextFile( F, Find32 ) then break;
        end;
        FindClose( F );
      end;
    end;
  end;
{$endif wince}
  if not HasSubDirs then
  begin
    DirTree.TVExpand( node, TVE_COLLAPSE );
    n := DirTree.TVItemChild[ node ];
    while n <> 0 do
    begin
      ii := n;
      n := DirTree.TVItemNext[ n ];
      //DirTree.TVDelete( ii );
      DeleteNode( ii );
    end;
  end;
  if DirTree.TVItemParent[ node ] = 0 then HasSubDirs := TRUE;
  DirTree.TVItemHasChildren[ node ] := HasSubDirs;
end;

{$IFDEF DIRDLGEX_LINKSPANEL}
procedure TOpenDirDialogEx.ClearLinks;
var i: Integer;
begin
  if LinksList = nil then Exit;
  LinksList.Clear;
  for i := LinksTape.ChildCount-1 downto 0 do
    LinksTape.Children[ i ].Free;
  LinksTape.Height := 8;
  LinksTape.Top := 0;
end;

function TOpenDirDialogEx.CollectLinks: PStrList;
var i: Integer;
begin
  Result := NewStrList;
  for i := 0 to LinksCount-1 do
    Result.Add( Links[ i ] );
end;
{$ENDIF DIRDLGEX_LINKSPANEL}

procedure TOpenDirDialogEx.CreateDialogForm;
var Sysimages: PImageList;
    BtOk, BtCancel, DTSubPanel: PControl;
    s: KOLString;
begin
   if not Assigned( DialogForm ) then
   begin
     if Title = '' then Title := 'Select folder' {$ifdef wince} + ':' {$endif};
{$ifndef wince}
     OleInit;
{$endif wince}
     DialogForm := NewForm( Applet, '' ){$ifndef wince}.SetSize( 324, 330 ).CenterOnParent{$endif};
     DialogForm.OnDestroy := DestroyingForm;
     DialogForm.OnClose := DoNotClose;
     DialogForm.Tabulate;
{$ifdef wince}
     DialogForm.Border := 4;
     DialogForm.Font.FontName:='Tahoma';
     DialogForm.Font.FontHeight:=-11;
{$else}
     DialogForm.Border := 6;
     DialogForm.MinWidth := 324;
     DialogForm.MinHeight := 330;
     DialogForm.ExStyle := DialogForm.ExStyle or
       WS_EX_DLGMODALFRAME or WS_EX_WINDOWEDGE;
     DialogForm.Style := DialogForm.Style and
       not (WS_MINIMIZEBOX or WS_MAXIMIZEBOX);
{$endif wince}
     Sysimages := NewImageList( DialogForm );
     Sysimages.LoadSystemIcons( TRUE );
     //DlgClient := NewPanel( DialogForm, esNone ).SetAlign(caClient);
     {$IFDEF USE_GRUSH}
     {$IFDEF TOGRUSH_OPTIONAL}
     if not NoGrush then
     {$ENDIF TOGRUSH_OPTIONAL}
     begin
       DialogForm.Color := clGRushLight;
       DlgClient := NewPanel( DialogForm, esNone ).SetAlign(caClient);
       //DlgClient.Border := 2;
     end
     {$IFDEF TOGRUSH_OPTIONAL}
       else DlgClient := DialogForm;
     {$ENDIF TOGRUSH_OPTIONAL}
     ;
     {$ELSE}
     DlgClient := DialogForm;
     {$ENDIF}

     {$IFDEF USE_GRUSH}
     {$IFDEF TOGRUSH_OPTIONAL}
     if not NoGrush then
     {$ENDIF TOGRUSH_OPTIONAL}
     begin
       DTSubPanel := KOL.NewPanel( DlgClient, esNone );
       DTSubPanel.Color := clWindow;
       DTSubPanel.Border := 2;
       BtnPanel := NewPanel( DlgClient, esTransparent )
         .SetSize( 0, 26 ).SetAlign(caBottom );
       //BtnPanel.Color := clGRushMedium;
       BtnPanel.Border := 2;
       DTSubPanel.SetAlign( caClient );
       DirTree := NewTreeView( DTSubPanel, [ tvoLinesRoot ], Sysimages, nil );
       {$IFNDEF DIRDLGEX_NO_DBLCLK_ON_NODE_OK}
       DirTree.OnMouseDblClk := DoubleClick;
       {$ENDIF}
       DirTree.Color := clWindow;
       DirTree.OnTVExpanding := DoExpanding;
       DirTree.SetAlign( caClient );
     end
     {$IFDEF TOGRUSH_OPTIONAL}
       else
     begin
       DTSubPanel := DlgClient;
       DirTree := NewTreeView( DTSubPanel, [ tvoLinesRoot ], Sysimages, nil );
       DirTree.Color := clWindow;
       DirTree.OnTVExpanding := DoExpanding;
       BtnPanel := NewPanel( DlgClient, esTransparent )
         .SetSize( 0, 26 ).SetAlign(caBottom );
       BtnPanel.Border := 2;
       DirTree.SetAlign( caClient );
     end
     {$ENDIF TOGRUSH_OPTIONAL}
     ;
     {$ELSE}
{$ifdef wince}
     NewLabel(DlgClient, Title).AutoSize(True).SetAlign(caTop);
{$endif wince}
     DTSubPanel := DlgClient;
     DirTree := NewTreeView( DTSubPanel, [ {$ifndef wince} tvoLinesRoot {$endif} ], Sysimages, nil );
     DirTree.Color := clWindow;
     DirTree.OnTVExpanding := DoExpanding;
     BtnPanel := NewPanel( DlgClient, esTransparent )
       .SetSize( 0, 26 ).SetAlign(caBottom );
     BtnPanel.Border := 2;
     DirTree.SetAlign( caClient );
     {$ENDIF}
     DTSubPanel.OnMessage := DoMsg;
     DlgClient := DTSubPanel; // !!!
     s := CancelCaption; if s = '' then s := 'Cancel';
     BtCancel := NewButton( BtnPanel, s );
     BtCancel.MinWidth := 75; BtCancel.OnClick := DoCancel;
     BtCancel.AutoSize( TRUE ).SetAlign( caRight );
     s := OKCaption; if s = '' then s := 'OK';
     BtOK := NewButton( BtnPanel, s );
     BtOK.MinWidth := 75; BtOK.OnClick := DoOK;
     BtOK.AutoSize( TRUE ).SetAlign( caRight );
     BtCancel.TabOrder := 1;
     BtOK.DefaultBtn := TRUE;
     BtCancel.CancelBtn := TRUE;
     DialogForm.OnShow := DoShow;
     {$IFDEF USE_GRUSH}
     {$IFDEF TOGRUSH_OPTIONAL}
     if not NoGrush then
     {$ENDIF TOGRUSH_OPTIONAL}
     begin
       BtOK.Transparent := TRUE;
       BtCancel.Transparent := TRUE;
     end;
     {$ENDIF USE_GRUSH}
   end;
end;

{$IFDEF DIRDLGEX_LINKSPANEL}
procedure TOpenDirDialogEx.CreateLinksPanel;
var BUp, BDn, BLt: PBitmap;
    {$IFDEF USE_GRUSH}
    cFrom: TColor;
    {$ENDIF USE_GRUSH}

    function NewArrowBitmap( const Pts: array of Integer ): PBitmap;
    begin
      Result := NewDibBitmap( 16, 8, pf32bit );
      Result.Canvas.Brush.Color := clBtnFace;
      Result.Canvas.FillRect( Result.BoundsRect );
      Result.Canvas.Brush.Color := clBlack;
      Result.Canvas.Polygon( [ MakePoint( Pts[ 0 ], Pts[ 1 ] ),
                               MakePoint( Pts[ 2 ], Pts[ 3 ] ),
                               MakePoint( Pts[ 4 ], Pts[ 5 ] ),
                               MakePoint( Pts[ 6 ], Pts[ 7 ] ) ] );
    end;

var PnUp, LUp, PnDn, LDn, PnLt, LLt: PControl;
    d: Integer;
begin
  if LinksPanel <> nil then Exit;
  GetDialogForm;

  BUp := NewArrowBitmap( [ 2, 6, 7, 1, 8, 1, 13, 6 ] );
  BDn := NewArrowBitmap( [ 2, 1, 7, 6, 8, 6, 13, 1 ] );
  BLt := NewArrowBitmap( [ 11, 0, 4, 3, 4, 4, 11, 7 ] );

  LinksPanel := NewPanel( DlgClient, esLowered )
    .SetSize( {$IFDEF DIRDLGEX_BIGGERPANEL} 14 + {$ENDIF} 64, 0 )
    .SetAlign( caLeft );
  //LinksPanel.OnShow := LinksPanelShowEvent;
  LinksPanel.Border := 2;
  {$IFNDEF DIRDLGEX_STDFONT}
{$ifndef wince}
  LinksPanel.Font.FontName := 'Arial';
  LinksPanel.Font.FontHeight := 14;
{$endif wince}
  {$ENDIF DIRDLGEX_STDFONT}

  d := 0;
  {$IFDEF USE_GRUSH}
    {$IFDEF TOGRUSH_OPTIONAL}
    if not NoGrush then
    {$ENDIF TOGRUSH_OPTIONAL}
      d := 2;
  {$ENDIF USE_GRUSH}
  NewPanelWithSingleButtonToolbar( LinksPanel, LinksPanel.Width-6+d, 15, caTop,
    BUp, '', '', PnUp, LUp, nil, LinksUpClick, LinksUpDnStop, nil );
  NewPanelWithSingleButtonToolbar( LinksPanel, LinksPanel.Width-6+d, 15, caBottom,
    BDn, '', '', PnDn, LDn, nil, LinksDnClick, LinksUpDnStop, nil );
  NewPanelWithSingleButtonToolbar( BtnPanel, 20, 16, caNone,
    BLt, '', '', PnLt, LLt, LinksAddClick, nil, nil, nil ); PnLt.Width := 20;
{$ifdef wince}
  PnLt.Align:=caLeft;
{$else}
  PnLt.SetPosition( 68, 0 );
  PnLt.Transparent := TRUE;
  LLt.Transparent := TRUE;
{$endif wince}
  LinksBox := NewPaintBox( LinksPanel ).SetAlign(caClient);
  LinksBox.Border := 0;
  LinksTape := NewPaintBox( LinksBox ).SetSize( LinksBox.Width, 0 );
  //LinksTape.DoubleBuffered := TRUE;
  {$IFDEF USE_GRUSH}
    {$IFDEF TOGRUSH_OPTIONAL}
    if not NoGrush then
    {$ENDIF TOGRUSH_OPTIONAL}
    begin
      LinksTape.Border := 2;
      LinksTape.MarginLeft := -2;
      LinksTape.MarginRight := -2;
      LinksTape.MarginTop := 2;
      LinksTape.MarginBottom := 2;
      //LinksPanel.Transparent := TRUE;
      PGRushControl( LinksPanel ).All_GradientStyle := gsHorizontal;
      cFrom := PGRushControl( LinksPanel ).Def_ColorFrom;
      PGRushControl( LinksPanel ).All_ColorTo := {
        PGRushControl( LinksPanel ).Def_ColorFrom;
      PGRushControl( LinksPanel ).All_ColorFrom :=} cFrom;
      LinksBox.Color := cFrom;
      //LinksAdd.Left := LinksAdd.Left + 6;
    end
    {$IFDEF TOGRUSH_OPTIONAL}
      else
    begin
      //LinksTape.Border := 0;
    end
    {$ENDIF TOGRUSH_OPTIONAL}
    ;
  {$ELSE not USE_GRUSH}
  //LinksTape.Border := 0;
  {$ENDIF USE_GRUSH}
  LinksPanel.Visible := FALSE;
  LinksRollTimer := NewTimer( 50 );
  //LinksRollTimer.Enabled := FALSE;
  LinksRollTimer.OnTimer := LinksRollTimerTimer;
  LinksPanel.Add2AutoFree( LinksRollTimer );
end;
{$ENDIF DIRDLGEX_LINKSPANEL}

procedure TOpenDirDialogEx.DeleteNode(node: Integer);
  function NodeIsParentOf( node, parent: Integer ): Boolean;
  begin
    Result := TRUE;
    while node <> 0 do
    begin
      if node = parent then Exit;
      node := DirTree.TVItemParent[ node ];
    end;
    Result := FALSE;
  end;
var sel, n: Integer;
begin
  sel := DirTree.TVSelected;
  if (sel <> 0) and NodeIsParentOf( sel, node ) then
  begin
    n := DirTree.TVItemPrevious[ node ];
    if n = 0 then
      n := DirTree.TVItemNext[ node ];
    DirTree.TVSelected := n;
  end;
  DirTree.TVDelete( node );
end;

destructor TOpenDirDialogEx.Destroy;
begin
  Free_And_Nil( DialogForm );
  FPath := '';
  FRecycledName := '';
  OKCaption := '';
  CancelCaption := '';
  Title := '';
  {$IFDEF DIRDLGEX_LINKSPANEL}
  LinksList.Free;
  {$ENDIF DIRDLGEX_LINKSPANEL}
  inherited;
{$ifndef wince}
  OleUnInit;
{$endif wince}
end;

procedure TOpenDirDialogEx.DestroyingForm(Sender: PObj);
begin
  DialogForm := nil;
end;

procedure TOpenDirDialogEx.DoCancel(Sender: PObj);
begin
  DialogForm.ModalResult := -1;
end;

function TOpenDirDialogEx.DoExpanding(Sender: PControl; Item: THandle;
  Expand: Boolean): Boolean;
begin
  Result := FALSE;
  if RescanningNode or RescanningTree then Exit;
  if Expand then
    RescanNode( Item );
end;

function TOpenDirDialogEx.DoFilterAttrs(Attrs: DWORD; const APath: KOLString): Boolean;
begin
  Result := (Attrs and FilterAttrs = 0);
  if not Result then Exit;
  if FilterRecycled then
  begin
    if (Attrs and (FILE_ATTRIBUTE_SYSTEM or FILE_ATTRIBUTE_HIDDEN) =
                  (FILE_ATTRIBUTE_SYSTEM or FILE_ATTRIBUTE_HIDDEN))
    then
    //if StrEq( APath, 'RECYCLED' ) then
      Result := FALSE;
  end;
end;

function TOpenDirDialogEx.DoMsg(var Msg: TMsg; var Rslt: Integer): Boolean;
var NMHdr: PNMHdr;
    NMCustomDraw: PNMCustomDraw;
    i: Integer;
begin
  Result := FALSE;
  if DialogForm = nil then Exit;
  if Msg.message = WM_USER_RESCANTREE then
  begin
    Rescantree;
    DirTree.Focused := TRUE;
    Result := TRUE;
  end
    else
  if Msg.message = WM_NOTIFY then
  begin // ������������� ������ ��������� ������� ����, ����� ��������� �������
        // � ��� �������� �����, � "��������" ������ [+], ���� ����
    NMHdr := Pointer( Msg.lParam );
    if DirTree = nil then Exit;
    if NMHdr.hwndFrom = DirTree.Handle then
    CASE LongInt(NMHdr.code) OF
    NM_CUSTOMDRAW:
      begin
        NMCustomDraw := Pointer( NMHdr );
        if NMCustomDraw.dwDrawStage = CDDS_ITEMPOSTPAINT then
        begin
          i := NMCustomDraw.dwItemSpec;
          if DirTree.TVItemData[ i ] = nil then // ���� ��� �� ����������
          begin
            CheckNodeHasChildren( i );          // ��������� ����
            DirTree.TVItemData[ i ] := Pointer( 1 ); // ���� = "��������"
          end;
          Rslt := CDRF_DODEFAULT; // ����� ������ ���� ��� ��� ������
        end
        else
        if NMCustomDraw.dwDrawStage = CDDS_PREPAINT then
          Rslt := CDRF_NOTIFYITEMDRAW // �������� ��� ������� ���� �� ������ CDDS_ITEMPREPAINT
        else
          Rslt := CDRF_NOTIFYPOSTPAINT; // ��� CDDS_ITEMPREPAINT: �������� � CDDS_ITEMPOSTPAINT
        Result := TRUE;
      end;
    END;
  end;
end;

procedure TOpenDirDialogEx.DoNotClose(Sender: PObj;
  var Accept: Boolean);
begin
  Accept := FALSE;
  DialogForm.Hide;
end;

procedure TOpenDirDialogEx.DoOK(Sender: PObj);
begin
  DialogForm.ModalResult := 1;
end;

procedure TOpenDirDialogEx.DoShow(Sender: PObj);
begin
  DlgClient.PostMsg( WM_USER_RESCANTREE, 0, 0 );
  {$IFDEF DIRDLGEX_LINKSPANEL}
  if LinksPanelOn and Assigned( LinksTape ) then
  begin
    Global_Align( LinksTape );
    SetupLinksTapeHeight;
  end;
  {$ENDIF DIRDLGEX_LINKSPANEL}
end;

type
  PControl_ = ^TControl_;
  TControl_ = object( TControl )
  end;

procedure TOpenDirDialogEx.DoubleClick(Sender: PControl;
  var M: TMouseEventData);
var N: DWORD;
    Where: DWORD;
begin
  N := DirTree.TVItemAtPos( M.X, M.Y, Where );
  if (N = DirTree.TVSelected) and
     not DirTree.TVItemHasChildren[ N ] then
     Form.ModalResult := 1;
end;

function TOpenDirDialogEx.Execute: Boolean;
var ParentForm: PControl_;
begin
  CreateDialogForm;
  DlgClient.ActiveControl := DirTree;
{$ifndef wince}
  DialogForm.Caption := Title;
{$endif wince}
  ParentForm := PControl_( Applet.ActiveControl );
  if ParentForm <> nil then
  begin
    if not ParentForm.fIsForm then
      ParentForm := PControl_( Applet );
  end;
  if ParentForm <> nil then
    DialogForm.StayOnTop := ParentForm.StayOnTop;
  DialogForm.ShowModal;
  DialogForm.Hide;
  if ParentForm <> nil then
    SetForegroundWindow( ParentForm.Handle );
  Result := DialogForm.ModalResult >= 0;
  if Result then
  begin
    Path := IncludeTrailingPathDelimiter(
      GetNodePath( DirTree.TVSelected ) );
  end;
end;

function TOpenDirDialogEx.GetDialogForm: PControl;
begin
  CreateDialogForm;
  Result := DialogForm;
end;

function TOpenDirDialogEx.GetFindFirstFileEx: TFindFirstFileEx;
begin
{$ifdef wince}
  FFindFirstFileEx:=@FindFirstFileEx;
{$else}
  if not Assigned( FFindFirstFileEx ) then
  begin
    k32 := GetModuleHandle( 'kernel32.dll' );
    FFindFirstFileEx := GetProcAddress( k32, 'FindFirstFileEx' + {$ifdef UNICODE_CTRLS}'W'{$else}'A'{$endif} );
  end;
{$endif wince}
  Result := FFindFirstFileEx;
end;

function TOpenDirDialogEx.GetNodePath(N: THandle): KOLString;
begin
  Result:=DirTree.TVItemPath(N, '\');
{$ifdef wince}
  System.Delete(Result, 1, 9);
{$endif wince}
end;

{$IFDEF DIRDLGEX_LINKSPANEL}
function TOpenDirDialogEx.GetLinks(idx: Integer): KOLString;
begin
  Result := '';
  if (LinksList <> nil) and (LinksList.Count > idx) then
    Result := LinksList.Items[ idx ];
end;

function TOpenDirDialogEx.GetLinksCount: Integer;
begin
  Result := 0;
  if LinksList <> nil then Result := LinksList.Count;
end;

function TOpenDirDialogEx.GetLinksPanelOn: Boolean;
begin
  Result := (LinksPanel <> nil) and (LinksPanel.Visible);
end;

procedure TOpenDirDialogEx.LinkClick(Sender: PObj);
var s, CurPath: KOLString;
begin
  s := IncludeTrailingPathDelimiter(
    PKOLChar( PControl( Sender ).CustomData ) );
  if DirectoryExists( s ) then
  begin
    CurPath := IncludeTrailingPathDelimiter(
      GetNodePath( DirTree.TVSelected ) );
    if StrEq( CurPath, s ) then
      Form.ModalResult := 1
    else Path := s;
  end;
end;

function TOpenDirDialogEx.LinkPresent(const s: KOLString): Boolean;
begin
  Result := (LinksList <> nil) and
            (LinksList.IndexOf_NoCase(
              IncludeTrailingPathDelimiter( s ) ) >= 0);
end;

procedure TOpenDirDialogEx.LinksAddClick(Sender: PObj);
var SL: PStrList;
    CurPath: KOLString;
begin
  CurPath := IncludeTrailingPathDelimiter(
    GetNodePath( DirTree.TVSelected ) );
  SL := NewStrList;
  if DirectoryExists( CurPath ) then
  begin
    SL.Add( CurPath );
    AddLinks( SL );
  end;
  SL.Free;
end;

procedure TOpenDirDialogEx.LinksDnClick(Sender: PControl; var Mouse: TMouseEventData);
begin
  LinksRollTimer.Tag := 1;
  LinksRollTimer.Enabled := TRUE;
end;

{procedure TOpenDirDialogEx.LinksPanelShowEvent(Sender: PObj);
begin
end;}

procedure TOpenDirDialogEx.LinksRollTimerTimer(Sender: PObj);
var NewTop, d: Integer;
begin
  d := Integer( LinksRollTimer.Tag );
  LinksRollTimer.Tag := Integer( LinksRollTimer.Tag ) + Sgn( d );
  NewTop := LinksTape.Top - (Sgn( d ) + d div 4) * 2;
  if (d > 0) and
     (NewTop + LinksTape.Height < LinksBox.Height) and
     (LinksTape.Top <= 0) then
  begin
    NewTop := LinksBox.Height - LinksTape.Height;
    if NewTop > 0 then
      NewTop := 0;
  end;
  if (d < 0) and (NewTop > 0) then NewTop := 0;
  if (NewTop = LinksTape.Top) or not Form.Visible then
    LinksRollTimer.Enabled := FALSE;
  LinksTape.Top := NewTop;
  LinksTape.Update;
end;

procedure TOpenDirDialogEx.LinksUpClick(Sender: PControl; var Mouse: TMouseEventData);
begin
  LinksRollTimer.Tag := DWORD( -1 );
  LinksRollTimer.Enabled := TRUE;
end;

procedure TOpenDirDialogEx.LinksUpDnStop(Sender: PControl; var Mouse: TMouseEventData);
begin
  LinksRollTimer.Enabled := FALSE;
end;
{$ENDIF DIRDLGEX_LINKSPANEL}

function TOpenDirDialogEx.RemoteIconSysIdx: Integer;
begin
  if FRemoteIconSysIdx = 0 then
  begin
    if DirectoryExists( '\\localhost\' ) then
      FRemoteIconSysIdx := DirIconSysIdxOffline( '\\localhost\' )
    else
      FRemoteIconSysIdx := DirIconSysIdxOffline( 'C:\' );
  end;
  Result := FRemoteIconSysIdx;
end;

{$IFDEF DIRDLGEX_LINKSPANEL}
procedure TOpenDirDialogEx.RemoveLink(const s: KOLString);
var i: Integer;
    Pn: PControl;
begin
  i := LinksList.IndexOf( IncludeTrailingPathDelimiter( s ) );
  if i >= 0 then
  begin
    Pn := Pointer( LinksList.Objects[ i ] );
    Pn.Free;
    LinksList.Delete( i );
  end;
  Global_Align( LinksTape );
  SetupLinksTapeHeight;
end;

procedure TOpenDirDialogEx.RemoveLinkClick(Sender: PMenu; Item: Integer);
var Pn: PControl;
    i: Integer;
begin
  Form.ModalResult := 0; //????
  Pn := Sender.CurCtl;
  if Pn <> nil then
  begin
    i := LinksList.IndexOfObj( Pn );
    if i >= 0 then
    begin
      RemoveLink( LinksList.Items[ i ] );
    end;
  end;
end;
{$ENDIF DIRDLGEX_LINKSPANEL}

procedure TOpenDirDialogEx.RescanDisks;
begin
  RescanNode( 0 );
end;

procedure TOpenDirDialogEx.RescanNode(node: Integer);
{ (����)������������ ������������� � �������� ����� node ������������ �����.
  ���� node = 0, �� ����������� ������ ������ �� ������ ����� ������.
}
var p, s: KOLString;
    i, j, n, d, m, ii: Integer;
    Find32: TWin32FindData;
    F: THandle;
    SL: PStrListEx;
{$ifndef wince}
    DL: PDirList;
    disk: Char;
{$endif wince}
    //test: KOLString;
begin
  if AppletTerminated or not AppletRunning then Exit;
  RescanningNode := TRUE;
  //Applet.ProcessMessages;
  TRY
    // ����������� ���� � ������������ ����� ��� ����� (�����, ���� ������� �������)
    p := '';
    if node <> 0 then
      p := IncludeTrailingPathDelimiter( GetNodePath( node ) );
    // � SL ������������� ������ �������� ���������� (��� ������)
    SL := NewStrListEx;
    TRY
      if node = 0 then
      begin
{$ifdef wince}
        SL.AddObject( 'My Device', 0 );
{$else}
        for disk := 'A' to 'Z' do
        begin
          case GetDriveType( PKOLChar( KOLString(disk) + ':\' ) ) of
          DRIVE_FIXED, DRIVE_RAMDISK:   ii := 0;
          DRIVE_REMOVABLE, DRIVE_CDROM: ii := 1;
          DRIVE_REMOTE:                 ii := 2;
          else ii := -1;
          end;
          if ii >= 0 then SL.AddObject( disk + ':', ii );
        end;
{$endif wince}
      end
        else
{$ifndef wince}
      if WinVer >= wvNT then // ������������ ����� ������� ������� - ��� NT/2K/XP
{$endif wince}
      begin
        _FindFirstFileEx;
        F := FFindFirstFileEx( PKOLChar( p + '*.*' ), FindExInfoStandard, @ Find32,
          FindExSearchLimitToDirectories, nil, 0 );
        if F <> INVALID_HANDLE_VALUE then
        begin
          TRY
            while TRUE do
            begin
              if Find32.dwFileAttributes and FILE_ATTRIBUTE_DIRECTORY <> 0 then
              if (Find32.cFileName <> KOLString( '.' )) and (Find32.cFileName <> '..') then
              if DoFilterAttrs( Find32.dwFileAttributes, Find32.{$ifdef wince}cFileName{$else}cAlternateFileName{$endif} ) then
                SL.Add( Find32.cFileName );
              if not FindNextFile( F, Find32 ) then break;
            end;
            SL.Sort( FALSE );
          FINALLY
            FindClose( F );
          END;
        end;
{$ifndef wince}
      end
        else
      begin
        DL := NewDirListEx( p, '*.*;*', FILE_ATTRIBUTE_DIRECTORY );
        TRY
          DL.Sort( [ ] );
          for i := 0 to DL.Count-1 do
            if DoFilterAttrs( DL.Items[ i ].dwFileAttributes,
                              DL.Items[ i ].cAlternateFileName ) then
              SL.Add( DL.Names[ i ] );
        FINALLY
          DL.Free;
        END;
{$endif wince}
      end;
      // ������ ��������������� ��� �������� ���� �������� node (��� �����
      // �� �������� ������)
      if node = 0 then
        n := DirTree.TVRoot
      else
        n := DirTree.TVItemChild[ node ];
      for i := 0 to SL.Count do
      begin
        //test := DirTree.TVItemText[ n ];
        //test := SL.Items[ i ];
        // ���� ��������� ��� � ������ ������ ��� �� ��� � ��������� ����
        while (n <> 0) and
              ( (i >= SL.Count) or
                (AnsiCompareStrNoCase( SL.Items[ i ], DirTree.TVItemText[ n ] ) > 0)
              ) do
        begin
        //test := DirTree.TVItemText[ n ];
        //test := SL.Items[ i ];
          // ���� ���������� � ������� ���� ����������� � ������, �� ��� �������
          // � �� ������� ������� �� ������ ����� �������� � ���������� ����
          d := n;
          s := DirTree.TVItemText[ n ];
          for j := 0 to SL.Count-1 do
            if AnsiCompareStrNoCase( SL.Items[ j ], s ) = 0 then
            begin
              d := 0; break; // ���� ����� � ������, �� �������
            end;
          if d = 0 then
            DirTree.TVItemData[ n ] := nil; // ����� ������ "�������� ���������"
          n := DirTree.TVItemNext[ n ];     // ������� � ���������� ���� ������
          if d <> 0 then  // ��������� ���� �������������� ����������
            //DirTree.TVDelete( d );
            DeleteNode( d );
        end;
        if i >= SL.Count then break;
        if (n <> 0) and
           (AnsiCompareStrNoCase( SL.Items[ i ], DirTree.TVItemText[ n ] ) = 0) then
        begin
          DirTree.TVItemData[ n ] := nil; // ����� ������ "�������� ���������"
          n := DirTree.TVItemNext[ n ];   // ������� � ���������� ���� ������
          continue;
        end;
        // �������� ������, ����� (�����) ��� ���������� ������ ��� ��� �
        // ��������� ���� (��� ���� ���������): ���� �������� ��� ����� ���� �����
        // (� ����� ������ �����):
        if n = 0 then
          m := DirTree.TVInsert( node, TVI_LAST, SL.Items[ i ] )
        else
        begin
          m := DirTree.TVItemPrevious[ n ];
          if m = 0 then
            m := DirTree.TVInsert( node, TVI_FIRST, SL.Items[ i ] )
          else
            m := DirTree.TVInsert( node, m, SL.Items[ i ] );
        end;
        if (SL.Objects[ i ] = 1) and FastScan then
          SL.Objects[ i ] := 2;
        CASE SL.Objects[ i ] OF
        0{,1}: ii := FileIconSystemIdx( p + SL.Items[ i ]{$ifndef wince} + '\' {$endif} );
{$ifndef wince}
        1: ii := DirIconSysIdxOffline( p + SL.Items[ i ] + '\' );
        {2:}else ii := RemoteIconSysIdx;
{$endif wince}
        END;
        DirTree.TVItemImage[ m ] := ii;
        DirTree.TVItemSelImg[ m ] := ii;
      end;
      if SL.Count = 0 then
        if node <> 0 then
          DirTree.TVItemHasChildren[ node ] := FALSE;
    FINALLY
      SL.Free;
    END;
  FINALLY
    RescanningNode := FALSE;
  END;
end;

procedure TOpenDirDialogEx.Rescantree;
var s, n, {$ifndef wince}d,{$endif} e: KOLString;
    node, parent, ii: Integer;
begin
  RescanningTree := TRUE;
  //DirTree.BeginUpdate;
  TRY
    RescanDisks;
    if (Path = '') or not DirectoryExists( Path ) then Path := GetWorkDir;
    node := DirTree.TVSelected;
    if (node = 0)
       // ������ �����: �������������� ���������� - �� �������� ���� � ����� (�����)
       // � ���������� ������ ���� ������ �� ������� ������
       OR
       (AnsiCompareStrNoCase( IncludeTrailingPathDelimiter(
                              GetNodePath( node ) ),
                              IncludeTrailingPathDelimiter( Path ) ) <> 0 )
       // ��� ������� ���� � ������ ��������� �� ����, ��� ������� � Path
    then
    begin
      s := Path;
{$ifdef wince}
      node:=DirTree.TVRoot;
      if s = '\' then
        RescanNode( node );
      DirTree.TVExpand( node, TVE_EXPAND );
{$else}
      node := 0;
{$endif wince}
      e := '';
      // ��������� ����, �� ������� �������� ��� ���� � ��������� �����
      while s <> '' do
      begin
        if AppletTerminated or not AppletRunning then Exit;
        n := Parse( s, '\/' );
        if n = '' then continue;
{$ifndef wince}
        if (n[ Length( n ) ] <> ':') and (pos( ':', n ) > 0) then
        begin
          d := Parse( n, ':' ) + ':';
          s := n + '\' + s;
          n := d;
        end;
{$endif wince}
        // n = ��������� ����, ������� ���� ���� ����� ����� ����� node,
        // ���� ��������� � ���� ������, ���� ��� ��� ���
        parent := node;
        if parent = 0 then
          node := DirTree.TVRoot
        else begin
{$ifndef wince}
          if (Length( n ) = 2) and (n[ 2 ] = ':') then
          begin
            if not IntIn( GetDriveType( PKOLChar( n + '\' ) ),
                    [ DRIVE_REMOVABLE, DRIVE_REMOTE, DRIVE_CDROM ] ) then
              RescanNode( parent );
          end
            else
{$endif wince}
              RescanNode( parent );
          node := DirTree.TVItemChild[ parent ];
        end;
        while node <> 0 do
        begin
          if AnsiCompareStrNoCase( DirTree.TVItemText[ node ], n ) = 0 then
            break;
          node := DirTree.TVItemNext[ node ];
        end;
        if node = 0 then
          node := DirTree.TVInsert( parent, TVI_LAST, n );
        if parent <> 0 then
          DirTree.TVExpand( parent, TVE_EXPAND );
        e := e + n + '\'; // �� ������ � e ������ ������ ����
        ii := FileIconSystemIdx( e );
        DirTree.TVItemImage[ node ] := ii;
        DirTree.TVItemSelImg[ node ] := ii;
      end;
      DirTree.TVSelected := node;
    end;
    if node <> 0 then
      DirTree.Perform( TVM_ENSUREVISIBLE, 0, node );
  FINALLY
    RescanningTree := FALSE;
    //DirTree.EndUpdate;
  END;
end;

{$IFDEF DIRDLGEX_LINKSPANEL}
procedure TOpenDirDialogEx.SetLinks(idx: Integer; const Value: KOLString);
var Bar, Pn: PControl;
    Bmp: PBitmap;
    Ico: PIcon;
    s: KOLString;
    H: Integer;
begin
  CreateLinksPanel;

  s := ExcludeTrailingPathDelimiter( Value );
  if LinksList = nil then
    LinksList := NewStrListEx;
  while LinksList.Count <= idx do
    LinksList.AddObject( '', 0 );
  if LinksList.Objects[ idx ] <> 0 then
  begin
    PObj( Pointer( LinksList.Objects[ idx ] ) ).Free;
  end;
  Bmp := NewDibBitmap( 32, 32, pf32bit );
  Bmp.Canvas.Brush.Color := clBtnFace;
  Bmp.Canvas.FillRect( Bmp.BoundsRect );
  if LinksImgList = nil then
  begin
    LinksImgList := NewImageList( LinksPanel );
    LinksImgList.LoadSystemIcons( FALSE );
  end;
  Ico := NewIcon;
  Ico.Handle := LinksImgList.ExtractIcon( FileIconSystemIdx( s ) );
  Ico.Draw( Bmp.Canvas.Handle, 0, 0 );
  Ico.Free;

  if LinksPopupMenu = nil then
  begin
    NewMenu( Form, 0, [ '' ], nil );
    LinksPopupMenu := NewMenu( Form, 0, [ '&Remove link' ], nil );
    LinksPopupMenu.AssignEvents( 0, [ RemoveLinkClick ] );
  end;

  H := 60;
  {$IFDEF DIRDLGEX_BIGGERPANEL}
  {$IFDEF USE_GRUSH}
    {$IFDEF TOGRUSH_OPTIONAL}
    if not NoGrush then
    {$ENDIF TOGRUSH_OPTIONAL}
      inc( H, 14 );
  {$ENDIF USE_GRUSH}
  {$ENDIF DIRDLGEX_BIGGERPANEL}
  NewPanelWithSingleButtonToolbar( LinksTape, LinksBox.Width,
    H, caTop, Bmp,
    ExtractFileName( s ), s, Pn, Bar, LinkClick, nil, nil, LinksPopupMenu );
  Pn.CreateWindow;

  LinksList.Items[ idx ] := IncludeTrailingPathDelimiter( Value );
  LinksList.Objects[ idx ] := DWORD( Pn );

  SetUpTaborders;
  SetupLinksTapeHeight;
end;

procedure TOpenDirDialogEx.SetLinksPanelOn(const Value: Boolean);
begin
  if LinksPanelOn = Value then Exit;
  GetDialogForm;
  if Assigned( LinksPanel ) then
    LinksPanel.Visible := Value;
  if not Value then LinksAdd.Visible := FALSE
  else
  begin
    CreateLinksPanel;
    LinksPanel.Visible := TRUE;
  end;
end;
{$ENDIF DIRDLGEX_LINKSPANEL}

procedure TOpenDirDialogEx.SetPath(const Value: KOLString);
begin
  FPath := Value;
  if FPath <> '' then
    FPath := IncludeTrailingPathDelimiter( FPath );
  if Assigned( DialogForm ) and (DialogForm.Visible) then
    Rescantree;
end;

{$IFDEF DIRDLGEX_LINKSPANEL}
procedure TOpenDirDialogEx.SetupLinksTapeHeight;
var H: Integer;
    Pn: PControl;
begin
  H := 0;
  if LinksList.Count > 0 then
  begin
    Pn := Pointer( LinksList.Objects[ LinksList.Count-1 ] );
    H := Pn.Top + Pn.Height;
  end;
  LinksTape.Height := H + 4;
end;

procedure TOpenDirDialogEx.SetUpTaborders;
var i: Integer;
    Pn: PControl;
begin
  for i := 0 to LinksCount-1 do
  begin
    Pn := Pointer( LinksList.Objects[ i ] );
    Pn.TabOrder := i;
  end;
end;
{$ENDIF DIRDLGEX_LINKSPANEL}

{$ifndef read_implementation}
end.
{$endif read_implementation}
