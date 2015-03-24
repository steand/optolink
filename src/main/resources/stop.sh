#!/bin/bash

PID=./optolink.pid


if [ -f $PID ]; then  
  ps `cat $PID` 
  if [ $? == 0 ]; then
   kill `cat $PID` 
   echo "Optolink adapter killed"
   rm $PID
  else
   echo "Prozess not found"
   exit 1
  fi
else
  echo "File ${PID} found"
  exit 1
fi
