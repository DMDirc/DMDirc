#!/bin/sh
#
# This script generates a .exe file that will install DMDirc
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

# Are we a git working copy, or SVN?
if [ -e ".svn" ]; then
	isSVN=1
else
	isSVN=0
fi;

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

# Check for some CLI params
compileJar="false"
updateSVN="true"
compileSetup="false"
useOldSetup="false"
isRelease=""
useUPX="false"
finalTag=""
signEXE="true"
compilerFlags="-Xs -XX -O2 -Or -Op1"
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
jrename="jre" # Filename for JRE without the .exe
TAGGED=""

showHelp() {
	echo "This will generate a DMDirc installer for a windows based system."
	echo "The following command line arguments are known:"
	echo "---------------------"
	echo "-h, --help                Help information"
	echo "-r, --release <version>   Generate a file based on an svn tag (or branch with -b as well)"
	echo "-b, --branch              Release in -r is a branch "
	echo "-s, --setup               Recompile the .exe file"
	echo "-o,                       If setup.exe compile fails, use old version"
	echo "-p, --plugins <plugins>   What plugins to add to the jar file"
	echo "-c, --compile             Recompile the .jar file"
	echo "-u, --unsigned            Don't sign the exe"
	echo "-e, --extra <tag>         Tag to add to final exe name to distinguish this build from a standard build"
	echo "-f, --flags <flags>       Extra flags to pass to the compiler"
	echo "    --jre                 Include the JRE in this installer"
	echo "    --jar <file>          use <file> as DMDirc.jar"
	echo "    --current             Use the current folder as the base for the build"
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
		--plugins|-p)
			shift
			plugins=${1}
			;;
		--jar)
			shift
			jarfile=${1}
			;;
		--jre)
			jre="http://www.dmdirc.com/getjava/windows/all"
			;;
		--jre64)
			# No specific jre64 for windows.
			echo "No specific 64ibt JRE for windows, exiting"
			exit 1;
			;;
		--current)
			location="../../"
			current="1"
			;;
		--compile|-c)
			compileJar="true"
			;;
		--setup|-s)
			compileSetup="true"
			;;
		-o)
			useOldSetup="true"
			;;
		--release|-r)
			shift
			isRelease=${1}
			;;
		--extra|-e)
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

if [ $isSVN -eq 1 -o "${TAGGED}" = "" ]; then
	echo "	ReleaseNumber: String = '${isRelease}';" > SetupConsts.inc
else
	echo "	ReleaseNumber: String = '${TAGGED}';" > SetupConsts.inc
fi;

FILES=""
# Icon Res file
if [ -e ${jarPath}"/src/com/dmdirc/res/icon.ico" ]; then
	ln -sf ${jarPath}"/src/com/dmdirc/res/icon.ico" ./icon.ico
	FILES="${FILES} icon.ico"
fi
echo "icon.ico ICON icon.ico" > icon.rc

# Other resources
echo "extractor RCDATA extractor.exe" > files.rc

COMPILER_IS_BROKEN="0";

if [ $isSVN -eq 1 -o "${TAGGED}" = "" ]; then
	NUM="${1}"
else
	NUM="${TAGGED}"
fi;



# Version Numbers
if [ "" = "${NUM}" ]; then
	MAJORVER="0"
	MINORVER="0"
	RELEASE="0"
	EXTRAVER="0"
	TEXTVER="${isRelease}"
	PRIVATE="1"
	USER=`whoami`
	HOST=`hostname`
	DATE=`date`
