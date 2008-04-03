#!/bin/sh

# Jar names of plugins to add to ALL installers. (* means all)
plugins="dns.jar identd.jar lagdisplay.jar logging.jar systray.jar timeplugin.jar osdplugin.jar"

# Additional Jar names of plugins to add to only Windows installers. (* means all)
plugins_windows=""

# Additional Jar names of plugins to add to only linux installers. (* means all)
plugins_linux=""

# Additional Jar names of plugins to add to only osx installers. (* means all)
plugins_osx=""

showHelp() {
	echo "This will generate the different DMDirc installers."
	echo "Usage: ${0} [params] <release>"
	echo "Release can be either 'trunk', 'this', a valid tag, or a branch (if -b is passed)"
	echo "The following params are known:"
	echo "---------------------"
	echo "-b,  --branch                       <release> is a branch"
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
		--branch|-b)
			BRANCH="-b "
			;;
	esac
	shift
done

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

if [ "${LAST}" != "" ]; then
	if [ "${LAST}" = "trunk" ]; then
		RELEASE=""
	elif [ "${LAST}" = "this" ]; then
		# Work out what type of build this is!
		thisDIR=${PWD}
		cd ..
		tempDIR=${PWD##*/}
		if [ "${tempDIR}" = "trunk" ]; then
			echo "This is a trunk release.";
		else
			echo "This is not a trunk release.";
			version=${tempDIR}
			cd ..
			tempDIR=${PWD##*/}
			if [ "${tempDIR}" = "tags" ]; then
				echo "Release of tag "${version}
				RELEASE="-r "${version}
			elif [ "${tempDIR}" = "branches" ]; then
				echo "Release of branch "${version}
				BRANCH="-b "
				RELEASE="-r "${version}
			else
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

# OSX doesn't use the installer
# and neither does anything else now,
#if [ "osx" != "${BUILDTARGET}" ]; then
#	echo "================================================================"
#	echo "Building Installer JAR "
#	echo "================================================================"
#	mkdir -p installer_temp/build
#	cd installer_temp
#	ln -sf ../../src/com
#	ln -sf ../../src/net
#	# I don't know why, but -d doesn't nicely put ALL generated class files here,
#	# just those that were in the dir of the java file that was requested for compile
#	# So we specify each of the different ones we want built into the jar file here.
#	FILELIST="com/dmdirc/installer/*.java"
#	FILELIST=${FILELIST}" com/dmdirc/installer/cliparser/*.java"
#	FILELIST=${FILELIST}" com/dmdirc/ui/swing/dialogs/wizard/*.java"
#	FILELIST=${FILELIST}" com/dmdirc/ui/interfaces/MainWindow.java"
#	FILELIST=${FILELIST}" com/dmdirc/ui/swing/MainFrame.java"
#	FILELIST=${FILELIST}" com/dmdirc/ui/swing/UIUtilities.java"
#	FILELIST=${FILELIST}" com/dmdirc/ui/swing/UIUtilities.java"
#	FILELIST=${FILELIST}" com/dmdirc/ui/swing/components/StandardDialog.java"
#	FILELIST=${FILELIST}" com/dmdirc/util/ListenerList.java"
#	FILELIST=${FILELIST}" com/dmdirc/util/WeakMapList.java"
#	FILELIST=${FILELIST}" com/dmdirc/util/MapList.java"
#	FILELIST=${FILELIST}" com/dmdirc/ui/swing/components/EtchedLineBorder.java"
#	FILELIST=${FILELIST}" com/dmdirc/util/EquatableWeakReference.java"
#	FILELIST=${FILELIST}" com/dmdirc/ui/swing/components/JWrappingLabel.java"
#	FILELIST=${FILELIST}" com/dmdirc/util/WeakList.java"
#	FILELIST=${FILELIST}" net/miginfocom/layout/*.java"
#	FILELIST=${FILELIST}" net/miginfocom/swing/*.java"
#	
#	${JAVAC} -d ./build ${FILELIST}
#	
#	if [ $? -ne 0 ]; then
#		echo "================================================================"
#		echo "Building installer failed."
#		echo "================================================================"
#		cd ${THISDIR}
#		rm -Rf installer_temp
#		exit 1;
#	fi
#	
#	cd build
#	echo "Manifest-Version: 1.0" > manifest.txt
#	echo "Created-By: DMDirc Installer" >> manifest.txt
#	echo "Main-Class: com.dmdirc.installer.Main" >> manifest.txt
#	echo "Class-Path: " >> manifest.txt
#	echo "" >> manifest.txt
#	${JAR} cmf manifest.txt installer.jar com net
#	if [ $? -ne 0 ]; then
#		echo "================================================================"
#		echo "Building installer failed."
#		echo "================================================================"
#		cd ${THISDIR}
#		rm -Rf installer_temp
#		exit 1;
#	else
#		rm -Rf ${THISDIR}/common/installer.jar
#		mv installer.jar ${THISDIR}/common/installer.jar
#	fi
#	
#	cd ${THISDIR}
#	rm -Rf installer_temp
#fi;


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
	./makeJar.sh ${OPT}${JARFILE}${JRE}-c -k -s ${BRANCH}${RELEASE} -p "${plugins}"
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
	./makeInstallerLinux.sh ${OPT}${JARFILE}${JRE}-k ${BRANCH}${RELEASE} -p "${plugins_linux}"
	cd ${THISDIR}
fi;

if [ "windows" = "${BUILDTARGET}" -o "" = "${BUILDTARGET}" ]; then
	echo "================================================================"
	echo "Building Windows installer"
	echo "================================================================"
	cd windows
	./makeInstallerWindows.sh ${OPT}${JARFILE}${JRE}-k -s ${BRANCH}${RELEASE} -p "${plugins_windows}"
	cd ${THISDIR}
fi;

if [ "osx" = "${BUILDTARGET}" -o "" = "${BUILDTARGET}" ]; then
	echo "================================================================"
	echo "Building OSX Bundle"
	echo "================================================================"
	cd osx
	./makeInstallerOSX.sh ${OPT}${JARFILE}-k -s ${BRANCH}${RELEASE} -p "${plugins_osx}"
	cd ${THISDIR}
fi;

#MD5BIN=`which md5sum`
#if [ "${MD5BIN}" != "" ]; then
#	echo "================================================================"
#	echo "Creating MD5SUM files"
#	echo "================================================================"
#	cd output
#	for outputFile in *; do
#		if [ "${outputFile##*.}" != "md5" ]; then
#			if [ -e "${outputFile}.md5" ]; then
#				rm -f "${outputFile}.md5"
#			fi
#			${MD5BIN} "${outputFile}" > "${outputFile}.md5"
#		fi
#	done
#	cd ${THISDIR}
#fi

echo "================================================================"
echo "Clean Up"
echo "================================================================"
# Now revert the trunk so as not to break updates.
for updatedir in ${REVERTLIST}; do
	SVN=`which svn`
	${SVN} revert ${updatedir}/*
done;

echo "================================================================"
echo "Release ready - see output folder"
echo "================================================================"
exit 0;
