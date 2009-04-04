#!/bin/bash
# This attempts to create a clean build of dmdirc, then if the build was a success
# it will create plugin jars for all the plugins in the addons directory.

if [ ${?} = "0" ]; then
	for dir in `ls -1 src/com/dmdirc/addons`; do
		if [ -e "src/com/dmdirc/addons/${dir}/.ignore" ]; then
			echo "------"
			echo "Not building: ${dir}"
			echo "------"
		else
			./createPluginJar.sh com.dmdirc.addons.${dir} ${dir}
		fi
	done
fi

if [ -d ${srcdir}/build/classes -a ! -e ${srcdir}/build/classes/plugins ]; then
	ln -s ${srcdir}/plugins ${srcdir}/build/classes/plugins;
fi
