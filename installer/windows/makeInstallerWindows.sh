#!/bin/sh
#
# This script generates a .exe file that will install DMDirc
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

# Name of the extractor
RUNNAME=extractor.exe
# Name of the installer (without .exe)
INSTALLNAME=DMDirc-Setup
# Name of the internal file
INTNAME=extractor.7z
# full name of the files to output to
RUNNAME="${PWD}/${RUNNAME}"
INTNAME="${PWD}/${INTNAME}"
# Get 7zip path
ZIP=`which 7z`

if [ "" = "${ZIP}" ]; then
	echo "7Zip not found, failing."
	exit 1;
fi

# Compress stuff!
compress() {
	${ZIP} a -yl ${INTNAME} $@ 2>/dev/null || {
		echo "Compression failed."
		kill -15 $$;
	};
}

# Get signcode path
SIGNCODE=`which signcode`

if [ "" = "${SIGNCODE}" ]; then
	echo "Signcode not found. EXE's will not be digitally signed."
	exit 1;
fi

# Sign stuff!
signexe() {
return;
	if [ "" != "${SIGNCODE}" ]; then
		if [ -e "../signing/DMDirc.spc" -a -e "../signing/DMDirc.pvk" ]; then
			echo "Digitally Signing EXE (${@})..."
			${SIGNCODE} -spc "../signing/DMDirc.spc" -v "../signing/DMDirc.pvk" -i "http://www.dmdirc.com/" -n "DMDirc Installer" $@ 2>/dev/null || {
				kill -15 $$;
			};
			rm ${@}.sig
			rm ${@}.bak
		fi
	fi
}

# Go!
echo "-----------"
if [ -e "${RUNNAME}" ]; then
	echo "Removing existing .exe file"
	rm -Rf "${RUNNAME}"
fi
if [ -e "${INTNAME}" ]; then
	echo "Removing existing .7z file"
	rm -Rf "${INTNAME}"
fi
echo "Creating .7z file"

# Check for some CLI params
compileJar="false"
updateSVN="true"
compileSetup="false"
useOldSetup="false"
isRelease=""
useUPX="false"
finalTag=""
signEXE="true"
compilerFlags=""
BRANCH="0"
location="../../../"

showHelp() {
	echo "This will generate a DMDirc installer for a windows based system."
	echo "The following command line arguments are known:"
	echo "---------------------"
	echo "-h, --help                Help information"
	echo "-r, --release [version]   Generate a file based on an svn tag (or branch with -b aswell)"
	echo "-b, --branch              Release in -r is a branch "
	echo "-s, --setup               Recompile the .exe file"
	echo "-e,                       If setup.exe compile fails, use old version"
	echo "-c, --compile             Recompile the .jar file"
	echo "-u, --unsigned            Don't sign the exe"
	echo "-t, --tag [tag]           Tag to add to final exe name to distinguish this build from a standard build"
	echo "-f, --flags [flags]       Extra flags to pass to the compiler"	
# This is not in the help cos its crappy really, and makes little/no difference to the
# exe size unless debugging information is added using --flags, in which case the person
# probably is Dataforce and knows about this flag anyway
#	echo "    --upx                 UPX binary if UPX is available on the path,"
#	echo "                          (Compression Level: 4 for signed exe, 9 for unsigned)"
	echo "-k, --keep                Keep the existing source tree when compiling"
	echo "                          (don't svn update beforehand)"
	echo "---------------------"
	exit 0;
}

while test -n "$1"; do
	case "$1" in
		--compile|-c)
			compileJar="true"
			;;
		--setup|-s)
			compileSetup="true"
			;;
		-e)
			useOldSetup="true"
			;;
		--release|-r)
			shift
			isRelease=${1}
			;;
		--tag|-t)
			shift
			finalTag="-${1}"
			;;
		--flags|-f)
			shift
			compilerFlags="${1} "
			;;
		--upx)
			useUPX="true"
			;;
		--unsigned|-u)
			signEXE="false"
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

jarPath="${location}trunk"
if [ "${isRelease}" != "" ]; then
	if [ "${BRANCH}" != "0" ]; then
		if [ -e "${location}branches/"${isRelease} ]; then
			jarPath="${location}branches/"${isRelease}
		else
			echo "Branch "${isRelease}" not found."
			exit 1;
		fi
	else
		if [ -e "${location}tags/"${isRelease} ]; then
			jarPath="${location}tags/"${isRelease}
		else
			echo "Tag "${isRelease}" not found."
			exit 1;
		fi
	fi
fi

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
		exit 0;
	fi;
	cd ${OLDPWD}
fi;

echo "Linking jar.."
ln -s ${jarPath}"/dist/DMDirc.jar" "./DMDirc.jar"

FILES="DMDirc.jar Setup.exe";
if [ ! -e "Setup.exe"  -o "${compileSetup}" = "true" ]; then
	echo "Setup.exe does not exist. Lets try and compile it."	
	FPC=`which fpc`
	if [ "${FPC}" = "" ]; then
		echo "FPC Compiler not found, Setup.exe can not be built."
		exit 1;
	else
		${FPC} -Sd -Twin32 ${compilerFlags}Setup.dpr
		if [ $? -ne 0 ]; then
			if [ -e "Setup.exe" -a "${useOldSetup}" = "true" ]; then
				echo "Unable to compile Setup.exe, using existing version."
			else
				echo "Unable to compile Setup.exe, terminating."
				exit 1;
			fi
		fi;
	fi
