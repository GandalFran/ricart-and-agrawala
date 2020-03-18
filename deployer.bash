#!/bin/bash

# ================================================== #
# 					constants						 #
# ================================================== #

# project
project_name=ssdd
service_path=/$project_name$service

# build project
project_folder=/home/gandalfran/repos/ssdd-ejercicios/$project_name
build_folder=/tmp/build

# tomcat
tomcat_tar=/tmp/tomcat.tar.gz
tomcat_download_folder=/tmp/tomcat
tomcat_folder_final=/tmp/tomcat
tomcat_uri=https://apache.brunneis.com/tomcat/tomcat-7/v7.0.100/bin/apache-tomcat-7.0.100.tar.gz

# client files location
client_jar_initial=$project_name.jar
client_jar_final=/tmp/$project_name.jar

# server files location
server_war_initial=$project_name.war
server_war_final=$tomcat_folder_final/webapps/$project_name.war

# tmp files
ntp_tmp_file=/tmp/ntp.tmp
log_folder=/tmp
log_file_sufix=simulation.log

# ================================================== #
# 					  utils							 #
# ================================================== #

# ssh key

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

# project jar and war generation utils

generate_build(){
	# if not exists the build folder, then create it
	if [ ! -d "$build_folder" ]; then
		mkdir $build_folder
	fi
	cp -R $project_folder $build_folder
	find $build_folder -type f -name *.java -exec javac '{}' \; rm '{}' \;
}

generate_project_jar(){
	jar -cfm $client_jar_initial $manifest_file $build_folder
}

generate_project_war(){
	jar -cf $server_war_initial $build_folder
}

generate_project_files(){
	generate_build
	generate_project_jar
	generate_project_war
}

clean_project(){
	if [ -f "$client_jar_initial" ]; then
		rm $client_jar_initial
	fi
	if [ -f "$server_war_initial" ]; then
		rm $server_war_initial
	fi
	if [ -d "$build_folder" ]; then
		rm -rf $build_folder
	fi
}

# tomcat utils

download_tomcat(){
	wget -q $tomcat_uri -O $tomcat_tar
	tar -zxf $tomcat_tar --directory /tmp
	mv /tmp/apache-tomcat-7.0.100 $tomcat_download_folder
	rm $tomcat_tar
}

deploy_tomcat(){
	user_host=$1
	copy_file $user_host $tomcat_download_folder $tomcat_folder_final
	remote_exec $user_host "$tomcat_folder_final/bin/startup.sh"
}

