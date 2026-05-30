package org.w21parser.common;

import org.bson.BsonDocument;
import static org.junit.Assert.*;
import static org.w21parser.common.Utils.testDateTime;
import static org.w21parser.strictObject.BhaRunTest.navigate;

public class FacilityOperator {
    public DataObjectReference businessAssociate = null;
    public String effectiveDateTime = null;
    public String terminationDateTime = null;
    private BsonDocument doc = null;

    private FacilityOperator(DataObjectReference businessAssociate, String effectiveDateTime, String terminationDateTime, BsonDocument doc) {
        this.businessAssociate = businessAssociate;
        this.effectiveDateTime = effectiveDateTime;
        this.terminationDateTime = terminationDateTime;
        this.doc = doc;
    }

    public static FacilityOperator build(DataObjectReference businessAssociate, String effectiveDateTime, String terminationDateTime, BsonDocument doc) {
        return new FacilityOperator(businessAssociate, effectiveDateTime, terminationDateTime, doc);
    }

    public static FacilityOperator build(DataObjectReference businessAssociate, String effectiveDateTime, String terminationDateTime) {
        return new FacilityOperator(businessAssociate, effectiveDateTime, terminationDateTime, null);
    }

    public void test() throws Exception {
        if (this.businessAssociate != null) {
            assertNotNull(this.doc);
            businessAssociate.setDataObjectReference((BsonDocument) navigate(this.doc, "BusinessAssociate"));
            businessAssociate.test();
        } else
            assertNull(this.doc);

        testDateTime(this.effectiveDateTime, "EffectiveDateTime", this.doc);
        testDateTime(this.terminationDateTime, "TerminationDateTime", this.doc);
    }

    public void setFacilityOperator(BsonDocument doc) {
        this.doc = doc;
    }
}
