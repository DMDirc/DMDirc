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

# This makes sure we have a clean build with no stale class files, it just takes longer
rm -Rf $MYDIR/build $MYDIR/dist

$ANT -buildfile $MYDIR/build.xml -k
if [ -f $MYDIR/dist/DMDirc.jar ]; then
	FILENAME=DMDirc`date +_%Y%m%d`_${SVNREV}.jar
	cp $MYDIR/dist/DMDirc.jar /home/dmdirc/www/nightly/$FILENAME
	if [ -e $WWWDIR/nightly/DMDirc_latest.jar ]; then
		rm $WWWDIR/nightly/DMDirc_latest.jar
	fi
	ln -s $WWWDIR/nightly/$FILENAME $WWWDIR/nightly/DMDirc_latest.jar
	/bin/sh $MYDIR/oblong.sh "Nightly Build" "Build Successful";
	/bin/sh $MYDIR/DoReports.sh
else
	/bin/sh $MYDIR/oblong.sh "Nightly Build" "Build Failed"
fi
$SVN revert ${MYDIR}/src/com/dmdirc/Main.java


