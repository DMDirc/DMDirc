{ Copyright (C) 2005 Bas Steendijk and Peter Green
  For conditions of distribution and use, see copyright notice in zlib_license.txt
  Which is included in the package
  ----------------------------------------------------------------------------- }

unit fastmd5;

{
pascal implementation of MD5

written by Bas Steendijk - steendijk@xs4all.nl

based on RFC1321 - The MD5 Message-Digest Algorithm

optimized for speed: saved on copying and sub calls in the core routine

verified on:
- Borland Delphi 3
- Borland Turbo Pascal 7
- Free Pascal 1.0.6 for i386 (on *nix)
- various other versions of freepascal on windows and linux i386
- various other versions of delphi
- free pascal 1.9.5 on powerpc darwin

this unit is endian portable but is likely to be significantly slower on big endian systems
}

{$Q-,R-}

interface





type
  Tmd5=array[0..15] of byte;

{$i uint32.inc}

type
  dvar=array[0..0] of byte;
  Tmd5state=record
    buf:array[0..63] of byte;
    H:array[0..3] of uint32;
    msglen:longint;
    msglenhi:longint;
  end;

procedure md5processblock(var h:array of uint32;const data);

procedure md5init(var state:tmd5state);
procedure md5process(var state:tmd5state;const data;len:longint);
procedure md5finish(var state:tmd5state;var result);

procedure getmd5(const data;len:longint;var result);

function md5tostr(const md5:tmd5):string;

implementation

function inttohex(val,bits:integer):string;
const
  hexchar:array[0..15] of char='0123456789abcdef';
begin
  inttohex := hexchar[val shr 4]+hexchar[val and $f];
end;

{$ifdef cpu386}
function rol(w,bits:uint32): uint32; assembler;
asm
  {cpu386 is not defined on freepascal. but fpc assembler is incompatible, uses different code}
  {inline($89/$d1/$d3/$c0);}
  mov   ecx,edx
  rol   eax,cl
end;
{$else}
function rol(w,bits:uint32):uint32;
begin
  rol := (w shl bits) or (w shr (32-bits));
end;
{$endif}


{function swapbytes(invalue:uint32):uint32;
var
  inbytes  : array[0..3] of byte absolute invalue;
  outbytes : array[0..3] of byte absolute result;


begin
  outbytes[0] := inbytes[3];
  outbytes[1] := inbytes[2];
  outbytes[2] := inbytes[1];
  outbytes[3] := inbytes[0];
end;}

procedure md5processblock(var h:array of uint32;const data);
const
  S11=7;  S12=12;  S13=17;  S14=22;
  S21=5;  S22=9;   S23=14;  S24=20;
  S31=4;  S32=11;  S33=16;  S34=23;
  S41=6;  S42=10;  S43=15;  S44=21;

var
  A,B,C,D:uint32;
  w:array[0..63] of byte absolute data;
  x:array[0..15] of uint32 {$ifndef ENDIAN_BIG} absolute data{$endif} ;
  y:array[0..63] of byte absolute x;
  {$ifdef ENDIAN_BIG}counter : integer;{$endif}
