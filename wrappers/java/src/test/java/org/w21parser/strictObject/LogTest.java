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

import static org.junit.Assert.*;
import static org.w21parser.Vortex21StrictValidationTest.printW21Exception;
import static org.w21parser.VortexNativeBindingTest.fromPath;
import static org.w21parser.strictObject.BhaRunTest.navigate;
import static org.w21parser.strictObject.BhaRunTest.testDefaultRootAttributes;

public class LogTest {
    private static final Logger logger = LoggerFactory.getLogger(LogTest.class);
    private W21ParserLoader parser1;
    private BsonDocument logDocument = null;

    @Before
    public void setUp() throws Exception {
        this.parser1 = W21ParserLoader.begin().withInputRulesValidator().withInputWitsmlStrict().withResourceStats().withIgnoreInputWitsmlNS().build();
        try {
            this.parser1.readFromFile(fromPath("Log"), W21ParserLoader.W21Object.Log);
        } catch (W21Exception e) {
            printW21Exception(logger, e);
            throw e;
        }
        try {
            this.logDocument = (BsonDocument) this.parser1.parse(W21ParserLoader.W21OutputType.BSON);
        } catch (W21Exception e) {
            printW21Exception(logger, e);
            throw e;
        }
    }

    @After
    public void tearDown() {
        assertEquals("Parser 1 close method must return 0", 0, this.parser1.close());
    }

    @Test
    public void rootAttributeTest() throws Exception {
        testDefaultRootAttributes(
                (BsonDocument) navigate(this.logDocument, "Log", "#attributes"),
                "523e4568-e89b-12d3-a456-426614174000",
                "any version",
                "any object version"
        );
    }

    @Test
    public void aliasesArrayTest() throws Exception {
        BsonArray aliases = (BsonArray) navigate(this.logDocument, "Log", "Aliases");
        assertNotNull(aliases);
        assertEquals(1, aliases.size());

        BsonValue alias1 = aliases.get(0);
        BsonDocument attributes = (BsonDocument) navigate(alias1, "#attributes");
        assertNotNull(attributes);

        assertEquals("log authority", attributes.get("authority").asString().getValue());
        assertEquals("Log identifier", alias1.asDocument().get("Identifier").asString().getValue());
        assertEquals("Log indentifier kind", alias1.asDocument().get("IdentifierKind").asString().getValue());
        assertEquals("Log description (max 2000 char allowed)", alias1.asDocument().get("Description").asString().getValue().trim());

        assertEquals(DateUtils.toTimestamp("2026-01-05T18:39:47Z"), alias1.asDocument().get("EffectiveDateTime").asDateTime().getValue());
        assertEquals(DateUtils.toTimestamp("2026-02-05T19:39:48Z"), alias1.asDocument().get("TerminationDateTime").asDateTime().getValue());
    }

    @Test
    public void citationTest() throws Exception {
        BsonValue citation = (BsonValue) navigate(this.logDocument, "Log", "Citation");

        assertNotNull(citation);
        assertEquals("Citation title string (max 256 chars)", citation.asDocument().get("Title").asString().getValue().trim());
        assertEquals("b", citation.asDocument().get("Originator").asString().getValue());
        assertEquals(DateUtils.toTimestamp("2026-02-05T19:39:47Z"), citation.asDocument().get("Creation").asDateTime().getValue());
        assertEquals("c", citation.asDocument().get("Format").asString().getValue());
        assertEquals("d", citation.asDocument().get("Editor").asString().getValue());
        assertEquals(DateUtils.toTimestamp("2026-01-15T18:49:44Z"), citation.asDocument().get("LastUpdate").asDateTime().getValue());
        assertEquals("Description in Log Citation", citation.asDocument().get("Description").asString().getValue());

        BsonArray editorHistory = citation.asDocument().get("EditorHistory").asArray();

        assertEquals(10, editorHistory.size());

        assertEquals("Short 64 char editor history in Log Citation 1", editorHistory.get(0).asString().getValue());
        assertEquals("Short 64 char editor history in Log Citation 2", editorHistory.get(1).asString().getValue());
        assertEquals("Short 64 char editor history in Log Citation 3", editorHistory.get(2).asString().getValue());
        assertEquals("Short 64 char editor history in Log Citation 4", editorHistory.get(3).asString().getValue());
        assertEquals("Short 64 char editor history in Log Citation 5", editorHistory.get(4).asString().getValue());
        assertEquals("Short 64 char editor history in Log Citation 6", editorHistory.get(5).asString().getValue());
        assertEquals("Short 64 char editor history in Log Citation 7", editorHistory.get(6).asString().getValue());
        assertEquals("Short 64 char editor history in Log Citation 8", editorHistory.get(7).asString().getValue());
        assertEquals("Short 64 char editor history in Log Citation 9", editorHistory.get(8).asString().getValue());
        assertEquals("Short 64 char editor history in Log Citation 10", editorHistory.get(9).asString().getValue());

        assertEquals("Key DescriptiveKeywords in @1234", citation.asDocument().get("DescriptiveKeywords").asString().getValue());
    }

