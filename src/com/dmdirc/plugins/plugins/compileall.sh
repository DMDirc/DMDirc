#!/bin/sh
# Quick script to recompile all plugins
rm ./*/*.class
cd ../../../../
javac -Xlint:all com/dmdirc/plugins/plugins/*/*.java
