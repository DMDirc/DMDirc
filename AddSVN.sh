#!/bin/sh

SVN=`which svn`
AWK=`which gawk`
TYPE="gawk"
AWK=""
if [ "" = "${AWK}" ]; then
	AWK=`which mawk`
	TYPE="mawk"
	if [ "" = "${AWK}" ]; then
		echo "Unknown awk variation, not running."
		exit 0;
	fi;
fi;

SVNREV=`$SVN info | grep Revision`
SVNREV=${SVNREV##*: }

if [ "${TYPE}" = "mawk" ]; then
	PRE='int SVN_REVISION = '
	POST='int SVN_REVISION = '${SVNREV}';'
	if [ "${1}" = "--pre" ]; then
		POST=${POST}' // ';
	elif [ "${1}" = "--post" ]; then
		POST=${POST}' \/\/ ';
	fi;
elif [ "${TYPE}" = "gawk" ]; then
	PRE='int SVN_REVISION = '
	POST='int SVN_REVISION = '${SVNREV}'; \/\/ '
fi;

OLD=""

if [ "${1}" = "--pre" ]; then
	# Substitute the version string
	OLD=${PRE}
	NEW=${POST}
elif [ "${1}" = "--post" ]; then
	# Unsubstitute the version string
	OLD=${POST}
	NEW=${PRE}
fi;

if [ "" != "${OLD}" ]; then
	${AWK} '{gsub(/'"${OLD}"'/,"'"${NEW}"'");print}' ${PWD}/src/com/dmdirc/Main.java > ${PWD}/src/com/dmdirc/Main.java.tmp 2>/dev/null
	if [ -e ${PWD}/src/com/dmdirc/Main.java.tmp ]; then
		mv ${PWD}/src/com/dmdirc/Main.java.tmp ${PWD}/src/com/dmdirc/Main.java
	fi;
	# cat ${PWD}/src/com/dmdirc/Main.java | grep SVN
fi;