#!/bin/sh
#
# This script launches the dmdirc java-based installer.
#
# DMDirc - Open Source IRC Client
# Copyright (c) 2006-2007 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

echo ""
echo "---------------------"
echo "Setup.sh"
echo "---------------------"
echo -n "Looking for java.. ";
JAVA=`which java`

if [ "" != "${JAVA}" ]; then
	echo "Success!"
else
	echo "Failed!"
	# This should in future offer to download and install the JVM automatically.
	echo ""
	echo "-----------------------------------------------------------------------"
	echo "Unable to complete setup!"
	echo "-----------------------------------------------------------------------"
	echo "Sorry, java is not installed on this machine."
	echo ""
	echo "DMDirc requires a 1.6.0 compatible JVM, you can get one from:"
	echo "http://jdl.sun.com/webapps/getjava/BrowserRedirect"
	echo "-----------------------------------------------------------------------"
	exit 1;
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
	echo "-s, --script              Don't use installer.jar (not implemented yet)"
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

if [ -e "installer.jar" ]; then
	if [ "${scriptOnly}" = "true" ]; then
		echo "Script-only install requested."
	else
		echo "Running installer.."
		${JAVA} -cp DMDirc.jar -jar installer.jar ${isRoot}${isRelease}
		if [ $? -ne 0 ]; then
			echo ""
			echo "-----------------------------------------------------------------------"
			echo "Unable to complete setup!"
			echo "-----------------------------------------------------------------------"
			echo "Sorry, the currently installed version of java is not compatible with"
			echo "DMDirc."
			echo ""
			echo "DMDirc requires a 1.6.0 compatible JVM, you can get one from:"
			echo "http://jdl.sun.com/webapps/getjava/BrowserRedirect"
			echo "-----------------------------------------------------------------------"
			exit 1;
		fi
		exit 0;
	fi
else 
	echo "No installer found!"
fi

## Script-Only install goes here.
echo "Script-Only functionality not implemented."
exit 1;