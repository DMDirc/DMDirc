{ Copyright (C) 2005 Bas Steendijk and Peter Green
  For conditions of distribution and use, see copyright notice in zlib_license.txt
  which is included in the package
  ----------------------------------------------------------------------------- }
unit binipstuff;

interface

{$include lcoreconfig.inc}

{$ifndef win32}
{$ifdef ipv6}
uses sockets;
{$endif}
{$endif}

{$ifdef fpc}
  {$mode delphi}
{$endif}
{$ifdef cpu386}{$define i386}{$endif}
{$ifdef i386}{$define ENDIAN_LITTLE}{$endif}

{$include uint32.inc}

const
  hexchars:array[0..15] of char='0123456789abcdef';
  AF_INET=2;
  {$ifdef win32}
    AF_INET6=23;
  {$else}
    AF_INET6=10;
  {$endif}

type
  {$ifdef ipv6}
    
    {$ifdef win32}
      {$define want_Tin6_addr}
    {$endif}
    {$ifdef ver1_0}
      {$define want_Tin6_addr}
    {$endif}
    {$ifdef want_Tin6_addr}
      Tin6_addr = packed record
        case byte of
          0: (u6_addr8  : array[0..15] of byte);
          1: (u6_addr16 : array[0..7] of Word);
          2: (u6_addr32 : array[0..3] of uint32);
          3: (s6_addr8  : array[0..15] of shortint);
          4: (s6_addr   : array[0..15] of shortint);
          5: (s6_addr16 : array[0..7] of smallint);
          6: (s6_addr32 : array[0..3] of LongInt);
      end;
    {$endif}
  {$endif}

  tbinip=record
    family:integer;
    {$ifdef ipv6}
      case integer of
        0: (ip:longint);
        1: (ip6:tin6_addr);
    {$else}
      ip:longint;
    {$endif}
  end;

  {$ifdef win32}
    TInetSockAddr = packed Record
      family:Word;
      port  :Word;
      addr  :uint32;
      pad   :array [1..8] of byte;
    end;
    {$ifdef ipv6}

      TInetSockAddr6 = packed record
        sin6_family: word;
        sin6_port: word;
        sin6_flowinfo: uint32;
        sin6_addr: tin6_addr;
        sin6_scope_id: uint32;
      end;
    {$endif}
  {$endif}



  {$ifdef ipv6}
    {$ifdef ver1_0}
      cuint16=word;
      cuint32=dword;
      sa_family_t=word;
      TInetSockAddr6 = packed record
        sin6_family: word;
        sin6_port: word;
        sin6_flowinfo: uint32;
        sin6_addr: tin6_addr;
        sin6_scope_id: uint32;
      end;
    {$endif}
  {$endif}
  TinetSockAddrv = packed record
    case integer of
      0: (InAddr:TInetSockAddr);
      {$ifdef ipv6}
      1: (InAddr6:TInetSockAddr6);
      {$endif}
  end;
  Pinetsockaddrv = ^Tinetsockaddrv;

  type
    tsockaddrin=TInetSockAddr;



{
bin IP list code, by beware
while this is really just a string, on the interface side it must be treated
as an opaque var which is passed as "var" when it needs to be modified}

  tbiniplist=string;

function biniplist_new:tbiniplist;
procedure biniplist_add(var l:tbiniplist;ip:tbinip);
function biniplist_getcount(const l:tbiniplist):integer;
function biniplist_get(const l:tbiniplist;index:integer):tbinip;
procedure biniplist_set(var l:tbiniplist;index:integer;ip:tbinip);
procedure biniplist_setcount(var l:tbiniplist;newlen:integer);
procedure biniplist_free(var l:tbiniplist);
procedure biniplist_addlist(var l:tbiniplist;const l2:tbiniplist);
function biniplist_tostr(const l:tbiniplist):string;
function isbiniplist(const l:tbiniplist):boolean;

function htons(w:word):word;
function htonl(i:uint32):uint32;

