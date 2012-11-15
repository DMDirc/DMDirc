#!/bin/bash

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

# Force rebuild of plugins.
export REBUILDPLUGINS="true"

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
	# unzip -l "${MYDIR}/dist/DMDirc.jar" | grep " plugins/" | tr -s ' ' | cut -d ' ' -f 5- | xargs zip "${MYDIR}/dist/DMDirc.jar" -d

	# Temporary, add certain plugins back in as the installer builds no
        # longer add any at all
	# cd "${MYDIR}"
	#$JAR -uvf "${MYDIR}/dist/DMDirc.jar" plugins/ui_swing.jar plugins/tabcompletion_bash.jar plugins/tabcompletion_mirc.jar plugins/parser_irc.jar
	# Build files automatically do the above, so lets just not remove them for now.

	cd "${MYDIR}/modules/installer"
	PACKAGENAME="DMDirc-Nightly"
	./makeAll.sh --extra "${FILEDATA}" --packagename "${PACKAGENAME}" --jar "${MYDIR}/dist/DMDirc.jar" --version "${GITREV}"
	cd "${MYDIR}"

	OUTPUTDIR="${MYDIR}/modules/installer/output"

	if [ ! -e "${OUTPUTDIR}/${PACKAGENAME}-${FILEDATA}.exe" -o  ! -e "${OUTPUTDIR}/${PACKAGENAME}-${FILEDATA}.deb" -o ! -e "${OUTPUTDIR}/${PACKAGENAME}-${FILEDATA}.dmg" -o ! -e "${OUTPUTDIR}/${PACKAGENAME}-${FILEDATA}.jar" ]; then
		# Report failure
		echo "Installer build failure."
		if [ -e "$SCRIPTDIR/nightly-failure.php" -a "${PHP}" != "" ]; then
			export DMDIRC_INSTALLERFAILURE=true;
			export BAMBOO_BUILD;
			$PHP -q $SCRIPTDIR/nightly-failure.php
		fi
	fi;

	# Re-add all plugins to the jar so that the nightly .jar has everything.
	# $JAR -uvf "${OUTPUTDIR}/${PACKAGENAME}-${FILEDATA}.jar" plugins

	# Submit plugins to addons site
	if [ -e "${HOME}/www/addons/submitplugin.php" ]; then
		for plugin in `ls modules/plugins/dist/*.jar`; do
			$PHP ${HOME}/www/addons/submitplugin.php $plugin
		done;
	fi;

	function handleNightly() {
		PACKAGENAME="${1}"
		FILEDATA="${2}"
		EXT="${3}"

		mv -v "${OUTPUTDIR}/${PACKAGENAME}-${FILEDATA}.${EXT}" "${WWWDIR}/nightly/${PACKAGENAME}-${FILEDATA}.${EXT}"
		if [ -e "${WWWDIR}/nightly/${PACKAGENAME}-${FILEDATA}.${EXT}" ]; then
			ln -sfv "${WWWDIR}/nightly/${PACKAGENAME}-${FILEDATA}.${EXT}" "${WWWDIR}/nightly/${PACKAGENAME}_latest.${EXT}"
		fi;
	}

	handleNightly "${PACKAGENAME}" "${FILEDATA}" "exe"
	handleNightly "${PACKAGENAME}" "${FILEDATA}" "dmg"
	handleNightly "${PACKAGENAME}" "${FILEDATA}" "deb"
	handleNightly "${PACKAGENAME}" "${FILEDATA}" "rpm"

	# Jars get a different name for some reason.
	mv -v "${OUTPUTDIR}/${PACKAGENAME}-${FILEDATA}.jar" "${WWWDIR}/nightly/DMDirc_${FILEDATA}.jar"
	if [ -e "${WWWDIR}/nightly/DMDirc_${FILEDATA}.jar" ]; then
		ln -sfv "${WWWDIR}/nightly/DMDirc_${FILEDATA}.jar" "${WWWDIR}/nightly/DMDirc_latest.jar"
	fi;

	# # Update Launchers
	# cd ${MYDIR}/launcher
	# sh ${MYDIR}/launcher/createUpdate.sh

	# Do normal reports
	if [ "${IS_BAMBOO}" == "" ]; then
		/bin/sh $MYDIR/DoReports.sh
	fi;
fi