    @Test
    public void logMiscTest() throws Exception {
        BsonValue log = (BsonValue) navigate(this.logDocument, "Log");

        assertEquals("log existence", navigate(log, "Existence"));
        assertEquals("string 2000 char object version reason", navigate(log, "ObjectVersionReason"));
        assertEquals("Business activity history", ((BsonString)navigate(log, "BusinessActivityHistory", 0)).asString().getValue());

        BsonArray extensionNameValues = (BsonArray) navigate(log, "ExtensionNameValue");

        assertNotNull(extensionNameValues);
        assertEquals(1, extensionNameValues.size());

        BsonValue extensionNameValue = extensionNameValues.get(0);

        assertEquals("name", navigate(extensionNameValue, "Name"));
        assertEquals("legacy uom", navigate(extensionNameValue, "Value", "#attributes", "uom"));
        assertEquals("r", navigate(extensionNameValue, "Value", "#value"));
        assertEquals("api gamma ray", navigate(extensionNameValue, "MeasureClass"));
        assertEquals(DateUtils.toTimestamp("2026-02-05T19:39:47Z"), ((BsonValue)navigate(extensionNameValue, "DTim")).asDateTime().getValue());
        assertEquals(10, ((BsonValue)navigate(extensionNameValue, "Index")).asInt64().getValue());
        assertEquals("Desc", navigate(extensionNameValue, "Description"));

        assertEquals("inactive", navigate(log, "ActiveStatus"));
        assertEquals("processed", navigate(log, "ChannelState"));
        assertEquals("Run Number In String 64", navigate(log, "RunNumber"));
        assertEquals("Pass number in String 64", navigate(log, "PassNumber"));
        assertEquals("Pass description in String 64", navigate(log, "PassDescription"));

        BsonArray passDetails = (BsonArray) navigate(log, "PassDetail");
        assertNotNull(passDetails);
        assertEquals(2, passDetails.size());

        assertEquals(-50, ((BsonInt64)navigate(passDetails, 0, "Pass")).getValue());
        assertEquals("Pass description 1", navigate(passDetails, 0, "Description"));

        assertEquals(100, ((BsonInt64)navigate(passDetails, 1, "Pass")).getValue());
        assertEquals("Pass description 2", navigate(passDetails, 1, "Description"));

        // See Abstract interval test case @ org.w21parser.abstractIntervalType
        assertEquals("Base Comment", navigate(log, "PrimaryIndexInterval", "Comment"));

        BsonDocument loggingCompany = (BsonDocument)navigate(log, "LoggingCompany");

        assertNotNull(loggingCompany);
        assertEquals("523e4568-e89b-12d3-a456-426614174091", navigate(loggingCompany, "Uuid"));
        assertEquals("Object version 64 char max", navigate(loggingCompany, "ObjectVersion"));
        assertEquals("prodml59.Log", navigate(loggingCompany, "QualifiedType"));
        assertEquals("Title @ LoggingCompany", navigate(loggingCompany, "Title"));
        assertEquals("http://www.example.com/schema/anyURILoggingCompany", navigate(loggingCompany, "EnergisticsUri"));
        BsonArray locatorUrl = (BsonArray) navigate(loggingCompany, "LocatorUrl");

        assertNotNull(locatorUrl);
        assertEquals(1, locatorUrl.size());
        assertEquals("http://www.example.com/schema/anyURILoggingCompanyA", locatorUrl.get(0).asString().getValue());

        extensionNameValues = (BsonArray) navigate(loggingCompany, "ExtensionNameValue");

        assertNotNull(extensionNameValues);
        assertEquals(1, extensionNameValues.size());
        extensionNameValue = extensionNameValues.get(0);

        assertEquals("Name A", navigate(extensionNameValue, "Name"));
        assertEquals("uom test", navigate(extensionNameValue, "Value", "#attributes", "uom"));
        assertEquals("Value String Measure", navigate(extensionNameValue, "Value", "#value"));
        assertEquals("area per amount of substance", navigate(extensionNameValue, "MeasureClass"));
        assertEquals(DateUtils.toTimestamp("2026-05-07T22:39:47Z"), ((BsonValue)navigate(extensionNameValue, "DTim")).asDateTime().getValue());
        assertEquals(7810, ((BsonInt64) navigate(extensionNameValue, "Index")).getValue());
        assertEquals("Description A", navigate(extensionNameValue, "Description"));
    }

