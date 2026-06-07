package org.w21parser.strictObject;

import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w21parser.W21Exception;
import org.w21parser.W21ParserLoader;
import org.w21parser.common.*;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.w21parser.Vortex21StrictValidationTest.printW21Exception;
import static org.w21parser.VortexNativeBindingTest.fromPath;
import static org.w21parser.common.Utils.*;
import static org.w21parser.strictObject.BhaRunTest.navigate;

public class WellTest {
    private static final Logger logger = LoggerFactory.getLogger(WellTest.class);
    private W21ParserLoader parser1;
    private BsonDocument wellDocument = null;

    @Before
    public void setUp() throws Exception {
        this.parser1 = W21ParserLoader.begin().withInputRulesValidator().withInputWitsmlStrict().withResourceStats().withIgnoreInputWitsmlNS().build();
        try {
            this.parser1.readFromFile(fromPath("Well"), W21ParserLoader.W21Object.Well);
        } catch (W21Exception e) {
            printW21Exception(logger, e);
            throw e;
        }
        try {
            this.wellDocument = (BsonDocument) this.parser1.parse(W21ParserLoader.W21OutputType.BSON);
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
    public void aliasesTest() throws Exception {
        Aliases.build((BsonArray) navigate(this.wellDocument, "Well", "Aliases"))
                .add(
                "A",
                "Identifier A",
                "IdentifierKind A",
                "Description A",
                "2021-02-03T04:05:06Z",
                "2021-09-25T19:29:41Z"
                )
                .add(
                        "B",
                        "Identifier B",
                        "IdentifierKind B",
                        "Description B",
                        "2023-02-03T04:05:06Z",
                        "2023-09-25T19:29:41Z"
                ).test();
    }

    @Test
    public void citationTest() throws Exception {
        Citation.build(
                "Title Citation",
                "Originator A",
                "2016-12-15T12:25:38Z",
                "Format test",
                "Editor test",
                "2016-02-05T19:39:47Z",
                "Desc",
                List.of(
                        "Edt Hst 1",
                        "Edt Hst 2",
                        "Edt Hst 3"
                ),
                "Desc words with max 2000 characters long. Testing this message length",
                (BsonDocument)navigate(this.wellDocument, "Well", "Citation")
        ).test();
    }

    @Test
    public void osduIntegrationTest() throws Exception {
        OSDUIntegration.build(
                List.of(
                        OSDULineageAssertion.build(
                                "ID_A",
                                "indirect"
                        ),
                        OSDULineageAssertion.build(
                                "ID_B",
                                "direct"
                        ),
                        OSDULineageAssertion.build(
                                "ID_C",
                                "reference"
                        )
                ),
                List.of(
                        "OwnerGroup test 1",
                        "OwnerGroup test 2",
                        "OwnerGroup test 3",
                        "OwnerGroup test 4"
                ),
                List.of(
                        "ViewerGroup test 1",
                        "ViewerGroup test 2",
                        "ViewerGroup test 3",
                        "ViewerGroup test 4",
                        "ViewerGroup test 5"
                ),
                List.of(
                        "LegalTags test 1",
                        "LegalTags test 2",
                        "LegalTags test 3"
                ),
                "{\"test\": 1234}",
                PlaneAngleMeasure.build(
                        "0.001 seca",
                        1.2
                ),
                PlaneAngleMeasure.build(
                        "rev",
                        10.0331
                ),
                OSDUSpatialLocationIntegration.build(
                        "2013-03-12T20:53:21Z",
                        "QuantitativeAccuracyBand test",
                        "QualitativeSpatialAccuracyType test",
                        "CoordinateQualityCheckPerformedBy test",
                        "2014-02-05T18:39:47Z",
                        List.of(
                                "CoordinateQualityCheckRemark test 1",
                                "CoordinateQualityCheckRemark test 2",
                                "CoordinateQualityCheckRemark test 3"
                        ),
                        List.of(
                                "AppliedOperation test 1",
                                "AppliedOperation test 2",
                                "AppliedOperation test 3"
                        )
                ),
                "Field test",
                "Country test",
                "State test",
                "County test",
                "City test",
                "Region test",
                "District test",
                "Block test",
                "Prospect test",
                "Play test",
                "Basin test",
                (BsonDocument) navigate(this.wellDocument, "Well", "OSDUIntegration")
        ).test();
    }

    @Test
    public void wellCustomDataTest() throws Exception {
        CustomData.build(
                List.of(
                        "<testElement namespace=\"http://example.com\">\n" +
                        "     <complexElement att=\"my attr\">\n" +
                        "      <value>12.456</value>\n" +
                        "      <name>NameStr</name>\n" +
                        "     </complexElement>\n" +
                        "    </testElement>",
                        "<testElement2>\n" +
                        "     Simple element\n" +
                        "    </testElement2>"
                ),
                (BsonDocument) navigate(this.wellDocument, "Well")
        ).test();
    }

    @Test
    public void wellExtensionNameValueTest() throws Exception {
        ArrayOf.build(
                List.of(
                        ExtensionNameValue.build(
                                "Name A",
                                "ValueUomA",
                                "ValueA",
                                "absorbed dose",
                                "2022-04-16T22:20:17Z",
                                1870L,
                                "Desc A"
                        ),
                        ExtensionNameValue.build(
                                "Name B",
                                "ValueUomB",
                                "ValueB",
                                "thermal diffusivity",
                                "2021-03-16T22:20:17Z",
                                187651L,
                                "Desc B"
                        )
                ),
                (BsonArray) navigate(this.wellDocument, "Well", "ExtensionNameValue")
        ).test();
    }

    @Test
    public void wellFacilityLifecyclePeriodTest() throws Exception {
        ArrayOf.build(
                List.of(
                        FacilityLifecyclePeriod.build(
                                "State in LifeCycleHistory 1",
                                "2016-04-03T19:26:47Z",
                                "2016-05-02T19:29:47Z"
                        ),
                        FacilityLifecyclePeriod.build(
                                "State in LifeCycleHistory 2",
                              "2015-04-03T19:26:47Z",
                                "2015-05-02T19:29:47Z"
                        )
                ),
                (BsonArray) navigate(this.wellDocument, "Well", "LifeCycleHistory")
        ).test();
    }

    @Test
    public void wellOperatorTest() throws Exception {
        DataObjectReference.build(
                "173e4567-e89b-12d3-a456-4266141740f2",
                "ObjectVersion in Operator",
                "witsml21.Well",
                "Title in Operator",
                "http://www.example.com/schema/anyURIOperatorA",
                List.of(
                        "http://www.example.com/schema/anyURIOperatorA1",
                        "http://www.example.com/schema/anyURIOperatorA2"
                ),
                List.of(
                        ExtensionNameValue.build(
                                "Name A",
                                "UomA",
                                "ValueA",
                                "electric field strength",
                                "2021-08-05T19:39:47Z",
                                881777L,
                                "Operator Desc A"
                        ),
                        ExtensionNameValue.build(
                                "Name B",
                                "UomB",
                                "ValueB",
                                "force per volume",
                                "2021-07-02T19:39:27Z",
                                8777L,
                                "Operator Desc A"
                        )
                ),
                (BsonDocument) navigate(this.wellDocument, "Well", "Operator")
        ).test();
    }

    @Test
    public void wellOriginalOperatorTest() throws Exception {
        DataObjectReference.build(
                "12ce4567-ed9b-12d3-a456-476614174e03",
                "ObjectVersion in OriginalOperator",
                "prodml10.BhaRun",
                "Title in OriginalOperator",
                "http://www.example.com/schema/anyURIOriginalOperatorA",
                List.of(
                        "http://www.example.com/schema/anyURIOriginalOperatorA1"
                ),
                List.of(
                        ExtensionNameValue.build(
                                "ENV NAME A",
                                "ENVUomA",
                                "ENVVALUEA",
                                "absorbed dose",
                                "2023-11-07T18:27:36Z",
                                7189198L,
                                "Oper Desc"
                        )
                ),
                (BsonDocument) navigate(this.wellDocument, "Well", "OriginalOperator")
        ).test();
    }

    @Test
    public void wellOperatorHistoryTest() throws Exception {
        ArrayOf.build(
            List.of(
                FacilityOperator.build(
                    DataObjectReference.build(
                            "723e4567-e89b-13d3-a456-426614174d0f",
                            "ObjectVersion in BusinessAssociate",
                            "eml11.xyz",
                            "Title in BusinessAssociate",
                            "http://www.example.com/schema/anyURIBusinessAssociateA",
                            List.of(
                                    "http://www.example.com/schema/anyURIBusinessAssociateA1",
                                    "http://www.example.com/schema/anyURIBusinessAssociateA2"
                            ),
                            List.of(
                                    ExtensionNameValue.build(
                                            "ENV1Name",
                                            "UomA1",
                                            "ValuaA1",
                                            "luminous flux",
                                            "2016-02-05T19:39:47Z",
                                            81762L,
                                            "Desc AB"
                                    )
                            )
                    ),
                    "2016-01-05T19:39:47Z",
                    "2016-02-07T11:29:48Z"
                ),
                FacilityOperator.build(
                        DataObjectReference.build(
                                "853e4568-e89b-13d3-a456-426614174d0f",
                                "ObjectVersion in BusinessAssociate2",
                                "eml12.xyza",
                                "Title in BusinessAssociate2",
                                "http://www.example.com/schema/anyURIBusinessAssociateB",
                                List.of(
                                        "http://www.example.com/schema/anyURIBusinessAssociateB1",
                                        "http://www.example.com/schema/anyURIBusinessAssociateB2"
                                ),
                                List.of(
                                        ExtensionNameValue.build(
                                                "ENV2Name",
                                                "UomB1",
                                                "ValuaB1",
                                                "magnetic vector potential",
                                                "2016-12-15T19:39:47Z",
                                                9176176L,
                                                "Desc ABABCD"
                                        )
                                )
                        ),
                        "2016-01-05T19:39:47Z",
                        "2016-02-07T11:29:48Z"
                )
            ),
                (BsonArray) navigate(this.wellDocument, "Well", "OperatorHistory")
        ).test();
    }

    @Test
    public void wellStatusHistoryTest() throws Exception {
        ArrayOf.build(
                List.of(
                        WellStatusPeriod.build(
                                "abandoned",
                                "2025-01-05T19:39:47Z",
                                "2025-05-15T09:29:27Z"
                        ),
                        WellStatusPeriod.build(
                                "working over",
                                "2026-01-15T19:39:47Z",
                                "2026-04-07T19:39:47Z"
                        )
                ),
                (BsonArray) navigate(this.wellDocument, "Well", "StatusHistory")
        ).test();
    }

    @Test
    public void wellPurposeHistoryTest() throws Exception {
        ArrayOf.build(
                List.of(
                        WellPurposePeriod.build(
                                "general srvc -- borehole re-acquisition",
                                "2023-07-01T20:21:12Z",
                                "2024-01-06T22:43:42Z"
                        ),
                        WellPurposePeriod.build(
                                "development -- infill development",
                                "2025-07-01T20:21:12Z",
                                "2026-01-06T22:43:42Z"
                        )
                ),
                (BsonArray) navigate(this.wellDocument, "Well", "PurposeHistory")
        ).test();
    }

    @Test
    public void wellInformationalGeographicLocationWGS84Test() throws Exception {
        Geographic2dPosition.build(
                PlaneAngleMeasureExt.build("LatitudeUom", 0.187),
                PlaneAngleMeasureExt.build("LongitudeUom", 172.01),
                0.9917,
                DataObjectReference.build(
                        "123e4f67-e8db-12d3-a456-426614174015",
                        "GeographicCrs ObjectVersion",
                        "custom21.abc",
                        "GeographicCrs title",
                        "http://www.example.com/schema/anyURIGeographicCrsA",
                        List.of(
                                "http://www.example.com/schema/anyURIGeographicCrsA1",
                                "http://www.example.com/schema/anyURIGeographicCrsA2"
                        ),
                        List.of(
                                ExtensionNameValue.build(
                                        "GeographicCrs Name A",
                                        "GeographicCrsUomA",
                                        "GeographicCrsValueA",
                                        "absorbed dose",
                                        "2022-10-25T20:39:47Z",
                                        777888L,
                                        "GeographicCrs Desc"
                                )
                        )
                ),
                (BsonDocument) navigate(this.wellDocument, "Well", "InformationalGeographicLocationWGS84")
        ).test();
    }

    @Test
    public void wellInformationalProjectedLocationTest() throws Exception {
        Projected2dPosition.build(
                18.01,
                20.0,
                DataObjectReference.build(
                        "c2fe4567-e89b-12d3-a456-426614174006",
                        "ProjectedCrs ObjectVersion",
                        "resqml32.xy",
                        "ProjectedCrs title",
                        "http://www.example.com/schema/anyURIProjectedCrsA",
                        List.of(
                                "http://www.example.com/schema/anyURIProjectedCrsA1"
                        ),
                        List.of(
                                ExtensionNameValue.build(
                                        "ProjectedCrs ENV NAME",
                                        "ProjectedCrsUomA",
                                        "ProjectedCrsValueA",
                                        "electric conductivity",
                                        "2023-11-15T11:29:27Z",
                                        18701988L,
                                        "DescABV"
                                )
                        )
                ),
                (BsonDocument) navigate(this.wellDocument, "Well", "InformationalProjectedLocation")
        ).test();
    }

    @Test
    public void wellDataSourceOrganizationTest() throws Exception {
        DataObjectReference.build(
                "123e4567-e89b-12d3-a456-426614174fc2",
                "DataSourceOrganization ObjectVersion",
                "custom81.test",
                "DataSourceOrganization title",
                "http://www.example.com/schema/anyURIDataSourceOrganizationA",
                List.of(
                        "http://www.example.com/schema/anyURIDataSourceOrganizationA1",
                        "http://www.example.com/schema/anyURIDataSourceOrganizationA2"
                ),
                List.of(
                        ExtensionNameValue.build(
                                "ENV A 1 NAME",
                                "UomA1",
                                "ValueA1",
                                "attenuation per frequency interval",
                                "2022-03-02T18:28:17Z",
                                99999L,
                                "DESC ABCD"
                        ),
                        ExtensionNameValue.build(
                                "ENV B 2 NAME",
                                "UomB2",
                                "ValueB2",
                                "cation exchange capacity",
                                "2021-01-15T19:30:27Z",
                                19287674318L,
                                "DESC XYZABC"
                        )
                ),
                (BsonDocument) navigate(this.wellDocument, "Well", "DataSourceOrganization")
        ).test();
    }

    @Test
    public void wellAbstractWellheadElevationTest() throws Exception {
        BsonDocument wellheadElevation = (BsonDocument) navigate(this.wellDocument, "Well", "WellheadElevation");
        testString("rdw212:DatumElevation", "#abstype", wellheadElevation);
        DatumElevation.build(
                PlaneAngleMeasureExt.build(
                        "WellheadElevationUom",
                        0.199826
                ),
                DataObjectReference.build(
                        "b23e4537-e89b-a2d3-a456-426614174ff2",
                        "WellheadElevation ObjectVersion",
                        "custom99.testA",
                        "WellheadElevation title",
                        "http://www.example.com/schema/anyURIWellheadElevationA",
                        List.of(
                                "http://www.example.com/schema/anyURIDataWellheadElevationA1",
                                "http://www.example.com/schema/anyURIWellheadElevationA2"
                        ),
                        List.of(
                                ExtensionNameValue.build(
                                        "ENV WellheadElevation NAME",
                                        "WellheadElevationUomA1",
                                        "WellheadElevationValueA1",
                                        "api gamma ray",
                                        "2020-02-01T10:28:17Z",
                                        1880999L,
                                        "DESC ABCD WellheadElevation"
                                ),
                                ExtensionNameValue.build(
                                        "ENV WellheadElevation B 2 NAME",
                                        "WellheadElevationUomB2",
                                        "WellheadElevationValueB2",
                                        "diffusion coefficient",
                                        "2023-05-15T13:28:21Z",
                                        189127674312L,
                                        "DESC XYZABC DEFG"
                                )
                        )
                ),
                wellheadElevation
        ).test();
    }

    @Test
    public void wellAbstractGroundElevationTest() throws Exception {
        BsonDocument groundElevation = (BsonDocument)navigate(this.wellDocument, "Well", "GroundElevation");
        testString("rdw212:ReferencePointElevation", "#abstype", groundElevation);
        ReferencePointElevation.build(
                PlaneAngleMeasureExt.build(
                        "GroundElevationUom",
                        127.11
                ),
                DataObjectReference.build(
                        "cf3e4591-e89b-a2d3-a456-416614874ff2",
                        "GroundElevation ObjectVersion",
                        "eml99.testB",
                        "GroundElevation title",
                        "http://www.example.com/schema/anyGroundElevationA",
                        List.of(
                                "http://www.example.com/schema/anyURIGroundElevationA1",
                                "http://www.example.com/schema/anyURIGroundElevationA2"
                        ),
                        List.of(
                                ExtensionNameValue.build(
                                        "ENV GroundElevation NAME",
                                        "GroundElevationA1",
                                        "GroundElevationValueA1",
                                        "volume",
                                        "2016-02-01T10:28:17Z",
                                        2870998L,
                                        "DESC ABCD GroundElevation A"
                                ),
                                ExtensionNameValue.build(
                                        "ENV GroundElevation B 2 NAME",
                                        "GroundElevationUomB2",
                                        "GroundElevationValueB2",
                                        "unitless",
                                        "2012-02-15T13:28:21Z",
                                        98912711674L,
                                        "DESC XYZABC DEFG unitless B"
                                )
                        )
                ),
                groundElevation
        ).test();
    }

    @Ignore
    @Test
    public void wellAbstractArrayOfWellSurfaceLocationTest() throws Exception {
        BsonArray arrayOfWellSurfaceLocation = (BsonArray) navigate(this.wellDocument, "Well", "WellSurfaceLocation");
        assertNotNull(arrayOfWellSurfaceLocation);
        assertEquals(1, arrayOfWellSurfaceLocation.size());

        BsonDocument item = arrayOfWellSurfaceLocation.get(0).asDocument();
        assertEquals(1, item.size());
        testString("rdw212:AbstractPosition", "#abstype", item);
        //TODO refactor C code for abstract this object
    }

    @Test
    public void wellMiscTest() throws Exception {
        BsonDocument well = (BsonDocument)navigate(this.wellDocument, "Well");
        testString("Exst", "Existence", well);
        testString(
                "Obj Version with max 2000 chars. Testing this message length",
                "ObjectVersionReason",
                well
        );
        testStringList(
                List.of(
                        "BusinessActivityHistory test 1",
                        "BusinessActivityHistory test 2",
                        "BusinessActivityHistory test 3"
                ),
                "BusinessActivityHistory", well
        );
        testString("inactive", "ActiveStatus", well);
        testString("UniqueIdentifier test", "UniqueIdentifier", well);
        testString("NameLegal test", "NameLegal", well);
        testString("NumGovt test", "NumGovt", well);
        testString("NumAPI test", "NumAPI", well);
        testString("OperatingEnvironment test", "OperatingEnvironment", well);
        testString("+23:01","TimeZone", well);
        testString("Basin test", "Basin", well);
        testString("Play test", "Play", well);
        testString("Prospect test", "Prospect", well);
        testString("Field test", "Field", well);
        testString("Country test", "Country", well);
        testString("State test", "State", well);
        testString("County test", "County", well);
        testString("Region test", "Region", well);
        testString("District test", "District", well);
        testString("NumLicense test", "NumLicense", well);
        testDateTime("2021-05-05T19:19:38Z", "DTimLicense", well);
        ArrayOf.build(
                List.of(
                        LicensePeriod.build(
                            "NumLicense test 1",
                            "2021-07-19T22:39:47Z",
                            "2022-04-01T11:19:17Z"
                        ),
                        LicensePeriod.build(
                                "NumLicense test 2",
                                "2023-07-19T22:39:47Z",
                                "2024-04-01T11:19:17Z"
                        )
                ),
                (BsonArray) navigate(well, "LicenseHistory")
        ).test();

        testString("Block test", "Block", well);
        testString("InterestType test", "InterestType", well);
        testStringAttribute("ppk", "uom", (BsonDocument) navigate(well, "PcInterest"));
        testDouble(17.0211, "#value", (BsonDocument) navigate(well, "PcInterest"));
        testString("SlotName test", "SlotName", well);
        testString("LifeCycleState test", "LifeCycleState", well);
        testString("OperatorDiv Test", "OperatorDiv", well);
        testString("temporarily abandoned", "StatusWell", well);
        testString("general srvc -- research -- strat test", "PurposeWell", well);
        testString("non HC gas -- CO2", "FluidWell", well);
        testString("huff-n-puff", "DirectionWell", well);
        testDateTime("2021-01-15T19:39:47Z", "DTimSpud", well);
        testDateTime("2022-03-05T23:29:57Z", "DTimPa", well);
        testStringAttribute("link[US]", "uom", (BsonDocument) navigate(well, "WaterDepth"));
        testDouble(1827.119801, "#value", (BsonDocument) navigate(well, "WaterDepth"));
    }
}
