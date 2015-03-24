#DEER-Service

A web service for [DEER](https://github.com/GeoKnow/DEER)
	
##Build

	mvn package
	
Then, copy the war package in a Servlet container (actual code is meant to be used in Tomcat 7)
	
##Configuration
	
Adapt in the web.xml a working directory :

    <!-- working directory -->
      <context-param>
      <description>Directory for saving Limes results</description>
      <param-name>workingDir</param-name>
      <param-value>/tmp/deer-service</param-value>
      </context-param>
      
## REST Calls


	

##Licence

The source code of this repo is published under the Apache License Version 2.0
