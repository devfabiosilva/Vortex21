package org.w21parser.common;

import org.bson.BsonDocument;

import static org.w21parser.common.Utils.*;

public class LogChannelAxis implements BsonDeserializable {
    public final String uid;
    public final Double axisStart;
    public final Double axisSpacing;
    public final Long axisCount;
    public final String axisName;
    public final String axisPropertyKind;
    public final String axisUom;
    private BsonDocument doc = null;

    private LogChannelAxis(String uid, Double axisStart, Double axisSpacing, Long axisCount, String axisName, String axisPropertyKind, String axisUom, BsonDocument doc) {
        this.uid = uid;
        this.axisStart = axisStart;
        this.axisSpacing = axisSpacing;
        this.axisCount = axisCount;
        this.axisName = axisName;
        this.axisPropertyKind = axisPropertyKind;
        this.axisUom = axisUom;
        this.doc = doc;
    }

    public static LogChannelAxis build(String uid, Double axisStart, Double axisSpacing, Long axisCount, String axisName, String axisPropertyKind, String axisUom, BsonDocument doc) {
        return new LogChannelAxis(uid, axisStart, axisSpacing, axisCount, axisName, axisPropertyKind, axisUom, doc);
    }

    public static LogChannelAxis build(String uid, Double axisStart, Double axisSpacing, Long axisCount, String axisName, String axisPropertyKind, String axisUom) {
        return new LogChannelAxis(uid, axisStart, axisSpacing, axisCount, axisName, axisPropertyKind, axisUom, null);
    }

    public void setLogChannelAxis(BsonDocument doc) {
        this.doc = doc;
    }

    @Override
    public void test() throws Exception {
        testStringAttribute(this.uid, "uid", this.doc);
        testDouble(this.axisStart, "AxisStart", this.doc);
        testDouble(this.axisSpacing, "AxisSpacing", this.doc);
        testLong(this.axisCount, "AxisCount", this.doc);
        testString(this.axisName, "AxisName", this.doc);
        testString(this.axisPropertyKind, "AxisPropertyKind", this.doc);
        testString(this.axisUom, "AxisUom", this.doc);
    }

    @Override
    public void setBsonDocument(BsonDocument doc) {
        this.doc = doc;
    }
}
