package org.w21parser;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.w21parser.VortexNativeBindingTest.fromPath;
import static org.w21parser.W21ParserLoader.W21Object.*;

public class Vortex21StrictValidationTest {
    private static final Logger logger = LoggerFactory.getLogger(Vortex21StrictValidationTest.class);

    List<W21ParserLoader.W21Object> witsml21List = List.of(
            BhaRun,
            CementJob,
            CementJobEvaluation,
            Channel,
            ChannelKind,
            ChannelKindDictionary,
            ChannelSet,
            CuttingsGeology,
            CuttingsGeologyInterval,
            DepthRegImage,
            DownholeComponent,
            DrillReport,
            ErrorTerm,
            ErrorTermDictionary,
            FluidsReport,
            InterpretedGeology,
            InterpretedGeologyInterval,
            Log,
            LoggingToolKind,
            LoggingToolKindDictionary,
            MudLogReport,
            MudlogReportInterval,
            OpsReport,
            PPFGChannel,
            PPFGChannelSet,
            PPFGLog,
            Rig,
            RigUtilization,
            Risk,
            ShowEvaluation,
            ShowEvaluationInterval,
            StimJob,
            StimJobStage,
            StimPerforationCluster,
            SurveyProgram,
            Target,
            ToolErrorModel,
            ToolErrorModelDictionary,
            Trajectory,
            TrajectoryStation,
            Tubular,
            WeightingFunction,
            WeightingFunctionDictionary,
            Well,
            Wellbore,
            WellboreCompletion,
            WellboreGeology,
            WellboreGeometry,
            WellboreGeometrySection,
            WellboreMarker,
            WellboreMarkerSet,
            WellCMLedger,
            WellCompletion
    );

    private W21ParserLoader parser1;
    private int expectedCloseStatusParser1 = 0;

    @Before
    public void setUp() throws Exception {
        this.parser1 = W21ParserLoader.begin().withInputRulesValidator().withInputWitsmlStrict().withResourceStats().withIgnoreInputWitsmlNS().build();
    }

    @After
    public void tearDown() {
        assertEquals("Parser 1 close method must return " + this.expectedCloseStatusParser1, this.expectedCloseStatusParser1, this.parser1.close());
    }

    @Test
    public void validateStrinctObjects() throws IOException, W21Exception {
        for (W21ParserLoader.W21Object object : witsml21List) {
            String objectName = object.toString();
            String fullObjectNamePath = fromPath(objectName);
            logger.info("Load and validate from file \"{}\" in normal mode", fullObjectNamePath);
            try {
                parser1.readFromFile(fullObjectNamePath, object);
                assertEquals(objectName, parser1.getInputObjectName());
            } catch (W21Exception e) {
                logger.error("Witsml 2.1 error code: {}\n", e.error);
                logger.error("Main message: \n{}\n", e.getMessage());
                logger.error("Fault String: \n{}\n", e.getFaultstring());
                logger.error("Fault String XML: \n{}\n", e.getXMLfaultdetail());
                throw e;
            }
        }
    }

    @Test
    public void validateStrinctFieldsInAutoDetectMode() throws IOException, W21Exception {
        assertEquals("Total objects must be 53", 53, witsml21List.size());
        for (W21ParserLoader.W21Object object : witsml21List) {
            String objectName = object.toString();
            String fullObjectNamePath = fromPath(objectName);
            logger.info("Load and validate from file \"{}\" in AutoDetect mode", fullObjectNamePath);
            try {
                parser1.readFromFile(fullObjectNamePath);
                assertEquals(objectName, parser1.getInputObjectName());
            } catch (W21Exception e) {
                logger.error("Witsml 2.1 error code: {}\n", e.error);
                logger.error("Main message: \n{}\n", e.getMessage());
                logger.error("Fault String: \n{}\n", e.getFaultstring());
                logger.error("Fault String XML: \n{}\n", e.getXMLfaultdetail());
                throw e;
            }
        }
    }
}
