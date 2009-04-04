#!/bin/sh

# cron doesn't seem to like doing this iself...
. /etc/profile

# Used for nightly.log to help diagnosing problems
env

# Path to WWW Directory
WWWDIR="/home/dmdirc/www"

# Path to trunk
MYDIR="/home/dmdirc/google"

# Path to scripts
SCRIPTDIR="/home/dmdirc/scripts"

# Path to ant binary
ANT="/usr/bin/ant"

# Path to svn binary
SVN="/usr/bin/svn"

# Path to jar binary
JAR="/usr/bin/jar"

# Where are the bamboo log files?
BAMBOO=/home/dmdirc/Bamboo/xml-data/builds/DMDIRC-NIGHTLY/download-data/build_logs/

cd ${MYDIR}

$SVN update
SVNREV=`$SVN info | grep Revision`
SVNREV=${SVNREV##*: }
export DMDIRC_SVN=${SVNREV}

# Archive old nightlies
if [ `date +%d` = "01" ]; then
	echo "Archiving Last Months Nightlies"
	OLDDIR=${WWWDIR}/nightly/old/`date -d yesterday +%B%y | tr "[:upper:]" "[:lower:]"`
	mkdir -p ${OLDDIR}
	mv -fv ${WWWDIR}/nightly/*_`date -d yesterday +%Y%m`??_* ${OLDDIR}
	mv -fv ${WWWDIR}/nightly/*-`date -d yesterday +%Y%m`??_* ${OLDDIR}
	rm -Rf ${WWWDIR}/nightly/*_latest*
fi

# The date/svn prefix to add to the end of the file names of stuff
FILEDATA=`date +%Y%m%d`_${SVNREV}

# Copy default settings from www to trunk for compile (if they exist)
REVERTLIST=""
if [ -e "${HOME}/www/updates/" ]; then
	for updatedir in `ls -1 src/com/dmdirc/config/defaults/`; do
		src="${HOME}/www/updates/${updatedir}"
		if [ -e ${src} ]; then
			REVERTLIST=${REVERTLIST}" src/com/dmdirc/config/defaults/${updatedir}/"
			cp -Rfv ${src}/* src/com/dmdirc/config/defaults/${updatedir}/
		fi;
	done
fi;

# Build plugins/jar
$ANT -Dchannel=NIGHTLY -k clean jar

# Now revert the trunk so as not to break updates.
for updatedir in ${REVERTLIST}; do
	${SVN} revert ${updatedir}/*
done;

PHP=`which php`

if [ "${BAMBOO_INSTALL}" != "" -a -e "${BAMBOO}" ]; then
	export BAMBOO_DIR=${BAMBOO};
	export BAMBOO_BUILD=`ls -cr1 ${BAMBOO} | tail -n 1 | awk -F. '{print $1}'`
	echo "This is Bamboo build "${BAMBOO_BUILD};
fi

# Check if build failed
if [ ! -e "$MYDIR/dist/DMDirc.jar" ]; then
	# Report failure
	echo "Build Failure."
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
		echo "Installer Build Failure."
		if [ -e "$SCRIPTDIR/nightly-failure.php" -a "${PHP}" != "" ]; then
			export DMDIRC_INSTALLERFAILURE=true;
			export BAMBOO_BUILD;
			$PHP -q $SCRIPTDIR/nightly-failure.php
		fi
	fi;

	# Re-Add all plugins to the jar so that the nightly .jar has everything.
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
	
	# Do normal reports
	if [ "${IS_BAMBOO}" == "" ]; then
		/bin/sh $MYDIR/DoReports.sh
	fi;
fi

$SVN revert src/com/dmdirc/Main.java