begin
  A := h[0];
  B := h[1];
  C := h[2];
  D := h[3];
  {$ifdef ENDIAN_BIG}
    for counter := 0 to 63 do begin
      y[counter] := w[counter xor 3];
    end;
  {$endif}
  a := rol(a + ((b and c) or ((not b) and d)) + x[ 0] + $d76aa478, S11) + b;
  d := rol(d + ((a and b) or ((not a) and c)) + x[ 1] + $e8c7b756, S12) + a;
  c := rol(c + ((d and a) or ((not d) and b)) + x[ 2] + $242070db, S13) + d;
  b := rol(b + ((c and d) or ((not c) and a)) + x[ 3] + $c1bdceee, S14) + c;
  a := rol(a + ((b and c) or ((not b) and d)) + x[ 4] + $f57c0faf, S11) + b;
  d := rol(d + ((a and b) or ((not a) and c)) + x[ 5] + $4787c62a, S12) + a;
  c := rol(c + ((d and a) or ((not d) and b)) + x[ 6] + $a8304613, S13) + d;
  b := rol(b + ((c and d) or ((not c) and a)) + x[ 7] + $fd469501, S14) + c;
  a := rol(a + ((b and c) or ((not b) and d)) + x[ 8] + $698098d8, S11) + b;
  d := rol(d + ((a and b) or ((not a) and c)) + x[ 9] + $8b44f7af, S12) + a;
  c := rol(c + ((d and a) or ((not d) and b)) + x[10] + $ffff5bb1, S13) + d;
  b := rol(b + ((c and d) or ((not c) and a)) + x[11] + $895cd7be, S14) + c;
  a := rol(a + ((b and c) or ((not b) and d)) + x[12] + $6b901122, S11) + b;
  d := rol(d + ((a and b) or ((not a) and c)) + x[13] + $fd987193, S12) + a;
  c := rol(c + ((d and a) or ((not d) and b)) + x[14] + $a679438e, S13) + d;
  b := rol(b + ((c and d) or ((not c) and a)) + x[15] + $49b40821, S14) + c;

  a := rol(a + ((b and d) or (c and (not d))) + x[ 1] + $f61e2562, S21) + b;
  d := rol(d + ((a and c) or (b and (not c))) + x[ 6] + $c040b340, S22) + a;
  c := rol(c + ((d and b) or (a and (not b))) + x[11] + $265e5a51, S23) + d;
  b := rol(b + ((c and a) or (d and (not a))) + x[ 0] + $e9b6c7aa, S24) + c;
  a := rol(a + ((b and d) or (c and (not d))) + x[ 5] + $d62f105d, S21) + b;
  d := rol(d + ((a and c) or (b and (not c))) + x[10] + $02441453, S22) + a;
  c := rol(c + ((d and b) or (a and (not b))) + x[15] + $d8a1e681, S23) + d;
  b := rol(b + ((c and a) or (d and (not a))) + x[ 4] + $e7d3fbc8, S24) + c;
  a := rol(a + ((b and d) or (c and (not d))) + x[ 9] + $21e1cde6, S21) + b;
  d := rol(d + ((a and c) or (b and (not c))) + x[14] + $c33707d6, S22) + a;
  c := rol(c + ((d and b) or (a and (not b))) + x[ 3] + $f4d50d87, S23) + d;
  b := rol(b + ((c and a) or (d and (not a))) + x[ 8] + $455a14ed, S24) + c;
  a := rol(a + ((b and d) or (c and (not d))) + x[13] + $a9e3e905, S21) + b;
  d := rol(d + ((a and c) or (b and (not c))) + x[ 2] + $fcefa3f8, S22) + a;
  c := rol(c + ((d and b) or (a and (not b))) + x[ 7] + $676f02d9, S23) + d;
  b := rol(b + ((c and a) or (d and (not a))) + x[12] + $8d2a4c8a, S24) + c;

  a := rol(a + (b xor c xor d) + x[ 5] + $fffa3942, S31) + b;
  d := rol(d + (a xor b xor c) + x[ 8] + $8771f681, S32) + a;
  c := rol(c + (d xor a xor b) + x[11] + $6d9d6122, S33) + d;
  b := rol(b + (c xor d xor a) + x[14] + $fde5380c, S34) + c;
  a := rol(a + (b xor c xor d) + x[ 1] + $a4beea44, S31) + b;
  d := rol(d + (a xor b xor c) + x[ 4] + $4bdecfa9, S32) + a;
  c := rol(c + (d xor a xor b) + x[ 7] + $f6bb4b60, S33) + d;
  b := rol(b + (c xor d xor a) + x[10] + $bebfbc70, S34) + c;
  a := rol(a + (b xor c xor d) + x[13] + $289b7ec6, S31) + b;
  d := rol(d + (a xor b xor c) + x[ 0] + $eaa127fa, S32) + a;
  c := rol(c + (d xor a xor b) + x[ 3] + $d4ef3085, S33) + d;
  b := rol(b + (c xor d xor a) + x[ 6] + $04881d05, S34) + c;
  a := rol(a + (b xor c xor d) + x[ 9] + $d9d4d039, S31) + b;
  d := rol(d + (a xor b xor c) + x[12] + $e6db99e5, S32) + a;
  c := rol(c + (d xor a xor b) + x[15] + $1fa27cf8, S33) + d;
  b := rol(b + (c xor d xor a) + x[ 2] + $c4ac5665, S34) + c;

  a := rol(a + (c xor (b or (not d))) + x[ 0] + $f4292244, S41) + b;
  d := rol(d + (b xor (a or (not c))) + x[ 7] + $432aff97, S42) + a;
  c := rol(c + (a xor (d or (not b))) + x[14] + $ab9423a7, S43) + d;
  b := rol(b + (d xor (c or (not a))) + x[ 5] + $fc93a039, S44) + c;
  a := rol(a + (c xor (b or (not d))) + x[12] + $655b59c3, S41) + b;
  d := rol(d + (b xor (a or (not c))) + x[ 3] + $8f0ccc92, S42) + a;
  c := rol(c + (a xor (d or (not b))) + x[10] + $ffeff47d, S43) + d;
  b := rol(b + (d xor (c or (not a))) + x[ 1] + $85845dd1, S44) + c;
  a := rol(a + (c xor (b or (not d))) + x[ 8] + $6fa87e4f, S41) + b;
  d := rol(d + (b xor (a or (not c))) + x[15] + $fe2ce6e0, S42) + a;
  c := rol(c + (a xor (d or (not b))) + x[ 6] + $a3014314, S43) + d;
  b := rol(b + (d xor (c or (not a))) + x[13] + $4e0811a1, S44) + c;
  a := rol(a + (c xor (b or (not d))) + x[ 4] + $f7537e82, S41) + b;
  d := rol(d + (b xor (a or (not c))) + x[11] + $bd3af235, S42) + a;
  c := rol(c + (a xor (d or (not b))) + x[ 2] + $2ad7d2bb, S43) + d;
  b := rol(b + (d xor (c or (not a))) + x[ 9] + $eb86d391, S44) + c;

  inc(h[0],A);
  inc(h[1],B);
  inc(h[2],C);
  inc(h[3],D);
