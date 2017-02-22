#!/bin/bash

touch merge_conflicts.txt
rm merge_conflicts.txt
touch merge_conflicts.txt

while read MERGE
do
	# Get the merge's parent commits
	COMMIT_1=$(git -C $1 rev-parse $MERGE^1)
	COMMIT_2=$(git -C $1 rev-parse $MERGE^2)

	git -C $1 checkout --force -b commit1 $COMMIT_1
	git -C $1 checkout --force -b commit2 $COMMIT_2

	# Attempt to merge the parents
	git -C $1 checkout commit1
	git -C $1 merge commit2 > merge.txt

	# Handle conflicts
	CONFLICTS=$(grep CONFLICT merge.txt | grep content | wc -l)
	if [ $CONFLICTS -gt 0 ]; then
			echo "$COMMIT_1 $COMMIT_2 $MERGE" >> merge_conflicts.txt
			git -C $1 reset --merge
			echo "--------------------------------"
			echo "   Found $CONFLICTS conflicts" 
			echo "--------------------------------"
	fi

	# Clean up
	git -C $1 checkout master
	git -C $1 branch -D commit1
	git -C $1 branch -D commit2
	git -C $1 reset --hard master
done <<< "$(git -C $1 rev-list --merges --max-parents=2 HEAD)"

rm merge.txt

echo "--------------------------------"
echo "    Finished Finding Conflicts"
echo "--------------------------------"
