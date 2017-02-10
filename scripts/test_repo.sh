#!/bin/bash

# Find all merge conflicts
if [ ! -f merge_conflicts.txt ]
then
  ./find_conflicts.sh
fi

# Run Conflerge on merge conflicts
if [ ! -f res.txt ] || [ ! -d conflerge_results ]
then
  ./merge_conflicts.sh > res.txt
fi

# Output <repositoryname>.csv file
./make_csv.sh
