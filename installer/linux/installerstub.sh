#!/bin/sh
#
# This script installs dmdirc
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

MD5=""

# Check the which command exists
WHICH=`which`
if [ "" != "${WHICH}" ]; then
	echo "which command not found. Aborting.";
	exit 0;
fi


###ADDITIONAL_STUFF###

errordialog() {
	# Send message to console.
	echo ""
	echo "-----------------------------------------------------------------------"
	echo "Error: ${1}"
	echo "-----------------------------------------------------------------------"
	echo "${2}"
	echo "-----------------------------------------------------------------------"

	# Now try to use the GUI Dialogs.
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
	if [ "${PIDOF}" = "" ]; then
		ISKDE=`${PIDOF} -x -s kdeinit`
		ISGNOME=`${PIDOF} -x -s gnome-panel`
	else
		ISKDE=`ps ux | grep kdeinit | grep -v grep`
		ISGNOME=`ps ux | grep gnome-panel | grep -v grep`
	fi;
	KDIALOG=`which kdialog`
	ZENITY=`which zenity`
	if [ "" != "${ISKDE}" -a "" != "${KDIALOG}" -a "" != "${DISPLAY}" ]; then
		echo "Dialog on Display: ${DISPLAY}"
		${KDIALOG} --title "DMDirc: ${1}" --error "${1}\n\n${2}"
	elif [ "" != "${ISGNOME}" -a "" != "${ZENITY}" -a "" != "${DISPLAY}" ]; then
		echo "Dialog on Display: ${DISPLAY}"
		${ZENITY} --error --title "DMDirc: ${1}" --text "${1}\n\n${2}"
	fi
}

# Location of .run stub end
ENDLINE=`grep -na "^###END INCLUDE###$" $0`
ENDLINE=$((${ENDLINE%%:*} + 1))

if [ "" = "${ENDLINE}" ]; then
	errordialog "DMDirc Setup" "End of stub not found. Aborting.";
	exit 0;
fi

# Attempt to get a name for a random dir in /tmp
random() {
	# First off, lets try mktemp.
	MKTEMP=`which mktemp`
	if [ "" != "${MKTEMP}" ]; then
		# mktemp exists \o
		DIR=`${MKTEMP} -d /tmp/DMDirc.XXXXXX`
		eval "$1=${DIR}"
		return;
	fi

	TMPDIR=${TMPDIR:=/tmp}
	BASEDIR=${TMPDIR}dmdirc_`whoami`_
	DIR=${PWD}
	while [ -d "${DIR}" ]; do
		if [ "" != "${RANDOM}" ]; then
			# Bash has a psuedo random number generator that can be accessed
			# using ${RANDOM}
			RND=${RANDOM}${RANDOM}${RANDOM}
		else
			# Dash (the ubuntu default sh) however does not.
			# od and awk however usually exist, and we can get a random number
			# from /dev/urandom or /dev/random
			OD=`which od`
			AWK=`which awk`
			RND=""
			if [ "" != "$OD" -a "" != "$AWK" ]; then
				# Try /dev/urandom first
				RAND=/dev/urandom
				# If it doesn't exist try /dev/random
				if [ ! -e "${RAND}" ]; then
					RAND=/dev/urandom;
				fi
				# This makes sure we only try to read if one exists
				if [ ! -e "${RAND}" ]; then
					RND=$(head -1 ${RAND} | od -N 3 -t u | awk '{ print $2 }')
				fi;
			fi;

			# No random number was generated, getting to here means that
			# ${RANDOM} doesn't exist, /dev/random doesn't exist, /dev/urandom doesn't exist
			# or that od/awk don't exist. Annoying.
			# Try using this processes PID instead!
			if [ "${RND}" = "" ]; then
				RND=$$
				DIR=${BASEDIR}${RND}
				if [ -e "${DIR}" ]; then
					# Lets hope this never happens.
					errordialog "DMDirc Setup" "Unable to create random directory";
					exit 0;
				fi;
			fi;
		fi;
		DIR=${BASEDIR}${RND}
	done
	mkdir ${DIR}
	eval "$1=${DIR}"
}

uncompress() {
	tail -n +${ENDLINE} "${OLDPWD}/$0" | gzip -cd | tar -xvf - 2>/dev/null || {
		echo "Decompression failed."
		kill -15 $$;
	};
}

