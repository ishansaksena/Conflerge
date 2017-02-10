Testing Conflerge
-----------------

First, clone the repo you want to test on and move all the files in this directory into that repo.

1)	Add these lines to your ~/.gitconfig file:

		[mergetool "conflerge"]
		  cmd = java -jar Conflerge.jar $BASE $LOCAL $REMOTE $MERGED
		[merge]
		  tool = conflerge

2) Run:

		./find_conflicts.sh. 

	This can take a while but you only need to do it once. It writes all the 
	commits that cause conflicts to merge_conflicts.txt, so do not delete that file 
	or you'll need to re-run the script.

	(Note: If either script won't run, change its permission with 'chmod +x find_conflicts.sh')

3) Run:
	
	./merge_conflicts.sh > res.txt. 

	After this is done, you can see the number of failed and successful merges with:

		grep FAILURE res.txt | wc -l 
		grep SUCCESS res.txt | wc -l

	More importantly, it creates a directory called conflerge_results, which has 
	all the files Conflerge merged (actual_*.java) and all the corresponding
	human-merged files (expected_*.java). Now we can compare them!

	(Note: If a file's name ends in '1', '11', etc it's because there were multiple versions of 
	that file. Test 'em all.)

4) The included Diff.jar file is handy for comparing the actual_ and expected_ files. Run:

		'java -jar ../Diff.jar actual_<name>.java expected_<name>.java'

	to compare the two files with a token diff. 

	If this doesn't output anything, the files don't differ and it's a success! If they do differ, the 
	output will not be super helpful so you'll need to compare manually. Sometimes it gives false alarms
	for things like import order, etc.

	(Note: every file pair differs by at least one line: '// Merged by Conflerge', which I added 
	temporarily to distinguish the merged files)

5) JavaParser.txt contains a lil writeup I did from running this process on the JavaParser repo. I noted 
the total conflicts, total merges, and the number of good and bad merges. I also gave a brief description 
of why each bad merge was bad. This is more or less a good template for us, although maybe we should switch
to a spreadsheet...

Have fun : )