else
	MAJORVER=${NUM%%.*}
	SUBVER=${NUM#*.}
	EXTRAVER="0"
	DOT=`expr index "${SUBVER}" .`
	if [ "${DOT}" = "0" ]; then
		MINORVER=${SUBVER}
		RELEASE="0"
	else
		MINORVER=${SUBVER%%.*}
		END=${SUBVER##*.}
		RELEASE=${END##*[^0-9]}
	fi
	TEXTVER=$NUM
	PRIVATE="0"
fi;

# Information for the below section:
#
# http://support.microsoft.com/kb/139491
# http://msdn2.microsoft.com/en-us/library/aa381049.aspx
# http://courses.cs.vt.edu/~cs3304/FreePascal/doc/prog/node14.html#SECTION001440000000000000000
# http://tortoisesvn.tigris.org/svn/tortoisesvn/trunk/src/Resources/TortoiseShell.rc2

echo "1 VERSIONINFO" > version.rc.1
echo "FILEVERSION 1, 0, 0, 0" >> version.rc.1
echo "PRODUCTVERSION ${MAJORVER}, ${MINORVER}, ${RELEASE}, ${EXTRAVER}" >> version.rc.1
if [ "${PRIVATE}" = "1" ]; then
	if [ "${COMPILER_IS_BROKEN}" = "0" ]; then
		echo "FILEFLAGSMASK 0x000A" >> version.rc.1
		echo "FILEFLAGS 0x3f" >> version.rc.1
	else
		echo "FILEFLAGS 0x000A" >> version.rc.1
	fi;
else
	echo "FILEFLAGSMASK 0" >> version.rc.1
fi;
echo "FILEOS 0x40004" >> version.rc.1
echo "FILETYPE 1" >> version.rc.1
echo "BEGIN" >> version.rc.1
echo "	BLOCK \"StringFileInfo\"" >> version.rc.1
echo "	BEGIN" >> version.rc.1
echo "		BLOCK \"040004E4\"" >> version.rc.1
echo "		BEGIN" >> version.rc.1
echo "			VALUE \"Comments\", \"http://www.dmdirc.com/\"" >> version.rc.1
echo "			VALUE \"CompanyName\", \"DMDirc\"" >> version.rc.1
cat version.rc.1 > version.rc
cat version.rc.1 > uninstallversion.rc
rm version.rc.1
echo "			VALUE \"FileDescription\", \"Installer for DMDirc ${TEXTVER}\"" >> version.rc
echo "			VALUE \"FileDescription\", \"Uninstaller for DMDirc\"" >> uninstallversion.rc

echo "			VALUE \"FileVersion\", \"2.0\"" > version.rc.2
echo "			VALUE \"InternalName\", \"DMDirc.jar\"" >> version.rc.2
echo "			VALUE \"LegalCopyright\", \"Copyright (c) 2006-2009 Chris Smith, Shane Mc Cormack, Gregory Holmes\"" >> version.rc.2
echo "			VALUE \"OriginalFilename\", \"$2\"" >> version.rc.2
echo "			VALUE \"ProductName\", \"DMDirc\"" >> version.rc.2
echo "			VALUE \"ProductVersion\", \"${TEXTVER}\"" >> version.rc.2
if [ "${PRIVATE}" = "1" ]; then
	echo "			VALUE \"PrivateBuild\", \"Build by ${USER}@${HOST} on ${DATE}\"" >> version.rc.2
fi;
echo "		END" >> version.rc.2
echo "	END" >> version.rc.2
echo "	BLOCK \"VarFileInfo\"" >> version.rc.2
echo "	BEGIN" >> version.rc.2
echo "		VALUE \"Translation\", 0x400, 1252" >> version.rc.2
echo "	END" >> version.rc.2
echo "END" >> version.rc.2


cat version.rc.2 >> version.rc
cat version.rc.2 >> uninstallversion.rc
rm version.rc.2

echo "1 24 \"UAC.manifest\"" > UAC.rc
echo "1 24 \"UAC_uninstaller.manifest\"" > UAC_uninstaller.rc

# Build res files
#windres -F pe-i386 -i version.rc -o version.res
#windres -F pe-i386 -i files.rc -o files.res
#windres -F pe-i386 -i icon.rc -o icon.res

# UAC really needs to match the product name / description properly
# so we have a special one for the uninstaller
cat UAC_uninstaller.rc > uninstall.rc
# Next line seems to be silly because we add a version resource
# later as all.rc is not for the uninstaller.
# cat uninstallversion.rc >> all.rc
cat uninstallversion.rc >> uninstall.rc
cat icon.rc >> uninstall.rc

windres -F pe-i386 -i uninstall.rc -o uninstall.res

cat UAC.rc > all.rc
cat version.rc >> all.rc
cat files.rc >> all.rc
cat icon.rc >> all.rc
# Build later after extractor.exe exists

cat UAC.rc > most.rc
cat version.rc >> most.rc
cat icon.rc >> most.rc

windres -F pe-i386 -i most.rc -o most.res

FILES="${FILES} DMDirc.jar Setup.exe";
if [ "" != "${jre}" ]; then
	if [ ! -e "../common/${jrename}.exe" ]; then
		echo "Downloading JRE to include in installer"
		getFile "${jre}" "../common/${jrename}.exe"
	fi
	ln -sf ../common/${jrename}.exe jre.exe
	FILES="${FILES} jre.exe"
fi;
DELETEFILES=${FILES}
if [ "" != "${DMDIRC_FPC}" ]; then
	FPC=${DMDIRC_FPC}
else
	FPC=`which fpc`
fi;
lazarusDir="/usr/share/lazarus"
if [ ! -e "${lazarusDir}/lcl" ]; then
	lazarusDir="/usr/lib/lazarus/"
fi;

compilerFlags="-Sd -Twin32 ${compilerFlags}";
extraFlags=""
if [ ! -e "Setup.exe"  -o "${compileSetup}" = "true" ]; then
	echo "Setup.exe does not exist. Lets try and compile it."
	if [ "${FPC}" = "" ]; then
		echo "FPC Compiler not found, Setup.exe can not be built."
		exit 1;
	else
		echo "Building Setup.exe..."
		extraFlags="-dKOL -Fu${PWD}/../../libwin/kolfpc -Fu{$PWD}/../../libwin/lcore -Fu{$PWD} -Fu${PWD}/../../libwin"
		${FPC} ${compilerFlags} ${extraFlags} Setup.dpr
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

# Shortcut.exe is from http://www.optimumx.com/download/#Shortcut
if [ ! -e Shortcut.exe ]; then
	getFile "http://binary.dmdirc.com/Shortcut.zip" "Shortcut.zip"
	unzip -q Shortcut.zip Shortcut.exe
	rm Shortcut.zip
fi
FILES="${FILES} Shortcut.exe"
DELETEFILES="${DELETEFILES} Shortcut.exe"

if [ "${isRelease}" != "" ]; then
	DOCSDIR=${jarPath}
else
	DOCSDIR="../common"
fi

if [ -e "${DOCSDIR}/README.TXT" ]; then
	ln -sf "${DOCSDIR}/README.TXT" .
	FILES="${FILES} README.TXT"
	DELETEFILES="${DELETEFILES} README.TXT"
fi

if [ -e "${DOCSDIR}/CHANGES.TXT" ]; then
	ln -sf "${DOCSDIR}/CHANGES.TXT" .
	FILES="${FILES} CHANGES.TXT"
	DELETEFILES="${DELETEFILES} CHANGES.TXT"
elif [ -e "${DOCSDIR}/CHANGELOG.TXT" ]; then
	ln -sf "${DOCSDIR}/CHANGELOG.TXT" .
	FILES="${FILES} CHANGELOG.TXT"
	DELETEFILES="${DELETEFILES} CHANGELOG.TXT"
fi

if [ -e "${jarPath}/launcher/windows" ]; then
	# Try to compile all
	olddir=${PWD}
	cd "${jarPath}/launcher/windows/"
	sh compile.sh
	cd ${olddir}
	# Now add to file list.
	for thisfile in `ls -1 ${jarPath}/launcher/windows/*.exe`; do
		ln -sf ${thisfile} .
		FILES="${FILES} ${thisfile}"
	done
fi

extraFlags="-dKOL -Fu${PWD}/../../libwin/kolfpc -Fu{$PWD} -Fu${PWD}/../../libwin"
${FPC} ${compilerFlags} ${extraFlags} ${3}Uninstaller.dpr
if [ -e "Uninstaller.exe" ]; then
	FILES="${FILES} Uninstaller.exe"
#	DELETEFILES="${DELETEFILES} Uninstaller.exe"
fi

# Add wget to allow downloading jre
if [ ! -e "wget.exe" ]; then
	getFile "http://binary.dmdirc.com/wget.exe" "wget.exe"
fi;

if [ ! -e "wget.exe" ]; then
	echo "wget.exe not found, unable to continue."
	exit 1;
fi;

FILES="${FILES} wget.exe"

compress $FILES

echo "Creating config.."
echo ";!@Install@!UTF-8!" > 7zip.conf
if [ "${isRelease}" != "" ]; then
	echo "Title=\"DMDirc Installation "${isRelease}"\"" >> 7zip.conf
	echo "BeginPrompt=\"Do you want to install DMDirc "${isRelease}"?\"" >> 7zip.conf
elif [ "${TAGGED}" != "" ]; then
	echo "Title=\"DMDirc Installation "${TAGGED}"\"" >> 7zip.conf
	echo "BeginPrompt=\"Do you want to install DMDirc "${TAGGED}"?\"" >> 7zip.conf
else
	echo "Title=\"DMDirc Installation\"" >> 7zip.conf
	echo "BeginPrompt=\"Do you want to install DMDirc?\"" >> 7zip.conf
fi;
echo "ExecuteFile=\"Setup.exe\"" >> 7zip.conf
echo ";!@InstallEnd@!" >> 7zip.conf

if [ ! -e "7zS.sfx" ]; then
	echo "Obtaining sfx stub.."
	getFile "http://binary.dmdirc.com/7zS.sfx" "7zS.sfx"
fi

if [ ! -e "7zS.sfx" ]; then
	echo "7zS.sfx not found, unable to continue."
	exit 1;
fi;

echo "Creating .exe"
cat 7zS.sfx 7zip.conf "${INTNAME}" > "${RUNNAME}"

doRename=0
if [ "${isRelease}" != "" ]; then
	doRename=1
elif [ $isSVN -eq 0 -a "${TAGGED}" != "" ]; then
	doRename=1
fi;

if [ ${doRename} -eq 1 ]; then
	if [ $isSVN -eq 1 ]; then
		if [ "${BRANCH}" = "1" ]; then
			releaseTag=branch-${isRelease}
		else
			releaseTag=${isRelease}
		fi;
	else
		if [ "${TAGGED}" = "" ]; then
			releaseTag=branch-${isRelease}
		else
			releaseTag=${TAGGED}
		fi;
	fi
	ORIGNAME="DMDirc-${releaseTag}-Setup${finalTag}.exe"
else
	ORIGNAME="${INSTALLNAME}${finalTag}.exe"
fi;

echo "Building launcher";

MD5BIN=`which md5sum`
AWK=`which awk`
MD5SUM=""
if [ "${MD5BIN}" != "" -a "${AWK}" != "" ]; then
	MD5SUM=`${MD5BIN} extractor.exe | ${AWK} '{print $1}'`
fi
echo "const" > consts.inc
echo "	MD5SUM: String = '${MD5SUM}';" >> consts.inc

# Code to extract and launch resource
echo "ExtractResource('extractor', 'dmdirc_extractor.exe', TempDir);" > ExtractCode.inc
if [ "${MD5SUM}" != "" ]; then
	echo "if FindCmdLineSwitch('-nomd5') or FindCmdLineSwitch('nomd5') or checkMD5(TempDir+'dmdirc_extractor.exe') then begin" >> ExtractCode.inc
	echo -n "	"; # Oh so important for code formatting!
fi;
echo "Launch(TempDir+'dmdirc_extractor.exe');" >> ExtractCode.inc
if [ "${MD5SUM}" != "" ]; then
	echo "end" >> ExtractCode.inc
	echo "else begin" >> ExtractCode.inc
	echo "	ErrorMessage := 'This copy of the DMDirc installer appears to be damaged and will now exit';" >> ExtractCode.inc
	echo "	ErrorMessage := ErrorMessage+#13#10+'You may choose to skip this check and run it anyway by passing the /nomd5 parameter';" >> ExtractCode.inc
	echo "	ErrorMessage := ErrorMessage+#13#10+'';" >> ExtractCode.inc
	echo "	ErrorMessage := ErrorMessage+#13#10;" >> ExtractCode.inc
	echo "	ErrorMessage := ErrorMessage+#13#10+'If you feel this is incorrect, or you require some further assistance,';" >> ExtractCode.inc
	echo "	ErrorMessage := ErrorMessage+#13#10+'please feel free to contact us.';" >> ExtractCode.inc
	echo "	" >> ExtractCode.inc
	echo "	MessageBox(0, PChar(ErrorMessage), 'Sorry, setup is unable to continue', MB_OK + MB_ICONSTOP);" >> ExtractCode.inc
	echo "end;" >> ExtractCode.inc
fi

windres -F pe-i386 -i all.rc -o all.res

${FPC} ${compilerFlags} ${3}Launcher.dpr
if [ $? -ne 0 ]; then
	if [ -e "Launcher.exe" ]; then
		echo "Unable to compile Launcher.exe, using existing version."
	else
		echo "Unable to compile Launcher.exe, terminating."
		exit 1;
	fi
fi

rm -f *.res
rm -f *.rc
rm -f *.inc
rm -f *.ppu

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

if [ "" != "${jre}" ]; then
	ORIGNAME=`echo ${ORIGNAME} | sed "s/.exe$/.${jrename}.exe/"`
fi;

mv ${FULLINSTALLER} ../output/${ORIGNAME}

# Quick hack to prevent deleting of 2 important files in ${FILES}
mv Setup.exe Setup.exe.tmp
mv Shortcut.exe Shortcut.exe.tmp

rm -f ${DELETEFILES}
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
