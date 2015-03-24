#!/bin/bash


prog_arg="-Dlogback.configurationFile=conf/logback_debug.xml"
prog_arg="${prog_arg} -Djava.library.path=/usr/lib/jni"
prog_arg="${prog_arg} -Dgnu.io.rxtx.SerialPorts=/dev/ttyAMA0"


java $prog_arg -jar lib/optolink-jar-with-dependencies.jar