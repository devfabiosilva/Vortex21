package org.w21parser.common;

import org.bson.BsonDateTime;
import org.bson.BsonDocument;
import org.bson.BsonInt64;
import org.bson.BsonValue;
import org.w21parser.DateUtils;

import static org.junit.Assert.*;
import static org.w21parser.strictObject.BhaRunTest.navigate;

public class ExtensionNameValue {
    final String name;
    final String valueUom;
    final String value;
    final String measureClass;
    final String dtim;
    final Long index;
    final String description;
    BsonValue extensionNameValue;

    public ExtensionNameValue(String name, String valueUom, String value, String measureClass, String dtim, Long index, String description,
                              BsonDocument extensionNameValue) {
        this.name = name;
        this.valueUom = valueUom;
        this.value = value;
        this.measureClass = measureClass;
        this.dtim = dtim;
        this.index = index;
        this.description = description;
        this.extensionNameValue = extensionNameValue;
    }

    public static ExtensionNameValue build(String name, String valueUom, String value, String measureClass, String dtim, Long index, String description,
                                           BsonDocument extensionNameValue) throws Exception {
        return new ExtensionNameValue(name, valueUom, value, measureClass, dtim, index, description, extensionNameValue);
    }

    public void test() throws Exception {
        String actualName = (String)navigate(extensionNameValue, "Name");
        if (name != null) {
            assertNotNull(actualName);
            assertEquals(name, actualName);
        } else
            assertNull(actualName);

        String actualValueUom = (String)navigate(extensionNameValue, "Value", "#attributes", "uom");
        if (valueUom != null) {
            assertNotNull(actualValueUom);
            assertEquals(valueUom, actualValueUom);
        } else
            assertNull(actualValueUom);

        String actualValue = (String) navigate(extensionNameValue, "Value", "#value");
        if (value != null) {
            assertNotNull(actualValue);
            assertEquals(value, actualValue);
        } else
            assertNull(actualValue);

        String actualMeasureClass = (String)navigate(extensionNameValue, "MeasureClass");
        if (measureClass != null) {
            assertNotNull(actualMeasureClass);
            assertEquals(measureClass, actualMeasureClass);
        } else
            assertNull(actualMeasureClass);

        BsonDateTime actualDTim = (BsonDateTime) navigate(extensionNameValue, "DTim");
        if (dtim != null) {
            assertNotNull(actualDTim);
            assertEquals(DateUtils.toTimestamp(dtim), ((BsonDateTime)actualDTim).getValue());
        } else
            assertNull(actualDTim);


        BsonInt64 actualIndex = (BsonInt64)navigate(extensionNameValue, "Index");
        if (index != null) {
            assertNotNull(actualIndex);
            assertEquals(index.longValue(), actualIndex.getValue());
        } else
            assertNull(actualIndex);

        String actualDescription = (String)navigate(extensionNameValue, "Description");
        if (description != null) {
            assertNotNull(actualDescription);
            assertEquals(description, actualDescription);
        } else
            assertNull(actualDescription);
    }

    void setExtensionNameValue(BsonValue extensionNameValue) {
        this.extensionNameValue = extensionNameValue;
    }
}
