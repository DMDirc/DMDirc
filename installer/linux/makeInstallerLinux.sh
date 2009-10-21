#!/bin/sh
#
# This script generates a .run file that will install DMDirc
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

# Final Name of the installer (without file extention)
INSTALLERNAME=DMDirc-Setup
# full name of the file to output to
RUNNAME="${PWD}/${INSTALLERNAME}.run"

# Are we a git working copy, or SVN?
if [ -e ".svn" ]; then
	isSVN=1
else
	isSVN=0
fi;

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

# Compress stuff!
compress() {
	tar cvfh - $@ | gzip - 2>/dev/null >>${RUNNAME} || {
		echo "Compression failed."
		kill -15 $$;
	};
}

WGET=`which wget`
FETCH=`which fetch`
CURL=`which curl`
getFile() {
	URL=${1}
	OUTPUT=${2}

	if [ "${WGET}" != "" ]; then
		${WGET} -O ${OUTPUT} ${URL}
	elif [ "${FETCH}" != "" ]; then
		${FETCH} -o ${OUTPUT} ${URL}
	elif [ "${CURL}" != "" ]; then
		${CURL} -o ${OUTPUT} ${URL}
	fi;
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
	echo "-r, --release <version>   Generate a file based on an svn tag (or branch with -b as well)"
	echo "-b, --branch              Release in -r is a branch "
	echo "-p, --plugins <plugins>   What plugins to add to the jar file"
	echo "-c, --compile             Recompile the .jar file"
	echo "    --jre                 Include the JRE in this installer"
	echo "    --jre64               Include the 64-Bit JRE in this installer"
	echo "    --jar <file>          use <file> as DMDirc.jar"
	echo "    --current             Use the current folder as the base for the build"
	echo "-e, --extra <tag>         Tag to add to final exe name to distinguish this build from a standard build"
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
if [ $isSVN -eq 1 ]; then
	location="../../../"
	current=""
else
	location="../../"
	current="1"
fi;
jarfile=""
jre=""
jrename="jre" # Filename for JRE without the .bin
TAGGED=""
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
		--extra|-e)
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
		--tag|-t)
			shift
			if [ ${isSVN} -eq 1 ]; then
				REGEX="^[0-9]+(\.[0-9]+(\.[0-9]+)?)?$"
				CHECKTAG=`echo ${1} | egrep "${REGEX}"`
				if [ "" = "${CHECKTAG}" ]; then
					echo "Specified tag ("${1}") is invalid."
					exit 1;
				fi;
				TAGGED="${1}"
			else
				TAGGED=`git describe --tags`
				TAGGED=${TAGGED%%-*}
			fi;
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
	if [ $isSVN -eq 1 ]; then
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
	fi;
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

FUNCTIONSLINE=`grep ${GREPOPTS} "^###FUNCTIONS_FILE###$" installerstub.sh`
FUNCTIONSLINE=$((${FUNCTIONSLINE%%:*} + 0))

head -n ${FUNCTIONSLINE} installerstub.sh  > ${RUNNAME}
cat functions.sh >> ${RUNNAME}
echo "" >> ${RUNNAME}
tail ${TAILOPTS}$((${FUNCTIONSLINE%%:*} + 1)) installerstub.sh >> ${RUNNAME}

# Add release info.
if [ $isSVN -eq 1 ]; then
	awk '{gsub(/###ADDITIONAL_STUFF###/,"isRelease=\"'${isRelease}'\"");print}' ${RUNNAME} > ${RUNNAME}.tmp
	mv ${RUNNAME}.tmp ${RUNNAME}
elif [ "${TAGGED}" != "" ]; then
	awk '{gsub(/###ADDITIONAL_STUFF###/,"isRelease=\"'${TAGGED}'\"");print}' ${RUNNAME} > ${RUNNAME}.tmp
	mv ${RUNNAME}.tmp ${RUNNAME}
fi;

FILES="DMDirc.jar";
echo "Compressing files.."
for FILE in "getjre.sh" "installjre.sh" "progressbar.sh" "functions.sh"; do
	if [ -e "${FILE}" ]; then
		FILES="${FILES} ${FILE}"
	fi
done;

if [ "" != "${jre}" ]; then
	if [ ! -e "../common/${jrename}.bin" ]; then
		echo "Downloading JRE to include in installer"
		getFile "${jre}" "../common/${jrename}.bin"
	fi
	ln -sf ../common/${jrename}.bin jre.bin
	FILES="${FILES} jre.bin"
fi;

if [ -e "setup.sh" ]; then
	FILES="${FILES} setup.sh"
else
	echo "[WARNING] Creating setup-less archive. This will just extract and immediately delete the .jar file unless the -e flag is used"
fi

#if [ -e "../common/installer.jar" ]; then
#	ln -sf ../common/installer.jar ./installer.jar
#	FILES="${FILES} installer.jar"
#else
#	echo "[WARNING] Creating installer-less archive - relying on setup.sh"
#fi

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

if [ -e "${jarPath}/launcher/unix" ]; then
	ln -sf ${jarPath}/launcher/unix/DMDirc.sh .
	FILES="${FILES} DMDirc.sh"
fi

if [ -e "uninstall.sh" ]; then
	FILES="${FILES} uninstall.sh"
fi

compress $FILES

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

if [ "${MD5BIN}" != "" -a "${AWK}" != "" ]; then
	echo "Adding MD5.."

	MD5SUM=""
	getMD5 ${RUNNAME} ${MD5SUM}

	echo "SUM obtained is: ${MD5SUM}"

	LINENUM=`grep ${GREPOPTS} "^MD5=\"\"$" ${RUNNAME}`
	LINENUM=${LINENUM%%:*}

	head -n $((${LINENUM} -1)) ${RUNNAME} > ${RUNNAME}.tmp
	echo 'MD5="'${MD5SUM}'"' >> ${RUNNAME}.tmp
	tail ${TAILOPTS}$((${LINENUM} +1)) ${RUNNAME} >> ${RUNNAME}.tmp
	mv ${RUNNAME}.tmp ${RUNNAME}
else
	echo "Not Adding MD5.."
fi;

echo "Chmodding"
chmod a+x ${RUNNAME}

doRename=0
if [ "${isRelease}" != "" ]; then
	doRename=1
elif [ $isSVN -eq 0 -a "${TAGGED}" != "" ]; then
	doRename=1	
fi;

if [ ${doRename} -eq 1 ]; then
	if [ $isSVN -eq 1 ]; then
		if [ "${BRANCH}" = "1" ]; then
			isRelease=branch-${isRelease}
		fi;
	else
		if [ "${TAGGED}" = "" ]; then
			isRelease=branch-${isRelease}
		else
			isRelease=${TAGGED}
		fi;
	fi
	if [ "" != "${finalTag}" ]; then
		finalTag="-${finalTag}"
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
mv uninstall.sh uninstall.sh.tmp
mv functions.sh functions.sh.tmp
rm -f ${FILES}
mv setup.sh.tmp setup.sh
mv getjre.sh.tmp getjre.sh
mv installjre.sh.tmp installjre.sh
mv progressbar.sh.tmp progressbar.sh
mv uninstall.sh.tmp uninstall.sh
mv functions.sh.tmp functions.sh

echo "-----------"
echo "Done."
echo "-----------"

# and Done \o
exit 0;