function ipstrtobin(const s:string;var binip:tbinip):boolean;
function ipstrtobinf(const s:string):tbinip;
function ipbintostr(const binip:tbinip):string;
{$ifdef ipv6}
function ip6bintostr(const bin:tin6_addr):string;
function ip6strtobin(const s:string;var bin:tin6_addr):boolean;
{$endif}

function comparebinip(const ip1,ip2:tbinip):boolean;
procedure maskbits(var binip:tbinip;bits:integer);
function comparebinipmask(ip1,ip2:tbinip;bits:integer):boolean;

procedure addipsoffamily(var l:tbiniplist;const l2:tbiniplist;family:integer);

{deprecated}
function longip(s:string):longint;

function needconverttov4(const ip:tbinip):boolean;
procedure converttov4(var ip:tbinip);

function inaddrvtobinip(inaddrv:tinetsockaddrv):tbinip;
function makeinaddrv(addr:tbinip;port:string;var inaddr:tinetsockaddrv):integer;
function inaddrsize(inaddr:tinetsockaddrv):integer;

implementation

uses sysutils;

function htons(w:word):word;
begin
  {$ifdef ENDIAN_LITTLE}
  result := ((w and $ff00) shr 8) or ((w and $ff) shl 8);
  {$else}
  result := w;
  {$endif}
end;

function htonl(i:uint32):uint32;
begin
  {$ifdef ENDIAN_LITTLE}
  result := (i shr 24) or (i shr 8 and $ff00) or (i shl 8 and $ff0000) or (i shl 24 and $ff000000);
  {$else}
  result := i;
  {$endif}
end;


function inaddrvtobinip(inaddrv:tinetsockaddrv):tbinip;
begin
  result.family := inaddrv.inaddr.family;
  if result.family = AF_INET then result.ip := inaddrv.inaddr.addr;
  {$ifdef ipv6}
  if result.family = AF_INET6 then result.ip6 := inaddrv.inaddr6.sin6_addr;
  {$endif}
end;

function makeinaddrv(addr:tbinip;port:string;var inaddr:tinetsockaddrv):integer;
begin
  result := 0;
{  biniptemp := forwardlookup(addr,10);}
  fillchar(inaddr,sizeof(inaddr),0);
  //writeln('converted address '+addr+' to binip '+ipbintostr(biniptemp));
  if addr.family = AF_INET then begin
    inAddr.InAddr.family:=AF_INET;
    inAddr.InAddr.port:=htons(strtointdef(port,0));
    inAddr.InAddr.addr:=addr.ip;
    result := sizeof(tinetsockaddr);
  end else
  {$ifdef ipv6}
  if addr.family = AF_INET6 then begin
    inAddr.InAddr6.sin6_family:=AF_INET6;
    inAddr.InAddr6.sin6_port:=htons(strtointdef(port,0));
    inAddr.InAddr6.sin6_addr:=addr.ip6;
    result := sizeof(tinetsockaddr6);
  end;
  {$endif}
end;

function inaddrsize(inaddr:tinetsockaddrv):integer;
begin
  {$ifdef ipv6}
  if inaddr.inaddr.family = AF_INET6 then result := sizeof(tinetsockaddr6) else
  {$endif}
  result := sizeof(tinetsockaddr);
end;

{internal}
{converts dotted v4 IP to longint. returns host endian order}
function longip(s:string):longint;
var
  l:longint;
  a,b:integer;
function convertbyte(const s:string):integer;
begin
  result := strtointdef(s,-1);
  if result < 0 then begin
    result := -1;
    exit;
  end;
  if result > 255 then begin
    result := -1;
    exit;
  end;
  {01 exception}
  if (result <> 0) and (s[1] = '0') then begin
    result := -1;
    exit;
  end;
  {+1 exception}
  if not (s[1] in ['0'..'9']) then begin
    result := -1;
    exit
  end;
end;

