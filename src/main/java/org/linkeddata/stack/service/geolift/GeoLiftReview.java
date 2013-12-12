package org.linkeddata.stack.service.geolift;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFReader;
import com.google.gson.Gson;

/**
 * Servlet implementation class TripleGeoReview
 */
public class GeoLiftReview extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private static String filePath;
	
	// Options for writing the config file
	static String configFile;
	static String configTemplate;
	static String outputFormat;
	static String execType;
	   

	   public void init( ){
		   filePath = getServletContext().getRealPath(File.separator);
	   }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	
    	 Model mod0 = ModelFactory.createDefaultModel();

	   	 RDFReader r0 = mod0.getReader( "N3" );
	   	 r0.read( mod0, "file:///"+filePath+"result/result.ttl" );
	   	 
	   	 String[] modArray = new String[2];
	   	 
	   	 ByteArrayOutputStream os = new ByteArrayOutputStream();
	   	 mod0.write(os);
	   	 modArray[0] = os.toString("UTF-8");
	     
	     Gson gson = new Gson();
	     String json = gson.toJson(modArray);
	     response.setContentType("application/json");
	     response.setCharacterEncoding("UTF-8");
	     response.getWriter().write(json);
    	}
}
