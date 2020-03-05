#!/bin/bash

# ================================================== #
# 					constants						 #
# ================================================== #

# project
service=/ntp
project_name=s3
service_path=/$project_name$service
project_folder=/home/gandalfran/repos/ssdd-ejercicios/$project_name

# tomcat
tomcat_tar=/tmp/tomcat.tar.gz
tomcat_download_folder=/tmp/tomcat
tomcat_folder_final=/home/i0901148/Escritorio/tomcat
tomcat_uri=https://apache.brunneis.com/tomcat/tomcat-7/v7.0.100/bin/apache-tomcat-7.0.100.tar.gz

# client files location
client_jar_initial=/tmp/$project_name.jar
client_jar_final=/tmp/$project_name.jar

# server files location
server_war_initial=/tmp/$project_name.war
server_war_final=$tomcat_folder_final/webapps/$project_name.war

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

copy_files(){
	user_host=$1
	initial=$2
	final=$3
	scp -rq $initial $user_host:$final
}

clean_file(){
	user_host=$1
	file=$2
	remote_exec $user_host "rm -rf $file"
}

# project jar and war generation utils

generate_project_jar(){
	classes_folder="$project_folder/src"
	jar -cfm $client_jar_initial $manifest_file $project_folder
}

generate_project_war(){
	jar -cf $server_war_initial $project_folder
}

regenerate_project_files(){
	rm $client_jar_initial
	rm $server_war_initial
	generate_project_jar
	generate_project_war
}

# tomcat utils

download_tomcat(){
	wget -q $tomcat_uri -O $tomcat_tar
	tar -zxf $tomcat_tar -C $tomcat_download_folder
	rm $tomcat_tar
}

