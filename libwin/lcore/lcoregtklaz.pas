{ Copyright (C) 2005 Bas Steendijk and Peter Green
  For conditions of distribution and use, see copyright notice in zlib_license.txt
  which is included in the package
  ----------------------------------------------------------------------------- }
      
unit lcoregtklaz;
{$mode delphi}
interface
	
uses baseunix,unix,glib, gdk, gtk,lcore,forms,fd_utils,classes;
//procedure lcoregtklazrun;
const
  G_IO_IN=1;
  G_IO_OUT=4;
  G_IO_PRI=2;
  G_IO_ERR=8;

  G_IO_HUP=16;
  G_IO_NVAL=32;
type
  tlaztimerwrapperinterface=class(ttimerwrapperinterface)
  public
    function createwrappedtimer : tobject;override;
//    procedure setinitialevent(wrappedtimer : tobject;newvalue : boolean);override;
    procedure setontimer(wrappedtimer : tobject;newvalue:tnotifyevent);override;
    procedure setenabled(wrappedtimer : tobject;newvalue : boolean);override;
    procedure setinterval(wrappedtimer : tobject;newvalue : integer);override;
  end;

procedure lcoregtklazinit;
implementation
  uses
    ExtCtrls;
{$I unixstuff.inc}
var
  giochannels : array[0..absoloutemaxs] of pgiochannel;

function iocallback(source:PGIOChannel; condition:TGIOCondition; data:gpointer):gboolean;cdecl;
// return true if we want the callback to stay
var
  fd                    : integer ;
  fdsrlocal , fdswlocal : fdset   ;
  currentasio           : tlasio  ;
begin
  fd := g_io_channel_unix_get_fd(source);
  fd_zero(fdsrlocal);
  fd_set(fd,fdsrlocal);
  fdswlocal := fdsrlocal;
  select(fd+1,@fdsrlocal,@fdswlocal,nil,0);
  if fd_isset(fd,fdsrlocal) or fd_isset(fd,fdsrlocal) then begin
    currentasio := fdreverse[fd];
    if assigned(currentasio) then begin
      currentasio.handlefdtrigger(fd_isset(currentasio.fdhandlein,fdsrlocal),fd_isset(currentasio.fdhandleout,fdswlocal));
    end else begin
      rmasterclr(fd);
      wmasterclr(fd);
    end;
  end;
  case condition of
    G_IO_IN : begin
      result := rmasterisset(fd);
    end;
    G_IO_OUT : begin
      result := wmasterisset(fd);
    end;
  end;
end;

procedure gtkrmasterset(fd : integer);
begin
  if not assigned(giochannels[fd]) then giochannels[fd] := g_io_channel_unix_new(fd);
  g_io_add_watch(giochannels[fd],G_IO_IN,iocallback,nil);
end;

procedure gtkrmasterclr(fd: integer);
begin
end;
  
procedure gtkwmasterset(fd : integer);
begin
  if not assigned(giochannels[fd]) then giochannels[fd] := g_io_channel_unix_new(fd);
  g_io_add_watch(giochannels[fd],G_IO_OUT,iocallback,nil);
end;

procedure gtkwmasterclr(fd: integer);
begin
end;

type
  tsc = class
    procedure dotasksandsink(sender:tobject;error:word);
  end;
var
  taskloopback : tlloopback;
  sc           : tsc;
procedure tsc.dotasksandsink(sender:tobject;error:word);
begin
  with tlasio(sender) do begin
    sinkdata(sender,error);
    processtasks;
  end;
end;
procedure gtkaddtask(ahandler:ttaskevent;aobj:tobject;awparam,alparam:longint);
begin
  taskloopback.sendstr(' ');
  
end;

procedure lcoregtklazinit;
begin
  onrmasterset := gtkrmasterset;
  onrmasterclr := gtkrmasterclr;
  onwmasterset := gtkwmasterset;
  onwmasterclr := gtkwmasterclr;
  onaddtask := gtkaddtask;
  taskloopback := tlloopback.create(nil);
  taskloopback.ondataavailable := sc.dotasksandsink;
  timerwrapperinterface := tlaztimerwrapperinterface.create(nil);
end;

function tlaztimerwrapperinterface.createwrappedtimer : tobject;
begin
  result := ttimer.create(nil);
end;
procedure tlaztimerwrapperinterface.setontimer(wrappedtimer : tobject;newvalue:tnotifyevent);
begin
  ttimer(wrappedtimer).ontimer := newvalue;
end;
procedure tlaztimerwrapperinterface.setenabled(wrappedtimer : tobject;newvalue : boolean);
begin
  ttimer(wrappedtimer).enabled := newvalue;
end;


procedure tlaztimerwrapperinterface.setinterval(wrappedtimer : tobject;newvalue : integer);
begin
  ttimer(wrappedtimer).interval := newvalue;
end;


end.

