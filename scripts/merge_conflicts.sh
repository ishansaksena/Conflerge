#!/bin/bash

function mergeCommits {
	# Get conflicting commits
  git checkout --force -b commit1 $1 
  git checkout --force -b commit2 $2
  git checkout commit1

	# Merge conflicting commits
  git merge commit2 > merge.txt
	
  # Clear the file we'll save successful files in
  rm files.txt
	
  # Read conflicts line-by-line
	while read CONFLICT
	do
		# Try to extract a .java file from the conflict line
		if [[ $CONFLICT =~ .*[[:space:]]([^\.]*\.java) ]]
		then
      # Make sure it's a content merge, not a file insertion or deletion
      if [[ $CONFLICT == *"content"* ]]
			then
				# Found one! Apply our mergetool
	      FILE=${BASH_REMATCH[1]}
  			RES="$(yes | git mergetool --tool=conflerge $FILE)"
        # Check if Conflerge succeeded
        if [[ $RES == *"SUCCESS"* ]] 
        then
          # It did! Output this so we can grep for it later
          echo "SUCCESS"
          # Write the result file to conflerge_results/...
          if [[ $FILE =~ .*/([^/]*.java) ]]
          then
            # Save the name of this file so we can use it later
            echo $FILE >> files.txt        
            # Write the merged file to the results folder
            FILENAME="conflerge_results/actual_"
            FILENAME+="${BASH_REMATCH[1]}"
            while [ -f $FILENAME ]
            do
              FILENAME+=1
            done
            cat $FILE > $FILENAME         
          fi   
        else
          # Conflerge failed; output this so we can grep for it
          echo "FAILURE"
        fi
      fi
		fi

	done <<< "$(grep CONFLICT merge.txt)"

	# Clean up the git state
  git reset --merge
  git checkout --force master
  git branch -D commit1
  git branch -D commit2
  git reset --hard master

  # Now, get the human merged files
  git checkout --force -b merged $3
  while read FILE
  do
    # Write the result file to conflerge_results/...
    if [[ $FILE =~ .*/([^/]*.java) ]]
    then
      # Write the merged file to the results folder
      FILENAME="conflerge_results/expected_"
      FILENAME+="${BASH_REMATCH[1]}"      
      while [ -f $FILENAME ]
      do
        FILENAME+=1
      done
      cat $FILE > $FILENAME         
    fi
   done < files.txt

  # Clean up the git state
  git reset --merge
  git checkout --force master
  git branch -D merged
  git reset --hard master

}

# Set up the destination 
mkdir conflerge_results

# Outer loop: read the contents of merge_conflicts.txt line by line
while read line
do
	COMMITS=($line)
	mergeCommits ${COMMITS[0]} ${COMMITS[1]} ${COMMITS[2]}

done < merge_conflicts.txt

rm merge.txt
