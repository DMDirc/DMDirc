{ Copyright (C) 2005 Bas Steendijk and Peter Green
  For conditions of distribution and use, see copyright notice in zlib_license.txt
  which is included in the package
  ----------------------------------------------------------------------------- }
type
  {delphi 3 and before do not have a 32 bits unsigned integer type,
  but longint has the correct behavior - it doesn't on newer delphi versions}
  {$ifndef fpc}
    {$ifdef ver70}{$define pred4}{$endif} {tp7}
    {$ifdef ver80}{$define pred4}{$endif} {delphi 1}
    {$ifdef ver90}{$define pred4}{$endif} {delphi 2}
    {$ifdef ver100}{$define pred4}{$endif} {delphi 3}
  {$endif}
  uint32={$ifdef pred4}longint{$else}longword{$endif};
