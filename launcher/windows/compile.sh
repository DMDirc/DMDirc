#!/bin/sh
rm UAC.manifest UAC.rc
if [ ! -e ../../installer/windows/UAC.rc ]; then
	echo "1 24 \"UAC.manifest\"" > UAC.rc
	cp ../../installer/windows/UAC.manifest .
else
	cp ../../installer/windows/UAC.manifest ../../installer/windows/UAC.rc .
fi;

if [ ! -e ./icon.ico ]; then
	ln -sf "../../src/com/dmdirc/res/icon.ico" ./icon.ico
fi

rm -Rf ./*.exe
compilerFlags="-Xs -XX -O2 -Or -Op1"
extraFlags="-Fu${PWD}/../../libwin"
fpc -Sd -Twin32 ${compilerFlags} ${extraFlags} DMDirc.dpr
fpc -Sd -Twin32 ${compilerFlags} ${extraFlags} DMDircUpdater.dpr
rm -Rf ./*.o ./*.or ./*.ppu
if [ -e DMDircUpdater.exe -a -e DMDirc.exe ]; then
	exit 0;
else
	exit 1;
fi
