#!/bin/sh

SVN=`which svn`
SED=`which sed`
if [ "" = "${SED}" ]; then
	echo "This needs sed."
	exit 0;
fi;

SVNREV=`$SVN info | grep Revision`
SVNREV=${SVNREV##*: }

GNUSED=`sed --version 2>&1 | grep GNU`
if [ "" != "${GNUSED}" ]; then
	PARAM="-r"
else
	PARAM="-E"
fi;

if [ "${1}" = "--pre" ]; then
	# Substitute the version string
	${SED} ${PARAM} 's/int SVN_REVISION = /int SVN_REVISION = '${SVNREV}'; \/\/ /' ${PWD}/src/com/dmdirc/Main.java > ${PWD}/src/com/dmdirc/Main.java.tmp 2>/dev/null
elif [ "${1}" = "--post" ]; then
	# Unsubstitute the version string
	${SED} ${PARAM} 's/int SVN_REVISION = .* ([0-9]+);/int SVN_REVISION = \1;/' ${PWD}/src/com/dmdirc/Main.java > ${PWD}/src/com/dmdirc/Main.java.tmp #2>/dev/null
fi;

if [ -e ${PWD}/src/com/dmdirc/Main.java.tmp ]; then
	mv ${PWD}/src/com/dmdirc/Main.java.tmp ${PWD}/src/com/dmdirc/Main.java
fi;

cat ${PWD}/src/com/dmdirc/Main.java | grep SVN
