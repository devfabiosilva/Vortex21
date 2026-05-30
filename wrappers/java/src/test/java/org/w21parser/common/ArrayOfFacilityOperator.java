package org.w21parser.common;

import org.bson.BsonArray;

import java.util.List;
import static org.junit.Assert.*;

public class ArrayOfFacilityOperator {
    public List<FacilityOperator> facilityOperatorList = null;
    private BsonArray docArray = null;

    private ArrayOfFacilityOperator(List<FacilityOperator> facilityOperatorList, BsonArray docArray) {
        this.facilityOperatorList = facilityOperatorList;
        this.docArray = docArray;
    }

    public static ArrayOfFacilityOperator build(List<FacilityOperator> facilityOperatorList, BsonArray docArray) {
        return new ArrayOfFacilityOperator(facilityOperatorList, docArray);
    }

    public static ArrayOfFacilityOperator build(List<FacilityOperator> facilityOperatorList) {
        return new ArrayOfFacilityOperator(facilityOperatorList, null);
    }

    public void test() throws Exception {
        if (this.facilityOperatorList != null) {
            assertNotNull(this.docArray);
            assertEquals(this.facilityOperatorList.size(), this.docArray.size());
            for (int i = 0; i < this.facilityOperatorList.size(); i++) {
                FacilityOperator facilityOperator = this.facilityOperatorList.get(i);
                facilityOperator.setFacilityOperator(this.docArray.get(i).asDocument());
                facilityOperator.test();
            }
        } else
            assertNull(this.docArray);
    }

    public void setFacilityOperator(BsonArray docArray) {
        this.docArray = docArray;
    }
}
