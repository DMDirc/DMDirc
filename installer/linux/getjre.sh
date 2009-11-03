#!/bin/sh
#
# This script downloads a JRE.
#

# Check the which command exists, and if so make sure it behaves how we want
# it to...
WHICH=`which which 2>/dev/null`
if [ "" = "${WHICH}" ]; then
	echo "which command not found. Aborting.";
	exit 0;
else
	# Solaris sucks
	BADWHICH=`which /`
	if [ "${BADWHICH}" != "" ]; then
		echo "Replacing bad which command.";
		# "Which" on solaris gives non-empty results for commands that don't exist
		which() {
			OUT=`${WHICH} ${1}`
			if [ $? -eq 0 ]; then
				echo ${OUT}
			else
				echo ""
			fi;
		}
	fi;
fi

PIDOF=`which pidof`
if [ "${PIDOF}" = "" ]; then
	# For some reason some distros hide pidof...
	if [ -e /sbin/pidof ]; then
		PIDOF=/sbin/pidof
	elif [ -e /usr/sbin/pidof ]; then
		PIDOF=/usr/sbin/pidof
	fi;
fi;

if [ -e "functions.sh" ]; then
	. `dirname $0`/functions.sh
else
	echo "Unable to find functions.sh, unable to continue."
	exit 1;
fi;

# Get the JRE.
ARCH=`uname -m`
ISAINFO=`which isainfo`
ISFREEBSD=`uname -s | grep -i FreeBSD`
if [ "${ISAINFO}" != "" ]; then
	# Solaris-ish
	ARCH=`uname -p`
fi;
URL="http://www.dmdirc.com/getjava/`uname -s`/${ARCH}"
if [ "${ISFREEBSD}" != "" ]; then
	RELEASE=`uname -r`
	URL="${URL}/${RELEASE}"
fi;

WGET=`which wget`
FETCH=`which fetch`
CURL=`which curl`
if [ "${WGET}" != "" ]; then
	length=`${WGET} --spider ${URL} 2>&1 | grep "Length:"| awk '{print $2, $3}' | sed 's/,//g'`
	actualLength=${length%% *}
elif [ "${FETCH}" != "" ]; then
	actualLength=`${FETCH} -s ${URL}`
elif [ "${CURL}" != "" ]; then
	length=`${CURL} -# -I ${URL} 2>&1 | grep "Content-Length:"| awk '{print $2}'`
fi;

# Convert the length from Bytes to something user-friendly
if [ ${actualLength} -ge 1048576 ]; then
	niceLength=`echo "scale=2; ${actualLength}/1048576" | bc`"MB"
elif [ ${actualLength} -ge 1024 ]; then
	niceLength=`echo "scale=2; ${actualLength}/1024" | bc`"KB"
else
	niceLength=`echo "scale=2; ${actualLength}/1024" | bc`"B"
fi;

if [ "${actualLength}" = "6" ]; then
	# Unable to download.
	errordialog "Download Failed" "Unable to find JRE for this platform (`uname -s`/${ARCH})."
	exit 1;
fi;

PIPE=""
wgetpid=""
# Make sure wget and the progressbar die if we do.
badclose() {
	if [ "${wgetpid}" != "" ]; then
		kill -9 ${wgetpid}
	fi;
	if [ "${PIPE}" != "" ]; then
		if [ -e ${PIPE} ]; then
			echo "quit" > ${PIPE}
			echo "Closing badly, pipe still exists."
		fi
	fi
}
trap 'badclose' INT TERM EXIT

if [ "" != "${1}" ]; then
	questiondialog "Download JRE" "${1} (Download Size: ${niceLength})"
	result=$?
else
	questiondialog "Download JRE" "Would you like to download the java JRE? (Download Size: ${niceLength})"
	result=$?
fi;
if [ $result -eq 0 ]; then
	PIPE=`mktemp progresspipe.XXXXXXXXXXXXXX`
	if [ "${WGET}" != "" ]; then
		${WGET} -q -O jre.bin ${URL} &
		wgetpid=${!}
	elif [ "${FETCH}" != "" ]; then
		${FETCH} -q -o jre.bin ${URL} &
		wgetpid=${!}
	elif [ "${CURL}" != "" ]; then
		${CURL} -s -o jre.bin ${URL} &
		wgetpid=${!}
	fi;
	/bin/sh ${PWD}/progressbar.sh "Downloading JRE.." ${actualLength} ${PIPE} ${wgetpid} &
	progressbarpid=${!}
	while [ `ps -p ${wgetpid} | wc -l` = 2 ]; do
		SIZE=`ls -l jre.bin | awk '{print $5}'`
		if [ -e ${PIPE} ]; then
			echo "${SIZE}" > ${PIPE}
		else
			kill -9 ${wgetpid}
			errordialog "Download Canceled" "Download Canceled by user"
			exit 1;
		fi;
	done;
	wgetpid=""
	if [ "${ISFREEBSD}" != "" -o "${ISAINFO}" != "" ]; then
		echo "Killing progressbar"
		kill ${progressbarpid}
	fi;
	messagedialog "Download Completed" "Download Completed"
	if [ -e ${PIPE} ]; then
		echo "Deleting Pipe ${PIPE}"
		rm -Rf "${PIPE}"
	fi;
	exit 0;
else
	messagedialog "Download JRE" "JRE Download Canceled"
	exit 1;
fi;
exit 1;