package org.linkeddata.stack.service.geolift;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.aksw.geolift.io.Reader;
import org.aksw.geolift.workflow.GeoLift;
import org.aksw.geolift.workflow.TSVConfigReader;

import com.google.common.collect.Multimap;
import com.hp.hpl.jena.rdf.model.Model;

/**
 * Servlet implementation class TripleGeoRun
 */
public class GeoLiftRun extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private static String filePath;
	static String[]args = new String[6];
	
	// Options for writing the config file
	static String configFile;
	static String outputFile;

	public void init( ){
		  filePath = getServletContext().getRealPath(File.separator);
	      configFile = filePath+"config"+File.separator+"config.tsv";
	      outputFile = filePath+"result"+File.separator+"result.ttl";
	      args[0] = "-i";
	  	  args[2] = "-c";
	      args[3] = configFile;
	      args[4] = "-o";
	      args[5] = outputFile;
	   }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		out.println("<HTML><HEAD><TITLE>Hello World!</TITLE>"
		+ "</HEAD><BODY>Hello World!!!</BODY></HTML>");
	}
	

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	
    	String strlen = request.getParameter("0");
    	int length = (Integer.parseInt(strlen)+3);
    	
    	String [] params = new String[length];
    	
    	for(int i=3; i<length; i++){
	    	params[i-3] = request.getParameter(Integer.toString(i));
	    	//System.out.println(params[i-3]);
    	}
    	
    	String input = request.getParameter("1");
    	//System.out.println(input);
    	int isCompletePath = Integer.parseInt(request.getParameter("2"));
    	
    	if(isCompletePath == 0){
    		args[1] = filePath+"examples"+File.separator+input;
    	}
    	if(isCompletePath == 1){
    		args[1] = input;
    	}
    	if(isCompletePath == 2){
    		String uploadFilePath = filePath.replace("GeoLift-Service"+File.separator, "");
    		args[1] = uploadFilePath+"generator"+File.separator+"uploads"+File.separator+input;
    	}
    	
    	try {
 
			File file = new File(configFile);
			
			if (!file.exists()) {
				file.createNewFile();
			}
 
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			
			for(int i=0; i<length-3; i++){
		    		String line = params[i];
		    		bw.write(line);
		        	bw.newLine();
		    }
			bw.close();
 
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
    	ExecuteGeoLift(args);
    	response.getWriter().write("Finished!");
    }
    
    // Start GeoLift with the configfile
 	public static void ExecuteGeoLift(String args[]) throws IOException{
 		for (int i=0;i<args.length;i++){
 		System.out.println(args[i]);
 		}
 		System.out.println("java -jar \""+filePath+"WEB-INF"+File.separator+
 				"lib"+File.separator+"geolift-0.3.jar\" -i "+args[1]+" -c "+args[3]+" -o "
 				+args[5]);
 		Process proc = Runtime.getRuntime().exec("java -jar "+filePath+"WEB-INF"+File.separator+
 				"lib"+File.separator+"geolift-0.3.jar -i "+args[1]+" -c "+args[3]+" -o "
 				+args[5]);
	 	InputStream in = proc.getInputStream();
	 	InputStream err = proc.getErrorStream();
	 	String line;
	 	BufferedReader input = new BufferedReader(new InputStreamReader(in));
	 	  while ((line = input.readLine()) != null) {
	 	    System.out.println(line);
	 	  }
	 	input = new BufferedReader(new InputStreamReader(err));
	 	  while ((line = input.readLine()) != null) {
	 	    System.out.println(line);
	 	  }
	 	input.close();
 		//GeoLift.main(args);
 	}

}
