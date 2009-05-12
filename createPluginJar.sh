#!/bin/bash
# This script will create a plugin jar file for a given plugin.

if [ "${1}" = "" -o "${2}" = "" ]; then
	echo "Usage Example: ${0} com.dmdirc.addons.windowstatus WindowStatusPlugin"
	echo "The above would create WindowStatusPlugin.jar in the plugins/ folder of the current dir"
	exit;
fi

srcdir=${PWD}
pluginname=${1}
foldername=${pluginname//.//}

newer=`find src/${foldername} -type f -newer ${srcdir}/plugins/${2}.jar 2>&1 | wc -l`

if [ $newer -eq 0 ]; then
	echo "${2}.jar appears to be up-to-date";
	exit 0;
fi

echo "Creating ${2}.jar for ${pluginname} (${foldername})"

if [ ! -e src/${foldername}/plugin.config ]; then
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

	if [ -d "${srcdir}/.git" ]; then
		GIT=`which git`
		REV=$(${GIT} --git-dir "${srcdir}/.git" describe --tags `${GIT} --git-dir "${srcdir}/.git" rev-list --max-count=1 HEAD -- "src/${foldername}"`);
	else
		cd $srcdir;
		SVN=`which svn`	
		SVNREV=`$SVN info $srcdir/src/$foldername 2>&1 | grep "Last Changed Rev"`
		SVNREV=${SVNREV##*: }

		if [ -n "$SVNREV" ]; then
			REV=`$SVN log -r $SVNREV | grep ^Git-version: | cut -f 2 -d ' '`
		else
			REV=0;
		fi;
		cd $TMPDIR;
	fi;

	echo "" >> META-INF/plugin.config
	echo "" >> META-INF/plugin.config
	echo "version:" >> META-INF/plugin.config;
	echo "  number=$REV" >> META-INF/plugin.config;
fi;

foo=`echo $foldername | sed -e 's/\/[^\/]*$//g'`
mkdir -p "$foo"
cd "${foo}"
ln -s "${srcdir}/build/classes/${foldername}" .
cd "$TMPDIR"
mkdir -p "${srcdir}/plugins/"

rm -Rf "${srcdir}/plugins/${2}.jar"
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

mv "${srcdir}/src/${foldername}/${2}.jar" "${srcdir}/plugins/"

cd "${srcdir}"
rm -Rf ${TMPDIR}
#echo "done";
