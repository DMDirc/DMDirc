#!/bin/sh
#
# This script downloads a JRE.
#

PIDOF=`which pidof`
if [ "${PIDOF}" = "" ]; then
	# For some reason some distros hide pidof...
	if [ -e /sbin/pidof ]; then
		PIDOF=/sbin/pidof
	elif [ -e /usr/sbin/pidof ]; then
		PIDOF=/usr/sbin/pidof
	fi;
fi;

## Helper Functions
if [ "${PIDOF}" != "" ]; then
	ISKDE=`${PIDOF} -x -s kdeinit`
	ISGNOME=`${PIDOF} -x -s gnome-panel`
else
	ISKDE=`ps ux | grep kdeinit | grep -v grep`
	ISGNOME=`ps ux | grep gnome-panel | grep -v grep`
fi;
KDIALOG=`which kdialog`
ZENITY=`which zenity`
errordialog() {
	# Send message to console.
	echo ""
	echo "-----------------------------------------------------------------------"
	echo "Error: ${1}"
	echo "-----------------------------------------------------------------------"
	echo "${2}"
	echo "-----------------------------------------------------------------------"

	# Now try to use the GUI Dialogs.
	if [ "" != "${ISKDE}" -a "" != "${KDIALOG}" -a "" != "${DISPLAY}" ]; then
		echo "Dialog on Display: ${DISPLAY}"
		${KDIALOG} --title "DMDirc: ${1}" --error "${2}"
	elif [ "" != "${ISGNOME}" -a "" != "${ZENITY}" -a "" != "${DISPLAY}" ]; then
		echo "Dialog on Display: ${DISPLAY}"
		${ZENITY} --error --title "DMDirc: ${1}" --text "${2}"
	fi
}

messagedialog() {
	# Send message to console.
	echo ""
	echo "-----------------------------------------------------------------------"
	echo "Info: ${1}"
	echo "-----------------------------------------------------------------------"
	echo "${2}"
	echo "-----------------------------------------------------------------------"

	# Now try to use the GUI Dialogs.
	if [ "" != "${ISKDE}" -a "" != "${KDIALOG}" -a "" != "${DISPLAY}" ]; then
		echo "Dialog on Display: ${DISPLAY}"
		${KDIALOG} --title "DMDirc: ${1}" --msgbox "${2}"
	elif [ "" != "${ISGNOME}" -a "" != "${ZENITY}" -a "" != "${DISPLAY}" ]; then
		echo "Dialog on Display: ${DISPLAY}"
		${ZENITY} --info --title "DMDirc: ${1}" --text "${2}"
	fi
}

questiondialog() {
	# Send question to console.
	echo ""
	echo "-----------------------------------------------------------------------"
	echo "Question: ${1}"
	echo "-----------------------------------------------------------------------"
	echo "${2}"
	echo "-----------------------------------------------------------------------"

	# Now try to use the GUI Dialogs.
	if [ "" != "${ISKDE}" -a "" != "${KDIALOG}" -a "" != "${DISPLAY}" ]; then
		echo "Dialog on Display: ${DISPLAY}"
		${KDIALOG} --title "DMDirc: ${1}" --yesno "${2}"
	elif [ "" != "${ISGNOME}" -a "" != "${ZENITY}" -a "" != "${DISPLAY}" ]; then
		echo "Dialog on Display: ${DISPLAY}"
		${ZENITY} --question --title "DMDirc: ${1}" --text "${2}"
	else
		echo "Unable to ask question, assuming no."
		return 1;
	fi
}

ARCH=`uname -m`
# This page redirects us to the correct JRE
URL="http://www.dmdirc.com/getjava/linux/`uname -m`"

length=`wget --spider ${URL} 2>&1 | grep "Length:"| awk '{print $2, $3}' | sed 's/,//g'`
actualLength=${length%% *}
niceLength=`echo ${length##* }  | sed 's/[()]//g'`

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
			rm -Rf ${PIPE}
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
	PIPE=`mktemp -p ${PWD} progresspipe.XXXXXXXXXXXXXX`
	/bin/sh ${PWD}/progressbar.sh "Downloading JRE.." ${actualLength} ${PIPE} &
	wget -q ${URL} -O jre.bin &
	wgetpid=${!}
	while [ `ps ${wgetpid} | wc -l` = 2 ]; do
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
	echo "quit" > ${PIPE}
	messagedialog "Download Completed" "Download Completed"
	exit 1;
else
	messagedialog "Download JRE" "JRE Download Canceled"
fi;