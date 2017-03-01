Testing Conflerge
-----------------

# Testing on a Single Repository

1) Clone the repo you want to test on into /tmp/

2)	Add these lines to your ~/.gitconfig file:

[mergetool "conflerge-tree"]
    cmd = java -jar ~/Conflerge/scripts/ConflergeTree.jar $BASE $LOCAL $REMOTE $MERGED
[mergetool "conflerge-token"]
    cmd = java -jar ~/Conflerge/scripts/ConflergeToken.jar $BASE $LOCAL $REMOTE $MERGED
[merge]
    tool = conflerge-tree
    tool = conflerge-token

3) Ensure that all the .sh files in Conflerge/scripts have run permission.

4) Run ./test-repo.sh from inside the Conflerge/scripts directory

	- test_repo.sh accepts 3 arguments with the form:

	`./test_repo.sh [repo name] [repouser-reponame] [merging approach]`

	- As an example, to test Conflerge using the tree-based merging approach on the 'elasticsearch' repo (https://github.com/elastic/elasticsearch) you would run:

	`./test_repo.sh elasticsearch elastic-elasticsearch tree`

5) Once this finishes, the results will be stored in <repo name>_<mergeapproach>.csv (ie elasticsearch_tree.csv)
