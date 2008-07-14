#!/bin/sh
# Check if there have been any changes to these files from the svn version
CHANGES=`svn status | grep -v ?`

compile() {
	# Is there any changes to this file?
	THISCHANGES=`echo ${CHANGES} | grep ${1}`
	
	# Compile if the output doesn't exist, or the local version differs from SVN
	if [ ! -e ${2} -o "" != "${THISCHANGES}" ]; then
		echo "	Compiling: ${1} -> ${2}"
		fpc -Sd -Twin32 ${1} >/dev/null 2>&1
		if [ $? -ne 0 ]; then
			echo "		Failed"
		fi;
		rm -Rf *.o
	fi;
}

compile "itunes.dpr" "itunes.dll"
compile "winamp.dpr" "winamp.dll"
compile "GetMediaInfo.dpr" "GetMediaInfo.exe"
