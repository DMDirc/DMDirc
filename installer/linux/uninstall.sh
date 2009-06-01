#!/bin/sh

if [ ! -e .uninstall.conf ]; then
	echo "No .uninstall.conf found, unable to continue."
	exit 1;
else
	INSTALLED_AS_ROOT=""
	INSTALL_LOCATION=""
	
	. .uninstall.conf
fi;

PIDOF=`which pidof`
if [ -z "${PIDOF}" ]; then
	# For some reason some distros hide pidof...
	if [ -e /sbin/pidof ]; then
		PIDOF=/sbin/pidof
	elif [ -e /usr/sbin/pidof ]; then
		PIDOF=/usr/sbin/pidof
	fi;
fi;

## Helper Functions
if [ -n "${PIDOF}" ]; then
	ISKDE=`${PIDOF} -x -s kdeinit`
	ISGNOME=`${PIDOF} -x -s gnome-panel`
else
	ISKDE=`pgrep kdeinit`
	ISGNOME=`pgrep gnome-panel`
fi;

KDIALOG=`which kdialog`
ZENITY=`which zenity`
DIALOG=`which dialog`
JAVA=`which java`

messagedialog() {
	# Send message to console.
	echo ""
	echo "-----------------------------------------------------------------------"
	echo "DMDirc: ${1}"
	echo "-----------------------------------------------------------------------"
	echo "${2}"
	echo "-----------------------------------------------------------------------"

	if [ "" != "${ISKDE}" -a "" != "${KDIALOG}" -a "" != "${DISPLAY}" ]; then
		echo "Dialog on Display: ${DISPLAY}"
		${KDIALOG} --title "DMDirc: ${1}" --msgbox "${2}"
	elif [ "" != "${ISGNOME}" -a "" != "${ZENITY}" -a "" != "${DISPLAY}" ]; then
		echo "Dialog on Display: ${DISPLAY}"
		${ZENITY} --info --title "DMDirc: ${1}" --text "${2}"
	elif [ "" != "${DIALOG}" ]; then
		${DIALOG} --title "DMDirc: ${1}" --msgbox "${2}" 8 40
	fi
}

questiondialog() {
	# Send question to console.
	echo ""
	echo "-----------------------------------------------------------------------"
	echo "DMDirc: ${1}"
	echo "-----------------------------------------------------------------------"
	echo "${2}"
	echo "-----------------------------------------------------------------------"

	if [ "" != "${ISKDE}" -a "" != "${KDIALOG}" -a "" != "${DISPLAY}" ]; then
		echo "Dialog on Display: ${DISPLAY}"
		${KDIALOG} --title "DMDirc: ${1}" --yesno "${2}"
	elif [ "" != "${ISGNOME}" -a "" != "${ZENITY}" -a "" != "${DISPLAY}" ]; then
		echo "Dialog on Display: ${DISPLAY}"
		${ZENITY} --question --title "DMDirc: ${1}" --text "${2}"
	elif [ "" != "${DIALOG}" ]; then
		${DIALOG} --title "DMDirc: ${1}" --yesno "${2}" 8 40
	else
		echo "Unable to show Dialog for question, assuming no"
		return 1
	fi
}

errordialog() {
	# Send error to console.
	echo ""
	echo "-----------------------------------------------------------------------"
	echo "[Error] DMDirc: ${1}"
	echo "-----------------------------------------------------------------------"
	echo "${2}"
	echo "-----------------------------------------------------------------------"

	if [ "" != "${ISKDE}" -a "" != "${KDIALOG}" -a "" != "${DISPLAY}" ]; then
		echo "Dialog on Display: ${DISPLAY}"
		${KDIALOG} --title "DMDirc: ${1}" --error "${2}"
	elif [ "" != "${ISGNOME}" -a "" != "${ZENITY}" -a "" != "${DISPLAY}" ]; then
		echo "Dialog on Display: ${DISPLAY}"
		${ZENITY} --error --title "DMDirc: ${1}" --text "${2}"
	elif [ "" != "${DIALOG}" ]; then
		${DIALOG} --title "[Error] DMDirc: ${1}" --msgbox "${2}" 8 40
	fi
}

if [ ${INSTALLED_AS_ROOT} -eq 1 ]; then
	USER=`whoami`
	if [ "${USER}" != "root" ]; then
		errordialog "Uninstaller" "Uninstall Aborted. Only root can use this script"
		exit 1;
	fi
fi

questiondialog "Uninstaller" "Are you sure you want to uninstall DMDirc?"
if [ $? -ne 0 ]; then
	messagedialog "Uninstaller" "Uninstall Aborted."
	echo "Uninstall Aborted"
	exit 1;
fi

${JAVA} -jar ${INSTALL_LOCATION}/DMDirc.jar -k

if [ $? -eq 0 ]; then
	errordialog "Uninstaller" "Uninstall Aborted - DMDirc is still running. Please close DMDirc before continuing"
	echo "Uninstall Aborted - DMDirc already running."
	exit 1;
fi

echo "Uninstalling DMDirc"
echo "Removing Shortcuts.."

TOOL=`which gconftool-2`
COMMAND=""
FILENAME=""
if [ ${INSTALLED_AS_ROOT} -eq 1 ]; then
	COMMAND="${TOOL} --config-source=`${TOOL} --get-default-source`"
	FILENAME="/usr/share/services/irc.protocol"
	rm -Rfv /usr/share/applications/DMDirc.desktop
else
	COMMAND="${TOOL}"
	FILENAME="${HOME}/.kde/share/services/irc.protocol"
	rm -Rfv ${HOME}/.local/share/applications/DMDirc.desktop
	rm -Rfv ${HOME}/Desktop/DMDirc.desktop
fi;

if [ "${TOOL}" != "" ]; then
	CURRENT=`${COMMAND} --get /desktop/gnome/url-handlers/irc/command`
	if [ "${CURRENT}" = "\"${INSTALL_LOCATION}/DMDirc.sh\" -e -c %s" ]; then
		echo "Removing Gnome Protocol Handler"
		${COMMAND} --unset /desktop/gnome/url-handlers/irc/enabled
		${COMMAND} --unset /desktop/gnome/url-handlers/irc/command
	else
		echo "Not Removing Gnome Protocol Handler"
	fi
fi

if [ -e "${FILENAME}" ]; then
	CURRENT=`grep DMDirc ${FILENAME}`
	if [ "" != "${CURRENT}" ]; then
		echo "Removing KDE Protocol Handler"
		rm -Rfv ${FILENAME}
	else
		echo "Not Removing KDE Protocol Handler"
	fi
fi

echo "Removing Installation Directory"

rm -Rfv ${INSTALL_LOCATION}

PROFILEDIR="${HOME}/.DMDirc"

if [ -e ${PROFILEDIR}/dmdirc.config ]; then
	questiondialog "Uninstaller" "A DMDirc profile has been detected (${PROFILEDIR}) Do you want to delete it as well?"
	if [ $? -eq 0 ]; then
		rm -Rfv "${PROFILEDIR}"
	fi
fi

messagedialog "Uninstaller" "DMDirc Uninstalled Successfully"

echo "Done."

exit 0;
