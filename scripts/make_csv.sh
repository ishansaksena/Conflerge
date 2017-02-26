#!/bin/bash

./rundiff.sh $2

REPO=$1

CONFLICTS=$(grep FAILURE $2/res.txt | wc -l)

SUCCESSES=$(grep SUCCESS $2/res.txt | wc -l)

PERFECT_MERGES=$(grep SUCCESS $2/*.out | wc -l)

PERFECT_NO_COMMENTS=$(grep SUCCESS $2/*.outc | wc -l)

CONFLICTS=$(($CONFLICTS + $SUCCESSES))

PERCENT_RESOLVED=$[SUCCESSES*100/CONFLICTS]

PERCENT_PERFECT=$[PERFECT_MERGES*100/SUCCESSES]

PERCENT_PERFECT_NO_COMMENTS=$[PERFECT_NO_COMMENTS*100/SUCCESSES]

echo ",Conflicts Found,Conflicts Resolved,Perfect Resolutions, Perfect w/o Comments,% Conflicts Resolved,% Perfect Resolutions, % Perfect No Comments" > $REPO.csv
printf "%s,%d,%d,%d,%d,%d,%d,%d" "$REPO" "$CONFLICTS" "$SUCCESSES" "$PERFECT_MERGES" "$PERFECT_NO_COMMENTS" "$PERCENT_RESOLVED" "$PERCENT_PERFECT" "$PERCENT_PERFECT_NO_COMMENTS" >> $REPO.csv
