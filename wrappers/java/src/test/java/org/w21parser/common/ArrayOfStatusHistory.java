package org.w21parser.common;

import org.bson.BsonArray;

import java.util.List;
import static org.junit.Assert.*;

public class ArrayOfStatusHistory {
    public List<StatusHistory> statusHistoryList = null;
    private BsonArray docArray = null;

    private ArrayOfStatusHistory(List<StatusHistory> statusHistoryList, BsonArray docArray) {
        this.statusHistoryList = statusHistoryList;
        this.docArray = docArray;
    }


    public static ArrayOfStatusHistory build(List<StatusHistory> statusHistoryList, BsonArray docArray) {
        return new ArrayOfStatusHistory(statusHistoryList, docArray);
    }

    public static ArrayOfStatusHistory build(List<StatusHistory> statusHistoryList) {
        return new ArrayOfStatusHistory(statusHistoryList, null);
    }

    public void test() throws Exception {
        if (this.statusHistoryList != null) {
            assertNotNull(this.docArray);
            assertEquals(this.statusHistoryList.size(), this.docArray.size());
            for (int i = 0; i < this.statusHistoryList.size(); i++) {
                StatusHistory statusHistory = this.statusHistoryList.get(i);
                statusHistory.setStatusHistory(this.docArray.get(i).asDocument());
                statusHistory.test();
            }
        } else
            assertNull(this.docArray);
    }

    public void setArrayOfStatusHistory(BsonArray docArray) {
        this.docArray = docArray;
    }
}
