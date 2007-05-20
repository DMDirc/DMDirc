#!/bin/sh
# Quick script to recompile all plugins
rm ./*/*.class
cd ../../../../../../
javac -Xlint:all uk/org/ownage/dmdirc/plugins/plugins/*/*.java
