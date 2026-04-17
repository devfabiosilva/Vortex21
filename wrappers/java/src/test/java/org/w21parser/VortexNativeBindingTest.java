package org.w21parser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

public class VortexNativeBindingTest {

    private static final Logger logger = LoggerFactory.getLogger(W21ParserLoader.class);
    private W21ParserLoader parser1;
    private W21ParserLoader parser2 = null;
    private int expectedCloseStatusParser1 = 0;
    private int expectedCloseStatusParser2 = 0;

    @Before
    public void setUp() throws Exception {
        this.parser1 = W21ParserLoader.begin().withInputRulesValidator().withInputWitsmlStrict().withResourceStats().withIgnoreInputWitsmlNS().build();
    }

    @After
    public void tearDown() {
        int errParser1 = this.parser1.close();
        int errParser2 = (this.parser2 != null)?(this.parser2.close()):0;
        assertEquals("Parser 1 close method must return " + this.expectedCloseStatusParser1, this.expectedCloseStatusParser1, errParser1);
        assertEquals("Parser 2 close method must return " + this.expectedCloseStatusParser2, this.expectedCloseStatusParser2, errParser2);
    }

    @Test
    public void assertAllNativeMethodsAreLinked() {
        Method[] methods = W21ParserLoader.class.getDeclaredMethods();
        for (Method m : methods) {
            if (m.getName().startsWith("jni") && Modifier.isNative(m.getModifiers())) {
                assertNotNull("Mapped native method check failed: " + m.getName(), m);
            }
        }
    }

    public static String fromPath(String object) {
        return "TestFiles/xmls/strict_valid/" + object + ".xml";
    }

    @Test
    public void validateErrorCodesContractTest() throws W21Exception, IOException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        // Tests if JNI_W21_BSON_OBJECT_ALREADY_PARSED = 4090 for native BSON alloc'd and
        // JNI_W21_JSON_STRING_ALREADY_PARSED = 4091 JSON string alloc'd is returned from C code
        // when parsed object is already allocated
        //
        // On loading document and CPU/Memory statistics:
        // Test if JNI_W21_RESOURCE_STAT_NOT_FINISHED_CORRECTLY_OR_NOT_PARSED = 4074 for BSON not parsed or finished
        // Test if JNI_W21_RESOURCE_STAT_JSON_NOT_FINISHED_CORRECTLY_OR_NOT_PARSED = 4075 for JSON not parsed or finished
        parser1.readFromFile(fromPath("BhaRun"));

        Method jniMethodStat1 = W21ParserLoader.class.getDeclaredMethod("jniStatParseTotalCycles");
        jniMethodStat1.setAccessible(true);

