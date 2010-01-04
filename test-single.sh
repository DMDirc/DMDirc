#!/bin/sh
if [ "" = "${1}" ]; then
	echo "Usage: ${0} <test file name>"
	echo "Example: ${0} **/plugins/PluginInfoTest.java"
	exit 1
fi;
ant -Djavac.includes=${1} -Dtest.includes=${1} test-single
