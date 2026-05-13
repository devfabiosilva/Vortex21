package org.w21parser.common;

import org.bson.BsonArray;
import org.bson.BsonDocument;

import java.util.List;
import static org.junit.Assert.*;
import static org.w21parser.strictObject.BhaRunTest.navigate;

public class CustomData {
    public List<String> customDataValues;
    private BsonDocument doc;

    private CustomData(List<String> customDataValues, BsonDocument doc) {
        this.customDataValues = customDataValues;
        this.doc = doc;
    }

    public static CustomData build(List<String> customDataValues, BsonDocument doc) {
        return new CustomData(customDataValues, doc);
    }

    public void test() throws Exception {
        BsonArray customData = (BsonArray) navigate(this.doc, "CustomData");
        if (this.customDataValues != null) {
            assertNotNull(customData);
            assertEquals(this.customDataValues.size(), customData.size());
            for (int i = 0; i < this.customDataValues.size(); i++)
                assertEquals(this.customDataValues.get(i), customData.get(i).asString().getValue().trim());
        } else
            assertNull(customData);
    }
}
