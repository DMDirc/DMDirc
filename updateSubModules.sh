#!/bin/bash
GIT=`which git`

# Init any new submodules
${GIT} submodule init

DIR=${PWD}

# Check every submodule.
${GIT} submodule status | while read -r LINE; do
	MISSING=`echo ${LINE} | grep ^-`
	CHANGED=`echo ${LINE} | grep ^+`
	MODULE=`echo ${LINE} | awk '{print $2}'`
	
	# If the module hasn't been checked out yet, then checkout.
	if [ "${MISSING}" != "" ]; then
		${GIT} submodule update ${MODULE}
		if [ ${?} -ne 0 ]; then
			echo "Error: Unable to update submodule ${MODULE}, aborting."
			exit 1;
		fi;
	# Else, rebase current changes onto the new upstream version.
	#   - If nothing has changed, this will just be a fast forward like normal
	#   - If there are uncommited changes, this will fail and leave the submodule
	#     as it is at the moment.
	#   - If there are commited revisions on top of the submodule, then they will
	#     be rebased onto the revision used upstream,
	elif [ "${CHANGED}" != "" ]; then
		# Get the revision used upstream
		OLDREV=`git diff ${MODULE} | grep -- "-Subproject" | awk '{print $3}'`
		if [ "${OLDREV}" != "" ]; then
			cd ${MODULE}
			${GIT} fetch origin 
			${GIT} rebase ${OLDREV}
			if [ ${?} -ne 0 ]; then
				echo "Error: Rebase failed on ${MODULE}, continuing with current HEAD."
				${GIT} rebase --abort
			fi;
		fi;
	fi;
	
	# Go back to main project directory.
	cd ${DIR}
done;