begin
  result := 0;
  a := pos('.',s);
  if a = 0 then exit;
  b := convertbyte(copy(s,1,a-1));if (b < 0) then exit;
  l := b shl 24;
  s := copy(s,a+1,256);
  a := pos('.',s);
  if a = 0 then exit;
  b := convertbyte(copy(s,1,a-1));if (b < 0) then exit;
  l := l or b shl 16;
  s := copy(s,a+1,256);
  a := pos('.',s);
  if a = 0 then exit;
  b := convertbyte(copy(s,1,a-1));if (b < 0) then exit;
  l := l or b shl 8;
  s := copy(s,a+1,256);
  b := convertbyte(copy(s,1,256));if (b < 0) then exit;
  l := l or b;
  result := l;
end;


function ipstrtobinf;
begin
  ipstrtobin(s,result);
end;

function ipstrtobin(const s:string;var binip:tbinip):boolean;
begin
  binip.family := 0;
  result := false;
  {$ifdef ipv6}
  if pos(':',s) <> 0 then begin
    {try ipv6. use builtin routine}
    result := ip6strtobin(s,binip.ip6);
    if result then binip.family := AF_INET6;
    exit;
  end;
  {$endif}

  {try v4}
  binip.ip := htonl(longip(s));
  if (binip.ip <> 0) or (s = '0.0.0.0') then begin
    result := true;
    binip.family := AF_INET;
    exit;
  end;
end;

function ipbintostr(const binip:tbinip):string;
var
  a:integer;
begin
  result := '';
  {$ifdef ipv6}
  if binip.family = AF_INET6 then begin
    result := ip6bintostr(binip.ip6);
  end else
  {$endif}
  if binip.family = AF_INET then begin
    a := htonl(binip.ip);
    result := inttostr(a shr 24)+'.'+inttostr((a shr 16) and $ff)+'.'+inttostr((a shr 8) and $ff)+'.'+inttostr(a and $ff);
  end;
end;


{------------------------------------------------------------------------------}

{$ifdef ipv6}

{
IPv6 address binary to/from string conversion routines
written by beware (steendijk at xs4all dot nl)

- implementation does not depend on other ipv6 code such as the tin6_addr type,
  the parameter can also be untyped.
- it is host endian neutral - binary format is aways network order
- it supports compression of zeroes
- it supports ::ffff:192.168.12.34 style addresses
- they are made to do the Right Thing, more efficient implementations are possible
}

{fpc has hostaddrtostr6 and strtohostaddr6 but the later isnt implemented yet}


function ip6bintostr(const bin:tin6_addr):string;
{base16 with lowercase output}
function makehex(w:word):string;
begin
  result := '';
  if w >= 4096 then result := result + hexchars[w shr 12];
  if w >= 256 then result := result + hexchars[w shr 8 and $f];
  if w >= 16 then result := result + hexchars[w shr 4 and $f];
  result := result + hexchars[w and $f];
end;

var
  a,b,c,addrlen:integer;
  runbegin,runlength:integer;
  bytes:array[0..15] of byte absolute bin;
  words:array[0..7] of word;
  dwords:array[0..3] of integer absolute words;
begin
  for a := 0 to 7 do begin
    words[a] := bytes[a shl 1] shl 8 or bytes[a shl 1 or 1];
  end;
  if (dwords[0] = 0) and (dwords[1] = 0) and (words[4] = 0) and (words[5] = $ffff) then begin
    {::ffff:/96 exception: v4 IP}
    addrlen := 6;
  end else begin
    addrlen := 8;
  end;
  {find longest run of zeroes}
  runbegin := 0;
  runlength := 0;
  for a := 0 to addrlen-1 do begin
    if words[a] = 0 then begin
      c := 0;
      for b := a to addrlen-1 do if words[b] = 0 then begin
        inc(c);
      end else break;
      if (c > runlength) then begin
        runlength := c;
        runbegin := a;
      end;
    end;
  end;
  result := '';
  for a := 0 to runbegin-1 do begin
    if (a <> 0) then result := result + ':';
    result := result + makehex(words[a]);
  end;
  if runlength > 0 then result := result + '::';
  c := runbegin+runlength;
  for a := c to addrlen-1 do begin
    if (a > c) then result := result + ':';
    result := result + makehex(words[a]);
  end;
  if addrlen = 6 then begin
    result := result + ':'+inttostr(bytes[12])+'.'+inttostr(bytes[13])+'.'+inttostr(bytes[14])+'.'+inttostr(bytes[15]);
  end;
