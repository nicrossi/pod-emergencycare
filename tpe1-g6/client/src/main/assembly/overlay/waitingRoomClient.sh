#!/bin/bash

SERVICE="waitingRoomClient"
MAIN_CLASS="ar.edu.itba.pod.tpe1.client.Client"

java $JAVA_OPTS $* -Dservice=$SERVICE -cp 'lib/jars/*'  $MAIN_CLASS