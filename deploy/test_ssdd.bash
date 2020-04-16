#!/bin/bash

NUM_ITERATIONS=10

NTP=true
EFFECTIVE_USER=root
#MACHINES=( "192.168.1.101" "192.168.1.102" "192.168.1.103" )
MACHINES=( "vm1" "vm2" "vm3" )

# clean and deploy
echo "CLEAN"
bash deployer.bash -clean $EFFECTIVE_USER ${MACHINES[0]} ${MACHINES[1]} ${MACHINES[2]}
echo "DEPLOY"
bash deployer.bash -deploy $EFFECTIVE_USER ${MACHINES[0]} ${MACHINES[1]} ${MACHINES[2]}

# iterate n times
for ((i = 1; i <= NUM_ITERATIONS; i++)); do
	echo "RUN ITERATION: $i"
	bash deployer.bash -redeploy $EFFECTIVE_USER ${MACHINES[0]} ${MACHINES[1]} ${MACHINES[2]}
	bash deployer.bash -run $NTP $EFFECTIVE_USER ${MACHINES[0]} ${MACHINES[1]} ${MACHINES[2]}
done