package org.w21parser.common;

import org.bson.BsonDocument;

import static org.w21parser.common.Utils.testDateTime;
import static org.w21parser.common.Utils.testString;

public class WellPurposePeriod implements BsonDeserializable {
    public String purpose = null;
    public String startDateTime = null;
    public String endDateTime = null;
    private BsonDocument doc = null;

    private WellPurposePeriod(String purpose, String startDateTime, String endDateTime, BsonDocument doc) {
        this.purpose = purpose;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.doc = doc;
    }

    public static WellPurposePeriod build(String purpose, String startDateTime, String endDateTime, BsonDocument doc) {
        return new WellPurposePeriod(purpose, startDateTime, endDateTime, doc);
    }

    public static WellPurposePeriod build(String purpose, String startDateTime, String endDateTime) {
        return new WellPurposePeriod(purpose, startDateTime, endDateTime, null);
    }

    @Override
    public void test() throws Exception {
        testString(this.purpose, "Purpose", this.doc);
        testDateTime(this.startDateTime, "StartDateTime", this.doc);
        testDateTime(this.endDateTime, "EndDateTime", this.doc);
    }

    @Override
    public void setBsonDocument(BsonDocument doc) {
        this.doc = doc;
    }

    public void setWellPurposePeriod(BsonDocument doc) {
        this.doc = doc;
    }
}
