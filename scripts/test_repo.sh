#!/bin/bash

REPO_FULL_NAME=$1
REPO_DIR=/tmp/$(cut -d'-' -f2 <<< $1)
RESULTS_DIR=$REPO_FULL_NAME_test_results

# create a dir to store all of our test results in for this repo
if [ ! -d $RESULTS_DIR ]
then
	mkdir $RESULTS_DIR
fi

# Find all merge conflicts for the given repo
if [ ! -f $RESULTS_DIR/merge_conflicts.txt ]
then
  ./find_conflicts.sh $REPO_DIR $RESULTS_DIR
fi

# Run Conflerge on found conflicts in the repo
#if [ ! -f res.txt ] || [ ! -d conflerge_results ]
#then
#  ./merge_conflicts.sh > res.txt
#fi

# Output <repositoryname>.csv file
#./make_csv.sh
