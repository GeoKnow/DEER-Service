import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.aksw.geolift.workflow.GeoLift;

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
	      configFile = filePath+"config/config.tsv";
	      outputFile = filePath+"result/result.ttl";
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
    	int length = (Integer.parseInt(strlen)+2);
    	String [] params = new String[length];
    	
    	for(int i=2; i<length; i++){
	    	params[i-2] = request.getParameter(Integer.toString(i));
    	}
    	
    	String input = request.getParameter("1");
    	
    	if(request.getParameter("isCompletePath") == "0"){
    		args[1] = filePath+"examples"+File.separator+input;
    	}else{
    		args[1] = input;
    	}
    	
    	try {
 
			File file = new File(configFile);
			
			if (!file.exists()) {
				file.createNewFile();
			}
 
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			
			for(int i=0; i<length-2; i++){
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
 		//System.out.println(args[1]);
 		GeoLift.main(args);
 	}

}
