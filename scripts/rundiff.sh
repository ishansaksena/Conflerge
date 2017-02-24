#!/bin/bash

pushd conflerge_results
for FILE in `ls`
  do
    if [[ $FILE =~ (expected_|actual_)(.*) ]]
    then
      F1="expected_"
      F2="actual_"
      F="${BASH_REMATCH[2]}"
      F1+="${BASH_REMATCH[2]}"      
      F2+="${BASH_REMATCH[2]}"
      echo $F1
      echo $F2
      java -jar ../DiffTool.jar $F1 $F2 > $F.out
      java -jar ../DiffTool.jar -c $F1 $F2 > $F.outc
      java -jar ../DiffTool.jar -c -i $F1 $F2 > $F.outci
    fi
done
popd
