#!/bin/bash

touch merge_conflicts.txt
rm merge_conflicts.txt
touch merge_conflicts.txt

while read MERGE
do
	# Get the merge's parent commits
	COMMIT_1=$(git rev-parse $MERGE^1)
	COMMIT_2=$(git rev-parse $MERGE^2)
	
	git checkout --force -b commit1 $COMMIT_1	
	git checkout --force -b commit2 $COMMIT_2

	# Attempt to merge the parents
	git checkout commit1
	git merge commit2 > merge.txt

	# Handle conflicts
	CONFLICTS=$(grep CONFLICT merge.txt | grep content | wc -l)
	if [ $CONFLICTS -gt 0 ]; then
			echo "$COMMIT_1 $COMMIT_2 $MERGE" >> merge_conflicts.txt
			git reset --merge
			echo "--------------------------------"
			echo "   Found $CONFLICTS conflicts" 
			echo "--------------------------------"
	fi

	# Clean up
	git checkout master
	git branch -D commit1
	git branch -D commit2
	git reset --hard master	
done <<< "$(git rev-list --merges --max-parents=2 HEAD)"

rm merge.txt

echo "--------------------------------"
echo "    Finished Finding Conflicts"
echo "--------------------------------"