end;

function ip6strtobin(const s:string;var bin:tin6_addr):boolean;
var
  a,b:integer;
  fields:array[0..7] of string;
  fieldcount:integer;
  emptyfield:integer;
  wordcount:integer;
  words:array[0..7] of word;
  bytes:array[0..15] of byte absolute bin;
begin
  result := false;
  for a := 0 to 7 do fields[a] := '';
  fieldcount := 0;
  for a := 1 to length(s) do begin
    if s[a] = ':' then inc(fieldcount) else fields[fieldcount] := fields[fieldcount] + s[a];
    if fieldcount > 7 then exit;
  end;
  if fieldcount < 2 then exit;

  {find the empty field (compressed zeroes), not counting the first and last there may be at most 1}
  emptyfield := -1;
  for a := 1 to fieldcount-1 do begin
    if fields[a] = '' then begin
      if emptyfield = -1 then emptyfield := a else exit;
    end;
  end;

  {check if last field is a valid v4 IP}
  a := longip(fields[fieldcount]);
  if (a <> 0) or (fields[fieldcount] = '0.0.0.0') then wordcount := 6 else wordcount := 8;
  {0:1:2:3:4:5:6.6.6.6
   0:1:2:3:4:5:6:7}
  fillchar(words,sizeof(words),0);
  if wordcount = 6 then begin
    if fieldcount > 6 then exit;
    words[6] := a shr 16;
    words[7] := a and $ffff;
  end;
  if emptyfield = -1 then begin
    {no run length: must be an exact number of fields}
    if wordcount = 6 then begin
      if fieldcount <> 6 then exit;
      emptyfield := 5;
    end else if wordcount = 8 then begin
      if fieldcount <> 7 then exit;
      emptyfield := 7;
    end else exit;
  end;
  for a := 0 to emptyfield do begin
    if fields[a] = '' then b := 0 else b := strtointdef('$'+fields[a],-1);
    if (b < 0) or (b > $ffff) then exit;
    words[a] := b;
  end;
  if wordcount = 6 then dec(fieldcount);
  for a := wordcount-1 downto wordcount-(fieldcount-emptyfield) do begin
    b := a+fieldcount-wordcount+1;
    if fields[b] = '' then b := 0 else b := strtointdef('$'+fields[b],-1);
    if (b < 0) or (b > $ffff) then exit;
    words[a] := b;
  end;
  for a := 0 to 7 do begin
    bytes[a shl 1] := words[a] shr 8;
    bytes[a shl 1 or 1] := words[a] and $ff;
  end;
  result := true;
end;
{$endif}

function comparebinip(const ip1,ip2:tbinip):boolean;
begin
  if (ip1.ip <> ip2.ip) then begin
    result := false;
    exit;
  end;

  {$ifdef ipv6}
  if ip1.family = AF_INET6 then begin
    if (ip1.ip6.s6_addr32[1] <> ip2.ip6.s6_addr32[1])
    or (ip1.ip6.s6_addr32[2] <> ip2.ip6.s6_addr32[2])
    or (ip1.ip6.s6_addr32[3] <> ip2.ip6.s6_addr32[3]) then begin
      result := false;
      exit;
    end;
  end;
  {$endif}

  result := (ip1.family = ip2.family);
end;

procedure maskbits(var binip:tbinip;bits:integer);
const
  ipmax={$ifdef ipv6}15{$else}3{$endif};
type tarr=array[0..ipmax] of byte;
var
  arr:^tarr;
  a,b:integer;
