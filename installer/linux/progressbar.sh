#!/bin/sh
#
# Show a progress bar.
#
# If kdialog/zenity are available they are used.
# Progress data is read from the created pipe, echoing "quit" to the pipe will 
# cause the script to terminate, as will the progress reaching 100%
#
# Pressing cancel on a zenity dialog will terminate right away, kdialog will
# only terminate when the next line of data is read from the pipe.
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
# Solaris also has a crappy version of tail!
tail -n +1 /dev/null >/dev/null 2>&1
if [ $? -eq 2 ]; then
	TAILOPTS="-"
else
	TAILOPTS="-n"
fi;

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
	ISKDE=`ps -Af | grep kdeinit | grep -v grep`
	ISGNOME=`ps -Af | grep gnome-panel | grep -v grep`
fi;
KDIALOG=`which kdialog`
ZENITY=`which zenity`
CAPTION=${1}
FILESIZE=${2}
WGETPID=${3}
progresswindow=""
TYPE=""
PIPE="progresspipe_${$}"
retval="1"
CONTINUE="1"

readprogress() {
	data=""
	input=""
	while [ ${CONTINUE} -eq "1" -a -e ${PIPE} ]; do
		if [ "${TYPE}" = "KDE" ]; then
			if [ `dcop ${progresswindow} wasCancelled` = "true" ]; then
				break;
			fi
		fi;
		input=`cat "${PIPE}" | tail ${TAILOPTS}1`
		if [ "${input}" = "quit" ]; then
			break;
		elif [ "${input}" != "" ]; then
			data=${input}
			input=""
			res=`echo "scale=4 ; (${data}/${FILESIZE})*100" | bc`
			val=${res%%.*}
			if [ "${val}" = "" ]; then
				val=0
			fi;
			if [ "${TYPE}" = "KDE" ]; then
				dcop ${progresswindow} setProgress ${val}
				if [ ${?} -ne 0 ]; then
					break;
				fi;
			elif [ "${TYPE}" = "GNOME" ]; then
				echo ${val}
				if [ $? -ne 0 ] ; then
					break;
				fi
			else
				if [ $((${val} % 2)) -eq 0 ]; then
					echo "-> "${val}"%"
				fi;
			fi;
			if [ "${val}" = "100" ]; then
				retval="0"
				CONTINUE="0"
				break;
			fi;
		fi;
	done;
}

if [ "" = "${CAPTION}" -o "" = ${FILESIZE} ]; then
	echo "Insufficient Parameters."
	echo "Usage: ${0} <caption> <totalvalue> [pipename]"
	exit;
fi;

if [ "" != "${3}" ]; then
	# Assume pipe name is what we want, delete existing file.
	PIPE=${3}
	rm -Rf ${PIPE}
else
	# Make sure we get a file that doesn't already exist, keep appending our
	# pid untill we get a file that hasn't been taken.
	while [ -e "${PIPE}" ]; do
		PIPE="${PIPE}_${$}"
	done;
fi;

echo "Using pipe: "${PIPE}
mkfifo "${PIPE}"
closeProgress() {
	CONTINUE="0"
	if [ "${TYPE}" = "KDE" -a ${retval} != "0" ]; then
		dcop ${progresswindow} close
	fi;
	echo "Exiting with value: $retval"
	if [ -e ${PIPE} ]; then
		echo "Attempting to kill wget"
		kill -9 ${WGETPID}
		echo "Emptying pipe"
		cat ${PIPE};
		echo "Deleting Pipe ${PIPE}"
		rm -Rf "${PIPE}"
	fi;
	exit $retval;
}
trap 'closeProgress' INT TERM EXIT

if [ "" != "${ISKDE}" -a "" != "${KDIALOG}" -a "" != "${DISPLAY}" ]; then
	echo "Progress dialog on Display: ${DISPLAY}"
	progresswindow=`${KDIALOG} --title "DMDirc: ${CAPTION}" --progressbar "${CAPTION}" 100`
	dcop ${progresswindow} setAutoClose true
	dcop ${progresswindow} showCancelButton true
	TYPE="KDE"
	readprogress
	CONTINUE="0"
	echo "Progress Bar Complete"
elif [ "" != "${ISGNOME}" -a "" != "${ZENITY}" -a "" != "${DISPLAY}" ]; then
	echo "Progress dialog on Display: ${DISPLAY}"
	TYPE="GNOME"
	readprogress | ${ZENITY} --progress --auto-close --auto-kill --title "DMDirc: ${CAPTION}" --text "${CAPTION}"
	CONTINUE="0"
	echo "Progress Bar Complete"
else
	echo "Progress For: ${CAPTION}"
	echo "-> 0%"
	readprogress
	CONTINUE="0"
	echo ""
	echo "Finished!"
fi
echo "Exiting progressbar"
exit 0;