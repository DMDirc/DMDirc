{ Copyright (C) 2005 Bas Steendijk and Peter Green
  For conditions of distribution and use, see copyright notice in zlib_license.txt
  which is included in the package
  ----------------------------------------------------------------------------- }
unit bfifo;
{$ifdef fpc}
  {$mode delphi}
{$endif}

interface

uses blinklist,pgtypes;

const
  pagesize=1420;

type
  tfifo=class(tobject)
  private
    l:tlinklist;     {add to}
    getl:tlinklist; {remove from}
    ofs:integer;
    getofs:integer;
  public
    size:integer;
    procedure add(data:pointer;len:integer);
    function get(var resultptr:pointer;len:integer):integer;
    procedure del(len:integer);
    constructor create;
    destructor destroy; override;
  end;


implementation

var
  testcount:integer;

{

xx1..... add
xxxxxxxx
....2xxx delete

1 ofs
2 getofs

}

procedure tfifo.add;
var
  a:integer;
  p:tlinklist;
begin
  if len <= 0 then exit;
  inc(size,len);
  while len > 0 do begin
    p := l;
    if ofs = pagesize then begin
      p := tplinklist.create;
      if getl = nil then getl := p;
      getmem(tplinklist(p).p,pagesize);
      inc(testcount);
      linklistadd(l,p);
      ofs := 0;
    end;
    a := pagesize - ofs;
    if len < a then a := len;
    move(data^,pointer(taddrint(tplinklist(p).p)+ofs)^,a);
    inc(taddrint(data),a);
    dec(len,a);
    inc(ofs,a);
  end;
end;

function tfifo.get;
var
  p:tlinklist;
  a:integer;
begin
  if len > size then len := size;
  if len <= 0 then begin
    result := 0;
    resultptr := nil;
    exit;
  end;
  p := getl;
  resultptr := pointer(taddrint(tplinklist(p).p)+getofs);
  result := pagesize-getofs;
  if result > len then result := len;
end;

procedure tfifo.del;
var
  a:integer;
  p,p2:tlinklist;
begin
  if len <= 0 then exit;
  p := getl;
  if len > size then len := size;
  dec(size,len);

  if len = 0 then exit;

  while len > 0 do begin
    a := pagesize-getofs;
    if a > len then a := len;
    inc(getofs,a);
    dec(len,a);
    if getofs = pagesize then begin
      p2 := p.prev;
      freemem(tplinklist(p).p);
      dec(testcount);
      linklistdel(l,p);
      p.destroy;
      p := p2;
      getl := p;
      getofs := 0;
    end;
  end;

  if size = 0 then begin
    if assigned(l) then begin
      p := l;
      freemem(tplinklist(p).p);
      dec(testcount);
      linklistdel(l,p);
      p.destroy;
      getl := nil;
    end;
    ofs := pagesize;
    getofs := 0;
  end;
end;

constructor tfifo.create;
begin
  ofs := pagesize;
  inherited create;
end;

destructor tfifo.destroy;
begin
  del(size);
  inherited destroy;
end;

end.
