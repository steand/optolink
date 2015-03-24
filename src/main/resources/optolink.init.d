#!/bin/sh
# Start/stop the optolink 
#
### BEGIN INIT INFO
# Provides:          optolink
# Default-Start:     2 3 4 5
# Default-Stop:
# Short-Description: Regular background program processing Viessmann optolink adapter
### END INIT INFO

HOME=/home/pi/optolink
USER=pi

PATH=/bin:/usr/bin:/sbin:/usr/sbin

test -f $HOME/start.sh || exit 0



case "$1" in
start)	cd $HOME 
	sudo -u $USER ./start.sh &
	;;
stop)   cd $HOME 
        ./stop.sh 
        ;;
restart) log_daemon_msg "Restarting periodic command scheduler" "cron" 
        $0 stop
        $0 start
        ;;
status)
        exit 0 
        ;;
*)	log_action_msg "Usage: /etc/init.d/cron {start|stop|status|restart}"
        exit 2
        ;;
esac
exit 0
