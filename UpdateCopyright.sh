#!/bin/sh
# Update Copyright headers in all files.

MONTH=`date +%m`
THISYEAR=`date +%Y`

if [ ${MONTH} -eq 12 ]; then
	THISYEAR=$((${THISYEAR} + 1))
elif [ ${MONTH} -ne 1 ]; then
	echo "This script only makes sense being run near the start of a year..."
	exit 1;
fi;

echo "Updating copyright to: ${THISYEAR}"

find . -regextype posix-egrep -iregex '.*/(.*\.(java|sh|php|xml|dpr|nsh|html|htm)|dmdirc.license|LICENCE|licenseheader.txt|DMDirc-Apple.c|DMDirc - MIT|copyright)$' -exec sed -i 's/\(Copyright.*\)2006-20[0-9][0-9]\(.*DMDirc.*\)/\12006-'${THISYEAR}'\2/' {} \;
