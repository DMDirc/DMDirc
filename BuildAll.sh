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
awk '{gsub(/public static final String VERSION = "SVN";/,"public static final String VERSION = \"Nightly - SVN Rev: '${SVNREV}'\";");print}' ${MYDIR}/src/uk/org/ownage/dmdirc/Main.java > ${MYDIR}/src/uk/org/ownage/dmdirc/Main.java.tmp
mv ${MYDIR}/src/uk/org/ownage/dmdirc/Main.java.tmp ${MYDIR}/src/uk/org/ownage/dmdirc/Main.java
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
$SVN revert ${MYDIR}/src/uk/org/ownage/dmdirc/Main.java


