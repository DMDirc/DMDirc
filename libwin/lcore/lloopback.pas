unit lloopback;

interface
uses lcore,classes;

type
  tlloopback=class(tlasio)
  public
    constructor create(aowner:tcomponent); override;
  end;


implementation
uses
{$ifdef ver1_0}
  linux;
{$else}
  baseunix,unix,unixutil;  
{$endif}
{$i unixstuff.inc}

constructor tlloopback.create(aowner:tcomponent);
begin
  inherited create(aowner);
  closehandles := true;
  assignpipe(fdhandlein,fdhandleout);

  eventcore.rmasterset(fdhandlein,false);//fd_set(fdhandlein,fdsrmaster);
  eventcore.wmasterclr(fdhandlein);//fd_clr(fdhandleout,fdswmaster);
  eventcore.setfdreverse(fdhandlein,self);
  eventcore.setfdreverse(fdhandleout,self);
  state := wsconnected;
end;
end.
