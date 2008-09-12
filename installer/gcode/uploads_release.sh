#!/bin/sh

# Leave blank here, loaded from config file.
USERNAME=""
PASSWORD=""

# Load stored password.
if [ -e "${HOME}/.GoogleCode" ]; then
. ${HOME}/.GoogleCode
fi;

PYTHON=`which python`
if [ "" = "${PYTHON}" ]; then
	echo 'Python not found. Aborting.'
	exit 1;
fi;

VERSION=""
while test -n "$1"; do
	LAST=${1}
	case "$1" in
		--version|-v)
			shift
			VERSION="${1}"
			;;
	esac
	shift
done

if [ "" = "${VERSION}" ]; then
	echo "Version not specified. Aborting"
	exit 1;
fi;

if [ "" = "${USERNAME}" ]; then
	echo "Username not found (Check: ${HOME}/.GoogleCode)"
#	exit 1;
fi;
if [ "" = "${PASSWORD}" ]; then
	echo "Password not found (Check: ${HOME}/.GoogleCode)"
#	exit 1;
fi;

uploadFile() {
	TYPE=${1}
	LABELS=${2}
	FILE=${3}
	
	if [ -e "../output/${FILE}" ]; then
		echo ""
		echo "Uploading ${TYPE}"
		${PYTHON} ${PWD}/googlecode_upload.py --summary "DMDirc ${VERSION} ${TYPE}" --user ${USERNAME} --pass ${PASSWORD} --labels="Featured,${LABELS}" ${FILE}
	else
		echo "Not Uploading ${TYPE} (File '${FILE}' Doesn't exist)"
	fi;
}

uploadFile "Jar" "Type-Executable,OpSys-All" "../output/DMDirc-${VERSION}.jar"
uploadFile "OS X Image" "Type-Archive,OpSys-OSX" "../output/DMDirc-${VERSION}.dmg"
uploadFile "Windows Installer" "Type-Installer,OpSys-Windows" "../output/DMDirc-${VERSION}-Setup.exe"
uploadFile "Unix Installer" "Type-Installer,OpSys-Linux,OpSys-BSD,OpSys-Solaris" "../output/DMDirc-${VERSION}-Setup.run"
exit 0;
