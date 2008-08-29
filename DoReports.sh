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

# Path to svn binary
SVN="/usr/bin/svn"

# Path to ant binary
ANT="/usr/bin/ant"

# Increase the memory allowed to be used when running stuff
export ANT_OPTS=-Xmx512m

if [ -n "${BAMBOO_INSTALL}" ]; then
	# Running as bamboo, symlink/create needed things to let it find the results
	# of the build
	if [ ! -e ${PWD}/reports ]; then
		ln -s ${MYDIR}/reports
	fi;
	if [ ! -e ${PWD}/build ]; then
		ln -s ${MYDIR}/build
	fi;
fi;

cd $MYDIR

$SVN update

# Anti-Clover stupidness!
rm -Rf ${MYDIR}/.clover
mkdir ${MYDIR}/.clover

if [ "$1" = "--all" ]; then
	$ANT -k clean findbugs
	$ANT -k domostreports
elif [ "$1" = "--findbugs" ]; then
	$ANT -k clean findbugs
else
	$ANT -k clean domostreports
fi

# Run junit issue notifier
PHP=`which php`
if [ -e "${SCRIPTDIR}/junit-failures.php" -a -n "${PHP}" ]; then
	${PHP} -q "${SCRIPTDIR}/junit-failures.php"
fi
