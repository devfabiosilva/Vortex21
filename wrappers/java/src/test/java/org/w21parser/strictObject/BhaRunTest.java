package org.w21parser.strictObject;

import org.bson.*;

import org.jetbrains.annotations.Nullable;
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
import static org.junit.Assert.assertNotNull;
import static org.w21parser.Vortex21StrictValidationTest.printW21Exception;
import static org.w21parser.VortexNativeBindingTest.fromPath;

public class BhaRunTest {

    private static final Logger logger = LoggerFactory.getLogger(BhaRunTest.class);
    private W21ParserLoader parser1;
    private BsonDocument bhaRunDocument = null;

    public static Object navigate(Object obj, Object... args) throws Exception {
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
            this.parser1.readFromFile(fromPath("BhaRun"), W21ParserLoader.W21Object.BhaRun);
        } catch (W21Exception e) {
            printW21Exception(logger, e);
            throw e;
        }
        bhaRunDocument = (BsonDocument)this.parser1.parse(W21ParserLoader.W21OutputType.BSON);
    }

    @After
    public void tearDown() {
        assertEquals("Parser 1 close method must return 0", 0, this.parser1.close());
    }

    public static void testDefaultRootAttributes(@Nullable BsonDocument bsonRootAttributes, String uuid, String schemaVersion, String objectVersion) {
        assertEquals(uuid, bsonRootAttributes.get("uuid").asString().getValue());
        assertEquals(schemaVersion, bsonRootAttributes.get("schemaVersion").asString().getValue());
        assertEquals(objectVersion, bsonRootAttributes.get("objectVersion").asString().getValue());
    }

    @Test
    public void rootAttributeTest() throws Exception {
        testDefaultRootAttributes(
            (BsonDocument)navigate(bhaRunDocument, "BhaRun", "#attributes"),
                "123e4567-e89b-12d3-a456-426614174000",
                "2.1",
                "2.11"
        );

    }

    @Test
    public void aliasesArrayTest() throws Exception {
        BsonArray aliases = (BsonArray)navigate(bhaRunDocument, "BhaRun", "Aliases");
        assertNotNull(aliases);
        assertEquals(2, aliases.size());

        BsonValue alias1 = aliases.get(0);
        BsonDocument attributes = (BsonDocument)navigate(alias1, "#attributes");
        assertNotNull(attributes);

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

        assertNotNull(citation);
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

        BsonArray extensionNameValues = (BsonArray) navigate(bhaRunObj, "ExtensionNameValue");

        assertEquals(4, extensionNameValues.size());

        BsonDocument extensionNameValue = (BsonDocument)extensionNameValues.get(0);

        assertEquals("Name in Extension", extensionNameValue.get("Name").asString().getValue());
        assertEquals("uom test", navigate(extensionNameValue, "Value", "#attributes", "uom"));
        assertEquals("test 1 in Value", navigate(extensionNameValue, "Value", "#value"));
        assertEquals("mass", navigate(extensionNameValue, "MeasureClass"));
        assertEquals(DateUtils.toTimestamp("2025-12-30T09:19:34Z"), ((BsonValue)navigate(extensionNameValue, "DTim")).asDateTime().getValue());
        assertEquals("Descrição (UTF-8 in Portuguese)", navigate(extensionNameValue, "Description"));

        extensionNameValue = (BsonDocument)extensionNameValues.get(1);

        assertEquals("#### Name in Extension 1 ####", extensionNameValue.get("Name").asString().getValue());
        assertEquals("uom test2", navigate(extensionNameValue, "Value", "#attributes", "uom"));
        assertEquals("test 2 in Value", navigate(extensionNameValue, "Value", "#value"));
        assertEquals("length per mass", navigate(extensionNameValue, "MeasureClass"));
        assertEquals(DateUtils.toTimestamp("2025-07-27T09:19:44Z"), ((BsonValue)navigate(extensionNameValue, "DTim")).asDateTime().getValue());
        assertEquals("Description 2", navigate(extensionNameValue, "Description"));

        extensionNameValue = (BsonDocument)extensionNameValues.get(2);

        assertEquals("#### Name in Extension 2 ####", extensionNameValue.get("Name").asString().getValue());
        assertEquals("uom test3", navigate(extensionNameValue, "Value", "#attributes", "uom"));
        assertEquals("test 3 in Value", navigate(extensionNameValue, "Value", "#value"));
        assertEquals("unitless", navigate(extensionNameValue, "MeasureClass"));
        assertEquals(DateUtils.toTimestamp("2025-10-30T19:21:54Z"), ((BsonValue)navigate(extensionNameValue, "DTim")).asDateTime().getValue());
        assertEquals("Description in this Extension Name Value", navigate(extensionNameValue, "Description"));

        extensionNameValue = (BsonDocument)extensionNameValues.get(3);

        assertEquals("q", extensionNameValue.get("Name").asString().getValue());
        assertEquals("", navigate(extensionNameValue, "Value", "#attributes", "uom"));
        assertEquals("r", navigate(extensionNameValue, "Value", "#value"));
        assertEquals("absorbed dose", navigate(extensionNameValue, "MeasureClass"));
        assertEquals(DateUtils.toTimestamp("2025-10-30T00:09:34Z"), ((BsonValue)navigate(extensionNameValue, "DTim")).asDateTime().getValue());
        assertEquals(1600, ((BsonValue)navigate(extensionNameValue, "Index")).asInt64().getValue());
        assertEquals("s", navigate(extensionNameValue, "Description"));

        assertEquals(DateUtils.toTimestamp("2025-12-30T20:09:34Z"), ((BsonValue)navigate(bhaRunObj, "DTimStart")).asDateTime().getValue());
        assertEquals(DateUtils.toTimestamp("2025-12-30T00:17:36Z"), ((BsonValue)navigate(bhaRunObj, "DTimStop")).asDateTime().getValue());
        assertEquals(DateUtils.toTimestamp("2025-08-30T01:09:34Z"), ((BsonValue)navigate(bhaRunObj, "DTimStartDrilling")).asDateTime().getValue());
        assertEquals(DateUtils.toTimestamp("2025-06-30T00:09:34Z"), ((BsonValue)navigate(bhaRunObj, "DTimStopDrilling")).asDateTime().getValue());

        assertEquals("0.01 dega/ft", navigate(bhaRunObj, "PlanDogleg", "#attributes", "uom"));
        assertEquals(7.0, ((BsonDouble)navigate(bhaRunObj, "PlanDogleg", "#value")).getValue(), 1E-6);

        assertEquals("rev/m", navigate(bhaRunObj, "ActDogleg", "#attributes", "uom"));
        assertEquals(87.0, ((BsonDouble)navigate(bhaRunObj, "ActDogleg", "#value")).getValue(), 1E-6);

        assertEquals("1/30 dega/m", navigate(bhaRunObj, "ActDoglegMx", "#attributes", "uom"));
        assertEquals(1.06, ((BsonDouble)navigate(bhaRunObj, "ActDoglegMx", "#value")).getValue(), 1E-6);

        assertEquals("plan", navigate(bhaRunObj, "BhaRunStatus"));
        assertEquals(-2, ((BsonInt64)navigate(bhaRunObj, "NumBitRun")).getValue());

        assertEquals(1234567890, ((BsonInt64)navigate(bhaRunObj, "NumStringRun")).getValue());

        assertEquals("aa", navigate(bhaRunObj, "ReasonTrip"));
        assertEquals("bb", navigate(bhaRunObj, "ObjectiveBha"));
    }

    @Test
    public void osduIntegrationTest() throws Exception {
        BsonDocument osduIntegration = (BsonDocument) navigate(bhaRunDocument, "BhaRun", "OSDUIntegration");

        BsonArray lineageAssertions = (BsonArray) navigate(osduIntegration, "LineageAssertions");

        assertNotNull(lineageAssertions);
        assertEquals(3, lineageAssertions.size());

        BsonDocument lineageAssertion = (BsonDocument)lineageAssertions.get(0);

        assertEquals("601", navigate(lineageAssertion,  "ID"));
        assertEquals("direct", navigate(lineageAssertion,  "LineageRelationshipKind"));

        lineageAssertion = (BsonDocument)lineageAssertions.get(1);

        assertEquals("602ID", navigate(lineageAssertion,  "ID"));
        assertEquals("indirect", navigate(lineageAssertion,  "LineageRelationshipKind"));

        lineageAssertion = (BsonDocument)lineageAssertions.get(2);

        assertEquals("603ID", navigate(lineageAssertion,  "ID"));
        assertEquals("reference", navigate(lineageAssertion,  "LineageRelationshipKind"));

        BsonArray ownerGroupArray = (BsonArray) navigate(osduIntegration, "OwnerGroup");

        assertNotNull(ownerGroupArray);
        assertEquals(2, ownerGroupArray.size());

        assertEquals("ownerGroup1", ((BsonString)navigate(ownerGroupArray, 0)).getValue());
        assertEquals("ownerGroup2", ((BsonString)navigate(ownerGroupArray, 1)).getValue());

        BsonArray viewerGroupArray = (BsonArray) navigate(osduIntegration, "ViewerGroup");

        assertNotNull(viewerGroupArray);
        assertEquals(2, viewerGroupArray.size());

        assertEquals("ViewerGroup1", ((BsonString)navigate(viewerGroupArray, 0)).getValue());
        assertEquals("ViewerGroup2", ((BsonString)navigate(viewerGroupArray, 1)).getValue());

        BsonArray legalTags = (BsonArray)navigate(osduIntegration, "LegalTags");

        assertNotNull(legalTags);
        assertEquals(2, legalTags.size());

        assertEquals("LegalTag1", ((BsonString)legalTags.get(0)).getValue());
        assertEquals("LegalTag2", ((BsonString)legalTags.get(1)).getValue());

        assertEquals("{\"test\":123}", navigate(osduIntegration, "OSDUGeoJSON"));

        BsonDocument wGS84Latitude = (BsonDocument) navigate(osduIntegration, "WGS84Latitude");

        assertEquals("0.001 seca", navigate(wGS84Latitude, "#attributes", "uom"));
        assertEquals(-1.234, ((BsonDouble)navigate(wGS84Latitude, "#value")).getValue(), 1E-6);

        BsonDocument wGS84Longitude = (BsonDocument) navigate(osduIntegration, "WGS84Longitude");
        assertEquals("urad", navigate(wGS84Longitude, "#attributes", "uom"));
        assertEquals(5.678E4, ((BsonDouble)navigate(wGS84Longitude, "#value")).getValue(), 1E-6);

        BsonDocument wGS84LocationMetadata = (BsonDocument) navigate(osduIntegration, "WGS84LocationMetadata");
        assertEquals(DateUtils.toTimestamp("2025-09-30T00:09:34Z"), ((BsonDateTime)navigate(wGS84LocationMetadata, "SpatialLocationCoordinatesDate")).asDateTime().getValue());
        assertEquals("g", navigate(wGS84LocationMetadata,"QuantitativeAccuracyBand"));
        assertEquals("h", navigate(wGS84LocationMetadata,"QualitativeSpatialAccuracyType"));
        assertEquals("i", navigate(wGS84LocationMetadata,"CoordinateQualityCheckPerformedBy"));
        assertEquals(DateUtils.toTimestamp("2025-11-30T10:09:34Z"), ((BsonDateTime)navigate(wGS84LocationMetadata, "CoordinateQualityCheckDateTime")).asDateTime().getValue());

        BsonArray coordinateQualityCheckRemarkArray = (BsonArray) navigate(wGS84LocationMetadata,"CoordinateQualityCheckRemark");

        assertNotNull(coordinateQualityCheckRemarkArray);
        assertEquals(2, coordinateQualityCheckRemarkArray.size());
        assertEquals("j", coordinateQualityCheckRemarkArray.get(0).asString().getValue());
        assertEquals("k", coordinateQualityCheckRemarkArray.get(1).asString().getValue());

        BsonArray appliedOperationArray = (BsonArray) navigate(wGS84LocationMetadata,"AppliedOperation");

        assertNotNull(appliedOperationArray);
        assertEquals(3, appliedOperationArray.size());
        assertEquals("l", appliedOperationArray.get(0).asString().getValue());
        assertEquals("m", appliedOperationArray.get(1).asString().getValue());
        assertEquals("n", appliedOperationArray.get(2).asString().getValue());
        assertEquals("Field", navigate(osduIntegration, "Field"));
        assertEquals("Brazil", navigate(osduIntegration, "Country"));
        assertEquals("Rio de Janeiro", navigate(osduIntegration, "State"));
        assertEquals("County field", navigate(osduIntegration, "County"));
        assertEquals("Duque de Caxias", navigate(osduIntegration, "City"));
        assertEquals("ABC", navigate(osduIntegration, "Region"));
        assertEquals("Nova Campinas", navigate(osduIntegration, "District"));
        assertEquals("Block field", navigate(osduIntegration, "Block"));
        assertEquals("Prospect field", navigate(osduIntegration, "Prospect"));
        assertEquals("basin field", navigate(osduIntegration, "Basin"));
    }

    @Test
    public void customDataInBhaRun() throws Exception {
        BsonArray customData = (BsonArray) navigate(bhaRunDocument, "BhaRun", "CustomData");
        assertEquals(2, customData.size());

        assertEquals("<b>Custom data Test 1 as string</b>", customData.get(0).asString().getValue().trim());
        assertEquals("<c atr=\"mycustomattr\">\n" +
                "         <d>Complex Custom Data</d>\n" +
                "        </c>", customData.get(1).asString().getValue().trim());
    }

    @Test
    public void drillingParamsTest() throws Exception {
        BsonArray drillingParams = (BsonArray) navigate(this.bhaRunDocument, "BhaRun", "DrillingParams");

        assertEquals(2, drillingParams.size());

        BsonDocument drillingParam = (BsonDocument) drillingParams.get(0);

        assertEquals("required string 64 A", navigate(drillingParam, "#attributes", "uid"));
        assertEquals("wk", navigate(drillingParam, "ETimOpBit", "#attributes", "uom"));
        assertEquals(-0.89, ((BsonDouble)navigate(drillingParam, "ETimOpBit", "#value")).getValue(), 1E-6);

        BsonDocument mdHoleStart = (BsonDocument) navigate(drillingParam, "MdHoleStart");

        assertEquals("ua", navigate(mdHoleStart, "MeasuredDepth", "#attributes", "uom"));
        assertEquals(150., ((BsonDouble)navigate(mdHoleStart, "MeasuredDepth", "#value")).getValue(), 1E-6);

        BsonDocument datum = (BsonDocument) navigate(mdHoleStart, "Datum");

        assertEquals("123e4567-e89b-12d3-a456-426614174001", navigate(datum, "Uuid"));
        assertEquals("t", navigate(datum, "ObjectVersion"));
        assertEquals("witsml21.BhaRun123", navigate(datum, "QualifiedType"));
        assertEquals("v", navigate(datum, "Title"));
        assertEquals("http://www.example.com/schema/anyURI", navigate(datum, "EnergisticsUri"));

        BsonArray locatorUrl = (BsonArray) navigate(datum, "LocatorUrl");

        assertEquals(2, locatorUrl.size());
        assertEquals("http://www.example.com/schema/anyURIB", ((BsonString)navigate(locatorUrl, 0)).asString().getValue());
        assertEquals("http://www.example.com/schema/anyURIC", ((BsonString)navigate(locatorUrl, 1)).asString().getValue());

        BsonArray extensionNameValues = (BsonArray) navigate(datum, "ExtensionNameValue");

        assertEquals(2, extensionNameValues.size());

        BsonDocument extensionNameValue = (BsonDocument)extensionNameValues.get(0);
        assertEquals("w", navigate(extensionNameValue, "Name"));
        assertEquals("testUom1", navigate(extensionNameValue, "Value", "#attributes", "uom"));
        assertEquals("x", navigate(extensionNameValue, "Value", "#value"));
        assertEquals("y", navigate(extensionNameValue, "Description"));

        extensionNameValue = (BsonDocument)extensionNameValues.get(1);
        assertEquals("w A", navigate(extensionNameValue, "Name"));
        assertEquals("abc", navigate(extensionNameValue, "Value", "#attributes", "uom"));
        assertEquals("xyz", navigate(extensionNameValue, "Value", "#value"));
        assertEquals("description in datum extension name value", navigate(extensionNameValue, "Description"));

        assertEquals("10 kN", navigate(drillingParam, "HkldRot", "#attributes", "uom"));
        assertEquals(6.81, ((BsonDouble)navigate(drillingParam, "HkldRot", "#value")).getValue(), 1E-6);

        assertEquals("tonf[UK]", navigate(drillingParam, "OverPull", "#attributes", "uom"));
        assertEquals(17.001, ((BsonDouble)navigate(drillingParam, "OverPull", "#value")).getValue(), 1E-6);

        assertEquals("N", navigate(drillingParam, "SlackOff", "#attributes", "uom"));
        assertEquals(0.0, ((BsonDouble)navigate(drillingParam, "SlackOff", "#value")).getValue(), 1E-6);

        assertEquals("mN", navigate(drillingParam, "HkldUp", "#attributes", "uom"));
        assertEquals(Double.NaN, ((BsonDouble)navigate(drillingParam, "HkldUp", "#value")).getValue(), 1E-6);

        assertEquals("pdl", navigate(drillingParam, "HkldDn", "#attributes", "uom"));
        assertEquals(Double.POSITIVE_INFINITY, ((BsonDouble)navigate(drillingParam, "HkldDn", "#value")).getValue(), 1E-6);

        assertEquals("pdl.ft", navigate(drillingParam, "TqOnBotAv", "#attributes", "uom"));
        assertEquals(Double.NEGATIVE_INFINITY, ((BsonDouble)navigate(drillingParam, "TqOnBotAv", "#value")).getValue(), 1E-6);

        assertEquals("J", navigate(drillingParam, "TqOnBotMx", "#attributes", "uom"));
        //assertEquals(0.0, ((BsonDouble)navigate(drillingParam, "TqOnBotMx", "#value")).getValue(), 1E-6);
        // IEEE 754 Standard below
        assertEquals("-0.0 value test failed: IEEE 754 Standard wrong. Was expected -0.0", 0, Double.compare(-0.0, ((BsonDouble)navigate(drillingParam, "TqOnBotMx", "#value")).getValue()));

        assertEquals("N.m", navigate(drillingParam, "TqOnBotMn", "#attributes", "uom"));
        assertEquals(1.0, ((BsonDouble)navigate(drillingParam, "TqOnBotMn", "#value")).getValue(), 1E-6);

        assertEquals("dN.m", navigate(drillingParam, "TqOffBotAv", "#attributes", "uom"));
        assertEquals(-1.0, ((BsonDouble)navigate(drillingParam, "TqOffBotAv", "#value")).getValue(), 1E-6);

        assertEquals("tonf[US].ft", navigate(drillingParam, "TqDhAv", "#attributes", "uom"));
        assertEquals(180, ((BsonDouble)navigate(drillingParam, "TqDhAv", "#value")).getValue(), 1E-6);

        assertEquals("uN", navigate(drillingParam, "WtAboveJar", "#attributes", "uom"));
        assertEquals(1892.1187, ((BsonDouble)navigate(drillingParam, "WtAboveJar", "#value")).getValue(), 1E-6);

        assertEquals("cN", navigate(drillingParam, "WtBelowJar", "#attributes", "uom"));
        assertEquals(-180.01, ((BsonDouble)navigate(drillingParam, "WtBelowJar", "#value")).getValue(), 1E-6);

        assertEquals("MN", navigate(drillingParam, "WtMud", "#attributes", "uom"));
        assertEquals(12.0, ((BsonDouble)navigate(drillingParam, "WtMud", "#value")).getValue(), 1E-6);

        assertEquals("any uom here 1 (required)", navigate(drillingParam, "FlowratePumpAv", "#attributes", "uom"));
        assertEquals(16, ((BsonDouble)navigate(drillingParam, "FlowratePumpAv", "#value")).getValue(), 1E-6);

        assertEquals("any uom here 2 (required)", navigate(drillingParam, "FlowratePumpMx", "#attributes", "uom"));
        assertEquals(17, ((BsonDouble)navigate(drillingParam, "FlowratePumpMx", "#value")).getValue(), 1E-6);

        assertEquals("any uom here 3 (required)", navigate(drillingParam, "FlowratePumpMn", "#attributes", "uom"));
        assertEquals(18.181, ((BsonDouble)navigate(drillingParam, "FlowratePumpMn", "#value")).getValue(), 1E-6);

        assertEquals("1000 ft/h", navigate(drillingParam, "VelNozzleAv", "#attributes", "uom"));
        assertEquals(2, ((BsonDouble)navigate(drillingParam, "VelNozzleAv", "#value")).getValue(), 1E-6);

        assertEquals("kW", navigate(drillingParam, "PowBit", "#attributes", "uom"));
        assertEquals(5, ((BsonDouble)navigate(drillingParam, "PowBit", "#value")).getValue(), 1E-6);

        assertEquals("any uom here 4 (required)", navigate(drillingParam, "PresDropBit", "#attributes", "uom"));
        assertEquals(160.09, ((BsonDouble)navigate(drillingParam, "PresDropBit", "#value")).getValue(), 1E-6);

        assertEquals("1/2 ms", navigate(drillingParam, "CTimHold", "#attributes", "uom"));
        assertEquals(0.8, ((BsonDouble)navigate(drillingParam, "CTimHold", "#value")).getValue(), 1E-6);

        assertEquals("Ga[t]", navigate(drillingParam, "CTimSteering", "#attributes", "uom"));
        assertEquals(-0.182, ((BsonDouble)navigate(drillingParam, "CTimSteering", "#value")).getValue(), 1E-6);

        assertEquals("min", navigate(drillingParam, "CTimDrillRot", "#attributes", "uom"));
        assertEquals(129.6, ((BsonDouble)navigate(drillingParam, "CTimDrillRot", "#value")).getValue(), 1E-6);

        assertEquals("na", navigate(drillingParam, "CTimDrillSlid", "#attributes", "uom"));
        assertEquals(1726.177, ((BsonDouble)navigate(drillingParam, "CTimDrillSlid", "#value")).getValue(), 1E-6);

        assertEquals("wk", navigate(drillingParam, "CTimCirc", "#attributes", "uom"));
        assertEquals(1889.0008, ((BsonDouble)navigate(drillingParam, "CTimCirc", "#value")).getValue(), 1E-6);

        assertEquals("us", navigate(drillingParam, "CTimReam", "#attributes", "uom"));
        assertEquals(17799.176, ((BsonDouble)navigate(drillingParam, "CTimReam", "#value")).getValue(), 1E-6);

        assertEquals("yd[US]", navigate(drillingParam, "DistDrillRot", "#attributes", "uom"));
        assertEquals(.6, ((BsonDouble)navigate(drillingParam, "DistDrillRot", "#value")).getValue(), 1E-6);

        assertEquals("m", navigate(drillingParam, "DistDrillSlid", "#attributes", "uom"));
        assertEquals(64.013, ((BsonDouble)navigate(drillingParam, "DistDrillSlid", "#value")).getValue(), 1E-6);

        assertEquals("rod[US]", navigate(drillingParam, "DistReam", "#attributes", "uom"));
        assertEquals(800.1, ((BsonDouble)navigate(drillingParam, "DistReam", "#value")).getValue(), 1E-6);

        assertEquals("yd[Se]", navigate(drillingParam, "DistHold", "#attributes", "uom"));
        assertEquals(713.009, ((BsonDouble)navigate(drillingParam, "DistHold", "#value")).getValue(), 1E-6);

        assertEquals("yd[SeT]", navigate(drillingParam, "DistSteering", "#attributes", "uom"));
        assertEquals(8.099, ((BsonDouble)navigate(drillingParam, "DistSteering", "#value")).getValue(), 1E-6);

        assertEquals("dega/h", navigate(drillingParam, "RpmAv", "#attributes", "uom"));
        assertEquals(0.891, ((BsonDouble)navigate(drillingParam, "RpmAv", "#value")).getValue(), 1E-6);

        assertEquals("rpm", navigate(drillingParam, "RpmMx", "#attributes", "uom"));
        assertEquals(1319.109, ((BsonDouble)navigate(drillingParam, "RpmMx", "#value")).getValue(), 1E-6);

        assertEquals("rad/s", navigate(drillingParam, "RpmMn", "#attributes", "uom"));
        assertEquals(35, ((BsonDouble)navigate(drillingParam, "RpmMn", "#value")).getValue(), 1E-6);

        assertEquals("rev/s", navigate(drillingParam, "RpmAvDh", "#attributes", "uom"));
        assertEquals(89, ((BsonDouble)navigate(drillingParam, "RpmAvDh", "#value")).getValue(), 1E-6);

        assertEquals("1000 ft/h", navigate(drillingParam, "RopAv", "#attributes", "uom"));
        assertEquals(600, ((BsonDouble)navigate(drillingParam, "RopAv", "#value")).getValue(), 1E-6);

        assertEquals("knot", navigate(drillingParam, "RopMx", "#attributes", "uom"));
        assertEquals(100, ((BsonDouble)navigate(drillingParam, "RopMx", "#value")).getValue(), 1E-6);

        assertEquals("m/min", navigate(drillingParam, "RopMn", "#attributes", "uom"));
        assertEquals(18.01, ((BsonDouble)navigate(drillingParam, "RopMn", "#value")).getValue(), 1E-6);

        assertEquals("10 kN", navigate(drillingParam, "WobAv", "#attributes", "uom"));
        assertEquals(10, ((BsonDouble)navigate(drillingParam, "WobAv", "#value")).getValue(), 1E-6);

        assertEquals("kdyne", navigate(drillingParam, "WobMx", "#attributes", "uom"));
        assertEquals(20, ((BsonDouble)navigate(drillingParam, "WobMx", "#value")).getValue(), 1E-6);

        assertEquals("Mgf", navigate(drillingParam, "WobMn", "#attributes", "uom"));
        assertEquals(9, ((BsonDouble)navigate(drillingParam, "WobMn", "#value")).getValue(), 1E-6);

        assertEquals("nN", navigate(drillingParam, "WobAvDh", "#attributes", "uom"));
        assertEquals(1801.89, ((BsonDouble)navigate(drillingParam, "WobAvDh", "#value")).getValue(), 1E-6);

        assertEquals("Text with max 2000 char", ((String)navigate(drillingParam, "ReasonTrip")).trim());

        assertEquals("G", navigate(drillingParam, "ObjectiveBha"));

        assertEquals("ccgr", navigate(drillingParam, "AziTop", "#attributes", "uom"));
        assertEquals(1.2, ((BsonDouble)navigate(drillingParam, "AziTop", "#value")).getValue(), 1E-6);

        assertEquals("mina", navigate(drillingParam, "AziBottom", "#attributes", "uom"));
        assertEquals(0.2, ((BsonDouble)navigate(drillingParam, "AziBottom", "#value")).getValue(), 1E-6);

        assertEquals("Mrad", navigate(drillingParam, "InclStart", "#attributes", "uom"));
        assertEquals(.004, ((BsonDouble)navigate(drillingParam, "InclStart", "#value")).getValue(), 1E-6);

        assertEquals("mrad", navigate(drillingParam, "InclMx", "#attributes", "uom"));
        assertEquals(123.01, ((BsonDouble)navigate(drillingParam, "InclMx", "#value")).getValue(), 1E-6);

        assertEquals("gon", navigate(drillingParam, "InclMn", "#attributes", "uom"));
        assertEquals(7.1, ((BsonDouble)navigate(drillingParam, "InclMn", "#value")).getValue(), 1E-6);

        assertEquals("mila", navigate(drillingParam, "InclStop", "#attributes", "uom"));
        assertEquals(54, ((BsonDouble)navigate(drillingParam, "InclStop", "#value")).getValue(), 1E-6);

        assertEquals("K", navigate(drillingParam, "TempMudDhMx", "#attributes", "uom"));
        assertEquals(560, ((BsonDouble)navigate(drillingParam, "TempMudDhMx", "#value")).getValue(), 1E-6);

        assertEquals("required uom with legacy 1", navigate(drillingParam, "PresPumpAv", "#attributes", "uom"));
        assertEquals(1.6, ((BsonDouble)navigate(drillingParam, "PresPumpAv", "#value")).getValue(), 1E-6);

        assertEquals("required uom with legacy 2", navigate(drillingParam, "FlowrateBit", "#attributes", "uom"));
        assertEquals(.35, ((BsonDouble)navigate(drillingParam, "FlowrateBit", "#value")).getValue(), 1E-6);

        assertEquals("oil-based", navigate(drillingParam, "MudClass"));
        assertEquals("diesel oil-based", navigate(drillingParam, "MudSubClass"));
        assertEquals("Optional comments (max size: 2000)", ((String)navigate(drillingParam, "Comments")).trim());

        extensionNameValues = (BsonArray) navigate(drillingParam, "ExtensionNameValue");

        assertEquals(1, extensionNameValues.size());

        extensionNameValue = (BsonDocument) extensionNameValues.get(0);

        assertEquals("D", navigate(extensionNameValue, "Name"));

        assertEquals("", navigate(extensionNameValue, "Value", "#attributes", "uom"));
        assertEquals("E", navigate(extensionNameValue, "Value", "#value"));
        assertEquals("absorbed dose", navigate(extensionNameValue, "MeasureClass"));
        assertEquals(DateUtils.toTimestamp("2024-12-30T00:07:21Z"), ((BsonDateTime)navigate(extensionNameValue, "DTim")).asDateTime().getValue());
        assertEquals(-10, ((BsonInt64)navigate(extensionNameValue, "Index")).getValue());
        assertEquals("F", navigate(extensionNameValue, "Description"));

        BsonDocument tubular = (BsonDocument) navigate(drillingParam, "Tubular");
        assertEquals("123e4567-e89b-12d3-a456-426614174003", navigate(tubular, "Uuid"));
        assertEquals("123.4 Any version with max 2000 chars", navigate(tubular, "ObjectVersion"));
        assertEquals("resqml40.xy", navigate(tubular, "QualifiedType"));
        assertEquals("Tubular title", navigate(tubular, "Title"));
        assertEquals("http://www.example.com/schema/anyURITubular", navigate(tubular, "EnergisticsUri"));

        locatorUrl = (BsonArray) navigate(tubular, "LocatorUrl");

        assertEquals(3, locatorUrl.size());
        assertEquals("http://www.example.com/schema/anyURIBTubular", ((BsonString)navigate(locatorUrl, 0)).asString().getValue());
        assertEquals("http://www.example.com/schema/anyURICTubular", ((BsonString)navigate(locatorUrl, 1)).asString().getValue());
        assertEquals("http://www.example.com/schema/anyURIDTubular", ((BsonString)navigate(locatorUrl, 2)).asString().getValue());

        extensionNameValues = (BsonArray) navigate(tubular, "ExtensionNameValue");

        assertEquals(1, extensionNameValues.size());

        extensionNameValue = (BsonDocument)extensionNameValues.get(0);
        assertEquals("I", navigate(extensionNameValue, "Name"));
        assertEquals("Any", navigate(extensionNameValue, "Value", "#attributes", "uom"));
        assertEquals("J", navigate(extensionNameValue, "Value", "#value"));
        assertEquals("api neutron", navigate(extensionNameValue, "MeasureClass"));
        assertEquals(DateUtils.toTimestamp("2025-12-30T14:09:34Z"), ((BsonDateTime)navigate(extensionNameValue, "DTim")).asDateTime().getValue());
        assertEquals(1000, ((BsonInt64)navigate(extensionNameValue, "Index")).getValue());
        assertEquals("Description in Extension Name Value @ Tubular", ((String)navigate(extensionNameValue, "Description")).trim());

        /////part 2

        drillingParam = (BsonDocument) drillingParams.get(1);

        assertEquals("required string 64 B", navigate(drillingParam, "#attributes", "uid"));
        assertEquals("a", navigate(drillingParam, "ETimOpBit", "#attributes", "uom"));
        assertEquals(0, ((BsonDouble)navigate(drillingParam, "ETimOpBit", "#value")).getValue(), 1E-6);

        mdHoleStart = (BsonDocument) navigate(drillingParam, "MdHoleStart");

        assertEquals("", navigate(mdHoleStart, "MeasuredDepth", "#attributes", "uom"));
        assertEquals(-.79, ((BsonDouble)navigate(mdHoleStart, "MeasuredDepth", "#value")).getValue(), 1E-6);

        datum = (BsonDocument) navigate(mdHoleStart, "Datum");

        assertEquals("123e4567-e89b-12d3-a456-42661417400A", navigate(datum, "Uuid"));
        assertEquals("Object version", navigate(datum, "ObjectVersion"));
        assertEquals("witsml21.Tubular", navigate(datum, "QualifiedType"));
        assertEquals("Second datum title", navigate(datum, "Title"));
        assertEquals("http://www.example.com/schema/anyURIK", navigate(datum, "EnergisticsUri"));

        locatorUrl = (BsonArray) navigate(datum, "LocatorUrl");

        assertEquals(1, locatorUrl.size());
        assertEquals("http://www.example.com/schema/anyURIL", ((BsonString)navigate(locatorUrl, 0)).asString().getValue());

        extensionNameValues = (BsonArray) navigate(datum, "ExtensionNameValue");

        assertEquals(1, extensionNameValues.size());

        extensionNameValue = (BsonDocument)extensionNameValues.get(0);
        assertEquals("w2", navigate(extensionNameValue, "Name"));
        assertEquals("testUom2", navigate(extensionNameValue, "Value", "#attributes", "uom"));
        assertEquals("x2", navigate(extensionNameValue, "Value", "#value"));
        assertEquals("y2", navigate(extensionNameValue, "Description"));

        assertEquals("N", navigate(drillingParam, "HkldRot", "#attributes", "uom"));
        assertEquals(8.19, ((BsonDouble)navigate(drillingParam, "HkldRot", "#value")).getValue(), 1E-6);

        assertEquals("TN", navigate(drillingParam, "OverPull", "#attributes", "uom"));
        assertEquals(1.001, ((BsonDouble)navigate(drillingParam, "OverPull", "#value")).getValue(), 1E-6);

        assertEquals("pN", navigate(drillingParam, "SlackOff", "#attributes", "uom"));
        assertEquals(12.0, ((BsonDouble)navigate(drillingParam, "SlackOff", "#value")).getValue(), 1E-6);

        assertEquals("gf", navigate(drillingParam, "HkldUp", "#attributes", "uom"));
        assertEquals(67, ((BsonDouble)navigate(drillingParam, "HkldUp", "#value")).getValue(), 1E-6);

        assertEquals("ozf", navigate(drillingParam, "HkldDn", "#attributes", "uom"));
        assertEquals(541.09, ((BsonDouble)navigate(drillingParam, "HkldDn", "#value")).getValue(), 1E-6);

        assertEquals("daN.m", navigate(drillingParam, "TqOnBotAv", "#attributes", "uom"));
        assertEquals(5, ((BsonDouble)navigate(drillingParam, "TqOnBotAv", "#value")).getValue(), 1E-6);

        assertEquals("J", navigate(drillingParam, "TqOnBotMx", "#attributes", "uom"));
        assertEquals(0.007, ((BsonDouble)navigate(drillingParam, "TqOnBotMx", "#value")).getValue(), 1E-6);

        assertEquals("pdl.ft", navigate(drillingParam, "TqOnBotMn", "#attributes", "uom"));
        assertEquals(0.5, ((BsonDouble)navigate(drillingParam, "TqOnBotMn", "#value")).getValue(), 1E-6);

        assertEquals("tonf[US].mi", navigate(drillingParam, "TqOffBotAv", "#attributes", "uom"));
        assertEquals(-1.89, ((BsonDouble)navigate(drillingParam, "TqOffBotAv", "#value")).getValue(), 1E-6);

        assertEquals("tonf[US].ft", navigate(drillingParam, "TqDhAv", "#attributes", "uom"));
        assertEquals(0.117, ((BsonDouble)navigate(drillingParam, "TqDhAv", "#value")).getValue(), 1E-6);

        assertEquals("uN", navigate(drillingParam, "WtAboveJar", "#attributes", "uom"));
        assertEquals(570, ((BsonDouble)navigate(drillingParam, "WtAboveJar", "#value")).getValue(), 1E-6);

        assertEquals("cN", navigate(drillingParam, "WtBelowJar", "#attributes", "uom"));
        assertEquals(180.67, ((BsonDouble)navigate(drillingParam, "WtBelowJar", "#value")).getValue(), 1E-6);

        assertEquals("MN", navigate(drillingParam, "WtMud", "#attributes", "uom"));
        assertEquals(781.678, ((BsonDouble)navigate(drillingParam, "WtMud", "#value")).getValue(), 1E-6);

        assertEquals("any uom here 5 (required)", navigate(drillingParam, "FlowratePumpAv", "#attributes", "uom"));
        assertEquals(1.1, ((BsonDouble)navigate(drillingParam, "FlowratePumpAv", "#value")).getValue(), 1E-6);

        assertEquals("any uom here 6 (required)", navigate(drillingParam, "FlowratePumpMx", "#attributes", "uom"));
        assertEquals(1.2, ((BsonDouble)navigate(drillingParam, "FlowratePumpMx", "#value")).getValue(), 1E-6);

        assertEquals("any uom here 7 (required)", navigate(drillingParam, "FlowratePumpMn", "#attributes", "uom"));
        assertEquals(1.3, ((BsonDouble)navigate(drillingParam, "FlowratePumpMn", "#value")).getValue(), 1E-6);

        assertEquals("1000 ft/h", navigate(drillingParam, "VelNozzleAv", "#attributes", "uom"));
        assertEquals(2.6, ((BsonDouble)navigate(drillingParam, "VelNozzleAv", "#value")).getValue(), 1E-6);

        assertEquals("MW", navigate(drillingParam, "PowBit", "#attributes", "uom"));
        assertEquals(.6, ((BsonDouble)navigate(drillingParam, "PowBit", "#value")).getValue(), 1E-6);

        assertEquals("any uom here 8 (required)", navigate(drillingParam, "PresDropBit", "#attributes", "uom"));
        assertEquals(1609, ((BsonDouble)navigate(drillingParam, "PresDropBit", "#value")).getValue(), 1E-6);

        assertEquals("1/2 ms", navigate(drillingParam, "CTimHold", "#attributes", "uom"));
        assertEquals(12.1, ((BsonDouble)navigate(drillingParam, "CTimHold", "#value")).getValue(), 1E-6);

        assertEquals("Ga[t]", navigate(drillingParam, "CTimSteering", "#attributes", "uom"));
        assertEquals(-300.20, ((BsonDouble)navigate(drillingParam, "CTimSteering", "#value")).getValue(), 1E-6);

        assertEquals("min", navigate(drillingParam, "CTimDrillRot", "#attributes", "uom"));
        assertEquals(6, ((BsonDouble)navigate(drillingParam, "CTimDrillRot", "#value")).getValue(), 1E-6);

        assertEquals("na", navigate(drillingParam, "CTimDrillSlid", "#attributes", "uom"));
        assertEquals(87.1, ((BsonDouble)navigate(drillingParam, "CTimDrillSlid", "#value")).getValue(), 1E-6);

        assertEquals("wk", navigate(drillingParam, "CTimCirc", "#attributes", "uom"));
        assertEquals(760, ((BsonDouble)navigate(drillingParam, "CTimCirc", "#value")).getValue(), 1E-6);

        assertEquals("ms", navigate(drillingParam, "CTimReam", "#attributes", "uom"));
        assertEquals(178999.176, ((BsonDouble)navigate(drillingParam, "CTimReam", "#value")).getValue(), 1E-6);

        assertEquals("m", navigate(drillingParam, "DistDrillRot", "#attributes", "uom"));
        assertEquals(.689, ((BsonDouble)navigate(drillingParam, "DistDrillRot", "#value")).getValue(), 1E-6);

        assertEquals("km", navigate(drillingParam, "DistDrillSlid", "#attributes", "uom"));
        assertEquals(64, ((BsonDouble)navigate(drillingParam, "DistDrillSlid", "#value")).getValue(), 1E-6);

        assertEquals("rod[US]", navigate(drillingParam, "DistReam", "#attributes", "uom"));
        assertEquals(182.7, ((BsonDouble)navigate(drillingParam, "DistReam", "#value")).getValue(), 1E-6);

        assertEquals("yd[Se]", navigate(drillingParam, "DistHold", "#attributes", "uom"));
        assertEquals(33.08, ((BsonDouble)navigate(drillingParam, "DistHold", "#value")).getValue(), 1E-6);

        assertEquals("yd[SeT]", navigate(drillingParam, "DistSteering", "#attributes", "uom"));
        assertEquals(90.01, ((BsonDouble)navigate(drillingParam, "DistSteering", "#value")).getValue(), 1E-6);

        assertEquals("dega/h", navigate(drillingParam, "RpmAv", "#attributes", "uom"));
        assertEquals(0.801, ((BsonDouble)navigate(drillingParam, "RpmAv", "#value")).getValue(), 1E-6);

        assertEquals("rpm", navigate(drillingParam, "RpmMx", "#attributes", "uom"));
        assertEquals(110, ((BsonDouble)navigate(drillingParam, "RpmMx", "#value")).getValue(), 1E-6);

        assertEquals("rad/s", navigate(drillingParam, "RpmMn", "#attributes", "uom"));
        assertEquals(0.1810, ((BsonDouble)navigate(drillingParam, "RpmMn", "#value")).getValue(), 1E-6);

        assertEquals("rev/s", navigate(drillingParam, "RpmAvDh", "#attributes", "uom"));
        assertEquals(879, ((BsonDouble)navigate(drillingParam, "RpmAvDh", "#value")).getValue(), 1E-6);

        assertEquals("1000 ft/h", navigate(drillingParam, "RopAv", "#attributes", "uom"));
        assertEquals(690, ((BsonDouble)navigate(drillingParam, "RopAv", "#value")).getValue(), 1E-6);

        assertEquals("knot", navigate(drillingParam, "RopMx", "#attributes", "uom"));
        assertEquals(870.0, ((BsonDouble)navigate(drillingParam, "RopMx", "#value")).getValue(), 1E-6);

        assertEquals("m/min", navigate(drillingParam, "RopMn", "#attributes", "uom"));
        assertEquals(18.7, ((BsonDouble)navigate(drillingParam, "RopMn", "#value")).getValue(), 1E-6);

        assertEquals("N", navigate(drillingParam, "WobAv", "#attributes", "uom"));
        assertEquals(8990, ((BsonDouble)navigate(drillingParam, "WobAv", "#value")).getValue(), 1E-6);

        assertEquals("kdyne", navigate(drillingParam, "WobMx", "#attributes", "uom"));
        assertEquals(7130.0, ((BsonDouble)navigate(drillingParam, "WobMx", "#value")).getValue(), 1E-6);

        assertEquals("Mgf", navigate(drillingParam, "WobMn", "#attributes", "uom"));
        assertEquals(11.81, ((BsonDouble)navigate(drillingParam, "WobMn", "#value")).getValue(), 1E-6);

        assertEquals("MN", navigate(drillingParam, "WobAvDh", "#attributes", "uom"));
        assertEquals(1.06, ((BsonDouble)navigate(drillingParam, "WobAvDh", "#value")).getValue(), 1E-6);

        assertEquals("F2", ((String)navigate(drillingParam, "ReasonTrip")).trim());

        assertEquals("G2", navigate(drillingParam, "ObjectiveBha"));

        assertEquals("0.001 seca", navigate(drillingParam, "AziTop", "#attributes", "uom"));
        assertEquals(1.271, ((BsonDouble)navigate(drillingParam, "AziTop", "#value")).getValue(), 1E-6);

        assertEquals("mila", navigate(drillingParam, "AziBottom", "#attributes", "uom"));
        assertEquals(41.81, ((BsonDouble)navigate(drillingParam, "AziBottom", "#value")).getValue(), 1E-6);

        assertEquals("Mrad", navigate(drillingParam, "InclStart", "#attributes", "uom"));
        assertEquals(0.017, ((BsonDouble)navigate(drillingParam, "InclStart", "#value")).getValue(), 1E-6);

        assertEquals("mrad", navigate(drillingParam, "InclMx", "#attributes", "uom"));
        assertEquals(1820.6, ((BsonDouble)navigate(drillingParam, "InclMx", "#value")).getValue(), 1E-6);

        assertEquals("gon", navigate(drillingParam, "InclMn", "#attributes", "uom"));
        assertEquals(10, ((BsonDouble)navigate(drillingParam, "InclMn", "#value")).getValue(), 1E-6);

        assertEquals("mina", navigate(drillingParam, "InclStop", "#attributes", "uom"));
        assertEquals(548, ((BsonDouble)navigate(drillingParam, "InclStop", "#value")).getValue(), 1E-6);

        assertEquals("K", navigate(drillingParam, "TempMudDhMx", "#attributes", "uom"));
        assertEquals(560, ((BsonDouble)navigate(drillingParam, "TempMudDhMx", "#value")).getValue(), 1E-6);

        assertEquals("required uom with legacy 3", navigate(drillingParam, "PresPumpAv", "#attributes", "uom"));
        assertEquals(1.6, ((BsonDouble)navigate(drillingParam, "PresPumpAv", "#value")).getValue(), 1E-6);

        assertEquals("required uom with legacy 4", navigate(drillingParam, "FlowrateBit", "#attributes", "uom"));
        assertEquals(19.1, ((BsonDouble)navigate(drillingParam, "FlowrateBit", "#value")).getValue(), 1E-6);

        assertEquals("oil-based", navigate(drillingParam, "MudClass"));
        assertEquals("aerated mud", navigate(drillingParam, "MudSubClass"));
        assertEquals("H2", ((String)navigate(drillingParam, "Comments")).trim());

        extensionNameValues = (BsonArray) navigate(drillingParam, "ExtensionNameValue");

        assertEquals(1, extensionNameValues.size());

        extensionNameValue = (BsonDocument) extensionNameValues.get(0);

        assertEquals("D2", navigate(extensionNameValue, "Name"));

        assertEquals("aaa", navigate(extensionNameValue, "Value", "#attributes", "uom"));
        assertEquals("E2", navigate(extensionNameValue, "Value", "#value"));
        assertEquals("absorbed dose", navigate(extensionNameValue, "MeasureClass"));
        assertEquals(DateUtils.toTimestamp("2025-09-30T01:09:34Z"), ((BsonDateTime)navigate(extensionNameValue, "DTim")).asDateTime().getValue());
        assertEquals(110, ((BsonInt64)navigate(extensionNameValue, "Index")).getValue());
        assertEquals("F2", navigate(extensionNameValue, "Description"));

        tubular = (BsonDocument) navigate(drillingParam, "Tubular");
        assertEquals("123e4567-e89b-12d3-a456-426614174003", navigate(tubular, "Uuid"));
        assertEquals("G2", navigate(tubular, "ObjectVersion"));
        assertEquals("resqml41.xy", navigate(tubular, "QualifiedType"));
        assertEquals("H2", navigate(tubular, "Title"));
        assertEquals("http://www.example.com/schema/anyURITubular2", navigate(tubular, "EnergisticsUri"));

        locatorUrl = (BsonArray) navigate(tubular, "LocatorUrl");

        assertEquals(1, locatorUrl.size());
        assertEquals("http://www.example.com/schema/anyURIBTubularB", ((BsonString)navigate(locatorUrl, 0)).asString().getValue());

        extensionNameValues = (BsonArray) navigate(tubular, "ExtensionNameValue");

        assertEquals(1, extensionNameValues.size());

        extensionNameValue = (BsonDocument)extensionNameValues.get(0);
        assertEquals("I2", navigate(extensionNameValue, "Name"));
        assertEquals("bbb", navigate(extensionNameValue, "Value", "#attributes", "uom"));
        assertEquals("J2", navigate(extensionNameValue, "Value", "#value"));
        assertEquals("absorbed dose", navigate(extensionNameValue, "MeasureClass"));
        assertEquals(DateUtils.toTimestamp("2025-12-30T22:09:34Z"), ((BsonDateTime)navigate(extensionNameValue, "DTim")).asDateTime().getValue());
        assertEquals(900, ((BsonInt64)navigate(extensionNameValue, "Index")).getValue());
        assertEquals("K2", navigate(extensionNameValue, "Description"));
    }

    @Test
    public void wellboreInBhaRunTest() throws Exception {
        BsonDocument wellbore = (BsonDocument) navigate(this.bhaRunDocument, "BhaRun", "Wellbore");

        assertEquals("123e4567-e89b-12d3-a456-426614174005", navigate(wellbore, "Uuid"));
        assertEquals("Object Version @ Wellbore", navigate(wellbore, "ObjectVersion"));
        assertEquals("prodml11.ab", navigate(wellbore, "QualifiedType"));
        assertEquals("Title at wellbore", navigate(wellbore, "Title"));
        assertEquals("http://www.example.com/schema/anyURIWellbore", navigate(wellbore, "EnergisticsUri"));

        BsonArray locatorUrl = (BsonArray) navigate(wellbore, "LocatorUrl");

        assertEquals(1, locatorUrl.size());

        assertEquals("http://www.example.com/schema/anyURIWellboreA", ((BsonString)navigate(locatorUrl.get(0))).asString().getValue());

        BsonArray extensionNameValues = (BsonArray) navigate(wellbore, "ExtensionNameValue");

        assertEquals(1, extensionNameValues.size());

        BsonValue extensionNameValue = extensionNameValues.get(0);

        assertEquals("WName", navigate(extensionNameValue, "Name"));
        assertEquals("wUOM", navigate(extensionNameValue, "Value", "#attributes", "uom"));
        assertEquals("P", navigate(extensionNameValue, "Value", "#value"));
        assertEquals("attenuation per frequency interval", navigate(extensionNameValue, "MeasureClass"));
        assertEquals(DateUtils.toTimestamp("2025-06-10T05:09:34Z"), ((BsonDateTime)navigate(extensionNameValue, "DTim")).asDateTime().getValue());
        assertEquals(1234567890, ((BsonInt64)navigate(extensionNameValue, "Index")).getValue());
        assertEquals("Q", navigate(extensionNameValue, "Description"));
    }

    @Test
    public void tubularAtBhaRunTest() throws Exception {
        BsonDocument tubular = (BsonDocument) navigate(this.bhaRunDocument, "BhaRun", "Tubular");

        assertEquals("F23e4567-e89b-12d3-a456-426614174A03", navigate(tubular, "Uuid"));
        assertEquals("&R >", navigate(tubular, "ObjectVersion"));
        assertEquals("eml71.BhaRun", navigate(tubular, "QualifiedType"));
        assertEquals("&ABC test A > b", navigate(tubular, "Title"));
        assertEquals("http://www.example.com/schema/anyURITubular", navigate(tubular, "EnergisticsUri"));

        BsonArray locatorUrl = (BsonArray) navigate(tubular, "LocatorUrl");

        assertEquals(1, locatorUrl.size());
        assertEquals("http://www.example.com/schema/anyURITubularA", locatorUrl.get(0).asString().getValue());

        BsonArray extensionNameValues = (BsonArray) navigate(tubular, "ExtensionNameValue");

        assertEquals(1, extensionNameValues.size());

        BsonValue extensionNameValue = extensionNameValues.get(0);

        assertEquals("Tubular extension name value", navigate(extensionNameValue, "Name"));

        assertEquals("any uom", navigate(extensionNameValue, "Value", "#attributes", "uom"));
        assertEquals("U", navigate(extensionNameValue, "Value", "#value"));
        assertEquals("dipole moment", navigate(extensionNameValue, "MeasureClass"));
        assertEquals(DateUtils.toTimestamp("2025-12-30T10:19:34Z"), ((BsonDateTime)navigate(extensionNameValue, "DTim")).asDateTime().getValue());
        assertEquals(32, ((BsonInt64)navigate(extensionNameValue, "Index")).getValue());
        assertEquals("V", navigate(extensionNameValue, "Description"));
    }
}
