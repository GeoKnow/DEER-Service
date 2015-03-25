package org.linkeddata.deer;

import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Test;

public class DeerTest {

  private String config = "config.ttl";
  private String outputFile = "enrichedBerlin.ttl";
  private String temp_config = "";

  private String endpoint = "http://192.168.2.17:8890/sparql";
  private String graph = "http://example.com/test-rev";

  @Test
  public void loadConfig() throws Exception {
    ClassLoader classLoader = getClass().getClassLoader();
    File file = new File(classLoader.getResource(config).getFile());
    String filePath =
        file.getAbsolutePath().substring(0, file.getAbsolutePath().lastIndexOf(File.separator));
    try {

      DeerImpl deer = new DeerImpl(FileUtils.readFileToString(file, "UTF-8"), filePath);
      assertTrue(deer.getOutputFile().endsWith(outputFile));

      // to test that I can save to an endpoint
      deer.setToEndpoint(endpoint);
      deer.setToGraph(graph);

      deer.execute();
      outputFile = deer.getOutputFile();
      File resfile = new File(outputFile);
      assertNotSame(0, resfile.length());
      temp_config = deer.getConfigFile();

      // to test that I can save to an endpoint
      DeerImpl.saveResults(deer.getOutputFile(), deer.getToEndpoint(), deer.getToGraph());

    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  @After
  public void deleteResults() {
    (new File(outputFile)).delete();
    (new File(temp_config)).delete();
    (new File(config)).delete();

  }

}
