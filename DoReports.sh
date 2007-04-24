#!/bin/sh

# cron doesn't seem to like doing this iself...
. /etc/profile

# Path to trunk
MYDIR="/home/dmdirc/google"
# Path to ant binary
ANT="/usr/bin/ant"
# Path to svn binary
SVN="/usr/bin/svn"

/bin/sh $MYDIR/oblong.sh "Reports" "Style Report Generation Started";
cd $MYDIR
$SVN update
$ANT -k -buildfile $MYDIR/doreports.xml reports
/bin/sh $MYDIR/oblong.sh "Reports" "Report Generation Complete"
