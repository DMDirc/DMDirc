#!/bin/bash
# Provides functions that are used in multiple scripts in the build process

function safe_mktemp {
	if [ -x "`which mktemp`" ] ; then
		TMPDIR=`mktemp -d`
	else
		if [ -d "$TMP" ] ; then
			# We're running under Windows then use the global TMP location
			TMPDIR="$TMP/$1-`date +%s`-$$-$RANDOM"
		else
			TMPDIR="`pwd`/build/temp/$1-$$-$RANDOM"
		fi
		# this is fragile
		if [ -d "$TMPDIR" ] ; then
			echo "no suitable temp folder found"
			exit 2
		fi
		mkdir -p "$TMPDIR"  
		if [ ! -d "$TMPDIR" ] ; then
			echo "no suitable temp folder found"
			exit 2
		fi
	fi
	echo $TMPDIR
}