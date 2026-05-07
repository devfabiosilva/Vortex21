package org.w21parser.common;

import org.bson.BsonArray;
import org.bson.BsonDateTime;
import org.bson.BsonDocument;
import org.bson.BsonValue;
import org.jetbrains.annotations.NotNull;
import org.w21parser.DateUtils;

import java.util.*;

import static org.junit.Assert.*;
import static org.w21parser.strictObject.BhaRunTest.navigate;

public class Aliases {
    private List<AliasField> aliasList = new ArrayList<>();
    private BsonArray aliasArray;

    private Aliases(BsonArray aliasArray) {
        this.aliasArray = aliasArray;
    }

    public static Aliases build(BsonArray aliasArray) {
        return new Aliases(aliasArray);
    }

    public Aliases add(
            String authority,
            String identifier,
            String identifierKind,
            String description,
            String effectiveDateTime,
            String terminationDateTime
    ) {
        aliasList.add(
                new AliasField(
                        authority,
                        identifier,
                        identifierKind,
                        description,
                        effectiveDateTime,
                        terminationDateTime
                )
        );
        return this;
    }

    public void test() throws Exception {
        assertNotNull(aliasArray);
        assertEquals(aliasList.size(), aliasArray.size());
        for (int i = 0; i < aliasList.size(); i++) {
            AliasField field = aliasList.get(i);
            BsonDocument alias = (BsonDocument)aliasArray.get(i);

            String str = (String)navigate(alias, "#attributes", "authority");
            if (field.authority != null) {
                assertNotNull(str);
                assertEquals(field.authority, str);
            } else
                assertNull(str);

            str = (String)navigate(alias, "Identifier");
            if (field.identifier != null) {
                assertNotNull(str);
                assertEquals(field.identifier, str);
            } else
                assertNull(str);

            str = (String)navigate(alias, "IdentifierKind");
            if (field.identifierKind != null) {
                assertNotNull(str);
                assertEquals(field.identifierKind, str);
            } else
                assertNull(str);

            str = (String)navigate(alias, "Description");
            if (field.description != null) {
                assertNotNull(str);
                assertEquals(field.description, str);
            } else
                assertNull(str);
            
            BsonDateTime bdt = (BsonDateTime)navigate(alias, "EffectiveDateTime");
            if (field.effectiveDateTime != null) {
                assertNotNull(bdt);
                assertEquals(DateUtils.toTimestamp(field.effectiveDateTime), bdt.getValue());
            } else
                assertNull(bdt);

            bdt = (BsonDateTime)navigate(alias, "TerminationDateTime");
            if (field.terminationDateTime != null) {
                assertNotNull(str);
                assertEquals(DateUtils.toTimestamp(field.terminationDateTime), bdt.getValue());
            } else
                assertNull(bdt);
        }
    }

    private class AliasField {
        public final String authority;
        public final String identifier;
        public final String identifierKind;
        public final String description;
        public final String effectiveDateTime;
        public final String terminationDateTime;

        public AliasField(String authority, String identifier, String identifierKind, String description, String effectiveDateTime, String terminationDateTime) {
            this.authority = authority;
            this.identifier = identifier;
            this.identifierKind = identifierKind;
            this.description = description;
            this.effectiveDateTime = effectiveDateTime;
            this.terminationDateTime = terminationDateTime;
        }
    }
}
