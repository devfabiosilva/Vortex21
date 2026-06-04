package org.w21parser.common;

import org.bson.BsonDocument;

public class PlaneAngleMeasureExt extends PlaneAngleMeasure {
    private PlaneAngleMeasureExt(String uom, Double value, BsonDocument doc) {
        super(uom, value, doc);
    }

    public static PlaneAngleMeasureExt build(String uom, Double value, BsonDocument doc) {
        return new PlaneAngleMeasureExt(uom, value, doc);
    }

    public static PlaneAngleMeasureExt build(String uom, Double value) {
        return new PlaneAngleMeasureExt(uom, value, null);
    }
}
