#!/bin/sh

# Path to trunk
MYDIR="/home/dmdirc/google"
# Path to ant binary
ANT="/usr/ant/bin/ant"
# Path to svn binary
SVN="/usr/local/bin/svn"

/bin/sh $MYDIR/oblong.sh "Reports" "Style Report Generation Started";
cd $MYDIR
$SVN update
$ANT -k -buildfile $MYDIR/doreports.xml reports
/bin/sh $MYDIR/oblong.sh "Reports" "Report Generation Complete"
