package org.w21parser.common;

import org.bson.BsonDateTime;
import org.bson.BsonDocument;
import org.bson.BsonInt64;
import org.bson.BsonValue;
import org.w21parser.DateUtils;

import static org.junit.Assert.*;
import static org.w21parser.strictObject.BhaRunTest.navigate;

public class ExtensionNameValue implements BsonDeserializable {
    final String name;
    final String valueUom;
    final String value;
    final String measureClass;
    final String dtim;
    final Long index;
    final String description;
    private BsonDocument doc;

    public ExtensionNameValue(String name, String valueUom, String value, String measureClass, String dtim, Long index, String description,
                              BsonDocument doc) {
        this.name = name;
        this.valueUom = valueUom;
        this.value = value;
        this.measureClass = measureClass;
        this.dtim = dtim;
        this.index = index;
        this.description = description;
        this.doc = doc;
    }

    public static ExtensionNameValue build(String name, String valueUom, String value, String measureClass, String dtim, Long index, String description,
                                           BsonDocument extensionNameValue) throws Exception {
        return new ExtensionNameValue(name, valueUom, value, measureClass, dtim, index, description, extensionNameValue);
    }

    public static ExtensionNameValue build(String name, String valueUom, String value, String measureClass, String dtim, Long index, String description) throws Exception {
        return new ExtensionNameValue(name, valueUom, value, measureClass, dtim, index, description, null);
    }

    @Override
    public void test() throws Exception {
        String actualName = (String)navigate(doc, "Name");
        if (name != null) {
            assertNotNull(actualName);
            assertEquals(name, actualName);
        } else
            assertNull(actualName);

        String actualValueUom = (String)navigate(doc, "Value", "#attributes", "uom");
        if (valueUom != null) {
            assertNotNull(actualValueUom);
            assertEquals(valueUom, actualValueUom);
        } else
            assertNull(actualValueUom);

        String actualValue = (String) navigate(doc, "Value", "#value");
        if (value != null) {
            assertNotNull(actualValue);
            assertEquals(value, actualValue);
        } else
            assertNull(actualValue);

        String actualMeasureClass = (String)navigate(doc, "MeasureClass");
        if (measureClass != null) {
            assertNotNull(actualMeasureClass);
            assertEquals(measureClass, actualMeasureClass);
        } else
            assertNull(actualMeasureClass);

        BsonDateTime actualDTim = (BsonDateTime) navigate(doc, "DTim");
        if (dtim != null) {
            assertNotNull(actualDTim);
            assertEquals(DateUtils.toTimestamp(dtim), ((BsonDateTime)actualDTim).getValue());
        } else
            assertNull(actualDTim);


        BsonInt64 actualIndex = (BsonInt64)navigate(doc, "Index");
        if (index != null) {
            assertNotNull(actualIndex);
            assertEquals(index.longValue(), actualIndex.getValue());
        } else
            assertNull(actualIndex);

        String actualDescription = (String)navigate(doc, "Description");
        if (description != null) {
            assertNotNull(actualDescription);
            assertEquals(description, actualDescription);
        } else
            assertNull(actualDescription);
    }

    @Override
    public void setBsonDocument(BsonDocument doc) {
        this.doc = doc;
    }

    //TODO remove
    void setExtensionNameValue(BsonValue doc) {
        this.doc = doc.asDocument();
    }
}