    @Test
    public void osduIntegrationTest() throws Exception {
        BsonDocument osduIntegration = (BsonDocument) navigate(this.logDocument, "Log", "OSDUIntegration");

        BsonArray lineageAssertions = (BsonArray) navigate(osduIntegration, "LineageAssertions");

        assertNotNull(lineageAssertions);
        assertEquals(1, lineageAssertions.size());

        BsonDocument lineageAssertion = (BsonDocument)lineageAssertions.get(0);

        assertEquals("602 ID STRING", navigate(lineageAssertion,  "ID"));
        assertEquals("reference", navigate(lineageAssertion,  "LineageRelationshipKind"));

        BsonArray ownerGroupArray = (BsonArray) navigate(osduIntegration, "OwnerGroup");

        assertNotNull(ownerGroupArray);
        assertEquals(1, ownerGroupArray.size());

        assertEquals("ownerGroup1", ((BsonString)navigate(ownerGroupArray, 0)).getValue());

        BsonArray viewerGroupArray = (BsonArray) navigate(osduIntegration, "ViewerGroup");

        assertNotNull(viewerGroupArray);
        assertEquals(2, viewerGroupArray.size());

        assertEquals("ViewerGroup1", ((BsonString)navigate(viewerGroupArray, 0)).getValue());
        assertEquals("ViewerGroup2", ((BsonString)navigate(viewerGroupArray, 1)).getValue());

        BsonArray legalTags = (BsonArray)navigate(osduIntegration, "LegalTags");

        assertNotNull(legalTags);
        assertEquals(3, legalTags.size());

        assertEquals("LegalTag1", ((BsonString)legalTags.get(0)).getValue());
        assertEquals("LegalTag2", ((BsonString)legalTags.get(1)).getValue());
        assertEquals("LegalTag3", ((BsonString)legalTags.get(2)).getValue());

        assertEquals("{\"test\":123456}", navigate(osduIntegration, "OSDUGeoJSON"));

        BsonDocument wGS84Latitude = (BsonDocument) navigate(osduIntegration, "WGS84Latitude");

        assertEquals("Mrad", navigate(wGS84Latitude, "#attributes", "uom"));
        assertEquals(12345, ((BsonDouble)navigate(wGS84Latitude, "#value")).getValue(), 1E-6);

        BsonDocument wGS84Longitude = (BsonDocument) navigate(osduIntegration, "WGS84Longitude");
        assertEquals("seca", navigate(wGS84Longitude, "#attributes", "uom"));
        assertEquals(-10.0102, ((BsonDouble)navigate(wGS84Longitude, "#value")).getValue(), 1E-6);

        BsonDocument wGS84LocationMetadata = (BsonDocument) navigate(osduIntegration, "WGS84LocationMetadata");
        assertEquals(DateUtils.toTimestamp("2026-03-18T17:59:47Z"), ((BsonDateTime)navigate(wGS84LocationMetadata, "SpatialLocationCoordinatesDate")).asDateTime().getValue());
        assertEquals("L", navigate(wGS84LocationMetadata,"QuantitativeAccuracyBand"));
        assertEquals("M", navigate(wGS84LocationMetadata,"QualitativeSpatialAccuracyType"));
        assertEquals("N", navigate(wGS84LocationMetadata,"CoordinateQualityCheckPerformedBy"));
        assertEquals(DateUtils.toTimestamp("2025-10-05T19:39:47Z"), ((BsonDateTime)navigate(wGS84LocationMetadata, "CoordinateQualityCheckDateTime")).asDateTime().getValue());

        BsonArray coordinateQualityCheckRemarkArray = (BsonArray) navigate(wGS84LocationMetadata,"CoordinateQualityCheckRemark");

        assertNotNull(coordinateQualityCheckRemarkArray);
        assertEquals(2, coordinateQualityCheckRemarkArray.size());
        assertEquals("S", coordinateQualityCheckRemarkArray.get(0).asString().getValue());
        assertEquals("R", coordinateQualityCheckRemarkArray.get(1).asString().getValue());

        BsonArray appliedOperationArray = (BsonArray) navigate(wGS84LocationMetadata,"AppliedOperation");

        assertNotNull(appliedOperationArray);
        assertEquals(3, appliedOperationArray.size());
        assertEquals("T", appliedOperationArray.get(0).asString().getValue());
        assertEquals("U", appliedOperationArray.get(1).asString().getValue());
        assertEquals("V", appliedOperationArray.get(2).asString().getValue());
        assertEquals("Field", navigate(osduIntegration, "Field"));
        assertEquals("Brazil", navigate(osduIntegration, "Country"));
        assertEquals("Rio de Janeiro", navigate(osduIntegration, "State"));
        assertEquals("County field", navigate(osduIntegration, "County"));
        assertEquals("Duque de Caxias", navigate(osduIntegration, "City"));
        assertEquals("ABCD", navigate(osduIntegration, "Region"));
        assertEquals("Nova Campinas", navigate(osduIntegration, "District"));
        assertEquals("Block field", navigate(osduIntegration, "Block"));
        assertEquals("Prospect field", navigate(osduIntegration, "Prospect"));
        assertEquals("Play field", navigate(osduIntegration, "Play"));
        assertEquals("basin field", navigate(osduIntegration, "Basin"));
    }

    @Test
    public void customDataInBhaRun() throws Exception {
        BsonArray customData = (BsonArray) navigate(this.logDocument, "Log", "CustomData");
        assertNull(customData);
    }
}
