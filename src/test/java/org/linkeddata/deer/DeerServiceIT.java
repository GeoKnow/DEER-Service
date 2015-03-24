package org.linkeddata.deer;

import static com.jayway.restassured.RestAssured.given;

import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

import com.jayway.restassured.RestAssured;

public class DeerServiceIT {

  private static final Logger log = Logger.getLogger(DeerServiceIT.class);

  private String configToFile = "berlin_to_file.ttl";
  private String configToEndpoint = "berlin_to_endpoint.ttl";

  @BeforeClass
  public static void init() {
    RestAssured.baseURI = "http://localhost";
    RestAssured.port = 8080;
    // TODO: find a way to parametrise this basePath
    RestAssured.basePath = "/deer-service";
  }

  public void testGet() {
    log.info(given().when().get().then().extract().body());

  }


  @Test
  public void testPost() throws Exception {

    // ClassLoader classLoader = getClass().getClassLoader();
    // File file = new File(classLoader.getResource(configToFile).getFile());
    // String config = FileUtils.readFileToString(file, "UTF-8");
    // log.info(config);
    // // creates a job
    // log.info("post deer config: " + file.getName());
    // log.info(given().header("Content-Type", "text/turtle").body(config).when().post().then()
    // .extract().body().asString());
    //
    //

  }
}
