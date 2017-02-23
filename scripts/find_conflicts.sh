#!/bin/bash

touch $2
rm $2
touch $2

DEFAULT_BRANCH=$(git -C $1 rev-parse --abbrev-ref HEAD)

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
			echo "$COMMIT_1 $COMMIT_2 $MERGE" >> $2
			git -C $1 reset --merge
			echo "--------------------------------"
			echo "   Found $CONFLICTS conflicts" 
			echo "--------------------------------"
	fi

	# Clean up
	git -C $1 checkout $DEFAULT_BRANCH
	git -C $1 branch -D commit1
	git -C $1 branch -D commit2
	git -C $1 reset --hard $DEFAULT_BRANCH
done <<< "$(git -C $1 rev-list --merges --max-parents=2 HEAD)"

rm merge.txt

echo "--------------------------------"
echo "    Finished Finding Conflicts"
echo "--------------------------------"
