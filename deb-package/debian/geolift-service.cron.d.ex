#
# Regular cron jobs for the geolift-service package
#
0 4	* * *	root	[ -x /usr/bin/geolift-service_maintenance ] && /usr/bin/geolift-service-ui_maintenance
