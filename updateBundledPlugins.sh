#!/bin/bash
# updateBundledPlugins.sh <jar>
# 
# Reads the version of each bundled plugin in the specified jar, and writes
# the names and versions to the jar's version.config file.

jar=$1
dir=`mktemp -d`
config="$dir/com/dmdirc/version.config"

# Read the jar's version.config out
unzip -qq $jar com/dmdirc/version.config -d $dir;

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
for plugin in `unzip -qql $jar plugins/* | cut -f 2 -d '/'`; do
 pluginName=${plugin%.jar}

 # Extract it to our temporary dir (can't extract zip files from stdin)
 unzip -qqj $jar plugins/$plugin -d $dir;

 # Read the plugin.config file and parse out the version number
 version=`unzip -c $dir/$plugin META-INF/plugin.config | grep -C 1 version: | grep number= | cut -f 2 -d '='`;

 # And add the version to our version.config
 echo "    $pluginName=$version" >> $config;
done;

echo "# --- End updateBundledPlugins.sh --- " >> $config;

# Update the version in the jar
jar uf $jar -C $dir com/dmdirc/version.config;

# Remove the temporary directory
rm -rf $dir;
