package org.w21parser.common;

import org.bson.*;
import org.w21parser.DateUtils;

import java.util.List;

import static org.junit.Assert.*;
import static org.w21parser.strictObject.BhaRunTest.navigate;

public class Utils {

    static void testDouble(Double expected, BsonDouble actual) {
        if (expected != null) {
            assertNotNull(actual);
            assertEquals(expected, actual.getValue(), 1E-6);
        } else
            assertNull(actual);
    }

    static void testDouble(Double expected, String key, BsonDocument doc) throws Exception {
        testDouble(expected, (BsonDouble) navigate(doc, key));
    }

    static void testLong(Long expected, BsonInt64 actual) {
        if (expected != null) {
            assertNotNull(actual);
            assertEquals(expected.longValue(), actual.getValue());
        } else
            assertNull(actual);
    }

    static void testLong(Long expected, String key, BsonDocument doc) throws Exception {
        testLong(expected, (BsonInt64) navigate(doc, key));
    }

    static void testString(String expected, String actual) {
        if (expected != null) {
            assertNotNull(actual);
            assertEquals(expected, actual);
        } else
            assertNull(actual);
    }

    static void testString(String expected, String key, BsonDocument doc) throws Exception {
        testString(expected, (String)navigate(doc, key));
    }

    static void testStringAttribute(String expected, String key, BsonDocument doc) throws Exception {
        testString(expected, (String)navigate(doc, "#attributes", key));
    }

    static void testDateTime(String expected, BsonDateTime bdt) {
        if (expected != null) {
            assertNotNull(bdt);
            assertEquals(DateUtils.toTimestamp(expected), bdt.asDateTime().getValue());
        } else
            assertNull(bdt);
    }

    static void testDateTime(String expected, String key, BsonDocument doc) throws Exception {
        testDateTime(expected, (BsonDateTime)navigate(doc, key));
    }

    static void testStringList(List<String> expected, BsonArray actual) {
        if (expected != null) {
            assertNotNull(actual);
            assertEquals(expected.size(), actual.size());
            for (int i = 0; i < expected.size(); i++)
                assertEquals(expected.get(i), actual.get(i).asString().getValue());
        } else
            assertNull(actual);
    }

    static void testStringList(List<String> expected, String key, BsonDocument doc) throws Exception {
        testStringList(expected, (BsonArray) navigate(doc, key));
    }
}