end;

procedure md5init(var state:tmd5state);
begin
  state.h[0] := $67452301;
  state.h[1] := $EFCDAB89;
  state.h[2] := $98BADCFE;
  state.h[3] := $10325476;
  state.msglen := 0;
  state.msglenhi := 0;
end;

procedure md5process(var state:tmd5state;const data;len:longint);
var
  a,b:longint;
  ofs:longint;
  p:dvar absolute data;
begin
  b := state.msglen and 63;

  inc(state.msglen,len);
  while (state.msglen > $20000000) do begin
    dec(state.msglen,$20000000);
    inc(state.msglenhi);
  end;
  ofs := 0;
  if b > 0 then begin
    a := 64-b;
    if a > len then a := len;
    move(p[0],state.buf[b],a);
    inc(ofs,a);
    dec(len,a);
    if b+a = 64 then md5processblock(state.h,state.buf);
    if len = 0 then exit;
  end;
  while len >= 64 do begin
    md5processblock(state.h,p[ofs]);
    inc(ofs,64);
    dec(len,64);
  end;
  if len > 0 then move(p[ofs],state.buf[0],len);
end;

procedure md5finish(var state:tmd5state;var result);
var
  b       :integer;
  {$ifdef endian_big}
    h       :tmd5 absolute state.h;
    r       :tmd5 absolute result;
    counter :integer ;
  {$endif}
begin
  b := state.msglen and 63;
  state.buf[b] := $80;
  if b >= 56 then begin
    {-- for a := b+1 to 63 do state.buf[a] := 0; }
    fillchar(state.buf[b+1],63-b,0);
    md5processblock(state.h,state.buf);
    fillchar(state.buf,56,0);
  end else begin
    {-- for a := b+1 to 55 do state.buf[a] := 0; }
    fillchar(state.buf[b+1],55-b,0);
  end;
  state.msglen := state.msglen shl 3;

  state.buf[56] := state.msglen;
  state.buf[57] := state.msglen shr 8;
  state.buf[58] := state.msglen shr 16;
  state.buf[59] := state.msglen shr 24;
  state.buf[60] := state.msglenhi;
  state.buf[61] := state.msglenhi shr 8;
  state.buf[62] := state.msglenhi shr 16;
  state.buf[63] := state.msglenhi shr 24;

  md5processblock(state.h,state.buf);
  {$ifdef ENDIAN_BIG}
    for counter := 0 to 15 do begin
      r[counter] := h[counter xor 3];
    end;
  {$else} 
    move(state.h,result,16);
  {$endif}
  fillchar(state,sizeof(state),0);
end;

procedure getmd5(const data;len:longint;var result);
var
  t:tmd5state;
begin
  md5init(t);
  md5process(t,data,len);
  md5finish(t,result);
end;

function md5tostr(const md5:tmd5):string;
var
  a:integer;
  s:string;
begin
  s := '';
  for a := 0 to 15 do s := s + inttohex(md5[a],2);
  md5tostr := s;
end;

end.
