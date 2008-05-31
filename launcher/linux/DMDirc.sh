#!/bin/sh
#
# This script launches dmdirc and attempts to update the jar file if needed.
#
# DMDirc - Open Source IRC Client
# Copyright (c) 2006-2008 Chris Smith, Shane Mc Cormack, Gregory Holmes
#
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the "Software"), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in
# all copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
# SOFTWARE.

LAUNCHERVERSION="2"

params=""

# Store params so that we can pass them back to the client
for param in "$@"; do
	SPACE=`echo "${param}" | grep " "`
	if [ "${SPACE}" != "" ]; then
		niceParam=`echo "${param}" | sed 's/"/\\\\"/g'`
		params=${params}" \"${niceParam}\""
	else
		params=${params}" ${param}"
	fi;
done;

# Check for some CLI params
profiledir="${HOME}/.DMDirc/"
while test -n "$1"; do
	case "$1" in
		--directory|-d)
			shift
			profiledir=${1}
			;;
	esac
	shift
done

PIDOF=`which pidof`
if [ "${PIDOF}" == "" ]; then
	# For some reason some distros hide pidof...
	if [ -e /sbin/pidof ]; then
		PIDOF=/sbin/pidof
	elif [ -e /usr/sbin/pidof ]; then
		PIDOF=/usr/sbin/pidof
	fi;
fi;

## Helper Functions
if [ "${PIDOF}" == "" ]; then
	ISKDE=`${PIDOF} -x -s kdeinit`
	ISGNOME=`${PIDOF} -x -s gnome-panel`
else
	ISKDE=`ps ux | grep kdeinit | grep -v grep`
	ISGNOME=`ps ux | grep gnome-panel | grep -v grep`
fi.
KDIALOG=`which kdialog`
ZENITY=`which zenity`
KSUDO=`which kdesudo`
GSUDO=`which gksudo`
errordialog() {
	# Send message to console.
	echo ""
	echo "-----------------------------------------------------------------------"
	echo "Error: ${1}"
	echo "-----------------------------------------------------------------------"
	echo "${2}"
	echo "-----------------------------------------------------------------------"

	if [ "" != "${ISKDE}" -a "" != "${KDIALOG}" -a "" != "${DISPLAY}" ]; then
		echo "Dialog on Display: ${DISPLAY}"
		${KDIALOG} --title "DMDirc: ${1}" --error "${1}\n\n${2}"
	elif [ "" != "${ISGNOME}" -a "" != "${ZENITY}" -a "" != "${DISPLAY}" ]; then
		echo "Dialog on Display: ${DISPLAY}"
		${ZENITY} --error --title "DMDirc: ${1}" --text "${1}\n\n${2}"
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

	if [ "" != "${ISKDE}" -a "" != "${KDIALOG}" -a "" != "${DISPLAY}" ]; then
		echo "Dialog on Display: ${DISPLAY}"
		${KDIALOG} --title "DMDirc: ${1}" --msgbox "${1}\n\n${2}"
	elif [ "" != "${ISGNOME}" -a "" != "${ZENITY}" -a "" != "${DISPLAY}" ]; then
		echo "Dialog on Display: ${DISPLAY}"
		${ZENITY} --info --title "DMDirc: ${1}" --text "${1}\n\n${2}"
	fi
}

jar=`dirname $0`/DMDirc.jar
launcherUpdater=${profiledir}/updateLauncher.sh
echo "---------------------"
echo "DMDirc - Open Source IRC Client"
echo "Launcher Version: ${LAUNCHERVERSION}"
echo "Copyright (c) 2006-2008 Chris Smith, Shane Mc Cormack, Gregory Holmes"
echo "---------------------"

echo -n "Checking for launcher updates in ${profiledir} - ";
if [ -e "${profiledir}/.launcher.sh.ignore" ]; then
	rm -Rf "${profiledir}/.launcher.sh.ignore"
	echo "Ignoring!";