begin
  arr := @binip.ip;
  if bits = 0 then b := 0 else b := ((bits-1) div 8)+1;
  for a := b to ipmax do begin
    arr[a] := 0;
  end;
  if (bits and 7 <> 0) then begin
    arr[bits shr 3] := arr[bits div 8] and not ($ff shr (bits and 7))
  end;
end;

function comparebinipmask;
begin
  maskbits(ip1,bits);
  maskbits(ip2,bits);
  result := comparebinip(ip1,ip2);
end;

function needconverttov4(const ip:tbinip):boolean;
begin
  {$ifdef ipv6}
  if ip.family = AF_INET6 then begin
    if (ip.ip6.u6_addr32[0] = 0) and (ip.ip6.u6_addr32[1] = 0) and
    (ip.ip6.u6_addr16[4] = 0) and (ip.ip6.u6_addr16[5] = $ffff) then begin
      result := true;
      exit;
    end;
  end;
  {$endif}

  result := false;
end;

{converts a binary IP to v4 if it is a v6 IP in the v4 range}
procedure converttov4(var ip:tbinip);
begin
  {$ifdef ipv6}
  if needconverttov4(ip) then begin
    ip.family := AF_INET;
    ip.ip := ip.ip6.s6_addr32[3];
  end;
  {$endif}
end;

{-----------biniplist stuff--------------------------------------------------}

const
  biniplist_prefix='bipl'#0;
  //fpc 1.0.x doesn't seem to like use of length function in a constant 
  //definition
  //biniplist_prefixlen=length(biniplist_prefix);
  biniplist_prefixlen=5;
  
function biniplist_new:tbiniplist;
begin
  result := biniplist_prefix;
end;

procedure biniplist_add(var l:tbiniplist;ip:tbinip);
var
  a:integer;
begin
  a := biniplist_getcount(l);
  biniplist_setcount(l,a+1);
  biniplist_set(l,a,ip);
end;

function biniplist_getcount(const l:tbiniplist):integer;
begin
  result := (length(l)-biniplist_prefixlen) div sizeof(tbinip);
end;

function biniplist_get(const l:tbiniplist;index:integer):tbinip;
begin
  if (index >= biniplist_getcount(l)) then begin
    fillchar(result,sizeof(result),0);
    exit;
  end;
  move(l[index*sizeof(tbinip)+1+biniplist_prefixlen],result,sizeof(result));
end;

procedure biniplist_set(var l:tbiniplist;index:integer;ip:tbinip);
begin
  uniquestring(l);
  move(ip,l[index*sizeof(tbinip)+1+biniplist_prefixlen],sizeof(ip));
end;

procedure biniplist_setcount(var l:tbiniplist;newlen:integer);
begin
  setlength(l,(sizeof(tbinip)*newlen)+biniplist_prefixlen);
end;

procedure biniplist_free(var l:tbiniplist);
begin
  l := '';
end;

procedure biniplist_addlist;
begin
  l := l + copy(l2,biniplist_prefixlen+1,maxlongint);
end;

function biniplist_tostr(const l:tbiniplist):string;
var
  a:integer;
begin
  result := '(';
  for a := 0 to biniplist_getcount(l)-1 do begin
    if result <> '(' then result := result + ', ';
    result := result + ipbintostr(biniplist_get(l,a));
  end;
  result := result + ')';
end;

function isbiniplist(const l:tbiniplist):boolean;
var
  i : integer;
begin
  for i := 1 to biniplist_prefixlen do begin
    if biniplist_prefix[i] <> l[i] then begin
      result := false;
      exit;
    end;
  end;
  result := true;
end;

procedure addipsoffamily(var l:tbiniplist;const l2:tbiniplist;family:integer);
var
  a:integer;
  biniptemp:tbinip;
begin
  for a := biniplist_getcount(l2)-1 downto 0 do begin
    biniptemp := biniplist_get(l2,a);
    if (biniptemp.family = family) then biniplist_add(l,biniptemp);
  end;
end;


end.
