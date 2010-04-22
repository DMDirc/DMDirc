#!/bin/sh

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

# Find out where we are
BASEDIR=$(cd "${0%/*}" 2>/dev/null; echo $PWD)

if [ ! -e ${BASEDIR}/.uninstall.conf ]; then
	echo "No .uninstall.conf found, unable to continue."
	exit 1;
else
	INSTALLED_AS_ROOT=""
	INSTALL_LOCATION=""
	
	. ${BASEDIR}/.uninstall.conf

	if [ "${INSTALL_LOCATION}" = "" ]; then
		echo "Unable to read .uninstall.conf, unable to continue."
		exit 1;
	fi;
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

JAVA=`which java`

if [ -e "${BASEDIR}/functions.sh" ]; then
	. ${BASEDIR}/functions.sh
else
	echo "Unable to find functions.sh, unable to continue."
	exit 1;
fi;

if [ "${INSTALLED_AS_ROOT}" -eq 1 ]; then
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
if [ "${INSTALLED_AS_ROOT}" -eq 1 ]; then
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
