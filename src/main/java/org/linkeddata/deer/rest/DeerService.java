package org.linkeddata.deer.rest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;
import org.linkeddata.deer.DeerImpl;
import org.linkeddata.utils.QueryChunks;

import com.google.gson.Gson;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFReader;
import com.ibm.icu.util.Calendar;

@Path("")
public class DeerService {

    private static final Logger log = Logger.getLogger(DeerService.class);

    private static String uriBase = "http://geoknow.org/specsontology/";
    @Context
    ServletContext context;

    @GET
    public Response sayHello() {
        return Response.ok("hello", MediaType.TEXT_PLAIN)
                .header("Access-Control-Allow-Origin", "*").header("Access-Control-Allow-Methods",
                        "GET").build();
    }

    @POST
    @Consumes("text/turtle")
    @Produces(MediaType.APPLICATION_JSON)
    public Response runFromText(String configuration) {

        String filePath = context.getRealPath(File.separator);
        log.info("context directory: " + filePath);
        String workingPath = filePath + "results";

        File resultDir = new File(workingPath);
        if (!resultDir.exists()) {
            resultDir.mkdirs();
        }

        try {

            // save the configuration file in the working path
            String filename = Calendar.getInstance().getTimeInMillis() + ".ttl";
            String uploadedFileLocation = workingPath + filename;

            InputStream in = IOUtils.toInputStream(configuration, "UTF-8");
            writeToFile(in, uploadedFileLocation);

            DeerImpl deer = new DeerImpl(uploadedFileLocation);
            deer.execute();

            Map<String, String> map = new HashMap<String, String>();
            // upload the file to a endpoint
            if (!deer.getToEndpoint().equals("")) {

                try {
                    if (saveResults(deer.getOutputFile(), deer.getToEndpoint(), deer.getToGraph())) {
                        // give back endpoint and graph if results were saved.
                        map.put("outputEndpoint", deer.getToEndpoint());
                        map.put("outputGraph", deer.getToGraph());
                        // delete the file
                        (new File(deer.getOutputFile())).delete();
                    } else
                        map.put("outputFile", deer.getOutputFile());

                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    log.error(e);
                    e.printStackTrace();
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(
                            MediaType.APPLICATION_JSON).entity(e.getMessage()).header(
                            "Access-Control-Allow-Origin", "*").header(
                            "Access-Control-Allow-Methods", "POST").build();
                }

            } else
                map.put("outputFile", deer.getOutputFile());

            // delete temporal configuration files
            (new File(uploadedFileLocation)).delete();
            (new File(deer.getConfigFile())).delete();

            // output resulting file/endpoint/graph
            Gson gson = new Gson();
            String json = gson.toJson(map);

            return Response.ok().header("Access-Control-Allow-Origin", "*").header(
                    "Access-Control-Allow-Methods", "POST").entity(json).build();

        } catch (IOException e) {
            log.error(e);
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(
                    MediaType.APPLICATION_JSON).entity(e.getMessage()).header(
                    "Access-Control-Allow-Origin", "*").header("Access-Control-Allow-Methods",
                    "POST").build();
        }

    }

    /**
     * Insert results into a Endpoint/graph
     * 
     * @param file
     * @param endpoint
     * @param graph
     * @param uriBase
     * @return false if no results were saved, and true if results were saved
     * @throws Exception
     */
    private static boolean saveResults(String fileName, String endpoint, String graph)
            throws Exception {

        Model model = ModelFactory.createDefaultModel();
        RDFReader reader = model.getReader();
        File file = new File(fileName);

        log.debug("reading " + file.toURI().toURL().toString());
        reader.read(model, file.toURI().toURL().toString());

        if (model.isEmpty() == true) {
            log.info("empty model, nothing to save ");
            return false;
        }

        List<String> insertqueries = QueryChunks.generateInsertChunks(graph, model, uriBase);
        Iterator<String> it = insertqueries.iterator();

        ClassLoader classLoader = HttpClients.class.getClassLoader();
        URL resource = classLoader.getResource("org/apache/http/message/BasicLineFormatter.class");

        System.out.println(resource);

        CloseableHttpClient httpClient = HttpClients.createDefault();

        while (it.hasNext()) {
            String q = it.next();

            HttpPost proxyMethod = new HttpPost(endpoint);
            ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
            postParameters.add(new BasicNameValuePair("query", q));
            postParameters.add(new BasicNameValuePair("format", "application/sparql-results+json"));
            proxyMethod.setEntity(new UrlEncodedFormEntity(postParameters));

            log.debug("connecting to " + endpoint);
            final CloseableHttpResponse response = httpClient.execute(proxyMethod);

            BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity()
                    .getContent()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                log.debug(inputLine);
            }
            in.close();

        }
        httpClient.close();
        return true;
    }

    // save file to new location
    private void writeToFile(InputStream uploadedInputStream, String uploadedFileLocation) {

        try {
            OutputStream out = new FileOutputStream(new File(uploadedFileLocation));
            int read = 0;
            byte[] bytes = new byte[1024];

            out = new FileOutputStream(new File(uploadedFileLocation));
            while ((read = uploadedInputStream.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }
            out.flush();
            out.close();
        } catch (IOException e) {

            e.printStackTrace();
        }

    }

}
