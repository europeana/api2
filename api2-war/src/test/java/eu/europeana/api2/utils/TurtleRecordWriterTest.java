package eu.europeana.api2.utils;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;

public class TurtleRecordWriterTest {

    private static final String RDF_INPUT_TEST = "photography_ProvidedCHO_TopFoto_co_uk_EU017407.rdf";

    @Test
    public void turtleWriterTest() throws IOException {
        InputStream rdfInput = TurtleRecordWriter.class.getClassLoader().getResourceAsStream(RDF_INPUT_TEST);
        Model modelResult = ModelFactory.createDefaultModel().read(rdfInput, "", "RDF/XML");
        OutputStream outputStream = new ByteArrayOutputStream();
        TurtleRecordWriter writer= new TurtleRecordWriter(outputStream);
        writer.write(modelResult);
        //check if null
        Assert.assertNotNull(outputStream);
        //close the streams
        outputStream.close();
        rdfInput.close();
    }
}
