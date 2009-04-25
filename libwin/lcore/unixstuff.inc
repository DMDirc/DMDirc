{$ifdef UNIX}
  {$macro on}
  {$ifdef VER1_0}
    {$define tv_sec := sec}
    {$define tv_usec := usec}
    function dup(const original:integer):integer;inline;
    begin
      linux.dup(original,result);
    end;
    {$define gettimeofdaysec := gettimeofday}
    const
      IPPROTO_UDP=17;
      IPPROTO_ICMP=1;
    function ntohs(invalue:word):word;inline;
    var
      invaluebytes : array[0..1] of byte absolute invalue;
      resultbytes : array[0..1] of byte absolute result;
    begin
      {$ifdef endian_little}
	resultbytes[0] := invaluebytes[1];
	resultbytes[1] := invaluebytes[0];
      {$else}
	result := invalue;
      {$endif}
    end;  
  {$else}
    
    {$define sigprocmask := fpsigprocmask}
    {$define sigaction   := fpsigaction}
    {$define fdclose     := fpclose}
    {$define fcntl       := fpfcntl}
    {$define fdwrite     := fpwrite}
    {$define fdread      := fpread}
    {$define fdopen      := fpopen}
    {$define select      := fpselect}
    {$define linuxerror  := fpgeterrno}
    {$define fork        := fpfork}
    {$define getpid      := fpgetpid}
    {$define getenv      := fpgetenv}
    {$define chmod       := fpchmod}
    {$define dup2        := fpdup2}
    {$ifndef ver1_9_2}
      {$define flock     := fpflock}
      {$ifndef ver1_9_4}
        procedure Execl(Todo:string);inline;
	var
	  p : ppchar;
	begin
	  p := unixutil.StringToPPChar(Todo,1);
	  if (p=nil) or (p^=nil) then exit;
	  fpexecv(p^,p);
	end;
      {$endif}
    {$endif}
    {$ifdef ver2_0}
      const
        IPPROTO_UDP=17;
        IPPROTO_ICMP=1;
    {$endif}
    {$ifdef ver1_9}
      const
        IPPROTO_UDP=17;
        IPPROTO_ICMP=1;
      function ntohs(invalue:word):word;inline;
      var
        invaluebytes : array[0..1] of byte absolute invalue;
	resultbytes : array[0..1] of byte absolute result;
      begin
        {$ifdef endian_little}
	  resultbytes[0] := invaluebytes[1];
	  resultbytes[1] := invaluebytes[0];
	{$else}
	  result := invalue;
	{$endif}
      end;
    {$endif}
    procedure gettimeofday(var tv:ttimeval);inline;
    begin
      fpgettimeofday(@tv,nil);    
    end;
    function gettimeofdaysec : longint;
    var
      tv:ttimeval;
    begin
      gettimeofday(tv);
      result := tv.tv_sec;
    end;

    //a function is used here rather than a define to prevent issues with tlasio.dup
    function dup(const original:integer):integer;inline;
    begin
      result := fpdup(original);
    end;
    function octal(invalue:longint):longint;
    var
      a : integer;
      i : integer;
    begin
      i := 0;
      result := 0;
      while invalue <> 0 do begin
        a := invalue mod 10;
        result := result + (a shl (i*3));

        invalue := invalue div 10;
        inc(i);
      end;
    end;
    const
      sys_eintr=esyseintr;

  {$endif}
{$endif}
