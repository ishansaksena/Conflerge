import subprocess

with open('repos.txt', 'r') as file:
	for line in file:
		repo = line.split('/')[-1].split('.')[0]
		org = line.split('/')[-2]

		# clone the repo into /tmp
		p = subprocess.Popen("git clone {0} && mv {1} /tmp/".format(line, repo), shell=True).wait()

		# generate conflerge results for repo
		p = subprocess.Popen("./test_repo.sh {0} {1}".format(repo, org + '-' + repo), shell=True).wait()

		# remove the repo
		p = subprocess.Popen("rm -rf /tmp/{0}".format(repo), shell=True).wait()
