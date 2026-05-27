package org.w21parser.common;

import org.bson.BsonArray;

import java.util.List;
import static org.junit.Assert.*;

public class ArrayOfFacilityLifecyclePeriod {
    public List<FacilityLifecyclePeriod> facilityLifecyclePeriodList = null;
    private BsonArray docArray;

    private ArrayOfFacilityLifecyclePeriod(List<FacilityLifecyclePeriod> facilityLifecyclePeriodList, BsonArray docArray) {
        this.facilityLifecyclePeriodList = facilityLifecyclePeriodList;
        this.docArray = docArray;
    }

    public static ArrayOfFacilityLifecyclePeriod build(List<FacilityLifecyclePeriod> facilityLifecyclePeriodList, BsonArray docArray) {
        return new ArrayOfFacilityLifecyclePeriod(facilityLifecyclePeriodList, docArray);
    }

    public static ArrayOfFacilityLifecyclePeriod build(List<FacilityLifecyclePeriod> facilityLifecyclePeriodList) {
        return new ArrayOfFacilityLifecyclePeriod(facilityLifecyclePeriodList, null);
    }

    public void test() throws Exception {
        if (this.facilityLifecyclePeriodList != null) {
            assertNotNull(this.docArray);
            assertEquals(this.facilityLifecyclePeriodList.size(), this.docArray.size());
            for (int i = 0; i < this.facilityLifecyclePeriodList.size(); i++) {
                FacilityLifecyclePeriod facilityLifecyclePeriod = this.facilityLifecyclePeriodList.get(i);
                facilityLifecyclePeriod.setFacilityLifecyclePeriod(this.docArray.get(i).asDocument());
                facilityLifecyclePeriod.test();
            }
        } else
            assertNull(this.docArray);
    }

    public void setArrayOfFacilityLifecyclePeriod(BsonArray docArray) {
        this.docArray = docArray;
    }
}

