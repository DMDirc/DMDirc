#!/bin/sh
#
# This script generates a .dmg file that includes dmdirc
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

# Final Name of the installer (without file extention)
INSTALLERNAME=DMDirc
# full name of the file to output to
RUNNAME="${PWD}/${INSTALLERNAME}.dmg"

MKHFS=`which mkfs.hfs`
MKHFSPLUS=`which mkfs.hfsplus`
HDIUTIL=`which hdiutil`

if [ "" = "${HDIUTIL}" ]; then
	if [ "" = "${MKHFS}" -o "" = "${MKHFSPLUS}" ]; then
		echo "This machine is unable to produce dmg images. Aborting."
		exit 1;
	fi;
fi;

# Go!
echo "-----------"
if [ -e "${RUNNAME}" ]; then
	echo "Removing existing .dmg file"
	rm -Rf ./*.dmg
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
			RUNNAME="${PWD}/${INSTALLERNAME}-${1}.dmg"
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

echo "Copying jar (${jarfile}).."
cp ${jarfile} "./DMDirc.jar"
if [ "" != "${plugins}" ]; then
	echo "Adding plugins to jar"
	ln -sf ${jarPath}"/plugins"
	pluginList=""
	for plugin in ${plugins}; do
		pluginList=${pluginList}" plugins/${plugin}"
	done
	jar -uvf "DMDirc.jar" ${pluginList}
	rm -Rf plugins;
fi

APPDIR="DMDirc.app"
CONTENTSDIR=${APPDIR}/Contents
RESDIR=${CONTENTSDIR}/Resources
MACOSDIR=${CONTENTSDIR}/MacOS

if [ -e "${APPDIR}" ]; then
	echo "Removing existing .app directory";
	rm -Rf ${APPDIR}
fi;

echo "Creating .app directory"
mkdir -pv ${APPDIR}
mkdir -pv ${CONTENTSDIR}
mkdir -pv ${RESDIR}
mkdir -pv ${RESDIR}/Java
mkdir -pv ${MACOSDIR}

echo "Creating meta files"
echo "APPL????" > ${CONTENTSDIR}/PkgInfo

if [ "${isRelease}" != "" ]; then
	if [ "${BRANCH}" = "1" ]; then
		bundleVersion=branch-${isRelease}
	else
		bundleVersion=${isRelease}
	fi
else
	bundleVersion="trunk-"`date +%Y%m%d_%H%M%S`
fi;

echo "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" > ${CONTENTSDIR}/Info.plist
echo "<!DOCTYPE plist SYSTEM \"file://localhost/System/Library/DTDs/PropertyList.dtd\">" >> ${CONTENTSDIR}/Info.plist
echo "<plist version=\"0.9\">" >> ${CONTENTSDIR}/Info.plist
echo "<dict>" >> ${CONTENTSDIR}/Info.plist
echo "	<key>CFBundleName</key>" >> ${CONTENTSDIR}/Info.plist
echo "	<string>DMDirc</string>" >> ${CONTENTSDIR}/Info.plist
echo "	<key>CFBundleAllowMixedLocalizations</key>" >> ${CONTENTSDIR}/Info.plist
echo "	<string>true</string>" >> ${CONTENTSDIR}/Info.plist
echo "	<key>CFBundleExecutable</key>" >> ${CONTENTSDIR}/Info.plist
echo "	<string>DMDirc.sh</string>" >> ${CONTENTSDIR}/Info.plist
echo "	<key>CFBundleDevelopmentRegion</key>" >> ${CONTENTSDIR}/Info.plist
echo "	<string>English</string>" >> ${CONTENTSDIR}/Info.plist
echo "	<key>CFBundlePackageType</key>" >> ${CONTENTSDIR}/Info.plist
echo "	<string>APPL</string>" >> ${CONTENTSDIR}/Info.plist
echo "	<key>CFBundleSignature</key>" >> ${CONTENTSDIR}/Info.plist
echo "	<string>????</string>" >> ${CONTENTSDIR}/Info.plist
echo "	<key>CFBundleInfoDictionaryVersion</key>" >> ${CONTENTSDIR}/Info.plist
echo "	<string>6.0</string>" >> ${CONTENTSDIR}/Info.plist
echo "	<key>CFBundleIconFile</key>" >> ${CONTENTSDIR}/Info.plist
echo "	<string>dmdirc.icns</string>" >> ${CONTENTSDIR}/Info.plist
echo "	<key>CFBundleVersion</key>" >> ${CONTENTSDIR}/Info.plist
echo "	<string>${bundleVersion}</string>" >> ${CONTENTSDIR}/Info.plist
echo "	<key>CFBundleShortVersionString</key>" >> ${CONTENTSDIR}/Info.plist
echo "	<string>${bundleVersion}</string>" >> ${CONTENTSDIR}/Info.plist
echo "	<key>Java</key>" >> ${CONTENTSDIR}/Info.plist
echo "	<dict>" >> ${CONTENTSDIR}/Info.plist
echo "		<key>WorkingDirectory</key>" >> ${CONTENTSDIR}/Info.plist
echo "		<string>\$APP_PACKAGE/Contents/Resources/Java</string>" >> ${CONTENTSDIR}/Info.plist
echo "		<key>MainClass</key>" >> ${CONTENTSDIR}/Info.plist
echo "		<string>com.dmdirc.Main</string>" >> ${CONTENTSDIR}/Info.plist
echo "		<key>JVMVersion</key>" >> ${CONTENTSDIR}/Info.plist
echo "		<string>1.6+</string>" >> ${CONTENTSDIR}/Info.plist
echo "		<key>ClassPath</key>" >> ${CONTENTSDIR}/Info.plist
echo "		<string>\$JAVAROOT/DMDirc.jar</string>" >> ${CONTENTSDIR}/Info.plist
echo "	</dict>" >> ${CONTENTSDIR}/Info.plist
#echo "	<key>Properties</key>" >> ${CONTENTSDIR}/Info.plist
#echo "	<dict>" >> ${CONTENTSDIR}/Info.plist
#echo "		<key>com.apple.mrj.application.growbox.intrudes</key>" >> ${CONTENTSDIR}/Info.plist
#echo "		<string>false</string>" >> ${CONTENTSDIR}/Info.plist
#echo "		<key>com.apple.mrj.application.live-resize</key>" >> ${CONTENTSDIR}/Info.plist
#echo "		<string>true</string>" >> ${CONTENTSDIR}/Info.plist
#echo "		<key>com.apple.mrj.application.apple.menu.about.name</key>" >> ${CONTENTSDIR}/Info.plist
#echo "		<string>DMDirc</string>" >> ${CONTENTSDIR}/Info.plist
#echo "		<key>com.apple.macos.useScreenMenuBar</key>" >> ${CONTENTSDIR}/Info.plist
#echo "		<string>true</string>" >> ${CONTENTSDIR}/Info.plist
#echo "	</dict>" >> ${CONTENTSDIR}/Info.plist
echo "</dict>" >> ${CONTENTSDIR}/Info.plist
echo "</plist>" >> ${CONTENTSDIR}/Info.plist

cp DMDirc.jar ${RESDIR}/Java/DMDirc.jar

#if [ -e "../common/installer.jar" ]; then
#	ln -sf ../common/installer.jar ./installer.jar
#	FILES="${FILES} installer.jar"
#else
#	echo "[WARNING] Creating installer-less archive - relying on setup.sh"
#fi

if [ -e ${jarPath}"/src/com/dmdirc/res/osx/dmdirc.icns" ]; then
	cp ${jarPath}"/src/com/dmdirc/res/osx/dmdirc.icns" ${RESDIR}/dmdirc.icns
fi

if [ "${isRelease}" != "" ]; then
	DOCSDIR=${jarPath}
else
	DOCSDIR="../common"
fi

if [ -e "${DOCSDIR}/README.TXT" ]; then
	cp "${DOCSDIR}/README.TXT" ${RESDIR}/README.TXT
fi

if [ -e "${DOCSDIR}/CHANGES.TXT" ]; then
	cp "${DOCSDIR}/CHANGES.TXT" ${RESDIR}/CHANGES.TXT
elif [ -e "${DOCSDIR}/CHANGELOG.TXT" ]; then
	cp "${DOCSDIR}/CHANGELOG.TXT" ${RESDIR}/CHANGELOG.TXT
fi

if [ -e "${jarPath}/launcher/osx" ]; then
	cp ${jarPath}/launcher/osx/DMDirc.sh ${MACOSDIR}/DMDirc.sh
	chmod a+x ${MACOSDIR}/DMDirc.sh
fi

echo "Packaging.."
# Create RUNNAME
# Create temp dir
DMG=package
if [ -e ${DMG} ]; then
	rm -Rf ${DMG}
fi;
mkdir ${DMG}
# Copy the application
mv ${APPDIR} ${DMG}/
# link to /Applications to allow easy drag and drop
ln -sf /Applications ${DMG}/

if [ -e ${jarPath}"/src/com/dmdirc/res/osx/VolumeIcon.icns" ]; then
	cp -v ${jarPath}"/src/com/dmdirc/res/osx/VolumeIcon.icns" ${DMG}/.VolumeIcon.icns
fi

if [ -e ${jarPath}"/src/com/dmdirc/res/osx/Background.png" ]; then
	mkdir ${DMG}/.background
	cp -v ${jarPath}"/src/com/dmdirc/res/osx/Background.png" ${DMG}/.background/background.png
fi

if [ -e ${PWD}/.DS_Store ]; then
	cp -v ${PWD}/.DS_Store ${DMG}/.DS_Store
fi

# Now, make a dmg
DMGMOUNTDIR=""
if [ "" = "${HDIUTIL}" ]; then
	SIZE=$((`du -sb ${DMG} | awk '{print $1}'`  + 10))
	DMGMOUNTDIR=${PWD}/dmg
	# Non-OSX
	# This doesn't work quite aswell as on OSX, but it works.
	if [ "" = "${MKHFS}" -a "" != "${MKHFSPLUS}" ]; then
		MKHFS=${MKHFSPLUS}
	fi;
	# mkfs.hfs will only create 4mb+ sized iamges :/
	if [ ${SIZE} -lt 4194304 ]; then
		echo "Size is less than 4MB"
		SIZE=4194304;
	fi;
	dd if=/dev/zero of=${RUNNAME} bs=${SIZE} count=1
	${MKHFS} -v 'DMDirc' ${RUNNAME}
	# Now try and mount
	# This could be a problem, as linux requires either root to mount, or an fstab
	# entry.
	# Try to mount, if this fails, let the user know what to add to fstab.
	if [ -e ${DMGMOUNTDIR} ]; then
		rm -Rf ${DMGMOUNTDIR}
	fi;
	mkdir ${DMGMOUNTDIR}
	mount ${DMGMOUNTDIR}
	MOUNTRES=${?}
	if [ ${MOUNTRES} -ne 0 ]; then
		# Try using full parameters - could be running as root.
		mount -t hfsplus -o loop ${RUNNAME} ${DMGMOUNTDIR}
		MOUNTRES=${?}
	fi;
	if [ ${MOUNTRES} -ne 0 ]; then
		echo "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@"
		echo "@                               ERROR                               @"
		echo "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@"
		echo "You do not have permission to mount the image."
		echo "Please add the following lines to /etc/fstab and rcd oun this script again"
		echo "# DMDirc OSX dmg image"
		echo "${RUNNAME} ${DMGMOUNTDIR} auto users,noauto,loop 0 0"
		echo "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@"
		echo "@                               ERROR                               @"
		echo "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@"
		exit 1;
	fi;
	mv -fv ${DMG}/* ${DMGMOUNTDIR}
	mv -fv ${DMG}/.[A-Za-z]* ${DMGMOUNTDIR}
	umount ${DMGMOUNTDIR}
	# If anyone finds out how to compress these nicely, add it here.
else
	# OSX
	# This creates better versions than non-OSX
	
	# Create Read/Write image
	${HDIUTIL} create -volname "DMDirc" -fs HFS+ -srcfolder ${DMG} -format UDRW ${RUNNAME}.RW.dmg
	# Make it auto-open
	BLESS=`which bless`
	if [ "" != "${BLESS}" ]; then
		if [ -e /Volumes/DMDirc ]; then
			${HDIUTIL} detach /Volumes/DMDirc
		fi;
		if [ ! -e /Volumes/DMDirc ]; then
			${HDIUTIL} attach ${RUNNAME}.RW.dmg
			${BLESS} -openfolder /Volumes/DMDirc
			${HDIUTIL} detach /Volumes/DMDirc
		fi;
	fi;
	# Convert to compressed read-only image
	${HDIUTIL} convert ${RUNNAME}.RW.dmg -format UDZO -imagekey zlib-level=9 -o ${RUNNAME}
fi;

echo "DMG Creation complete!"

if [ "${isRelease}" != "" ]; then
	if [ "${BRANCH}" = "1" ]; then
		isRelease=branch-${isRelease}
	fi;
	if [ "" != "${finalTag}" ]; then
		finalTag="-${finalTag}"
	fi;
	finalname=DMDirc-${isRelease}${finalTag}.dmg
else
	finalname=${RUNNAME##*/}
fi;

mv ${RUNNAME} ../output/${finalname}

rm -Rfv ${DMG} ${APPDIR} ${DMGMOUNTDIR} DMDirc.jar

echo "-----------"
echo "Done."
echo "-----------"

# and Done \o
exit 0;