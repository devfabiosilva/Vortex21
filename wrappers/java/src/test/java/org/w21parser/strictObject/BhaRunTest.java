package org.w21parser.strictObject;

import org.bson.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w21parser.DateUtils;
import org.w21parser.W21Exception;
import org.w21parser.W21ParserLoader;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.w21parser.VortexNativeBindingTest.fromPath;

public class BhaRunTest {

    private static final Logger logger = LoggerFactory.getLogger(BhaRunTest.class);
    private W21ParserLoader parser1;
    private BsonDocument bhaRunDocument = null;

    public Object navigate(Object obj, Object... args) throws Exception {
        Object tmp = obj;
        for (Object arg : args) {
            if (tmp == null) return null;

            if (tmp instanceof BsonDocument) {
                BsonValue value = ((BsonDocument) tmp).get(arg.toString());
                tmp = (value != null && value.isString()) ? value.asString().getValue() : value;
            } else if (tmp instanceof BSONObject) {
                tmp = ((BSONObject) tmp).get(arg.toString());
            } else if (tmp instanceof List) {
                int index = (arg instanceof Number) ? ((Number) arg).intValue() : Integer.parseInt(arg.toString());
                tmp = ((List<?>) tmp).get(index);
            }
            else
                throw new Exception("Unable to navigate : " + tmp.getClass().getName());
        }

        return tmp;
    }

    @Before
    public void setUp() throws Exception {
        this.parser1 = W21ParserLoader.begin().withInputRulesValidator().withInputWitsmlStrict().withResourceStats().withIgnoreInputWitsmlNS().build();
        try {
            this.parser1.readFromFile(fromPath("BhaRun"));
        } catch (W21Exception e) {
            logger.error("Main message: {}", e.getMessage());
            logger.error("Fault detail: {}", e.getFaultstring());
            logger.error("XML Fault detail: {}", e.getXMLfaultdetail());
            throw e;
        }
        bhaRunDocument = (BsonDocument)this.parser1.parse(W21ParserLoader.W21OutputType.BSON);
    }

    @After
    public void tearDown() {
        assertEquals("Parser 1 close method must return 0", 0, this.parser1.close());
    }

    @Test
    public void rootAttributeTest() throws Exception {
        BsonDocument attributes = (BsonDocument)navigate(bhaRunDocument, "BhaRun", "#attributes");
        assertEquals("123e4567-e89b-12d3-a456-426614174000", attributes.get("uuid").asString().getValue());
        assertEquals("2.1", attributes.get("schemaVersion").asString().getValue());
        assertEquals("2.11", attributes.get("objectVersion").asString().getValue());
    }

    @Test
    public void aliasesArrayTest() throws Exception {
        BsonArray aliases = (BsonArray)navigate(bhaRunDocument, "BhaRun", "Aliases");
        assertEquals(2, aliases.size());

        BsonValue alias1 = aliases.get(0);
        BsonDocument attributes = (BsonDocument)navigate(alias1, "#attributes");

        assertEquals("abc test", attributes.get("authority").asString().getValue());
        assertEquals("identifier", alias1.asDocument().get("Identifier").asString().getValue());
        assertEquals("identifier kind test", alias1.asDocument().get("IdentifierKind").asString().getValue());
        assertEquals("description test", alias1.asDocument().get("Description").asString().getValue());

        assertEquals(DateUtils.toTimestamp("2025-12-30T00:11:44Z"), alias1.asDocument().get("EffectiveDateTime").asDateTime().getValue());
        assertEquals(DateUtils.toTimestamp("2025-12-30T00:09:34Z"), alias1.asDocument().get("TerminationDateTime").asDateTime().getValue());

        BsonValue alias2 = aliases.get(1);
        attributes = (BsonDocument)navigate(alias2, "#attributes");

        assertEquals("ABC", attributes.get("authority").asString().getValue());
        assertEquals("IDENTIFIER", alias2.asDocument().get("Identifier").asString().getValue());
        assertEquals("identifier KIND TEST", alias2.asDocument().get("IdentifierKind").asString().getValue());
        assertEquals("DESCRIPTION TEST", alias2.asDocument().get("Description").asString().getValue());

        assertEquals(DateUtils.toTimestamp("2025-12-30T20:10:34Z"), alias2.asDocument().get("EffectiveDateTime").asDateTime().getValue());
        assertEquals(DateUtils.toTimestamp("2024-12-30T01:01:38Z"), alias2.asDocument().get("TerminationDateTime").asDateTime().getValue());
    }

    @Test
    public void citationTest() throws Exception {
        BsonValue citation = (BsonValue) navigate(bhaRunDocument, "BhaRun", "Citation");

        assertEquals("Citation Title", citation.asDocument().get("Title").asString().getValue());
        assertEquals("a", citation.asDocument().get("Originator").asString().getValue());
        assertEquals(DateUtils.toTimestamp("2025-12-30T14:19:54Z"), citation.asDocument().get("Creation").asDateTime().getValue());
        assertEquals("b", citation.asDocument().get("Format").asString().getValue());
        assertEquals("EDITOR IN CITATION", citation.asDocument().get("Editor").asString().getValue());
        assertEquals(DateUtils.toTimestamp("2025-12-30T08:32:34Z"), citation.asDocument().get("LastUpdate").asDateTime().getValue());
        assertEquals("Description in Citation", citation.asDocument().get("Description").asString().getValue());

        BsonArray editorHistory = citation.asDocument().get("EditorHistory").asArray();

        assertEquals(3, editorHistory.size());

        assertEquals("Editor History 1", editorHistory.get(0).asString().getValue());
        assertEquals("Editor History 2", editorHistory.get(1).asString().getValue());
        assertEquals("Editor History 3", editorHistory.get(2).asString().getValue());

        assertEquals("Key DescriptiveKeywords in @123", citation.asDocument().get("DescriptiveKeywords").asString().getValue());
    }

    @Test
    public void miscTest() throws Exception {
        BsonDocument bhaRunObj = (BsonDocument) navigate(bhaRunDocument, "BhaRun");

        assertEquals("Existence test", bhaRunObj.get("Existence").asString().getValue());
        assertEquals("Object version reason", bhaRunObj.get("ObjectVersionReason").asString().getValue());

        BsonArray businessActivityHistory = (BsonArray) bhaRunObj.get("BusinessActivityHistory");

        assertEquals(2, businessActivityHistory.size());
        assertEquals("Business Activity History 1", businessActivityHistory.get(0).asString().getValue());
        assertEquals("Business Activity History 2", businessActivityHistory.get(1).asString().getValue());
    }
}
