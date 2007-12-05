#!/bin/sh
rm -Rf ./*.exe
fpc -Sd -Twin32 DMDirc.dpr
fpc -Sd -Twin32 DMDircUpdater.dpr
rm -Rf ./*.o ./*.or
if [ -e DMDircUpdater.exe -a -e DMDirc.exe ]; then
	exit 0;
else
	exit 1;
fi