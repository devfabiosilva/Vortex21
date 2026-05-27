package org.w21parser.common;

import org.bson.BsonDocument;

import static org.w21parser.common.Utils.testDateTime;
import static org.w21parser.common.Utils.testString;

public class FacilityLifecyclePeriod {
    public String state = null;
    public String startDateTime = null;
    public String endDateTime = null;
    private BsonDocument doc = null;

    private FacilityLifecyclePeriod(String state, String startDateTime, String endDateTime, BsonDocument doc) {
        this.state = state;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.doc = doc;
    }

    public static FacilityLifecyclePeriod build(String state, String startDateTime, String endDateTime, BsonDocument doc) {
        return new FacilityLifecyclePeriod(state, startDateTime, endDateTime, doc);
    }

    public static FacilityLifecyclePeriod build(String state, String startDateTime, String endDateTime) {
        return new FacilityLifecyclePeriod(state, startDateTime, endDateTime, null);
    }

    public void test() throws Exception {
        testString(this.state, "State", this.doc);
        testDateTime(this.startDateTime, "StartDateTime", this.doc);
        testDateTime(this.endDateTime, "EndDateTime", this.doc);
    }

    public void setFacilityLifecyclePeriod(BsonDocument doc) {
        this.doc = doc;
    }
}
