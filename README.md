#GeoLift-Service

A web service for GeoLift

##Building

To deploy the service to your Tomcat server run:

	mvn tomcat:deploy
	
or to redeploy:

	mvn tomcat:redeploy
	
##Directories
	
The Geolift-Service webapp folder should have the following subdirectories:

	\config
	\result
	\examples

##HTTP Calls

###Running Geolift

####Load a configuration file

POST a params array to /Loadfile

	params: {
					configFile : name of config file,
					dataFile: name of data file
				 	}
				 	
This will save a config.tsv file to webapps/config and return the settings from the file.

This class will read the files from the GeoKnow Generator (https://github.com/GeoKnow/GeoKnowGeneratorUI) upload 
folder, which should be in the same Tomcat server:

	\webapps\generator\uploads
	
If a different behaviour is desired this class must be edited.
				 	
####Start the enrichment process

POST a params array to /GeoLiftRun

	params[0] = number of configuration settings for Geolift (sum of params [3...*])
	params[1] = name of file / URI to process
	params[2] = 0 : file / 1 : URI
	params[3...*] = parameters, eg: "1 nlp useFoxLight true"
	
The enriched data is saved to webapps/result/result.ttl.
	
####Open the output from the enrichment process

POST a params array to /GeoLiftReview

	No parameters necessary
	
Opens the webapps/result/result.ttl file and returns the model.
	
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
