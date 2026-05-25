package org.w21parser.common;

import org.bson.BsonDocument;

import static org.w21parser.common.Utils.testString;

public class Data {
    public String data;
    public String fileUri;
    private BsonDocument doc;

    private Data(String data, String fileUri, BsonDocument doc) {
        this.data = data;
        this.fileUri = fileUri;
        this.doc = doc;
    }

    public static Data build(String data, String fileUri, BsonDocument doc) {
        return new Data(data, fileUri, doc);
    }

    public static Data build(String data, String fileUri) {
        return new Data(data, fileUri, null);
    }

    public void test() throws Exception {
        testString(this.data, "Data", this.doc);
        testString(this.fileUri, "FileUri", this.doc);
    }

    public void setData(BsonDocument doc) {
        this.doc = doc;
    }
}
