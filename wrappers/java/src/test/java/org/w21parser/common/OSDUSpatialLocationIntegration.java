package org.w21parser.common;

import org.bson.BsonDocument;

import java.util.List;

import static org.w21parser.common.Utils.*;

public class OSDUSpatialLocationIntegration {
    public final String spatialLocationCoordinatesDate;
    public final String quantitativeAccuracyBand;
    public final String qualitativeSpatialAccuracyType;
    public final String coordinateQualityCheckPerformedBy;
    public final String coordinateQualityCheckDateTime;
    public final List<String> coordinateQualityCheckRemark;
    public final List<String> appliedOperation;
    private BsonDocument doc;

    private OSDUSpatialLocationIntegration(String spatialLocationCoordinatesDate, String quantitativeAccuracyBand, String qualitativeSpatialAccuracyType, String coordinateQualityCheckPerformedBy, String coordinateQualityCheckDateTime, List<String> coordinateQualityCheckRemark, List<String> appliedOperation, BsonDocument doc) {
        this.spatialLocationCoordinatesDate = spatialLocationCoordinatesDate;
        this.quantitativeAccuracyBand = quantitativeAccuracyBand;
        this.qualitativeSpatialAccuracyType = qualitativeSpatialAccuracyType;
        this.coordinateQualityCheckPerformedBy = coordinateQualityCheckPerformedBy;
        this.coordinateQualityCheckDateTime = coordinateQualityCheckDateTime;
        this.coordinateQualityCheckRemark = coordinateQualityCheckRemark;
        this.appliedOperation = appliedOperation;
        this.doc = doc;
    }

    public static OSDUSpatialLocationIntegration build(String spatialLocationCoordinatesDate, String quantitativeAccuracyBand, String qualitativeSpatialAccuracyType, String coordinateQualityCheckPerformedBy, String coordinateQualityCheckDateTime, List<String> coordinateQualityCheckRemark, List<String> appliedOperation, BsonDocument doc) {
        return new OSDUSpatialLocationIntegration(spatialLocationCoordinatesDate, quantitativeAccuracyBand, qualitativeSpatialAccuracyType, coordinateQualityCheckPerformedBy, coordinateQualityCheckDateTime, coordinateQualityCheckRemark, appliedOperation, doc);
    }

    public void test() throws Exception {
        testDateTime(this.spatialLocationCoordinatesDate, "SpatialLocationCoordinatesDate", this.doc);
        testString(this.quantitativeAccuracyBand, "QuantitativeAccuracyBand", this.doc);
        testString(this.qualitativeSpatialAccuracyType, "QualitativeSpatialAccuracyType" , this.doc);
        testString(this.coordinateQualityCheckPerformedBy, "CoordinateQualityCheckPerformedBy" , this.doc);
        testDateTime(this.coordinateQualityCheckDateTime,"CoordinateQualityCheckDateTime" , this.doc);
        testStringList(this.coordinateQualityCheckRemark, "CoordinateQualityCheckRemark" , this.doc);
        testStringList(this.appliedOperation, "AppliedOperation" , this.doc);
    }

    public void setOSDUSpatialLocationIntegration(BsonDocument doc) {
        this.doc = doc;
    }
}
