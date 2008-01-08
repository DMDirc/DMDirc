#!/bin/sh
# Update Copyright headers in all files.

OLD="2006-2007"
NEW="2006-2008"

grep -iR "${OLD}" ./* | grep -v /.svn/ | grep -v "Binary file" | grep -v UpdateCopyright.sh | grep -v update.sh | awk -F: '{print "echo \"Updating \\\""$1"\\\"\"\nsed -i \"s/'${OLD}'/'${NEW}'/g\" \""$1"\""}' > update.sh
sh update.sh
rm update.sh