deploy_tomcat(){
	user_host=$1
	copy_files $user_host $tomcat_download_folder $tomcat_folder_final
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
        	echo -e "\t-cleanclient <user>@<host>: clean client files in host"
        	echo -e "\t-cleanserver <user>@<host>: clean server files in host"
        	echo -e "\t-copyclient <user>@<host>: copy client files in host"
        	echo -e "\t-copyserver <user>@<host>: copy server files in host"
        	echo -e "\t-runclient <user>@<host> <execution args>: run client in host"
        	echo -e "\t-deployall <user> <server1> <server2> <server3> <execution args>: deploy all in indicated client and servers"
        	echo -e "\t-redeployall <user> <server1> <server2> <server3> <execution args>: redeploy all in indicated client and servers"
        	echo -e "\t-cleanall <user> <server1> <server2> <server3> <execution args>: clean all server and client files (tomcat included)"
        	echo -e "\t-h help"
        	echo -e "EXAMPLES"
        	echo -e "\t- deploy all"
        	echo -e "\t\tbash deployer.bash -deployall user 172.20.1.0 172.20.1.1 172.20.1.2"
        	echo -e "\t- redeploy all"
        	echo -e "\t\tbash deployer.bash -redeployall user 172.20.1.0 172.20.1.1 172.20.1.2"
        	echo -e "\t- clean all"
        	echo -e "\t\tbash deployer.bash -cleanall user 172.20.1.0 172.20.1.1 172.20.1.2"
      ;;
      "-genkey")
			gen_key
      ;;
      "-newsession")
			i=$((i+1))
			user_host=${args[$i]}
			share_key $user_host
      ;;
      "-cleanclient")
			i=$((i+1))
			user_host=${args[$i]}
			clean_file $user_host $client_jar_final
      ;;
      "-cleanserver")
			i=$((i+1))
			user_host=${args[$i]}
			clean_server $user_host $server_war_final
      ;;
      "-copyclient")
			i=$((i+1))
			user_host=${args[$i]}
			copy_files $user_host $client_jar_initial $client_jar_final
      ;;
      "-copyserver")
			i=$((i+1))
			user_host=${args[$i]}
			copy_files $user_host $server_war_initial $server_war_final
			remote_exec $user_host "$tomcat_folder_final/bin/shutdown.sh"
			remote_exec $user_host "$tomcat_folder_final/bin/startup.sh"
			echo "wait 5 seconds for server to deploy copied .war file properly..."
			sleep 5
      ;;
      "-runclient")
			i=$((i+1))
			user_host=${args[$i]}
			i=$((i+1))
			num_iterations=${args[$i]}
			i=$((i+1))
			is_marzullo=${args[$i]}
			i=$((i+1))
			server_ip_1=${args[$i]}
			i=$((i+1))
			server_ip_2=${args[$i]}
			i=$((i+1))
			server_ip_3=${args[$i]}

			service_uri_1="http://$server_ip_1$service_path"
			service_uri_2="http://$server_ip_2$service_path"
			service_uri_3="http://$server_ip_3$service_path"

			remote_exec $user_host "java -jar $client_jar_final $num_iterations $is_marzullo $service_uri_1 $service_uri_2 $service_uri_3"
      ;;
      "-deployall")
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

			# calculate services uris
			service_uri_1="http://$host1:8080$service_path"
			service_uri_2="http://$host2:8080$service_path"
			service_uri_3="http://$host3:8080$service_path"

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
			echo "generating project .jar and .war files ..."
			generate_project_jar
			generate_project_war

			# copy client files
			echo "copying new client files ..."
			copy_files $user_host1 $client_jar_initial $client_jar_final
			copy_files $user_host2 $client_jar_initial $client_jar_final
			copy_files $user_host3 $client_jar_initial $client_jar_final

			# copy server files
			echo "copying new server files ..."
			copy_files $user_host1 $server_war_initial $server_war_final
			copy_files $user_host2 $server_war_initial $server_war_final
			copy_files $user_host3 $server_war_initial $server_war_final

			# wait for server to complete deploy
			echo "waiting 5 seconds to allow servers to complete the deploy ..."
			sleep 5

			# run clients
			remote_exec $user_host1 "java -jar $client_jar_final $service_uri_1 $service_uri_2 $service_uri_3" &
			remote_exec $user_host2 "java -jar $client_jar_final $service_uri_1 $service_uri_2 $service_uri_3" &
			remote_exec $user_host3 "java -jar $client_jar_final $service_uri_1 $service_uri_2 $service_uri_3"
      ;;
      "-redeployall")
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

			# calculate services uris
			service_uri_1="http://$host1:8080$service_path"
			service_uri_2="http://$host2:8080$service_path"
			service_uri_3="http://$host3:8080$service_path"

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
			sleep 2

			# restart tomcat in each server
			echo "restarting tomcat ..."
			restart_tomcat  $user_host1
			restart_tomcat  $user_host2
			restart_tomcat  $user_host3

			# regenerate client and server files
			echo "generating project .jar and .war files ..."
			regenerate_project_files

			# copy client files
			echo "copying new client files ..."
			copy_files $user_host1 $client_jar_initial $client_jar_final
			copy_files $user_host2 $client_jar_initial $client_jar_final
			copy_files $user_host3 $client_jar_initial $client_jar_final

			# copy server files
			echo "copying new server files ..."
			copy_files $user_host1 $server_war_initial $server_war_final
			copy_files $user_host2 $server_war_initial $server_war_final
			copy_files $user_host3 $server_war_initial $server_war_final

			# wait for server to complete deploy
			echo "waiting 5 seconds to allow servers to complete the deploy ..."
			sleep 5

			# run clients
			remote_exec $user_host1 "java -jar $client_jar_final $service_uri_1 $service_uri_2 $service_uri_3" &
			remote_exec $user_host2 "java -jar $client_jar_final $service_uri_1 $service_uri_2 $service_uri_3" &
			remote_exec $user_host3 "java -jar $client_jar_final $service_uri_1 $service_uri_2 $service_uri_3"
      ;;
      "-cleanall")
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

			# clean tomcat download folder
			echo "cleaning tomcat download files ..."
			rm $tomcat_download_folder

			# cleaning project .jar and .war files
			echo "cleaning project .jar and .war files ..."
			rm $client_jar_initial
			rm $server_war_initial

			# shutdown tomcat in each server
			echo "shutting down tomcat ..."
			remote_exec $user_host1 "$tomcat_folder_final/bin/shutdown.sh"
			remote_exec $user_host2 "$tomcat_folder_final/bin/shutdown.sh"
			remote_exec $user_host3 "$tomcat_folder_final/bin/shutdown.sh"

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
      ;;
      *)
        	echo "ERROR: unknown command: $selected_command, run deployer.bash -h for help"
        	break
      ;;
  	esac
done