elif [ -e "${profiledir}/.launcher.sh" ]; then
	echo "Found!";
	echo "Attempting to update..";

	cat <<EOF> ${launcherUpdater}
		cd `dirname $0`
		errordialog() {
			# Send message to console.
			echo ""
			echo "-----------------------------------------------------------------------"
			echo "Error: \${1}"
			echo "-----------------------------------------------------------------------"
			echo "\${2}"
			echo "-----------------------------------------------------------------------"

			if [ "" != "${ISKDE}" -a "" != "${KDIALOG}" -a "" != "${DISPLAY}" ]; then
				echo "Dialog on Display: ${DISPLAY}"
				${KDIALOG} --title "DMDirc: \${1}" --error "\${1}\n\n\${2}"
			elif [ "" != "${ISGNOME}" -a "" != "${ZENITY}" -a "" != "${DISPLAY}" ]; then
				echo "Dialog on Display: ${DISPLAY}"
				${ZENITY} --error --title "DMDirc: \${1}" --text "\${1}\n\n\${2}"
			fi
		}

		messagedialog() {
			# Send message to console.
			echo ""
			echo "-----------------------------------------------------------------------"
			echo "Info: \${1}"
			echo "----------------------------------------------------------------------"
			echo "\${2}"
			echo "-----------------------------------------------------------------------"

			if [ "" != "${ISKDE}" -a "" != "${KDIALOG}" -a "" != "${DISPLAY}" ]; then
				echo "Dialog on Display: ${DISPLAY}"
				${KDIALOG} --title "DMDirc: \${1}" --msgbox "\${1}\n\n\${2}"
			elif [ "" != "${ISGNOME}" -a "" != "${ZENITY}" -a "" != "${DISPLAY}" ]; then
				echo "Dialog on Display: ${DISPLAY}"
				${ZENITY} --info --title "DMDirc: \${1}" --text "\${1}\n\n\${2}"
			fi
		}

		mv -fv ${profiledir}/.launcher.sh ${0}
		if [ ! -e "${profiledir}/.launcher.sh" ]; then
			echo "Launcher Update successful."
			messagedialog "Launcher Update" "Launcher Update successful"
		else
			if [ "${UID}" = "" ]; then
				UID=`id -u`;
			fi
			if [ "0" != "${UID}" ]; then
				if [ "" != "${ISKDE}" -a "" != "${KSUDO}" -a "" != "${DISPLAY}" ]; then
					echo "Password dialog on ${DISPLAY}"
					${KSUDO} --comment "DMDirc Client Updater requires root access to modify the global installation" -- mv -fv "${profiledir}/.launcher.sh" "${0}"
				elif [ "" != "${ISGNOME}" -a "" != "${GSUDO}" -a "" != "${DISPLAY}" ]; then
					echo "Password dialog on ${DISPLAY}"
					${GSUDO} -k --message "DMDirc Client Updater requires root access to modify the global installation" -- mv -fv "${profiledir}/.launcher.sh" "${0}"
				else
					echo "DMDirc Client Updater requires root access to modify the global installation"
					sudo mv -fv "${profiledir}/.launcher.sh" "${0}"
				fi;
			fi
			if [ ! -e "${profiledir}/.DMDirc.jar" ]; then
				echo "Update successful."
				messagedialog "Launcher Update" "Launcher Update successful"
			else
				echo "Launcher failed."
				errordialog "Launcher Update" "Launcher Update failed, using old version"
				touch ${profiledir}/.launcher.sh.ignore
			fi;
		fi;
		sh ${0} ${params}
EOF
	chmod a+x ${launcherUpdater}
	${launcherUpdater}
	exit 0;
else
	echo "Not found.";
fi;

if [ -e "${launcherUpdater}" ]; then
	rm -Rf "${launcherUpdater}"
fi;

echo -n "Checking for client updates in ${profiledir} - ";
if [ -e "${profiledir}/.DMDirc.jar" ]; then
	echo "Found!";
	echo "Attempting to update..";
	mv -fv ${profiledir}/.DMDirc.jar ${jar}
	if [ ! -e "${profiledir}/.DMDirc.jar" ]; then
		echo "Update successful."
		messagedialog "Client Update" "Client Update successful"
	else
		if [ "${UID}" = "" ]; then
			UID=`id -u`;
		fi
		if [ "0" != "${UID}" ]; then
			if [ "" != "${ISKDE}" -a "" != "${KSUDO}" -a "" != "${DISPLAY}" ]; then
				echo "Password dialog on ${DISPLAY}"
				${KSUDO} --comment "DMDirc Client Updater requires root access to modify the global installation" mv -fv "${profiledir}/.DMDirc.jar" "${jar}"
			elif [ "" != "${ISGNOME}" -a "" != "${GSUDO}" -a "" != "${DISPLAY}" ]; then
				echo "Password dialog on ${DISPLAY}"
				${GSUDO} -k --message "DMDirc Client Updater requires root access to modify the global installation" mv -fv "${profiledir}/.DMDirc.jar" "${jar}"
			else
				echo "DMDirc Client Updater requires root access to modify the global installation"
				sudo mv -fv "${profiledir}/.DMDirc.jar" "${jar}"
			fi;
		fi
		if [ ! -e "${profiledir}/.DMDirc.jar" ]; then
			echo "Update successful."
			messagedialog "Client Update" "Client Update successful"
		else
			echo "Update failed."
			errordialog "Client Update" "Client Update failed, using old version"
		fi;
	fi
else
	echo "Not found.";
fi;

echo -n "Looking for java - ";
JAVA=`which java`

if [ "" != "${JAVA}" ]; then
	echo "Success!"
else
	echo "Failed!"
	# This should in future offer to download and install the JVM automatically.
	ERROR="Sorry, java is not installed on this machine.";
	ERROR=${ERROR}"\n"
	ERROR=${ERROR}"\nDMDirc requires a 1.6.0 compatible JVM, you can get one from:";
	ERROR=${ERROR}"\nhttp://jdl.sun.com/webapps/getjava/BrowserRedirect";
	errordialog "Unable to launch dmdirc!" "${ERROR}";
	exit 1;
fi

echo -n "Running DMDirc - "
if [ -e "${jar}" ]; then
	# Check that DMDirc will run, if java is not 1.6 this will fail.
	# We do it this way otherwise segfaults etc would cause an unable to launch
	# error message to be printed.
	${JAVA} -jar ${jar} --help >/dev/null 2>&1
	if [ $? -ne 0 ]; then
		echo "Failed."
		ERROR="Sorry, the currently installed version of java is not compatible with";
		ERROR=${ERROR}"\nDMDirc.";
		ERROR=${ERROR}"\n";
		ERROR=${ERROR}"\nDMDirc requires a 1.6.0 compatible JVM, you can get one from:";
		ERROR=${ERROR}"\nhttp://jdl.sun.com/webapps/getjava/BrowserRedirect";
		errordialog "Unable to launch dmdirc!" "${ERROR}";
		exit 1;
	fi

	# Now we can run the client for real, this allows stderr/stdout output
	# to be seen, and the shell script exits with the correct exit code.
	${JAVA} -ea -jar ${jar} -l linux-${LAUNCHERVERSION} ${params}
	exit $?;
else
	echo "Failed.";
	errordialog "Unable to launch dmdirc!" "No jar file found";
fi
