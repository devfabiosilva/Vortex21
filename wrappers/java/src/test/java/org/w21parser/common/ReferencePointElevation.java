package org.w21parser.common;

import org.bson.BsonDocument;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.w21parser.strictObject.BhaRunTest.navigate;

public class ReferencePointElevation {
    public PlaneAngleMeasureExt elevation = null;
    public DataObjectReference referencePoint = null;
    private BsonDocument doc = null;

    private ReferencePointElevation(PlaneAngleMeasureExt elevation, DataObjectReference referencePoint, BsonDocument doc) {
        this.elevation = elevation;
        this.referencePoint = referencePoint;
        this.doc = doc;
    }

    public static ReferencePointElevation build(PlaneAngleMeasureExt elevation, DataObjectReference referencePoint, BsonDocument doc) {
        return new ReferencePointElevation(elevation, referencePoint, doc);
    }

    public static ReferencePointElevation build(PlaneAngleMeasureExt elevation, DataObjectReference referencePoint) {
        return new ReferencePointElevation(elevation, referencePoint, null);
    }

    public void test() throws Exception {
        BsonDocument elevation = (BsonDocument) navigate(this.doc, "Elevation");
        if (this.elevation != null) {
            assertNotNull(elevation);
            this.elevation.setPlaneAngleMeasure(elevation);
            this.elevation.test();
        } else
            assertNull(elevation);

        BsonDocument referencePoint = (BsonDocument) navigate(this.doc, "ReferencePoint");
        if (this.referencePoint != null) {
            assertNotNull(referencePoint);
            this.referencePoint.setDataObjectReference(referencePoint);
            this.referencePoint.test();
        } else
            assertNull(referencePoint);
    }

    public void setReferencePointElevation(BsonDocument doc) {
        this.doc = doc;
    }
}
