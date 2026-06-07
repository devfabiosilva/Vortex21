package org.w21parser.common;

import org.bson.BsonDocument;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.w21parser.common.Utils.testDouble;
import static org.w21parser.strictObject.BhaRunTest.navigate;

public class ProjectedCompoundPosition implements BsonDeserializable {
    public Double coordinate1 = null;
    public Double coordinate2 = null;
    public Double verticalCoordinate = null;
    public ProjectedCompoundCrs projectedCompoundCrs = null;
    private BsonDocument doc = null;

    private ProjectedCompoundPosition(Double coordinate1, Double coordinate2, Double verticalCoordinate, ProjectedCompoundCrs projectedCompoundCrs, BsonDocument doc) {
        this.coordinate1 = coordinate1;
        this.coordinate2 = coordinate2;
        this.verticalCoordinate = verticalCoordinate;
        this.projectedCompoundCrs = projectedCompoundCrs;
        this.doc = doc;
    }

    public static ProjectedCompoundPosition build(Double coordinate1, Double coordinate2, Double verticalCoordinate, ProjectedCompoundCrs projectedCompoundCrs, BsonDocument doc) {
        return new ProjectedCompoundPosition(coordinate1, coordinate2, verticalCoordinate, projectedCompoundCrs, doc);
    }

    public static ProjectedCompoundPosition build(Double coordinate1, Double coordinate2, Double verticalCoordinate, ProjectedCompoundCrs projectedCompoundCrs) {
        return new ProjectedCompoundPosition(coordinate1, coordinate2, verticalCoordinate, projectedCompoundCrs, null);
    }

    @Override
    public void test() throws Exception {
        testDouble(this.coordinate1, "Coordinate1", this.doc);
        testDouble(this.coordinate2, "Coordinate2", this.doc);
        testDouble(this.verticalCoordinate, "VerticalCoordinate", this.doc);
        BsonDocument projectedCompoundCrs = (BsonDocument) navigate(this.doc, "ProjectedCompoundCrs");
        if (this.projectedCompoundCrs != null) {
            assertNotNull(projectedCompoundCrs);
            this.projectedCompoundCrs.setBsonDocument(projectedCompoundCrs);
            this.projectedCompoundCrs.test();
        } else
            assertNull(projectedCompoundCrs);
    }

    @Override
    public void setBsonDocument(BsonDocument doc) {
        this.doc = doc;
    }
}
