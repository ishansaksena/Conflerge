Testing Conflerge
-----------------

1) Clone the repo you want to test on and move all the files in this directory into that repo.
We used the Javaparser project (https://github.com/javaparser/javaparser) in the example in our 
paper. As the paper mentions, these scripts do not generate interesting results on all repositories.

2)	Add these lines to your ~/.gitconfig file:

		[mergetool "conflerge"]
		  cmd = java -jar Conflerge.jar $BASE $LOCAL $REMOTE $MERGED
		[merge]
		  tool = conflerge

3) Ensure that all the .sh files have run permission.

4) Run ./test_repo.sh in the repo being tested.

5) Once this finishes, the results will be stored in <repo name>.csv
