#!/bin/sh
## Temporary Hack, this should be removed at some point when we stop
## using everything in /installer
## This script only understands the parameters needed by BuildAll and the
## installer-* ant targets.

if [ ! -e "../../modules/installer/windows/" ]; then
	echo "Unable to find NSIS Source"
	exit 1;
fi;


signEXE="true"

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
		--tag|-t)
			TAGGED=`git describe --tags`
			TAGGED=${TAGGED%%-*}
			;;
		--unsigned|-u)
			signEXE="false"
			;;
		--extra|-e)
			shift
			finalTag="-${1}"
			;;
	esac
	shift
done

INSTALLNAME=DMDirc-Setup
OLDDIR=`pwd`

mkdir -p ../../modules/installer/output
rm -Rfv ../../modules/installer/windows/files
mkdir -p ../../modules/installer/windows/files

cp "${jarfile}" "../../modules/installer/windows/files/DMDirc.jar"
cp "../../src/com/dmdirc/licences/DMDirc - MIT" "../../modules/installer/windows/licence.txt"
cd ../../modules/installer/windows/files

if [ "" != "${plugins}" ]; then
	echo "Adding plugins to jar"
	ln -sf ${jarPath}"/plugins"
	pluginList=""
	for plugin in ${plugins}; do
		pluginList=${pluginList}" plugins/${plugin}"
	done
	jar -uvf "DMDirc.jar" ${pluginList}

	../../../../updateBundledPlugins.sh
	rm -Rf plugins;
fi

cp ../../../../src/com/dmdirc/res/icon.ico icon.ico

cd ..

for NSI in updater.nsi launcher.nsi installer.nsi; do
	LASTCOMMIT=`git rev-list --max-count=1 HEAD -- $NSI`
	NSISVERSION=`git describe --tags --always $LASTCOMMIT`
	makensis -DVERSION="${NSISVERSION}" -V2 $NSI;
done

rm "license.txt"

cd "${OLDDIR}"
SRC="../../modules/installer/output/DMDirc-Setup.exe"

doRename=0
if [ "${TAGGED}" != "" ]; then
	doRename=1
fi;

if [ ${doRename} -eq 1 ]; then
	if [ "${TAGGED}" = "" ]; then
		releaseTag=branch-${isRelease}
	else
		releaseTag=${TAGGED}
	fi;
	DEST="DMDirc-${releaseTag}-Setup${finalTag}.exe"
else
	DEST="${INSTALLNAME}${finalTag}.exe"
fi;

mv "${SRC}" "../output/${DEST}"

# Get signcode path
SIGNCODE=`which signcode`

if [ "" = "${SIGNCODE}" ]; then
	echo "Signcode not found. EXE's will not be digitally signed."
fi

# Sign stuff!
signexe() {
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

FULLINSTALLER="../output/${DEST}"

echo "Chmodding.."
chmod a+x ${FULLINSTALLER}
if [ "${signEXE}" = "true" ]; then
	echo "Signing.."
	signexe ${FULLINSTALLER}
else
	echo "Not Signing.."
fi;
