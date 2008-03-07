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

ISKDE=`pidof -x -s kdeinit`
KDIALOG=`which kdialog`
ISGNOME=`pidof -x -s gnome-panel`
ZENITY=`which zenity`
CAPTION=${1}
FILESIZE=${2}
progresswindow=""
TYPE=""
PIPE="progresspipe_${$}"
retval="1"

readprogress() {
	data=""
	input=""
	while [ -e ${PIPE} ]; do
		if [ "${TYPE}" = "KDE" ]; then
			if [ `dcop ${progresswindow} wasCancelled` = "true" ]; then
				break;
			fi
		fi;
		input=`cat "${PIPE}"`
		if [ "${input}" == "quit" ]; then
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
	if [ "${TYPE}" = "KDE" -a ${retval} != "0" ]; then
		dcop ${progresswindow} close
	fi;
	echo "Deleting ${PIPE}"
	rm -Rf "${PIPE}"
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
elif [ "" != "${ISGNOME}" -a "" != "${ZENITY}" -a "" != "${DISPLAY}" ]; then
	echo "Progress dialog on Display: ${DISPLAY}"
	TYPE="GNOME"
	readprogress | ${ZENITY} --progress --auto-close --auto-kill --title "DMDirc: ${CAPTION}" --text "${CAPTION}"
else
	echo "Progress For: ${CAPTION}"
	echo "-> 0%"
	readprogress
	echo ""
	echo "Finished!"
fi