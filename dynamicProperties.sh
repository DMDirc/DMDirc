#!/bin/bash

SOURCE="${1}"
TARGET="${2}"

if [ "" = "${TARGET}" -o ! -e "${SOURCE}" ]; then
	echo "Usage: ${0} dynamic.properties private.properties";
else
	# Make sure target exists.
	if [ ! -e "${TARGET}" ]; then
		mkdir -p `dirname "${TARGET}"`
		touch "${TARGET}"
	fi;
	
	# Remove existing private.properties line
	sed '/^private\.classpath[\s]*=.*$/d' -i "${TARGET}"

	# Add new one.
	cat "${SOURCE}" | grep "^private\.classpath" >> "${TARGET}"
fi;
