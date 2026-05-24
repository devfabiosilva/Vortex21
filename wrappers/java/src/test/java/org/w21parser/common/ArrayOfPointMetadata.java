package org.w21parser.common;

import org.bson.BsonArray;

import java.util.List;

import static org.junit.Assert.*;

public class ArrayOfPointMetadata {

    public List<PointMetadata> pointMetadataList = null;
    private BsonArray docArray = null;

    private ArrayOfPointMetadata(List<PointMetadata> pointMetadataList, BsonArray docArray) {
        this.pointMetadataList = pointMetadataList;
        this.docArray = docArray;
    }

    public static ArrayOfPointMetadata build(List<PointMetadata> pointMetadataList, BsonArray docArray) {
        return new ArrayOfPointMetadata(pointMetadataList, docArray);
    }

    public static ArrayOfPointMetadata build(List<PointMetadata> pointMetadataList) {
        return new ArrayOfPointMetadata(pointMetadataList, null);
    }

    public void test() throws Exception {
        if (this.pointMetadataList != null) {
            assertNotNull(this.docArray);
            assertEquals(this.pointMetadataList.size(), this.docArray.size());
            for (int i = 0; i < this.pointMetadataList.size(); i++) {
                PointMetadata pointMetadata = this.pointMetadataList.get(i);
                pointMetadata.setPointMetadata(this.docArray.get(i).asDocument());
                pointMetadata.test();
            }
        } else
            assertNull(this.docArray);
    }

    public void setArrayOfPointMetadata(BsonArray docArray) {
        this.docArray = docArray;
    }
}
