package org.w21parser.common;

import org.bson.BsonDocument;

import static org.w21parser.common.Utils.*;

public class Alias implements BsonDeserializable {
    public String authority = null;
    public String identifier = null;
    public String identifierKind = null;
    public String description = null;
    public String effectiveDateTime = null;
    public String terminationDateTime = null;
    private BsonDocument doc;

    private Alias(String authority, String identifier, String identifierKind, String description, String effectiveDateTime, String terminationDateTime, BsonDocument doc) {
        this.authority = authority;
        this.identifier = identifier;
        this.identifierKind = identifierKind;
        this.description = description;
        this.effectiveDateTime = effectiveDateTime;
        this.terminationDateTime = terminationDateTime;
        this.doc = doc;
    }

    public static Alias build(String authority, String identifier, String identifierKind, String description, String effectiveDateTime, String terminationDateTime, BsonDocument doc) {
        return new Alias(authority, identifier, identifierKind, description, effectiveDateTime, terminationDateTime, doc);
    }

    public static Alias build(String authority, String identifier, String identifierKind, String description, String effectiveDateTime, String terminationDateTime) {
        return new Alias(authority, identifier, identifierKind, description, effectiveDateTime, terminationDateTime, null);
    }

    @Override
    public void test() throws Exception {
        testStringAttribute(this.authority, "Authority", this.doc);
        testString(this.identifier, "Identifier", this.doc);
        testString(this.identifierKind, "IdentifierKind", this.doc);
        testString(this.description, "Description", this.doc);
        testDateTime(this.effectiveDateTime, "EffectiveDateTime", this.doc);
        testDateTime(this.terminationDateTime, "TerminationDateTime", this.doc);
    }

    @Override
    public void setBsonDocument(BsonDocument doc) {
        this.doc = doc;
    }
}
