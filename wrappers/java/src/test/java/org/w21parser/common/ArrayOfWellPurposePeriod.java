package org.w21parser.common;

import org.bson.BsonArray;

import java.util.List;

import static org.junit.Assert.*;

public class ArrayOfWellPurposePeriod {
    public List<WellPurposePeriod> wellPurposePeriodList = null;
    private BsonArray docArray = null;

    private ArrayOfWellPurposePeriod(List<WellPurposePeriod> wellPurposePeriodList, BsonArray docArray) {
        this.wellPurposePeriodList = wellPurposePeriodList;
        this.docArray = docArray;
    }

    public static ArrayOfWellPurposePeriod build(List<WellPurposePeriod> wellPurposePeriodList, BsonArray docArray) {
        return new ArrayOfWellPurposePeriod(wellPurposePeriodList, docArray);
    }

    public static ArrayOfWellPurposePeriod build(List<WellPurposePeriod> wellPurposePeriodList) {
        return new ArrayOfWellPurposePeriod(wellPurposePeriodList, null);
    }

    public void test() throws Exception {
        if (this.wellPurposePeriodList != null) {
            assertNotNull(this.docArray);
            assertEquals(this.wellPurposePeriodList.size(), this.docArray.size());

            for (int i = 0; i < this.wellPurposePeriodList.size(); i++) {
                WellPurposePeriod wellPurposePeriod = this.wellPurposePeriodList.get(i);
                wellPurposePeriod.setWellPurposePeriod(this.docArray.get(i).asDocument());
                wellPurposePeriod.test();
            }
        } else
            assertNull(this.docArray);
    }

    public void setArrayOfWellPurposePeriod(BsonArray docArray) {
        this.docArray = docArray;
    }
}
