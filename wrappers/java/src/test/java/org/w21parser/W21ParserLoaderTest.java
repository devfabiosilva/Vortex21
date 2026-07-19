package org.w21parser;

import org.bson.BsonDocument;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.w21parser.VortexNativeBindingTest.fromPath;
import static org.w21parser.W21ParserLoader.W21OutputType.BSON;

public class W21ParserLoaderTest {

    private static final Logger logger = LoggerFactory.getLogger(W21ParserLoaderTest.class);
    W21ParserLoader parser1, parser2;
    @Before
    public void setUp() throws Exception {
        parser1 = W21ParserLoader.begin().withInputRulesValidator().withInputWitsmlStrict().withResourceStats().withIgnoreInputWitsmlNS().build();
        parser2 = W21ParserLoader.begin().withInputRulesValidator().withInputWitsmlStrict().withIgnoreInputWitsmlNS().build();
    }

    @After
    public void tearDown() {
        if (parser1 != null)
            System.out.println("Close result1 = " + parser1.close());

        if (parser2 != null)
            System.out.println("Close result2 = " + parser2.close());
    }

    @Test
    public void build() {
        byte[] dest = new byte[25];
    }

    @Test
    public void close() {

    }

    @Test
    public void testParseFromStream() throws Exception {
        parser1.readFromFile("../../xmls/OpsReport2.xml",  W21ParserLoader.W21Object.OpsReport);
        //Object obj = parser1.parse();
        Object obj;
        obj = parser1.parse(BSON);
        parser1.parseJson(W21ParserLoader.W21OutputJsonType.JSON_STRING);
        ((BsonDocument) obj).toJson();
        System.out.println(parser1.loadStatistics());
        parser1.close();
    }

    @Ignore
    @Test
    public void testExample() throws Exception {
        W21ParserLoader myParser = W21ParserLoader
                .begin().withInputRulesValidator()
                .withInputWitsmlStrict()
                .withResourceStats()
                .withIgnoreInputWitsmlNS()
                .build();
        try {
            // First parser OpsReport
            myParser.readFromFile(fromPath("Log"));
            Object obj;
            obj = myParser.parse(BSON); // Object parsed type (Json, Bson object, Bson serialized)
            ((BsonDocument) obj).toJson();
            // Load document and process statistic for Prometheus/Grafana ...
            System.out.println(myParser.loadStatistics());

        } catch (W21Exception e) {
            logger.error("Message {}", e.getMessage());
            logger.error("Fault message {}", e.getFaultstring());
            logger.error("Fault message {}", e.getXMLfaultdetail());
        } finally {
            parser1.close();
        }
    }
}