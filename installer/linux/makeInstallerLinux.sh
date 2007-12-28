#!/bin/sh
#
# This script generates a .run file that will install DMDirc
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

# Final Name of the installer (without file extention)
INSTALLERNAME=DMDirc-Setup
# Location of .run stub start
STARTLINE=`grep -n "^###START INCLUDE###$" $0`
STARTLINE=$((${STARTLINE%%:*} + 1))
# full name of the file to output to
RUNNAME="${PWD}/${INSTALLERNAME}.run"

# Compress stuff!
compress() {
	tar cvfh - $@ | gzip - 2>/dev/null >>${RUNNAME} || {
		echo "Compression failed."
		kill -15 $$;
	};
}

# Go!
echo "-----------"
if [ -e "${RUNNAME}" ]; then
	echo "Removing existing .run file"
	rm -Rf ./*.run
fi

showHelp() {
	echo "This will generate a DMDirc installer for a unix based system."
	echo "The following command line arguments are known:"
	echo "---------------------"
	echo "-h, --help                Help information"
	echo "-r, --release <version>   Generate a file based on an svn tag (or branch with -b aswell)"
	echo "-b, --branch              Release in -r is a branch "
	echo "-p, --plugins <plugins>   What plugins to add to the jar file"
	echo "-c, --compile             Recompile the .jar file"
	echo "    --jre                 Include the JRE in this installer"
	echo "    --jre64               Include the 64-Bit JRE in this installer"
	echo "    --jar <file>          use <file> as DMDirc.jar"
	echo "    --current             Use the current folder as the base for the build"
	echo "-t, --tag <tag>           Tag to add to final exe name to distinguish this build from a standard build"
	echo "-k, --keep                Keep the existing source tree when compiling"
	echo "                          (don't svn update beforehand)"
	echo "---------------------"
	exit 0;
}

# Check for some CLI params
compileJar="false"
updateSVN="true"
isRelease=""
finalTag=""
BRANCH="0"
plugins=""
location="../../../"
jarfile=""
current=""
jre=""
jrename="jre" # Filename for JRE without the .bin
while test -n "$1"; do
	case "$1" in
		--plugins|-p)
			shift
			plugins=${1}
			;;
		--jar)
			shift
			jarfile=${1}
			;;
		--jre)
			jre="http://www.dmdirc.com/getjava/linux/i686"
			;;
		--jre64)
			jre="http://www.dmdirc.com/getjava/linux/x86_64"
			jrename="jre64"
			;;
		--current)
			location="../../"
			current="1"
			;;
		--compile|-c)
			compileJar="true"
			;;
		--release|-r)
			shift
			isRelease=${1}
			;;
		--tag|-t)
			shift
			finalTag=${1}
			RUNNAME="${PWD}/${INSTALLERNAME}-${1}.run"
			;;
		--keep|-k)
			updateSVN="false"
			;;
		--help|-h)
			showHelp;
			;;
		--branch|-b)
			BRANCH="1"
			;;
	esac
	shift
done
if [ "" = "${current}" ]; then
	jarPath="${location}trunk"
else
	jarPath="${location}"
fi
if [ "${isRelease}" != "" ]; then
	if [ "${BRANCH}" != "0" ]; then
		if [ -e "${location}/${isRelease}" ]; then
			jarPath="${location}/${isRelease}"
		else
			echo "Branch "${isRelease}" not found."
			exit 1;
		fi
	else
		if [ -e "${location}/${isRelease}" ]; then
			jarPath="${location}/${isRelease}"
		else
			echo "Tag "${isRelease}" not found."
			exit 1;
		fi
	fi
fi

if [ "" = "${jarfile}" ]; then
	jarfile=${jarPath}"/dist/DMDirc.jar"
	if [ ! -e ${jarPath}"/dist/DMDirc.jar" -o "${compileJar}" = "true" ]; then
		echo "Creating jar.."
		OLDPWD=${PWD}
		cd ${jarPath}
		if [ "${updateSVN}" = "true" ]; then
			svn update
		fi
		rm -Rf build dist
		ant jar
		if [ ! -e "dist/DMDirc.jar" ]; then
			echo "There was an error creating the .jar file. Aborting."
			exit 1;
		fi;
		cd ${OLDPWD}
	fi;
elif [ ! -e "${jarfile}" ]; then
	echo "Requested Jar file (${jarfile}) does not exist."
	exit 1;
fi;

if [ "" = "${plugins}" ]; then
	echo "Linking jar (${jarfile}).."
	ln -sf ${jarfile} "./DMDirc.jar"
else
	echo "Copying jar (${jarfile}).."
	cp ${jarfile} "./DMDirc.jar"

	echo "Adding plugins to jar"
	ln -sf ${jarPath}"/plugins"
	pluginList=""
	for plugin in ${plugins}; do
		pluginList=${pluginList}" plugins/${plugin}"
	done
	jar -uvf "DMDirc.jar" ${pluginList}
	rm -Rf plugins;
fi

echo "Creating .run file"
echo "Adding stub.."
tail -n +${STARTLINE} $0 > ${RUNNAME}

# Add release info.
awk '{gsub(/###ADDITIONAL_STUFF###/,"isRelease=\"'${isRelease}'\"");print}' ${RUNNAME} > ${RUNNAME}.tmp
mv ${RUNNAME}.tmp ${RUNNAME}

FILES="DMDirc.jar";
echo "Compressing files.."
for FILE in "getjre.sh" "installjre.sh" "progressbar.sh"; do
	if [ -e "${FILE}" ]; then
		FILES="${FILES} ${FILE}"
	fi
done;

if [ "" != "${jre}" ]; then
	if [ ! -e "../common/${jrename}.bin" ]; then
		echo "Downloading JRE to include in installer"
		wget ${jre} -O ../common/${jrename}.bin
	fi
	ln -sf ../common/${jrename}.bin jre.bin
	FILES="${FILES} jre.bin"
fi;

if [ -e "setup.sh" ]; then
	FILES="${FILES} setup.sh"
else
	echo "[WARNING] Creating setup-less archive. This will just extract and immediately delete the .jar file unless the -e flag is used"
fi

if [ -e "../common/installer.jar" ]; then
	ln -sf ../common/installer.jar ./installer.jar
	FILES="${FILES} installer.jar"
else
	echo "[WARNING] Creating installer-less archive - relying on setup.sh"
fi

if [ -e ${jarPath}"/src/com/dmdirc/res/source/logo.svg" ]; then
	ln -sf ${jarPath}"/src/com/dmdirc/res/source/logo.svg" ./icon.svg
	FILES="${FILES} icon.svg"
fi

if [ "${isRelease}" != "" ]; then
	DOCSDIR=${jarPath}
else
	DOCSDIR="../common"
fi

if [ -e "${DOCSDIR}/README.TXT" ]; then
	ln -sf "${DOCSDIR}/README.TXT" .
	FILES="${FILES} README.TXT"
fi

if [ -e "${DOCSDIR}/CHANGES.TXT" ]; then
	ln -sf "${DOCSDIR}/CHANGES.TXT" .
	FILES="${FILES} CHANGES.TXT"
elif [ -e "${DOCSDIR}/CHANGELOG.TXT" ]; then
	ln -sf "${DOCSDIR}/CHANGELOG.TXT" .
	FILES="${FILES} CHANGELOG.TXT"
fi

if [ -e "${jarPath}/launcher/linux" ]; then
	ln -sf ${jarPath}/launcher/linux/DMDirc.sh .
	FILES="${FILES} DMDirc.sh"
fi

compress $FILES

MD5BIN=`which md5sum`
AWK=`which awk`
getMD5() {
	# Everything below the MD5SUM Line
	MD5LINE=`grep -na "^MD5=\".*\"$" ${1}`
	MD5LINE=$((${MD5LINE%%:*} + 1))

	MD5SUM=`tail -n +${MD5LINE} "${1}" | ${MD5BIN} - | ${AWK} '{print $1}'`
	return;
}

if [ "${MD5BIN}" != "" -a "${AWK}" != "" ]; then
	echo "Adding MD5.."

	MD5SUM=""
	getMD5 ${RUNNAME} ${MD5SUM}

	echo "SUM obtained is: ${MD5SUM}"

	LINENUM=`grep -na "^MD5=\"\"$" ${RUNNAME}`
	LINENUM=${LINENUM%%:*}

	head -n $((${LINENUM} -1)) ${RUNNAME} > ${RUNNAME}.tmp
	echo 'MD5="'${MD5SUM}'"' >> ${RUNNAME}.tmp
	tail -n +$((${LINENUM} +1)) ${RUNNAME} >> ${RUNNAME}.tmp
	mv ${RUNNAME}.tmp ${RUNNAME}
else
	echo "Not Adding MD5.."
fi;

echo "Chmodding"
chmod a+x ${RUNNAME}


if [ "${isRelease}" != "" ]; then
	if [ "${Branch}" != "" ]; then
		isRelease=branch-${isRelease}
	fi;
	finalname=DMDirc-${isRelease}-Setup${finalTag}.run
else
	finalname=${RUNNAME##*/}
fi;

if [ "" != "${jre}" ]; then
	finalname=`echo ${finalname} | sed "s/.run$/.${jrename}.run/"`
fi;

mv ${RUNNAME} ../output/${finalname}
mv setup.sh setup.sh.tmp
mv getjre.sh getjre.sh.tmp
mv installjre.sh installjre.sh.tmp
mv progressbar.sh progressbar.sh.tmp
rm -f ${FILES}
mv setup.sh.tmp setup.sh
mv getjre.sh.tmp getjre.sh
mv installjre.sh.tmp installjre.sh
mv progressbar.sh.tmp progressbar.sh

echo "-----------"
echo "Done."
echo "-----------"

# and Done \o
exit 0;
### Everything below here is part of the .run stub.
###START INCLUDE###
#!/bin/sh
#
# This script installs dmdirc
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
	ISKDE=`pidof -x -s kdeinit`
	KDIALOG=`which kdialog`
	ISGNOME=`pidof -x -s gnome-panel`
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
		DIR=`${MKTEMP} -d`
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
	echo "-s, --script      Don't use installer.jar (not implemented yet)"
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
###END INCLUDE###
