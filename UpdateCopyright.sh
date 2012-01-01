#!/bin/sh
# Update Copyright headers in all files.

THISYEAR=`date +%Y`
THISYEAR=$((${THISYEAR} + 1))

find . -regextype posix-egrep -iregex '.*/(.*\.(java|sh|php|xml|dpr|nsh|html|htm)|dmdirc.license|DMDirc-Apple.c|DMDirc - MIT|copyright)$' -exec sed -i 's/\(Copyright.*\)2006-20[0-9][0-9]\(.*DMDirc.*\)/\12006-'${THISYEAR}'\2/' {} \;