restart_tomcat(){
	user_host=$1
	remote_exec $user_host "$tomcat_folder_final/bin/shutdown.sh"
	sleep 1
	remote_exec $user_host "$tomcat_folder_final/bin/startup.sh"
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

run_application(){
	user=$1
	host1=$2
	host2=$3
	host3=$4

	user_host1="$user@$host1"
	user_host2="$user@$host2"
	user_host3="$user@$host3"

	# log files names
	log_file_host1=$(get_log_name 0)
	log_file_host2=$(get_log_name 1)
	log_file_host3=$(get_log_name 2)
	log_file_total=$(get_log_name total_)

	# clean temporary files from other executions in remotes
	echo "cleaning temporary files in remotes ..."
	clean_file $user_host1 $ntp_tmp_file
	clean_file $user_host1 $log_file_total
	clean_file $user_host1 $log_file_host1
	clean_file $user_host2 $log_file_host2
	clean_file $user_host3 $log_file_host3

	# run supervisor ntp
	echo "running supervisor ntp ..."
	remote_exec_app $user_host1 "supervisor ntp $ntp_tmp_file $host2 $host3"

	# restart critical section and run simulation
	echo "running simulation ..."
	remote_exec_app $user_host1 "supervisor restartCs 6 $host1 $host2 $host3"
	remote_exec_app $user_host1 "simulation $log_file_host1 6 1 2 0 $host1 $host2 $host3" &
	remote_exec_app $user_host2 "simulation $log_file_host2 6 3 4 1 $host1 $host2 $host3" &
	remote_exec_app $user_host3 "simulation $log_file_host3 6 5 6 2 $host1 $host2 $host3"

	# calculate ntp in supervisor
	echo "running supervisor ntp ..."
	remote_exec_app $user_host1 "supervisor ntp $ntp_tmp_file $host2 $host3"

	# copy logs to machine where supervisor is allocated
	echo "copying logs to supervisor..."
	copy_file_between_remotes $user_host2 $user_host1 $log_file_host2 $log_file_host2
	copy_file_between_remotes $user_host3 $user_host1 $log_file_host3 $log_file_host3

	# correct time in logs
	echo "adjusting time in logs ..."
	remote_exec_app $user_host1 "supervisor correctLog $ntp_tmp_file 1 $log_file_host2 $host2 2 $log_file_host3 $host3"

	# join and sort logs
	echo "joining and sorting logs ..."
	remote_exec $user_host1 "cat $log_file_host1 $log_file_host2 $log_file_host3 | sort -k 3 | awk NF > $log_file_total"

	# run supervisor log comprobation
	echo "run log comprobation ..."
	remote_exec_app $user_host1 "verification $ntp_tmp_file $log_file_total"

	# clean temporary files in remotes
	echo "cleaning temporary files in remotes ..."
	clean_file $user_host1 $ntp_tmp_file
	clean_file $user_host1 $log_file_total
	clean_file $user_host1 $log_file_host1
	clean_file $user_host2 $log_file_host2
	clean_file $user_host3 $log_file_host3
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
        	echo -e "\t-newsession <user>@<host>: copy ssh key in host"
        	echo -e "\t-run <user> <host1> <host2> <host3> : runs application in the given hosts"
        	echo -e "\t-deploy <user> <host1> <host2> <host3> : deploys and runs application in the given hosts"
        	echo -e "\t-redeploy <user> <host1> <host2> <host3> : cleans current deploy, then deploys and runs application in the given hosts"
        	echo -e "\t-clean <user> <host1> <host2> <host3> : cleans current deploy"
        	echo -e "\t-h help"
        	echo -e "EXAMPLES"
        	echo -e "\t- generate SSH key and export sessions"
        	echo -e "\t\tbash deployer.bash -genkey -newsession user@172.20.1.0"
        	echo -e "\t- deploy all"
        	echo -e "\t\tbash deployer.bash -deploy user 172.20.1.0 172.20.1.1 172.20.1.2"
        	echo -e "\t- redeploy all"
        	echo -e "\t\tbash deployer.bash -redeploy user 172.20.1.0 172.20.1.1 172.20.1.2"
        	echo -e "\t- run application"
        	echo -e "\t\tbash deployer.bash -run user 172.20.1.0 172.20.1.1 172.20.1.2"
        	echo -e "\t- clean current deploy"
        	echo -e "\t\tbash deployer.bash -clean user 172.20.1.0 172.20.1.1 172.20.1.2"
      ;;
      "-genkey")
			gen_key
      ;;
      "-newsession")
			i=$((i+1))
			user_host=${args[$i]}
			share_key $user_host
      ;;
      "-run")
			i=$((i+1))
			user=${args[$i]}
			i=$((i+1))
			host1=${args[$i]}
			i=$((i+1))
			host2=${args[$i]}
			i=$((i+1))
			host3=${args[$i]}
			run_application $user $host1 $host2 $host3
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

			# calculate user_hosts
			user_host1="$user@$host1"
			user_host2="$user@$host2"
			user_host3="$user@$host3"

			# generate session key and share keys
			echo "preparing ssh connections ..."
			gen_key
			share_key $user_host1
			share_key $user_host2
			share_key $user_host3

			# download tomcat
			echo "downloading tomcat ..."
			download_tomcat

			# deploy tomcat in each server
			echo "deploying tomcat in servers ..."
			deploy_tomcat $user_host1
			deploy_tomcat $user_host2
			deploy_tomcat $user_host3

			# generate client and server files
			# echo "generating project .jar and .war files ..."
			# generate_project_jar
			# generate_project_war

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
			echo "waiting 5 seconds to allow servers to complete the deploy ..."
			sleep 5

			# run application
			echo "running application ..."
			run_application $user $host1 $host2 $host3
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

			# calculate user_hosts
			user_host1="$user@$host1"
			user_host2="$user@$host2"
			user_host3="$user@$host3"

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
			# wait for server to detect that files has been deleted
			sleep 5

			# restart tomcat in each server
			echo "restarting tomcat ..."
			restart_tomcat  $user_host1
			restart_tomcat  $user_host2
			restart_tomcat  $user_host3

			# regenerate client and server files
			# echo "generating project .jar and .war files ..."
			# regenerate_project_files

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
			echo "waiting 5 seconds to allow servers to complete the deploy ..."
			sleep 5

			# run application
			echo "running clients ..."
			run_application $user $host1 $host2 $host3
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

			# shutdown tomcat in each server
			echo "shutting down tomcat ..."
			remote_exec $user_host1 "$tomcat_folder_final/bin/shutdown.sh"
			remote_exec $user_host2 "$tomcat_folder_final/bin/shutdown.sh"
			remote_exec $user_host3 "$tomcat_folder_final/bin/shutdown.sh"

			# clean tomcat download folder
			echo "cleaning tomcat download files ..."
			rm -rf $tomcat_download_folder

			# cleaning local project build folder, .jar and .war files
			echo "cleaning local project files ..."
			# clean_project

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

			# clean tmp folder where temporary files are located
			remote_exec $user_host1 "rm -rf /tmp/*"
			remote_exec $user_host2 "rm -rf /tmp/*"
			remote_exec $user_host3 "rm -rf /tmp/*"
			
      ;;
      *)
        	echo "ERROR: unknown command: $selected_command, run deployer.bash -h for help"
        	break
      ;;
  	esac
done