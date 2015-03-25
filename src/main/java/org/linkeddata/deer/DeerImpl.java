package org.linkeddata.deer;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.aksw.deer.workflow.Deer;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.log4j.Logger;
import org.linkeddata.utils.QueryChunks;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
  public static int saveResults(String resFile, String endpoint, String graph) throws Exception {

    Model model = ModelFactory.createDefaultModel();
    RDFReader reader = model.getReader();
    File file = new File(resFile);

    log.info("outputFile: " + resFile);
    log.info("toEndpoint: " + endpoint);
    log.info("toGraph: " + graph);

    reader.read(model, file.toURI().toURL().toString());

    int triples = model.listStatements().toList().size();
    if (model.isEmpty() == true) {
      log.info("empty model, nothing to save ");
    }

    List<String> insertqueries = QueryChunks.generateInsertChunks(graph, model, uriBase);
    Iterator<String> it = insertqueries.iterator();

    // CloseableHttpClient httpClient = HttpClients.createDefault();
    DefaultHttpClient httpClient = new DefaultHttpClient();

    while (it.hasNext()) {
      String q = it.next();

      HttpPost httpPost = new HttpPost(endpoint);
      httpPost.addHeader(HTTP.CONTENT_TYPE, "application/x-www-form-urlencoded;charset=UTF-8");
      ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
      postParameters.add(new BasicNameValuePair("query", q));
      postParameters.add(new BasicNameValuePair("format", "application/sparql-results+json"));
      httpPost.setEntity(new UrlEncodedFormEntity(postParameters, "UTF-8"));

      log.debug("connecting to " + endpoint);
      // final CloseableHttpResponse response = httpClient.execute(proxyMethod);
      HttpResponse response = httpClient.execute(httpPost);
      String result = IOUtils.toString(response.getEntity().getContent(), "UTF-8");
      log.debug(result);
      if (response.getStatusLine().getStatusCode() != 200) {
        throw new IOException("Could not insert data: " + endpoint + "\n query: \n" + q);
      }

      // verify response
      ObjectMapper mapper = new ObjectMapper();
      JsonNode rootNode = mapper.readTree(result);
      Iterator<JsonNode> bindingsIter = rootNode.path("results").path("bindings").elements();
      if (bindingsIter.hasNext()) {
        JsonNode bindingNode = bindingsIter.next();
        if (!bindingNode.get("callret-0").path("value").textValue().contains("done")) {
          throw new IOException("Could not insert data: " + endpoint + "\n query: \n" + q);
        }
      }

      httpPost.releaseConnection();
    }
    // httpClient.close();
    return triples;
  }

}
