package org.linkeddata.deer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Test;

public class DeerTest {

    private String config = "config.ttl";
    private String outputFile = "enrichedBerlin.ttl";
    private String temp_config = "";

    @Test
    public void loadConfig() {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(config).getFile());

        try {

            DeerImpl deer = new DeerImpl(file.getAbsolutePath());
            assertEquals(outputFile, deer.getOutputFile().substring(
                    deer.getOutputFile().lastIndexOf(File.separator) + 1,
                    deer.getOutputFile().length()));

            deer.execute();
            outputFile = deer.getOutputFile();
            File resfile = new File(outputFile);
            assertNotSame(0, resfile.length());
            temp_config = deer.getConfigFile();

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
