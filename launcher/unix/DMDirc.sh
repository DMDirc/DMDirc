#!/bin/sh
#
# This script launches dmdirc and attempts to update the jar file if needed.
#
# DMDirc - Open Source IRC Client
# Copyright (c) 2006-2009 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

LAUNCHERVERSION="11"

params=""

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

# Store params so that we can pass them back to the client
for param in "$@"; do
	PSN=`echo "${param}" | grep "^-psn_"`
	if [ "" = "${PSN}" ]; then
		SPACE=`echo "${param}" | grep " "`
		if [ "${SPACE}" != "" ]; then
			niceParam=`echo "${param}" | sed 's/"/\\\\"/g'`
			params=${params}" \"${niceParam}\""
		else
			params=${params}" ${param}"
		fi;
	fi;
done;

# Check for OS X
OSASCRIPT=`which osascript`
KERNEL=`uname -s`
ISOSX="0"
# Kernel is darwin, and osascript exists, probably OS X!
if [ "${KERNEL}" = "Darwin" -a "" != "${OSASCRIPT}" ]; then
	ISOSX="1"
fi;

# Check for some CLI params
if [ "${ISOSX}" = "1" ]; then
	profiledir="${HOME}/Library/Preferences/DMDirc"
else
	profiledir="${HOME}/.DMDirc/"
fi;

while test -n "$1"; do
	case "$1" in
		--directory|-d)
			shift
			profiledir=${1}
			;;
	esac
	shift
done

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
	KSUDO=`which kdesudo`
	GSUDO=`which gksudo`
fi;

errordialog() {
	# Send message to console.
	echo ""
	echo "-----------------------------------------------------------------------"
	echo "Error: ${1}"
	echo "-----------------------------------------------------------------------"
	echo "${2}"
	echo "-----------------------------------------------------------------------"

	if [ "${ISOSX}" = "1" -a "" != "${OSASCRIPT}" ]; then
		echo "Displaying dialog.."
		${OSASCRIPT} -e 'tell application "System Events"' -e "activate" -e "display dialog \"${1}\n${2}\" buttons {\"Ok\"} with icon stop" -e 'end tell'
	else
		if [ "" != "${ISKDE}" -a "" != "${KDIALOG}" -a "" != "${DISPLAY}" ]; then
			echo "Dialog on Display: ${DISPLAY}"
			${KDIALOG} --title "DMDirc: ${1}" --error "${1}\n\n${2}"
		elif [ "" != "${ISGNOME}" -a "" != "${ZENITY}" -a "" != "${DISPLAY}" ]; then
			echo "Dialog on Display: ${DISPLAY}"
			${ZENITY} --error --title "DMDirc: ${1}" --text "${1}\n\n${2}"
		fi
	fi;
}

messagedialog() {
	# Send message to console.
	echo ""
	echo "-----------------------------------------------------------------------"
	echo "Info: ${1}"
	echo "-----------------------------------------------------------------------"
	echo "${2}"
	echo "-----------------------------------------------------------------------"

	if [ "${ISOSX}" = "1" -a "" != "${OSASCRIPT}" ]; then
		echo "Displaying dialog.."
		${OSASCRIPT} -e 'tell application "System Events"' -e "activate" -e "display dialog \"${1}\n${2}\" buttons {\"Ok\"} giving up after 120 with icon note" -e 'end tell'
	else
		if [ "" != "${ISKDE}" -a "" != "${KDIALOG}" -a "" != "${DISPLAY}" ]; then
			echo "Dialog on Display: ${DISPLAY}"
			${KDIALOG} --title "DMDirc: ${1}" --msgbox "${1}\n\n${2}"
		elif [ "" != "${ISGNOME}" -a "" != "${ZENITY}" -a "" != "${DISPLAY}" ]; then
			echo "Dialog on Display: ${DISPLAY}"
			${ZENITY} --info --title "DMDirc: ${1}" --text "${1}\n\n${2}"
		fi
	fi;
}

