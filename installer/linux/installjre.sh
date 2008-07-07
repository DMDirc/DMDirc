#!/bin/sh
#
# Install the JRE.
#

# Find out what params we should pass to things.
# Solaris has a nice and ancient version of grep in /usr/bin
grep -na "" /dev/null >/dev/null 2>&1
if [ $? -eq 2 ]; then
	GREPOPTS="-n"
else
	GREPOPTS="-na"
fi;
# Solaris also has a crappy version of tail!
tail -n +1 /dev/null >/dev/null 2>&1
if [ $? -eq 2 ]; then
	TAILOPTS="+"
else
	TAILOPTS="-n +"
fi;
# Check the which command exists, and if so make sure it behaves how we want
# it to...
WHICH=`which which 2>/dev/null`
if [ "" = "${WHICH}" ]; then
	echo "which command not found. Aborting.";
	exit 0;
else
	# Solaris sucks
	BADWHICH=`which /`
	if [ "${BADWHICH}" != "" ]; then
		echo "Replacing bad which command.";
		# "Which" on solaris gives non-empty results for commands that don't exist
		which() {
			OUT=`${WHICH} ${1}`
			if [ $? -eq 0 ]; then
				echo ${OUT}
			else
				echo ""
			fi;
		}
	fi;
fi

PIDOF=`which pidof`
if [ "${PIDOF}" = "" ]; then
	# For some reason some distros hide pidof...
	if [ -e /sbin/pidof ]; then
		PIDOF=/sbin/pidof
	elif [ -e /usr/sbin/pidof ]; then
		PIDOF=/usr/sbin/pidof
	fi;
fi;

## Helper Functions
if [ "${PIDOF}" != "" ]; then
	ISKDE=`${PIDOF} -x -s kdeinit`
	ISGNOME=`${PIDOF} -x -s gnome-panel`
else
	ISKDE=`ps -Af | grep kdeinit | grep -v grep`
	ISGNOME=`ps -Af | grep gnome-panel | grep -v grep`
fi;
KDIALOG=`which kdialog`
ZENITY=`which zenity`

errordialog() {
	# Send message to console.
	echo ""
	echo "-----------------------------------------------------------------------"
	echo "Error: ${1}"
	echo "-----------------------------------------------------------------------"
	echo "${2}"
	echo "-----------------------------------------------------------------------"

	# Now try to use the GUI Dialogs.
	if [ "" != "${ISKDE}" -a "" != "${KDIALOG}" -a "" != "${DISPLAY}" ]; then
		echo "Dialog on Display: ${DISPLAY}"
		${KDIALOG} --title "DMDirc: ${1}" --error "${2}"
	elif [ "" != "${ISGNOME}" -a "" != "${ZENITY}" -a "" != "${DISPLAY}" ]; then
		echo "Dialog on Display: ${DISPLAY}"
		${ZENITY} --error --title "DMDirc: ${1}" --text "${2}"
	fi
}

messagedialog() {
	# Send message to console.
	echo ""
	echo "-----------------------------------------------------------------------"
	echo "Info: ${1}"
	echo "-----------------------------------------------------------------------"
	echo "${2}"
	echo "-----------------------------------------------------------------------"

	# Now try to use the GUI Dialogs.
	if [ "" != "${ISKDE}" -a "" != "${KDIALOG}" -a "" != "${DISPLAY}" ]; then
		echo "Dialog on Display: ${DISPLAY}"
		${KDIALOG} --title "DMDirc: ${1}" --msgbox "${2}"
	elif [ "" != "${ISGNOME}" -a "" != "${ZENITY}" -a "" != "${DISPLAY}" ]; then
		echo "Dialog on Display: ${DISPLAY}"
		${ZENITY} --info --title "DMDirc: ${1}" --text "${2}"
	fi
}

