#!/bin/sh
rm Vista.pas UAC.manifest UAC.rc
if [ ! -e ../../installer/windows/UAC.rc ]; then
	echo "1 24 \"UAC.manifest\"" > UAC.rc
	cp ../../installer/windows/Vista.pas ../../installer/windows/UAC.manifest .
else
	cp ../../installer/windows/Vista.pas ../../installer/windows/UAC.manifest ../../installer/windows/UAC.rc .
fi;

rm -Rf ./*.exe
fpc -Sd -Twin32 DMDirc.dpr
fpc -Sd -Twin32 DMDircUpdater.dpr
rm -Rf ./*.o ./*.or ./*.ppu
if [ -e DMDircUpdater.exe -a -e DMDirc.exe ]; then
	exit 0;
else
	exit 1;
fi