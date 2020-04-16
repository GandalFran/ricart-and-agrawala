#!/bin/bash

# ================================================== #
# 					constants						 #
# ================================================== #

# project
project_name=ssdd
service_path=/$project_name$service

# tomcat
tomcat_tar=/tmp/tomcat.tar.gz
tomcat_download_folder=/tmp/tomcat
tomcat_folder_final=/opt/tomcat
tomcat_uri=http://apache.uvigo.es/tomcat/tomcat-7/v7.0.103/bin/apache-tomcat-7.0.103.tar.gz

# client files location
client_jar_initial=$project_name.jar
client_jar_final=/tmp/$project_name.jar

# server files location
server_war_initial=$project_name.war
server_war_final=$tomcat_folder_final/webapps/$project_name.war

# tmp files
tmp_folder=/tmp/ssdd
ntp_file=$tmp_folder/ntp.bin
log_folder=$tmp_folder/log
log_file_sufix=simulation.log

# ================================================== #
# 					  utils							 #
# ================================================== #

# ssh utils
gen_key(){
	ssh-keygen -t rsa	
}

share_key(){
	user_host=$1
	ssh-copy-id -i $HOME/.ssh/id_rsa.pub $user_host
}

# basic utils
remote_exec(){
	user_host=$1
	command=$2
	ssh $user_host "$command"
}

copy_file(){
	user_host=$1
	initial=$2
	final=$3
	scp -rq $initial $user_host:$final
}

copy_file_from_remote(){
	user_host=$1
	initial=$2
	final=$3
	scp -rq $user_host:$initial $final
}

copy_file_between_remotes(){
	user_host_initial=$1
	user_host_final=$2
	initial=$3
	final=$4
	scp -rq $user_host_initial:$initial /tmp/transfer.tmp
	scp -rq /tmp/transfer.tmp $user_host_final:$final 
}

clean_file(){
	user_host=$1
	file=$2
	remote_exec $user_host "rm -rf $file"
}

# ntp utils
enable_ntp(){
	echo ""
	user_host=$1
	remote_exec $user_host "sudo service ntp start"
}

disable_ntp(){
	echo ""
	user_host=$1
	# se activa ntp porque no va a haber comprobaciones
	#remote_exec $user_host "timedatectl set-ntp 1"
	#remote_exec $user_host "sudo service ntp start"
}

# tomcat utils
download_tomcat(){
	wget -q $tomcat_uri -O $tomcat_tar
	tar -zxf $tomcat_tar --directory /tmp
	mv /tmp/apache-tomcat-7.0.103 $tomcat_download_folder
	rm $tomcat_tar
}

deploy_tomcat(){
	user_host=$1
	copy_file $user_host $tomcat_download_folder $tomcat_folder_final
	remote_exec $user_host "sudo chown -R vagrant:vagrant $tomcat_folder_final"
	copy_file $user_host tomcat.service /etc/systemd/system/tomcat.service
	remote_exec $user_host "sudo systemctl enable tomcat.service"
	remote_exec $user_host "sudo systemctl daemon-reload"
	remote_exec $user_host "sudo systemctl start tomcat.service"
}

restart_tomcat(){
	user_host=$1
	remote_exec $user_host "systemctl restart tomcat.service"
}

# server utils
clean_server(){
	user_host=$1
	clean_file $user_host $server_war_final
	clean_file $user_host $tomcat_folder_final/webapps/$project_name
}

# client utils
get_log_name(){
	hostid=$1
	echo "$log_folder/$hostid$log_file_sufix"
}

remote_exec_app(){
	user_host=$1
	appargs=$2
	remote_exec $user_host "java -jar $client_jar_final $appargs"
}

clean_tmp_files(){
	user_host1=$1
	user_host2=$2
	user_host3=$3
	clean_file $user_host1 $ntp_file
	clean_file $user_host1 $log_file_total
	clean_file $user_host1 $log_file_host1
	clean_file $user_host2 $log_file_host2
	clean_file $user_host3 $log_file_host3
}

