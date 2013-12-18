package org.linkeddata.stack.service.geolift;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//import org.aksw.geolift.workflow.*;


import com.google.gson.Gson;

/**
 * Servlet implementation class LoadFile
 */
public class LoadFile extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	
	// Options for writing the config file
	static String configFile;
	static String[] dataFile = new String[1];
	static String configTemplate;
	static String outputFormat;
	static String execType;
	ArrayList<String> configList = new ArrayList<String>();
	ArrayList<String[]> config = new ArrayList<String[]>();

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
    @SuppressWarnings("static-access")
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	 String filePath = getServletContext().getRealPath(File.separator);
    	 filePath = filePath.replace("GeoLift-Service"+File.separator, "");
    	 configFile = filePath+"generator"+File.separator+"uploads"+File.separator+request.getParameter("configFile");
    	 //configFile = configFile.replace("\\", "/");
    	 dataFile[0] = filePath+"generator"+File.separator+"uploads"+File.separator+request.getParameter("dataFile");
    	 //dataFile[0] = dataFile[0].replace("\\", "/");
    	 configList.clear();
    	 config.clear();
    	 config.add(dataFile);
    	 readConfig(configFile);
    	 
    	 Gson gson = new Gson();
	     String json = gson.toJson(config);
	     response.setContentType("application/json");
	     response.setCharacterEncoding("UTF-8");
	     response.getWriter().write(json);
    	}
    
    private void readConfig(String configFile){
    	
    	BufferedReader br = null;
    	 
		try {
 
			String sCurrentLine;
 
			br = new BufferedReader(new FileReader(configFile));
 
			while ((sCurrentLine = br.readLine()) != null) {
				if ( sCurrentLine.trim().length() != 0 ) {  
						configList.add(sCurrentLine);
				}
			}
			
			for(int i=0; i<configList.size(); i++){
					String[]line = new String[2];
					String[]parts = configList.get(i).split(" ");
					config.add(parts);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
    	 
		}
    }
    
}
