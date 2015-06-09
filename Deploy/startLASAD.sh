#!/bin/bash

#TODO You MIGHT need to change the filepath in quotations to the Tomcat directory to be used on your server
tomcatDir="${PWD}/apache-tomcat-8.0.23"
tomcatStartupScript="/bin/./startup.sh"

lasadServerPid=$(ps ax | grep '[L]ASAD-Server.jar' | awk '{print $1}')

if [[ "$lasadServerPid"  =~ ^[0-9]+$ ]]
then
echo "Server already running, ID: $lasadServerPid"
else
#TODO You MIGHT need to change the filepath in quotations to the lasad-server directory on your server
cd ${PWD}/lasad-server
echo "Starting server from dir: ${PWD}"

echo "Starting server"
java -Djava.security.policy=java.policy -jar LASAD-Server.jar server.cfg >> server.log 2>&1&
fi

echo "pausing before starting tomcat"
sleep 5

tomcatStartup=$tomcatDir$tomcatStartupScript
echo "Starting tomcat"
$tomcatStartup
