package org.w21parser.common;

import org.bson.BsonArray;
import org.bson.BsonDocument;

import java.util.List;

import static org.junit.Assert.*;

public class ArrayOfExtensionNameValue {
    public List<ExtensionNameValue> extensionNameValueList = null;
    private BsonArray docArray = null;

    private ArrayOfExtensionNameValue(List<ExtensionNameValue> extensionNameValueList, BsonArray docArray) {
        this.extensionNameValueList = extensionNameValueList;
        this.docArray = docArray;
    }

    public static ArrayOfExtensionNameValue build(List<ExtensionNameValue> extensionNameValueList, BsonArray docArray) {
        return new ArrayOfExtensionNameValue(extensionNameValueList, docArray);
    }

    public static ArrayOfExtensionNameValue build(List<ExtensionNameValue> extensionNameValueList) {
        return new ArrayOfExtensionNameValue(extensionNameValueList, null);
    }

    public void test() throws Exception {
        if (this.extensionNameValueList != null) {
            assertNotNull(this.docArray);
            assertEquals(this.extensionNameValueList.size(), this.docArray.size());
            for (int i = 0; i < this.extensionNameValueList.size(); i++) {
                ExtensionNameValue extensionNameValue = this.extensionNameValueList.get(i);
                extensionNameValue.setExtensionNameValue(this.docArray.get(i).asDocument());
                extensionNameValue.test();
            }
        } else
            assertNull(this.docArray);
    }

    public void setExtensionNameValue(BsonArray docArray) {
        this.docArray = docArray;
    }
}
