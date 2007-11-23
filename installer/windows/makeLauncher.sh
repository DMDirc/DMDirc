#!/bin/sh
#
# This script generates a .exe file that will just launch a pre-defined exe
# This exe exists solely to rebrand the installer as DMDirc not 7zip, and to
# give the correct icon.
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

# Icon Res file
echo "icon.ico ICON icon.ico" > icon.rc

# Other resources
echo "extractor RCDATA extractor.exe" > files.rc

COMPILER_IS_BROKEN="0";

# Version Numbers
if [ "" = "${1}" ]; then
	MAJORVER="0"
	MINORVER="0"
	RELEASE="0"
	TEXTVER="Trunk"
	PRIVATE="1"
	USER=`whoami`
	HOST=`hostname`
	DATE=`date`
else
	MAJORVER=${1%%.*}
	SUBVER=${1#*.}
	DOT=`expr index "${SUBVER}" .`
	if [ "${DOT}" = "0" ]; then
		MINORVER=${SUBVER}
		RELEASE="0"
	else
		MINORVER=${SUBVER%%.*}
		RELEASE=${SUBVER##*.}
	fi
	TEXTVER=$1
	PRIVATE="0"
fi;

# Information for the below section:
#
# http://support.microsoft.com/kb/139491
# http://msdn2.microsoft.com/en-us/library/aa381049.aspx
# http://courses.cs.vt.edu/~cs3304/FreePascal/doc/prog/node14.html#SECTION001440000000000000000
# http://tortoisesvn.tigris.org/svn/tortoisesvn/trunk/src/Resources/TortoiseShell.rc2

echo "1 VERSIONINFO" > version.rc
echo "FILEVERSION 1, 0, 0, 0" >> version.rc
echo "PRODUCTVERSION ${MAJORVER}, ${MINORVER}, ${RELEASE}, 0" >> version.rc
if [ "${PRIVATE}" = "1" ]; then
	if [ "${COMPILER_IS_BROKEN}" = "0" ]; then
		echo "FILEFLAGSMASK 0x000A" >> version.rc
		echo "FILEFLAGS 0x3f" >> version.rc
	else
		echo "FILEFLAGS 0x000A" >> version.rc
	fi;
else
	echo "FILEFLAGSMASK 0" >> version.rc
fi;
echo "FILEOS 0x40004" >> version.rc
echo "FILETYPE 1" >> version.rc
echo "BEGIN" >> version.rc
echo "	BLOCK \"StringFileInfo\"" >> version.rc
echo "	BEGIN" >> version.rc
echo "		BLOCK \"040004E4\"" >> version.rc
echo "		BEGIN" >> version.rc
echo "			VALUE \"Comments\", \"http://www.dmdirc.com/\"" >> version.rc
echo "			VALUE \"CompanyName\", \"DMDirc\"" >> version.rc
echo "			VALUE \"FileDescription\", \"Installer for DMDirc ${TEXTVER}\"" >> version.rc
echo "			VALUE \"FileVersion\", \"1.0\"" >> version.rc
echo "			VALUE \"InternalName\", \"DMDirc.jar\"" >> version.rc
echo "			VALUE \"LegalCopyright\", \"Copyright (c) 2006-2007 Chris Smith, Shane Mc Cormack, Gregory Holmes\"" >> version.rc
echo "			VALUE \"OriginalFilename\", \"$2\"" >> version.rc
echo "			VALUE \"ProductName\", \"DMDirc\"" >> version.rc
echo "			VALUE \"ProductVersion\", \"${TEXTVER}\"" >> version.rc
if [ "${PRIVATE}" = "1" ]; then
	echo "			VALUE \"PrivateBuild\", \"Build by ${USER}@${HOST} on ${DATE}\"" >> version.rc
fi;
echo "		END" >> version.rc
echo "	END" >> version.rc
echo "	BLOCK \"VarFileInfo\"" >> version.rc
echo "	BEGIN" >> version.rc
echo "		VALUE \"Translation\", 0x400, 1252" >> version.rc
echo "	END" >> version.rc
echo "END" >> version.rc

echo "1 24 \"UAC.manifest\"" > UAC.rc

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

# Build res files
#windres -F pe-i386 -i version.rc -o version.res
#windres -F pe-i386 -i files.rc -o files.res
#windres -F pe-i386 -i icon.rc -o icon.res

cat UAC.rc > all.rc
cat version.rc >> all.rc
cat files.rc >> all.rc
cat icon.rc >> all.rc
windres -F pe-i386 -i all.rc -o all.res

FPC=`which fpc`
${FPC} -Sd -Twin32 ${3}Launcher.dpr
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
