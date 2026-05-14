package org.w21parser.common;

import org.bson.BsonDocument;

import static org.w21parser.common.Utils.testLong;
import static org.w21parser.common.Utils.testString;

public class PassDetail {
    public final Long pass;
    public final String description;
    private BsonDocument doc;

    private PassDetail(Long pass, String description, BsonDocument doc) {
        this.pass = pass;
        this.description = description;
        this.doc = doc;
    }

    public static PassDetail build(Long pass, String description, BsonDocument doc) {
        return new PassDetail(pass, description, doc);
    }

    public void test() throws Exception {
        testLong(this.pass, "Pass", this.doc);
        testString(this.description, "Description", this.doc);
    }

}
