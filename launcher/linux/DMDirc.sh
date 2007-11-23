#!/bin/sh
#
# This script launches dmdirc and attempts to update the jar file if needed.
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

params="${@}"

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

jar=`dirname $0`/DMDirc.jar

echo "---------------------"
echo "DMDirc - Open Source IRC Client"
echo "Copyright (c) 2006-2007 Chris Smith, Shane Mc Cormack, Gregory Holmes"
echo "---------------------"
echo -n "Checking for updates in ${profiledir} - ";
if [ -e "${profiledir}/.DMDirc.jar" ]; then
	echo "Found!";
	echo "Attempting to update..";
	mv -Rfv ${profiledir}/.DMDirc.jar ${jar}
	if [ $? -eq 0 ]; then
		echo "Update successful."
	else
		echo "Update failed."
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
	echo ""
	echo "-----------------------------------------------------------------------"
	echo "Unable to launch dmdirc!"
	echo "-----------------------------------------------------------------------"
	echo "Sorry, java is not installed on this machine."
	echo ""
	echo "DMDirc requires a 1.6.0 compatible JVM, you can get one from:"
	echo "http://jdl.sun.com/webapps/getjava/BrowserRedirect"
	echo "-----------------------------------------------------------------------"
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
		echo ""
		echo "-----------------------------------------------------------------------"
		echo "Unable to launch DMDirc!"
		echo "-----------------------------------------------------------------------"
		echo "Sorry, the currently installed version of java is not compatible with"
		echo "DMDirc."
		echo ""
		echo "DMDirc requires a 1.6.0 compatible JVM, you can get one from:"
		echo "http://jdl.sun.com/webapps/getjava/BrowserRedirect"
		echo "-----------------------------------------------------------------------"
		exit 1;
	fi
	
	# Now we can run the client for real, this allows stderr/stdout output
	# to be seen, and the shell script exits with the correct exit code.
	${JAVA} -jar ${jar} ${params}
	exit $?;
else 
	echo "Failed: No jar file found!"
fi