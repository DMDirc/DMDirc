#!/bin/bash
# This attempts to create a clean build of dmdirc, then if the build was a success
# it will create plugin jars for all the plugins in the addons directory.

if [ ${?} = "0" ]; then
	for dir in `ls -1 src/com/dmdirc/addons`; do
		./createPluginJar.sh com.dmdirc.addons.${dir} ${dir}
	done
fi
