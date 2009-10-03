#!/bin/sh

# Jar names of plugins to add to ALL installers. (* means all)
plugins="ui_swing.jar tabcompletion_bash.jar tabcompletion_mirc.jar dcc.jar dns.jar identd.jar lagdisplay.jar logging.jar systray.jar time.jar osd.jar"

# Additional Jar names of plugins to add to only Windows installers. (* means all)
plugins_windows=""

# Additional Jar names of plugins to add to only linux installers. (* means all)
plugins_linux=""

# Additional Jar names of plugins to add to only osx installers. (* means all)
plugins_osx=""

# Are we a git working copy, or SVN?
if [ -e ".svn" ]; then
	isSVN=1
else
	isSVN=0
fi;


showHelp() {
	echo "This will generate the different DMDirc installers."
	if [ ${isSVN} -eq 1 ]; then
		echo "Usage: ${0} [params] <release>"
		echo "Release can be either 'trunk', 'this', a valid tag, or a branch (if -b is passed)"
	else
		echo "Usage: ${0} [params]"
	fi;
	echo "The following params are known:"
	echo "---------------------"
	if [ ${isSVN} -eq 1 ]; then
		echo "-b,  --branch                       <release> is a branch"
	else
		echo "-t,  --tag                          This is a tagged release"
	fi;
	echo "     --jar <file>                   Use <file> instead of compiling a jar."
	echo "     --fulljar <file>               Use <file> instead of compiling a jar, and don't run makeJar on it."
	echo "     --jre                          Include a jre in the installers."
	echo "     --jre64                        Include a 64bit jre in the installers."
	echo "     --target <target>              Build only a specific target. <target> should be one of 'windows', 'linux' or 'osx'."
	echo "-p,  --plugins <plugins>            Plugins to add to all the jars."
	echo "-pl, --plugins-linux <plugins>      Plugins to linux installer."
	echo "-pw, --plugins-windows <plugins>    Plugins to windows installer."
	echo "-po  --plugins-osx <plugins>        Plugins to osx installer."
	echo "-h,  --help                         Help information"
	echo "-o,  --opt <options>                Additional options to pass to the make*Installer.sh files"
	echo "     --upload                       Try to upload to google code when done (Only works on tags)"
	echo "-c   --channel [channel]            Channel to pass to ant (if not passed, 'NONE', if passed without a value, 'STABLE')"
	echo "     --compile                      Recompile the .jar file (otherwise use the existing file from dist/)"
	echo "---------------------"
	exit 0;
}

# Check for some CLI params
LAST=""
OPT=""
BRANCH=""
JARFILE=""
JRE=""
FULLJAR=""
BUILDTARGET=""
UPLOAD="0"
TAG="0"
TAGGED=""
CHANNEL=""
compileJar=""
while test -n "$1"; do
	LAST=${1}
	case "$1" in
		--plugins|-p)
			shift
			plugins="${1}"
			;;
		--target)
			shift
			BUILDTARGET="${1}"
			;;
		--jar)
			shift
			JARFILE="--jar ${1} "
			;;
		--fulljar)
			shift
			JARFILE="--jar ${1} "
			FULLJAR="1"
			;;
		--jre|--jre64)
			JRE="${1} "
			;;
		--plugins-linux|-pl)
			shift
			plugins_linux="${1}"
			;;
		--plugins-windows|-pw)
			shift
			plugins_windows="${1}"
			;;
		--plugins-osx|-po)
			shift
			plugins_osx="${1}"
			;;
		--opt|-o)
			shift
			OPT="${1} "
			;;
		--help|-h)
			showHelp;
			;;
		--upload)
			UPLOAD="1";
			;;
		--branch|-b)
			BRANCH="-b "
			;;
		--compile)
			compileJar="--compile "
			;;
		--tag|-t)
			if [ ${isSVN} -eq 1 ]; then
				shift
				REGEX="^[0-9]+(\.[0-9]+(\.[0-9]+)?)?$"
				CHECKTAG=`echo ${1} | egrep "${REGEX}"`
				if [ "" = "${CHECKTAG}" ]; then
					echo "Specified tag ("${1}") is invalid."
					exit 1;
				fi;
				TAGGED="-t ${1} "
			else
				TAGGED="-t "
			fi;
			# Always recompile if tagging
			compileJar="--compile "
			;;
		--channel|-c)
			PASSEDPARAM=`echo "${2}" | grep -v ^- | grep -v " "`
			if [ "${PASSEDPARAM}" != "" ]; then
				shift
				CHANNEL="--channel ${PASSEDPARAM} ";
			else
				CHANNEL="--channel STABLE ";
			fi;
			# Always recompile if passing a channel
			compileJar="--compile "
			;;
	esac
	shift
