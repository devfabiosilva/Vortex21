package org.w21parser.strictObject;

import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w21parser.W21Exception;
import org.w21parser.W21ParserLoader;
import org.w21parser.common.*;

import java.util.List;

import static org.junit.Assert.assertEquals;
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
        ArrayOfExtensionNameValue.build(
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
    }
}
