package org.w21parser;

import org.bson.BsonBinaryReader;

import org.bson.BsonDocument;
import org.bson.ByteBufNIO;
import org.bson.RawBsonDocument;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.RawBsonDocumentCodec;
import org.bson.io.ByteBufferBsonInput;
import org.w21parser.telemetry.W21TelemetrySummary21;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.w21parser.W21Errors.W21_INVALID_BSON_PARSING_OPTION;

public class W21ParserLoader {

    static {
        try {
            System.loadLibrary("w21parser");
        } catch (Throwable e) {
            System.out.println("JNI example load library error.");
            System.out.println(e.getMessage());
            throw e;
        }
    }
    /*
    public static void main(String[] args) {
        W21ParserLoader parser1;
        try {
            parser1 = W21ParserLoader.begin().
                    withInputRulesValidator().withInputWitsmlStrict().
                    withResourceStats().withIgnoreInputWitsmlNS().build();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return;
        }

        try {
            parser1.readFromFile("../../xmls/OpsReport2.xml");

            Object obj = parser1.parse();
            System.out.println("Obj " + obj.toString());
            System.out.println(parser1.loadStatistics());
        } catch (W21Exception e) {
            System.out.println("Error " + e.getMessage() + "\nfaultdetail: " + e.getFaultstring() + "\nxmlfaultdetail: " + e.getXMLfaultdetail());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            if (parser1 != null)
                parser1.close();
        }
    }
    */

