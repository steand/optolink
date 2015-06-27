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
Files for runtime are found in ./traget
Or download the install package from Github.

##Install & running
All test was running on a Raspberry PI B with Raspbian "wheezy".  
Installing:  
   
1. Install the rxtx (apt)
2. Config /dev/ttyAMA0 (see: https://cae2100.wordpress.com/2012/12/23/raspberry-pi-and-the-serial-port/)
3. Install your optolink hardware 
4. copy: optolink-runtime.zip from the target folder to Rasberry and extract it.
5. Edit conf/optolink.xml for your heading system.
6. start it: ```./start_debug.sh ```  


##Test it
Run a terminal programm (like putty), connect to you raspberry by using port 31113 and raw protocol.
Suported command:
  
* list -> list all (thing) definition (in xml-File)
* get <Thing.id> [<channel.id>,<channel.id>,..] -> get Data for thing from heating system.
* set <ThingId>:<ChannelId> <value>  (The syntax of Value is not checked)

##Further doing (my ToDo List)
1. bug-fixing (if bugs found ;-)
2. Build a stable version (together with openhab2).





