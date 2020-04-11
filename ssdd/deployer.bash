#!/bin/bash

# ================================================== #
# 					constants						 #
# ================================================== #
project_name=s3
service=/ntp
tomcat7_path=/home/i0901148/Escritorio/tomcat

# other constants
client_jar_initial=../../../desktop/$project_name.jar
client_jar_final=/tmp/$project_name.jar

server_war_initial=../../../desktop/$project_name.war
server_war_final=$tomcat7_path/webapps/$project_name.war

service_path=/$project_name$service

# ================================================== #
# 					  utils							 #
# ================================================== #
gen_key(){
	ssh-keygen -t rsa	
}

share_key(){
	user_host=$1
	ssh-copy-id -i $HOME/.ssh/id_rsa.pub $user_host
}

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
	remote_exec $user_host "rm $file"
}

deploy_tomcat(){
	user_host=$1
	tomcat_uri=https://apache.brunneis.com/tomcat/tomcat-7/v7.0.100/bin/apache-tomcat-7.0.100.tar.gz

	# download tomcat
	wget $tomcat_uri
	# unzip tomcat
	tar -zxf apache-tomcat-7.0.100.tar.gz
	# copy files
	copy_files $user_host apache-tomcat-7.0.100 $tomcat7_path
	# rm tar.gz
	rm apache-tomcat-7.0.100.tar.gz
	rm -rf apache-tomcat-7.0.100
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
        	echo -e "\t-session <user>@<host>: copy ssh key in host"
        	echo -e "\t-cleanclient <user>@<host>: copy client files in host"
        	echo -e "\t-cleanserver <user>@<host>: copy server files in host"
        	echo -e "\t-copyclient <user>@<host>: copy client files in host"
        	echo -e "\t-copyserver <user>@<host>: copy server files in host"
        	echo -e "\t-run <user>@<host> <num_iteration> <marzullo> <server_ip1> <server_ip2> <server_ip3>: run client in host"
        	echo -e "\t-deploy <user>@<server> <user>@<server1> <user>@<server1> <user>@<server3> <server_ip1> <server_ip2> <server_ip3> <num_iterations> <marzullo>"
        	echo -e "\t-redeploy <user>@<server> <user>@<server1> <user>@<server1> <user>@<server3> <server_ip1> <server_ip2> <server_ip3> <num_iteration> <marzullo>"
        	echo -e "\t-h help"
        	echo -e "EXAMPLES"
        	echo -e "\t- deploy all"
        	echo -e "\t\tbash deployer.bash -deploy client@172.20.1.0 server1@172.20.1.1 server2@172.20.1.2 server3@172.20.1.3 172.20.1.1 172.20.1.2 172.20.1.3 8 true"
        	echo -e "\t- redeploy all"
        	echo -e "\t\tbash deployer.bash -redeploy client@172.20.1.0 server1@172.20.1.1 server2@172.20.1.2 server3@172.20.1.3 172.20.1.1 172.20.1.2 172.20.1.3 8 true"

      ;;
      "-genkey")
			gen_key
      ;;
      "-session")
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
			clean_file $user_host $server_war_final
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
			remote_exec $user_host "$tomcat7_path/bin/shutdown.sh"
			remote_exec $user_host "$tomcat7_path/bin/startup.sh"
			echo "wait 5 seconds for server to deploy copied .war file properly..."
			sleep 5
      ;;
      "-run")
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
      "-deploy")
			i=$((i+1))
			user_client=${args[$i]}
			i=$((i+1))
			user_server1=${args[$i]}
			i=$((i+1))
			user_server2=${args[$i]}
			i=$((i+1))
			user_server3=${args[$i]}
			i=$((i+1))
			server_ip_1=${args[$i]}
			i=$((i+1))
			server_ip_2=${args[$i]}
			i=$((i+1))
			server_ip_3=${args[$i]}
			i=$((i+1))
			num_iterations=${args[$i]}
			i=$((i+1))
			is_marzullo=${args[$i]}

			# generate session key and share keys
			gen_key
			share_key $user_client
			share_key $user_server1
			share_key $user_server2
			share_key $user_server3

			# deploy tomcat in each server
			deploy_tomcat $user_server1
			deploy_tomcat $user_server2
			deploy_tomcat $user_server3
			remote_exec $user_server1 "$tomcat7_path/bin/startup.sh"
			remote_exec $user_server2 "$tomcat7_path/bin/startup.sh"
			remote_exec $user_server3 "$tomcat7_path/bin/startup.sh"

			# copy client files
			copy_files $user_client $client_jar_initial $client_jar_final

			# copy server files
			copy_files $user_server1 $server_war_initial $server_war_final
			copy_files $user_server2 $server_war_initial $server_war_final
			copy_files $user_server3 $server_war_initial $server_war_final
			sleep 5

			# calculate services uris
			service_uri_1="http://$server_ip_1$service_path"
			service_uri_2="http://$server_ip_2$service_path"
			service_uri_3="http://$server_ip_3$service_path"

			# run client
			remote_exec $user_client "java -jar $client_jar_final $num_iterations $is_marzullo $service_uri_1 $service_uri_2 $service_uri_3"
      ;;
      "-redeploy")
			i=$((i+1))
			user_client=${args[$i]}
			i=$((i+1))
			user_server1=${args[$i]}
			i=$((i+1))
			user_server2=${args[$i]}
			i=$((i+1))
			user_server3=${args[$i]}
			i=$((i+1))
			server_ip_1=${args[$i]}
			i=$((i+1))
			server_ip_2=${args[$i]}
			i=$((i+1))
			server_ip_3=${args[$i]}
			i=$((i+1))
			num_iterations=${args[$i]}
			i=$((i+1))
			is_marzullo=${args[$i]}

			# clean server and client files
			clean_file $user_client $client_jar_final
			clean_file $user_server1 $server_war_final
			clean_file $user_server2 $server_war_final
			clean_file $user_server3 $server_war_final

			# restart tomcat in each server
			remote_exec $user_server1 "$tomcat7_path/bin/shutdown.sh"
			remote_exec $user_server2 "$tomcat7_path/bin/shutdown.sh"
			remote_exec $user_server3 "$tomcat7_path/bin/shutdown.sh"
			remote_exec $user_server1 "$tomcat7_path/bin/startup.sh"
			remote_exec $user_server2 "$tomcat7_path/bin/startup.sh"
			remote_exec $user_server3 "$tomcat7_path/bin/startup.sh"

			# copy client files
			copy_files $user_client $client_jar_initial $client_jar_final

			# copy server files
			copy_files $user_server1 $server_war_initial $server_war_final
			copy_files $user_server2 $server_war_initial $server_war_final
			copy_files $user_server3 $server_war_initial $server_war_final
			sleep 5

			# calculate services uris
			service_uri_1="http://$server_ip_1$service_path"
			service_uri_2="http://$server_ip_2$service_path"
			service_uri_3="http://$server_ip_3$service_path"

			# run client
			remote_exec $user_host "java -jar $client_jar_final $num_iterations $is_marzullo $service_uri_1 $service_uri_2 $service_uri_3"
      ;;
      *)
        	echo "ERROR: unknown command: $selected_command, run deployer.bash -h for help"
        	break
      ;;
  	esac
done