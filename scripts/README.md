#Assessing Conflerge with Github repositories

### Reproducing results

* Update your `.gitconfig` to include:
```bash
[mergetool "conflerge-tree"]
    cmd = java -jar ~/Conflerge/scripts/ConflergeTree.jar $BASE $LOCAL $REMOTE $MERGED
[mergetool "conflerge-token"]
    cmd = java -jar ~/Conflerge/scripts/ConflergeToken.jar $BASE $LOCAL $REMOTE $MERGED
[merge]
    tool = conflerge-tree
    tool = conflerge-token
```

*  Ensure that all `.sh` files in `Conflerge/scripts` have run permission

*  From inside `Conflerge/scripts`, run:

`python3 tests_for_paper.py {tree|token}`

* Specifying the positional argument as either `tree` or `token` is required, and determines whether to evaluate Conflerge using a tree or token based merge strategy
* The default set of Github repositories on which to test is defined in `Conflerge/scripts/repos.txt`
        *       As the script runs it will output test results for each individual repo to files in the form `[repo]_[merge_strategy].csv` (ie `javaparser_token.csv`)
        *  Once every repository in `repos.txt` has been processed, the script will output an aggregated summary of results to `totals.csv`
        
Note: The most recent update to Conflerge has (positively) shifted the ratio of "Correct" resolutions and "Correct without comments" resolutions. Otherwise, the results are unchanged. The versions in this repository's .jar files are the same used to generate the tests so the results can be reproduced. If you wish to use Conflerge, please use the .jar files in the repo's top-level directory: the versions here do not correctly handle conflicting comments.

