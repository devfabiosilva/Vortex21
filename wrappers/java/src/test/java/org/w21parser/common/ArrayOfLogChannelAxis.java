package org.w21parser.common;

import org.bson.BsonArray;

import java.util.List;
import static org.junit.Assert.*;

public class ArrayOfLogChannelAxis {
    public List<LogChannelAxis> logChannelAxisList = null;
    private BsonArray docArray = null;

    private ArrayOfLogChannelAxis(List<LogChannelAxis> logChannelAxisList, BsonArray docArray) {
        this.logChannelAxisList = logChannelAxisList;
        this.docArray = docArray;
    }

    public static ArrayOfLogChannelAxis build(List<LogChannelAxis> logChannelAxisList, BsonArray docArray) {
        return new ArrayOfLogChannelAxis(logChannelAxisList, docArray);
    }

    public static ArrayOfLogChannelAxis build(List<LogChannelAxis> logChannelAxisList) {
        return new ArrayOfLogChannelAxis(logChannelAxisList, null);
    }

    public void test() throws Exception {
        if (this.logChannelAxisList != null) {
            assertNotNull(this.docArray);
            assertEquals(this.logChannelAxisList.size(), this.docArray.size());

            for (int i = 0; i < this.logChannelAxisList.size(); i++) {
                LogChannelAxis logChannelAxis = logChannelAxisList.get(i);
                logChannelAxis.setLogChannelAxis(this.docArray.get(i).asDocument());
                logChannelAxis.test();
            }
        } else
            assertNull(this.docArray);
    }

    public void setLogChannelAxisList(BsonArray docArray) {
        this.docArray = docArray;
    }
}
