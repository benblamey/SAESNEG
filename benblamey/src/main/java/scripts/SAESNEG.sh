#!/bin/bash

# Set this directory to be the working directory.
cd "$(dirname "$0")"

while true
do
	# nice - to specify a lower CPU scheduling priority.
	# note that classpath separator is : and not ; in Linux.
	# Double angle brackets to append.
	nice -n 10 java -classpath /var/lib/tomcat/webapps/benblamey.evaluation/WEB-INF/lib/benblamey.jar:/var/lib/tomcat/webapps/benblamey.evaluation/WEB-INF/lib/* benblamey.saesneg.DaemonMain >> /var/log/SAESNEG.log 2>> /var/log/SAESNEG.log < /dev/null
	sleep 10
done

# To run on a particular user, try like this:
# java -classpath /var/lib/tomcat/webapps/benblamey.evaluation/WEB-INF/lib/benblamey.jar:/var/lib/tomcat/webapps/benblamey.evaluation/WEB-INF/lib/* benblamey.saesneg.DaemonMain "Ben Blamey"
