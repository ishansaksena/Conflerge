# script to run Conflerge testing a list of GitHub repos one by one
# TODO: parallelize this
import subprocess

from github import Github

g = Github("username", "password")

for repo in g.search_repositories("language:java", sort="stars", order="desc"):
	if repo.name == "weex":
		# clone the repo to /tmp/
		p = subprocess.Popen("git clone {0} && mv {1} /tmp/".format(repo.ssh_url, repo.name), shell=True).wait()

		# test the repo using Conflerge
		p = subprocess.Popen("./test_repo.sh {0} {1}".format(repo.name, repo.full_name.replace('/', '-')), shell=True).wait()
		# cleanup
		p = subprocess.Popen("rm -rf /tmp/{0}".format(repo.name), shell=True).wait()