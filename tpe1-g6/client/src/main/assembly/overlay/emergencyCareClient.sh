#!/bin/bash

PATH_TO_CODE_BASE=`pwd`

#JAVA_OPTS="-Djava.rmi.server.codebase=file://$PATH_TO_CODE_BASE/lib/jars/rmi-params-client-1.0-SNAPSHOT.jar"

SERVICE="emergencyCareClient"
MAIN_CLASS="ar.edu.itba.pod.tpe1.client.Client"

java $JAVA_OPTS $* -Dservice=$SERVICE -cp 'lib/jars/*'  $MAIN_CLASS
