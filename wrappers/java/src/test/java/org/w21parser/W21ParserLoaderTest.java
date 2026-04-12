package org.w21parser;

import org.bson.BsonDocument;
import org.bson.RawBsonDocument;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicReference;

public class W21ParserLoaderTest {

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
        obj = parser1.parse(W21ParserLoader.W21OutputType.BSON);
        parser1.parseJson(W21ParserLoader.W21OutputJsonType.JSON_STRING);
        ((BsonDocument) obj).toJson();
        System.out.println(parser1.loadStatistics());
        parser1.close();
        //byte [] dest = new byte[25];
        //if (obj != null)
        //    System.out.println("Obj " + obj.toString());
    }
}