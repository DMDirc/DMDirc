#!/bin/sh
#
# This script generates a jar file for a release version of DMDirc
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

# Final Name of the file (without file extention)
FILENAME=DMDirc
# full name of the file to output to
RUNNAME="${PWD}/${FILENAME}.jar"

# Are we a git working copy, or SVN?
if [ -e ".svn" ]; then
	isSVN=1
else
	isSVN=0
fi;

# Go!
echo "-----------"
if [ -e "${RUNNAME}" ]; then
	echo "Removing existing file"
	rm -Rf ${RUNNAME}
fi

showHelp() {
	echo "This will generate a DMDirc jar file for any system with java on it."
	echo "The following command line arguments are known:"
	echo "---------------------"
	echo "-h, --help                Help information"
	echo "-r, --release <version>   Generate a file based on an svn tag (or branch with -b as well)"
	echo "-b, --branch              Release in -r is a branch "
	echo "-p, --plugins <plugins>   What plugins to add to the jar file"
	echo "-c, --compile             Recompile the .jar file (otherwise use the existing file from dist/)"
	echo "    --jar <file>          use <file> as DMDirc.jar (ie just add the plugins to it and rename)"
	echo "    --current             Use the current folder as the base for the build"
	echo "-e, --extra <tag>         Tag to add to final name to distinguish this build from a standard build"
	echo "-k, --keep                Keep the existing source tree when compiling"
	echo "                          (don't svn update beforehand)"
	echo "    --channel [channel]  Channel to pass to ant (if not passed, 'NONE', if passed without a value, 'STABLE')"
	echo "---------------------"
	exit 0;
}

# Check for some CLI params
compileJar="false"
updateSVN="true"
isRelease=""
finalTag=""
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
jrename="jre" # Filename for JRE without the .bin
TAGGED=""
CHANNEL="NONE"
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
		--current)
			location="../../"
			current="1"
			;;
		--compile|-c)
			compileJar="true"
			;;
		--release|-r)
			shift
			isRelease=${1}
			;;
		--extra|-e)
			shift
			finalTag=${1}
			RUNNAME="${PWD}/${FILENAME}-${1}.jar"
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
			if [ ${isSVN} -eq 1 ]; then
				shift
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
		--channel)
			PASSEDPARAM=`echo "${2}" | grep -v ^-`
			if [ "${PASSEDPARAM}" != "" ]; then
				shift
				CHANNEL=${PASSEDPARAM};
			else
				CHANNEL="STABLE";
			fi;
			;;
	esac
	shift
done
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
		fi;
	fi	
fi

if [ "" = "${jarfile}" ]; then
	jarfile=${jarPath}"/dist/DMDirc.jar"
	if [ ! -e ${jarPath}"/dist/DMDirc.jar" -o "${compileJar}" = "true" ]; then
		echo "Creating jar.."
		OLDPWD=${PWD}
		cd ${jarPath}
		if [ "${updateSVN}" = "true" ]; then
			if [ $isSVN -eq 1 ]; then
				svn update
			fi;
		fi
		ant -Dchannel=${CHANNEL} clean jar
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

echo "Copying jar (${jarfile} -> ${RUNNAME}).."
cp ${jarfile} ${RUNNAME}

# Remove plugins added by createAllPluginJar
zip -d ${RUNNAME} plugins plugins/*

if [ "${plugins}" != "" ]; then
	echo "Adding plugins to jar"
	ln -sf ${jarPath}"/plugins"
	pluginList=""
	for plugin in ${plugins}; do
		pluginList=${pluginList}" plugins/${plugin}"
	done
	jar -uvf "${RUNNAME}" ${pluginList}
	rm -Rf plugins;
fi;
doRename=0
if [ "${isRelease}" != "" ]; then
	doRename=1
elif [ $isSVN -eq 0 -a "${TAGGED}" != "" ]; then
	doRename=1	
fi;

if [ ${doRename} -eq 1 ]; then
	if [ $isSVN -eq 1 ]; then
		if [ "${BRANCH}" = "1" ]; then
			isRelease=branch-${isRelease}
		fi;
	else
		if [ "${TAGGED}" = "" ]; then
			isRelease=branch-${isRelease}
		else
			isRelease=${TAGGED}
		fi;
	fi
	if [ "" != "${finalTag}" ]; then
		finalTag="-${finalTag}"
	fi;
	finalname=DMDirc-${isRelease}${finalTag}.jar
else
	finalname=${RUNNAME##*/}
fi;

mv ${RUNNAME} ../output/${finalname}
echo "-----------"
echo "Done."
echo "-----------"

# and Done \o
exit 0;
