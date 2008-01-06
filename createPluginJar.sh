#!/bin/bash
# This script will create a plugin jar file for a given plugin.

if [ "${1}" = "" -o "${2}" = "" ]; then
	echo "Usage Example: ${0} com.dmdirc.addons.windowstatus WindowStatusPlugin"
	echo "The above would create WindowStatusPlugin.jar in the plugins/ folder of the current dir"
	exit;
fi

pluginname=${1}
foldername=${pluginname//.//}

echo "Creating ${2}.jar for ${pluginname} (${foldername})"

if [ ! -e src/${foldername}/plugin.info ]; then
	echo "no plugin.info found";
	exit 0;
fi

#echo "looking for classes"
srcdir=${PWD}
TMPDIR=`mktemp -d`
#echo "Using temp dir: ${TMPDIR}"
cd $TMPDIR

mkdir META-INF
cp ${srcdir}/src/${foldername}/plugin.info META-INF/
foo=`echo $foldername | sed -e 's/\/[^\/]*$//g'`
mkdir -p $foo
cd ${foo}
ln -s ${srcdir}/build/classes/${foldername} .
cd $TMPDIR
mkdir -p ${srcdir}/plugins/
rm -Rf ${srcdir}/plugins/${2}.jar
jar -cvf ${srcdir}/src/${foldername}/${2}.jar META-INF/plugin.info >/dev/null
bit=""
while [ 1 -eq 1 ]; do
	bit=${bit}/*
	ls ${foo}${bit}/* >/dev/null 2>&1
	if [ ${?} -ne 0 ]; then
		break;
	else
		for thisfile in `ls -1 ${foo}${bit}/*.class`; do
			jar -uvf ${srcdir}/src/${foldername}/${2}.jar ${thisfile} >/dev/null
		done
	fi
done

mv ${srcdir}/src/${foldername}/${2}.jar ${srcdir}/plugins/

#echo "done";
