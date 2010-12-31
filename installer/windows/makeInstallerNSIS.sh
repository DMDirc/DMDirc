#!/bin/sh
## Temporary Hack, this should be removed at some point when we stop
## using everything in /installer
## This script only understands the parameters needed by BuildAll and the
## installer-* ant targets.

if [ ! -e "../../modules/installer/windows/" ]; then
	echo "Unable to find NSIS Source"
	exit 1;
fi;


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
		--extra|-e)
			shift
			finalTag="-${1}"
	esac
	shift
done

INSTALLNAME=DMDirc-Setup
OLDDIR=`pwd`

mkdir -p ../../modules/installer/output
rm -Rfv ../../modules/installer/windows/files
mkdir -p ../../modules/installer/windows/files

cp "${jarfile}" "../../modules/installer/windows/files/DMDirc.jar"
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

makensis -DVERSION="${TAGGED}" -V2 updater.nsi
makensis -DVERSION="${TAGGED}" -V2 launcher.nsi
makensis -DVERSION="${TAGGED}" -V2 installer.nsi

cd "${OLDDIR}"
SRC="../../modules/installer/output/DMDirc-${TAGGED}-Setup.exe"

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
