#!/bin/bash

#TODO Change the filepath in quotations to the Tomcat directory to be used on your server
tomcatDir="/Users/kevin/LASAD-8/Deploy/apache-tomcat-8.0.23"
tomcatStartupScript="/bin/./startup.sh"

lasadServerPid=$(ps ax | grep '[L]ASAD-Server.jar' | awk '{print $1}')

if [[ "$lasadServerPid"  =~ ^[0-9]+$ ]]
then
echo "Server already running, ID: $lasadServerPid"
else
#TODO Change the filepath to lasad-server on your server (not in quotations)
cd /Users/kevin/LASAD-8/Deploy/lasad-server
echo "Starting server from dir: $pwd"

echo "Starting server"
java -Djava.security.policy=java.policy -jar LASAD-Server.jar server.cfg >> server.log 2>&1&
fi

echo "pausing before starting tomcat"
sleep 5

tomcatStartup=$tomcatDir$tomcatStartupScript
echo "Starting tomcat"
$tomcatStartup