        try {
            jniMethodStat1.invoke(parser1);
            fail("jniStatParseTotalCycles method should NOT load CPU cycles before BSON parsing");
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof W21Exception) {
                W21Exception ex = (W21Exception) cause;
                assertEquals("JNI contract violated: Error code JNI_W21_RESOURCE_STAT_NOT_FINISHED_CORRECTLY_OR_NOT_PARSED changed in C code",
                        4074, ex.error);
            } else
                throw e;
        }

        Method jniMethodStat2 = W21ParserLoader.class.getDeclaredMethod("jniStatParseJsonTotalCycles");
        jniMethodStat2.setAccessible(true);

        try {
            jniMethodStat2.invoke(parser1);
            fail("jniStatParseJsonTotalCycles method should NOT load CPU cycles before JSON string parsing");
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof W21Exception) {
                W21Exception ex = (W21Exception) cause;
                assertEquals("JNI contract violated: Error code JNI_W21_RESOURCE_STAT_JSON_NOT_FINISHED_CORRECTLY_OR_NOT_PARSED changed in C code",
                        4075, ex.error);
            } else
                throw e;
        }
        // ALLOC'D BSON OBJECT
        // Access private native method
        Method jniMethod = W21ParserLoader.class.getDeclaredMethod("jniParse");
        jniMethod.setAccessible(true);

        // First success execution: invoke C structs to BSON object transformation
        jniMethod.invoke(parser1);

        assertTrue("CPU cycles on BSON parsing should be greater than 0", (Long)jniMethodStat1.invoke(parser1) > 0);

        try {
            // Second execution: Should throw exception because
            // allocated BSON object in C pointer variable is already loaded
            jniMethod.invoke(parser1);
            fail("jniParse method should not execute again a new BSON object for same WITSML object");
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof W21Exception) {
                W21Exception ex = (W21Exception) cause;
                assertEquals("JNI contract violated: Error code JNI_W21_BSON_OBJECT_ALREADY_PARSED changed in C code",
                        4090, ex.error);
            } else
                throw e;
        }

        // ALLOC'D JSON UTF-8 STRING

        // Access private native method
        jniMethod = W21ParserLoader.class.getDeclaredMethod("jniParseJson");
        jniMethod.setAccessible(true);

        // First success execution: invoke C structs to UTF-8 JSON string transformation
        jniMethod.invoke(parser1);

        assertTrue("CPU cycles on JSON string parsing should be greater than 0", (Long)jniMethodStat2.invoke(parser1) > 0);

        try {
            // Second execution: Should throw exception because
            // allocated UTF-8 JSON string in C pointer variable is already loaded
            jniMethod.invoke(parser1);
            fail("jniParseJson method should not execute again a new UTF-8 JSON string for same WITSML object");
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof W21Exception) {
                W21Exception ex = (W21Exception) cause;
                assertEquals("JNI contract violated: Error code JNI_W21_JSON_STRING_ALREADY_PARSED changed in C code",
                        4091, ex.error);
            } else
                throw e;
        }
    }

    private void assertJniError(Method method, W21ParserLoader instance, String msgOnError, int expectedError, Object... args) throws IllegalAccessException, InvocationTargetException {
        try {
            method.invoke(instance, args);
            fail("Should have thrown W21Exception with expected code " + expectedError);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof W21Exception)
                assertEquals(msgOnError, expectedError, ((W21Exception) e.getCause()).error);
            else
                throw e;
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T getPrivateField(Object instance, String fieldName) throws Exception {
        Field field = instance.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return (T) field.get(instance);
    }

    private void setPrivateField(Object instance, String fieldName, Object value) throws Exception {
        Field field = instance.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(instance, value);
    }

    private String getStringFromByteBuffer(ByteBuffer bb){
        bb.rewind();
        byte[] bytes = new byte[bb.remaining()];
        bb.get(bytes);

        return new String(bytes, StandardCharsets.UTF_8);
    }

    @Test
    public void memoryBoundaryTest() throws Exception {
        // This test try to manipulate and test access violation in C pointer.
        // Please, ignore message from JNI such as:
        //[w21parser] jniParse unexpected: Unable to load C pointer at jni_get_handler: 4043. Ignoring ...
        //[w21parser] jniClose: Unexpected error. Unable to close this instance. jni_get_handler error: 4043. Memory may be leaked. Try later
        //[w21parser] jniParseJson unexpected: Unable to load C pointer at jni_get_handler: 4044. Ignoring ...
        //[w21parser] jniParseJson unexpected: Unable to load C pointer at jni_get_handler: 4043. Ignoring ...
        //[w21parser] jniParseJson unexpected: locker not found or not initialized
        // Ignore messages above. These messages are expected because we are violating C memory access
        // simulating attack or wrong use. This test SHOULD PASS without crash your VM :)

        logger.warn("============================ BEGIN CONTROLLED JNI ACCESS VIOLATION TEST ============================");
        logger.warn("THIS TEST TRY TO MANIPULATE AND TEST MEMORY ACCESS VIOLATION IN C POINTER.\nPlease, ignore message from JNI [w21parser] below");

        long oldJniHandler = getPrivateField(this.parser1, "jniHandler");
        assertTrue("jniHandler must be mapped (NOT NULL C pointer)", oldJniHandler != 0);

        long oldJniHandlerCheck = getPrivateField(this.parser1, "jniHandlerCheck");
        assertTrue("jniHandlerCheck must be NOT ZERO", oldJniHandlerCheck != 0);

        this.parser1.readFromFile(fromPath("BhaRun"));

        // Access violation intentional in C pointer. JNI SHOULD detect such violation without crash VM
        setPrivateField(this.parser1, "jniHandler", oldJniHandler + 1);

        try {
            this.parser1.parse(W21ParserLoader.W21OutputType.BSON);
            fail("parser1.parse() should not execute/access C memory access violation");
        } catch (W21Exception e) {
            //JNI_W21_ERROR_GET_HANDLER_CHECK_ERROR 4043
            assertEquals("JNI contract violated: Error code JNI_W21_ERROR_GET_HANDLER_CHECK_ERROR changed in C code", 4043, e.error);
        } finally {
            setPrivateField(this.parser1, "jniHandler", oldJniHandler);
        }

        // Access violation in C pointer checker. JNI SHOULD NOT execute/close/access invalid allocated instance
        setPrivateField(this.parser1, "jniHandlerCheck", oldJniHandlerCheck + 1);

        assertEquals("parser1.close() should not close an invalid check or memory", 4043, this.parser1.close());

        setPrivateField(this.parser1, "jniHandlerCheck", oldJniHandlerCheck);

        setPrivateField(this.parser1, "jniHandler", 0);

        try {
            this.parser1.parseJson(W21ParserLoader.W21OutputJsonType.JSON_STRING);
            fail("parser1.parseJson() could not try to access C NULL pointer");
        } catch (W21Exception e) {
            // JNI_W21_ERROR_GET_HANDLER_INCONSISTENT 4044
            assertEquals("JNI contract violated: Error code JNI_W21_ERROR_GET_HANDLER_INCONSISTENT changed in C code", 4044, e.error);
        } finally {
            setPrivateField(this.parser1, "jniHandler", oldJniHandler);
        }

        setPrivateField(this.parser1, "jniHandlerCheck", 0);

        try {
            this.parser1.parseJson(W21ParserLoader.W21OutputJsonType.BYTE_ARRAY);
            fail("parser1.parseJson() could not try to access C pointer with invalid check");
        } catch (W21Exception e) {
            // JNI_W21_ERROR_GET_HANDLER_CHECK_ERROR 4043
            assertEquals("JNI contract violated: Error code JNI_W21_ERROR_GET_HANDLER_INCONSISTENT changed in C code", 4043, e.error);
        } finally {
            setPrivateField(this.parser1, "jniHandlerCheck", oldJniHandlerCheck);
        }

        setPrivateField(this.parser1, "jniHandler", 0);
        setPrivateField(this.parser1, "jniHandlerCheck", 0);

        try {
            this.parser1.parseJson(W21ParserLoader.W21OutputJsonType.BYTE_ARRAY);
            fail("parser1.parseJson() could not try to access C pointer with null pointer");
        } catch (Exception e) {
            assertEquals("Invalid message from jniParseJson", "jniParseJson unexpected: locker not found or not initialized. Unable to execute", e.getMessage());
        } finally {
            setPrivateField(this.parser1, "jniHandlerCheck", oldJniHandlerCheck);
            setPrivateField(this.parser1, "jniHandler", oldJniHandler);
        }

        logger.warn("IGNORE MESSAGES ABOVE. THESE MESSAGES ARE EXPECTED BECAUSE WE ARE TRYING TO VIOLATE C MEMORY ACCESS SIMULATING ATTACK OR WRONG USE. This test SHOULD PASS without crash your VM :)");
        logger.warn("============================= END CONTROLLED JNI ACCESS VIOLATION TEST =============================");
    }

    @Test
    public void memoryBoundaryInByteBufferTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, W21Exception {
        Method jniMethod = W21ParserLoader.class.getDeclaredMethod("jniReadOpsReportFromByteBuffer", ByteBuffer.class, long.class);
        jniMethod.setAccessible(true);
        //JNI_W21_DIRECT_BUFFER_INPUT_BUFFER 4064
        assertJniError(
                jniMethod, this.parser1,
                "JNI should reject null byte buffer",
                4064,
                null,
                0
        );

        ByteBuffer bb = ByteBuffer.allocateDirect(0);

        //JNI_W21_DIRECT_BUFFER_INPUT_BUFFER_INVALID_SIZE 4065
        assertJniError(
                jniMethod, this.parser1,
                "JNI should reject empty byte buffer",
                4065,
                bb,
                0
        );

        bb = ByteBuffer.allocateDirect(1);
        //JNI_W21_DIRECT_BUFFER_INPUT_BUFFER_INVALID_SIZE 4065
        assertJniError(
                jniMethod, this.parser1,
                "JNI should reject byte buffer with invalid size (buffer overflow)",
                4065,
                bb,
                bb.limit()*2
        );

        try {
            this.parser1.readFromStream("<InvalidXml />");
            fail("Could not execute an invalid stream");
        } catch (W21Exception e) {
            //SOAP_TAG_MISMATCH 3
            if (e.error != 3)
                throw e;
        }

        try {
            this.parser1.readFromStream("text here");
            fail("Could not execute an invalid text stream");
        } catch (W21Exception e) {
            //SOAP_END_TAG 53
            if (e.error != 53)
                throw e;
        }

        try {
            this.parser1.readFromStream("");
            fail("Could not execute an invalid EMPTY stream");
        } catch (W21Exception e) {
            //JNI_W21_DIRECT_BUFFER_INPUT_BUFFER_INVALID_SIZE 4065
            if (e.error != 4065)
                throw e;
        }

        try {
            this.parser1.readFromStream(null);
            fail("Could not execute an invalid NULL stream");
        } catch (NullPointerException e) {
            logger.warn("Test NPE guard. PASS", e);
        }
    }

    @Test
    public void validateInitializationTest() throws Exception {
        Method jniMethod = W21ParserLoader.class.getDeclaredMethod("jniInit", long.class, long.class, boolean.class);
        jniMethod.setAccessible(true);

        this.parser2 = new W21ParserLoader();

        //JNI_W21_ERROR_INIT_INPUT = 4030
        assertJniError(
                jniMethod, this.parser2,
                "JNI contract violated: Error code JNI_W21_ERROR_INIT_INPUT changed in C code",
                4030,
                1234,
                0,
                true
        );

        //JNI_W21_ERROR_INIT_OUTPUT = 4031
        assertJniError(
                jniMethod, this.parser2,
                "JNI contract violated: Error code JNI_W21_ERROR_INIT_OUTPUT changed in C code",
                4031,
                0,
                5678,
                false
        );

        long jniHandler = getPrivateField(this.parser2, "jniHandler");
        assertEquals("jniHandler must be mapped to 0 (NULL C pointer) before initialize", 0, jniHandler);

        long jniHandlerCheck = getPrivateField(this.parser2, "jniHandlerCheck");
        assertEquals("jniHandlerCheck must be 0 before initialize", 0, jniHandlerCheck);

        ByteBuffer bbBson = getPrivateField(this.parser2, "bbBson");
        assertNull("bbBson must initialize with null value", bbBson);

        ByteBuffer bbJson = getPrivateField(this.parser2, "bbJson");
        assertNull("bbBson must initialize with null value", bbJson);

        jniMethod.invoke(this.parser2, 0, 0, false);

        jniHandler = getPrivateField(this.parser2, "jniHandler");
        assertTrue("jniHandler must be mapped to not zero (NOT NULL C pointer) after jniInit call", jniHandler != 0);

        jniHandlerCheck = getPrivateField(this.parser2, "jniHandlerCheck");
        assertTrue("jniHandlerCheck must be NOT ZERO after jniInit call", jniHandlerCheck != 0);

        bbBson = getPrivateField(this.parser2, "bbBson");
        assertNull("bbBson must initialize with null value", bbBson);

        bbJson = getPrivateField(this.parser2, "bbJson");
        assertNull("bbBson must initialize with null value", bbJson);

        //JNI_W21_ERROR_INIT_ALREADY_CLOSED 4033
        this.expectedCloseStatusParser2 = 4033;

        assertEquals("parser2.close() must return 0", 0, this.parser2.close());

        jniHandler = getPrivateField(this.parser2, "jniHandler");
        assertEquals("jniHandler must be set to zero (NULL C pointer) after parser2.close() call", 0, jniHandler);

        jniHandlerCheck = getPrivateField(this.parser2, "jniHandlerCheck");
        assertEquals("jniHandlerCheck must be ZERO after parser2.close() call", 0, jniHandlerCheck);

        final String textInCSecurePointer = "WITSML-2.1-SECURE-POINTER";
        bbBson = getPrivateField(this.parser2, "bbBson");
        assertNotNull("bbBson must finalize with non null value", bbBson);
        assertEquals("Expected C secure pointer in bbBson after close", textInCSecurePointer, getStringFromByteBuffer(bbBson));

        bbJson = getPrivateField(this.parser2, "bbJson");
        assertNotNull("bbBson must finalize with non null value", bbJson);
        assertEquals("Expected C secure pointer in bbJson after close", textInCSecurePointer, getStringFromByteBuffer(bbJson));
    }
}
