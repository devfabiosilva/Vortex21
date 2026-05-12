package org.w21parser.common;

import org.bson.BsonArray;
import org.bson.BsonDateTime;
import org.bson.BsonDocument;
import org.w21parser.DateUtils;

import java.util.List;

import static org.junit.Assert.*;
import static org.w21parser.strictObject.BhaRunTest.navigate;

public class Utils {
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