run_application(){
	ntp_enabled=$1
	user=$2
	host1=$3
	host2=$4
	host3=$5

	user_host1="$user@$host1"
	user_host2="$user@$host2"
	user_host3="$user@$host3"

	# log files names
	log_file_host1=$(get_log_name 1)
	log_file_host2=$(get_log_name 2)
	log_file_host3=$(get_log_name 3)
	log_file_total=$(get_log_name total_)
	
	# cleaning files from other executions
	echo "cleaning temporary files in remotes (results of other executions) ..."
	clean_tmp_files $user_host1 $user_host2 $user_host3

	# run supervisor ntp
	if [ $ntp_enabled == "true" ]
	then
		echo "running supervisor ntp ..."
		remote_exec_app $user_host1 "supervisor ntp $ntp_file $host2 $host3"
	fi

	# restart critical section and run simulation
	echo "running simulation ..."
	remote_exec_app $user_host1 "supervisor restartCs 6 $host1 $host2 $host3"
	remote_exec_app $user_host1 "simulation $log_file_host1 6 1 2 0 $host1 $host2 $host3" &
	remote_exec_app $user_host2 "simulation $log_file_host2 6 3 4 1 $host1 $host2 $host3" &
	remote_exec_app $user_host3 "simulation $log_file_host3 6 5 6 2 $host1 $host2 $host3"

	# calculate ntp in supervisor
	if [ $ntp_enabled == "true" ]
	then
		echo "running supervisor ntp ..."
		remote_exec_app $user_host1 "supervisor ntp $ntp_file $host2 $host3"
	fi

	# copy logs to machine where supervisor is allocated
	echo "copying logs to supervisor..."
	copy_file_between_remotes $user_host2 $user_host1 $log_file_host2 $log_file_host2
	copy_file_between_remotes $user_host3 $user_host1 $log_file_host3 $log_file_host3

	# correct time in logs
	if [ $ntp_enabled == "true" ]
	then
		echo "adjusting time in logs ..."
		remote_exec_app $user_host1 "supervisor correctLog $ntp_file $log_file_host2 $host2 $log_file_host3 $host3"
	fi
	
	# join and sort logs
	echo "joining and sorting logs ..."
	remote_exec $user_host1 "cat $log_file_host1 $log_file_host2 $log_file_host3 | awk NF | sort -k 3 > $log_file_total"

	# run supervisor log comprobation
	if [ $ntp_enabled == "true" ]
	then
		echo "run log comprobation ..."
		remote_exec_app $user_host1 "verification $ntp_file $log_file_total $log_file_host2 $log_file_host3"
	else
		echo "run log comprobation ..."
		remote_exec_app $user_host1 "verification $ntp_file $log_file_total"
	fi

	# clean temporary files in remotes
	echo "cleaning temporary files in remotes ..."
	#clean_tmp_files $user_host1 $user_host2 $user_host3
	remote_exec $user_host1 "cp $log_file_total /home/vagrant/ssdd/simulation.log"
}

# ================================================== #
# 					application						 #
# ================================================== #

args=( "$@" )
argsNumber=$#

if [ $argsNumber -eq 0 ]
then
	echo "ERROR: no arguments detected, run deployer.bash -h for help"
fi

