package org.w21parser.common;

import org.bson.BsonDocument;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.w21parser.common.Utils.testDouble;
import static org.w21parser.strictObject.BhaRunTest.navigate;

public class Projected2dPosition {
    public Double coordinate1 = null;
    public Double coordinate2 = null;
    public DataObjectReference projectedCrs = null;
    private BsonDocument doc;

    private Projected2dPosition(Double coordinate1, Double coordinate2, DataObjectReference projectedCrs, BsonDocument doc) {
        this.coordinate1 = coordinate1;
        this.coordinate2 = coordinate2;
        this.projectedCrs = projectedCrs;
        this.doc = doc;
    }

    public static Projected2dPosition build(Double coordinate1, Double coordinate2, DataObjectReference projectedCrs, BsonDocument doc) {
        return new Projected2dPosition(coordinate1, coordinate2, projectedCrs, doc);
    }

    public static Projected2dPosition build(Double coordinate1, Double coordinate2, DataObjectReference projectedCrs) {
        return new Projected2dPosition(coordinate1, coordinate2, projectedCrs, null);
    }

    public void test() throws Exception {
        testDouble(this.coordinate1, "Coordinate1", this.doc);
        testDouble(this.coordinate2, "Coordinate2", this.doc);

        BsonDocument projectedCrs = (BsonDocument) navigate(this.doc, "ProjectedCrs");
        if (this.projectedCrs != null) {
            assertNotNull(projectedCrs);
            this.projectedCrs.setDataObjectReference(projectedCrs);
            this.projectedCrs.test();
        } else
            assertNull(projectedCrs);
    }

    public void setProjected2dPosition(BsonDocument doc) {
        this.doc = doc;
    }
}
