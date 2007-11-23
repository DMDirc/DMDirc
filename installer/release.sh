#!/bin/sh

showHelp() {
	echo "This will generate the different DMDirc installers."
	echo "Usage: ${0} [params] <release>"
	echo "Release can be either 'trunk', a valid tag, or a branch (if -b is passed)"
	echo "The following params are known:"
	echo "---------------------"
	echo "-b, --branch              <release> is a branch"
	echo "-h, --help                Help information"
	echo "-o, --opt [options]       Additional options to pass to the make*Installer.sh files"
	echo "---------------------"
	exit 0;
}

# Check for some CLI params
LAST=""
OPT=""
BRANCH=""
while test -n "$1"; do
	LAST=${1}
	case "$1" in
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

if [ "${LAST}" != "" ]; then
	if [ "${LAST}" = "trunk" ]; then
		RELEASE=""
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
	echo "Release can be either 'trunk' or a valid tag. (see ${0} --help for further information)"
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
${JAVAC} -d ./build com/dmdirc/installer/*.java
${JAVAC} -d ./build com/dmdirc/installer/cliparser/*.java
${JAVAC} -d ./build com/dmdirc/ui/swing/dialogs/wizard/*.java
${JAVAC} -d ./build com/dmdirc/ui/interfaces/MainWindow.java
${JAVAC} -d ./build com/dmdirc/ui/swing/MainFrame.java
${JAVAC} -d ./build com/dmdirc/ui/swing/UIUtilities.java
${JAVAC} -d ./build com/dmdirc/ui/swing/UIUtilities.java
${JAVAC} -d ./build com/dmdirc/ui/swing/components/StandardDialog.java
${JAVAC} -d ./build com/dmdirc/util/ListenerList.java
${JAVAC} -d ./build com/dmdirc/util/WeakMapList.java
${JAVAC} -d ./build com/dmdirc/util/WeakList.java
${JAVAC} -d ./build net/miginfocom/layout/*.java
${JAVAC} -d ./build net/miginfocom/swing/*.java
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
./makeInstallerLinux.sh ${OPT}-c -k ${BRANCH}${RELEASE}
cd ${THISDIR}

echo "================================================================"
echo "Building Windows installer"
echo "================================================================"
cd windows
./makeInstallerWindows.sh ${OPT}-k -s ${BRANCH}${RELEASE}
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