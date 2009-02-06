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

if [ ! -e src/${foldername}/plugin.info -a ! -e src/${foldername}/plugin.config ]; then
	echo "no plugin.info or plugin.config found";
	exit 0;
fi

#echo "looking for classes"
TMPDIR=`mktemp -d`
#echo "Using temp dir: ${TMPDIR}"
cd $TMPDIR

mkdir META-INF
if [ -e ${srcdir}/src/${foldername}/plugin.info ]; then
	cp ${srcdir}/src/${foldername}/plugin.info META-INF/
fi;
if [ -e ${srcdir}/src/${foldername}/plugin.config ]; then
	cp ${srcdir}/src/${foldername}/plugin.config META-INF/
fi;

# Add the SVN version if there's no version specified and we know the SVN rev
if [ -e META-INF/plugin.info ]; then
	if ! grep "^version=" META-INF/plugin.info >/dev/null; then
		SVN=`which svn`	
		SVNREV=`$SVN info $srcdir/src/$foldername 2>&1 | grep "Last Changed Rev"`
		SVNREV=${SVNREV##*: }
		echo "" >> META-INF/plugin.info
	
		if [ -n "$SVNREV" ]; then
			echo "version=$SVNREV" >> META-INF/plugin.info;
		else
			echo "version=0" >> META-INF/plugin.info;
		fi
	
		if ! grep "^friendlyversion=" META-INF/plugin.info >/dev/null; then
			echo "friendlyversion=$SVNREV" >> META-INF/plugin.info
		fi
	fi
fi;

# Do the same for plugin.config
# This is rudimentary, it a version: section already exists (eg to specify
# friendlyversion) then it won't add the number= key.
if [ -e META-INF/plugin.config ]; then
	if ! grep "^version:" META-INF/plugin.config >/dev/null; then
		SVN=`which svn`	
		SVNREV=`$SVN info $srcdir/src/$foldername 2>&1 | grep "Last Changed Rev"`
		SVNREV=${SVNREV##*: }
		echo "" >> META-INF/plugin.config
		echo "" >> META-INF/plugin.config
	
		echo "version:" >> META-INF/plugin.config;
		if [ -n "$SVNREV" ]; then
			echo "  number=$SVNREV" >> META-INF/plugin.config;
		else
			echo "  number=0" >> META-INF/plugin.config;
		fi
		
		# Add to keysections list
		sed 's/keysections:/keysections:\n  version/g' META-INF/plugin.config > META-INF/plugin.config.temp
		rm -Rf META-INF/plugin.config
		mv META-INF/plugin.config.temp META-INF/plugin.config
	fi
fi;

foo=`echo $foldername | sed -e 's/\/[^\/]*$//g'`
mkdir -p $foo
cd ${foo}
ln -s ${srcdir}/build/classes/${foldername} .
cd $TMPDIR
mkdir -p ${srcdir}/plugins/
rm -Rf ${srcdir}/plugins/${2}.jar
jar -cvf ${srcdir}/src/${foldername}/${2}.jar META-INF >/dev/null
bit=""
while [ 1 -eq 1 ]; do
	bit=${bit}/*
	ls ${foo}${bit}/* >/dev/null 2>&1
	if [ ${?} -ne 0 ]; then
		break;
	else
		DIR=${PWD}
		for prepackage in `ls ${srcdir}/src/${foldername}${bit}/prePackage.sh 2>/dev/null`; do
			cd `dirname ${prepackage}`
			/bin/sh ${prepackage}
			cd ${DIR}
		done;
		jar -uvf ${srcdir}/src/${foldername}/${2}.jar `ls -1 ${foo}${bit}/*.class ${foo}${bit}/*.png ${foo}${bit}/*.exe ${foo}${bit}/*.dll 2>/dev/null` >/dev/null
	fi
done

mv ${srcdir}/src/${foldername}/${2}.jar ${srcdir}/plugins/

cd ${srcdir}
rm -Rf ${TMPDIR}
#echo "done";
