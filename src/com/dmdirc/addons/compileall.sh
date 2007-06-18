#!/bin/sh
# Quick script to recompile all plugins
rm ./*/*.class
rm ./*/*/*.class
cd ../../../
javac -Xlint:all com/dmdirc/addons/*/*.java com/dmdirc/addons/*/*/*.java
