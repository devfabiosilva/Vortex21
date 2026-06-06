package org.w21parser.common;

import org.bson.BsonArray;

import java.util.List;

import static org.junit.Assert.*;

public class ArrayOf<T extends BsonDeserializable> {
    public List<T> list = null;
    private BsonArray docArray = null;

    private ArrayOf(List<T> list, BsonArray docArray) {
        this.list = list;
        this.docArray = docArray;
    }

    public static <T extends BsonDeserializable> ArrayOf<T> build(List<T> list, BsonArray docArray) {
        return new ArrayOf<T>(list, docArray);
    }

    public static <T extends BsonDeserializable> ArrayOf<T> build(List<T> list) {
        return new ArrayOf<T>(list, null);
    }

    public void test() throws Exception {
        if (this.list != null) {
            assertNotNull(this.docArray);
            assertEquals(this.list.size(), this.docArray.size());
            for (int i = 0; i < this.list.size(); i++) {
                T item = this.list.get(i);
                item.setBsonDocument(this.docArray.get(i).asDocument());
                item.test();
            }
        } else
            assertNull(this.docArray);
    }

    public void setBsonArray(BsonArray docArray) {
        this.docArray = docArray;
    }
}