questiondialog() {
	# Send question to console.
	echo ""
	echo "-----------------------------------------------------------------------"
	echo "Question: ${1}"
	echo "-----------------------------------------------------------------------"
	echo "${2}"
	echo "-----------------------------------------------------------------------"

	# Now try to use the GUI Dialogs.
	if [ "" != "${ISKDE}" -a "" != "${KDIALOG}" -a "" != "${DISPLAY}" ]; then
		echo "Dialog on Display: ${DISPLAY}"
		${KDIALOG} --title "DMDirc: ${1}" --yesno "${2}"
	elif [ "" != "${ISGNOME}" -a "" != "${ZENITY}" -a "" != "${DISPLAY}" ]; then
		echo "Dialog on Display: ${DISPLAY}"
		${ZENITY} --question --title "DMDirc: ${1}" --text "${2}"
	else
		echo "Unable to ask question, assuming no."
		return 1;
	fi
}

showLicense() {
	# Get License Text
	FILE=`mktemp -p ${PWD} license.XXXXXXXXXXXXXX`
	
	# Location of license start
	STARTLINE=`grep ${GREPOPTS} "^more <<\"EOF\"$" jre.bin`
	STARTLINE=$((${STARTLINE%%:*} + 1))
	# Location of license end
	ENDLINE=`grep ${GREPOPTS} "Do you agree to the above license terms?" jre.bin`
	ENDLINE=$((${ENDLINE%%:*} - 2))
	
	head -n ${ENDLINE} jre.bin | tail ${TAILOPTS}${STARTLINE} > ${FILE}
	
	# Send text to console.
	echo ""
	echo "-----------------------------------------------------------------------"
	echo "Java License"
	echo "-----------------------------------------------------------------------"
	cat ${FILE}
	echo "-----------------------------------------------------------------------"

	# Now try to use the GUI Dialogs.
	if [ "" != "${ISKDE}" -a "" != "${KDIALOG}" -a "" != "${DISPLAY}" ]; then
		echo "Dialog on Display: ${DISPLAY}"
		${KDIALOG} --title "DMDirc: Java License" --textbox ${FILE} 600 400
	elif [ "" != "${ISGNOME}" -a "" != "${ZENITY}" -a "" != "${DISPLAY}" ]; then
		echo "Dialog on Display: ${DISPLAY}"
		${ZENITY} --text-info --title "DMDirc: Java License" --filename=${FILE} --width=600 --height=400
	fi
	
	# Remove temp file
	rm -Rf ${FILE}
}


if [ "" != "${1}" ]; then
	questiondialog "Java Install" "${1}"
	result=$?
	if [ $result -ne 0 ]; then
		exit 1;
	fi;
fi;

