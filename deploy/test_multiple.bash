#!/bin/bash

NUM_ITERATIONS=30
EFFECTIVE_USER=root
#MACHINES=( "192.168.1.101" "192.168.1.102" "192.168.1.103" )
MACHINES=( "vm1" "vm2" "vm3" )

getElapsedTime(){
	text=$1
	elapsed=$(echo "$text" | grep -E 'elapsed time: [0-9]+:[0-9]+:[0-9]+' | awk 'BEGIN {FS=" "; OFS=" "} {print $3}')
	echo "$elapsed"
}

getCSViolations(){
	text=$1
	violations=$(echo "$text" | grep -E 'Violaciones detectadas: [0-9]+' | awk 'BEGIN {FS=" "; OFS=" "} {print $3}')
	echo "$violations"
}

getCSDisolvedViolations(){
	text=$1
	disolved=$(echo "$text" | grep -E 'disuelta' | awk 'BEGIN {FS=" "; OFS=" "} {print $3}')
	echo "$disolved"
}

# print time estimation

TIME=$(( 60 + 270*$NUM_ITERATIONS))
echo "Estimated time to run all tests: $(TZ=UTC0 printf '%(%H:%M:%S)T\n' "$TIME")"

# clean and deploy
echo "Cleaning"
bash deployer.bash -clean $EFFECTIVE_USER ${MACHINES[0]} ${MACHINES[1]} ${MACHINES[2]} > /dev/null
echo "Deploying"
bash deployer.bash -deploy $EFFECTIVE_USER ${MACHINES[0]} ${MACHINES[1]} ${MACHINES[2]} > /dev/null

# iterate n times
for ((i = 1; i <= NUM_ITERATIONS; i++)); do
	echo "Iteration $i"

	echo -e "\t - redeploying"
	bash deployer.bash -redeploy $EFFECTIVE_USER ${MACHINES[0]} ${MACHINES[1]} ${MACHINES[2]} > /dev/null

	echo -e "\t - running simulation"
	text=$(bash deployer.bash -run true $EFFECTIVE_USER ${MACHINES[0]} ${MACHINES[1]} ${MACHINES[2]})

	elapsed=$(getElapsedTime "$text")
	num_violations=$(getCSViolations "$text")
	disolved=$(getCSDisolvedViolations "$text")

	echo -e "\t - elapsed time: $elapsed"
	echo -e "\t - critical section violations: $num_violations"
	echo -e "\t - disolved: $disolved"
	if [ "$num_violations" -gt "0" ]
	then
		echo -e "\t - detected violations: "
		echo "$text"
	fi
done



