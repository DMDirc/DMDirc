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

/bin/sh $MYDIR/oblong.sh "Nightly Build" "Build Started"

cd $MYDIR/
$SVN update
SVNREV=`$SVN info | grep Revision`
SVNREV=${SVNREV##*: }
export DMDIRC_SVN=${SVNREV}

# Substitute the version string
awk '{gsub(/String VERSION = "SVN ("+SVN_REVISION+")"/,"String VERSION = \"Nightly - SVN Rev: '${SVNREV}'\"");print}' ${MYDIR}/src/com/dmdirc/Main.java > ${MYDIR}/src/com/dmdirc/Main.java.tmp
mv ${MYDIR}/src/com/dmdirc/Main.java.tmp ${MYDIR}/src/com/dmdirc/Main.java

# Substitute the update channel
awk '{gsub(/UpdateChannel UPDATE_CHANNEL = UpdateChannel.NONE/,"UpdateChannel UPDATE_CHANNEL = UpdateChannel.NIGHTLY");print}' ${MYDIR}/src/com/dmdirc/Main.java > ${MYDIR}/src/com/dmdirc/Main.java.tmp
mv ${MYDIR}/src/com/dmdirc/Main.java.tmp ${MYDIR}/src/com/dmdirc/Main.java

# This no longer exists
## Substitue the release date
#awk '{gsub(/int RELEASE_DATE = 0/,"int RELEASE_DATE = '`date +%Y%m%d`'");print}' ${MYDIR}/src/com/dmdirc/Main.java > ${MYDIR}/src/com/dmdirc/Main.java.tmp
#mv ${MYDIR}/src/com/dmdirc/Main.java.tmp ${MYDIR}/src/com/dmdirc/Main.java

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
$ANT -buildfile $MYDIR/build.xml -k clean jar

# Now revert the trunk so as not to break updates.
for updatedir in ${REVERTLIST}; do
	${SVN} revert ${updatedir}/*
done;

PHP=`which php`

if [ "${IS_BAMBOO}" != "" -e ${BAMBOO} ]; then
	export BAMBOO_DIR=${BAMBOO};
	export BAMBOO_BUILD=`ls -1 ${BAMBOO} | tail -n 1 | awk -F. '{print $1}'`
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
	cd "${MYDIR}/installer"
	./release.sh --jar "${MYDIR}/dist/DMDirc.jar" --opt "--tag ${FILEDATA}" trunk
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

	# Add plugins to jar
	$JAR -uvf "dist/DMDirc.jar" plugins

	# Submit plugins to addons site
	if [ -e "${HOME}/www/addons/submitplugin.php" ]; then
		for plugin in `ls plugins/*.jar`; do
			$PHP ${HOME}/www/addons/submitplugin.php $plugin
		done;
	fi;
	
	# Move installers/jar to nightlies site
	FILENAME=DMDirc_${FILEDATA}.jar
	mv "${MYDIR}/installer/output/DMDirc-Setup-${FILEDATA}.exe" "${WWWDIR}/nightly/DMDirc-Setup-${FILEDATA}.exe"
	mv "${MYDIR}/installer/output/DMDirc-Setup-${FILEDATA}.run" "${WWWDIR}/nightly/DMDirc-Setup-${FILEDATA}.run"
	mv "${MYDIR}/installer/output/DMDirc-${FILEDATA}.dmg" "${WWWDIR}/nightly/DMDirc-${FILEDATA}.dmg"
	rm -Rf "${MYDIR}/installer/output/DMDirc-${FILEDATA}.jar"
	cp $MYDIR/dist/DMDirc.jar /home/dmdirc/www/nightly/$FILENAME

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
	cd ${MYDIR}
	# /bin/sh $MYDIR/oblong.sh "Nightly Build" "Build Successful";
	
	# Do normal reports
	if [ "${IS_BAMBOO}" == "" ]; then
		/bin/sh $MYDIR/DoReports.sh
	fi;
fi

$SVN revert ${MYDIR}/src/com/dmdirc/Main.java


