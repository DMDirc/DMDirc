const
  MAX_PROFILE_LEN = 80;
  MM_MAX_AXES_NAMELEN = 16;
  MM_MAX_NUMAXES = 16;
  HW_PROFILE_GUIDLEN = 39;

type
  HDEVNOTIFY = Pointer;
  PHDEVNOTIFY = ^HDEVNOTIFY;

  _GET_FILEEX_INFO_LEVELS = (GetFileExInfoStandard, GetFileExMaxInfoLevel);
  TGetFileExInfoLevels = _GET_FILEEX_INFO_LEVELS;
  GET_FILEEX_INFO_LEVELS = _GET_FILEEX_INFO_LEVELS;

  _FINDEX_INFO_LEVELS = (FindExInfoStandard, FindExInfoMaxInfoLevel);
  TFindexInfoLevels = _FINDEX_INFO_LEVELS;
  FINDEX_INFO_LEVELS = _FINDEX_INFO_LEVELS;

  _FINDEX_SEARCH_OPS = (FindExSearchNameMatch, FindExSearchLimitToDirectories,
    FindExSearchLimitToDevices, FindExSearchMaxSearchOp);
  TFindexSearchOps = _FINDEX_SEARCH_OPS;
  FINDEX_SEARCH_OPS = _FINDEX_SEARCH_OPS;

  TFNPropEnumProc = TFarProc;
  TFNPropEnumProcEx = TFarProc;
  TFNEditWordBreakProc = TFarProc;
  TFNNameEnumProc = TFarProc;
  TFNWinStaEnumProc = TFNNameEnumProc;
  TFNDeskTopEnumProc = TFNNameEnumProc;
  TFNWndProc = TFarProc;
  TFNDlgProc = TFarProc;
  TFNTimerProc = TFarProc;
  TFNGrayStringProc = TFarProc;
  TFNWndEnumProc = TFarProc;
  TFNSendAsyncProc = TFarProc;
  TFNDrawStateProc = TFarProc;
  TFNOldFontEnumProcW = TFarProc;
  TFNGObjEnumProc = TFarProc;
  TFNLineDDAProc = TFarProc;
  TFNFontEnumProcW = TFarProc;
  TFNProgressRoutine = TFarProc;
  TFNLocaleEnumProc = TFarProc;
  TFNCodepageEnumProc = TFarProc;
  TFNDateFmtEnumProc = TFarProc;
  TFNTimeFmtEnumProc = TFarProc;
  TFNCalInfoEnumProc = TFarProc;
  TFNICMEnumProc = TFarProc;

  MakeIntAtomW = PWideChar;
  
  PRecoveryAgentInformationW = ^TRecoveryAgentInformationW;
  _RECOVERY_AGENT_INFORMATIONW = record
    NextEntryOffset: DWORD;
    AgentNameLength: DWORD;
    AgentInformation: array[0..0] of CHAR;
  end;
  TRecoveryAgentInformationW = _RECOVERY_AGENT_INFORMATIONW;
  RECOVERY_AGENT_INFORMATIONW = _RECOVERY_AGENT_INFORMATIONW;
  
  PWin32FindDataW = ^TWin32FindDataW;
  _WIN32_FIND_DATAW = record
    dwFileAttributes: DWORD;
    ftCreationTime: TFileTime;
    ftLastAccessTime: TFileTime;
    ftLastWriteTime: TFileTime;
    nFileSizeHigh: DWORD;
    nFileSizeLow: DWORD;
    dwReserved0: DWORD;
    dwReserved1: DWORD;
    cFileName: array[0..MAX_PATH - 1] of WideChar;
    cAlternateFileName: array[0..13] of WideChar;
  end;
  TWin32FindDataW = _WIN32_FIND_DATAW;
  WIN32_FIND_DATAW = _WIN32_FIND_DATAW;

  PHWProfileInfoW = ^THWProfileInfoW;
  tagHW_PROFILE_INFOW = packed record
    dwDockInfo: DWORD;
    szHwProfileGuid: packed array[0..HW_PROFILE_GUIDLEN-1] of WideChar;
    szHwProfileName: packed array[0..MAX_PROFILE_LEN-1] of WideChar;
  end;
  THWProfileInfoW = tagHW_PROFILE_INFOW;
  HW_PROFILE_INFOW = tagHW_PROFILE_INFOW;

  PLogColorSpaceW = ^TLogColorSpaceW;
  tagLOGCOLORSPACEW = packed record
    lcsSignature: DWORD;
    lcsVersion: DWORD;
    lcsSize: DWORD;
    lcsCSType: LCSCSTYPE;
    lcsIntent: LCSGAMUTMATCH;
    lcsEndpoints: TCIEXYZTriple;
    lcsGammaRed: DWORD;
    lcsGammaGreen: DWORD;
    lcsGammaBlue: DWORD;
    lcsFilename: array[0..259] of WideChar;
  end;
  TLogColorSpaceW = tagLOGCOLORSPACEW;
  LOGCOLORSPACEW = tagLOGCOLORSPACEW;

  PNewTextMetricW = ^TNewTextMetricW;
  tagNEWTEXTMETRICW = record
    tmHeight: Longint;
    tmAscent: Longint;
    tmDescent: Longint;
    tmInternalLeading: Longint;
    tmExternalLeading: Longint;
    tmAveCharWidth: Longint;
    tmMaxCharWidth: Longint;
    tmWeight: Longint;
    tmOverhang: Longint;
    tmDigitizedAspectX: Longint;
    tmDigitizedAspectY: Longint;
    tmFirstChar: WideChar;
    tmLastChar: WideChar;
    tmDefaultChar: WideChar;
    tmBreakChar: WideChar;
    tmItalic: Byte;
    tmUnderlined: Byte;
    tmStruckOut: Byte;
    tmPitchAndFamily: Byte;
    tmCharSet: Byte;
    ntmFlags: DWORD;
    ntmSizeEM: UINT;
    ntmCellHeight: UINT;
    ntmAvgWidth: UINT;
  end;
  TNewTextMetricW = tagNEWTEXTMETRICW;
  NEWTEXTMETRICW = tagNEWTEXTMETRICW;

  PNewTextMetricExW = ^TNewTextMetricExW;
  tagNEWTEXTMETRICEXW = packed record
    ntmTm: TNewTextMetricW;
    ntmFontSig: TFontSignature;
  end;
  TNewTextMetricExW = tagNEWTEXTMETRICEXW;
  NEWTEXTMETRICEXW = tagNEWTEXTMETRICEXW;

  PEnumLogFontW = ^TEnumLogFontW;
  tagENUMLOGFONTW = packed record
    elfLogFont: TLogFontW;
    elfFullName: array[0..LF_FULLFACESIZE - 1] of WideChar;
    elfStyle: array[0..LF_FACESIZE - 1] of WideChar;
  end;
  TEnumLogFontW = tagENUMLOGFONTW;
  ENUMLOGFONTW = tagENUMLOGFONTW;

  PEnumLogFontExW = ^TEnumLogFontExW;
  tagENUMLOGFONTEXW = packed record
    elfLogFont: TLogFontW;
    elfFullName: array[0..LF_FULLFACESIZE - 1] of WideChar;
    elfStyle: array[0..LF_FACESIZE - 1] of WideChar;
    elfScript: array[0..LF_FACESIZE - 1] of WideChar;
  end;
  TEnumLogFontExW = tagENUMLOGFONTEXW;
  ENUMLOGFONTEXW = tagENUMLOGFONTEXW;

  PExtLogFontW = ^TExtLogFontW;
  tagEXTLOGFONTW = record
    elfLogFont: TLogFontW;
    elfFullName: array[0..LF_FULLFACESIZE - 1] of WideChar;
    elfStyle: array[0..LF_FACESIZE - 1] of WideChar;
    elfVersion: DWORD;     { 0 for the first release of NT }
    elfStyleSize: DWORD;
    elfMatch: DWORD;
    elfReserved: DWORD;
    elfVendorId: array[0..ELF_VENDOR_SIZE - 1] of Byte;
    elfCulture: DWORD;     { 0 for Latin }
    elfPanose: TPanose;
  end;
  TExtLogFontW = tagEXTLOGFONTW;
  EXTLOGFONTW = tagEXTLOGFONTW;

  PDisplayDeviceW = ^TDisplayDeviceW;
  _DISPLAY_DEVICEW = packed record
    cb: DWORD;
    DeviceName: array[0..31] of WideChar;
    DeviceString: array[0..127] of WideChar;
    StateFlags: DWORD;
  end;
  TDisplayDeviceW = _DISPLAY_DEVICEW;

  POutlineTextmetricW = ^TOutlineTextmetricW;
  _OUTLINETEXTMETRICW = record
    otmSize: UINT;
    otmTextMetrics: TTextMetricW;
    otmFiller: Byte;
    otmPanoseNumber: TPanose;
    otmfsSelection: UINT;
    otmfsType: UINT;
    otmsCharSlopeRise: Integer;
    otmsCharSlopeRun: Integer;
    otmItalicAngle: Integer;
    otmEMSquare: UINT;
    otmAscent: Integer;
    otmDescent: Integer;
    otmLineGap: UINT;
    otmsCapEmHeight: UINT;
    otmsXHeight: UINT;
    otmrcFontBox: TRect;
    otmMacAscent: Integer;
    otmMacDescent: Integer;
    otmMacLineGap: UINT;
    otmusMinimumPPEM: UINT;
    otmptSubscriptSize: TPoint;
    otmptSubscriptOffset: TPoint;
    otmptSuperscriptSize: TPoint;
    otmptSuperscriptOffset: TPoint;
    otmsStrikeoutSize: UINT;
    otmsStrikeoutPosition: Integer;
    otmsUnderscoreSize: Integer;
    otmsUnderscorePosition: Integer;
    otmpFamilyName: PWideChar;
    otmpFaceName: PWideChar;
    otmpStyleName: PWideChar;
    otmpFullName: PWideChar;
  end;
  TOutlineTextmetricW = _OUTLINETEXTMETRICW;
  OUTLINETEXTMETRICW = _OUTLINETEXTMETRICW;

  PPolyTextW = ^TPolyTextW;
  tagPOLYTEXTW = packed record
    x: Integer;
    y: Integer;
    n: UINT;
    PAnsiChar: PWideChar;
    uiFlags: UINT;
    rcl: TRect;
    pdx: PINT;
  end;
  TPolyTextW = tagPOLYTEXTW;
  POLYTEXTW = tagPOLYTEXTW;

  PGCPResultsW = ^TGCPResultsW;
  tagGCP_RESULTSW = packed record
    lStructSize: DWORD;
    lpOutString: PWideChar;
    lpOrder: PUINT;
    lpDx: PINT;
    lpCaretPos: PINT;
    lpClass: PWideChar;
    lpGlyphs: PUINT;
    nGlyphs: UINT;
    nMaxFit: Integer;
  end;
  TGCPResultsW = tagGCP_RESULTSW;
  GCP_RESULTSW = tagGCP_RESULTSW;

 PAxisInfoW = ^TAxisInfoW;
  tagAXISINFOW = packed record
    axMinValue: Longint;
    axMaxValue: Longint;
    axAxisName: array[0..MM_MAX_AXES_NAMELEN-1] of WideChar;
  end;
  TAxisInfoW = tagAXISINFOW;
  PAxesListW = ^TAxesListW;
  tagAXESLISTW = packed record
    axlReserved: DWORD;
    axlNumAxes: DWORD;
    axlAxisInfo: array[0..MM_MAX_NUMAXES-1] of TAxisInfoW;
  end;
  TAxesListW = tagAXESLISTW;

  PDesignVector = ^TDesignVector;
  tagDESIGNVECTOR = packed record
    dvReserved: DWORD;
    dvNumAxes: DWORD;
    dvValues: array[0..MM_MAX_NUMAXES-1] of Longint;
  end;
  TDesignVector = tagDESIGNVECTOR;

  PEnumLogFontExDVW = ^TEnumLogFontExDVW;
  tagENUMLOGFONTEXDVW = packed record
    elfEnumLogfontEx: TEnumLogFontExW;
    elfDesignVector: TDesignVector;
  end;
  TEnumLogFontExDVW = tagENUMLOGFONTEXDVW;

  PEnumTextMetricW = ^TEnumTextMetricW;
  tagENUMTEXTMETRICW = packed record
    etmNewTextMetricEx: TNewTextMetricExW;
    etmAxesList: TAxesListW;
  end;
  TEnumTextMetricW = tagENUMTEXTMETRICW;

  PDocInfoW = ^TDocInfoW;
  _DOCINFOW = packed record
    cbSize: Integer;
    lpszDocName: PWideChar;
    lpszOutput: PWideChar;
    lpszDatatype: PWideChar;
    fwType: DWORD;
  end;
  TDocInfoW = _DOCINFOW;
  DOCINFOW = _DOCINFOW;

  PCreateStructW = ^TCreateStructW;
  tagCREATESTRUCTW = packed record
    lpCreateParams: Pointer;
    hInstance: HINST;
    hMenu: HMENU;
    hwndParent: HWND;
    cy: Integer;
    cx: Integer;
    y: Integer;
    x: Integer;
    style: Longint;
    lpszName: PWideChar;
    lpszClass: PWideChar;
    dwExStyle: DWORD;
  end;
  TCreateStructW = tagCREATESTRUCTW;
  CREATESTRUCTW = tagCREATESTRUCTW;

  TPRMsgBoxCallback = procedure(var lpHelpInfo: THelpInfo);
  PMsgBoxParamsW = ^TMsgBoxParamsW;
  tagMSGBOXPARAMSW = packed record
    cbSize: UINT;
    hwndOwner: HWND;
    hInstance: HINST;
    lpszText: PWideChar;
    lpszCaption: PWideChar;
    dwStyle: DWORD;
    lpszIcon: PWideChar;
    dwContextHelpId: DWORD;
    lpfnMsgBoxCallback: TPRMsgBoxCallback;
    dwLanguageId: DWORD;
  end;
  TMsgBoxParamsW = tagMSGBOXPARAMSW;
  MSGBOXPARAMSW = tagMSGBOXPARAMSW;

  PMDICreateStructW = ^TMDICreateStructW;
  tagMDICREATESTRUCTW = packed record
    szClass: PWideChar;
    szTitle: PWideChar;
    hOwner: THandle;
    x: Integer;
    y: Integer;
    cx: Integer;
    cy: Integer;
    style: DWORD;
    lParam: LPARAM;  { app-defined stuff }
  end;
  TMDICreateStructW = tagMDICREATESTRUCTW;
  MDICREATESTRUCTW = tagMDICREATESTRUCTW;

  PMultiKeyHelpW = ^TMultiKeyHelpW;
  tagMULTIKEYHELPW = record
    mkSize: DWORD;
    mkKeylist: WideChar;
    szKeyphrase: array[0..0] of WideChar;
  end;
  TMultiKeyHelpW = tagMULTIKEYHELPW;
  MULTIKEYHELPW = tagMULTIKEYHELPW;

  PHelpWinInfoW = ^THelpWinInfoW;
  tagHELPWININFOW = record
    wStructSize: Integer;
    x: Integer;
    y: Integer;
    dx: Integer;
    dy: Integer;
    wMax: Integer;
    rgchMember: array[0..1] of WideChar;
  end;
  THelpWinInfoW = tagHELPWININFOW;
  HELPWININFOW = tagHELPWININFOW;

  PNonClientMetricsW = ^TNonClientMetricsW;
  tagNONCLIENTMETRICSW = packed record
    cbSize: UINT;
    iBorderWidth: Integer;
    iScrollWidth: Integer;
    iScrollHeight: Integer;
    iCaptionWidth: Integer;
    iCaptionHeight: Integer;
    lfCaptionFont: TLogFontW;
    iSmCaptionWidth: Integer;
    iSmCaptionHeight: Integer;
    lfSmCaptionFont: TLogFontW;
    iMenuWidth: Integer;
    iMenuHeight: Integer;
    lfMenuFont: TLogFontW;
    lfStatusFont: TLogFontW;
    lfMessageFont: TLogFontW;
  end;
  TNonClientMetricsW = tagNONCLIENTMETRICSW;
  NONCLIENTMETRICSW = tagNONCLIENTMETRICSW;

  PIconMetricsW = ^TIconMetricsW;
  tagICONMETRICSW = packed record
    cbSize: UINT;
    iHorzSpacing: Integer;
    iVertSpacing: Integer;
    iTitleWrap: Integer;
    lfFont: TLogFontW;
  end;
  TIconMetricsW = tagICONMETRICSW;
  ICONMETRICSW = tagICONMETRICSW;

  PSerialKeysW = ^TSerialKeysW;
  tagSERIALKEYSW = packed record
    cbSize: UINT;
    dwFlags: DWORD;
    lpszActivePort: PWideChar;
    lpszPort: PWideChar;
    iBaudRate: UINT;
    iPortState: UINT;
    iActive: UINT;
  end;
  TSerialKeysW = tagSERIALKEYSW;
  SERIALKEYSW = tagSERIALKEYSW;

  PHighContrastW = ^THighContrastW;
  tagHIGHCONTRASTW = packed record
    cbSize: UINT;
    dwFlags: DWORD;
    lpszDefaultScheme: PWideChar;
  end;
  THighContrastW = tagHIGHCONTRASTW;
  HIGHCONTRASTW = tagHIGHCONTRASTW;

  PSoundsEntryW = ^TSoundsEntryW;
  tagSOUNDSENTRYW = packed record
    cbSize: UINT;
    dwFlags: DWORD;
    iFSTextEffect: DWORD;
    iFSTextEffectMSec: DWORD;
    iFSTextEffectColorBits: DWORD;
    iFSGrafEffect: DWORD;
    iFSGrafEffectMSec: DWORD;
    iFSGrafEffectColor: DWORD;
    iWindowsEffect: DWORD;
    iWindowsEffectMSec: DWORD;
    lpszWindowsEffectDLL: PWideChar;
    iWindowsEffectOrdinal: DWORD;
  end;
  TSoundsEntryW = tagSOUNDSENTRYW;
  SOUNDSENTRYW = tagSOUNDSENTRYW;

  PNumberFmtW = ^TNumberFmtW;
  _numberfmtW = packed record
    NumDigits: UINT;        { number of decimal digits }
    LeadingZero: UINT;      { if leading zero in decimal fields }
    Grouping: UINT;         { group size left of decimal }
    lpDecimalSep: PWideChar;   { ptr to decimal separator string }
    lpThousandSep: PWideChar;  { ptr to thousand separator string }
    NegativeOrder: UINT;    { negative number ordering }
  end;
  TNumberFmtW = _numberfmtW;
  NUMBERFMTW = _numberfmtW;

  PCurrencyFmtW = ^TCurrencyFmtW;
  _currencyfmtW = packed record
    NumDigits: UINT;           { number of decimal digits }
    LeadingZero: UINT;         { if leading zero in decimal fields }
    Grouping: UINT;            { group size left of decimal }
    lpDecimalSep: PWideChar;      { ptr to decimal separator string }
    lpThousandSep: PWideChar;     { ptr to thousand separator string }
    NegativeOrder: UINT;       { negative currency ordering }
    PositiveOrder: UINT;       { positive currency ordering }
    lpCurrencySymbol: PWideChar;  { ptr to currency symbol string }
  end;
  TCurrencyFmtW = _currencyfmtW;
  CURRENCYFMTW = _currencyfmtW;

  PPValueW = ^TPValueW;
  pvalueW = packed record
    pv_valuename: PWideChar;           { The value name pointer }
    pv_valuelen: BOOL;
    pv_value_context: Pointer;
    pv_type: DWORD;
  end;
  TPValueW = pvalueW;

  PValueEntW = ^TValueEntW;
  value_entW = packed record
    ve_valuename: PWideChar;
    ve_valuelen: DWORD;
    ve_valueptr: DWORD;
    ve_type: DWORD;
  end;
  TValueEntW = value_entW;
  VALENTW = value_entW;

  PNetResourceW = ^TNetResourceW;
  _NETRESOURCEW = packed record
    dwScope: DWORD;
    dwType: DWORD;
    dwDisplayType: DWORD;
    dwUsage: DWORD;
    lpLocalName: PWideChar;
    lpRemoteName: PWideChar;
    lpComment: PWideChar;
    lpProvider: PWideChar;
  end;
  TNetResourceW = _NETRESOURCEW;
  NETRESOURCEW = _NETRESOURCEW;

  PDiscDlgStructW = ^TDiscDlgStructW;
  _DISCDLGSTRUCTW = packed record
    cbStructure: DWORD;       { size of this structure in bytes }
    hwndOwner: HWND;          { owner window for the dialog }
    lpLocalName: PWideChar;       { local device name }
    lpRemoteName: PWideChar;      { network resource name }
    dwFlags: DWORD;
  end;
  TDiscDlgStructW = _DISCDLGSTRUCTW;
  DISCDLGSTRUCTW = _DISCDLGSTRUCTW;

  PUniversalNameInfoW = ^TUniversalNameInfoW;
  _UNIVERSAL_NAME_INFOW = packed record
    lpUniversalName: PWideChar;
  end;
  TUniversalNameInfoW = _UNIVERSAL_NAME_INFOW;
  UNIVERSAL_NAME_INFOW = _UNIVERSAL_NAME_INFOW;

  PRemoteNameInfoW = ^TRemoteNameInfoW;
  _REMOTE_NAME_INFOW = packed record
    lpUniversalName: PWideChar;
    lpConnectionName: PWideChar;
    lpRemainingPath: PWideChar;
  end;
  TRemoteNameInfoW = _REMOTE_NAME_INFOW;
  REMOTE_NAME_INFOW = _REMOTE_NAME_INFOW;

  PAltTabInfo = ^TAltTabInfo;
  tagALTTABINFO = packed record
    cbSize: DWORD;
    cItems: Integer;
    cColumns: Integer;
    cRows: Integer;
    iColFocus: Integer;
    iRowFocus: Integer;
    cxItem: Integer;
    cyItem: Integer;
    ptStart: TPoint;
  end;
  TAltTabInfo = tagALTTABINFO;
  
  PMenuItemInfoW = ^TMenuItemInfoW;
  tagMENUITEMINFOW = packed record
    cbSize: UINT;
    fMask: UINT;
    fType: UINT;             { used if MIIM_TYPE}
    fState: UINT;            { used if MIIM_STATE}
    wID: UINT;               { used if MIIM_ID}
    hSubMenu: HMENU;         { used if MIIM_SUBMENU}
    hbmpChecked: HBITMAP;    { used if MIIM_CHECKMARKS}
    hbmpUnchecked: HBITMAP;  { used if MIIM_CHECKMARKS}
    dwItemData: DWORD;       { used if MIIM_DATA}
    dwTypeData: PWideChar;      { used if MIIM_TYPE}
    cch: UINT;               { used if MIIM_TYPE}
    hbmpItem: HBITMAP;       { used if MIIM_BITMAP}
  end;
  TMenuItemInfoW = tagMENUITEMINFOW;
  MENUITEMINFOW = tagMENUITEMINFOW;

  PMenuItemInfo = PMenuItemInfoW;
  TMenuItemInfo = TMenuItemInfoW;
  MENUITEMINFO = MENUITEMINFOW;
