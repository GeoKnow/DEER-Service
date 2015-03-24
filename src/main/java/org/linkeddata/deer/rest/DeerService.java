package org.linkeddata.deer.rest;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.log4j.Logger;
import org.linkeddata.deer.DeerImpl;

import com.google.gson.Gson;
import com.hp.hpl.jena.rdf.model.ResourceRequiredException;

/**
 * A REST services for Linked Data Enrichment using DEER.
 * 
 * @author alejandragarciarojas
 *
 */
@Path("")
public class DeerService {

  private static final Logger log = Logger.getLogger(DeerService.class);

  @Context
  ServletContext context;

  /**
   * Returns the working directory where config and output files are saved
   * 
   * @param context
   * @return path
   */
  private File getWorkingDir(@Context ServletContext context) {

    String workingPath = context.getInitParameter("workingDir");
    File resultDir = new File(workingPath);
    if (!resultDir.exists()) {
      log.warn("working directory " + workingPath + " doesnt exist, attempt to create it... ");
      resultDir.mkdirs();
    }
    return resultDir;
  }

  /**
   * Provides a list of all configuration files processed by the service.
   * 
   * @param context
   * @return a JSON array of UUIDs
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response getAllConfigurations(@Context ServletContext context) {

    File workingDir = getWorkingDir(context);

    Collection<File> fileIterator =
        FileUtils
            .listFiles(workingDir, new WildcardFileFilter("*_config.ttl"), TrueFileFilter.TRUE);
    List<String> results = new ArrayList<String>();
    for (File f : fileIterator)
      results.add(f.getName().replace("_config.ttl", ""));

    Gson gson = new Gson();
    String json = gson.toJson(results);

    return Response.ok().entity(json).header("Access-Control-Allow-Origin", "*")
        .header("Access-Control-Allow-Methods", "GET").build();
  }

  /**
   * Provides the content of a specific configuration file reading the uuid.json file produced with
   * the results
   * 
   * @param uuid
   * @param context
   * @return JSON object with the configuration parameters
   */
  @GET
  @Path("{uuid}")
  @Produces("text/turtle")
  public Response getConfiguration(@PathParam("uuid") String uuid, @Context ServletContext context) {

    String workingPath = getWorkingDir(context).getAbsolutePath();

    String configFile = workingPath + File.separator + uuid + "_config.ttl";
    log.debug(configFile);
    try {

      String config = FileUtils.readFileToString(new File(configFile), "UTF-8");
      return Response.ok().header("Access-Control-Allow-Origin", "*")
          .header("Access-Control-Allow-Methods", "GET").type("text/turtle").entity(config).build();

    } catch (IOException e) {
      e.printStackTrace();
      return Response.status(Response.Status.NOT_FOUND).header("Access-Control-Allow-Origin", "*")
          .header("Access-Control-Allow-Methods", "GET").build();
    }

  }

  /**
   * Submit a configuration script in and execute Deer.
   * 
   * @param configuration string in text/turtle format
   * @return Response wit a JSON object with the output results
   */
  @POST
  @Consumes("text/turtle")
  @Produces(MediaType.APPLICATION_JSON)
  public Response runFromText(String configuration) {

    File resultDir = getWorkingDir(context);
    try {

      DeerImpl deer = new DeerImpl(configuration, resultDir.getAbsolutePath());
      deer.execute();

      Map<String, String> map = new HashMap<String, String>();

      map.put("outputFile", deer.getOutputFile());
      map.put("withId", deer.getWithId());

      // insert results to an endpoint
      if (!deer.getToEndpoint().equals("")) {
        try {
          if (deer.saveResults()) {
            // give back endpoint and graph if results were saved.
            map.put("outputEndpoint", deer.getToEndpoint());
            map.put("outputGraph", deer.getToGraph());
            // delete the file
            (new File(deer.getOutputFile())).delete();
          }
        } catch (Exception e) {
          // TODO Auto-generated catch block
          log.error(e);
          e.printStackTrace();
          return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
              .type(MediaType.APPLICATION_JSON).entity(e.getMessage())
              .header("Access-Control-Allow-Origin", "*")
              .header("Access-Control-Allow-Methods", "POST").build();
        }

      } else
        map.put("outputFile", deer.getOutputFile());

      // output resulting file/endpoint/graph
      Gson gson = new Gson();
      String json = gson.toJson(map);

      return Response.ok().header("Access-Control-Allow-Origin", "*")
          .header("Access-Control-Allow-Methods", "POST").type(MediaType.APPLICATION_JSON)
          .entity(json).build();

    } catch (ResourceRequiredException e) {
      log.error(e);
      return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage())
          .header("Access-Control-Allow-Origin", "*")
          .header("Access-Control-Allow-Methods", "POST").build();
    } catch (IOException e) {
      log.error(e);
      e.printStackTrace();
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage())
          .header("Access-Control-Allow-Origin", "*")
          .header("Access-Control-Allow-Methods", "POST").build();
    }

  }

}
