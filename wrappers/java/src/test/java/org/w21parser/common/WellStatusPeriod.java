package org.w21parser.common;

import org.bson.BsonDocument;

import static org.w21parser.common.Utils.testDateTime;
import static org.w21parser.common.Utils.testString;

public class WellStatusPeriod {
    public String status = null;
    public String startDateTime = null;
    public String endDateTime = null;
    private BsonDocument doc = null;

    private WellStatusPeriod(String status, String startDateTime, String endDateTime, BsonDocument doc) {
        this.status = status;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.doc = doc;
    }

    public static WellStatusPeriod build(String status, String startDateTime, String endDateTime, BsonDocument doc) {
        return new WellStatusPeriod(status, startDateTime, endDateTime, doc);
    }

    public static WellStatusPeriod build(String status, String startDateTime, String endDateTime) {
        return new WellStatusPeriod(status, startDateTime, endDateTime, null);
    }

    public void test() throws Exception {
        testString(this.status, "Status", this.doc);
        testDateTime(this.startDateTime, "StartDateTime", this.doc);
        testDateTime(this.endDateTime, "EndDateTime", this.doc);
    }

    public void setStatusHistory(BsonDocument doc) {
        this.doc = doc;
    }
}
