#!/bin/sh

# Jar names of plugins to add to ALL installers. (* means all)
plugins="dns.jar identd.jar lagdisplay.jar logging.jar systray.jar timeplugin.jar osdplugin.jar"

# Additional Jar names of plugins to add to only Windows installers. (* means all)
plugins_windows=""

# Additional Jar names of plugins to add to only linux installers. (* means all)
plugins_linux=""

showHelp() {
	echo "This will generate the different DMDirc installers."
	echo "Usage: ${0} [params] <release>"
	echo "Release can be either 'trunk', 'this', a valid tag, or a branch (if -b is passed)"
	echo "The following params are known:"
	echo "---------------------"
	echo "-b, --branch                       <release> is a branch"
	echo "    --jar <file>                   Use <file> instead of compiling a jar."
	echo "-p, --plugins <plugins>            Plugins to add to all the jars."
	echo "-pl, --plugins-linux <plugins>     Plugins to linux installer."
	echo "-pw, --plugins-windows <plugins>   Plugins to linux installer."
	echo "-h, --help                         Help information"
	echo "-o, --opt <options>                Additional options to pass to the make*Installer.sh files"
	echo "---------------------"
	exit 0;
}

# Check for some CLI params
LAST=""
OPT=""
BRANCH=""
JARFILE=""
while test -n "$1"; do
	LAST=${1}
	case "$1" in
		--plugins|-p)
			shift
			plugins="${1}"
			;;
		--jar)
			shift
			JARFILE="--jar ${1} "
			;;
		--plugins-linux|-pl)
			shift
			plugins_linux="${1}"
			;;
		--plugins-windows|-pw)
			shift
			plugins_windows="${1}"
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

if [ "${plugins}" = "*" -o "${plugins_linux}" = "*" -o "${plugins_windows}" = "*" ]; then
	echo "Something is all.";
	allPlugins=""
	for thisfile in `ls -1 ../plugins/*.jar`; do
		allPlugins=${allPlugins}" ${thisfile##*/}"
	done
	if [ "${plugins}" = "*" ]; then plugins=${allPlugins}; fi
	if [ "${plugins_linux}" = "*" ]; then plugins_linux=${allPlugins}; fi
	if [ "${plugins_windows}" = "*" ]; then plugins_windows=${allPlugins}; fi
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
			elif [ "${tempDIR}" = "branches" ]; then
				echo "Release of branch "${version}
				BRANCH="-b "
			else
				echo "Unknown release target."
				exit 0;
			fi
			RELEASE="-r "${version}
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

JAR=/usr/bin/jar
JAVAC=/usr/bin/javac
THISDIR=${PWD}

echo "================================================================"
echo "Removing existing releases from output directory"
echo "================================================================"
rm -Rf output/*.run output/*.exe
echo "================================================================"
echo "Building Installer JAR "
echo "================================================================"
mkdir -p installer_temp/build
cd installer_temp
ln -s ../../src/com
ln -s ../../src/net
# I don't know why, but -d doesn't nicely put ALL generated class files here,
# just those that were in the dir of the java file that was requested for compile
# So we specify each of the different ones we want built into the jar file here.
FILELIST="com/dmdirc/installer/*.java"
FILELIST=${FILELIST}" com/dmdirc/installer/cliparser/*.java"
FILELIST=${FILELIST}" com/dmdirc/ui/swing/dialogs/wizard/*.java"
FILELIST=${FILELIST}" com/dmdirc/ui/interfaces/MainWindow.java"
FILELIST=${FILELIST}" com/dmdirc/ui/swing/MainFrame.java"
FILELIST=${FILELIST}" com/dmdirc/ui/swing/UIUtilities.java"
FILELIST=${FILELIST}" com/dmdirc/ui/swing/UIUtilities.java"
FILELIST=${FILELIST}" com/dmdirc/ui/swing/components/StandardDialog.java"
FILELIST=${FILELIST}" com/dmdirc/util/ListenerList.java"
FILELIST=${FILELIST}" com/dmdirc/util/WeakMapList.java"
FILELIST=${FILELIST}" com/dmdirc/util/WeakList.java"
FILELIST=${FILELIST}" net/miginfocom/layout/*.java"
FILELIST=${FILELIST}" net/miginfocom/swing/*.java"

${JAVAC} -d ./build ${FILELIST}

if [ $? -ne 0 ]; then
	echo "================================================================"
	echo "Building installer failed."
	echo "================================================================"
	cd ${THISDIR}
	rm -Rf installer_temp
	exit 1;
fi
cd build
echo "Manifest-Version: 1.0" > manifest.txt
echo "Created-By: DMDirc Installer" >> manifest.txt
echo "Main-Class: com.dmdirc.installer.Main" >> manifest.txt
echo "Class-Path: " >> manifest.txt
echo "" >> manifest.txt
${JAR} cmf manifest.txt installer.jar com net
if [ $? -ne 0 ]; then
	echo "================================================================"
	echo "Building installer failed."
	echo "================================================================"
	cd ${THISDIR}
	rm -Rf installer_temp	
	exit 1;
else
	rm -Rf ${THISDIR}/common/installer.jar
	mv installer.jar ${THISDIR}/common/installer.jar
fi

cd ${THISDIR}
rm -Rf installer_temp

echo "================================================================"
echo "Building linux installer"
echo "================================================================"
cd linux
./makeInstallerLinux.sh ${OPT}${JARFILE}-c -k ${BRANCH}${RELEASE} -p "${plugins} ${plugins_linux}"
cd ${THISDIR}

echo "================================================================"
echo "Building Windows installer"
echo "================================================================"
cd windows
./makeInstallerWindows.sh ${OPT}${JARFILE}-k -s ${BRANCH}${RELEASE} -p "${plugins} ${plugins_windows}"
cd ${THISDIR}

MD5BIN=`which md5sum`
if [ "${MD5BIN}" != "" ]; then
	echo "================================================================"
	echo "Creating MD5SUM files"
	echo "================================================================"
	cd output
	for outputFile in *; do
		if [ "${outputFile##*.}" != "md5" ]; then
			if [ -e "${outputFile}.md5" ]; then
				rm -f "${outputFile}.md5"
			fi
			${MD5BIN} "${outputFile}" > "${outputFile}.md5"
		fi
	done
	cd ${THISDIR}
fi

echo "================================================================"
echo "Release ready - see output folder"
echo "================================================================"
exit 0;