package org.w21parser;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

public class VersionTest {
    private static final Logger logger = LoggerFactory.getLogger(VersionTest.class);

    @Test
    public void checkVersionStr() {
        assertEquals(-9223372036854710272L, W21ParserLoader.jniGetVersion());
    }

    @Test
    public void checkBuildDateStr() {
        assertEquals(-216172579506632069L, W21ParserLoader.jniGetBuildDate());
    }

    @Test
    public void testGetVersionStr() throws Exception {
        W21ParserLoader parser1 = W21ParserLoader.begin().build();
        int err;
        try {
            assertEquals("0.1.0-beta", parser1.getVersionStr());
        } finally {
            err = parser1.close();
        }

        if (err != 0)
            logger.warn("Close instance for retrieve testGetVersionStr status code: {}", err);
    }

    @Test
    public void testGetBuildDateStr() throws Exception {
        W21ParserLoader parser1 = W21ParserLoader.begin().build();
        int err;
        try {
            assertEquals("202607151739-GMT: -3", parser1.getBuildDateStr());
        } finally {
            err = parser1.close();
        }

        if (err != 0)
            logger.warn("Close instance for retrieve testGetBuildDateStr status code: {}", err);
    }
}
