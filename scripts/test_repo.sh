#!/bin/bash

REPO_FULL_NAME=$2
REPO_DIR=/tmp/$(cut -d'-' -f2 <<< $1)
RESULTS_DIR=${REPO_FULL_NAME}_test_results


# create a directory to store this repo's data
if [ ! -d ${RESULTS_DIR} ]
	then
	mkdir ${RESULTS_DIR}
fi

# Find all merge conflicts for the given repo
if [ ! -f {$RESULTS_DIR}/merge_conflicts.txt ]
	then
	# copy if this is one of the repos already checked (in previous versions of this script)
	if [ -f /tmp/${REPO_FULL_NAME}_conflicts.txt ]
		then
		cp /tmp/${REPO_FULL_NAME}-conflicts.txt ${RESULTS_DIR}/merge_conflicts.txt
	else
		./find_conflicts.sh $REPO_DIR $RESULTS_DIR
	fi
fi

# Run Conflerge on found conflicts in the repo
#if [ ! -f res.txt ] || [ ! -d conflerge_results ]
#then
#  ./merge_conflicts.sh > res.txt
#fi

# Output <repositoryname>.csv file
#./make_csv.sh
