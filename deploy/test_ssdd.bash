#!/bin/bash

NUM_ITERATIONS=10

NTP="true"
EFFECTIVE_USER=root
MACHINES=( "vm1" "vm2" "vm3")

run(){
	# shuffle array
	MACHINES_SHUFFLED=($(shuf -e "${MACHINES[@]}"))
	M1=${MACHINES_SHUFFLED[0]}
	M2=${MACHINES_SHUFFLED[1]}
	M3=${MACHINES_SHUFFLED[2]}

	# log information
	echo "	configuration: $M1 $M2 $M3"

	# run application
	bash deployer.bash -run $NTP $EFFECTIVE_USER $M1 $M2 $M3
}


# clean and deploy
echo "CLEAN"
bash deployer.bash -clean $EFFECTIVE_USER ${MACHINES[0]} ${MACHINES[1]} ${MACHINES[2]}
echo "DEPLOY"
bash deployer.bash -deploy $EFFECTIVE_USER ${MACHINES[0]} ${MACHINES[1]} ${MACHINES[2]}

# iterate n times
for ((i = 1; i <= NUM_ITERATIONS; i++)); do
	echo "ITERATION: $i"
	bash deployer.bash -redeploy $EFFECTIVE_USER ${MACHINES[0]} ${MACHINES[1]} ${MACHINES[2]}
	bash deployer.bash -run $NTP $EFFECTIVE_USER ${MACHINES[0]} ${MACHINES[1]} ${MACHINES[2]}
done