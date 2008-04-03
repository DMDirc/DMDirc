#!/bin/sh
#
# This script launches the dmdirc java-based installer.
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

PIDOF=`which pidof`
if [ "" != "${PIDOF}" ]; then
	ISKDE=`${PIDOF} -x -s kdeinit`
	KDIALOG=`which kdialog`
	ISGNOME=`${PIDOF} -x -s gnome-panel`
	ZENITY=`which zenity`
else
	ISKDE=""
	KDIALOG=""
	ISGNOME=""
	ZENITY=""
fi;

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

UNAME=`uname -a`
isLinux=`echo ${UNAME} | grep -i linux`

echo ""
echo "---------------------"
echo "Setup.sh"
echo "---------------------"
echo -n "Looking for java.. ";
# Location where ports on FreeBSD/PCBSD installs java6
# check it first, because it isn't added to the path automatically
JAVA="/usr/local/jdk1.6.0/jre/bin/java"
if [ ! -e "${JAVA}" ]; then
	JAVA=`which java`
fi

installjre() {
	result=1
	if [ ! -e "jre.bin" ]; then
		message="Would you like to download and install java?"
		if [ "install" != "${1}" ]; then
			message="Java was not detected on your machine. Would you like to download and install it now?" 
		elif [ "upgrade" != "${1}" ]; then
			message="The version of java detected on your machine is not compatible with DMDirc. Would you like to download and install a compatible version now?"
		fi;
		/bin/sh getjre.sh "${message}"
		if [ $? -eq 0 ]; then
			/bin/sh installjre.sh
			result=$?
		fi;
	else
		message="Would you like to install java?"
		if [ "install" != "${1}" ]; then
			message="Java was not detected on your machine. Would you like to install it now?" 
		elif [ "upgrade" != "${1}" ]; then
			message="The version of java detected on your machine is not compatible with DMDirc. Would you like to install a compatible version now?"
		fi;
		/bin/sh installjre.sh  "${message}"
		result=$?
	fi;
	if [ ${result} -ne 0 ]; then
		if [ "upgrade" != "${1}" ]; then
			errordialog "DMDirc Setup" "Sorry, DMDirc setup can not continue without an updated version of java."
		else
			errordialog "DMDirc Setup" "Sorry, DMDirc setup can not continue without java."
		fi;
		exit 1;
	else
		if [ -e ".jrepath" ]; then
			. .jrepath
			JAVA=`which java`
		fi;
	fi;
}

if [ "" != "${JAVA}" ]; then
	echo "Success!"
else
	echo "Failed!"
	if [ "" == "${isLinux}" ]; then
		errordialog "DMDirc Setup" "Sorry, DMDirc setup can not continue without java 6."
		exit 1
	fi;
	installjre "install"
fi

echo "Success!"

if [ "${UID}" = "" ]; then
	UID=`id -u`;
fi
if [ "0" = "${UID}" ]; then
	echo "Running as root.."
	isRoot="--isroot";
else
	echo "Running as user.."
	isRoot="";
fi

showHelp() {
	echo "This will setup DMDirc on a unix based system."
	echo "The following command line arguments are known:"
	echo "---------------------"
	echo "-h, --help                Help information"
	echo "-r, --release [version]   This is a release"
#	echo "-s, --script              Don't use installer.jar (not implemented yet)"
	echo "---------------------"
	exit 0;
}

# Check for some CLI params
scriptOnly="false"
isRelease=""
while test -n "$1"; do
	case "$1" in
		--script|-s)
			scriptOnly="true"
			shift
			;;
		--release|-r)
			shift
			isRelease=${1}
			shift
			;;
		--help|-h)
			showHelp;
			;;
	esac
done

if [ "${isRelease}" != "" ]; then
	isRelease=" --release "${isRelease}
fi

if [ -e "DMDirc.jar" ]; then
	if [ "${scriptOnly}" = "true" ]; then
		echo "Script-only install requested."
	else
		echo "Running installer.."
		${JAVA} -cp DMDirc.jar com.dmdirc.installer.Main ${isRoot}${isRelease}
		if [ $? -ne 0 ]; then
			if [ "" == "${isLinux}" ]; then
				errordialog "DMDirc Setup" "Sorry, DMDirc setup can not continue without java 6."
				exit 1
			fi;
			installjre "upgrade"
			echo "Trying to run installer again.."
			${JAVA} -cp DMDirc.jar com.dmdirc.installer.Main ${isRoot}${isRelease}
			if [ $? -ne 0 ]; then
				exit 1;
			fi;
		fi
		exit 0;
	fi
else
	echo "No installer found!"
fi

## Script-Only install goes here.
echo "Script-Only functionality not implemented."
exit 1;