for (( i=0; i<$argsNumber; i+=1 ))
do
	selected_command=${args[$i]}
	case $selected_command in
      	"-h") 
        	echo -e "SSDD deployer"
        	echo -e "\t-genkey: generates ssh key"
        	echo -e "\t-newsession <user> <host>: copy ssh key in host"
        	echo -e "\t-deploy <user> <host1> <host2> <host3> : deploys application in the given hosts"
        	echo -e "\t-redeploy <user> <host1> <host2> <host3> : cleans current application deploy, then deploys the application again"
        	echo -e "\t-run <ntp> <user> <host1> <host2> <host3> : runs application in the given hosts"
        	echo -e "\t-clean <user> <host1> <host2> <host3> : cleans current deploy"
        	echo -e "\t-h help"
        	echo -e "EXAMPLES"
        	echo -e "\t- generate SSH key and export sessions"
        	echo -e "\t\tbash deployer.bash -genkey -newsession user vm1"
        	echo -e "\t- deploy all"
        	echo -e "\t\tbash deployer.bash -deploy user vm1 vm2 vm3"
        	echo -e "\t- redeploy all"
        	echo -e "\t\tbash deployer.bash -redeploy user vm1 vm2 vm3"
        	echo -e "\t- run application"
        	echo -e "\t\tbash deployer.bash -run true user vm1 vm2 vm3"
        	echo -e "\t- clean current deploy"
        	echo -e "\t\tbash deployer.bash -clean user vm1 vm2 vm3"
      ;;
      "-genkey")
			gen_key
      ;;
      "-newsession")
			i=$((i+1))
			user=${args[$i]}
			i=$((i+1))
			host=${args[$i]}
			user_host="$user@$host"
			share_key $user_host
      ;;
      "-deploy")
			i=$((i+1))
			user=${args[$i]}
			i=$((i+1))
			host1=${args[$i]}
			i=$((i+1))
			host2=${args[$i]}
			i=$((i+1))
			host3=${args[$i]}

			# check if user is root, because if not, deploy is impossible (because of service creation)
			if [ $user != "root" ]
			then
				echo "ERROR: deploy needs root permissions"
				break
			fi

			# calculate user_hosts
			user_host1="$user@$host1"
			user_host2="$user@$host2"
			user_host3="$user@$host3"

			# create temporary and log fodlers in remote
			remote_exec $user_host1 "mkdir -p $tmp_folder"
			remote_exec $user_host2 "mkdir -p $tmp_folder"
			remote_exec $user_host3 "mkdir -p $tmp_folder"
			remote_exec $user_host1 "mkdir -p $log_folder"
			remote_exec $user_host2 "mkdir -p $log_folder"
			remote_exec $user_host3 "mkdir -p $log_folder"

			# download tomcat
			echo "downloading tomcat ..."
			download_tomcat

			# deploy tomcat in each server
			echo "deploying tomcat in servers ..."
			deploy_tomcat $user_host1
			deploy_tomcat $user_host2
			deploy_tomcat $user_host3

			# copy client files
			echo "copying new client files ..."
			copy_file $user_host1 $client_jar_initial $client_jar_final
			copy_file $user_host2 $client_jar_initial $client_jar_final
			copy_file $user_host3 $client_jar_initial $client_jar_final

			# copy server files
			echo "copying new server files ..."
			copy_file $user_host1 $server_war_initial $server_war_final
			copy_file $user_host2 $server_war_initial $server_war_final
			copy_file $user_host3 $server_war_initial $server_war_final

			# wait for server to complete deploy
			echo "waiting 10 seconds to allow servers to complete the deploy ..."
			sleep 10

      ;;
      "-redeploy")
			i=$((i+1))
			user=${args[$i]}
			i=$((i+1))
			host1=${args[$i]}
			i=$((i+1))
			host2=${args[$i]}
			i=$((i+1))
			host3=${args[$i]}

			# check if user is root, because if not, deploy is impossible (because of service creation)
			if [ $user != "root" ]
			then
				echo "ERROR: redeploy needs root permissions"
				break
			fi

			# calculate user_hosts
			user_host1="$user@$host1"
			user_host2="$user@$host2"
			user_host3="$user@$host3"

			# create temporary and log fodlers in remote
			remote_exec $user_host1 "mkdir -p $tmp_folder"
			remote_exec $user_host2 "mkdir -p $tmp_folder"
			remote_exec $user_host3 "mkdir -p $tmp_folder"
			remote_exec $user_host1 "mkdir -p $log_folder"
			remote_exec $user_host2 "mkdir -p $log_folder"
			remote_exec $user_host3 "mkdir -p $log_folder"

			# clean client files
			echo "cleaning older client files ..."
			clean_file $user_host1 $client_jar_final
			clean_file $user_host2 $client_jar_final
			clean_file $user_host3 $client_jar_final

			# clean server files
			echo "cleaning older server files ..."
			clean_server $user_host1
			clean_server $user_host2
			clean_server $user_host3

			# copy client files
			echo "copying new client files ..."
			copy_file $user_host1 $client_jar_initial $client_jar_final
			copy_file $user_host2 $client_jar_initial $client_jar_final
			copy_file $user_host3 $client_jar_initial $client_jar_final

			# copy server files
			echo "copying new server files ..."
			copy_file $user_host1 $server_war_initial $server_war_final
			copy_file $user_host2 $server_war_initial $server_war_final
			copy_file $user_host3 $server_war_initial $server_war_final

			# wait for server to complete deploy
			echo "waiting 10 seconds to allow servers to complete the deploy ..."
			sleep 10

      ;;
      "-run")
			i=$((i+1))
			ntp_enabled=${args[$i]}
			i=$((i+1))
			user=${args[$i]}
			i=$((i+1))
			host1=${args[$i]}
			i=$((i+1))
			host2=${args[$i]}
			i=$((i+1))
			host3=${args[$i]}

			# calculate user_hosts
			user_host1="$user@$host1"
			user_host2="$user@$host2"
			user_host3="$user@$host3"

			# start or stop ntp
			if [ $ntp_enabled == "true" ]
			then
				echo ""
				enable_ntp $user_host1
				enable_ntp $user_host2
				enable_ntp $user_host3
			elif  [ $ntp_enabled == "false" ]
			then
				disable_ntp $user_host1
				disable_ntp $user_host2
				disable_ntp $user_host3
			else
				echo "ERROR: ntp value should be true or false"
				break
			fi

			# kill old java processes
			remote_exec $user_host1 "pkill -f 'java -jar $client_jar_final'"
			remote_exec $user_host2 "pkill -f 'java -jar $client_jar_final'"
			remote_exec $user_host3 "pkill -f 'java -jar $client_jar_final'"

			# run application
			echo "running application ..."
			START=$SECONDS
			run_application $ntp_enabled $user $host1 $host2 $host3
			ELAPSED=$(( SECONDS - START ))
			echo "elapsed time: $(TZ=UTC0 printf '%(%H:%M:%S)T\n' "$ELAPSED")"
      ;;
      "-clean")
			i=$((i+1))
			user=${args[$i]}
			i=$((i+1))
			host1=${args[$i]}
			i=$((i+1))
			host2=${args[$i]}
			i=$((i+1))
			host3=${args[$i]}

			# calculate user_hosts
			user_host1="$user@$host1"
			user_host2="$user@$host2"
			user_host3="$user@$host3"

			# clean client files in remotes
			echo "cleaning client files ..."
			clean_file $user_host1 $client_jar_final
			clean_file $user_host2 $client_jar_final
			clean_file $user_host3 $client_jar_final

			# clean tomcat and server files in remotes
			echo "cleaning tomcat and server files ..."
			clean_file $user_host1 $tomcat_folder_final
			clean_file $user_host2 $tomcat_folder_final
			clean_file $user_host3 $tomcat_folder_final

			# clean log and tmp files folders
			clean_file $user_host1 $log_folder
			clean_file $user_host2 $log_folder
			clean_file $user_host3 $log_folder
			clean_file $user_host1 $tmp_folder
			clean_file $user_host2 $tmp_folder
			clean_file $user_host3 $tmp_folder
			
      ;;
      *)
        	echo "ERROR: unknown command: $selected_command, run deployer.bash -h for help"
        	break
      ;;
  	esac
done