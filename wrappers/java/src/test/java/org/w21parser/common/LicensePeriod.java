package org.w21parser.common;

import org.bson.BsonDocument;

import static org.w21parser.common.Utils.testDateTime;
import static org.w21parser.common.Utils.testString;

public class LicensePeriod implements BsonDeserializable {
    public String numLicense = null;
    public String terminationDateTime = null;
    public String effectiveDateTime = null;
    private BsonDocument doc = null;

    private LicensePeriod(String numLicense, String terminationDateTime, String effectiveDateTime, BsonDocument doc) {
        this.numLicense = numLicense;
        this.terminationDateTime = terminationDateTime;
        this.effectiveDateTime = effectiveDateTime;
        this.doc = doc;
    }

    public static LicensePeriod build(String numLicense, String terminationDateTime, String effectiveDateTime, BsonDocument doc) {
        return new LicensePeriod(numLicense, terminationDateTime, effectiveDateTime, doc);
    }

    public static LicensePeriod build(String numLicense, String terminationDateTime, String effectiveDateTime) {
        return new LicensePeriod(numLicense, terminationDateTime, effectiveDateTime, null);
    }

    @Override
    public void test() throws Exception {
        testString(this.numLicense, "NumLicense", this.doc);
        testDateTime(this.terminationDateTime, "TerminationDateTime", this.doc);
        testDateTime(this.effectiveDateTime, "EffectiveDateTime", this.doc);
    }

    @Override
    public void setBsonDocument(BsonDocument doc) {
        this.doc = doc;
    }
}