messagedialog "Java Install" "Before java can be installed, please review the following license."
showLicense
questiondialog "Java Install" "Do you agree to the Java License?"
if [ $? -eq 0 ]; then
	# Look to see where the JRE wants to install to
	JREJAVAHOME=`grep ${GREPOPTS} "^javahome=" jre.bin`
 	JREJAVAHOME=${JREJAVAHOME##*=}
	
	echo "JREJAVAHOME: ${JREJAVAHOME}"
	
	if [ "${UID}" = "" ]; then
		UID=`id -u`;
	fi
	if [ "0" = "${UID}" ]; then
		installdir=/usr/lib/jvm/${JREJAVAHOME}
	else
		installdir=${HOME}/${JREJAVAHOME}
	fi;
	
	echo "installdir: ${installdir}"
	
	if [ ! -e ${installdir} ]; then
		# Hack jre.bin to allow us to install without asking for a license, or failing
		# the checksum.
		
		# Location of license start
		STARTLINE=`grep ${GREPOPTS} "^more <<\"EOF\"$" jre.bin`
		STARTLINE=${STARTLINE%%:*}
		# Location of license end
		ENDLINE=`grep ${GREPOPTS} "If you don't agree to the license you can't install this software" jre.bin`
		ENDLINE=$((${ENDLINE%%:*} + 3))
		# Location of checksum start
		CSSTARTLINE=`grep ${GREPOPTS} "^if \[ -x /usr/bin/sum \]; then$" jre.bin`
		CSSTARTLINE=${CSSTARTLINE%%:*}
		# Location of checksum end
		CSENDLINE=`grep ${GREPOPTS} "Can't find /usr/bin/sum to do checksum" jre.bin`
		CSENDLINE=$((${CSENDLINE%%:*} + 2))
		# Location of script end
		SCENDLINE=`grep ${GREPOPTS} "^echo \"Done.\"$" jre.bin`
		SCENDLINE=$((${SCENDLINE%%:*} + 2 - (${ENDLINE} - ${STARTLINE}) - (${CSENDLINE} - ${CSSTARTLINE})))
		# Remove the license and checksum stuff!
		head -n $((${STARTLINE} -1)) jre.bin > jre.bin.tmp
		tail ${TAILOPTS}$((${ENDLINE})) jre.bin | head -n $((${CSSTARTLINE} -1 - ${ENDLINE})) >> jre.bin.tmp
		echo "tail \${tail_args} +${SCENDLINE} \"\$0\" > \$outname" >> jre.bin.tmp
		tail ${TAILOPTS}$((${CSENDLINE})) jre.bin >> jre.bin.tmp
		
		messagedialog "Java Install" "Java install will begin when you press OK.\nThis may take some time, so please wait.\n\nYou will be informed when the installation is completed."
		yes | sh jre.bin.tmp
		rm -Rf jre.bin.tmp
		mv ${JREJAVAHOME} ${installdir}
		
		if [ "0" = "${UID}" ]; then
			mkdir -p /usr/lib/jvm/
			
			# Add to global path.
			if [ -e "/usr/bin/java" ]; then
				rm -Rf /usr/bin/java
			fi;
			ln -s /usr/lib/jvm/${JREJAVAHOME}/bin/java /usr/bin/java
		else
			# Add to path.
			if [ -e ${HOME}/.profile ]; then
				echo "" >> ${HOME}/.profile
				echo "# set PATH so it includes user's private java if it exists" >> ${HOME}/.profile
				echo "if [ -d ~/${JREJAVAHOME}/bin ] ; then" >> ${HOME}/.profile
				echo "	PATH=~/${JREJAVAHOME}/bin:\"\${PATH}\"" >> ${HOME}/.profile
				echo "fi" >> ${HOME}/.profile
			fi
			if [ -e ${HOME}/.cshrc ]; then
				echo "" >> ${HOME}/.cshrc
				echo "# set PATH so it includes user's private java if it exists" >> ${HOME}/.cshrc
				echo "if( -d ~/${JREJAVAHOME}/bin ) then" >> ${HOME}/.cshrc
				echo "	set path = (~/${JREJAVAHOME}/bin \$path)" >> ${HOME}/.cshrc
				echo "endfi" >> ${HOME}/.cshrc
			fi
			
			echo "Adding JREPath: ${PWD}/jrepath"
			# This allows the main installer to continue with the new java version.
			echo "export PATH=~/${JREJAVAHOME}/bin:\"\${PATH}\"" > ${PWD}/jrepath
			
			# This allows dmdirc launcher to find the jre if the path is not set.
			ln -sf ${installdir} ${HOME}/jre
		fi;
		
		messagedialog "Java Install" "Java installation complete"
		exit 0;
	else
		messagedialog "Java Install" "An existing install was found at ${installdir}, but this directory is not in the current path, DMDirc setup will try to use this version of java for the install."
		echo "Adding JREPath: ${PWD}/jrepath"
		# This allows the main installer to continue with the new java version.
		echo "export PATH=~/${JREJAVAHOME}/bin:\"\${PATH}\"" > ${PWD}/jrepath
		exit 0;
	fi;
else
	errordialog "Java Install" "You must agree to the license before java can be installed"
fi;
exit 1;