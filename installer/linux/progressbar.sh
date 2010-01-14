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
if [ -n "${PIDOF}" ]; then
	ISKDE=`${PIDOF} -x -s kdeinit kdeinit4`
	ISGNOME=`${PIDOF} -x -s gnome-panel`
else
	ISKDE=`pgrep kdeinit`
	ISGNOME=`pgrep gnome-panel`
fi;

KDIALOG=`which kdialog`
ZENITY=`which zenity`
USEKDIALOG="0";
QDBUS=`which qdbus`
DBUSSEND=`which dbus-send`
DCOP=`which dcop`
KDETYPE=""
if [ "${ISKDE}" != "" -o "${ZENITY}" = "" ]; then
	# Check to see if we have the dcop or dbus binaries needed..
	USEDCOP=`kdialog --help | grep -i dcop`
	if [ "${USEDCOP}" != "" -a "${DCOP}" != "" ]; then
		KDETYPE="dcop"
		USEKDIALOG="1";
	elif [ "${USEDCOP}" = "" -a "${QDBUS}" != "" ]; then
		KDETYPE="qdbus"
		USEKDIALOG="1";
	elif [ "${USEDCOP}" = "" -a "${DBUSSEND}" != "" ]; then
		KDETYPE="dbussend"
		USEKDIALOG="1";
	fi;
fi;

# Check for special watchdog mode.
# In this mode we are used to kill ourself after a certain time limit to make sure we don't hang.
if [ "${1}" = "--watchdog" ]; then
	if [ "" != "${2}" ]; then
		echo "Watchdog for: ${2}"
		sleep 5;
		kill -1 ${2} >/dev/null 2>&1 &
		sleep 5;
		kill -9 ${2} >/dev/null 2>&1 &
		exit 0;
	else
		echo "No PID specified to watch."
		exit 1;
	fi;

	exit 1;
fi;

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
			wasCancelled="false"
			if [ "${KDETYPE}" = "dcop" ]; then
				wasCancelled=`${DCOP} ${progresswindow} wasCancelled`;
			elif [ "${KDETYPE}" = "qdbus" ]; then
				wasCancelled=`${QDBUS} ${progresswindow} org.kde.kdialog.ProgressDialog.wasCancelled`;
			elif [ "${KDETYPE}" = "dbussend" ]; then
				wasCancelled=`${DBUSSEND} --print-reply --dest=${progresswindow} org.kde.kdialog.ProgressDialog.wasCancelled | grep boolean | awk '{print $2}'`;
			fi
			if [ "${wasCancelled}" = "true" ]; then
				break;
			fi;
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
				if [ "${KDETYPE}" = "dcop" ]; then
					${DCOP} ${progresswindow} setProgress ${val}
					returnVal=${?}
				elif [ "${KDETYPE}" = "qdbus" ]; then
					${QDBUS} ${progresswindow} org.freedesktop.DBus.Properties.Set org.kde.kdialog.ProgressDialog value ${val}
					returnVal=${?}
				elif [ "${KDETYPE}" = "dbussend" ]; then
					${DBUSSEND} --print-reply --dest=${progresswindow} org.freedesktop.DBus.Properties.Set string:'org.kde.kdialog.ProgressDialog' string:'value' variant:int32:${val} > /dev/null
					returnVal=${?}
				fi;
				
				if [ ${returnVal} -ne 0 ]; then
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

if [ "" = "${CAPTION}" -o "" = "${FILESIZE}" ]; then
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
EMPTYPIPE="1"
WDPID=""
WD2PID=""
closeProgress() {
	CONTINUE="0"
	if [ "${TYPE}" = "KDE" -a ${retval} != "0" ]; then
		if [ "${KDETYPE}" = "dcop" ]; then
			${DCOP} ${progresswindow} close
		elif [ "${KDETYPE}" = "qdbus" ]; then
			${QDBUS} ${progresswindow} org.kde.kdialog.ProgressDialog.close
		elif [ "${KDETYPE}" = "dbussend" ]; then
			${DBUSSEND} --print-reply --dest=${progresswindow} org.kde.kdialog.ProgressDialog.close > /dev/null
		fi;
	fi;
	echo "Exiting with value: $retval"
	if [ "${WGETPID}" != "" ]; then
		echo "Attempting to kill wget"
		kill -9 ${WGETPID}
	fi;
	
	if [ -e ${PIPE} ]; then
		if [ "${EMPTYPIPE}" = "1" ]; then
			sh ${0} --watchdog ${$} &
			WDPID=${!}
		
			EMPTYPIPE="2"
			echo "Emptying pipe (${PIPE}) this may hang if no data has been written to the pipe."
			cat ${PIPE};
		elif [ "${EMPTYPIPE}" = "2" ]; then
			echo "Helping pipe (${PIPE}) this may hang if nothing is reading from the pipe."
			sh ${0} --watchdog ${$} &
			WD2PID=${!}
			EMPTYPIPE="3"
			echo "." > ${PIPE};
		else
			echo "Ignoring Pipe..."
		fi;
		echo "Killing watchdog 1."
		kill -9 ${WDPID}
		echo "Killing watchdog 2."
		kill -9 ${WD2PID}
		echo "Deleting Pipe ${PIPE}"
		rm -Rf "${PIPE}"
	fi;
	exit $retval;
}
trap 'closeProgress' INT TERM EXIT


# if kdialog exists, and we have a display, and we are not running gnome,
# and either we are running kde or zenity doesn't exist..
if [ "" != "${KDIALOG}" -a "" != "${DISPLAY}" -a "" = "${ISGNOME}" -a "${USEKDIALOG}" = "1" ]; then
	echo "Progress dialog on Display: ${DISPLAY}"
	progresswindow=`${KDIALOG} --title "DMDirc: ${CAPTION}" --progressbar "${CAPTION}" 100`
	if [ "${KDETYPE}" = "dcop" ]; then
		${DCOP} ${progresswindow} setAutoClose true
		${DCOP} ${progresswindow} showCancelButton true
	elif [ "${KDETYPE}" = "qdbus" ]; then
		${QDBUS} ${progresswindow} org.freedesktop.DBus.Properties.Set org.kde.kdialog.ProgressDialog autoClose true
		${QDBUS} ${progresswindow} org.kde.kdialog.ProgressDialog.showCancelButton true
	elif [ "${KDETYPE}" = "dbussend" ]; then
		${DBUSSEND} --print-reply --dest=${progresswindow} org.kde.kdialog.ProgressDialog.showCancelButton boolean:true >/dev/null
		${DBUSSEND} --print-reply --dest=${progresswindow} org.freedesktop.DBus.Properties.Set string:'org.kde.kdialog.ProgressDialog' string:'autoClose' variant:boolean:true > /dev/null
	fi;
	TYPE="KDE"
	readprogress
	CONTINUE="0"
	echo "Progress Bar Complete"
elif [ "" != "${ZENITY}" -a "" != "${DISPLAY}" ]; then
	# Else, if zenity exists and we have a display
	echo "Progress dialog on Display: ${DISPLAY}"
	TYPE="GNOME"
	readprogress | ${ZENITY} --progress --auto-close --auto-kill --title "DMDirc: ${CAPTION}" --text "${CAPTION}"
	CONTINUE="0"
	echo "Progress Bar Complete"
else
	# Else, basic command-line progress
	echo "Progress For: ${CAPTION}"
	echo "-> 0%"
	readprogress
	CONTINUE="0"
	echo ""
	echo "Finished!"
fi
echo "Exiting progressbar"
exit 0;