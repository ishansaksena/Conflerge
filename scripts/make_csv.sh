#!/bin/bash

./rundiff.sh

REPO=$(basename "$PWD")

CONFLICTS=$(grep FAILURE res.txt | wc -l)

SUCCESSES=$(grep SUCCESS res.txt | wc -l)

PERFECT_MERGES=$(grep SUCCESS conflerge_results/*.out | wc -l)

PERFECT_NO_COMMENTS=$(grep SUCCESS conflerge_results/*.outc | wc -l)

PERFECT_NO_COMMENTS_IMPORTS=$(grep SUCCESS conflerge_results/*.outci | wc -l)

CONFLICTS=$(($CONFLICTS + $SUCCESSES))

PERCENT_RESOLVED=$[SUCCESSES*100/CONFLICTS]

PERCENT_PERFECT=$[PERFECT_MERGES*100/SUCCESSES]

PERCENT_PERFECT_NO_COMMENTS=$[PERFECT_NO_COMMENTS*100/SUCCESSES]

PERCENT_PERFECT_NO_COMMENTS_IMPORTS=$[PERFECT_NO_COMMENTS_IMPORTS*100/SUCCESSES]

echo ",Conflicts Found,Conflicts Resolved,Perfect Resolutions, Perfect w/o comments, Perfect w/o comments or imports, % Conflicts Resolved,% Perfect Resolutions, %Perfect No Comments, %Perfect w/o comments or imports" > $REPO.csv
printf "%s,%d,%d,%d,%d,%d,%d,%d,%d,%d" "$REPO" "$CONFLICTS" "$SUCCESSES" "$PERFECT_MERGES" "$PERFECT_NO_COMMENTS" "$PERFECT_NO_COMMENTS_IMPORTS" "$PERCENT_RESOLVED" "$PERCENT_PERFECT" "$PERCENT_PERFECT_NO_COMMENTS" "$PERCENT_PERFECT_NO_COMMENTS_IMPORTS" >> $REPO.csv
