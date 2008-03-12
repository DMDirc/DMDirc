#!/bin/sh

# cron doesn't do this
. /etc/profile
. ${HOME}/.bashrc

# Used for the report.log to help diagnosing problems
env

# Path to trunk
MYDIR="/home/dmdirc/google"
# Path to scripts
SCRIPTDIR="/home/dmdirc/scripts"
# Path to ant binary
ANT="/usr/bin/ant"
# Path to svn binary
SVN="/usr/bin/svn"

# Increase the memory allowed to be used when running stuff
export ANT_OPTS=-Xmx256m

#/bin/sh $MYDIR/oblong.sh "Reports" "Style Report Generation Started";
cd $MYDIR
$SVN update
$ANT clean
if [ "$1" = "--all" ]; then
	$ANT -k -buildfile $MYDIR/doreports.xml doallreports
elif [ "$1" = "--findbugs" ]; then
	$ANT -k -buildfile $MYDIR/style_build.xml findbugs
else
	$ANT -k -buildfile $MYDIR/doreports.xml domostreports
fi

# Run junit issue notifier
PHP=`which php`
if [ -e "$SCRIPTDIR/junit-failures.php" -a "${PHP}" != "" ]; then
	$PHP -q $SCRIPTDIR/junit-failures.php
fi

# Oblong junit announcement
LINE=`cat junitreports/overview-summary.html | grep "%</td"`
PASSRATE=`expr "$LINE" : '.*<td>\(.*\)%</td>'`
if [ "${PASSRATE}" = "" ]; then
	/bin/sh $MYDIR/oblong.sh "Reports" "Report Generation Complete (Junit tests failed to run)"
else
	/bin/sh $MYDIR/oblong.sh "Reports" "Report Generation Complete (Junit Pass Rate: ${PASSRATE}%)"
fi
