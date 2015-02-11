package org.linkeddata.deer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.aksw.deer.workflow.Deer;
import org.apache.log4j.Logger;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.RDFReader;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.update.UpdateAction;
import com.ibm.icu.util.Calendar;

public class DeerImpl {

    private static final Logger log = Logger.getLogger(DeerImpl.class);

    private String configFile;
    private String outputFile;
    private String outputFormat;
    private String toEndpoint;
    private String toGraph;

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

    public DeerImpl(String file) throws IOException {

        Model model = ModelFactory.createDefaultModel();
        File f = new File(file);

        RDFReader rdf = model.getReader();
        rdf.read(model, f.toURI().toURL().toString());

        String path = f.getAbsolutePath().substring(0,
                f.getAbsolutePath().lastIndexOf(File.separator) + 1);

        log.debug(path);

        Property outputFileProperty = model
                .getProperty("http://geoknow.org/specsontology/outputFile");
        Property outputFormatProperty = model
                .getProperty("http://geoknow.org/specsontology/outputFormat");
        Property toEndpointProperty = model
                .getProperty("http://geoknow.org/specsontology/toEndpoint");
        Property toGraphProperty = model.getProperty("http://geoknow.org/specsontology/toGraph");

        StmtIterator stmtIt = model.listStatements(null, outputFileProperty, (RDFNode) null);
        if (stmtIt.hasNext()) {
            Statement st = stmtIt.next();
            // add a path to the output file
            outputFile = path + st.getObject().asLiteral().getString();
            log.debug(outputFile);

            // update the triple
            String rename = "" + "delete { <" + st.getSubject().getURI() + ">  <"
                    + outputFileProperty.getURI() + "> ?file }\n" + "insert { <"
                    + st.getSubject().getURI() + ">  <" + outputFileProperty.getURI() + "> \""
                    + outputFile + "\" }\n" + "where { <" + st.getSubject().getURI() + ">  <"
                    + outputFileProperty.getURI() + "> ?file }";
            log.debug(rename);
            UpdateAction.parseExecute(rename, model);
        }

        stmtIt = model.listStatements(null, outputFormatProperty, (RDFNode) null);
        if (stmtIt.hasNext()) {
            Statement st = stmtIt.next();
            outputFormat = st.getObject().asLiteral().getString();
        }

        stmtIt = model.listStatements(null, toEndpointProperty, (RDFNode) null);
        if (stmtIt.hasNext()) {
            Statement st = stmtIt.next();
            toEndpoint = st.getObject().asResource().getURI();
        }

        stmtIt = model.listStatements(null, toGraphProperty, (RDFNode) null);
        if (stmtIt.hasNext()) {
            Statement st = stmtIt.next();
            toGraph = st.getObject().asResource().getURI();
        }

        this.configFile = path + Calendar.getInstance().getTimeInMillis() + "_" + f.getName();
        log.debug("writing a new configuration with system paths:" + this.configFile);
        OutputStream out = new FileOutputStream(new File(this.configFile));
        model.write(out, "TTL");

        log.debug("outputFile: " + outputFile);
        log.debug("outputFormat: " + outputFormat);
        log.debug("toEndpoint: " + toEndpoint);
        log.debug("toGraph: " + toGraph);

    }

    public void execute() throws IOException {

        log.debug("executing " + configFile);
        log.debug("outputFile " + outputFile);

        String[] args = { configFile };
        for (int i = 0; i < args.length; i++) {
            System.out.println(args[i]);
        }

        Deer.main(args);

    }
}
