package eu.europeana.api2.utils;

import eu.europeana.api.commons.utils.TurtleRecordWriter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RiotException;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;

public class TurtleRecordWriterTest {

    private static final String RDF_INPUT_TEST = "photography_ProvidedCHO_TopFoto_co_uk_EU017407.rdf";
    private static final String TTL_INPUT_TEST = "photography_ProvidedCHO_TopFoto_co_uk_EU017407.txt";
    private static final String WRONG_TTL_INPUT_TEST = "WrongTurtleInput.txt";

    // test if generated turtle format by TurtleRecordWriter is valid
    @Test
    public void turtleWriterTest() throws IOException {
        InputStream rdfInput = TurtleRecordWriterTest.class.getClassLoader().getResourceAsStream(RDF_INPUT_TEST);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        TurtleRecordWriter writer = new TurtleRecordWriter(outputStream);
        Model modelResult = ModelFactory.createDefaultModel().read(rdfInput, "", "RDF/XML");
        writer.write(modelResult);
        rdfInput.close();
        writer.close();
        //check if null
        Assert.assertNotNull(outputStream);
        ByteArrayInputStream inStream = new ByteArrayInputStream(outputStream.toByteArray());
        //check with jena
        Model jenaModel = ModelFactory.createDefaultModel().read(inStream, "", "Turtle");
        Assert.assertTrue("Turtle Format is invalid", ! jenaModel.isEmpty());
        //close the stream
        outputStream.close();
        inStream.close();
    }

    // to test valid turtle format
    @Test
    public void testValidTurtleOutput() throws IOException {
        InputStream rdfInput = TurtleRecordWriterTest.class.getClassLoader().getResourceAsStream(TTL_INPUT_TEST);
        Assert.assertNotNull(rdfInput);
        Model jenaModel = ModelFactory.createDefaultModel().read(rdfInput, "", "Turtle");
        Assert.assertTrue("Turtle Format is invalid", ! jenaModel.isEmpty());
        rdfInput.close();
    }

    // to test the invalid turtle format. Jena should throw error
    @Test
    public void testInValidTurtleOutput() throws IOException {
        InputStream rdfInput = TurtleRecordWriterTest.class.getClassLoader().getResourceAsStream(WRONG_TTL_INPUT_TEST);
        Assert.assertNotNull(rdfInput);
        try {
            Model jenaModel = ModelFactory.createDefaultModel().read(rdfInput, "", "Turtle");
            Assert.assertTrue("Turtle fomat is invalid", jenaModel.isEmpty());
            rdfInput.close();
        } catch (RiotException e) {
            Assert.assertTrue( ! e.getMessage().isEmpty());
        }
    }
}
