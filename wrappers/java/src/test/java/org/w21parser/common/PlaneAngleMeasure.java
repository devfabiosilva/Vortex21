package org.w21parser.common;

import org.bson.BsonDocument;

import static org.w21parser.common.Utils.*;

public class PlaneAngleMeasure {
    public final String uom;
    public final Double value;
    private BsonDocument doc;

    private PlaneAngleMeasure(String uom, Double value, BsonDocument doc) {
        this.uom = uom;
        this.value = value;
        this.doc = doc;
    }

    public static PlaneAngleMeasure build(String uom, Double value, BsonDocument doc) {
        return new PlaneAngleMeasure(uom, value, doc);
    }

    public void test() throws Exception {
        testStringAttribute(this.uom, "uom", doc);
        testDouble(this.value, "#value", doc);
    }

    public void setPlaneAngleMeasure(BsonDocument doc) {
        this.doc = doc;
    }
}
