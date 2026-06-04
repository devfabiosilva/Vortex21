package org.w21parser.common;

import org.bson.BsonDocument;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.w21parser.strictObject.BhaRunTest.navigate;

public class DatumElevation {
    public PlaneAngleMeasureExt elevation = null;
    public DataObjectReference datum = null;
    private BsonDocument doc = null;

    private DatumElevation(PlaneAngleMeasureExt elevation, DataObjectReference datum, BsonDocument doc) {
        this.elevation = elevation;
        this.datum = datum;
        this.doc = doc;
    }

    public static DatumElevation build(PlaneAngleMeasureExt elevation, DataObjectReference datum, BsonDocument doc) {
        return new DatumElevation(elevation, datum, doc);
    }

    public static DatumElevation build(PlaneAngleMeasureExt elevation, DataObjectReference datum) {
        return new DatumElevation(elevation, datum, null);
    }

    public void test() throws Exception {
        BsonDocument elevation = (BsonDocument) navigate(this.doc, "Elevation");
        if (this.elevation != null) {
            assertNotNull(elevation);
            this.elevation.setPlaneAngleMeasure(elevation);
            this.elevation.test();
        } else
            assertNull(elevation);

        BsonDocument datum = (BsonDocument) navigate(this.doc, "Datum");
        if (this.datum != null) {
            assertNotNull(datum);
            this.datum.setDataObjectReference(datum);
            this.datum.test();
        } else
            assertNull(datum);
    }

    public void setDatumElevation(BsonDocument doc) {
        this.doc = doc;
    }
}