    public static void main(String[] args) throws Exception {
        int numThreads = 2;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        for (int i = 0; i < numThreads; i++) {
            executor.submit(() -> {
                // 1. instance
                W21ParserLoader loader;
                try {
                    loader = W21ParserLoader.begin().withInputRulesValidator().withInputWitsmlStrict().withIgnoreInputWitsmlNS().withResourceStats().build();
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
                try {

                    // 2. Initialize JNI
                    // loader.jniInit();

                    System.out.println("=== Iniciando Teste de Stress WITSML 2.1 ===");

                    // 3. Call Benchmark
                    loader.benchmarkPerformanceRealista();

                    // 4. Close with security
                    // loader.jniClose();

                    System.out.println("=== Teste Finalizado com Sucesso ===");

                } catch (Exception e) {
                    System.err.println("Erro durante a execução do benchmark:");
                    e.printStackTrace();
                } finally {
                    loader.close();
                }
            });
        }
        executor.shutdown();
        //executor.awaitTermination(10, TimeUnit.MINUTES);
    }

    private final CharsetEncoder encoder;
    private final int INITIAL_BYTE_BUFFER_SIZE = 1024*1024; // 1 KB
    private final RawBsonDocumentCodec BSON_CODEC;
    private volatile ByteBuffer bbBson; //output
    private volatile ByteBuffer bbJson; // output
    private ByteBuffer bbXml; // input

    private long jniHandler = 0;
    private long jniHandlerCheck = 0;
    private boolean inputRulesValidatorEnable = false;
    private boolean hasResourceStats = false;
    private long inputConfig = 0;
    private long outputConfig = 0;

    private W21Object inputObject = W21Object.AUTODETECT;

    private native String jniInGetObjectName() throws W21Exception;
    private native void jniInit(long inputConfig, long outputConfig, boolean hasResourceStats) throws W21Exception;
    private native int jniClose();

    private native int jniLoadSoapXmlStrict();
    private native int jniLoadSoapXmlIgnoreNS();
    private native void jniInputRulesValidatorEnable() throws W21Exception;
    private native void jniInputRulesValidatorDisable();

    public String getInputObjectName() throws W21Exception {
        return this.jniInGetObjectName();
    }
    /**
     * Reads and parses a WITSML 2.1 document from a memory buffer using <b>AutoDetect</b> mode.
     * * <p>This native method identifies the underlying WITSML object type automatically
     * before processing. For cases where the object type is already known (e.g., BhaRun),
     * specific methods such as {@code jniReadBhaRunFromByteBuffer} should be preferred
     * for better performance or strict validation.</p>
     *
     * <p>The native layer performs the following operations:</p>
     * <ul>
     * <li><b>Identification:</b> Auto-detects and loads the WITSML 2.1 object type within the XML.</li>
     * <li><b>Validation:</b> Validates the XML integrity and schema compliance.</li>
     * <li><b>Parsing:</b> Maps the validated data into the corresponding C structures.</li>
     * </ul>
     *
     * @param bbXml The {@link ByteBuffer} containing the WITSML 2.1 XML content.
     * @param limit The maximum number of bytes to be read from the buffer.
     * @throws W21Exception If the object type cannot be detected, if validation fails,
     * or if a parsing error occurs within the gSOAP engine.
     * @see #jniParse()
     * @see #jniParseJson()
     */
    private native void jniReadFromByteBuffer(ByteBuffer bbXml, long limit) throws W21Exception;

    /**
     * Reads and parses a WITSML 2.1 document from a memory buffer with <b>BhaRun</b> object.
     *
     * <p>The native layer performs the following operations:</p>
     * <ul>
     * <li><b>Identification:</b> Loads the WITSML 2.1 object type within the XML.</li>
     * <li><b>Validation:</b> Validates the XML integrity and schema compliance.</li>
     * <li><b>Parsing:</b> Maps the validated data into the corresponding C structures.</li>
     * </ul>
     *
     * @param bbXml The {@link ByteBuffer} containing the WITSML 2.1 XML content.
     * @param limit The maximum number of bytes to be read from the buffer.
     * @throws W21Exception If the object type cannot be detected, if validation fails,
     * or if a parsing error occurs within the gSOAP engine.
     * @see #jniParseBhaRun()
     * @see #jniParseJsonBhaRun()
     */
    private native void jniReadBhaRunFromByteBuffer(ByteBuffer bbXml, long limit) throws W21Exception;

    /**
     * Reads and parses a WITSML 2.1 document from a memory buffer with <b>CementJob</b> object.
     *
     * <p>The native layer performs the following operations:</p>
     * <ul>
     * <li><b>Identification:</b> Loads the WITSML 2.1 object type within the XML.</li>
     * <li><b>Validation:</b> Validates the XML integrity and schema compliance.</li>
     * <li><b>Parsing:</b> Maps the validated data into the corresponding C structures.</li>
     * </ul>
     *
     * @param bbXml The {@link ByteBuffer} containing the WITSML 2.1 XML content.
     * @param limit The maximum number of bytes to be read from the buffer.
     * @throws W21Exception If the object type cannot be detected, if validation fails,
     * or if a parsing error occurs within the gSOAP engine.
     * @see #jniParseCementJob()
     * @see #jniParseJsonCementJob()
     */
    private native void jniReadCementJobFromByteBuffer(ByteBuffer bbXml, long limit) throws W21Exception;

    /**
     * Reads and parses a WITSML 2.1 document from a memory buffer with <b>CementJobEvaluation</b> object.
     *
     * <p>The native layer performs the following operations:</p>
     * <ul>
     * <li><b>Identification:</b> Loads the WITSML 2.1 object type within the XML.</li>
     * <li><b>Validation:</b> Validates the XML integrity and schema compliance.</li>
     * <li><b>Parsing:</b> Maps the validated data into the corresponding C structures.</li>
     * </ul>
     *
     * @param bbXml The {@link ByteBuffer} containing the WITSML 2.1 XML content.
     * @param limit The maximum number of bytes to be read from the buffer.
     * @throws W21Exception If the object type cannot be detected, if validation fails,
     * or if a parsing error occurs within the gSOAP engine.
     * @see #jniParseCementJobEvaluation()
     * @see #jniParseJsonCementJobEvaluation()
     */
    private native void jniReadCementJobEvaluationFromByteBuffer(ByteBuffer bbXml, long limit) throws W21Exception;

    /**
     * Reads and parses a WITSML 2.1 document from a memory buffer with <b>Channel</b> object.
     *
     * <p>The native layer performs the following operations:</p>
     * <ul>
     * <li><b>Identification:</b> Loads the WITSML 2.1 object type within the XML.</li>
     * <li><b>Validation:</b> Validates the XML integrity and schema compliance.</li>
     * <li><b>Parsing:</b> Maps the validated data into the corresponding C structures.</li>
     * </ul>
     *
     * @param bbXml The {@link ByteBuffer} containing the WITSML 2.1 XML content.
     * @param limit The maximum number of bytes to be read from the buffer.
     * @throws W21Exception If the object type cannot be detected, if validation fails,
     * or if a parsing error occurs within the gSOAP engine.
     * @see #jniParseChannel()
     * @see #jniParseJsonChannel()
     */
    private native void jniReadChannelFromByteBuffer(ByteBuffer bbXml, long limit) throws W21Exception;

    /**
     * Reads and parses a WITSML 2.1 document from a memory buffer with <b>ChannelKind</b> object.
     *
     * <p>The native layer performs the following operations:</p>
     * <ul>
     * <li><b>Identification:</b> Loads the WITSML 2.1 object type within the XML.</li>
     * <li><b>Validation:</b> Validates the XML integrity and schema compliance.</li>
     * <li><b>Parsing:</b> Maps the validated data into the corresponding C structures.</li>
     * </ul>
     *
     * @param bbXml The {@link ByteBuffer} containing the WITSML 2.1 XML content.
     * @param limit The maximum number of bytes to be read from the buffer.
     * @throws W21Exception If the object type cannot be detected, if validation fails,
     * or if a parsing error occurs within the gSOAP engine.
     * @see #jniParseChannelKind()
     * @see #jniParseJsonChannelKind()
     */
    private native void jniReadChannelKindFromByteBuffer(ByteBuffer bbXml, long limit) throws W21Exception;

    /**
     * Reads and parses a WITSML 2.1 document from a memory buffer with <b>ChannelKindDictionary</b> object.
     *
     * <p>The native layer performs the following operations:</p>
     * <ul>
     * <li><b>Identification:</b> Loads the WITSML 2.1 object type within the XML.</li>
     * <li><b>Validation:</b> Validates the XML integrity and schema compliance.</li>
     * <li><b>Parsing:</b> Maps the validated data into the corresponding C structures.</li>
     * </ul>
     *
     * @param bbXml The {@link ByteBuffer} containing the WITSML 2.1 XML content.
     * @param limit The maximum number of bytes to be read from the buffer.
     * @throws W21Exception If the object type cannot be detected, if validation fails,
     * or if a parsing error occurs within the gSOAP engine.
     * @see #jniParseChannelKindDictionary()
     * @see #jniParseJsonChannelKindDictionary()
     */
    private native void jniReadChannelKindDictionaryFromByteBuffer(ByteBuffer bbXml, long limit) throws W21Exception;

    /**
     * Reads and parses a WITSML 2.1 document from a memory buffer with <b>ChannelSet</b> object.
     *
     * <p>The native layer performs the following operations:</p>
     * <ul>
     * <li><b>Identification:</b> Loads the WITSML 2.1 object type within the XML.</li>
     * <li><b>Validation:</b> Validates the XML integrity and schema compliance.</li>
     * <li><b>Parsing:</b> Maps the validated data into the corresponding C structures.</li>
     * </ul>
     *
     * @param bbXml The {@link ByteBuffer} containing the WITSML 2.1 XML content.
     * @param limit The maximum number of bytes to be read from the buffer.
     * @throws W21Exception If the object type cannot be detected, if validation fails,
     * or if a parsing error occurs within the gSOAP engine.
     * @see #jniParseChannelSet()
     * @see #jniParseJsonChannelSet()
     */
    private native void jniReadChannelSetFromByteBuffer(ByteBuffer bbXml, long limit) throws W21Exception;

    /**
     * Reads and parses a WITSML 2.1 document from a memory buffer with <b>CuttingsGeology</b> object.
     *
     * <p>The native layer performs the following operations:</p>
     * <ul>
     * <li><b>Identification:</b> Loads the WITSML 2.1 object type within the XML.</li>
     * <li><b>Validation:</b> Validates the XML integrity and schema compliance.</li>
     * <li><b>Parsing:</b> Maps the validated data into the corresponding C structures.</li>
     * </ul>
     *
     * @param bbXml The {@link ByteBuffer} containing the WITSML 2.1 XML content.
     * @param limit The maximum number of bytes to be read from the buffer.
     * @throws W21Exception If the object type cannot be detected, if validation fails,
     * or if a parsing error occurs within the gSOAP engine.
     * @see #jniParseCuttingsGeology()
     * @see #jniParseJsonCuttingsGeology()
     */
    private native void jniReadCuttingsGeologyFromByteBuffer(ByteBuffer bbXml, long limit) throws W21Exception;

    /**
     * Reads and parses a WITSML 2.1 document from a memory buffer with <b>CuttingsGeologyInterval</b> object.
     *
     * <p>The native layer performs the following operations:</p>
     * <ul>
     * <li><b>Identification:</b> Loads the WITSML 2.1 object type within the XML.</li>
     * <li><b>Validation:</b> Validates the XML integrity and schema compliance.</li>
     * <li><b>Parsing:</b> Maps the validated data into the corresponding C structures.</li>
     * </ul>
     *
     * @param bbXml The {@link ByteBuffer} containing the WITSML 2.1 XML content.
     * @param limit The maximum number of bytes to be read from the buffer.
     * @throws W21Exception If the object type cannot be detected, if validation fails,
     * or if a parsing error occurs within the gSOAP engine.
     * @see #jniParseCuttingsGeologyInterval()
     * @see #jniParseJsonCuttingsGeologyInterval()
     */
    private native void jniReadCuttingsGeologyIntervalFromByteBuffer(ByteBuffer bbXml, long limit) throws W21Exception;

    /**
     * Reads and parses a WITSML 2.1 document from a memory buffer with <b>DepthRegImage</b> object.
     *
     * <p>The native layer performs the following operations:</p>
     * <ul>
     * <li><b>Identification:</b> Loads the WITSML 2.1 object type within the XML.</li>
     * <li><b>Validation:</b> Validates the XML integrity and schema compliance.</li>
     * <li><b>Parsing:</b> Maps the validated data into the corresponding C structures.</li>
     * </ul>
     *
     * @param bbXml The {@link ByteBuffer} containing the WITSML 2.1 XML content.
     * @param limit The maximum number of bytes to be read from the buffer.
     * @throws W21Exception If the object type cannot be detected, if validation fails,
     * or if a parsing error occurs within the gSOAP engine.
     * @see #jniParseDepthRegImage()
     * @see #jniParseJsonDepthRegImage()
     */
    private native void jniReadDepthRegImageFromByteBuffer(ByteBuffer bbXml, long limit) throws W21Exception;

    /**
     * Reads and parses a WITSML 2.1 document from a memory buffer with <b>DownholeComponent</b> object.
     *
     * <p>The native layer performs the following operations:</p>
     * <ul>
     * <li><b>Identification:</b> Loads the WITSML 2.1 object type within the XML.</li>
     * <li><b>Validation:</b> Validates the XML integrity and schema compliance.</li>
     * <li><b>Parsing:</b> Maps the validated data into the corresponding C structures.</li>
     * </ul>
     *
     * @param bbXml The {@link ByteBuffer} containing the WITSML 2.1 XML content.
     * @param limit The maximum number of bytes to be read from the buffer.
     * @throws W21Exception If the object type cannot be detected, if validation fails,
     * or if a parsing error occurs within the gSOAP engine.
     * @see #jniParseDownholeComponent()
     * @see #jniParseJsonDownholeComponent()
     */
    private native void jniReadDownholeComponentFromByteBuffer(ByteBuffer bbXml, long limit) throws W21Exception;

    /**
     * Reads and parses a WITSML 2.1 document from a memory buffer with <b>DrillReport</b> object.
     *
     * <p>The native layer performs the following operations:</p>
     * <ul>
     * <li><b>Identification:</b> Loads the WITSML 2.1 object type within the XML.</li>
     * <li><b>Validation:</b> Validates the XML integrity and schema compliance.</li>
     * <li><b>Parsing:</b> Maps the validated data into the corresponding C structures.</li>
     * </ul>
     *
     * @param bbXml The {@link ByteBuffer} containing the WITSML 2.1 XML content.
     * @param limit The maximum number of bytes to be read from the buffer.
     * @throws W21Exception If the object type cannot be detected, if validation fails,
     * or if a parsing error occurs within the gSOAP engine.
     * @see #jniParseDrillReport()
     * @see #jniParseJsonDrillReport()
     */
    private native void jniReadDrillReportFromByteBuffer(ByteBuffer bbXml, long limit) throws W21Exception;

    /**
     * Reads and parses a WITSML 2.1 document from a memory buffer with <b>ErrorTerm</b> object.
     *
     * <p>The native layer performs the following operations:</p>
     * <ul>
     * <li><b>Identification:</b> Loads the WITSML 2.1 object type within the XML.</li>
     * <li><b>Validation:</b> Validates the XML integrity and schema compliance.</li>
     * <li><b>Parsing:</b> Maps the validated data into the corresponding C structures.</li>
     * </ul>
     *
     * @param bbXml The {@link ByteBuffer} containing the WITSML 2.1 XML content.
     * @param limit The maximum number of bytes to be read from the buffer.
     * @throws W21Exception If the object type cannot be detected, if validation fails,
     * or if a parsing error occurs within the gSOAP engine.
     * @see #jniParseErrorTerm()
     * @see #jniParseJsonErrorTerm()
     */
    private native void jniReadErrorTermFromByteBuffer(ByteBuffer bbXml, long limit) throws W21Exception;

    /**
     * Reads and parses a WITSML 2.1 document from a memory buffer with <b>ErrorTermDictionary</b> object.
     *
     * <p>The native layer performs the following operations:</p>
     * <ul>
     * <li><b>Identification:</b> Loads the WITSML 2.1 object type within the XML.</li>
     * <li><b>Validation:</b> Validates the XML integrity and schema compliance.</li>
     * <li><b>Parsing:</b> Maps the validated data into the corresponding C structures.</li>
     * </ul>
     *
     * @param bbXml The {@link ByteBuffer} containing the WITSML 2.1 XML content.
     * @param limit The maximum number of bytes to be read from the buffer.
     * @throws W21Exception If the object type cannot be detected, if validation fails,
     * or if a parsing error occurs within the gSOAP engine.
     * @see #jniParseErrorTermDictionary()
     * @see #jniParseJsonErrorTermDictionary()
     */
    private native void jniReadErrorTermDictionaryFromByteBuffer(ByteBuffer bbXml, long limit) throws W21Exception;

    /**
     * Reads and parses a WITSML 2.1 document from a memory buffer with <b>FluidsReport</b> object.
     *
     * <p>The native layer performs the following operations:</p>
     * <ul>
     * <li><b>Identification:</b> Loads the WITSML 2.1 object type within the XML.</li>
     * <li><b>Validation:</b> Validates the XML integrity and schema compliance.</li>
     * <li><b>Parsing:</b> Maps the validated data into the corresponding C structures.</li>
     * </ul>
     *
     * @param bbXml The {@link ByteBuffer} containing the WITSML 2.1 XML content.
     * @param limit The maximum number of bytes to be read from the buffer.
     * @throws W21Exception If the object type cannot be detected, if validation fails,
     * or if a parsing error occurs within the gSOAP engine.
     * @see #jniParseFluidsReport()
     * @see #jniParseJsonFluidsReport()
     */
    private native void jniReadFluidsReportFromByteBuffer(ByteBuffer bbXml, long limit) throws W21Exception;

    /**
     * Reads and parses a WITSML 2.1 document from a memory buffer with <b>InterpretedGeology</b> object.
     *
     * <p>The native layer performs the following operations:</p>
     * <ul>
     * <li><b>Identification:</b> Loads the WITSML 2.1 object type within the XML.</li>
     * <li><b>Validation:</b> Validates the XML integrity and schema compliance.</li>
     * <li><b>Parsing:</b> Maps the validated data into the corresponding C structures.</li>
     * </ul>
     *
     * @param bbXml The {@link ByteBuffer} containing the WITSML 2.1 XML content.
     * @param limit The maximum number of bytes to be read from the buffer.
     * @throws W21Exception If the object type cannot be detected, if validation fails,
     * or if a parsing error occurs within the gSOAP engine.
     * @see #jniParseInterpretedGeology()
     * @see #jniParseJsonInterpretedGeology()
     */
    private native void jniReadInterpretedGeologyFromByteBuffer(ByteBuffer bbXml, long limit) throws W21Exception;

    /**
     * Reads and parses a WITSML 2.1 document from a memory buffer with <b>InterpretedGeology</b> object.
     *
     * <p>The native layer performs the following operations:</p>
     * <ul>
     * <li><b>Identification:</b> Loads the WITSML 2.1 object type within the XML.</li>
     * <li><b>Validation:</b> Validates the XML integrity and schema compliance.</li>
     * <li><b>Parsing:</b> Maps the validated data into the corresponding C structures.</li>
     * </ul>
     *
     * @param bbXml The {@link ByteBuffer} containing the WITSML 2.1 XML content.
     * @param limit The maximum number of bytes to be read from the buffer.
     * @throws W21Exception If the object type cannot be detected, if validation fails,
     * or if a parsing error occurs within the gSOAP engine.
     * @see #jniParseInterpretedGeology()
     * @see #jniParseJsonInterpretedGeology()
     */
    private native void jniReadInterpretedGeologyIntervalFromByteBuffer(ByteBuffer bbXml, long limit) throws W21Exception;

    /**
     * Reads and parses a WITSML 2.1 document from a memory buffer with <b>Log</b> object.
     *
     * <p>The native layer performs the following operations:</p>
     * <ul>
     * <li><b>Identification:</b> Loads the WITSML 2.1 object type within the XML.</li>
     * <li><b>Validation:</b> Validates the XML integrity and schema compliance.</li>
     * <li><b>Parsing:</b> Maps the validated data into the corresponding C structures.</li>
     * </ul>
     *
     * @param bbXml The {@link ByteBuffer} containing the WITSML 2.1 XML content.
     * @param limit The maximum number of bytes to be read from the buffer.
     * @throws W21Exception If the object type cannot be detected, if validation fails,
     * or if a parsing error occurs within the gSOAP engine.
     * @see #jniParseLog()
     * @see #jniParseJsonLog()
     */
    private native void jniReadLogFromByteBuffer(ByteBuffer bbXml, long limit) throws W21Exception;

    /**
     * Reads and parses a WITSML 2.1 document from a memory buffer with <b>LoggingToolKind</b> object.
     *
     * <p>The native layer performs the following operations:</p>
     * <ul>
     * <li><b>Identification:</b> Loads the WITSML 2.1 object type within the XML.</li>
     * <li><b>Validation:</b> Validates the XML integrity and schema compliance.</li>
     * <li><b>Parsing:</b> Maps the validated data into the corresponding C structures.</li>
     * </ul>
     *
     * @param bbXml The {@link ByteBuffer} containing the WITSML 2.1 XML content.
     * @param limit The maximum number of bytes to be read from the buffer.
     * @throws W21Exception If the object type cannot be detected, if validation fails,
     * or if a parsing error occurs within the gSOAP engine.
     * @see #jniParseLoggingToolKind()
     * @see #jniParseJsonLoggingToolKind()
     */
    private native void jniReadLoggingToolKindFromByteBuffer(ByteBuffer bbXml, long limit) throws W21Exception;

    /**
     * Reads and parses a WITSML 2.1 document from a memory buffer with <b>LoggingToolKindDictionary</b> object.
     *
     * <p>The native layer performs the following operations:</p>
     * <ul>
     * <li><b>Identification:</b> Loads the WITSML 2.1 object type within the XML.</li>
     * <li><b>Validation:</b> Validates the XML integrity and schema compliance.</li>
     * <li><b>Parsing:</b> Maps the validated data into the corresponding C structures.</li>
     * </ul>
     *
     * @param bbXml The {@link ByteBuffer} containing the WITSML 2.1 XML content.
     * @param limit The maximum number of bytes to be read from the buffer.
     * @throws W21Exception If the object type cannot be detected, if validation fails,
     * or if a parsing error occurs within the gSOAP engine.
     * @see #jniParseLoggingToolKindDictionary()
     * @see #jniParseJsonLoggingToolKindDictionary()
     */
    private native void jniReadLoggingToolKindDictionaryFromByteBuffer(ByteBuffer bbXml, long limit) throws W21Exception;

    /**
     * Reads and parses a WITSML 2.1 document from a memory buffer with <b>MudLogReport</b> object.
     *
     * <p>The native layer performs the following operations:</p>
     * <ul>
     * <li><b>Identification:</b> Loads the WITSML 2.1 object type within the XML.</li>
     * <li><b>Validation:</b> Validates the XML integrity and schema compliance.</li>
     * <li><b>Parsing:</b> Maps the validated data into the corresponding C structures.</li>
     * </ul>
     *
     * @param bbXml The {@link ByteBuffer} containing the WITSML 2.1 XML content.
     * @param limit The maximum number of bytes to be read from the buffer.
     * @throws W21Exception If the object type cannot be detected, if validation fails,
     * or if a parsing error occurs within the gSOAP engine.
     * @see #jniParseMudLogReport()
     * @see #jniParseJsonMudLogReport()
     */
    private native void jniReadMudLogReportFromByteBuffer(ByteBuffer bbXml, long limit) throws W21Exception;

    /**
     * Reads and parses a WITSML 2.1 document from a memory buffer with <b>MudlogReportInterval</b> object.
     *
     * <p>The native layer performs the following operations:</p>
     * <ul>
     * <li><b>Identification:</b> Loads the WITSML 2.1 object type within the XML.</li>
     * <li><b>Validation:</b> Validates the XML integrity and schema compliance.</li>
     * <li><b>Parsing:</b> Maps the validated data into the corresponding C structures.</li>
     * </ul>
     *
     * @param bbXml The {@link ByteBuffer} containing the WITSML 2.1 XML content.
     * @param limit The maximum number of bytes to be read from the buffer.
     * @throws W21Exception If the object type cannot be detected, if validation fails,
     * or if a parsing error occurs within the gSOAP engine.
     * @see #jniParseMudlogReportInterval()
     * @see #jniParseJsonMudlogReportInterval()
     */
    private native void jniReadMudlogReportIntervalFromByteBuffer(ByteBuffer bbXml, long limit) throws W21Exception;

    /**
     * Reads and parses a WITSML 2.1 document from a memory buffer with <b>OpsReport</b> object.
     *
     * <p>The native layer performs the following operations:</p>
     * <ul>
     * <li><b>Identification:</b> Loads the WITSML 2.1 object type within the XML.</li>
     * <li><b>Validation:</b> Validates the XML integrity and schema compliance.</li>
     * <li><b>Parsing:</b> Maps the validated data into the corresponding C structures.</li>
     * </ul>
     *
     * @param bbXml The {@link ByteBuffer} containing the WITSML 2.1 XML content.
     * @param limit The maximum number of bytes to be read from the buffer.
     * @throws W21Exception If the object type cannot be detected, if validation fails,
     * or if a parsing error occurs within the gSOAP engine.
     * @see #jniParseOpsReport()
     * @see #jniParseJsonOpsReport()
     */
    private native void jniReadOpsReportFromByteBuffer(ByteBuffer bbXml, long limit) throws W21Exception;

    /**
     * Reads and parses a WITSML 2.1 document from a memory buffer with <b>PPFGChannel</b> object.
     *
     * <p>The native layer performs the following operations:</p>
     * <ul>
     * <li><b>Identification:</b> Loads the WITSML 2.1 object type within the XML.</li>
     * <li><b>Validation:</b> Validates the XML integrity and schema compliance.</li>
     * <li><b>Parsing:</b> Maps the validated data into the corresponding C structures.</li>
     * </ul>
     *
     * @param bbXml The {@link ByteBuffer} containing the WITSML 2.1 XML content.
     * @param limit The maximum number of bytes to be read from the buffer.
     * @throws W21Exception If the object type cannot be detected, if validation fails,
     * or if a parsing error occurs within the gSOAP engine.
     * @see #jniParsePPFGChannel()
     * @see #jniParseJsonPPFGChannel()
     */
    private native void jniReadPPFGChannelFromByteBuffer(ByteBuffer bbXml, long limit) throws W21Exception;

    /**
     * Reads and parses a WITSML 2.1 document from a memory buffer with <b>PPFGChannelSet</b> object.
     *
     * <p>The native layer performs the following operations:</p>
     * <ul>
     * <li><b>Identification:</b> Loads the WITSML 2.1 object type within the XML.</li>
     * <li><b>Validation:</b> Validates the XML integrity and schema compliance.</li>
     * <li><b>Parsing:</b> Maps the validated data into the corresponding C structures.</li>
     * </ul>
     *
     * @param bbXml The {@link ByteBuffer} containing the WITSML 2.1 XML content.
     * @param limit The maximum number of bytes to be read from the buffer.
     * @throws W21Exception If the object type cannot be detected, if validation fails,
     * or if a parsing error occurs within the gSOAP engine.
     * @see #jniParsePPFGChannelSet()
     * @see #jniParseJsonPPFGChannelSet()
     */
    private native void jniReadPPFGChannelSetFromByteBuffer(ByteBuffer bbXml, long limit) throws W21Exception;

    /**
     * Reads and parses a WITSML 2.1 document from a memory buffer with <b>PPFGLog</b> object.
     *
     * <p>The native layer performs the following operations:</p>
     * <ul>
     * <li><b>Identification:</b> Loads the WITSML 2.1 object type within the XML.</li>
     * <li><b>Validation:</b> Validates the XML integrity and schema compliance.</li>
     * <li><b>Parsing:</b> Maps the validated data into the corresponding C structures.</li>
     * </ul>
     *
     * @param bbXml The {@link ByteBuffer} containing the WITSML 2.1 XML content.
     * @param limit The maximum number of bytes to be read from the buffer.
     * @throws W21Exception If the object type cannot be detected, if validation fails,
     * or if a parsing error occurs within the gSOAP engine.
     * @see #jniParsePPFGLog()
     * @see #jniParseJsonPPFGLog()
     */
    private native void jniReadPPFGLogFromByteBuffer(ByteBuffer bbXml, long limit) throws W21Exception;

    /**
     * Reads and parses a WITSML 2.1 document from a memory buffer with <b>Rig</b> object.
     *
     * <p>The native layer performs the following operations:</p>
     * <ul>
     * <li><b>Identification:</b> Loads the WITSML 2.1 object type within the XML.</li>
     * <li><b>Validation:</b> Validates the XML integrity and schema compliance.</li>
     * <li><b>Parsing:</b> Maps the validated data into the corresponding C structures.</li>
     * </ul>
     *
     * @param bbXml The {@link ByteBuffer} containing the WITSML 2.1 XML content.
     * @param limit The maximum number of bytes to be read from the buffer.
     * @throws W21Exception If the object type cannot be detected, if validation fails,
     * or if a parsing error occurs within the gSOAP engine.
     * @see #jniParseRig()
     * @see #jniParseJsonRig()
     */
    private native void jniReadRigFromByteBuffer(ByteBuffer bbXml, long limit) throws W21Exception;

    /**
     * Reads and parses a WITSML 2.1 document from a memory buffer with <b>RigUtilization</b> object.
     *
     * <p>The native layer performs the following operations:</p>
     * <ul>
     * <li><b>Identification:</b> Loads the WITSML 2.1 object type within the XML.</li>
     * <li><b>Validation:</b> Validates the XML integrity and schema compliance.</li>
     * <li><b>Parsing:</b> Maps the validated data into the corresponding C structures.</li>
     * </ul>
     *
     * @param bbXml The {@link ByteBuffer} containing the WITSML 2.1 XML content.
     * @param limit The maximum number of bytes to be read from the buffer.
     * @throws W21Exception If the object type cannot be detected, if validation fails,
     * or if a parsing error occurs within the gSOAP engine.
     * @see #jniParseRigUtilization()
     * @see #jniParseJsonRigUtilization()
     */
    private native void jniReadRigUtilizationFromByteBuffer(ByteBuffer bbXml, long limit) throws W21Exception;

    /**
     * Reads and parses a WITSML 2.1 document from a memory buffer with <b>Risk</b> object.
     *
     * <p>The native layer performs the following operations:</p>
     * <ul>
     * <li><b>Identification:</b> Loads the WITSML 2.1 object type within the XML.</li>
     * <li><b>Validation:</b> Validates the XML integrity and schema compliance.</li>
     * <li><b>Parsing:</b> Maps the validated data into the corresponding C structures.</li>
     * </ul>
     *
     * @param bbXml The {@link ByteBuffer} containing the WITSML 2.1 XML content.
     * @param limit The maximum number of bytes to be read from the buffer.
     * @throws W21Exception If the object type cannot be detected, if validation fails,
     * or if a parsing error occurs within the gSOAP engine.
     * @see #jniParseRisk()
     * @see #jniParseJsonRisk()
     */
    private native void jniReadRiskFromByteBuffer(ByteBuffer bbXml, long limit) throws W21Exception;

    /**
     * Reads and parses a WITSML 2.1 document from a memory buffer with <b>ShowEvaluation</b> object.
     *
     * <p>The native layer performs the following operations:</p>
     * <ul>
     * <li><b>Identification:</b> Loads the WITSML 2.1 object type within the XML.</li>
     * <li><b>Validation:</b> Validates the XML integrity and schema compliance.</li>
     * <li><b>Parsing:</b> Maps the validated data into the corresponding C structures.</li>
     * </ul>
     *
     * @param bbXml The {@link ByteBuffer} containing the WITSML 2.1 XML content.
     * @param limit The maximum number of bytes to be read from the buffer.
     * @throws W21Exception If the object type cannot be detected, if validation fails,
     * or if a parsing error occurs within the gSOAP engine.
     * @see #jniParseShowEvaluation()
     * @see #jniParseJsonShowEvaluation()
     */
    private native void jniReadShowEvaluationFromByteBuffer(ByteBuffer bbXml, long limit) throws W21Exception;

    /**
     * Reads and parses a WITSML 2.1 document from a memory buffer with <b>ShowEvaluationInterval</b> object.
     *
     * <p>The native layer performs the following operations:</p>
     * <ul>
     * <li><b>Identification:</b> Loads the WITSML 2.1 object type within the XML.</li>
     * <li><b>Validation:</b> Validates the XML integrity and schema compliance.</li>
     * <li><b>Parsing:</b> Maps the validated data into the corresponding C structures.</li>
     * </ul>
     *
     * @param bbXml The {@link ByteBuffer} containing the WITSML 2.1 XML content.
     * @param limit The maximum number of bytes to be read from the buffer.
     * @throws W21Exception If the object type cannot be detected, if validation fails,
     * or if a parsing error occurs within the gSOAP engine.
     * @see #jniParseShowEvaluationInterval()
     * @see #jniParseJsonShowEvaluationInterval()
     */
    private native void jniReadShowEvaluationIntervalFromByteBuffer(ByteBuffer bbXml, long limit) throws W21Exception;

    /**
     * Reads and parses a WITSML 2.1 document from a memory buffer with <b>StimJob</b> object.
     *
     * <p>The native layer performs the following operations:</p>
     * <ul>
     * <li><b>Identification:</b> Loads the WITSML 2.1 object type within the XML.</li>
     * <li><b>Validation:</b> Validates the XML integrity and schema compliance.</li>
     * <li><b>Parsing:</b> Maps the validated data into the corresponding C structures.</li>
     * </ul>
     *
     * @param bbXml The {@link ByteBuffer} containing the WITSML 2.1 XML content.
     * @param limit The maximum number of bytes to be read from the buffer.
     * @throws W21Exception If the object type cannot be detected, if validation fails,
     * or if a parsing error occurs within the gSOAP engine.
     * @see #jniParseStimJob()
     * @see #jniParseJsonStimJob()
     */
    private native void jniReadStimJobFromByteBuffer(ByteBuffer bbXml, long limit) throws W21Exception;

    /**
     * Reads and parses a WITSML 2.1 document from a memory buffer with <b>StimJobStage</b> object.
     *
     * <p>The native layer performs the following operations:</p>
     * <ul>
     * <li><b>Identification:</b> Loads the WITSML 2.1 object type within the XML.</li>
     * <li><b>Validation:</b> Validates the XML integrity and schema compliance.</li>
     * <li><b>Parsing:</b> Maps the validated data into the corresponding C structures.</li>
     * </ul>
     *
     * @param bbXml The {@link ByteBuffer} containing the WITSML 2.1 XML content.
     * @param limit The maximum number of bytes to be read from the buffer.
     * @throws W21Exception If the object type cannot be detected, if validation fails,
     * or if a parsing error occurs within the gSOAP engine.
     * @see #jniParseStimJobStage()
     * @see #jniParseJsonStimJobStage()
     */
    private native void jniReadStimJobStageFromByteBuffer(ByteBuffer bbXml, long limit) throws W21Exception;

    /**
     * Reads and parses a WITSML 2.1 document from a memory buffer with <b>StimPerforationCluster</b> object.
     *
     * <p>The native layer performs the following operations:</p>
     * <ul>
     * <li><b>Identification:</b> Loads the WITSML 2.1 object type within the XML.</li>
     * <li><b>Validation:</b> Validates the XML integrity and schema compliance.</li>
     * <li><b>Parsing:</b> Maps the validated data into the corresponding C structures.</li>
     * </ul>
     *
     * @param bbXml The {@link ByteBuffer} containing the WITSML 2.1 XML content.
     * @param limit The maximum number of bytes to be read from the buffer.
     * @throws W21Exception If the object type cannot be detected, if validation fails,
     * or if a parsing error occurs within the gSOAP engine.
     * @see #jniParseStimPerforationCluster()
     * @see #jniParseJsonStimPerforationCluster()
     */
    private native void jniReadStimPerforationClusterFromByteBuffer(ByteBuffer bbXml, long limit) throws W21Exception;

    /**
     * Reads and parses a WITSML 2.1 document from a memory buffer with <b>SurveyProgram</b> object.
     *
     * <p>The native layer performs the following operations:</p>
     * <ul>
     * <li><b>Identification:</b> Loads the WITSML 2.1 object type within the XML.</li>
     * <li><b>Validation:</b> Validates the XML integrity and schema compliance.</li>
     * <li><b>Parsing:</b> Maps the validated data into the corresponding C structures.</li>
     * </ul>
     *
     * @param bbXml The {@link ByteBuffer} containing the WITSML 2.1 XML content.
     * @param limit The maximum number of bytes to be read from the buffer.
     * @throws W21Exception If the object type cannot be detected, if validation fails,
     * or if a parsing error occurs within the gSOAP engine.
     * @see #jniParseSurveyProgram()
     * @see #jniParseJsonSurveyProgram()
     */
    private native void jniReadSurveyProgramFromByteBuffer(ByteBuffer bbXml, long limit) throws W21Exception;

    /**
     * Reads and parses a WITSML 2.1 document from a memory buffer with <b>Target</b> object.
     *
     * <p>The native layer performs the following operations:</p>
     * <ul>
     * <li><b>Identification:</b> Loads the WITSML 2.1 object type within the XML.</li>
     * <li><b>Validation:</b> Validates the XML integrity and schema compliance.</li>
     * <li><b>Parsing:</b> Maps the validated data into the corresponding C structures.</li>
     * </ul>
     *
     * @param bbXml The {@link ByteBuffer} containing the WITSML 2.1 XML content.
     * @param limit The maximum number of bytes to be read from the buffer.
     * @throws W21Exception If the object type cannot be detected, if validation fails,
     * or if a parsing error occurs within the gSOAP engine.
     * @see #jniParseTarget()
     * @see #jniParseJsonTarget()
     */
    private native void jniReadTargetFromByteBuffer(ByteBuffer bbXml, long limit) throws W21Exception;

    /**
     * Reads and parses a WITSML 2.1 document from a memory buffer with <b>ToolErrorModel</b> object.
     *
     * <p>The native layer performs the following operations:</p>
     * <ul>
     * <li><b>Identification:</b> Loads the WITSML 2.1 object type within the XML.</li>
     * <li><b>Validation:</b> Validates the XML integrity and schema compliance.</li>
     * <li><b>Parsing:</b> Maps the validated data into the corresponding C structures.</li>
     * </ul>
     *
     * @param bbXml The {@link ByteBuffer} containing the WITSML 2.1 XML content.
     * @param limit The maximum number of bytes to be read from the buffer.
     * @throws W21Exception If the object type cannot be detected, if validation fails,
     * or if a parsing error occurs within the gSOAP engine.
     * @see #jniParseToolErrorModel()
     * @see #jniParseJsonToolErrorModel()
     */
    private native void jniReadToolErrorModelFromByteBuffer(ByteBuffer bbXml, long limit) throws W21Exception;

    /**
     * Reads and parses a WITSML 2.1 document from a memory buffer with <b>ToolErrorModelDictionary</b> object.
     *
     * <p>The native layer performs the following operations:</p>
     * <ul>
     * <li><b>Identification:</b> Loads the WITSML 2.1 object type within the XML.</li>
     * <li><b>Validation:</b> Validates the XML integrity and schema compliance.</li>
     * <li><b>Parsing:</b> Maps the validated data into the corresponding C structures.</li>
     * </ul>
     *
     * @param bbXml The {@link ByteBuffer} containing the WITSML 2.1 XML content.
     * @param limit The maximum number of bytes to be read from the buffer.
     * @throws W21Exception If the object type cannot be detected, if validation fails,
     * or if a parsing error occurs within the gSOAP engine.
     * @see #jniParseToolErrorModelDictionary()
     * @see #jniParseJsonToolErrorModelDictionary()
     */
    private native void jniReadToolErrorModelDictionaryFromByteBuffer(ByteBuffer bbXml, long limit) throws W21Exception;

    /**
     * Reads and parses a WITSML 2.1 document from a memory buffer with <b>Trajectory</b> object.
     *
     * <p>The native layer performs the following operations:</p>
     * <ul>
     * <li><b>Identification:</b> Loads the WITSML 2.1 object type within the XML.</li>
     * <li><b>Validation:</b> Validates the XML integrity and schema compliance.</li>
     * <li><b>Parsing:</b> Maps the validated data into the corresponding C structures.</li>
     * </ul>
     *
     * @param bbXml The {@link ByteBuffer} containing the WITSML 2.1 XML content.
     * @param limit The maximum number of bytes to be read from the buffer.
     * @throws W21Exception If the object type cannot be detected, if validation fails,
     * or if a parsing error occurs within the gSOAP engine.
     * @see #jniParseTrajectory()
     * @see #jniParseJsonTrajectory()
     */
    private native void jniReadTrajectoryFromByteBuffer(ByteBuffer bbXml, long limit) throws W21Exception;

    /**
     * Reads and parses a WITSML 2.1 document from a memory buffer with <b>TrajectoryStation</b> object.
     *
     * <p>The native layer performs the following operations:</p>
     * <ul>
     * <li><b>Identification:</b> Loads the WITSML 2.1 object type within the XML.</li>
     * <li><b>Validation:</b> Validates the XML integrity and schema compliance.</li>
     * <li><b>Parsing:</b> Maps the validated data into the corresponding C structures.</li>
     * </ul>
     *
     * @param bbXml The {@link ByteBuffer} containing the WITSML 2.1 XML content.
     * @param limit The maximum number of bytes to be read from the buffer.
     * @throws W21Exception If the object type cannot be detected, if validation fails,
     * or if a parsing error occurs within the gSOAP engine.
     * @see #jniParseTrajectoryStation()
     * @see #jniParseJsonTrajectoryStation()
     */
    private native void jniReadTrajectoryStationFromByteBuffer(ByteBuffer bbXml, long limit) throws W21Exception;

    /**
     * Reads and parses a WITSML 2.1 document from a memory buffer with <b>Tubular</b> object.
     *
     * <p>The native layer performs the following operations:</p>
     * <ul>
     * <li><b>Identification:</b> Loads the WITSML 2.1 object type within the XML.</li>
     * <li><b>Validation:</b> Validates the XML integrity and schema compliance.</li>
     * <li><b>Parsing:</b> Maps the validated data into the corresponding C structures.</li>
     * </ul>
     *
     * @param bbXml The {@link ByteBuffer} containing the WITSML 2.1 XML content.
     * @param limit The maximum number of bytes to be read from the buffer.
     * @throws W21Exception If the object type cannot be detected, if validation fails,
     * or if a parsing error occurs within the gSOAP engine.
     * @see #jniParseTubular()
     * @see #jniParseJsonTubular()
     */
    private native void jniReadTubularFromByteBuffer(ByteBuffer bbXml, long limit) throws W21Exception;

    /**
     * Reads and parses a WITSML 2.1 document from a memory buffer with <b>WeightingFunction</b> object.
     *
     * <p>The native layer performs the following operations:</p>
     * <ul>
     * <li><b>Identification:</b> Loads the WITSML 2.1 object type within the XML.</li>
     * <li><b>Validation:</b> Validates the XML integrity and schema compliance.</li>
     * <li><b>Parsing:</b> Maps the validated data into the corresponding C structures.</li>
     * </ul>
     *
     * @param bbXml The {@link ByteBuffer} containing the WITSML 2.1 XML content.
     * @param limit The maximum number of bytes to be read from the buffer.
     * @throws W21Exception If the object type cannot be detected, if validation fails,
     * or if a parsing error occurs within the gSOAP engine.
     * @see #jniParseWeightingFunction()
     * @see #jniParseJsonWeightingFunction()
     */
    private native void jniReadWeightingFunctionFromByteBuffer(ByteBuffer bbXml, long limit) throws W21Exception;

    /**
     * Reads and parses a WITSML 2.1 document from a memory buffer with <b>WeightingFunctionDictionary</b> object.
     *
     * <p>The native layer performs the following operations:</p>
     * <ul>
     * <li><b>Identification:</b> Loads the WITSML 2.1 object type within the XML.</li>
     * <li><b>Validation:</b> Validates the XML integrity and schema compliance.</li>
     * <li><b>Parsing:</b> Maps the validated data into the corresponding C structures.</li>
     * </ul>
     *
     * @param bbXml The {@link ByteBuffer} containing the WITSML 2.1 XML content.
     * @param limit The maximum number of bytes to be read from the buffer.
     * @throws W21Exception If the object type cannot be detected, if validation fails,
     * or if a parsing error occurs within the gSOAP engine.
     * @see #jniParseWeightingFunctionDictionary()
     * @see #jniParseJsonWeightingFunctionDictionary()
     */
    private native void jniReadWeightingFunctionDictionaryFromByteBuffer(ByteBuffer bbXml, long limit) throws W21Exception;

    /**
     * Reads and parses a WITSML 2.1 document from a memory buffer with <b>Well</b> object.
     *
     * <p>The native layer performs the following operations:</p>
     * <ul>
     * <li><b>Identification:</b> Loads the WITSML 2.1 object type within the XML.</li>
     * <li><b>Validation:</b> Validates the XML integrity and schema compliance.</li>
     * <li><b>Parsing:</b> Maps the validated data into the corresponding C structures.</li>
     * </ul>
     *
     * @param bbXml The {@link ByteBuffer} containing the WITSML 2.1 XML content.
     * @param limit The maximum number of bytes to be read from the buffer.
     * @throws W21Exception If the object type cannot be detected, if validation fails,
     * or if a parsing error occurs within the gSOAP engine.
     * @see #jniParseWell()
     * @see #jniParseJsonWell()
     */
    private native void jniReadWellFromByteBuffer(ByteBuffer bbXml, long limit) throws W21Exception;

    /**
     * Reads and parses a WITSML 2.1 document from a memory buffer with <b>Wellbore</b> object.
     *
     * <p>The native layer performs the following operations:</p>
     * <ul>
     * <li><b>Identification:</b> Loads the WITSML 2.1 object type within the XML.</li>
     * <li><b>Validation:</b> Validates the XML integrity and schema compliance.</li>
     * <li><b>Parsing:</b> Maps the validated data into the corresponding C structures.</li>
     * </ul>
     *
     * @param bbXml The {@link ByteBuffer} containing the WITSML 2.1 XML content.
     * @param limit The maximum number of bytes to be read from the buffer.
     * @throws W21Exception If the object type cannot be detected, if validation fails,
     * or if a parsing error occurs within the gSOAP engine.
     * @see #jniParseWellbore()
     * @see #jniParseJsonWellbore()
     */
    private native void jniReadWellboreFromByteBuffer(ByteBuffer bbXml, long limit) throws W21Exception;

    /**
     * Reads and parses a WITSML 2.1 document from a memory buffer with <b>WellboreCompletion</b> object.
     *
     * <p>The native layer performs the following operations:</p>
     * <ul>
     * <li><b>Identification:</b> Loads the WITSML 2.1 object type within the XML.</li>
     * <li><b>Validation:</b> Validates the XML integrity and schema compliance.</li>
     * <li><b>Parsing:</b> Maps the validated data into the corresponding C structures.</li>
     * </ul>
     *
     * @param bbXml The {@link ByteBuffer} containing the WITSML 2.1 XML content.
     * @param limit The maximum number of bytes to be read from the buffer.
     * @throws W21Exception If the object type cannot be detected, if validation fails,
     * or if a parsing error occurs within the gSOAP engine.
     * @see #jniParseWellboreCompletion()
     * @see #jniParseJsonWellboreCompletion()
     */
    private native void jniReadWellboreCompletionFromByteBuffer(ByteBuffer bbXml, long limit) throws W21Exception;

    /**
     * Reads and parses a WITSML 2.1 document from a memory buffer with <b>WellboreGeology</b> object.
     *
     * <p>The native layer performs the following operations:</p>
     * <ul>
     * <li><b>Identification:</b> Loads the WITSML 2.1 object type within the XML.</li>
     * <li><b>Validation:</b> Validates the XML integrity and schema compliance.</li>
     * <li><b>Parsing:</b> Maps the validated data into the corresponding C structures.</li>
     * </ul>
     *
     * @param bbXml The {@link ByteBuffer} containing the WITSML 2.1 XML content.
     * @param limit The maximum number of bytes to be read from the buffer.
     * @throws W21Exception If the object type cannot be detected, if validation fails,
     * or if a parsing error occurs within the gSOAP engine.
     * @see #jniParseWellboreGeology()
     * @see #jniParseJsonWellboreGeology()
     */
    private native void jniReadWellboreGeologyFromByteBuffer(ByteBuffer bbXml, long limit) throws W21Exception;

    /**
     * Reads and parses a WITSML 2.1 document from a memory buffer with <b>WellboreGeometry</b> object.
     *
     * <p>The native layer performs the following operations:</p>
     * <ul>
     * <li><b>Identification:</b> Loads the WITSML 2.1 object type within the XML.</li>
     * <li><b>Validation:</b> Validates the XML integrity and schema compliance.</li>
     * <li><b>Parsing:</b> Maps the validated data into the corresponding C structures.</li>
     * </ul>
     *
     * @param bbXml The {@link ByteBuffer} containing the WITSML 2.1 XML content.
     * @param limit The maximum number of bytes to be read from the buffer.
     * @throws W21Exception If the object type cannot be detected, if validation fails,
     * or if a parsing error occurs within the gSOAP engine.
     * @see #jniParseWellboreGeometry()
     * @see #jniParseJsonWellboreGeometry()
     */
    private native void jniReadWellboreGeometryFromByteBuffer(ByteBuffer bbXml, long limit) throws W21Exception;

    /**
     * Reads and parses a WITSML 2.1 document from a memory buffer with <b>WellboreGeometrySection</b> object.
     *
     * <p>The native layer performs the following operations:</p>
     * <ul>
     * <li><b>Identification:</b> Loads the WITSML 2.1 object type within the XML.</li>
     * <li><b>Validation:</b> Validates the XML integrity and schema compliance.</li>
     * <li><b>Parsing:</b> Maps the validated data into the corresponding C structures.</li>
     * </ul>
     *
     * @param bbXml The {@link ByteBuffer} containing the WITSML 2.1 XML content.
     * @param limit The maximum number of bytes to be read from the buffer.
     * @throws W21Exception If the object type cannot be detected, if validation fails,
     * or if a parsing error occurs within the gSOAP engine.
     * @see #jniParseWellboreGeometrySection()
     * @see #jniParseJsonWellboreGeometrySection()
     */
    private native void jniReadWellboreGeometrySectionFromByteBuffer(ByteBuffer bbXml, long limit) throws W21Exception;

    /**
     * Reads and parses a WITSML 2.1 document from a memory buffer with <b>WellboreMarker</b> object.
     *
     * <p>The native layer performs the following operations:</p>
     * <ul>
     * <li><b>Identification:</b> Loads the WITSML 2.1 object type within the XML.</li>
     * <li><b>Validation:</b> Validates the XML integrity and schema compliance.</li>
     * <li><b>Parsing:</b> Maps the validated data into the corresponding C structures.</li>
     * </ul>
     *
     * @param bbXml The {@link ByteBuffer} containing the WITSML 2.1 XML content.
     * @param limit The maximum number of bytes to be read from the buffer.
     * @throws W21Exception If the object type cannot be detected, if validation fails,
     * or if a parsing error occurs within the gSOAP engine.
     * @see #jniParseWellboreMarker()
     * @see #jniParseJsonWellboreMarker()
     */
    private native void jniReadWellboreMarkerFromByteBuffer(ByteBuffer bbXml, long limit) throws W21Exception;

    /**
     * Reads and parses a WITSML 2.1 document from a memory buffer with <b>WellboreMarkerSet</b> object.
     *
     * <p>The native layer performs the following operations:</p>
     * <ul>
     * <li><b>Identification:</b> Loads the WITSML 2.1 object type within the XML.</li>
     * <li><b>Validation:</b> Validates the XML integrity and schema compliance.</li>
     * <li><b>Parsing:</b> Maps the validated data into the corresponding C structures.</li>
     * </ul>
     *
     * @param bbXml The {@link ByteBuffer} containing the WITSML 2.1 XML content.
     * @param limit The maximum number of bytes to be read from the buffer.
     * @throws W21Exception If the object type cannot be detected, if validation fails,
     * or if a parsing error occurs within the gSOAP engine.
     * @see #jniParseWellboreMarkerSet()
     * @see #jniParseJsonWellboreMarkerSet()
     */
    private native void jniReadWellboreMarkerSetFromByteBuffer(ByteBuffer bbXml, long limit) throws W21Exception;

    /**
     * Reads and parses a WITSML 2.1 document from a memory buffer with <b>WellCMLedger</b> object.
     *
     * <p>The native layer performs the following operations:</p>
     * <ul>
     * <li><b>Identification:</b> Loads the WITSML 2.1 object type within the XML.</li>
     * <li><b>Validation:</b> Validates the XML integrity and schema compliance.</li>
     * <li><b>Parsing:</b> Maps the validated data into the corresponding C structures.</li>
     * </ul>
     *
     * @param bbXml The {@link ByteBuffer} containing the WITSML 2.1 XML content.
     * @param limit The maximum number of bytes to be read from the buffer.
     * @throws W21Exception If the object type cannot be detected, if validation fails,
     * or if a parsing error occurs within the gSOAP engine.
     * @see #jniParseWellCMLedger()
     * @see #jniParseJsonWellCMLedger()
     */
    private native void jniReadWellCMLedgerFromByteBuffer(ByteBuffer bbXml, long limit) throws W21Exception;

    /**
     * Reads and parses a WITSML 2.1 document from a memory buffer with <b>WellCompletion</b> object.
     *
     * <p>The native layer performs the following operations:</p>
     * <ul>
     * <li><b>Identification:</b> Loads the WITSML 2.1 object type within the XML.</li>
     * <li><b>Validation:</b> Validates the XML integrity and schema compliance.</li>
     * <li><b>Parsing:</b> Maps the validated data into the corresponding C structures.</li>
     * </ul>
     *
     * @param bbXml The {@link ByteBuffer} containing the WITSML 2.1 XML content.
     * @param limit The maximum number of bytes to be read from the buffer.
     * @throws W21Exception If the object type cannot be detected, if validation fails,
     * or if a parsing error occurs within the gSOAP engine.
     * @see #jniParseWellCompletion()
     * @see #jniParseJsonWellCompletion()
     */
    private native void jniReadWellCompletionFromByteBuffer(ByteBuffer bbXml, long limit) throws W21Exception;

    //==============================================================================//
    //====================== INPUT: NATIVE MEMORY MANAGEMENT =======================//
    //==============================================================================//

    /**
     * Resets the native parser state and clears all internal C structures in previous object parsed.
     * <p>
     * This method is used for multi-document processing, ensuring
     * all allocated memory is ready to be used in a new parsing.
     * No GC is used.
     * No realloc is need for configuration and instance
     * </p>
     */
    private native void jniW21Recycle() throws W21Exception;

    /**
     * Acquires a memory access on the native Direct Byte Buffer (Zero Copy Access).
     * <p>
     * This method initiates a protected session. The memory address in {@code bbBson} or {@code bbJson}
     * will remain stable until the access is released by #jniEndRead().
     * </p>
     * @return 0 on successful and memory area in {@code bbBson} or {@code bbJson} are safe to read.
     * A native error code meaning that memory area cannot be read.
     * @see #jniEndRead()
     */
    private native int jniBeginRead();

    /**
     * Releases a memory access on the native Direct Byte Buffer (Zero Copy Access).
     * <p>
     * This method ends a protected session. The memory address in {@code bbBson} or {@code bbJson}
     * will be able to be recycled or free after #jniEndRead().
     * </p>
     * @return 0 on successful and memory area in {@code bbBson} or {@code bbJson} are safe to be recycled or free.
     * Non zero code, meaning that memory area cannot be recycled or free.
     * {@code bbBson} or {@code bbJson} cannot be read
     * @see #jniBeginRead()
     * @see #jniW21Recycle()
     */
    private native int jniEndRead();

    //==============================================================================//
    //============ INPUT: NATIVE WITSML 2.1 TO BSON OBJECT METHODS =================//
    //==============================================================================//
    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a BSON structure using
     * AutoDetect mode.
     * * Upon success, the direct ByteBuffer ({@code bbBson}) will map to the
     * natively allocated BSON memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the BSON serialization process.
     */
    private native void jniParse() throws W21Exception;

    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a BSON structure using
     * BhaRun mode.
     * * Upon success, the direct ByteBuffer ({@code bbBson}) will map to the
     * natively allocated BSON memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the BSON serialization process.
     */
    private native void jniParseBhaRun() throws W21Exception;

    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a BSON structure using
     * CementJob mode.
     * * Upon success, the direct ByteBuffer ({@code bbBson}) will map to the
     * natively allocated BSON memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the BSON serialization process.
     */
    private native void jniParseCementJob() throws W21Exception;

    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a BSON structure using
     * CementJobEvaluation mode.
     * Upon success, the direct ByteBuffer ({@code bbBson}) will map to the
     * natively allocated BSON memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the BSON serialization process.
     */
    private native void jniParseCementJobEvaluation() throws W21Exception;

    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a BSON structure using
     * Channel mode.
     * * Upon success, the direct ByteBuffer ({@code bbBson}) will map to the
     * natively allocated BSON memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the BSON serialization process.
     */
    private native void jniParseChannel() throws W21Exception;

    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a BSON structure using
     * ChannelKind mode.
     * * Upon success, the direct ByteBuffer ({@code bbBson}) will map to the
     * natively allocated BSON memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the BSON serialization process.
     */
    private native void jniParseChannelKind() throws W21Exception;

    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a BSON structure using
     * ChannelKindDictionary mode.
     * * Upon success, the direct ByteBuffer ({@code bbBson}) will map to the
     * natively allocated BSON memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the BSON serialization process.
     */
    private native void jniParseChannelKindDictionary() throws W21Exception;

    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a BSON structure using
     * ChannelSet mode.
     * * Upon success, the direct ByteBuffer ({@code bbBson}) will map to the
     * natively allocated BSON memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the BSON serialization process.
     */
    private native void jniParseChannelSet() throws W21Exception;

    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a BSON structure using
     * CuttingsGeology mode.
     * * Upon success, the direct ByteBuffer ({@code bbBson}) will map to the
     * natively allocated BSON memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the BSON serialization process.
     */
    private native void jniParseCuttingsGeology() throws W21Exception;

    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a BSON structure using
     * CuttingsGeologyInterval mode.
     * * Upon success, the direct ByteBuffer ({@code bbBson}) will map to the
     * natively allocated BSON memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the BSON serialization process.
     */
    private native void jniParseCuttingsGeologyInterval() throws W21Exception;

    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a BSON structure using
     * DepthRegImage mode.
     * * Upon success, the direct ByteBuffer ({@code bbBson}) will map to the
     * natively allocated BSON memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the BSON serialization process.
     */
    private native void jniParseDepthRegImage() throws W21Exception;

    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a BSON structure using
     * DownholeComponent mode.
     * * Upon success, the direct ByteBuffer ({@code bbBson}) will map to the
     * natively allocated BSON memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the BSON serialization process.
     */
    private native void jniParseDownholeComponent() throws W21Exception;

    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a BSON structure using
     * DrillReport mode.
     * * Upon success, the direct ByteBuffer ({@code bbBson}) will map to the
     * natively allocated BSON memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the BSON serialization process.
     */
    private native void jniParseDrillReport() throws W21Exception;

    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a BSON structure using
     * ErrorTerm mode.
     * * Upon success, the direct ByteBuffer ({@code bbBson}) will map to the
     * natively allocated BSON memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the BSON serialization process.
     */
    private native void jniParseErrorTerm() throws W21Exception;

    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a BSON structure using
     * ErrorTermDictionary mode.
     * * Upon success, the direct ByteBuffer ({@code bbBson}) will map to the
     * natively allocated BSON memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the BSON serialization process.
     */
    private native void jniParseErrorTermDictionary() throws W21Exception;

    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a BSON structure using
     * FluidsReport mode.
     * * Upon success, the direct ByteBuffer ({@code bbBson}) will map to the
     * natively allocated BSON memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the BSON serialization process.
     */
    private native void jniParseFluidsReport() throws W21Exception;

    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a BSON structure using
     * InterpretedGeology mode.
     * * Upon success, the direct ByteBuffer ({@code bbBson}) will map to the
     * natively allocated BSON memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the BSON serialization process.
     */
    private native void jniParseInterpretedGeology() throws W21Exception;

    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a BSON structure using
     * InterpretedGeologyInterval mode.
     * * Upon success, the direct ByteBuffer ({@code bbBson}) will map to the
     * natively allocated BSON memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the BSON serialization process.
     */
    private native void jniParseInterpretedGeologyInterval() throws W21Exception;

    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a BSON structure using
     * Log mode.
     * * Upon success, the direct ByteBuffer ({@code bbBson}) will map to the
     * natively allocated BSON memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the BSON serialization process.
     */
    private native void jniParseLog() throws W21Exception;

    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a BSON structure using
     * LoggingToolKind mode.
     * * Upon success, the direct ByteBuffer ({@code bbBson}) will map to the
     * natively allocated BSON memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the BSON serialization process.
     */
    private native void jniParseLoggingToolKind() throws W21Exception;

    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a BSON structure using
     * LoggingToolKindDictionary mode.
     * * Upon success, the direct ByteBuffer ({@code bbBson}) will map to the
     * natively allocated BSON memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the BSON serialization process.
     */
    private native void jniParseLoggingToolKindDictionary() throws W21Exception;

    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a BSON structure using
     * MudLogReport mode.
     * * Upon success, the direct ByteBuffer ({@code bbBson}) will map to the
     * natively allocated BSON memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the BSON serialization process.
     */
    private native void jniParseMudLogReport() throws W21Exception;

    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a BSON structure using
     * MudlogReportInterval mode.
     * * Upon success, the direct ByteBuffer ({@code bbBson}) will map to the
     * natively allocated BSON memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the BSON serialization process.
     */
    private native void jniParseMudlogReportInterval() throws W21Exception;

    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a BSON structure using
     * OpsReport mode.
     * * Upon success, the direct ByteBuffer ({@code bbBson}) will map to the
     * natively allocated BSON memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the BSON serialization process.
     */
    private native void jniParseOpsReport() throws W21Exception;

    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a BSON structure using
     * PPFGChannel mode.
     * * Upon success, the direct ByteBuffer ({@code bbBson}) will map to the
     * natively allocated BSON memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the BSON serialization process.
     */
    private native void jniParsePPFGChannel() throws W21Exception;

    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a BSON structure using
     * PPFGChannelSet mode.
     * * Upon success, the direct ByteBuffer ({@code bbBson}) will map to the
     * natively allocated BSON memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the BSON serialization process.
     */
    private native void jniParsePPFGChannelSet() throws W21Exception;

    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a BSON structure using
     * PPFGLog mode.
     * * Upon success, the direct ByteBuffer ({@code bbBson}) will map to the
     * natively allocated BSON memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the BSON serialization process.
     */
    private native void jniParsePPFGLog() throws W21Exception;

    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a BSON structure using
     * Rig mode.
     * * Upon success, the direct ByteBuffer ({@code bbBson}) will map to the
     * natively allocated BSON memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the BSON serialization process.
     */
    private native void jniParseRig() throws W21Exception;

    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a BSON structure using
     * RigUtilization mode.
     * * Upon success, the direct ByteBuffer ({@code bbBson}) will map to the
     * natively allocated BSON memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the BSON serialization process.
     */
    private native void jniParseRigUtilization() throws W21Exception;

    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a BSON structure using
     * Risk mode.
     * * Upon success, the direct ByteBuffer ({@code bbBson}) will map to the
     * natively allocated BSON memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the BSON serialization process.
     */
    private native void jniParseRisk() throws W21Exception;

    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a BSON structure using
     * ShowEvaluation mode.
     * * Upon success, the direct ByteBuffer ({@code bbBson}) will map to the
     * natively allocated BSON memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the BSON serialization process.
     */
    private native void jniParseShowEvaluation() throws W21Exception;

    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a BSON structure using
     * ShowEvaluationInterval mode.
     * * Upon success, the direct ByteBuffer ({@code bbBson}) will map to the
     * natively allocated BSON memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the BSON serialization process.
     */
    private native void jniParseShowEvaluationInterval() throws W21Exception;

    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a BSON structure using
     * StimJob mode.
     * * Upon success, the direct ByteBuffer ({@code bbBson}) will map to the
     * natively allocated BSON memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the BSON serialization process.
     */
    private native void jniParseStimJob() throws W21Exception;

    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a BSON structure using
     * StimJob mode.
     * * Upon success, the direct ByteBuffer ({@code bbBson}) will map to the
     * natively allocated BSON memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the BSON serialization process.
     */
    private native void jniParseStimJobStage() throws W21Exception;

    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a BSON structure using
     * StimPerforationCluster mode.
     * * Upon success, the direct ByteBuffer ({@code bbBson}) will map to the
     * natively allocated BSON memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the BSON serialization process.
     */
    private native void jniParseStimPerforationCluster() throws W21Exception;

    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a BSON structure using
     * SurveyProgram mode.
     * * Upon success, the direct ByteBuffer ({@code bbBson}) will map to the
     * natively allocated BSON memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the BSON serialization process.
     */
    private native void jniParseSurveyProgram() throws W21Exception;

    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a BSON structure using
     * Target mode.
     * * Upon success, the direct ByteBuffer ({@code bbBson}) will map to the
     * natively allocated BSON memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the BSON serialization process.
     */
    private native void jniParseTarget() throws W21Exception;

    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a BSON structure using
     * ToolErrorModel mode.
     * * Upon success, the direct ByteBuffer ({@code bbBson}) will map to the
     * natively allocated BSON memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the BSON serialization process.
     */
    private native void jniParseToolErrorModel() throws W21Exception;

    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a BSON structure using
     * ToolErrorModelDictionary mode.
     * * Upon success, the direct ByteBuffer ({@code bbBson}) will map to the
     * natively allocated BSON memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the BSON serialization process.
     */
    private native void jniParseToolErrorModelDictionary() throws W21Exception;

    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a BSON structure using
     * Trajectory mode.
     * * Upon success, the direct ByteBuffer ({@code bbBson}) will map to the
     * natively allocated BSON memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the BSON serialization process.
     */
    private native void jniParseTrajectory() throws W21Exception;

    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a BSON structure using
     * TrajectoryStation mode.
     * * Upon success, the direct ByteBuffer ({@code bbBson}) will map to the
     * natively allocated BSON memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the BSON serialization process.
     */
    private native void jniParseTrajectoryStation() throws W21Exception;

    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a BSON structure using
     * Tubular mode.
     * * Upon success, the direct ByteBuffer ({@code bbBson}) will map to the
     * natively allocated BSON memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the BSON serialization process.
     */
    private native void jniParseTubular() throws W21Exception;

    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a BSON structure using
     * WeightingFunction mode.
     * * Upon success, the direct ByteBuffer ({@code bbBson}) will map to the
     * natively allocated BSON memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the BSON serialization process.
     */
    private native void jniParseWeightingFunction() throws W21Exception;

    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a BSON structure using
     * WeightingFunctionDictionary mode.
     * * Upon success, the direct ByteBuffer ({@code bbBson}) will map to the
     * natively allocated BSON memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the BSON serialization process.
     */
    private native void jniParseWeightingFunctionDictionary() throws W21Exception;

    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a BSON structure using
     * Well mode.
     * * Upon success, the direct ByteBuffer ({@code bbBson}) will map to the
     * natively allocated BSON memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the BSON serialization process.
     */
    private native void jniParseWell() throws W21Exception;

    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a BSON structure using
     * Wellbore mode.
     * * Upon success, the direct ByteBuffer ({@code bbBson}) will map to the
     * natively allocated BSON memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the BSON serialization process.
     */
    private native void jniParseWellbore() throws W21Exception;

    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a BSON structure using
     * WellboreCompletion mode.
     * * Upon success, the direct ByteBuffer ({@code bbBson}) will map to the
     * natively allocated BSON memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the BSON serialization process.
     */
    private native void jniParseWellboreCompletion() throws W21Exception;

    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a BSON structure using
     * WellboreGeology mode.
     * * Upon success, the direct ByteBuffer ({@code bbBson}) will map to the
     * natively allocated BSON memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the BSON serialization process.
     */
    private native void jniParseWellboreGeology() throws W21Exception;

    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a BSON structure using
     * WellboreGeometry mode.
     * * Upon success, the direct ByteBuffer ({@code bbBson}) will map to the
     * natively allocated BSON memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the BSON serialization process.
     */
    private native void jniParseWellboreGeometry() throws W21Exception;

    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a BSON structure using
     * WellboreGeometrySection mode.
     * * Upon success, the direct ByteBuffer ({@code bbBson}) will map to the
     * natively allocated BSON memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the BSON serialization process.
     */
    private native void jniParseWellboreGeometrySection() throws W21Exception;

    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a BSON structure using
     * WellboreMarker mode.
     * * Upon success, the direct ByteBuffer ({@code bbBson}) will map to the
     * natively allocated BSON memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the BSON serialization process.
     */
    private native void jniParseWellboreMarker() throws W21Exception;

    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a BSON structure using
     * WellboreMarkerSet mode.
     * * Upon success, the direct ByteBuffer ({@code bbBson}) will map to the
     * natively allocated BSON memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the BSON serialization process.
     */
    private native void jniParseWellboreMarkerSet() throws W21Exception;

    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a BSON structure using
     * WellCMLedger mode.
     * * Upon success, the direct ByteBuffer ({@code bbBson}) will map to the
     * natively allocated BSON memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the BSON serialization process.
     */
    private native void jniParseWellCMLedger() throws W21Exception;

    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a BSON structure using
     * WellCompletion mode.
     * * Upon success, the direct ByteBuffer ({@code bbBson}) will map to the
     * natively allocated BSON memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the BSON serialization process.
     */
    private native void jniParseWellCompletion() throws W21Exception;

    //==============================================================================//
    //============ INPUT: NATIVE WITSML 2.1 TO JSON STRING METHODS =================//
    //==============================================================================//
    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a JSON string structure using
     * AutoDetect mode.
     * * Upon success, the direct ByteBuffer ({@code bbJson}) will map to the
     * natively allocated JSON string memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the JSON serialization process.
     */
    private native void jniParseJson() throws W21Exception;

    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a JSON string structure using
     * BhaRun mode.
     * * Upon success, the direct ByteBuffer ({@code bbJson}) will map to the
     * natively allocated JSON string memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the JSON serialization process.
     */
    private native void jniParseJsonBhaRun() throws W21Exception;

    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a JSON string structure using
     * CementJob mode.
     * * Upon success, the direct ByteBuffer ({@code bbJson}) will map to the
     * natively allocated JSON string memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the JSON serialization process.
     */
    private native void jniParseJsonCementJob() throws W21Exception;

    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a JSON string structure using
     * CementJobEvaluation mode.
     * * Upon success, the direct ByteBuffer ({@code bbJson}) will map to the
     * natively allocated JSON string memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the JSON serialization process.
     */
    private native void jniParseJsonCementJobEvaluation() throws W21Exception;

    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a JSON string structure using
     * Channel mode.
     * * Upon success, the direct ByteBuffer ({@code bbJson}) will map to the
     * natively allocated JSON string memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the JSON serialization process.
     */
    private native void jniParseJsonChannel() throws W21Exception;

    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a JSON string structure using
     * ChannelKind mode.
     * * Upon success, the direct ByteBuffer ({@code bbJson}) will map to the
     * natively allocated JSON string memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the JSON serialization process.
     */
    private native void jniParseJsonChannelKind() throws W21Exception;

    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a JSON string structure using
     * ChannelKindDictionary mode.
     * * Upon success, the direct ByteBuffer ({@code bbJson}) will map to the
     * natively allocated JSON string memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the JSON serialization process.
     */
    private native void jniParseJsonChannelKindDictionary() throws W21Exception;

    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a JSON string structure using
     * ChannelSet mode.
     * * Upon success, the direct ByteBuffer ({@code bbJson}) will map to the
     * natively allocated JSON string memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the JSON serialization process.
     */
    private native void jniParseJsonChannelSet() throws W21Exception;

    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a JSON string structure using
     * CuttingsGeology mode.
     * * Upon success, the direct ByteBuffer ({@code bbJson}) will map to the
     * natively allocated JSON string memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the JSON serialization process.
     */
    private native void jniParseJsonCuttingsGeology() throws W21Exception;

    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a JSON string structure using
     * CuttingsGeologyInterval mode.
     * * Upon success, the direct ByteBuffer ({@code bbJson}) will map to the
     * natively allocated JSON string memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the JSON serialization process.
     */
    private native void jniParseJsonCuttingsGeologyInterval() throws W21Exception;

    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a JSON string structure using
     * DepthRegImage mode.
     * * Upon success, the direct ByteBuffer ({@code bbJson}) will map to the
     * natively allocated JSON string memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the JSON serialization process.
     */
    private native void jniParseJsonDepthRegImage() throws W21Exception;

    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a JSON string structure using
     * DownholeComponent mode.
     * * Upon success, the direct ByteBuffer ({@code bbJson}) will map to the
     * natively allocated JSON string memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the JSON serialization process.
     */
    private native void jniParseJsonDownholeComponent() throws W21Exception;

    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a JSON string structure using
     * DrillReport mode.
     * * Upon success, the direct ByteBuffer ({@code bbJson}) will map to the
     * natively allocated JSON string memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the JSON serialization process.
     */
    private native void jniParseJsonDrillReport() throws W21Exception;

    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a JSON string structure using
     * ErrorTerm mode.
     * * Upon success, the direct ByteBuffer ({@code bbJson}) will map to the
     * natively allocated JSON string memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the JSON serialization process.
     */
    private native void jniParseJsonErrorTerm() throws W21Exception;

    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a JSON string structure using
     * ErrorTermDictionary mode.
     * * Upon success, the direct ByteBuffer ({@code bbJson}) will map to the
     * natively allocated JSON string memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the JSON serialization process.
     */
    private native void jniParseJsonErrorTermDictionary() throws W21Exception;

    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a JSON string structure using
     * FluidsReport mode.
     * * Upon success, the direct ByteBuffer ({@code bbJson}) will map to the
     * natively allocated JSON string memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the JSON serialization process.
     */
    private native void jniParseJsonFluidsReport() throws W21Exception;

    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a JSON string structure using
     * InterpretedGeology mode.
     * * Upon success, the direct ByteBuffer ({@code bbJson}) will map to the
     * natively allocated JSON string memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the JSON serialization process.
     */
    private native void jniParseJsonInterpretedGeology() throws W21Exception;

    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a JSON string structure using
     * InterpretedGeologyInterval mode.
     * * Upon success, the direct ByteBuffer ({@code bbJson}) will map to the
     * natively allocated JSON string memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the JSON serialization process.
     */
    private native void jniParseJsonInterpretedGeologyInterval() throws W21Exception;

    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a JSON string structure using
     * Log mode.
     * * Upon success, the direct ByteBuffer ({@code bbJson}) will map to the
     * natively allocated JSON string memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the JSON serialization process.
     */
    private native void jniParseJsonLog() throws W21Exception;

    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a JSON string structure using
     * LoggingToolKind mode.
     * * Upon success, the direct ByteBuffer ({@code bbJson}) will map to the
     * natively allocated JSON string memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the JSON serialization process.
     */
    private native void jniParseJsonLoggingToolKind() throws W21Exception;

    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a JSON string structure using
     * LoggingToolKindDictionary mode.
     * * Upon success, the direct ByteBuffer ({@code bbJson}) will map to the
     * natively allocated JSON string memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the JSON serialization process.
     */
    private native void jniParseJsonLoggingToolKindDictionary() throws W21Exception;

    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a JSON string structure using
     * MudLogReport mode.
     * * Upon success, the direct ByteBuffer ({@code bbJson}) will map to the
     * natively allocated JSON string memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the JSON serialization process.
     */
    private native void jniParseJsonMudLogReport() throws W21Exception;

    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a JSON string structure using
     * MudlogReportInterval mode.
     * * Upon success, the direct ByteBuffer ({@code bbJson}) will map to the
     * natively allocated JSON string memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the JSON serialization process.
     */
    private native void jniParseJsonMudlogReportInterval() throws W21Exception;

    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a JSON string structure using
     * OpsReport mode.
     * * Upon success, the direct ByteBuffer ({@code bbJson}) will map to the
     * natively allocated JSON string memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the JSON serialization process.
     */
    private native void jniParseJsonOpsReport() throws W21Exception;

    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a JSON string structure using
     * PPFGChannel mode.
     * * Upon success, the direct ByteBuffer ({@code bbJson}) will map to the
     * natively allocated JSON string memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the JSON serialization process.
     */
    private native void jniParseJsonPPFGChannel() throws W21Exception;

    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a JSON string structure using
     * PPFGChannelSet mode.
     * * Upon success, the direct ByteBuffer ({@code bbJson}) will map to the
     * natively allocated JSON string memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the JSON serialization process.
     */
    private native void jniParseJsonPPFGChannelSet() throws W21Exception;

    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a JSON string structure using
     * PPFGLog mode.
     * * Upon success, the direct ByteBuffer ({@code bbJson}) will map to the
     * natively allocated JSON string memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the JSON serialization process.
     */
    private native void jniParseJsonPPFGLog() throws W21Exception;

    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a JSON string structure using
     * Rig mode.
     * * Upon success, the direct ByteBuffer ({@code bbJson}) will map to the
     * natively allocated JSON string memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the JSON serialization process.
     */
    private native void jniParseJsonRig() throws W21Exception;

    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a JSON string structure using
     * RigUtilization mode.
     * * Upon success, the direct ByteBuffer ({@code bbJson}) will map to the
     * natively allocated JSON string memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the JSON serialization process.
     */
    private native void jniParseJsonRigUtilization() throws W21Exception;

    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a JSON string structure using
     * Risk mode.
     * * Upon success, the direct ByteBuffer ({@code bbJson}) will map to the
     * natively allocated JSON string memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the JSON serialization process.
     */
    private native void jniParseJsonRisk() throws W21Exception;

    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a JSON string structure using
     * ShowEvaluation mode.
     * * Upon success, the direct ByteBuffer ({@code bbJson}) will map to the
     * natively allocated JSON string memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the JSON serialization process.
     */
    private native void jniParseJsonShowEvaluation() throws W21Exception;

    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a JSON string structure using
     * EvaluationInterval mode.
     * * Upon success, the direct ByteBuffer ({@code bbJson}) will map to the
     * natively allocated JSON string memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the JSON serialization process.
     */
    private native void jniParseJsonShowEvaluationInterval() throws W21Exception;

    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a JSON string structure using
     * StimJob mode.
     * * Upon success, the direct ByteBuffer ({@code bbJson}) will map to the
     * natively allocated JSON string memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the JSON serialization process.
     */
    private native void jniParseJsonStimJob() throws W21Exception;

    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a JSON string structure using
     * StimJobStage mode.
     * * Upon success, the direct ByteBuffer ({@code bbJson}) will map to the
     * natively allocated JSON string memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the JSON serialization process.
     */
    private native void jniParseJsonStimJobStage() throws W21Exception;

    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a JSON string structure using
     * StimPerforationCluster mode.
     * * Upon success, the direct ByteBuffer ({@code bbJson}) will map to the
     * natively allocated JSON string memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the JSON serialization process.
     */
    private native void jniParseJsonStimPerforationCluster() throws W21Exception;

    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a JSON string structure using
     * SurveyProgram mode.
     * * Upon success, the direct ByteBuffer ({@code bbJson}) will map to the
     * natively allocated JSON string memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the JSON serialization process.
     */
    private native void jniParseJsonSurveyProgram() throws W21Exception;

    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a JSON string structure using
     * Target mode.
     * * Upon success, the direct ByteBuffer ({@code bbJson}) will map to the
     * natively allocated JSON string memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the JSON serialization process.
     */
    private native void jniParseJsonTarget() throws W21Exception;

    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a JSON string structure using
     * ToolErrorModel mode.
     * * Upon success, the direct ByteBuffer ({@code bbJson}) will map to the
     * natively allocated JSON string memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the JSON serialization process.
     */
    private native void jniParseJsonToolErrorModel() throws W21Exception;

    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a JSON string structure using
     * ToolErrorModelDictionary mode.
     * * Upon success, the direct ByteBuffer ({@code bbJson}) will map to the
     * natively allocated JSON string memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the JSON serialization process.
     */
    private native void jniParseJsonToolErrorModelDictionary() throws W21Exception;

    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a JSON string structure using
     * Trajectory mode.
     * * Upon success, the direct ByteBuffer ({@code bbJson}) will map to the
     * natively allocated JSON string memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the JSON serialization process.
     */
    private native void jniParseJsonTrajectory() throws W21Exception;

    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a JSON string structure using
     * TrajectoryStation mode.
     * * Upon success, the direct ByteBuffer ({@code bbJson}) will map to the
     * natively allocated JSON string memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the JSON serialization process.
     */
    private native void jniParseJsonTrajectoryStation() throws W21Exception;

    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a JSON string structure using
     * Tubular mode.
     * * Upon success, the direct ByteBuffer ({@code bbJson}) will map to the
     * natively allocated JSON string memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the JSON serialization process.
     */
    private native void jniParseJsonTubular() throws W21Exception;

    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a JSON string structure using
     * WeightingFunction mode.
     * * Upon success, the direct ByteBuffer ({@code bbJson}) will map to the
     * natively allocated JSON string memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the JSON serialization process.
     */
    private native void jniParseJsonWeightingFunction() throws W21Exception;

    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a JSON string structure using
     * WeightingFunctionDictionary mode.
     * * Upon success, the direct ByteBuffer ({@code bbJson}) will map to the
     * natively allocated JSON string memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the JSON serialization process.
     */
    private native void jniParseJsonWeightingFunctionDictionary() throws W21Exception;

    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a JSON string structure using
     * Well mode.
     * * Upon success, the direct ByteBuffer ({@code bbJson}) will map to the
     * natively allocated JSON string memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the JSON serialization process.
     */
    private native void jniParseJsonWell() throws W21Exception;

    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a JSON string structure using
     * Wellbore mode.
     * * Upon success, the direct ByteBuffer ({@code bbJson}) will map to the
     * natively allocated JSON string memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the JSON serialization process.
     */
    private native void jniParseJsonWellbore() throws W21Exception;

    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a JSON string structure using
     * WellboreCompletion mode.
     * * Upon success, the direct ByteBuffer ({@code bbJson}) will map to the
     * natively allocated JSON string memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the JSON serialization process.
     */
    private native void jniParseJsonWellboreCompletion() throws W21Exception;

    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a JSON string structure using
     * WellboreGeology mode.
     * * Upon success, the direct ByteBuffer ({@code bbJson}) will map to the
     * natively allocated JSON string memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the JSON serialization process.
     */
    private native void jniParseJsonWellboreGeology() throws W21Exception;

    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a JSON string structure using
     * WellboreGeometry mode.
     * * Upon success, the direct ByteBuffer ({@code bbJson}) will map to the
     * natively allocated JSON string memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the JSON serialization process.
     */
    private native void jniParseJsonWellboreGeometry() throws W21Exception;

    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a JSON string structure using
     * WellboreGeometrySection mode.
     * * Upon success, the direct ByteBuffer ({@code bbJson}) will map to the
     * natively allocated JSON string memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the JSON serialization process.
     */
    private native void jniParseJsonWellboreGeometrySection() throws W21Exception;

    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a JSON string structure using
     * WellboreMarker mode.
     * * Upon success, the direct ByteBuffer ({@code bbJson}) will map to the
     * natively allocated JSON string memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the JSON serialization process.
     */
    private native void jniParseJsonWellboreMarker() throws W21Exception;

    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a JSON string structure using
     * WellboreMarkerSet mode.
     * * Upon success, the direct ByteBuffer ({@code bbJson}) will map to the
     * natively allocated JSON string memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the JSON serialization process.
     */
    private native void jniParseJsonWellboreMarkerSet() throws W21Exception;

    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a JSON string structure using
     * WellCMLedger mode.
     * * Upon success, the direct ByteBuffer ({@code bbJson}) will map to the
     * natively allocated JSON string memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the JSON serialization process.
     */
    private native void jniParseJsonWellCMLedger() throws W21Exception;

    /**
     * Triggers the main parsing engine for WITSML 2.1 data.
     * <p>
     * This method utilizes the internal C structures allocated by the gSOAP engine
     * to transform the previously loaded XML buffer into a JSON string structure using
     * WellCompletion mode.
     * * Upon success, the direct ByteBuffer ({@code bbJson}) will map to the
     * natively allocated JSON string memory block.
     * </p>
     * @throws W21Exception if the native parser encounters a structural
     * error during the JSON serialization process.
     */
    private native void jniParseJsonWellCompletion() throws W21Exception;

    private native long jniStatReadTotalCycles() throws W21Exception;
    private native long jniStatReadTotalNanos() throws W21Exception;
    private native long jniStatReadMem() throws W21Exception;

    private native long jniStatParseTotalCycles() throws W21Exception;
    private native long jniStatParseTotalNanos() throws W21Exception;
    private native long jniStatParseMem() throws W21Exception;

    private native long jniStatParseJsonTotalCycles() throws W21Exception;
    private native long jniStatParseJsonTotalNanos() throws W21Exception;
    private native long jniStatParseJsonMem() throws W21Exception;

    private native int jniStatCosts() throws W21Exception;
    private native int jniStatStrings() throws W21Exception;
    private native int jniStatShorts() throws W21Exception;
    private native int jniStatInts() throws W21Exception;
    private native int jniStatLong64s() throws W21Exception;
    private native int jniStatEnums() throws W21Exception;
    private native int jniStatArrays() throws W21Exception;
    private native int jniStatBooleans() throws W21Exception;
    private native int jniStatDoubles() throws W21Exception;
    private native int jniStatDateTimes() throws W21Exception;
    private native int jniStatMeasures() throws W21Exception;
    private native int jniStatEventTypes() throws W21Exception;
    private native int jniStatTotal() throws W21Exception;

    public W21ParserLoader() {
        this.BSON_CODEC = new RawBsonDocumentCodec();
        this.bbXml = ByteBuffer.allocateDirect(INITIAL_BYTE_BUFFER_SIZE);
        this.encoder = StandardCharsets.UTF_8.newEncoder();
        this.bbBson = null;
        this.bbJson = null;
    }

    public static W21ParserLoader begin() {
        return new W21ParserLoader();
    }

    public W21ParserLoader withInputWitsmlStrict() {
        this.inputConfig |= this.jniLoadSoapXmlStrict(); // DONE load SOAP_XML_STRICT from JNI
        return this;
    }

    public W21ParserLoader withIgnoreInputWitsmlNS() {
        this.inputConfig |= this.jniLoadSoapXmlIgnoreNS(); // DONE load SOAP_XML_IGNORENS from JNI
        return this;
    }

    public W21ParserLoader withResourceStats() {
        this.hasResourceStats = true;
        return this;
    }

    public W21ParserLoader withInputRulesValidator() throws W21Exception{
        this.inputRulesValidatorEnable = true;
        return this;
    }

    public W21ParserLoader build() throws Exception {
        this.jniInit(this.inputConfig, this.outputConfig, this.hasResourceStats);

        if (this.inputRulesValidatorEnable) {
            Exception ex = null;
            try {
                this.jniInputRulesValidatorEnable();
            } catch (Exception e) {
                ex = e;
            } finally {
                if (ex != null)
                    this.jniClose();
            }

            if (ex != null)
                throw ex;
        }

        return this;
    }

    public void readFromStream(String xml) throws W21Exception {
        readFromStream(xml, W21Object.AUTODETECT);
    }

    public void readFromStream(String xml, W21Object inputObject) throws W21Exception {
        int len = xml.length();
        if (len > this.bbXml.capacity())
            this.bbXml = ByteBuffer.allocateDirect(len);
        this.bbXml.clear();
        encoder.encode(CharBuffer.wrap(xml), this.bbXml, true);
        this.bbXml.flip();
        this.jniW21Recycle();
        prepareReadFromByteBuffer(inputObject);
    }

    /**
     * Loads and parses a WITSML file using <b>Auto-Detect</b> mode.
     * <p>This is a convenience method that clears the previous parser state and
     * automatically identifies the WITSML object type contained within the file.</p>
     *
     * @param path The path to the WITSML XML file.
     * @throws IOException If an I/O error occurs.
     * @throws W21Exception If the native engine fails to validate or parse the document.
     */
    private void prepareReadFile(String path) throws IOException {
        try (RandomAccessFile file = new RandomAccessFile(path, "r");
             FileChannel channel = file.getChannel()) {

            long fileSize = channel.size();

            if (fileSize > this.bbXml.capacity())
                this.bbXml = ByteBuffer.allocateDirect((int) fileSize);

            this.bbXml.clear();
            channel.read(bbXml);
            this.bbXml.flip();
        }
    }

    public void readFromFile(String path) throws IOException, W21Exception {
        readFromFile(path, W21Object.AUTODETECT);
    }

    private void prepareReadFromByteBuffer(W21Object inputObject) throws W21Exception {
        switch (this.inputObject = inputObject) {
            case AUTODETECT:
                this.jniReadFromByteBuffer(bbXml , bbXml.limit());
                break;
            case BhaRun:
                this.jniReadBhaRunFromByteBuffer(bbXml, bbXml.limit());
                break;
            case CementJob:
                this.jniReadCementJobFromByteBuffer(bbXml, bbXml.limit());
                break;
            case CementJobEvaluation:
                this.jniReadCementJobEvaluationFromByteBuffer(bbXml, bbXml.limit());
                break;
            case Channel:
                this.jniReadChannelFromByteBuffer(bbXml, bbXml.limit());
                break;
            case ChannelKind:
                this.jniReadChannelKindFromByteBuffer(bbXml, bbXml.limit());
                break;
            case ChannelKindDictionary:
                this.jniReadChannelKindDictionaryFromByteBuffer(bbXml, bbXml.limit());
                break;
            case ChannelSet:
                this.jniReadChannelSetFromByteBuffer(bbXml, bbXml.limit());
                break;
            case CuttingsGeology:
                this.jniReadCuttingsGeologyFromByteBuffer(bbXml, bbXml.limit());
                break;
            case CuttingsGeologyInterval:
                this.jniReadCuttingsGeologyIntervalFromByteBuffer(bbXml, bbXml.limit());
                break;
            case DepthRegImage:
                this.jniReadDepthRegImageFromByteBuffer(bbXml, bbXml.limit());
                break;
            case DownholeComponent:
                this.jniReadDownholeComponentFromByteBuffer(bbXml, bbXml.limit());
                break;
            case DrillReport:
                this.jniReadDrillReportFromByteBuffer(bbXml, bbXml.limit());
                break;
            case ErrorTerm:
                this.jniReadErrorTermFromByteBuffer(bbXml, bbXml.limit());
                break;
            case ErrorTermDictionary:
                this.jniReadErrorTermDictionaryFromByteBuffer(bbXml, bbXml.limit());
                break;
            case FluidsReport:
                this.jniReadFluidsReportFromByteBuffer(bbXml, bbXml.limit());
                break;
            case InterpretedGeology:
                this.jniReadInterpretedGeologyFromByteBuffer(bbXml, bbXml.limit());
                break;
            case InterpretedGeologyInterval:
                this.jniReadInterpretedGeologyIntervalFromByteBuffer(bbXml, bbXml.limit());
                break;
            case Log:
                this.jniReadLogFromByteBuffer(bbXml, bbXml.limit());
                break;
            case LoggingToolKind:
                this.jniReadLoggingToolKindFromByteBuffer(bbXml, bbXml.limit());
                break;
            case LoggingToolKindDictionary:
                this.jniReadLoggingToolKindDictionaryFromByteBuffer(bbXml, bbXml.limit());
                break;
            case MudLogReport:
                this.jniReadMudLogReportFromByteBuffer(bbXml, bbXml.limit());
                break;
            case MudlogReportInterval:
                this.jniReadMudlogReportIntervalFromByteBuffer(bbXml, bbXml.limit());
                break;
            case OpsReport:
                this.jniReadOpsReportFromByteBuffer(bbXml, bbXml.limit());
                break;
            case PPFGChannel:
                this.jniReadPPFGChannelFromByteBuffer(bbXml, bbXml.limit());
                break;
            case PPFGChannelSet:
                this.jniReadPPFGChannelSetFromByteBuffer(bbXml, bbXml.limit());
                break;
            case PPFGLog:
                this.jniReadPPFGLogFromByteBuffer(bbXml, bbXml.limit());
                break;
            case Rig:
                this.jniReadRigFromByteBuffer(bbXml, bbXml.limit());
                break;
            case RigUtilization:
                this.jniReadRigUtilizationFromByteBuffer(bbXml, bbXml.limit());
                break;
            case Risk:
                this.jniReadRiskFromByteBuffer(bbXml, bbXml.limit());
                break;
            case ShowEvaluation:
                this.jniReadShowEvaluationFromByteBuffer(bbXml, bbXml.limit());
                break;
            case ShowEvaluationInterval:
                this.jniReadShowEvaluationIntervalFromByteBuffer(bbXml, bbXml.limit());
                break;
            case StimJob:
                this.jniReadStimJobFromByteBuffer(bbXml, bbXml.limit());
                break;
            case StimJobStage:
                this.jniReadStimJobStageFromByteBuffer(bbXml, bbXml.limit());
                break;
            case StimPerforationCluster:
                this.jniReadStimPerforationClusterFromByteBuffer(bbXml, bbXml.limit());
                break;
            case SurveyProgram:
                this.jniReadSurveyProgramFromByteBuffer(bbXml, bbXml.limit());
                break;
            case Target:
                this.jniReadTargetFromByteBuffer(bbXml, bbXml.limit());
                break;
            case ToolErrorModel:
                this.jniReadToolErrorModelFromByteBuffer(bbXml, bbXml.limit());
                break;
            case ToolErrorModelDictionary:
                this.jniReadToolErrorModelDictionaryFromByteBuffer(bbXml, bbXml.limit());
                break;
            case Trajectory:
                this.jniReadTrajectoryFromByteBuffer(bbXml, bbXml.limit());
                break;
            case TrajectoryStation:
                this.jniReadTrajectoryStationFromByteBuffer(bbXml, bbXml.limit());
                break;
            case Tubular:
                this.jniReadTubularFromByteBuffer(bbXml, bbXml.limit());
                break;
            case WeightingFunction:
                this.jniReadWeightingFunctionFromByteBuffer(bbXml, bbXml.limit());
                break;
            case WeightingFunctionDictionary:
                this.jniReadWeightingFunctionDictionaryFromByteBuffer(bbXml, bbXml.limit());
                break;
            case Well:
                this.jniReadWellFromByteBuffer(bbXml, bbXml.limit());
                break;
            case Wellbore:
                this.jniReadWellboreFromByteBuffer(bbXml, bbXml.limit());
                break;
            case WellboreCompletion:
                this.jniReadWellboreCompletionFromByteBuffer(bbXml, bbXml.limit());
                break;
            case WellboreGeology:
                this.jniReadWellboreGeologyFromByteBuffer(bbXml, bbXml.limit());
                break;
            case WellboreGeometry:
                this.jniReadWellboreGeometryFromByteBuffer(bbXml, bbXml.limit());
                break;
            case WellboreGeometrySection:
                this.jniReadWellboreGeometrySectionFromByteBuffer(bbXml, bbXml.limit());
                break;
            case WellboreMarker:
                this.jniReadWellboreMarkerFromByteBuffer(bbXml, bbXml.limit());
                break;
            case WellboreMarkerSet:
                this.jniReadWellboreMarkerSetFromByteBuffer(bbXml, bbXml.limit());
                break;
            case WellCMLedger:
                this.jniReadWellCMLedgerFromByteBuffer(bbXml, bbXml.limit());
                break;
            case WellCompletion:
                this.jniReadWellCompletionFromByteBuffer(bbXml, bbXml.limit());
        }
    }

    /**
     * Loads a WITSML file into memory, recycles the native parser state, and executes
     * the parsing logic for a specific object type.
     * * <p>The process follows these steps:</p>
     * <ol>
     * <li>Reads the file into a dedicated {@code DirectByteBuffer}.</li>
     * <li>Calls {@code jniW21Recycle()} to ensure the native C engine is cleared of previous data.</li>
     * <li>Dispatches the buffer to the corresponding native JNI read method based on the {@code inputObject}.</li>
     * </ol>
     *
     * @param path The path to the WITSML XML file.
     * @param inputObject The specific {@link W21Object} type to parse (e.g., Well, Trajectory, or AUTODETECT).
     * @throws IOException If an I/O error occurs during file reading.
     * @throws W21Exception If the native C engine encounters a validation error,
     * schema mismatch, or parsing failure.
     */
    public void readFromFile(String path, W21Object inputObject) throws IOException, W21Exception {
        prepareReadFile(path);
        this.jniW21Recycle();
        prepareReadFromByteBuffer(inputObject);
    }

    public enum W21OutputJsonType {
        JSON_STRING, // Bson Object
        BYTE_ARRAY, // save/send
    }

    /**
     * Converts the previously parsed and validated WITSML 2.1 native C structure
     * into a JSON representation.
     * * <p>Similar to {@link #parse(W21OutputType)}, this method manages the transition
     * from the native C-struct to a Java-accessible format, specifically targeting
     * standard JSON. It utilizes the native engine's JSON serialization capabilities.</p>
     * * @param type The desired JSON output format (Standard String or Byte Array).
     * <ul>
     * <li>{@code JSON_STRING}: Returns a UTF-8 encoded {@link String}.</li>
     * <li>{@code BYTE_ARRAY}: Returns the raw JSON bytes, useful for high-performance
     * IO or custom decoding.</li>
     * </ul>
     * @return An {@link Object} (either {@code String} or {@code byte[]}) containing the JSON data.
     * @throws W21Exception If the native JSON engine fails or state mismatch occurs.
     * @throws Exception For encoding or memory access issues.
     */
    public Object parseJson(W21OutputJsonType type) throws Exception {
        boolean isAlreadyParsed = false;

        try {
            switch (this.inputObject) {
                case AUTODETECT:
                    this.jniParseJson();
                    break;
                case BhaRun:
                    this.jniParseJsonBhaRun();
                    break;
                case CementJob:
                    this.jniParseJsonCementJob();
                    break;
                case CementJobEvaluation:
                    this.jniParseJsonCementJobEvaluation();
                    break;
                case Channel:
                    this.jniParseJsonChannel();
                    break;
                case ChannelKind:
                    this.jniParseJsonChannelKind();
                    break;
                case ChannelKindDictionary:
                    this.jniParseJsonChannelKindDictionary();
                    break;
                case ChannelSet:
                    this.jniParseJsonChannelSet();
                    break;
                case CuttingsGeology:
                    this.jniParseJsonCuttingsGeology();
                    break;
                case CuttingsGeologyInterval:
                    this.jniParseJsonCuttingsGeologyInterval();
                    break;
                case DepthRegImage:
                    this.jniParseJsonDepthRegImage();
                    break;
                case DownholeComponent:
                    this.jniParseJsonDownholeComponent();
                    break;
                case DrillReport:
                    this.jniParseJsonDrillReport();
                    break;
                case ErrorTerm:
                    this.jniParseJsonErrorTerm();
                    break;
                case ErrorTermDictionary:
                    this.jniParseJsonErrorTermDictionary();
                    break;
                case FluidsReport:
                    this.jniParseJsonFluidsReport();
                    break;
                case InterpretedGeology:
                    this.jniParseJsonInterpretedGeology();
                    break;
                case InterpretedGeologyInterval:
                    this.jniParseJsonInterpretedGeologyInterval();
                    break;
                case Log:
                    this.jniParseJsonLog();
                    break;
                case LoggingToolKind:
                    this.jniParseJsonLoggingToolKind();
                    break;
                case LoggingToolKindDictionary:
                    this.jniParseJsonLoggingToolKindDictionary();
                    break;
                case MudLogReport:
                    this.jniParseJsonMudLogReport();
                    break;
                case MudlogReportInterval:
                    this.jniParseJsonMudlogReportInterval();
                    break;
                case OpsReport:
                    this.jniParseJsonOpsReport();
                    break;
                case PPFGChannel:
                    this.jniParseJsonPPFGChannel();
                    break;
                case PPFGChannelSet:
                    this.jniParseJsonPPFGChannelSet();
                    break;
                case PPFGLog:
                    this.jniParseJsonPPFGLog();
                    break;
                case Rig:
                    this.jniParseJsonRig();
                    break;
                case  RigUtilization:
                    this.jniParseJsonRigUtilization();
                    break;
                case Risk:
                    this.jniParseJsonRisk();
                    break;
                case ShowEvaluation:
                    this.jniParseJsonShowEvaluation();
                    break;
                case ShowEvaluationInterval:
                    this.jniParseJsonShowEvaluationInterval();
                    break;
                case StimJob:
                    this.jniParseJsonStimJob();
                    break;
                case StimJobStage:
                    this.jniParseJsonStimJobStage();
                    break;
                case StimPerforationCluster:
                    this.jniParseJsonStimPerforationCluster();
                    break;
                case SurveyProgram:
                    this.jniParseJsonSurveyProgram();
                    break;
                case Target:
                    this.jniParseJsonTarget();
                    break;
                case ToolErrorModel:
                    this.jniParseJsonToolErrorModel();
                    break;
                case ToolErrorModelDictionary:
                    this.jniParseJsonToolErrorModelDictionary();
                    break;
                case Trajectory:
                    this.jniParseJsonTrajectory();
                    break;
                case TrajectoryStation:
                    this.jniParseJsonTrajectoryStation();
                    break;
                case Tubular:
                    this.jniParseJsonTubular();
                    break;
                case WeightingFunction:
                    this.jniParseJsonWeightingFunction();
                    break;
                case WeightingFunctionDictionary:
                    this.jniParseJsonWeightingFunctionDictionary();
                    break;
                case Well:
                    this.jniParseJsonWell();
                    break;
                case Wellbore:
                    this.jniParseJsonWellbore();
                    break;
                case WellboreCompletion:
                    this.jniParseJsonWellboreCompletion();
                    break;
                case WellboreGeology:
                    this.jniParseJsonWellboreGeology();
                    break;
                case WellboreGeometry:
                    this.jniParseJsonWellboreGeometry();
                    break;
                case WellboreGeometrySection:
                    this.jniParseJsonWellboreGeometrySection();
                    break;
                case WellboreMarker:
                    this.jniParseJsonWellboreMarker();
                    break;
                case WellboreMarkerSet:
                    this.jniParseJsonWellboreMarkerSet();
                    break;
                case WellCMLedger:
                    this.jniParseJsonWellCMLedger();
                    break;
                case WellCompletion:
                    this.jniParseJsonWellCompletion();
            }
        } catch (W21Exception e) {
            //JNI_W21_JSON_STRING_ALREADY_PARSED = 4091
            if (!(isAlreadyParsed = e.error == 4091))
                throw e;
        }

        Exception ex = null;
        Object json = null;

        if (isAlreadyParsed) {
            this.bbJson.rewind();
            byte[] bytes = new byte[this.bbJson.remaining()];
            this.bbJson.get(bytes);
            if (type == W21OutputJsonType.BYTE_ARRAY)
                return bytes;

            return new String(bytes, StandardCharsets.UTF_8);
        }

        int err = this.jniBeginRead();
        if (err != 0)
            throw new W21Exception("jniBeginRead error at parseJson", err, "", "");

        try {
            this.bbJson.rewind();
            json = new byte[this.bbJson.remaining()];
            this.bbJson.get((byte [])json);

            if (type == W21OutputJsonType.JSON_STRING)
                json = new String((byte [])json, StandardCharsets.UTF_8);

        } catch (Exception e) {
            ex = e;
        } finally {
            err = this.jniEndRead();
        }

        if (err != 0)
            throw new W21Exception("jniEndRead() error at parseJson", err, "", "");

        if (ex == null)
            return json;

        throw ex;
    }

    public enum W21OutputType {
        //RAW_BSON,      // RawBsonDocument (Zero-Copy/Lazy)
        BSON,           // Bson Object
        BSON_BYTE_ARRAY, // save/send
        //MAP             // mapper TODO remove it
    }

    public enum W21Object {
        AUTODETECT,
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
    }

    /**
     * Orchestrates the conversion from the native C-struct to the desired Java BSON output.
     * * <p>This method performs a state-aware execution:</p>
     * <ul>
     * <li><b>Step 1:</b> Invokes the specific native parsing logic based on the {@code inputObject} type
     * (e.g., Well, Trajectory, BhaRun).</li>
     * <li><b>Step 2:</b> If the object was already parsed in a previous call (handled by error code 4090),
     * it reuses the existing BSON buffer.</li>
     * <li><b>Step 3:</b> Manages native memory access through a safe read-lock session
     * ({@code jniBeginRead} / {@code jniEndRead}).</li>
     * <li><b>Step 4:</b> Decodes the native BSON stream into either a raw {@code byte[]}
     * or a structured {@code BsonDocument}.</li>
     * </ul>
     *
     * @param type The requested output format (BSON Object or Byte Array).
     * @return The converted WITSML 2.1 data as an {@link Object} (castable to {@code byte[]} or {@code BsonDocument}).
     * @throws W21Exception If native parsing fails or if the gSOAP engine encounters validation errors.
     * @throws Exception For unexpected I/O or BSON decoding issues.
     */
    public Object parse(W21OutputType type) throws Exception {
        boolean isAlreadyParsed = false;
        try {
            switch (this.inputObject) {
                case AUTODETECT:
                    this.jniParse();
                    break;
                case BhaRun:
                    this.jniParseBhaRun();
                    break;
                case CementJob:
                    this.jniParseCementJob();
                    break;
                case CementJobEvaluation:
                    this.jniParseCementJobEvaluation();
                    break;
                case Channel:
                    this.jniParseChannel();
                    break;
                case ChannelKind:
                    this.jniParseChannelKind();
                    break;
                case ChannelKindDictionary:
                    this.jniParseChannelKindDictionary();
                    break;
                case ChannelSet:
                    this.jniParseChannelSet();
                    break;
                case CuttingsGeology:
                    this.jniParseCuttingsGeology();
                    break;
                case CuttingsGeologyInterval:
                    this.jniParseCuttingsGeologyInterval();
                    break;
                case DepthRegImage:
                    this.jniParseDepthRegImage();
                    break;
                case DownholeComponent:
                    this.jniParseDownholeComponent();
                    break;
                case DrillReport:
                    this.jniParseDrillReport();
                    break;
                case ErrorTerm:
                    this.jniParseErrorTerm();
                    break;
                case ErrorTermDictionary:
                    this.jniParseErrorTermDictionary();
                    break;
                case FluidsReport:
                    this.jniParseFluidsReport();
                    break;
                case InterpretedGeology:
                    this.jniParseInterpretedGeology();
                    break;
                case InterpretedGeologyInterval:
                    this.jniParseInterpretedGeologyInterval();
                    break;
                case Log:
                    this.jniParseLog();
                    break;
                case LoggingToolKind:
                    this.jniParseLoggingToolKind();
                    break;
                case LoggingToolKindDictionary:
                    this.jniParseLoggingToolKindDictionary();
                    break;
                case MudLogReport:
                    this.jniParseMudLogReport();
                    break;
                case MudlogReportInterval:
                    this.jniParseMudlogReportInterval();
                    break;
                case OpsReport:
                    this.jniParseOpsReport();
                    break;
                case PPFGChannel:
                    this.jniParsePPFGChannel();
                    break;
                case PPFGChannelSet:
                    this.jniParsePPFGChannelSet();
                    break;
                case PPFGLog:
                    this.jniParsePPFGLog();
                    break;
                case Rig:
                    this.jniParseRig();
                    break;
                case RigUtilization:
                    this.jniParseRigUtilization();
                    break;
                case Risk:
                    this.jniParseRisk();
                    break;
                case ShowEvaluation:
                    this.jniParseShowEvaluation();
                    break;
                case ShowEvaluationInterval:
                    this.jniParseShowEvaluationInterval();
                    break;
                case StimJob:
                    this.jniParseStimJob();
                    break;
                case StimJobStage:
                    this.jniParseStimJobStage();
                    break;
                case StimPerforationCluster:
                    this.jniParseStimPerforationCluster();
                    break;
                case SurveyProgram:
                    this.jniParseSurveyProgram();
                    break;
                case Target:
                    this.jniParseTarget();
                    break;
                case ToolErrorModel:
                    this.jniParseToolErrorModel();
                    break;
                case ToolErrorModelDictionary:
                    this.jniParseToolErrorModelDictionary();
                    break;
                case Trajectory:
                    this.jniParseTrajectory();
                    break;
                case TrajectoryStation:
                    this.jniParseTrajectoryStation();
                    break;
                case Tubular:
                    this.jniParseTubular();
                    break;
                case WeightingFunction:
                    this.jniParseWeightingFunction();
                    break;
                case WeightingFunctionDictionary:
                    this.jniParseWeightingFunctionDictionary();
                    break;
                case Well:
                    this.jniParseWell();
                    break;
                case Wellbore:
                    this.jniParseWellbore();
                    break;
                case WellboreCompletion:
                    this.jniParseWellboreCompletion();
                    break;
                case WellboreGeology:
                    this.jniParseWellboreGeology();
                    break;
                case WellboreGeometry:
                    this.jniParseWellboreGeometry();
                    break;
                case WellboreGeometrySection:
                    this.jniParseWellboreGeometrySection();
                    break;
                case WellboreMarker:
                    this.jniParseWellboreMarker();
                    break;
                case WellboreMarkerSet:
                    this.jniParseWellboreMarkerSet();
                    break;
                case WellCMLedger:
                    this.jniParseWellCMLedger();
                    break;
                case WellCompletion:
                    this.jniParseWellCompletion();
            }
        } catch (W21Exception e) {
            //JNI_W21_BSON_OBJECT_ALREADY_PARSED = 4090
            if (!(isAlreadyParsed = (e.error == 4090)))
                throw e;
        }

        Object obj = null;
        Exception ex = null;
        ByteBufNIO byteBuf = null;

        if (isAlreadyParsed) {
            this.bbBson.rewind();
            if (W21OutputType.BSON_BYTE_ARRAY == type) {
                obj = new byte[this.bbBson.remaining()];
                this.bbBson.get((byte[]) obj);
            } else if (W21OutputType.BSON == type) {
                byteBuf = new ByteBufNIO(this.bbBson);
                try (ByteBufferBsonInput bsonInput = new ByteBufferBsonInput(byteBuf);
                     BsonBinaryReader reader = new BsonBinaryReader(bsonInput)) {

                    // 3. "Lazy" decode
                    RawBsonDocument doc = BSON_CODEC.decode(reader, DecoderContext.builder().build());

                    obj = new BsonDocument();
                    ((BsonDocument) obj).putAll(doc);
                }
            } else
                throw new W21Exception("Invalid option: " + type, W21_INVALID_BSON_PARSING_OPTION, null, null);

            return obj;
        }

        int err = this.jniBeginRead();
        if (err != 0)
            throw new W21Exception("jniBeginRead error at jniParse()", err, "", "");

        if (W21OutputType.BSON_BYTE_ARRAY == type) {

            try {
                this.bbBson.rewind();
                obj = new byte[this.bbBson.remaining()];
                this.bbBson.get((byte[]) obj);
            } catch (Exception e) {
                ex = e;
            } finally {
                err = jniEndRead();
            }

            if (err != 0)
                throw new W21Exception("jniEndRead() error on BSON to Byte Array parsing", err, "", "");

            if (ex == null)
                return obj;

            throw ex;
        }

        try {
            // 1. Wrapper ByteBufNIO
            byteBuf = new ByteBufNIO(this.bbBson);
        } catch (Exception e) {
            ex = e;
        } finally {
            if (ex != null) {
                err = this.jniEndRead();
            }
        }

        if (ex == null) {
            // 2. Input and Reader native access
            try (ByteBufferBsonInput bsonInput = new ByteBufferBsonInput(byteBuf);
                BsonBinaryReader reader = new BsonBinaryReader(bsonInput)) {

                // 3. "Lazy" decode
                RawBsonDocument doc = BSON_CODEC.decode(reader, DecoderContext.builder().build());

                if (type == W21OutputType.BSON) {
                    obj = new BsonDocument();
                    ((BsonDocument) obj).putAll(doc);
                } else
                    throw new W21Exception("Invalid option: " + type, W21_INVALID_BSON_PARSING_OPTION, null, null);

            } catch (Exception e) {
                ex = e;
            } finally {
                err = this.jniEndRead();
            }

            if (err != 0)
                throw new W21Exception("jniEndRead() error", err, "", "");

            if (ex == null)
                return obj;

            throw ex;
        }

        if (err != 0)
            throw new W21Exception("jniEndRead() error on exception", err, "", "");

        throw ex;
    }

    public Object parse() throws Exception {
        this.jniParse();

        int err = this.jniBeginRead();
        if (err != 0)
            throw new W21Exception("jniBeginRead error", err, "", "");

        Exception ex = null;

        ByteBufNIO byteBuf = null;
        try {
            // 1. Wrapper ByteBufNIO
            byteBuf = new ByteBufNIO(this.bbBson);
        } catch (Exception e) {
            ex = e;
        } finally {
            if (ex != null)
                err = this.jniEndRead();
        }

        Object obj = null;
        if (ex == null) {
            // 2. Input and Reader native access
            try (ByteBufferBsonInput bsonInput = new ByteBufferBsonInput(byteBuf);
                 BsonBinaryReader reader = new BsonBinaryReader(bsonInput)) {

                // 3. "Lazy" decode
                RawBsonDocument doc = BSON_CODEC.decode(reader, DecoderContext.builder().build());

                //BsonDocument safeDoc = new BsonDocument();
                //safeDoc.putAll(doc);
                //obj = (Object) safeDoc;
                // Check if exists (Do not exist. Only for test)
                if (doc.containsKey("wellName")) {
                    System.out.println("Well: " + doc.getString("wellName").getValue());
                }

            } catch (Exception e) {
                ex = e;
            } finally {
                err = this.jniEndRead();
            }

            if (err != 0)
                throw new W21Exception("jniEndRead() error", err, "", "");

            if (ex == null)
                return obj;

            throw ex;
        }

        if (err != 0)
            throw new W21Exception("jniEndRead() error on exception", err, "", "");

        throw ex;
    }
    
    public int close() {
        return this.jniClose();
    }

    public W21TelemetrySummary21 loadStatistics() throws W21Exception {

        if (this.hasResourceStats) {
            W21TelemetrySummary21 w21TelemetrySummary = new W21TelemetrySummary21(
                    this.jniInGetObjectName(),
                    this.bbXml.limit()
            );

            long summaryTotalCycles = this.jniStatReadTotalCycles();
            long summaryTotalNanos = this.jniStatReadTotalNanos();
            long summaryTotalMem = this.jniStatReadMem();
            long tmp;

            w21TelemetrySummary.setReadTotalCycles(summaryTotalCycles);
            w21TelemetrySummary.setReadTotalNanos(summaryTotalNanos);
            w21TelemetrySummary.setReadMem(summaryTotalMem);

            boolean hasParsingStat = true;

            try {
                tmp = this.jniStatParseTotalCycles();
                summaryTotalCycles += tmp;
                w21TelemetrySummary.setParseTotalCycles(tmp);
                tmp = this.jniStatParseTotalNanos();
                summaryTotalNanos += tmp;
                w21TelemetrySummary.setParseTotalNanos(tmp);
                tmp = this.jniStatParseMem();
                summaryTotalMem += tmp;
                w21TelemetrySummary.setParseMem(tmp);
            } catch (W21Exception e) {
                // JNI_W21_RESOURCE_STAT_NOT_FINISHED_CORRECTLY_OR_NOT_PARSED = 4074
                if (e.error != 4074)
                    throw e;

                hasParsingStat = false;
            }

            boolean hasParsingJsonStat = true;

            try {
                tmp = this.jniStatParseJsonTotalCycles();
                summaryTotalCycles += tmp;
                w21TelemetrySummary.setParseJsonTotalCycles(tmp);
                tmp = this.jniStatParseJsonTotalNanos();
                summaryTotalNanos += tmp;
                w21TelemetrySummary.setParseJsonTotalNanos(tmp);
                tmp = this.jniStatParseJsonMem();
                summaryTotalMem += tmp;
                w21TelemetrySummary.setParseJsonMem(tmp);
            } catch (W21Exception e) {
                // JNI_W21_RESOURCE_STAT_JSON_NOT_FINISHED_CORRECTLY_OR_NOT_PARSED = 4075
                if (e.error != 4075)
                    throw e;

                hasParsingJsonStat = false;
            }

            if (hasParsingStat || hasParsingJsonStat) {
                w21TelemetrySummary.setCosts(this.jniStatCosts());
                w21TelemetrySummary.setStrings(this.jniStatStrings());
                w21TelemetrySummary.setShorts(this.jniStatShorts());
                w21TelemetrySummary.setInts(this.jniStatInts());
                w21TelemetrySummary.setLong64s(this.jniStatLong64s());
                w21TelemetrySummary.setEnums(this.jniStatEnums());
                w21TelemetrySummary.setArrays(this.jniStatArrays());
                w21TelemetrySummary.setBooleans(this.jniStatBooleans());
                w21TelemetrySummary.setDoubles(this.jniStatDoubles());
                w21TelemetrySummary.setDateTimes(this.jniStatDateTimes());
                w21TelemetrySummary.setMeasures(this.jniStatMeasures());
                w21TelemetrySummary.setEventTypes(this.jniStatEventTypes());
                w21TelemetrySummary.setTotal(this.jniStatTotal());
            }
            w21TelemetrySummary.setHasReadingStat(true);
            w21TelemetrySummary.setHasParsingStat(hasParsingStat);
            w21TelemetrySummary.setHasParsingJsonStat(hasParsingJsonStat);
            w21TelemetrySummary.setSummaryTotalCycles(summaryTotalCycles);
            w21TelemetrySummary.setSummaryTotalNanos(summaryTotalNanos);
            w21TelemetrySummary.setSummaryTotalMem(summaryTotalMem);

            return w21TelemetrySummary;

            //throw new W21Exception("Unable loading WITSML 2.1 statistics", W21Errors.W21_UNABLE_LOAD_STATISTICS, null, null);
        }

        throw new W21Exception("WITSML 2.1 document and resources statistics are not initialized", W21Errors.W21_LOAD_STATISTICS_NOT_INITIALIZED, null, null);
    }

    public void benchmarkPerformanceRealista() throws Exception {
        // 1. Prepare
        String xmlContent = Files.readString(Paths.get("../../xmls/OpsReport2.xml"));
        this.bbXml.clear();
        encoder.encode(CharBuffer.wrap(xmlContent), this.bbXml, true);
        this.bbXml.flip();

        int iteracoes = 10000;

        // Warm-up: 100 iter to JIT and Cache L3 "wake"
        for(int i=0; i<100; i++) {
            this.jniW21Recycle();
            this.jniReadFromByteBuffer(this.bbXml, this.bbXml.limit());
            this.parse();
        }

        // --- INITIAL ---
        long start = System.nanoTime();
        //boolean error = false;
        for (int i = 0; i < iteracoes; i++) {

            // 2. Recycle C (Mandatory)
            this.jniW21Recycle();

                // 3. AVX2 parsing
            this.jniReadFromByteBuffer(this.bbXml, (long)this.bbXml.limit());
            this.parse();
        }

        long end = System.nanoTime();
        // -------------------------------

        double totalSeconds = (end - start) / 1e9;
        long totalBytes = (long)this.bbXml.limit() * iteracoes;

        System.out.printf("Total time: %.4f s\n", totalSeconds);
        System.out.printf("Throughput: %.2f MB/s\n", (totalBytes / 1024.0 / 1024.0) / totalSeconds);
        System.out.printf("Latency: %.4f ms\n", (totalSeconds / iteracoes) * 1000);

        System.out.println(this.loadStatistics());
        this.close();
    }
}