fi

ls
if [ ! -e "Setup.exe" ]; then
	echo "Still can't find Setup.exe, terminating."
	exit 1;
fi

echo "Compressing files.."

if [ -e "../common/installer.jar" ]; then
	ln -s ../common/installer.jar ./installer.jar
	FILES="${FILES} installer.jar"
else
	echo "[WARNING] Creating installer-less archive - relying on Setup.exe"
fi 

if [ -e ${jarPath}"/src/com/dmdirc/res/icon.ico" ]; then
	ln -s ${jarPath}"/src/com/dmdirc/res/icon.ico" ./icon.ico
	FILES="${FILES} icon.ico"
fi

# Shortcut.exe is from http://www.optimumx.com/download/#Shortcut
if [ ! -e Shortcut.exe ]; then
	wget http://www.optimumx.com/download/Shortcut.zip
	unzip -q Shortcut.zip Shortcut.exe
	rm Shortcut.zip
fi
FILES="${FILES} Shortcut.exe"

if [ "${isRelease}" != "" ]; then
	DOCSDIR=${jarPath}
else
	DOCSDIR="../common"
fi

if [ -e "${DOCSDIR}/README.TXT" ]; then
	ln -s "${DOCSDIR}/README.TXT" .
	FILES="${FILES} README.TXT"
fi

if [ -e "${DOCSDIR}/CHANGES.TXT" ]; then
	ln -s "${DOCSDIR}/CHANGES.TXT" .
	FILES="${FILES} CHANGES.TXT"
elif [ -e "${DOCSDIR}/CHANGELOG.TXT" ]; then
	ln -s "${DOCSDIR}/CHANGELOG.TXT" .
	FILES="${FILES} CHANGELOG.TXT"
fi

if [ -e "${jarPath}/launcher/windows" ]; then
	# Try to compile all
	olddir=${PWD}
	cd "${jarPath}/launcher/windows/"
	sh compile.sh
	cd ${olddir}
	# Now add to file list.
	for thisfile in `ls -1 ${jarPath}/launcher/windows/*.exe`; do
		ln -s "${jarPath}/launcher/windows/"${thisfile} .
		FILES="${FILES} ${thisfile}"
	done
fi

compress $FILES

echo "Creating config.."
echo ";!@Install@!UTF-8!" > 7zip.conf
if [ "${isRelease}" != "" ]; then
	echo "Title=\"DMDirc Installation "${isRelease}"\"" >> 7zip.conf
#	echo "BeginPrompt=\"Do you want to install DMDirc "${isRelease}"?\"" >> 7zip.conf
else
	echo "Title=\"DMDirc Installation\"" > 7zip.conf
#	echo "BeginPrompt=\"Do you want to install DMDirc?\"" >> 7zip.conf
fi;
echo "ExecuteFile=\"Setup.exe\"" >> 7zip.conf
echo ";!@InstallEnd@!" >> 7zip.conf

if [ ! -e "7zS.sfx" ]; then
	echo "Obtaining sfx stub.."
	wget http://kent.dl.sourceforge.net/sourceforge/sevenzip/7z447_extra.tar.bz2
	tar -jxvf 7z447_extra.tar.bz2 7zS.sfx
	rm 7z447_extra.tar.bz2
fi

echo "Creating .exe"
cat 7zS.sfx 7zip.conf "${INTNAME}" > "${RUNNAME}"

if [ "${isRelease}" != "" ]; then
	ORIGNAME="DMDirc-${isRelease}-Setup${finalTag}.exe"
else
	ORIGNAME="${INSTALLNAME}${finalTag}.exe"
fi;

echo "Building launcher";
sh makeLauncher.sh "${isRelease}" "${ORIGNAME}" "${compilerFlags}"
if [ $? -ne 0 ]; then
	exit 1;
fi
FULLINSTALLER="${PWD}/${INSTALLNAME}${finalTag}.exe"
mv Launcher.exe ${FULLINSTALLER}

if [ "${useUPX}" = "true" ]; then
	UPX=`which upx`
	if [ "${UPX}" != "" ]; then	
		if [ "${signEXE}" = "true" ]; then
			${UPX} -4 ${FULLINSTALLER}
		else
			${UPX} -9 ${FULLINSTALLER}
		fi
	fi
fi

echo "Chmodding.."
chmod a+x ${FULLINSTALLER}
if [ "${signEXE}" = "true" ]; then
	echo "Signing.."
	signexe ${FULLINSTALLER}
else
	echo "Not Signing.."
fi;

mv ${FULLINSTALLER} ../output/${ORIGNAME}

# Quick hack to prevent deleting of 2 important files in ${FILES}
mv Setup.exe Setup.exe.tmp
mv Shortcut.exe Shortcut.exe.tmp

rm -f ${FILES}
rm -f ./7zip.conf
rm -f ./*.o
rm -f ./*.or
rm -f ${RUNNAME}
rm -f ${INTNAME}
rm -f icon.ico

# And un-hack
mv Setup.exe.tmp Setup.exe
mv Shortcut.exe.tmp Shortcut.exe

echo "-----------"
echo "Done."
echo "-----------"

# and Done \o
exit 0;