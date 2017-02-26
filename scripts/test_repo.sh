#!/bin/bash

REPO_FULL_NAME=$2
REPO_NAME=$(cut -d'-' -f2 <<< $1)
REPO_DIR=/tmp/${REPO_NAME}
RESULTS_DIR=${REPO_FULL_NAME}_test_results


# create a directory to store this repo's data
if [ ! -d ${RESULTS_DIR} ]
	then
	mkdir ${RESULTS_DIR}
fi

# Find all merge conflicts for the given repo
if [ ! -f ${RESULTS_DIR}/merge_conflicts.txt ]
	then
	# copy if this is one of the repos already checked (in previous versions of this script)
	if [ -f /tmp/${REPO_FULL_NAME}-conflicts.txt ]
		then
		echo "Found a previous conflict report for this repo, copying..."
		cp /tmp/${REPO_FULL_NAME}-conflicts.txt ${RESULTS_DIR}/merge_conflicts.txt
	else
		./find_conflicts.sh ${REPO_DIR} ${RESULTS_DIR}
	fi
fi

# Run Conflerge on found conflicts in the repo
if [ ! -f ${RESULTS_DIR}/res.txt ]
	then
	./merge_conflicts.sh ${REPO_DIR} ${RESULTS_DIR} > ${RESULTS_DIR}/res.txt
fi

# Output <repositoryname>.csv file
./make_csv.sh ${REPO_NAME} ${RESULTS_DIR}
