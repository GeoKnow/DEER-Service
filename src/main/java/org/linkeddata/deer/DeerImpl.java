package org.linkeddata.deer;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.aksw.deer.workflow.Deer;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.log4j.Logger;
import org.linkeddata.utils.QueryChunks;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.RDFReader;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceRequiredException;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.update.UpdateAction;

/**
 * Implementation of a class to execute Deer.
 * 
 * @author alejandragarciarojas
 *
 */
public class DeerImpl {

  private static final Logger log = Logger.getLogger(DeerImpl.class);

  private String configFile;
  private String outputFile;
  private String outputFormat;
  private String toEndpoint;
  private String toGraph;
  private String withId;

  private static String uriBase = "http://geoknow.org/specsontology/";

  public String getConfigFile() {
    return configFile;
  }

  public String getOutputFile() {
    return outputFile;
  }

  public String getOutputFormat() {
    return outputFormat;
  }

  public String getToEndpoint() {
    return toEndpoint;
  }

  public String getToGraph() {
    return toGraph;
  }

  public void setToEndpoint(String toEndpoint) {
    this.toEndpoint = toEndpoint;
  }

  public void setToGraph(String toGraph) {
    this.toGraph = toGraph;
  }

  public String getWithId() {
    return withId;
  }

  public void setWithId(String withId) {
    this.withId = withId;
  }

  /**
   * Initialize a Deer object. Given the configuration in turtle format, it will adapt the output
   * accordingly and will provide an ID if required. Important properties are:
   * 
   * <pre>
   *  <http://geoknow.org/specsontology/withId>
   *  <http://geoknow.org/specsontology/outputFile>
   *  <http://geoknow.org/specsontology/outputFormat>
   *  <http://geoknow.org/specsontology/toEndpoint>
   *  <http://geoknow.org/specsontology/toGraph>
   * </pre>
   * 
   * @param configuration
   * @param pathResults
   * @throws IOException
   */
  public DeerImpl(String configuration, String pathResults) throws IOException,
      ResourceRequiredException {

    log.debug("path: " + pathResults);

    Model model = ModelFactory.createDefaultModel();
    model.read(new ByteArrayInputStream(configuration.getBytes()), null, "TURTLE");

    // Initialize properties
    Property withIdProperty = model.getProperty("http://geoknow.org/specsontology/withId");
    Property outputFileProperty = model.getProperty("http://geoknow.org/specsontology/outputFile");
    Property outputFormatProperty =
        model.getProperty("http://geoknow.org/specsontology/outputFormat");
    Property toEndpointProperty = model.getProperty("http://geoknow.org/specsontology/toEndpoint");
    Property toGraphProperty = model.getProperty("http://geoknow.org/specsontology/toGraph");

    String path = pathResults + File.separator;

    // verify if the configurations provides an ID
    StmtIterator stmtIt = model.listStatements(null, withIdProperty, (RDFNode) null);
    if (stmtIt.hasNext()) {
      Statement st = stmtIt.next();
      withId = st.getObject().asLiteral().getString();
    } else
      withId = UUID.randomUUID().toString();

    // add a path to the output file
    Resource outPutDataset = null;
    stmtIt = model.listStatements(null, outputFileProperty, (RDFNode) null);
    if (stmtIt.hasNext()) {
      Statement st = stmtIt.next();
      outPutDataset = st.getSubject();
      outputFile = path + withId + "_" + st.getObject().asLiteral().getString();
      log.debug(outputFile);

      // update the triple
      String rename =
          "delete { <" + st.getSubject().getURI() + ">  <" + outputFileProperty.getURI()
              + "> ?file }\n" + "insert { <" + st.getSubject().getURI() + ">  <"
              + outputFileProperty.getURI() + "> \"" + outputFile + "\" }\n" + "where { <"
              + st.getSubject().getURI() + ">  <" + outputFileProperty.getURI() + "> ?file }";
      log.debug(rename);
      UpdateAction.parseExecute(rename, model);
    } else
      outputFile = "";

    // Verify the rest of the properties
    stmtIt = model.listStatements(null, outputFormatProperty, (RDFNode) null);
    if (stmtIt.hasNext()) {
      Statement st = stmtIt.next();
      outputFormat = st.getObject().asLiteral().getString();
    } else
      outputFormat = "";

    stmtIt = model.listStatements(null, toEndpointProperty, (RDFNode) null);
    if (stmtIt.hasNext()) {
      Statement st = stmtIt.next();
      toEndpoint = st.getObject().asResource().getURI();
      if (outPutDataset == null)
        outPutDataset = st.getSubject();
    } else
      toEndpoint = "";

    stmtIt = model.listStatements(null, toGraphProperty, (RDFNode) null);
    if (stmtIt.hasNext()) {
      Statement st = stmtIt.next();
      toGraph = st.getObject().asResource().getURI();
    } else
      toGraph = "";

    // validate if no output configuration has being provided
    if (outputFile.equals("") && outPutDataset != null) {
      outputFile = path + withId + "_output.ttl";
      model.add(outPutDataset, outputFileProperty, model.createLiteral(outputFile));
      model.add(outPutDataset, outputFormatProperty, model.createLiteral("Turtle"));
    }

    this.configFile = path + withId + "_config.ttl";
    log.info("writing a new configuration with system paths:" + this.configFile);
    OutputStream out = new FileOutputStream(new File(this.configFile));
    model.write(out, "TURTLE");

  }

