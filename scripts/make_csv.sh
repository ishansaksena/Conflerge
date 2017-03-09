#!/bin/bash

./rundiff.sh $2

# The tested repository
REPO=$1

# Number of conflicts Conflerge failed to merge
UNRESOLVED=$(grep FAILURE $2/res.txt | wc -l)

# Number of conflicts Conflerge merged
RESOLVED=$(grep SUCCESS $2/res.txt | wc -l)

# Number of Conflerge merges identical to human merges
PERFECT=$(grep SUCCESS $2/*.out | wc -l)

# Number of Conflerge merges identical to human merges, ignoring comments
ALL_PERFECT_NC=$(grep SUCCESS $2/*.outc | wc -l)

# Total number of tested conflicts
CONFLICTS=$[UNRESOLVED+RESOLVED]

# Number of Conflerge merges identical to human merges but differing in comments
PERFECT_NC=$[ALL_PERFECT_NC-PERFECT]

# Number of incorrect merges
INCORRECT=$[RESOLVED-ALL_PERFECT_NC]

echo "Repo,Conflicts,Unresolved,Correct,CorrectNC,Incorrect" > ${REPO}_$3.csv
printf "%s,%d,%d,%d,%d,%d" "$REPO" "$CONFLICTS" "$UNRESOLVED" "$PERFECT" "$PERFECT_NC" "$INCORRECT" >> ${REPO}_$3.csv
