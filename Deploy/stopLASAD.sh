#!/bin/bash

#TODO Change the filepath in quotations to the Tomcat directory to be used on your server
tomcatDir="/Users/kevin/LASAD-8/Deploy/apache-tomcat-8.0.23"
tomcatShutdown="/bin/./shutdown.sh"

lasadServerPid=$(ps ax | grep '[L]ASAD-Server.jar' | awk '{print $1}')

if [[ "$lasadServerPid"  =~ ^[0-9]+$ ]]
then
echo "Shutting down processId $lasadServerPid"
kill -9 $lasadServerPid
else
echo "No server process to kill"
fi

tomcatShutdown=$tomcatDir$tomcatShutdown
echo "Shutting down $tomcatShutdown"
$tomcatShutdown