done

if [ ! -e output ]; then
	mkdir output
fi;

if [ "${plugins}" = "*" -o "${plugins_linux}" = "*" -o "${plugins_windows}" = "*" -o "${plugins_osx}" = "*" ]; then
	echo "Something is all.";
	allPlugins=""
	for thisfile in `ls -1 ../plugins/*.jar`; do
		allPlugins=${allPlugins}" ${thisfile##*/}"
	done
	if [ "${plugins}" = "*" ]; then plugins=${allPlugins}; fi
	if [ "${plugins_linux}" = "*" ]; then plugins_linux=${allPlugins}; fi
	if [ "${plugins_windows}" = "*" ]; then plugins_windows=${allPlugins}; fi
	if [ "${plugins_osx}" = "*" ]; then plugins_osx=${allPlugins}; fi
fi;

if [ ${isSVN} -eq 1 ]; then
	VERSION=""
	if [ "${LAST}" != "" ]; then
		if [ "${LAST}" = "trunk" ]; then
			VERSION="Trunk"
			RELEASE=""
		elif [ "${LAST}" = "this" ]; then
			# Work out what type of build this is!
			thisDIR=${PWD}
			cd ..
			tempDIR=${PWD##*/}
			if [ "${tempDIR}" = "trunk" ]; then
				VERSION="Trunk"
				echo "This is a trunk release.";
			else
				echo "This is not a trunk release.";
				VERSION=${tempDIR}
				cd ..
				tempDIR=${PWD##*/}
				if [ "${tempDIR}" = "tags" ]; then
					echo "Release of tag "${version}
					RELEASE="-r "${VERSION}
					TAG="1"
				elif [ "${tempDIR}" = "branches" ]; then
					echo "Release of branch "${version}
					BRANCH="-b "
					RELEASE="-r "${VERSION}
				else
					VERSION="Unknown"
					echo "Unknown release target - Building as trunk build"
					OPT="--current ${OPT}"
				fi
			fi;
			cd ${thisDIR}
		elif [ "${BRANCH}" != "" -a ! -e "../../branches/"${LAST} ]; then
			echo "Branch '"${LAST}"' not found."
			exit 1;
		elif [ "${BRANCH}" = "" -a ! -e "../../tags/"${LAST} ]; then
			echo "Tag '"${LAST}"' not found."
			exit 1;
		else
			RELEASE="-r "${LAST}
		fi
	else
		echo "Usage: ${0} [params] <release>"
		echo "Release can be either 'this', 'trunk' or a valid tag. (see ${0} --help for further information)"
		exit 1;
	fi
else
	VERSION=`git branch | grep ^* | sed "s/^* //g"`
	if [ "${VERSION}" = "master" ]; then
		RELEASE=""
	else
		RELEASE="-r ${VERSION}"
	fi;
fi;

JAR=`which jar`
JAVAC=`which javac`
# OSX Users might have a non 1.6 javac, look specifically for it.
if [ -e "/System/Library/Frameworks/JavaVM.framework/Versions/1.6.0/Commands/javac" ]; then
	JAVAC="/System/Library/Frameworks/JavaVM.framework/Versions/1.6.0/Commands/javac"
elif [ -e "/System/Library/Frameworks/JavaVM.framework/Versions/1.6/Commands/javac" ]; then
	JAVAC="/System/Library/Frameworks/JavaVM.framework/Versions/1.6/Commands/javac"
fi;

THISDIR=${PWD}

echo "================================================================"
echo "Removing existing releases from output directory"
echo "================================================================"
rm -Rf output/*.run output/*.exe output/*.dmg

# Copy default settings from www to trunk for compile (if they exist, and we are
# building a new jar)
REVERTLIST=""
if [ "" = "${JARFILE}" ]; then
	if [ -e "${HOME}/www/updates/" ]; then
		echo "================================================================"
		echo "Applying settings update to this source"
		echo "================================================================"
		for updatedir in `ls -1 ../src/com/dmdirc/config/defaults/`; do
			src="${HOME}/www/updates/${updatedir}"
			if [ -e ${src} ]; then
				REVERTLIST=${REVERTLIST}" ../src/com/dmdirc/config/defaults/${updatedir}/"
				cp -Rfv ${src}/* ../src/com/dmdirc/config/defaults/${updatedir}/
			fi;
		done
	fi;
fi;

if [ "" = "${FULLJAR}" ]; then
	echo "================================================================"
	echo "Building Release Jar"
	echo "================================================================"
	cd jar
	./makeJar.sh ${compileJar}${CHANNEL}${OPT}${JARFILE}${JRE}-c -k -s ${TAGGED}${BRANCH}${RELEASE} -p "${plugins}"
	RESULT=${?}
	cd ${THISDIR}

	if [ ${RESULT} -eq 0 ]; then
		JARNAME=`ls -1tr output | grep jar$ | tail -n 1`
		JARFILE="--jar ../output/${JARNAME} "
	else
		echo "Failed to build release jar, aborting."
		exit 1;
	fi;
fi;

if [ "linux" = "${BUILDTARGET}" -o "" = "${BUILDTARGET}" ]; then
	echo "================================================================"
	echo "Building linux installer"
	echo "================================================================"
	cd linux
	./makeInstallerLinux.sh ${OPT}${JARFILE}${JRE}-k ${TAGGED}${BRANCH}${RELEASE} -p "${plugins_linux}"
	cd ${THISDIR}
fi;

if [ "windows" = "${BUILDTARGET}" -o "" = "${BUILDTARGET}" ]; then
	echo "================================================================"
	echo "Building Windows installer"
	echo "================================================================"
	cd windows
	./makeInstallerWindows.sh ${OPT}${JARFILE}${JRE}-k -s ${TAGGED}${BRANCH}${RELEASE} -p "${plugins_windows}"
	cd ${THISDIR}
fi;

if [ "osx" = "${BUILDTARGET}" -o "" = "${BUILDTARGET}" ]; then
	echo "================================================================"
	echo "Building OSX Bundle"
	echo "================================================================"
	cd osx
	./makeInstallerOSX.sh ${OPT}${JARFILE}-k -s ${TAGGED}${BRANCH}${RELEASE} -p "${plugins_osx}"
	cd ${THISDIR}
fi;


echo "================================================================"
echo "Clean Up"
echo "================================================================"
# Now revert the trunk so as not to break updates.
for updatedir in ${REVERTLIST}; do
	if [ ${isSVN} -eq 1 ]; then
		SVN=`which svn`
		${SVN} revert ${updatedir}/*
	else
		GIT=`which git`
		${GIT} checkout ${updatedir}
	fi;
done;

if [ "1" = "${UPLOAD}" -a "" != "${TAGGED}" ]; then
	echo "================================================================"
	echo "Uploading to GoogleCode"
	echo "================================================================"
	
	cd gcode
	sh uploads_release.sh -v ${VERSION}
else
	echo "Not uploading to GoogleCode (Only tagged releases can be uploaded)"
fi;

echo "================================================================"
echo "Release ready - see output folder"
echo "================================================================"
exit 0;
