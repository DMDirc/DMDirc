#!/bin/sh
# Update Copyright headers in all files.

THISYEAR=`date +%Y`
OLD="2006-"$((${THISYEAR} - 1))
NEW="2006-"${THISYEAR}

grep -iRI "${OLD}" ./* | grep -v /.git/ | grep -v UpdateCopyright.sh | grep -v update.sh | awk -F: '{print "echo \"Updating \\\""$1"\\\"\"\nsed -i \"s/'${OLD}'/'${NEW}'/g\" \""$1"\""}' > update.sh
sh update.sh
rm update.sh
