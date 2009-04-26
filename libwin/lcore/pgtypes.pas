{io core originally for linux bworld}

{ Copyright (C) 2005 Bas Steendijk and Peter Green
  For conditions of distribution and use, see copyright notice in zlib_license.txt
  which is included in the package
  ----------------------------------------------------------------------------- }

unit pgtypes;
interface
  type
    {$ifdef cpu386}{$define i386}{$endif}
    {$ifdef i386}
      taddrint=longint;
    {$else}
      taddrint=sizeint;
    {$endif}
    paddrint=^taddrint;

implementation
end.
