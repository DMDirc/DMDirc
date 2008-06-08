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

# Linux needs an entry in fstab to allow normal users to mount things (like a
# dmg image).
# These 2 config vars choose what dir and file we mount so that fstab can be
# correct, these should be full paths, and are ignored on OSX.
# This is the name of the image that gets mounted:
LINUXIMAGE=${PWD}/DMDirc.dmg
# This is the dir we mount it in
LINUXIMAGEDIR=${PWD}/dmg
# fstab entry should read:
# ${LINUXIMAGE} ${LINUXIMAGEDIR} auto users,noauto,loop 0 0

MKISOFS=`which mkisofs`
MKISOFS_WORKS="0"
PREFER_MKISOFS="1"
MKHFS=`which mkfs.hfs`
MKHFSPLUS=`which mkfs.hfsplus`
HDIUTIL=`which hdiutil`

JNIName="libDMDirc-Apple.jnilib"

if [ ! -e "${JNIName}" ]; then
	if [ -e "/System/Library/Frameworks/JavaVM.framework/Headers" ]; then
		GCC=`which gcc`
		${GCC} -dynamiclib -framework JavaVM -framework Carbon -o ${JNIName} DMDirc-Apple.c -arch x86_64
		if [ ! -e "${JNIName}" ]; then
			echo "JNI Lib not found and failed to compile. Aborting."
			exit 1;
		fi;
	else
		echo "JNI Lib not found, unable to compile on this system. Aborting."
		exit 1;
	fi;
fi;

USE_MKISOFS="0"
if [ "" != "${MKISOFS}" ]; then
	MKISOFS_TEST=`${MKISOFS} --help 2>&1 | grep apple`
	if [ "" != "${MKISOFS_TEST}" ]; then
		MKISOFS_WORKS="1"
		USE_MKISOFS="${PREFER_MKISOFS}"
	fi;
fi

if [ "" = "${HDIUTIL}" ]; then
	if [ "" = "${MKHFS}" -a "" = "${MKHFSPLUS}" ]; then
		if [ "1" != "${MKISOFS_WORKS}" ]; then
			echo "This machine is unable to produce dmg images. Aborting."
			exit 1;
		else
			USE_MKISOFS="1"
		fi;
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
echo "APPLDMDI" > ${CONTENTSDIR}/PkgInfo

if [ "${isRelease}" != "" ]; then
	if [ "${BRANCH}" = "1" ]; then
		bundleVersion=branch-${isRelease}
	else
		bundleVersion=${isRelease}
	fi
else
	bundleVersion="trunk-"`date +%Y%m%d_%H%M%S`
fi;


cat <<EOF> ${CONTENTSDIR}/Info.plist

<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist SYSTEM "file://localhost/System/Library/DTDs/PropertyList.dtd">
<plist version="0.9">
<dict>
	<key>CFBundleName</key>
	<string>DMDirc</string>
	<key>CFBundleIdentifier</key>
	<string>com.dmdirc.osx</string>
	<key>CFBundleAllowMixedLocalizations</key>
	<string>true</string>
	<key>CFBundleExecutable</key>
	<string>DMDirc.sh</string>
	<key>CFBundleDevelopmentRegion</key>
	<string>English</string>
	<key>CFBundlePackageType</key>
	<string>APPL</string>
	<key>CFBundleSignature</key>
	<string>DMDI</string>
	<key>CFBundleInfoDictionaryVersion</key>
	<string>6.0</string>
	<key>CFBundleIconFile</key>
	<string>dmdirc.icns</string>
	<key>CFBundleVersion</key>
	<string>${bundleVersion}</string>
	<key>CFBundleShortVersionString</key>
	<string>${bundleVersion}</string>
	<key>Java</key>
	<dict>
		<key>WorkingDirectory</key>
		<string>\$APP_PACKAGE/Contents/Resources/Java</string>
		<key>MainClass</key>
		<string>com.dmdirc.Main</string>
		<key>JVMVersion</key>
		<string>1.6+</string>
		<key>ClassPath</key>
		<string>\$JAVAROOT/DMDirc.jar</string>
	</dict>
	<key>CFBundleURLTypes</key>
	<array>
		<dict>
			<key>CFBundleURLName</key>
			<string>IRC URL</string>
			<key>CFBundleURLSchemes</key>
			<array>
				<string>irc</string>
			</array>
		</dict>
	</array>
</dict>
</plist>
EOF

#	<key>Properties</key>
#	<dict>
#		<key>com.apple.mrj.application.growbox.intrudes</key>
#		<string>false</string>
#		<key>com.apple.mrj.application.live-resize</key>
#		<string>true</string>
#		<key>com.apple.mrj.application.apple.menu.about.name</key>
#		<string>DMDirc</string>
#		<key>apple.laf.useScreenMenuBar</key>
#		<string>true</string>
#	</dict>

cp DMDirc.jar ${RESDIR}/Java/DMDirc.jar
cp ${JNIName} ${RESDIR}/Java/${JNIName}

#if [ -e "../common/installer.jar" ]; then
#	ln -sf ../common/installer.jar ./installer.jar
#	FILES="${FILES} installer.jar"
#else
#	echo "[WARNING] Creating installer-less archive - relying on setup.sh"
#fi

if [ -e ${jarPath}"/installer/osx/res/dmdirc.icns" ]; then
	cp ${jarPath}"/installer/osx/res/dmdirc.icns" ${RESDIR}/dmdirc.icns
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

