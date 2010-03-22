#!/bin/bash
# updateBundledPlugins.sh <jar>
# 
# Reads the version of each bundled plugin in the specified jar, and writes
# the names and versions to the jar's version.config file.

jar=$1
if [ -x "`which mktemp`" ] ; then
	dir=`mktemp -d`
elif [ -d "$TMP" ] ; then
	# We're running under Windows and have no mktemp
	dir="$TMP/${jar}-`date +%s`-$$-$RANDOM"
	# this is fragile
	if [ -d "$dir" ] ; then
		echo "Can't build on Windows, no suitable temp folder found"
		exit 2
	fi
	mkdir -p "$dir"  
	if [ ! -d "$dir" ] ; then
		echo "Can't build on Windows, no suitable temp folder found"
		exit 2
	fi
else
	echo "No suitable mktemp"
	exit 2
fi
config="$dir/com/dmdirc/version.config"
jar="`pwd`/$jar"

cd $dir
# Read the jar's version.config out
jar -xf $jar

# Check for previous data
startOffset=`grep -n "Begin updateBundledPlugins.sh" $config | head -n 1 | cut -f 1 -d ':'`
endOffset=`grep -n "End updateBundledPlugins.sh" $config | tail -n 1 | cut -f 1 -d ':'`

if [ -n "$startOffset" -a -n "$endOffset" ]; then
 # Previous data found, let's get rid of it

 head -n $(($startOffset-2)) $config >> $config.tmp;
 tail -n +$(($endOffset+1)) $config >> $config.tmp;

 mv $config.tmp $config;
fi;

# Add our new sections
echo "" >> $config;
echo "# --- Begin updateBundledPlugins.sh (`date`) --- " >> $config
echo "keysections:" >> $config;
echo "    bundledplugins_versions" >> $config;
echo "" >> $config;
echo "bundledplugins_versions:" >> $config;

# For each plugin in the jar...
for plugin in `ls plugins/* | cut -f 2 -d '/'`; do
 pluginName=${plugin%.jar}

 # Extract it to our temporary dir (can't extract zip files from stdin)
 jar -xf plugins/$plugin;
 # Read the plugin.config file and parse out the version number
 version=`cat META-INF/plugin.config | grep -1 version: | grep number= | cut -f 2 -d '='`;

 # And add the version to our version.config
 echo "    $pluginName=$version" >> $config;
done;

echo "# --- End updateBundledPlugins.sh --- " >> $config;

# Update the version in the jar
jar uf $jar -C $dir com/dmdirc/version.config;

# Remove the temporary directory
cd ..
rm -rf $dir;
