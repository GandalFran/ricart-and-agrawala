#!/bin/bash

FOLDER=logs
NUM_ITERATIONS=$1

USER=root
MACHINES=( "192.168.37.133" "192.168.37.130" "192.168.37.132")


function run(){
	command=$1

	# shuffle array
	MACHINES_SHUFFLED=($(shuf -e "${MACHINES[@]}"))
	M1=${MACHINES_SHUFFLED[0]}
	M2=${MACHINES_SHUFFLED[1]}
	M3=${MACHINES_SHUFFLED[2]}

	# log information
	echo "server configuration: $M1 $M2 $M3"

	# run application
	sudo bash deployer.bash -$command $USER $M1 $M2 $M3
}


# clean
echo "CLEAN"
run "clean"

# deploy first time
echo "ITERATION: 0"
run "deploy"

# iterate n-1 times
for (( i=1; i<$NUM_ITERATIONS; i+=1 ))
do
	echo "ITERATION: $i"
	run "redeploy"
done

#clean last time
echo "CLEAN"
run "clean"