#!/bin/bash

stopScript="/Users/kevin/LASAD-CMU/Deploy/stopLASAD.sh"
startScript="/Users/kevin/LASAD-CMU/Deploy/startLASAD.sh"

echo "Rebooting LASAD"
lasadStop=$stopScript
$lasadStop

declare -i i=0

while [[ $(ps ax | grep '[L]ASAD-Server.jar' | awk '{print $1}') =~ ^[0-9]+$ ]]
do
    i=$((i+1))
    sleep 2
    if [ ! $i -lt 10 ]
    then
        break
    fi
done

lasadStart=$startScript
$lasadStart

declare -i j=0

while true
do
    if [[ $(ps ax | grep '[L]ASAD-Server.jar' | awk '{print $1}') =~ ^[0-9]+$ ]]
    then
        echo "This is an automated test for notification of SUCCESSFUL reboot of LASAD via email service"
        break
    else
        j=$((j+1))
        sleep 2
        if [ ! $j -lt 10 ]
        then
            echo "This is an automated test for notification of FAILED reboot of LASAD via email service"
            break
        fi
    fi
done
