
{ Copyright (C) 2005 Bas Steendijk and Peter Green
  For conditions of distribution and use, see copyright notice in zlib_license.txt
  which is included in the package
  ----------------------------------------------------------------------------- }

{this enables the entire ipv6 functionality - resolving, connecting, etc
this is enabled by default to make all apps using lcore automatically ipv6 aware.
to disable, undefine it here, or define "noipv6" in the app}

{$ifndef noipv6}
{$define ipv6}
{$endif}

{-------------------------------------------------------------------------------------}
{there are 2 ways to use DNS in lcore: dnscore, which an entire built in DNS client, and getaddrinfo.
dnscore is always included on *nix to avoid libc dependency problems, but getaddrinfo is used on windows.
when getaddrinfo is used, there is no reason to include dnscore, and it increases the exe size,
unless you want to use custom nameserver addresses. enable this setting to always include it.}

{-$define syncdnscore}

{-------------------------------------------------------------------------------------}
{lcore contains a built in general purpose secure random number generator, which is used elsewhere in lcore, for
example by the DNS resolver. the used random function can be hooked to point to one's own RNG as desired.
it is then also possible to not include the built in RNG in the exe, which reduces code size}

{-$define nolcorernd}

{-------------------------------------------------------------------------------------}
{on windows up to XP, listening on ipv6 will not listen on ipv4, while on other platforms it does, 
so a single listener cant get all connections for a port number, only those for one address family.
also it means a portable app would gave to deal with inconsistent behavior.
enable this option to simulate the behavior of listening on both v4 and v6}

{$ifdef win32}{$ifdef ipv6}
{$define secondlistener}
{$endif}{$endif}

{-------------------------------------------------------------------------------------}