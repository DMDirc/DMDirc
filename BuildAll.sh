#!/bin/sh

# cron doesn't seem to like doing this iself...
. /etc/profile

# Used for nightly.log to help diagnosing problems
env

# Path to WWW Directory
WWWDIR="/home/dmdirc/www"

# Path to trunk
MYDIR="/home/dmdirc/working/nightly"

# Path to scripts
SCRIPTDIR="/home/dmdirc/scripts"

# Path to ant binary
ANT="/usr/bin/ant"

# Path to git binary
GIT="/usr/bin/git"

# Path to jar binary
JAR="/usr/bin/jar"

cd ${MYDIR}

if [ -d .git ]; then
	$GIT reset --hard
	$GIT checkout master
	$GIT pull
	$GIT submodule init
	$GIT submodule update
	GITREV=`$GIT describe`
else
	echo "GIT Directory not found."
	exit 1;
fi;
export DMDIRC_GIT=${GITREV}

# Archive old nightlies
if [ `date +%d` = "01" ]; then
	echo "Archiving last month's nightlies..."
	OLDDIR=${WWWDIR}/nightly/old/`date -d yesterday +%B%y | tr "[:upper:]" "[:lower:]"`
	mkdir -p ${OLDDIR}
	mv -fv ${WWWDIR}/nightly/*_`date -d yesterday +%Y%m`??_* ${OLDDIR}
	mv -fv ${WWWDIR}/nightly/*-`date -d yesterday +%Y%m`??_* ${OLDDIR}
	rm -Rf ${WWWDIR}/nightly/*_latest*
fi

# The date/git rev to add to the end of the file names of stuff
FILEDATA=`date +%Y%m%d`_${GITREV}

# Build plugins/jar
$ANT -Dchannel=NIGHTLY -k clean jar

PHP=`which php`

# Check if build failed
if [ ! -e "$MYDIR/dist/DMDirc.jar" ]; then
	# Report failure
	echo "Build failure"
	if [ -e "$SCRIPTDIR/nightly-failure.php" -a "${PHP}" != "" ]; then
		$PHP -q $SCRIPTDIR/nightly-failure.php
	fi
else
	# Build installers
	# Delete all automatically added plugins from the jar to allow the installer
	# to add its own on a per-os basis
	unzip -l ${MYDIR}/dist/DMDirc.jar | grep " plugins/" | tr -s ' ' | cut -d ' ' -f 5- | xargs zip ${MYDIR}/dist/DMDirc.jar -d
	cd "${MYDIR}/installer"
	./release.sh --jar "${MYDIR}/dist/DMDirc.jar" --opt "--extra ${FILEDATA}" trunk
	cd "${MYDIR}"
	
	if [ ! -e "${MYDIR}/installer/output/DMDirc-Setup-${FILEDATA}.exe" -o  ! -e "${MYDIR}/installer/output/DMDirc-Setup-${FILEDATA}.run" -o ! -e "${MYDIR}/installer/output/DMDirc-${FILEDATA}.dmg" -o ! -e "${MYDIR}/installer/output/DMDirc-${FILEDATA}.jar" ]; then
		# Report failure
		echo "Installer build failure."
		if [ -e "$SCRIPTDIR/nightly-failure.php" -a "${PHP}" != "" ]; then
			export DMDIRC_INSTALLERFAILURE=true;
			export BAMBOO_BUILD;
			$PHP -q $SCRIPTDIR/nightly-failure.php
		fi
	fi;

	# Re-add all plugins to the jar so that the nightly .jar has everything.
	$JAR -uvf "dist/DMDirc.jar" plugins

	# Submit plugins to addons site
	if [ -e "${HOME}/www/addons/submitplugin.php" ]; then
		for plugin in `ls plugins/*.jar`; do
			$PHP ${HOME}/www/addons/submitplugin.php $plugin
		done;
	fi;
	
	# Move installers/jar to nightlies site
	FILENAME=DMDirc_${FILEDATA}.jar
	mv "installer/output/DMDirc-Setup-${FILEDATA}.exe" "${WWWDIR}/nightly/DMDirc-Setup-${FILEDATA}.exe"
	mv "installer/output/DMDirc-Setup-${FILEDATA}.run" "${WWWDIR}/nightly/DMDirc-Setup-${FILEDATA}.run"
	mv "installer/output/DMDirc-${FILEDATA}.dmg" "${WWWDIR}/nightly/DMDirc-${FILEDATA}.dmg"
	rm -Rf "installer/output/DMDirc-${FILEDATA}.jar"
	cp dist/DMDirc.jar /home/dmdirc/www/nightly/$FILENAME

	if [ -e "${WWWDIR}/nightly/${FILENAME}" ]; then
		ln -sf ${WWWDIR}/nightly/${FILENAME} $WWWDIR/nightly/DMDirc_latest.jar
	fi;
	if [ -e "${WWWDIR}/nightly/DMDirc-Setup-${FILEDATA}.run" ]; then
		ln -sf "${WWWDIR}/nightly/DMDirc-Setup-${FILEDATA}.run" $WWWDIR/nightly/DMDirc-Setup_latest.run
	fi;
	if [ -e "${WWWDIR}/nightly/DMDirc-Setup-${FILEDATA}.exe" ]; then
		ln -sf "${WWWDIR}/nightly/DMDirc-Setup-${FILEDATA}.exe" $WWWDIR/nightly/DMDirc-Setup_latest.exe
	fi;
	if [ -e "${WWWDIR}/nightly/DMDirc-${FILEDATA}.dmg" ]; then
		ln -sf "${WWWDIR}/nightly/DMDirc-${FILEDATA}.dmg" $WWWDIR/nightly/DMDirc_latest.dmg
	fi;
	
	# Update Launchers
	cd ${MYDIR}/launcher
	sh ${MYDIR}/launcher/createUpdate.sh
	
	# Do normal reports
	if [ "${IS_BAMBOO}" == "" ]; then
		/bin/sh $MYDIR/DoReports.sh
	fi;
fi
