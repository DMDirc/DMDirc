#!/bin/sh
# This script generates the launcher updates
LAUNCHERDIR=`dirname $0`
LAUNCHERUPDATEDIR="/home/dmdirc/www/updates/launchers/"

UNIXVERSION=`cat ${LAUNCHERDIR}/unix/DMDirc.sh | grep LAUNCHERVERSION= | awk -F\" '{print $2}'`
WINDOWSVERSION=`cat ${LAUNCHERDIR}/windows/DMDirc.dpr | grep "launcherVersion: String =" | awk -F\' '{print $2}'`

# Find out what params we should pass to things.
# Solaris has a nice and ancient version of grep in /usr/bin
grep -na "" /dev/null >/dev/null 2>&1
if [ $? -eq 2 ]; then
	GREPOPTS="-n"
else
	GREPOPTS="-na"
fi;
# Solaris also has a crappy version of tail!
tail -n +1 /dev/null >/dev/null 2>&1
if [ $? -eq 2 ]; then
	TAILOPTS="+"
else
	TAILOPTS="-n +"
fi;

if [ ! -e "${LAUNCHERUPDATEDIR}/unix-${UNIXVERSION}.sh" ]; then
	echo "Creating Launcher Update: unix-${UNIXVERSION}";
	
	FUNCTIONSFILE="${LAUNCHERDIR}/../installer/linux/functions.sh"
	SRCFILE=${LAUNCHERDIR}/unix/DMDirc.sh
	DESTFILE=${LAUNCHERUPDATEDIR}/unix-${UNIXVERSION}.sh
	
	if [ -e "${FUNCTIONSFILE}" ]; then
		FUNCTIONSLINE=`grep ${GREPOPTS} "^###FUNCTIONS_FILE###$" ${SRCFILE}`
		if [ "${FUNCTIONSLINE}" == "" ]; then
			echo "    Functions already built into launcher."
			cp ${SRCFILE} ${DESTFILE}
		else
			echo "    Including functions.sh into launcher."
			FUNCTIONSLINE=$((${FUNCTIONSLINE%%:*} + 0))
			
			head -n ${FUNCTIONSLINE} ${SRCFILE} > ${DESTFILE}
			cat functions.sh >> ${DESTFILE}
			echo "" >> ${DESTFILE}
			tail ${TAILOPTS}$((${FUNCTIONSLINE%%:*} + 1)) ${SRCFILE} >> ${DESTFILE}
		fi;
	else
		echo "    Unable to create unix launcher update, no functions.sh found."
	fi;
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