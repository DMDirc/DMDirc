#!/bin/sh

SVN=`which svn`

SVNREV=`$SVN info | grep Revision`
SVNREV=${SVNREV##*: }

PRE='int SVN_REVISION = '
POST='int SVN_REVISION = '${SVNREV}'; \/\/ '

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
	awk '{gsub(/'"${OLD}"'/,"'"${NEW}"'");print}' ${PWD}/src/com/dmdirc/Main.java > ${PWD}/src/com/dmdirc/Main.java.tmp 2>/dev/null
	mv ${PWD}/src/com/dmdirc/Main.java.tmp ${PWD}/src/com/dmdirc/Main.java
fi;