getConfigOption() {
	FILE="${profiledir}/dmdirc.config"
	WANTED_DOMAIN="${1}"
	WANTED_KEY="${2}"
	CURRENT_SECTION=""
	if [ "${WANTED_KEY}" != "" -a "${WANTED_DOMAIN}" = "" ]; then
		if [ -e "${FILE}" ]; then
			cat ${FILE} | sed 's/\\/\\\\/g' | while IFS='' read -r LINE; do
				IS_SECTION=`echo ${LINE} | egrep "^.*:$"`
				IS_KEYVALUE=`echo ${LINE} | egrep "^[[:space:]]+.*=.*$"`
				if [ "" != "${IS_SECTION}" ]; then
					CURRENT_SECTION=${LINE%%:*}
				elif [ "" != "${IS_KEYVALUE}" ]; then
					KEY=`echo ${LINE%%=*} | sed 's/^\s*//g'`
					VALUE=${LINE##*=}
					if [ "${WANTED_DOMAIN}" = "${CURRENT_SECTION}" -a "${WANTED_KEY}" = "${KEY}" ]; then
						echo ${VALUE};
					fi;
				fi;
			done;
		fi;
	fi;
}

# LOOKANDFEEL=`getConfigOption "ui" "lookandfeel" | tail -n 1`

if [ "${ISOSX}" = "1" ]; then
	jarDir=`dirname $0`/../Resources/Java/
	jar=${jarDir}DMDirc.jar
else
	jar=`dirname $0`/DMDirc.jar
fi
launcherUpdater=${profiledir}/updateLauncher.sh
BSDJava1="/usr/local/jdk1.6.0/jre/bin/java"
BSDJava2="/usr/local/diablo-jdk1.6.0/jre/bin/java"
echo "---------------------"
echo "DMDirc - Open Source IRC Client"
echo "Launcher Version: ${LAUNCHERVERSION}"
echo "Copyright (c) 2006-2009 Chris Smith, Shane Mc Cormack, Gregory Holmes"
echo "---------------------"
if [ "${ISOSX}" = "1" ]; then
	echo "Running on OS X."
elif [ "${KERNEL}" = "Linux" ]; then
	echo "Running on Linux."
elif [ "`echo ${KERNEL} | grep -i BSD`" != "" ]; then
	echo "Running on BSD."
elif [ "`echo ${KERNEL} | grep -i SunOS`" != "" ]; then
	echo "Running on Solaris."
else
	echo "Running on unknown unix variation: ${KERNEL}."
fi;

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

			if [ "${ISOSX}" = "1" -a "" != "${OSASCRIPT}" ]; then
				echo "Displaying dialog.."
				${OSASCRIPT} -e 'tell application "System Events"' -e "activate" -e "display dialog \"${1}\n${2}\" buttons {\"Ok\"} with icon stop" -e 'end tell'
			else
				if [ "" != "${ISKDE}" -a "" != "${KDIALOG}" -a "" != "${DISPLAY}" ]; then
					echo "Dialog on Display: ${DISPLAY}"
					${KDIALOG} --title "DMDirc: \${1}" --error "\${1}\n\n\${2}"
				elif [ "" != "${ISGNOME}" -a "" != "${ZENITY}" -a "" != "${DISPLAY}" ]; then
					echo "Dialog on Display: ${DISPLAY}"
					${ZENITY} --error --title "DMDirc: \${1}" --text "\${1}\n\n\${2}"
				fi
			fi
		}

		messagedialog() {
			# Send message to console.
			echo ""
			echo "-----------------------------------------------------------------------"
			echo "Info: \${1}"
			echo "-----------------------------------------------------------------------"
			echo "\${2}"
			echo "-----------------------------------------------------------------------"

			if [ "${ISOSX}" = "1" -a "" != "${OSASCRIPT}" ]; then
				echo "Displaying dialog.."
				${OSASCRIPT} -e 'tell application "System Events"' -e "activate" -e "display dialog \"${1}\n${2}\" buttons {\"Ok\"} giving up after 120 with icon note" -e 'end tell'
			else
				if [ "" != "${ISKDE}" -a "" != "${KDIALOG}" -a "" != "${DISPLAY}" ]; then
					echo "Dialog on Display: ${DISPLAY}"
					${KDIALOG} --title "DMDirc: \${1}" --msgbox "\${1}\n\n\${2}"
				elif [ "" != "${ISGNOME}" -a "" != "${ZENITY}" -a "" != "${DISPLAY}" ]; then
					echo "Dialog on Display: ${DISPLAY}"
					${ZENITY} --info --title "DMDirc: \${1}" --text "\${1}\n\n\${2}"
				fi
			fi;
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
				if [ "${ISOSX}" = "1" ]; then
					messagedialog "DMDirc" "The DMDirc Client Updater was unable to modify the client installation, trying again with administrator access"
					if [ $? -eq 0 ]; then
						echo "Password dialog on display"
						osascript -e do shell script "mv -fv \"${profiledir}/.launcher.sh\" \"${0}\"" with administrator privileges
					fi;
				else
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
				fi;
			fi
			if [ ! -e "${profiledir}/.launcher.sh" ]; then
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
			if [ "${ISOSX}" = "1" ]; then
				messagedialog "DMDirc" "The DMDirc Client Updater was unable to modify the client installation, trying again with administrator access"
				if [ $? -eq 0 ]; then
					echo "Password dialog on display"
					osascript -e "do shell script \"mv -fv \\\"${profiledir}/.DMDirc.jar\\\" \\\"${jar}\\\"\" with administrator privileges"
				fi;
			else
				if [ "" != "${ISKDE}" -a "" != "${KSUDO}" -a "" != "${DISPLAY}" ]; then
					echo "Password dialog on ${DISPLAY}"
					${KSUDO} --comment "DMDirc Client Updater requires root access to modify the global installation" -- mv -fv "${profiledir}/.DMDirc.jar" "${jar}"
				elif [ "" != "${ISGNOME}" -a "" != "${GSUDO}" -a "" != "${DISPLAY}" ]; then
					echo "Password dialog on ${DISPLAY}"
					${GSUDO} -k --message "DMDirc Client Updater requires root access to modify the global installation" -- mv -fv "${profiledir}/.DMDirc.jar" "${jar}"
				else
					echo "DMDirc Client Updater requires root access to modify the global installation"
					sudo mv -fv "${profiledir}/.DMDirc.jar" "${jar}"
				fi;
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
if [ "${ISOSX}" = "1" ]; then
	JAVA=`which java`
	if [ -e "/System/Library/Frameworks/JavaVM.framework/Versions/1.6.0/Commands/java" ]; then
		JAVA="/System/Library/Frameworks/JavaVM.framework/Versions/1.6.0/Commands/java"
	elif [ -e "/System/Library/Frameworks/JavaVM.framework/Versions/1.6/Commands/java" ]; then
		JAVA="/System/Library/Frameworks/JavaVM.framework/Versions/1.6/Commands/java"
	fi;
else
	if [ -e "${HOME}/.profile" ]; then
		# Source the profile incase java can't be found otherwise
		. ${HOME}/.profile
	fi;
	JAVA=`which java`
	if [ ! -e "${JAVA}" ]; then
		# Location where ports on FreeBSD/PCBSD installs java6
		# check it first, because it isn't added to the path automatically
		JAVA=${BSDJava1}
		if [ ! -e "${JAVA}" ]; then
			# Try alternative BSD Location
			JAVA=${BSDJava2}
		fi;
	fi;
fi;

if [ "" != "${JAVA}" ]; then
	echo "Success! (${JAVA})"
else
	echo "Failed!"
	ERROR="Sorry, java does not appear to be installed on this machine.";
	ERROR=${ERROR}"\n"
	if [ "${ISOSX}" = "1" ]; then
		ERROR=${ERROR}"\nDMDirc requires a 1.6.0 compatible JVM.";
	else
		ERROR=${ERROR}"\nDMDirc requires a 1.6.0 compatible JVM, you can get one from: http://www.java.com";
		ERROR=${ERROR}"\nor reinstall DMDirc and let the installer install one for you.";
	fi;
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
		FAILED=1
		# If we are on BSD, check to see if there is alternative versions of java
		# than the one in the path.
		if [ "`echo ${KERNEL} | grep -i BSD`" != "" ]; then
			if [ "${JAVA}" != "${BSDJava1}" -a "${JAVA}" != "${BSDJava2}" ]; then
				JAVA=${BSDJava1}
				if [ ! -e "${JAVA}" ]; then
					JAVA=${BSDJava2}
				fi;
				# Now check to see if DMDirc runs again.
				${JAVA} -jar ${jar} --help >/dev/null 2>&1
				if [ $? -eq 0 ]; then
					# It runs!
					FAILED=0
				fi;
			fi;
		fi;
		if [ ${FAILED} -eq 1 ]; then
			echo "Failed."
			ERROR="Sorry, the currently installed version of java is not compatible with DMDirc.";
			ERROR=${ERROR}"\n";
			if [ "${ISOSX}" = "1" ]; then
				ERROR=${ERROR}"\nDMDirc requires a 1.6.0 compatible JVM.";
			else
				ERROR=${ERROR}"\nDMDirc requires a 1.6.0 compatible JVM, you can get one from: http://www.java.com";
				ERROR=${ERROR}"\nor reinstall DMDirc and let the installer install one for you.";
			fi;
			errordialog "Unable to launch dmdirc!" "${ERROR}";
			exit 1;
		fi;
	fi

	# Now we can run the client for real, this allows stderr/stdout output
	# to be seen, and the shell script exits with the correct exit code.
	APPLEOPTS=""
	if [ "${ISOSX}" = "1" ]; then
		APPLEOPTS="${APPLEOPTS} -Djava.library.path=${jarDir}"
		#APPLEOPTS="${APPLEOPTS} -Dcom.apple.mrj.application.growbox.intrudes=false"
		#APPLEOPTS="${APPLEOPTS} -Dcom.apple.mrj.application.live-resize=true"
		APPLEOPTS="${APPLEOPTS} -Dcom.apple.mrj.application.apple.menu.about.name=DMDirc"
		#APPLEOPTS="${APPLEOPTS} -Dapple.awt.showGrowBox=true"
		#APPLEOPTS="${APPLEOPTS} -Dapple.laf.useScreenMenuBar=true"
	fi;
	${JAVA}${APPLEOPTS} -ea -jar ${jar} -l unix-${LAUNCHERVERSION} ${params}
	EXITCODE=${?}
	if [ ${EXITCODE} -eq 42 ]; then
		# The client says we need to up update, rerun ourself before exiting.
		${0} ${params}
	fi;
	exit ${EXITCODE};
else
	echo "Failed.";
	errordialog "Unable to launch dmdirc!" "No jar file found";
fi
