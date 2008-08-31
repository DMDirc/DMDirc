#!/bin/sh
# This script generates the launcher updates
LAUNCHERDIR="/home/dmdirc/google/launcher"
LAUNCHERUPDATEDIR="/home/dmdirc/www/updates/launchers/"

UNIXVERSION=`cat ${LAUNCHERDIR}/unix/DMDirc.sh | grep LAUNCHERVERSION= | awk -F\" '{print $2}'`
WINDOWSVERSION=`cat ${LAUNCHERDIR}/windows/DMDirc.dpr | grep "launcherVersion: String =" | awk -F\' '{print $2}'`

if [ ! -e "${LAUNCHERUPDATEDIR}/unix-${UNIXVERSION}.sh" ]; then
	echo "Creating Launcher Update: unix-${UNIXVERSION}";
	cp -${LAUNCHERDIR}/unix/DMDirc.sh ${LAUNCHERUPDATEDIR}/unix-${UNIXVERSION}.sh
fi;

if [ ! -e "${LAUNCHERUPDATEDIR}/windows-${WINDOWSVERSION}.zip" ]; then
	echo "Creating Launcher Update: windows-${WINDOWSVERSION}";
	OLDDIR=${PWD}
	cd ${LAUNCHERDIR}/windows
	sh compile.sh
	# Create symlinks
	for exe in `ls *.exe`; do
		ln -s ${exe} .${exe}
	done;

	# Create Zip File
	zip -9 ${LAUNCHERUPDATEDIR}/windows-${WINDOWSVERSION}.zip .*.exe

	# Remove temp exes
	for exe in `ls .*.exe`; do
		rm -Rf ${exe}
	done;

	cd ${OLDDIR}
fi;