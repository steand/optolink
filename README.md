# optolink

Viessmann heating systems with vitotronic has a optolink Interface for maintenance. 
This interface can use for get/set data in the heating system.
For more information about this interface see: http://openv.wikispaces.com/

The java-application is a slim adapter to this interface.
On southbound it use the serial interface for connect the optolink interface (special hardware requert).
On northbound it provides a TCP/IP raw Port for communication and a UDP/IP Port as broadcast interface
to search the adapter in the local network.

Primary is is develop for a adaption from [openhab2](https://github.com/openhab/openhab2/). 
It supports on the northbound the concept of openhab2 things.

##Build
The application is develop in Eclipse (Luna) with maven support.
Requierd Lib's: rxtx, slf4j, logback (see pom.xml file)
You can build runtime by Run->Run As->Maven install.
jar file is found in the folder ./traget

##Install & running
All test was running on a Raspberry PI B with Raspbian "wheezy".  
Installing:  
   
1. Install the rxtx (apt)
2. Config /dev/ttyAMA0 (see: https://cae2100.wordpress.com/2012/12/23/raspberry-pi-and-the-serial-port/)
3. Install your optolink hardware 
4. copy: optolink-<version>-SNAPSHOT-jar-with-dependencies.jar from the target folder to Rasberry
5. copy optolink.xml and logback.xml to Rasberry - same folder than jar file.
6. Edit optolink.xml for your heading system.
7. start it: ```java  -Dlogback.configurationFile=./logback.xml -Djava.library.path=/usr/lib/jni -Dgnu.io.rxtx.SerialPorts=/dev/ttyAMA0 -jar optolink-0.0.1-SNAPSHOT-jar-with-dependencies.jar ```  


##Test it
Run a terminal programm (like putty), connect to you raspberry by using port 31113 and raw protocol.
Suported command:
  
* list -> list all (thing) definition (in xml-File)
* get <Thing.id> -> get Data for thing from heating system.

##Further doing (my ToDo List)
1. integration to openlink2 (binding) -> priority
2. implement set function
3. implementaion of logfile.
3. bug-fixing
4. Full integration in PI (start.sh, logfile, run it with startup PI,..)
last: Build a stable version (together with openhab2).





