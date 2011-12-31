#!/bin/sh
# Update Copyright headers in all files.

THISYEAR=`date +%Y`
THISYEAR=$((${THISYEAR} + 1))

find . -regextype posix-egrep -iregex '.*\.(java|sh|php|xml)$' -exec sed -i 's/\(Copyright.*\)2006-20[0-9][0-9]/\12006-'${THISYEAR}'/' {} \;

