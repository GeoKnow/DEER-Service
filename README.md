#DEER-Service

A web service for [DEER](https://github.com/GeoKnow/DEER)	
##Building

Tested in Tomcat7

To deploy the service to your Tomcat server run:

	mvn tomcat:deploy
	
or to redeploy:

	mvn tomcat:redeploy
	
##Directories
	
The Deer-Service webapp folder should have the following subdirectories:

	/config
	/result
	/examples

##HTTP Calls

###Running DEER

####Load a configuration file

POST a params array to /Loadfile

	params: {
					configFile : name of config file,
					dataFile: name of data file
				 	}
				 	
This will save a config.tsv file to webapp/config and return the settings from the file.

This class will read the files from the GeoKnow Generator (https://github.com/GeoKnow/GeoKnowGeneratorUI) upload 
folder, which should be in the same Tomcat server:

	/webapps/generator/uploads
	
				 	
####Start the enrichment process

POST a params array to /run

	params[0] = number of configuration settings for Geolift (sum of params [3...*])
	params[1] = name of file / URI to process
	params[2] = 0 (file job) / 1 (URI job)
	params[3...*] = parameters, eg: "1 nlp useFoxLight true"
	
The enriched data is saved to webapps/result/result.ttl. To run this class without the GeoKnow Generator, set params[1] = name of file, params[2] = 0, and put the file from params[1] in the examples folder.
	
####Open the output from the enrichment process

POST a params array to /review

No parameters necessary, the class will automatically open the file in the webapp/results folder created by run and return the model.
	
####Save the output to a SPARQL endpoint
	
POST a params array to /ImportRDF

	var params = {
	    rdfFile: "result.ttl", 
	    endpoint: the endpoint where the model is to be saved, 
	    graph: the graph within the endpoint that should be saved to, 
	    uriBase : the base URI for your program / data 
	 	};
	

##Licence

The source code of this repo is published under the Apache License Version 2.0
