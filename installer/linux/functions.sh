#!/bin/sh

# Check for OS X
OSASCRIPT=`which osascript`
KERNEL=`uname -s`
ISOSX="0"
# Kernel is darwin, and osascript exists, probably OS X!
if [ "${KERNEL}" = "Darwin" -a "" != "${OSASCRIPT}" ]; then
	ISOSX="1"
fi;

if [ "${ISOSX}" != "1" ]; then
	PIDOF=`which pidof`
	if [ "${PIDOF}" = "" ]; then
		# For some reason some distros hide pidof...
		if [ -e /sbin/pidof ]; then
			PIDOF=/sbin/pidof
		elif [ -e /usr/sbin/pidof ]; then
			PIDOF=/usr/sbin/pidof
		fi;
	fi;
	
	PGREP=`which pgrep`
	if [ "${PIDOF}" != "" ]; then
		ISKDE=`${PIDOF} -x -s kdeinit kdeinit4`
		ISGNOME=`${PIDOF} -x -s gnome-panel`
	elif [ "${PGREP}" != "" ]; then
		ISKDE=`pgrep kdeinit`
		ISGNOME=`pgrep gnome-panel`
	else
		ISKDE=`ps -Af | grep kdeinit | grep -v grep`
		ISGNOME=`ps -Af | grep gnome-panel | grep -v grep`
	fi;
	KDIALOG=`which kdialog`
	ZENITY=`which zenity`
	DIALOG=`which dialog`
	
	KSUDO=`which kdesudo`
	GSUDO=`which gksudo`
fi;

if [ "${ISKDE}" != "" -o "${ZENITY}" = "" ]; then
	USEKDIALOG="1";
else
	USEKDIALOG="0";
fi;

messagedialog() {
	# Send message to console.
	echo ""
	echo "-----------------------------------------------------------------------"
	echo "DMDirc: ${1}"
	echo "-----------------------------------------------------------------------"
	echo "${2}"
	echo "-----------------------------------------------------------------------"


	# If we are on OSX
	if [ "${ISOSX}" = "1" -a "" != "${OSASCRIPT}" ]; then
		echo "Displaying dialog.."
		${OSASCRIPT} -e 'tell application "System Events"' -e "activate" -e "display dialog \"${1}\n${2}\" buttons {\"Ok\"} giving up after 120 with icon note" -e 'end tell'
	elif [ "" != "${KDIALOG}" -a "" != "${DISPLAY}" -a "" = "${ISGNOME}" -a "${USEKDIALOG}" = "1" ]; then
		# else if kdialog exists, and we have a display, and we are not running
		# gnome, and either we are running kde or zenity doesn't exist..
		echo "Dialog on Display: ${DISPLAY}"
		${KDIALOG} --title "DMDirc: ${1}" --msgbox "${2}"
	elif [ "" != "${ZENITY}" -a "" != "${DISPLAY}" ]; then
		# Else, if zenity exists and we have a display
		echo "Dialog on Display: ${DISPLAY}"
		${ZENITY} --info --title "DMDirc: ${1}" --text "${2}"
	elif [ "" != "${DIALOG}" ]; then
		# Else, if dialog exists and we have a display
		${DIALOG} --title "DMDirc: ${1}" --msgbox "${2}" 8 40 
	fi;
}

questiondialog() {
	# Send question to console.
	echo ""
	echo "-----------------------------------------------------------------------"
	echo "DMDirc: ${1}"
	echo "-----------------------------------------------------------------------"
	echo "${2}"
	echo "-----------------------------------------------------------------------"

	# If we are on OSX
	if [ "${ISOSX}" = "1" -a "" != "${OSASCRIPT}" ]; then
		echo "Displaying dialog.."
		${OSASCRIPT} -e 'tell application "System Events"' -e "activate" -e "display dialog \"Line 1\nLine 2\" with icon note" -e 'end tell'
	elif [ "" != "${KDIALOG}" -a "" != "${DISPLAY}" -a "" = "${ISGNOME}" -a "${USEKDIALOG}" = "1" ]; then
		# else if kdialog exists, and we have a display, and we are not running
		# gnome, and either we are running kde or zenity doesn't exist..
		echo "Dialog on Display: ${DISPLAY}"
		${KDIALOG} --title "DMDirc: ${1}" --yesno "${2}"
	elif [ "" != "${ZENITY}" -a "" != "${DISPLAY}" ]; then
		# Else, if zenity exists and we have a display
		echo "Dialog on Display: ${DISPLAY}"
		${ZENITY} --question --title "DMDirc: ${1}" --text "${2}"
	elif [ "" != "${DIALOG}" ]; then
		# Else, if dialog exists and we have a display
		${DIALOG} --title "DMDirc: ${1}" --yesno "${2}" 8 40
	elif [ "${3}" != "" ]; then
		# Else, fail and return default.
		echo "Unable to ask question, assuming '${3}'."
		return ${3};
	else
		# Else, fail and return no.
		echo "Unable to ask question, assuming no."
		return 1;
	fi;
}

errordialog() {
	# Send error to console.
	echo ""
	echo "-----------------------------------------------------------------------"
	echo "[Error] DMDirc: ${1}"
	echo "-----------------------------------------------------------------------"
	echo "${2}"
	echo "-----------------------------------------------------------------------"

	# If we are on OSX
	if [ "${ISOSX}" = "1" -a "" != "${OSASCRIPT}" ]; then
		echo "Displaying dialog.."
		${OSASCRIPT} -e 'tell application "System Events"' -e "activate" -e "display dialog \"${1}\n${2}\" buttons {\"Ok\"} with icon stop" -e 'end tell'
	elif [ "" != "${KDIALOG}" -a "" != "${DISPLAY}" -a "" = "${ISGNOME}" -a "${USEKDIALOG}" = "1" ]; then
		# else if kdialog exists, and we have a display, and we are not running
		# gnome, and either we are running kde or zenity doesn't exist..
		echo "Dialog on Display: ${DISPLAY}"
		${KDIALOG} --title "DMDirc: ${1}" --error "${2}"
	elif [ "" != "${ZENITY}" -a "" != "${DISPLAY}" ]; then
		# Else, if zenity exists and we have a display
		echo "Dialog on Display: ${DISPLAY}"
		${ZENITY} --error --title "DMDirc: ${1}" --text "${2}"
	elif [ "" != "${DIALOG}" ]; then
		# Else, if dialog exists and we have a display
		${DIALOG} --title "[Error] DMDirc: ${1}" --msgbox "${2}" 8 40
	fi
}