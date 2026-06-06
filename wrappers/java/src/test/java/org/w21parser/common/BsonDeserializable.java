package org.w21parser.common;

import org.bson.BsonDocument;

public interface BsonDeserializable {
    void test() throws Exception;
    void setBsonDocument(BsonDocument doc);
}
