package org.w21parser.common;

import org.bson.BsonDocument;

import static org.w21parser.common.Utils.testString;

public class OSDULineageAssertion {
    public final String iD;
    public final String lineageRelationshipKind;
    private BsonDocument doc;

    private OSDULineageAssertion(String iD, String lineageRelationshipKind, BsonDocument doc) {
        this.iD = iD;
        this.lineageRelationshipKind = lineageRelationshipKind;
        this.doc = doc;
    }

    public static OSDULineageAssertion build(String iD, String lineageRelationshipKind, BsonDocument doc) {
        return new OSDULineageAssertion(iD, lineageRelationshipKind, doc);
    }

    public void setOSDULineageAssertion(BsonDocument doc) {
        this.doc = doc;
    }

    public void test() throws Exception {
        testString(this.iD, "ID", this.doc);
        testString(this.lineageRelationshipKind, "LineageRelationshipKind", this.doc);
    }
}
