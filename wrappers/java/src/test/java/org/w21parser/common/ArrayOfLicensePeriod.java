package org.w21parser.common;

import org.bson.BsonArray;

import java.util.List;
import static org.junit.Assert.*;

public class ArrayOfLicensePeriod {
    public List<LicensePeriod> licensePeriodList = null;
    private BsonArray docArray = null;

    private ArrayOfLicensePeriod(List<LicensePeriod> licensePeriodList, BsonArray docArray) {
        this.licensePeriodList = licensePeriodList;
        this.docArray = docArray;
    }

    public static ArrayOfLicensePeriod build(List<LicensePeriod> licensePeriodList, BsonArray docArray) {
        return new ArrayOfLicensePeriod(licensePeriodList, docArray);
    }

    public static ArrayOfLicensePeriod build(List<LicensePeriod> licensePeriodList) {
        return new ArrayOfLicensePeriod(licensePeriodList, null);
    }

    public void test() throws Exception {
        if (this.licensePeriodList != null) {
            assertNotNull(this.docArray);
            assertEquals(this.licensePeriodList.size(), this.docArray.size());
            for (int i = 0; i < this.licensePeriodList.size(); i++) {
                LicensePeriod licensePeriod = this.licensePeriodList.get(i);
                licensePeriod.setLicensePeriod(this.docArray.get(i).asDocument());
                licensePeriod.test();
            }
        }
    }

    public void setArrayOfLicensePeriod(BsonArray doc) {
        this.docArray = docArray;
    }
}