if [ -e ${jarPath}"/installer/osx/res/VolumeIcon.icns" ]; then
	cp -v ${jarPath}"/installer/osx/res/VolumeIcon.icns" ${DMG}/.VolumeIcon.icns
fi

if [ -e ${jarPath}"/installer/osx/res/Background.png" ]; then
	mkdir ${DMG}/.background
	cp -v ${jarPath}"/installer/osx/res/Background.png" ${DMG}/.background/background.png
fi

if [ -e ${PWD}/.DS_Store ]; then
	cp -v ${PWD}/.DS_Store ${DMG}/.DS_Store
fi

# Now, make a dmg
if [ "" = "${HDIUTIL}" ]; then
	# Make sure the variables are set
	if [ "" = "${LINUXIMAGEDIR}" ]; then
		LINUXIMAGEDIR=${PWD}/dmg
	fi;
	if [ "" = "${LINUXIMAGED}" ]; then	
		LINUXIMAGE=${PWD}/DMDirc.dmg
	fi;

	# Non-OSX
	# This doesn't work quite aswell as on OSX, but it works.
	IMGBLOCKSIZE=4096 # OSX Default = 4096
	MVRES=0
	createImage() {
		if [ "1" = "${USE_MKISOFS}" ]; then
			# http://lists.apple.com/archives/java-dev/2008/Mar/msg00266.html
			
			${MKISOFS} -V 'DMDirc' -no-pad -r -z -apple -o "${LINUXIMAGE}" -hfs-creator "DMDI" -hfs-bless "/Volumes/DMDirc" "${DMG}"
		else
			MVRES=0
			SIZE=${1}
			if [ "" = "${MKHFS}" -a "" != "${MKHFSPLUS}" ]; then
				MKHFS=${MKHFSPLUS}
			fi;
			# mkfs.hfs will only create 512kb+ sized images
			if [ ${SIZE} -lt 524288 ]; then
				echo "Size is less than 512kb"
				SIZE=524288;
			fi;
			dd if=/dev/zero of=${LINUXIMAGE} bs=${SIZE} count=1
			${MKHFS} -b ${IMGBLOCKSIZE} -v 'DMDirc' ${LINUXIMAGE}
			if [ ${?} -ne 0 ]; then
				echo "mkfs.hfs failed to create the image, aborting."
				exit 1;
			fi;
			# Now try and mount
			# This could be a problem, as linux requires either root to mount, or an fstab
			# entry.
			# Try to mount, if this fails, let the user know what to add to fstab.
			if [ -e ${LINUXIMAGEDIR} ]; then
				rm -Rf ${LINUXIMAGEDIR}
			fi;
			mkdir ${LINUXIMAGEDIR}
			mount ${LINUXIMAGEDIR}
			MOUNTRES=${?}
			if [ ${MOUNTRES} -ne 0 ]; then
				# Try using full parameters - could be running as root.
				mount -t hfsplus -o loop ${RUNNAME} ${LINUXIMAGEDIR}
				MOUNTRES=${?}
			fi;
			if [ ${MOUNTRES} -ne 0 ]; then
				echo "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@"
				echo "@                               ERROR                               @"
				echo "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@"
				echo "You do not have permission to mount the image."
				echo "Please add the following lines to /etc/fstab and re run this script again"
				echo "# DMDirc OSX dmg image"
				echo "${LINUXIMAGE} ${LINUXIMAGEDIR} auto users,noauto,loop 0 0"
				echo "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@"
				echo "@                               ERROR                               @"
				echo "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@"
				exit 1;
			fi;
			mv -fv ${DMG}/* ${LINUXIMAGEDIR}
			MVRES=${?}
			if [ ${MVRES} -ne 0 ]; then
				echo "Image incorrect size."
			else
				mv -fv ${DMG}/.[A-Za-z]* ${LINUXIMAGEDIR}
				MVRES=${?}
				if [ ${MVRES} -ne 0 ]; then
					echo "Image incorrect size."
				fi;
			fi;
			umount ${LINUXIMAGEDIR}
			# If anyone finds out how to compress these nicely, add it here.
		fi;
	}
	
	if [ "1" = "${USE_MKISOFS}" ]; then
		createImage
	else
		INCREASE=10
		IMAGESIZE=$(((`du -s --block-size ${IMGBLOCKSIZE} ${DMG} | awk '{print $1}'` + ${INCREASE}) * ${IMGBLOCKSIZE} ))
		createImage ${IMAGESIZE}
		
		# This sucks, but it makes sure the image gets created at a size that contains
		# everything.
		# I can't manage to get du to return the size that the files actually end up
		# using on the image.
		TRIES=0
		while [ ${MVRES} -ne 0 -a ${TRIES} -ne 5 ]; do
			TRIES=$((${TRIES} + 1))
			echo "Previous ImageSize of ${IMAGESIZE} failed"
			INCREASE=$((${INCREASE} + 5))
			IMAGESIZE=$(((`du -s --block-size ${IMGBLOCKSIZE} ${DMG} | awk '{print $1}'` + ${INCREASE}) * ${IMGBLOCKSIZE} ))
			echo "Trying to build with ImageSize ${IMAGESIZE}"
			createImage ${IMAGESIZE}
		done;
		
		if [ ${MVRES} -ne 0 ]; then
			echo "OSX Build Failed - Imagesize: ${IMAGESIZE}"
			exit 1;
		fi;
	fi;
	
	if [ "${LINUXIMAGE}" != "${RUNNAME}" ]; then
		# Rename the image
		mv ${LINUXIMAGE} ${RUNNAME}
	fi;
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
	rm ${RUNNAME}.RW.dmg
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