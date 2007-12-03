#!/bin/sh

# cron doesn't seem to like doing this iself...
. /etc/profile

# Path to WWW Directory
WWWDIR="/home/dmdirc/www"
# Path to trunk
MYDIR="/home/dmdirc/google"
# Path to ant binary
ANT="/usr/bin/ant"
# Path to svn binary
SVN="/usr/bin/svn"
# Path to zip binary
ZIP="/usr/bin/zip"

cd ${MYDIR}

/bin/sh $MYDIR/oblong.sh "Nightly Build" "Build Started"
rm -Rf $MYDIR/dist
rm -Rf $MYDIR/build

cd $MYDIR/
$SVN update
SVNREV=`$SVN info | grep Revision`
SVNREV=${SVNREV##*: }

# Substitute the version string
awk '{gsub(/String VERSION = "SVN"/,"String VERSION = \"Nightly - SVN Rev: '${SVNREV}'\"");print}' ${MYDIR}/src/com/dmdirc/Main.java > ${MYDIR}/src/com/dmdirc/Main.java.tmp
mv ${MYDIR}/src/com/dmdirc/Main.java.tmp ${MYDIR}/src/com/dmdirc/Main.java

# Substitute the update channel
awk '{gsub(/UpdateChannel UPDATE_CHANNEL = UpdateChannel.NONE/,"UpdateChannel UPDATE_CHANNEL = UpdateChannel.NIGHTLY");print}' ${MYDIR}/src/com/dmdirc/Main.java > ${MYDIR}/src/com/dmdirc/Main.java.tmp
mv ${MYDIR}/src/com/dmdirc/Main.java.tmp ${MYDIR}/src/com/dmdirc/Main.java

# Substitue the release date
awk '{gsub(/int RELEASE_DATE = 0/,"int RELEASE_DATE = '`date +%Y%m%d`'");print}' ${MYDIR}/src/com/dmdirc/Main.java > ${MYDIR}/src/com/dmdirc/Main.java.tmp
mv ${MYDIR}/src/com/dmdirc/Main.java.tmp ${MYDIR}/src/com/dmdirc/Main.java

# The date/svn prefix to add to the end of the file names of stuff
FILEDATA=`date +_%Y%m%d`_${SVNREV}

# Build plugins/jar
$ANT -buildfile $MYDIR/build.xml -k clean jar

# Build installers
if [ -e "$MYDIR/dist/DMDirc.jar" ]; then
	cd "${MYDIR}/installer"
	./release.sh --jar "${MYDIR}/dist/DMDirc.jar" --opt "--tag ${FILEDATA}" trunk
	cd "${MYDIR}"
fi;

if [ -f $MYDIR/dist/DMDirc.jar ]; then
	FILENAME=DMDirc${FILEDATA}.jar
	mv "${MYDIR}/installer/output/DMDirc-Setup-${FILEDATA}.exe" "${WWWDIR}/nightly/DMDirc-Setup-${FILEDATA}.exe"
	mv "${MYDIR}/installer/output/DMDirc-Setup-${FILEDATA}.run" "${WWWDIR}/nightly/DMDirc-Setup-${FILEDATA}.run"
	cp $MYDIR/dist/DMDirc.jar /home/dmdirc/www/nightly/$FILENAME
	${ZIP} -r9 /home/dmdirc/www/nightly/Plugins${FILEDATA}.zip plugins
	if [ -e $WWWDIR/nightly/DMDirc_latest.jar ]; then
		rm $WWWDIR/nightly/DMDirc_latest.jar
	fi
	if [ -e $WWWDIR/nightly/Plugins_latest.zip ]; then
		rm $WWWDIR/nightly/Plugins_latest.zip
	fi
	if [ -e $WWWDIR/nightly/DMDirc-Setup_latest.exe ]; then
		rm $WWWDIR/nightly/DMDirc-Setup_latest.exe
	fi
	if [ -e $WWWDIR/nightly/DMDirc-Setup_latest.run ]; then
		rm $WWWDIR/nightly/DMDirc-Setup_latest.run
	fi
	ln -s $WWWDIR/nightly/$FILENAME $WWWDIR/nightly/DMDirc_latest.jar
	ln -s $WWWDIR/nightly/Plugins${FILEDATA}.zip $WWWDIR/nightly/Plugins_latest.zip
	ln -s "${WWWDIR}/nightly/DMDirc-Setup-${FILEDATA}.run" $WWWDIR/nightly/DMDirc-Setup_latest.run
	ln -s "${WWWDIR}/nightly/DMDirc-Setup-${FILEDATA}.exe" $WWWDIR/nightly/DMDirc-Setup_latest.exe
	cd ${MYDIR}
	/bin/sh $MYDIR/oblong.sh "Nightly Build" "Build Successful";
	/bin/sh $MYDIR/DoReports.sh
else
	/bin/sh $MYDIR/oblong.sh "Nightly Build" "Build Failed"
fi
$SVN revert ${MYDIR}/src/com/dmdirc/Main.java


