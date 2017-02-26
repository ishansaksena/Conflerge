#!/bin/bash
REPO_DIR=$1

function mergeCommits {

  # Get and merge the conflicting commits
  git -C ${REPO_DIR} checkout --force -b commit1 $1
  git -C ${REPO_DIR} checkout --force -b commit2 $2
  git -C ${REPO_DIR} checkout commit1
  git -C ${REPO_DIR} merge commit2 > merge.txt
  
  # Clear the file we'll save successful files in
  touch files.txt
  rm files.txt
  touch files.txt
  
  # Read each conflict line
  while read CONFLICT
  do

    # Try to extract a .java file from the conflict line
    if [[ $CONFLICT =~ .*[[:space:]]([^\.]*\.java) ]]
    then

      # Make sure it's a content merge, not a file insertion or deletion
      if [[ $CONFLICT == *"content"* ]]
      then

        # Found one! Get its name and apply our mergetool
        FILE=${BASH_REMATCH[1]}
        RES="$(yes | git -C ${REPO_DIR} mergetool --tool=conflerge $FILE)"

        # Check if Conflerge succeeded
        if [[ $RES == *"SUCCESS"* ]] 
        then

          # It did! Output this so we can grep for it later
          echo "SUCCESS"

          # Get the file's name without the full path
          if [[ $FILE =~ .*/([^/]*.java) ]]
          then

            # Save the name of this file so we can use it later
            echo $FILE >> files.txt        

            # Write the merged file to the results folder
            FILENAME="conflerge_results/actual_"
            FILENAME+="${BASH_REMATCH[1]}"

            # Make sure this file has unique name
            while [ -f $FILENAME ]
            do
              FILENAME+=1
            done
            
            # Write the file contents to the file in conflerge_results/
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
  git -C ${REPO_DIR} reset --merge
  git -C ${REPO_DIR} checkout --force master
  git -C ${REPO_DIR} branch -D commit1
  git -C ${REPO_DIR} branch -D commit2
  git -C ${REPO_DIR} reset --hard master

  # Now, get the human merged files
  git -C ${REPO_DIR} checkout --force -b merged $3
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
  git -C ${REPO_DIR} reset --merge
  git -C ${REPO_DIR} checkout --force master
  git -C ${REPO_DIR} branch -D merged
  git -C ${REPO_DIR} reset --hard master

}

# Set up the destination 
if [ ! -d conflerge_results ]
then
  mkdir conflerge_results
fi

# Outer loop: read the contents of merge_conflicts.txt line by line
while read line
do
  COMMITS=($line)
  mergeCommits ${COMMITS[0]} ${COMMITS[1]} ${COMMITS[2]}

done < merge_conflicts.txt

rm files.txt
rm merge.txt