showHelp() {
	echo "This will install DMDirc on a unix based system."
	echo "The following command line arguments are known:"
	echo "---------------------"
	echo "-h, --help        Help information"
	echo "-e, --extract     Extract .run file only, do not run setup.sh"
#	echo "-s, --script      Don't use installer.jar (not implemented yet)"
	echo "---------------------"
	exit 0;
}

# Defaults
extractOnly="false"
setupParams=""
skipMD5="false"

# Begin
echo "---------------------"
echo "DMDirc Unix Setup"
if [ "${isRelease}" != "" ]; then
	echo "Version: "${isRelease};
	setupParams="${setupParams} --release "${isRelease}
fi;
echo "---------------------"
# Check for cmdline args
while test -n "$1"; do
	case "$1" in
		--help|-h)
			showHelp
			;;
		--extract|-e)
			extractOnly="true"
			;;
		--script|-s)
			setupParams="${setupParams} --script"
			;;
		--nomd5)
			skipMD5="true"
			;;
	esac
	shift
done

MD5BIN=`which md5sum`
AWK=`which awk`
getMD5() {
	# Everything below the MD5SUM Line
	MD5LINE=`grep -na "^MD5=\".*\"$" ${1}`
	MD5LINE=$((${MD5LINE%%:*} + 1))

	MD5SUM=`tail -n +${MD5LINE} "${1}" | ${MD5BIN} - | ${AWK} '{print $1}'`
	return;
}

if [ "${MD5BIN}" != "" ]; then
	if [ ${skipMD5} != "true" ]; then
		#if [ -e "${0}.md5"  ]; then
		#	echo "Checking MD5 using ${0}.md5.."
		#	${MD5BIN} --check --status ${0}.md5
		#	if [ $? = 0 ]; then
		#		echo "MD5 Check Passed!"
		#	else
		#		ERROR="This copy of the DMDirc installer appears to be damaged and will now exit.";
		#		ERROR=${ERROR}"\nYou may choose to skip this check and run it anyway by passing the --nomd5 parameter";
		#		errordialog "DMDirc Setup: MD5 Check Failed!" "${ERROR}";
		#		exit 1;
		#	fi
		#elif [ "${MD5}" != ""  ]; then
		if [ "${MD5}" != ""  ]; then
			echo "Checking MD5 using built in hash.."
			if [ "${AWK}" != "" ]; then
				MD5SUM=""
				getMD5 ${0} ${MD5SUM}

				echo "SUM obtained is: ${MD5SUM}"
				echo "SUM expected is: ${MD5}"
				if [ "${MD5SUM}" = "${MD5}" ]; then
					echo "MD5 Check Passed!"
				else
					ERROR="This copy of the DMDirc installer appears to be damaged and will now exit.";
					ERROR=${ERROR}"\nYou may choose to skip this check and run it anyway by passing the --nomd5 parameter";
					errordialog "DMDirc Setup: MD5 Check Failed!" "${ERROR}";
					exit 1;
				fi;
			else
				echo "MD5 Check skipped (awk not found).."
			fi;
		else
			#if [ "${MD5BIN}" = "" ]; then
			#	echo "MD5 Check skipped (md5sum not found).."
			#else
				echo "MD5 Check skipped (No MD5 hash found to compare against).."
			#fi
		fi;
	else
		echo "MD5 Check skipped (Requested).."
	fi
fi;

OLDPWD=${PWD}
echo "Getting Temp Dir"
random TEMPDIR
echo "Got Temp Dir: ${TEMPDIR}"
cd ${TEMPDIR}
echo "Uncompressing to temp dir.."
uncompress
echo "Done."
# Check if extract only was wanted.
if [ "${extractOnly}" = "true" ]; then
	echo "Extracted. (Files can be found in: ${TEMPDIR})"
	exit 0;
fi

if [ -e "${TEMPDIR}/setup.sh" ]; then
	echo "Running setup.."
	chmod a+x ${TEMPDIR}/setup.sh
	${TEMPDIR}/setup.sh ${setupParams}
	echo ""
	if [ $? -eq 0 ]; then
		echo "Setup completed."
	else
		echo "Setup failed."
	fi
else
	echo "No setup.sh found. This was pointless?"
fi
echo "Removing temp dir"
cd ${OLDPWD}
rm -Rf ${TEMPDIR}
echo "Installation Completed."
# Job Done!
exit 0;