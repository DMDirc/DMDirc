#!/bin/sh
#
# This script installs dmdirc
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

MD5=""

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

# Find out what params we should pass to things.
# Solaris has a nice and ancient version of grep in /usr/bin
grep -na "" /dev/null >/dev/null 2>&1
if [ $? -eq 2 ]; then
	GREPOPTS="-n"
else
	GREPOPTS="-na"
fi;
# Solaris also has a crappy version of tail!
tail -n +1 /dev/null >/dev/null 2>&1
if [ $? -eq 2 ]; then
	TAILOPTS="+"
else
	TAILOPTS="-n +"
fi;

###ADDITIONAL_STUFF###

###FUNCTIONS_FILE###

# Location of .run stub end
ENDLINE=`grep ${GREPOPTS} "^###END STUB###$" $0`
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
#	DEBUGINFO="\n\nRunname: ${0}"
#	DEBUGINFO="${DEBUGINFO}\nOldPwd: ${OLDPWD}"

	# Try runname, in old dir.
	FILE=${OLDPWD}/$0
	if [ ! -e ${FILE} ]; then
		# Else, try run filename in old dir
		FILE=${OLDPWD}/`basename $0`
		if [ ! -e ${FILE} ]; then
			# Else try run name
			FILE=$0
			if [ ! -e ${FILE} ]; then
				# Unable to find this file!
				errordialog "DMDirc Setup" "Unable to find installer.\nThis shouldn't happen, please raise a bug report at http://bugs.dmdirc.com${DEBUGINFO}";
				echo "Removing temp dir"
				cd ${OLDPWD}
				rm -Rf ${TEMPDIR}
				exit 1;
			fi;
		fi;
	fi;
	echo "Decompressing: ${FILE}"
#	DEBUGINFO="${DEBUGINFO}\nFile: ${FILE}"
#	DEBUGINFO="${DEBUGINFO}\nCommand: tail ${TAILOPTS}${ENDLINE} "${FILE}" | gzip -cd | tar -xvf - 2>&1"
	OUTPUT=`tail ${TAILOPTS}${ENDLINE} "${FILE}" | gzip -cd | tar -xvf - 2>&1`
	if [ "${OUTPUT}" = "" ]; then
		echo "Decompression failed."
		errordialog "DMDirc Setup" "Decompression failed.\nThis shouldn't happen, please raise a bug report at http://bugs.dmdirc.com${DEBUGINFO}";
		echo "Removing temp dir"
		cd ${OLDPWD}
		rm -Rf ${TEMPDIR}
		exit 1;
	fi;
	echo "Decompression successful."
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
if [ "${MD5BIN}" = "" ]; then
	MD5BIN=`which md5`
fi;
AWK=`which awk`
getMD5() {
	if [ "${MD5BIN}" != "" ]; then
		echo "test" | ${MD5BIN} -
		if [ $? -eq 0 ]; then
			echo "Linux-Style MD5SUM: ${MD5BIN}"
			getMD5Linux $@
		else
			echo "BSD-Style MD5SUM: ${MD5BIN}"
			getMD5BSD $@
		fi;
	fi;
}

getMD5Linux() {
	# Everything below the MD5SUM Line
	MD5LINE=`grep ${GREPOPTS} "^MD5=\".*\"$" ${1}`
	MD5LINE=$((${MD5LINE%%:*} + 1))

	MD5SUM=`tail ${TAILOPTS}${MD5LINE} "${1}" | ${MD5BIN} - | ${AWK} '{print $1}'`
	return;
}

getMD5BSD() {
	# Everything below the MD5SUM Line
	MD5LINE=`grep ${GREPOPTS} "^MD5=\".*\"$" ${1}`
	MD5LINE=$((${MD5LINE%%:*} + 1))

	MD5SUM=`tail ${TAILOPTS}${MD5LINE} "${1}" | ${MD5BIN} | ${AWK} '{print $1}'`
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
				elif [ "${MD5SUM}" = "" -o "${MD5}" = "" ]; then
					ERROR="The DMDirc installer is unable to verify the checksum of this download.";
					if [ "${MD5SUM}" = "" ]; then
						ERROR=${ERROR}"(Unable to calculate sum locally)";
					else
						ERROR=${ERROR}"(No checksum found in file)";
					fi;
					
					ERROR=${ERROR}"\nDo you want to continue anyway?";
					
					questiondialog "DMDirc Setup: MD5 Check Failed!" "${ERROR}";
					exit 1;
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
		echo "Setup did not complete."
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
###END STUB###
