package org.w21parser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static org.junit.Assert.*;

public class VortexNativeBindingTest {

    private W21ParserLoader parser1;

    @Before
    public void setUp() throws Exception {
        parser1 = W21ParserLoader.begin().withInputRulesValidator().withInputWitsmlStrict().withResourceStats().withIgnoreInputWitsmlNS().build();
    }

    @After
    public void tearDown() {
        assertEquals("Parser 1 close method must return 0", 0, parser1.close());
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

    @Test
    public void validateErrorCodesContractTest() throws W21Exception, IOException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        // Tests if JNI_W21_BSON_OBJECT_ALREADY_PARSED = 4090 for native BSON alloc'd and
        // JNI_W21_JSON_STRING_ALREADY_PARSED = 4091 JSON string alloc'd is returned from C code
        // when parsed object is already allocated
        //
        // On loading document and CPU/Memory statistics:
        // Test if JNI_W21_RESOURCE_STAT_NOT_FINISHED_CORRECTLY_OR_NOT_PARSED = 4074 for BSON not parsed or finished
        // Test if JNI_W21_RESOURCE_STAT_JSON_NOT_FINISHED_CORRECTLY_OR_NOT_PARSED = 4075 for JSON not parsed or finished
        parser1.readFromFile("TestFiles/xmls/strict_valid/BhaRun.xml");

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
            }
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
            }
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
            }
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
            }
        }
    }
}
