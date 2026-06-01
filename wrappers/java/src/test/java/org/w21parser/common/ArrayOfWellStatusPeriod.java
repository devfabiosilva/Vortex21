package org.w21parser.common;

import org.bson.BsonArray;

import java.util.List;
import static org.junit.Assert.*;

public class ArrayOfWellStatusPeriod {
    public List<WellStatusPeriod> wellStatusPeriodList = null;
    private BsonArray docArray = null;

    private ArrayOfWellStatusPeriod(List<WellStatusPeriod> wellStatusPeriodList, BsonArray docArray) {
        this.wellStatusPeriodList = wellStatusPeriodList;
        this.docArray = docArray;
    }


    public static ArrayOfWellStatusPeriod build(List<WellStatusPeriod> wellStatusPeriodList, BsonArray docArray) {
        return new ArrayOfWellStatusPeriod(wellStatusPeriodList, docArray);
    }

    public static ArrayOfWellStatusPeriod build(List<WellStatusPeriod> wellStatusPeriodList) {
        return new ArrayOfWellStatusPeriod(wellStatusPeriodList, null);
    }

    public void test() throws Exception {
        if (this.wellStatusPeriodList != null) {
            assertNotNull(this.docArray);
            assertEquals(this.wellStatusPeriodList.size(), this.docArray.size());
            for (int i = 0; i < this.wellStatusPeriodList.size(); i++) {
                WellStatusPeriod wellStatusPeriod = this.wellStatusPeriodList.get(i);
                wellStatusPeriod.setStatusHistory(this.docArray.get(i).asDocument());
                wellStatusPeriod.test();
            }
        } else
            assertNull(this.docArray);
    }

    public void setArrayOfStatusHistory(BsonArray docArray) {
        this.docArray = docArray;
    }
}
