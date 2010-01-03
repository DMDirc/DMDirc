#!/bin/bash
# This script will create a plugin jar file for a given plugin.

if [ "${1}" = "" -o "${2}" = "" ]; then
	echo "Usage Example: ${0} com.dmdirc.addons.windowstatus WindowStatusPlugin"
	echo "The above would create WindowStatusPlugin.jar in the plugins/ folder of the current dir"
	exit;
fi

destdir=${PWD}
srcdir=${PWD}/modules/plugins/
pluginname=${1}
foldername=${pluginname//.//}

newer=`find src/${foldername} -type f -newer ${srcdir}/plugins/${2}.jar 2>&1 | wc -l`

if [ $newer -eq 0 ]; then
	echo "${2}.jar appears to be up-to-date";
	exit 0;
fi

echo "Creating ${2}.jar for ${pluginname} (${foldername})"

if [ ! -e modules/plugins/src/${foldername}/plugin.config ]; then
	echo "no plugin.config found";
	exit 0;
fi

#echo "looking for classes"
TMPDIR=`mktemp -d`
#echo "Using temp dir: ${TMPDIR}"
cd $TMPDIR

mkdir META-INF
if [ -e "${srcdir}/src/${foldername}/plugin.config" ]; then
	cp "${srcdir}/src/${foldername}/plugin.config" META-INF/
fi;

# Do the same for plugin.config
# This is rudimentary, it a version: section already exists (eg to specify
# friendlyversion) then it won't add the number= key.
if [ -e META-INF/plugin.config ]; then
	VERSIONLINE=`grep -n "version:$" META-INF/plugin.config | cut -f 1 -d ':'`

	if [ -z "$VERSIONLINE" ]; then
		sed 's/keysections:/keysections:\n  version/g' META-INF/plugin.config > META-INF/plugin.config.temp
		rm -Rf META-INF/plugin.config
		mv META-INF/plugin.config.temp META-INF/plugin.config		
	fi;

	GIT=`which git`
	REV=$(${GIT} --git-dir "${srcdir}/.git" describe --tags `${GIT} --git-dir "${srcdir}/.git" rev-list --max-count=1 HEAD -- "src/${foldername}"`);

	echo "" >> META-INF/plugin.config
	echo "" >> META-INF/plugin.config
	echo "version:" >> META-INF/plugin.config;
	echo "  number=$REV" >> META-INF/plugin.config;
fi;

foo=`echo $foldername | sed -e 's/\/[^\/]*$//g'`
mkdir -p "$foo"
cd "${foo}"
ln -s "${destdir}/build/classes/${foldername}" .
cd "$TMPDIR"
mkdir -p "${destdir}/plugins/"

rm -Rf "${destdir}/plugins/${2}.jar"
jar -cvf "${srcdir}/src/${foldername}/${2}.jar" META-INF >/dev/null
bit=""
while [ 1 -eq 1 ]; do
	bit=${bit}/*
	ls ${foo}${bit}/* >/dev/null 2>&1
	if [ ${?} -ne 0 ]; then
		break;
	else
		DIR="${PWD}"
		for prepackage in `ls "${srcdir}/src/${foldername}${bit}/prePackage.sh" 2>/dev/null`; do
			cd `dirname "${prepackage}"`
			/bin/sh "${prepackage}"
			cd ${DIR}
		done;
		jar -uvf "${srcdir}/src/${foldername}/${2}.jar" `ls -1 ${foo}${bit}/*.class ${foo}${bit}/*.png ${foo}${bit}/*.exe ${foo}${bit}/*.dll 2>/dev/null` >/dev/null
	fi
done

mv "${srcdir}/src/${foldername}/${2}.jar" "${destdir}/plugins/"

cd "${srcdir}"
rm -Rf ${TMPDIR}
#echo "done";
