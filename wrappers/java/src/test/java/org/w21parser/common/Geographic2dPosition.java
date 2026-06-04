package org.w21parser.common;

import org.bson.BsonDocument;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.w21parser.common.Utils.testDouble;
import static org.w21parser.strictObject.BhaRunTest.navigate;

public class Geographic2dPosition {
    public PlaneAngleMeasureExt latitude = null;
    public PlaneAngleMeasureExt longitude = null;
    public Double epoch = null;
    public DataObjectReference geographicCrs = null;
    private BsonDocument doc = null;

    private Geographic2dPosition(PlaneAngleMeasureExt latitude, PlaneAngleMeasureExt longitude, Double epoch, DataObjectReference geographicCrs, BsonDocument doc) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.epoch = epoch;
        this.geographicCrs = geographicCrs;
        this.doc = doc;
    }

    public static Geographic2dPosition build(PlaneAngleMeasureExt latitude, PlaneAngleMeasureExt longitude, Double epoch, DataObjectReference geographicCrs, BsonDocument doc) {
        return new Geographic2dPosition(latitude, longitude, epoch, geographicCrs, doc);
    }

    public static Geographic2dPosition build(PlaneAngleMeasureExt latitude, PlaneAngleMeasureExt longitude, Double epoch, DataObjectReference geographicCrs) {
        return new Geographic2dPosition(latitude, longitude, epoch, geographicCrs, null);
    }

    public void test() throws Exception {
        BsonDocument latitude = (BsonDocument) navigate(this.doc, "Latitude");
        if (this.latitude != null) {
            assertNotNull(latitude);
            this.latitude.setPlaneAngleMeasure(latitude);
            this.latitude.test();
        } else
            assertNull(latitude);

        BsonDocument longitude = (BsonDocument)navigate(this.doc, "Longitude");
        if (this.longitude != null) {
            assertNotNull(longitude);
            this.longitude.setPlaneAngleMeasure(longitude);
            this.longitude.test();
        } else
            assertNull(longitude);

        testDouble(this.epoch, "Epoch", this.doc);

        BsonDocument geographicCrs = (BsonDocument) navigate(this.doc, "GeographicCrs");
        if (this.geographicCrs != null) {
            assertNotNull(geographicCrs);
            this.geographicCrs.setDataObjectReference(geographicCrs);
            this.geographicCrs.test();
        } else
            assertNull(geographicCrs);
    }

    public void setGeographic2dPosition(BsonDocument doc) {
        this.doc = doc;
    }
}