  /**
   * Executes a Deer process
   * 
   * @throws IOException
   */
  public void execute() throws IOException {

    log.debug("executing " + configFile);
    log.info("outputFile: " + outputFile);
    log.info("outputFormat: " + outputFormat);
    log.info("toEndpoint: " + toEndpoint);
    log.info("toGraph: " + toGraph);

    String[] args = {configFile};
    for (int i = 0; i < args.length; i++) {
      System.out.println(args[i]);
    }

    Deer.main(args);

  }

  /**
   * Insert results into a Endpoint/graph
   * 
   * @param resutlFile output file from the DEER process
   * @param endpoint where results should be saved
   * @param graph where results should be saved
   * @return false if no results were saved, and true if results were saved
   * @throws Exception
   */
  public boolean saveResults() throws Exception {

    log.debug("Saving toEndpoint: " + toEndpoint);
    log.debug("Saving toGraph: " + toGraph);

    Model model = ModelFactory.createDefaultModel();
    RDFReader reader = model.getReader();
    File file = new File(outputFile);

    log.debug("reading " + file.getAbsolutePath());
    reader.read(model, file.toURI().toURL().toString());

    if (model.isEmpty() == true) {
      log.info("empty model, nothing to save ");
      return false;
    }

    QueryChunks.setLinesLimit(50);
    List<String> insertqueries = QueryChunks.generateInsertChunks(this.toGraph, model, uriBase);
    Iterator<String> it = insertqueries.iterator();

    CloseableHttpClient httpClient = HttpClients.createDefault();

    while (it.hasNext()) {
      String q = it.next();


      HttpPost proxyMethod = new HttpPost(this.toEndpoint);
      proxyMethod.addHeader(HTTP.CONTENT_TYPE, "charset=UTF-8");
      ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
      postParameters.add(new BasicNameValuePair("query", q));
      postParameters.add(new BasicNameValuePair("format", "application/sparql-results+json"));
      proxyMethod.setEntity(new UrlEncodedFormEntity(postParameters));

      log.debug("connecting to " + this.toEndpoint);
      final CloseableHttpResponse response = httpClient.execute(proxyMethod);

      BufferedReader in =
          new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
      String inputLine, result = "";
      while ((inputLine = in.readLine()) != null) {
        result += inputLine;
      }
      in.close();

      if (response.getStatusLine().getStatusCode() != 200) {
        throw new IOException("Could not insert data: " + toEndpoint + "\n "
            + result.substring(0, 500) + "\n query: \n" + q);
      }

    }
    httpClient.close();
    return true;
  }


}
