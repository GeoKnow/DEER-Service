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

Path | Method | Description
-----|-----|-----
/ | GET | Get an array of the Ids of executed processes
/{uuid} | GET | Get the configuration file of a given process ID
/ |POST | Post a new process, will return a JSON object the location of the output

### Executind a Deer process

For executing a Deer process, the POST method has to be called with the following specs:

Spec | Description
-----|-----
Path | /
Method | POST
Content-Type | text/turtle
body | the configuration of a deer in text/turtle

## Deer configuration

The configuration of deer

##Licence

Apache License Version 2.0
