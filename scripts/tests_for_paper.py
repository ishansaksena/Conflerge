# This script runs evaluation tests on a set of git repositories
# as specified in Conflerge/scripts/repos.txt
# summarizing the results in a single table
import os
import argparse
import subprocess

parser = argparse.ArgumentParser()
parser.add_argument(
    "merging_strategy",
    help="Selects which merge approach to test conflerge with (trees or tokens)",
    choices=["tree", "token"]
)
args = parser.parse_args()

with open('repos.txt', 'r') as file:
    for line in file:
        repo = line.split('/')[-1].split('.')[0]
        org = line.split('/')[-2]

        # clone the repo into /tmp
        p = subprocess.Popen("git clone {0}".format(line), shell=True).wait()

        p = subprocess.Popen("mv {0} /tmp/".format(repo), shell=True)

        # generate conflerge results for repo
        p = subprocess.Popen("./test_repo.sh {0} {1} {2}".format(repo, org + '-' + repo, args.merging_strategy), shell=True).wait()

        # remove the repository
        p = subprocess.Popen("rm -rf /tmp/{0}".format(repo), shell=True).wait()


# Returns string representation of num/denom as a percentage
def percent_str(num,denom):
    return "{0:.1f}".format(100*num/denom)

csv_header = ""
csv_data = []

# Look for csv results files
for filename in os.listdir("."):

    # Ignore irrelevant files
    if not filename.endswith(args.merging_strategy + ".csv"):
        continue

    # Store the data line
    with open(filename) as f:
        lines = f.readlines()
        csv_header = lines[0].strip()
        data = lines[1]

    csv_data.append(data)

# Create the overall results file
with open(args.merging_strategy + "_totals.csv", "w") as file:

    file.write(csv_header + "\n")

    # Track cumulative numbers
    total_conflicts = 0
    total_unresolved = 0
    total_perfect = 0
    total_perf_nc = 0
    total_incorrect = 0

    # Process each repo's data
    for line in csv_data:

        # Separate by csv item
        items = line.split(",")

        # Get the counts from the csv
        repo = items[0]
        conflicts = int(items[1])
        unresolved = int(items[2])
        perfect = int(items[3])
        perf_nc = int(items[4])
        incorrect = int(items[5])

        # Get percentages from counts
        data_out = [repo,
                    str(conflicts),
                    percent_str(unresolved,conflicts),
                    percent_str(perfect, conflicts),
                    percent_str(perf_nc, conflicts),
                    percent_str(incorrect, conflicts)]

        # Write data_out to the output csv
        file.write(",".join(data_out) + "\n")

        # Update the totals
        total_conflicts += conflicts
        total_unresolved += unresolved
        total_perfect += perfect
        total_perf_nc += perf_nc
        total_incorrect += incorrect

    # Get the totals row
    data_out = ["TOTALS",
                str(total_conflicts),
                percent_str(total_unresolved, total_conflicts),
                percent_str(total_perfect, total_conflicts),
                percent_str(total_perf_nc, total_conflicts),
                percent_str(total_incorrect, total_conflicts)]

    # Write totals row to file
    file.write(",".join(data_out)+"\n")

# clean up any remaining intermediate files (results directories)
p = subprocess.Popen("rm -rf ./*results", shell=True).wait()

