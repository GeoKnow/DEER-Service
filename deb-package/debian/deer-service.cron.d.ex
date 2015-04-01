#
# Regular cron jobs for the deer-service package
#
0 4	* * *	root	[ -x /usr/bin/deer-service_maintenance ] && /usr/bin/deer-service_maintenance
