#!/bin/sh

# cron doesn't seem to like doing this iself...
. /etc/profile

# Path to WWW Directory
WWWDIR="/home/dmdirc/www"
# Path to trunk
MYDIR="/home/dmdirc/google"
# Path to ant binary
ANT="/usr/ant/bin/ant"
# Path to svn binary
SVN="/usr/local/bin/svn"

/bin/sh $MYDIR/oblong.sh "Nightly Build" "Build Started"
rm -Rf $MYDIR/dist
rm -Rf $MYDIR/build

cd $MYDIR/
$SVN update
$ANT -buildfile $MYDIR/build.xml -k
if [ -f $MYDIR/dist/DMDirc.jar ]; then
	FILENAME=DMDirc`date +_%Y%m%d`.jar
	cp $MYDIR/dist/DMDirc.jar /home/dmdirc/www/nightly/$FILENAME
	cp $MYDIR/dist/lib/swing-layout-1.0.jar /home/dmdirc/www/nightly/lib
	if [ -e $WWWDIR/nightly/DMDirc_latest.jar ]; then
		rm $WWWDIR/nightly/DMDirc_latest.jar
	fi
	ln -s $WWWDIR/nightly/$FILENAME $WWWDIR/nightly/DMDirc_latest.jar
	/bin/sh $MYDIR/oblong.sh "Nightly Build" "Build Successful";
	/bin/sh $MYDIR/DoReports.sh
else
	/bin/sh $MYDIR/oblong.sh "Nightly Build" "Build Failed"
fi


