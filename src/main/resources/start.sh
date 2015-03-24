#!/bin/bash

PID=./optolink.pid


prog_arg="-Dlogback.configurationFile=lib/logback.xml"
prog_arg="${prog_arg} -Djava.library.path=/usr/lib/jni"
prog_arg="${prog_arg} -Dgnu.io.rxtx.SerialPorts=/dev/ttyAMA0"


java $prog_arg -jar lib/optolink-jar-with-dependencies.jar >/dev/null 2>&1 &
echo $! >$PID

echo "Optolink adapter started"