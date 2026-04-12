package org.w21parser.telemetry;

import org.bson.BsonDocument;
import org.bson.BsonDouble;
import org.bson.BsonInt64;
import org.bson.BsonString;

public class W21TelemetrySummary21 {
    private final double NANO_TO_SECONDS =  1_000_000_000.0;
    private final long NANO_TO_MILLI = 1_000_000;
    private final long BYTE_TO_MEGABYTE = 1024*1024;
    private boolean hasReadingStat;
    private boolean hasParsingStat;
    private boolean hasParsingJsonStat;
    private final String objectName;
    private final long objectSize;
    private long readTotalCycles = -1;
    private long readTotalNanos = -1;
    private long readMem = -1;

    ////
    private int costs = -1;
    private int strings = -1;
    private int shorts = -1;
    private int ints = -1;
    private int long64s = -1;
    private int enums = -1;
    private int arrays = -1;
    private int booleans = -1;
    private int doubles = -1;
    private int dateTimes = -1;
    private int measures = -1;
    private int eventTypes = -1;
    private int total = -1;
    ////

    private double readMemMB = -1.0;
    private String readMemMBStr = null;

    private long parseTotalCycles = -1;
    private long parseTotalNanos = -1;
    private long parseMem = -1;

    private long parseJsonTotalCycles = -1;
    private long parseJsonTotalNanos = -1;
    private long parseJsonMem = -1;

    private long summaryTotalCycles = -1;
    private long summaryTotalNanos = -1;
    private long summaryTotalMem = -1;
    private double summaryTotalMemMB = -1.0;
    private String summaryTotalMemMBStr = null;
    private double summaryThroughputMBs = -1.0;
    private String summaryThroughputMBsStr = null;
    private double summaryCPB = -1.0;
    private String summaryCPBStr = null;


    public double getSummaryCPB() {

        if (this.summaryCPB >= 0.0)
            return this.summaryCPB;

        if (this.objectSize >= 0)
            return (this.summaryCPB = ((double) getSummaryTotalCycles() / this.objectSize));

        return this.summaryCPB = 0.0;
    }

    public String getSummaryCPBStr() {
        return ((this.summaryCPBStr != null)?this.summaryCPBStr:(this.summaryCPBStr = String.format("%.2f", getSummaryCPB())));
    }

    public String getSummaryThroughputMBsStr() {
        return ((this.summaryThroughputMBsStr != null)?this.summaryThroughputMBsStr:(this.summaryThroughputMBsStr = String.format("%.2f", getSummaryThroughputMBs())));
    }

    public double getSummaryThroughputMBs() {
        if (this.summaryThroughputMBs >= 0.0)
            return this.summaryThroughputMBs;

        this.summaryThroughputMBs = 0.0;
        long l = getSummaryTotalNanos();
        if (l <= 0)
            return this.summaryThroughputMBs;

        return (this.summaryThroughputMBs = (double)this.objectSize * NANO_TO_SECONDS / (l *  BYTE_TO_MEGABYTE));
    }

    public String getSummaryTotalMemMBStr() {
        return ((this.summaryTotalMemMBStr != null)?this.summaryTotalMemMBStr:(this.summaryTotalMemMBStr = String.format("%.2f", getSummaryTotalMemMB())));
    }

    double getSummaryTotalMemMB() {
        if (this.summaryTotalMemMB > -1.0)
            return this.summaryTotalMemMB;

        return (double)this.getSummaryTotalMem() / (double)BYTE_TO_MEGABYTE;
    }

    public void setSummaryTotalMem(long summaryTotalMem) {
        this.summaryTotalMem = summaryTotalMem;
    }

    public long getSummaryTotalMem() {
        if (this.summaryTotalMem > -1)
            return this.summaryTotalMem;

        return 0;
    }

    public void setSummaryTotalNanos(long summaryTotalNanos) {
        this.summaryTotalNanos = summaryTotalNanos;
    }

    public long getSummaryTotalNanos() {
        if (this.summaryTotalNanos > -1)
            return this.summaryTotalNanos;

        return 0;
    }

    public void setSummaryTotalCycles(long summaryTotalCycles) {
        this.summaryTotalCycles = summaryTotalCycles;
    }

    public long getSummaryTotalCycles() {
        if (this.summaryTotalCycles > -1)
            return this.summaryTotalCycles;

        return 0;
    }

    private String readCPBStr = null;
    private double readCPB = -1.0;

    private String parseCPBStr = null;
    private double parseCPB = -1.0;
    private double parseMBs = -1.0;
    private String parseMBsStr = null;
    private double parseThroughputMBs = -1.0;
    private String parseThroughputMBsStr = null;

    private double throughputJsonMBs = -1.0;
    private String parseJsonThroughputMBsStr = null;

    private double parseJsonCPB = -1.0;
    private String parseJsonCPBStr = null;

    private double meanCPB = -1.0;
    private String meanCPBStr = null;

    private String toStr = null;

    private double witsmlThroughputMBs = -1.0;
    private String witsmlThroughputMBsStr = null;

    private double objectSizeMB = -1.0;
    private String objectSizeMBStr = null;

    private String parseJsonMemMBStr = null;
    private double parseJsonMBs = -1.0;

    public W21TelemetrySummary21(
            String objectName,
            long objectSize
    ) {
        this.objectName = objectName;
        this.objectSize = objectSize;
    }

    public void setHasReadingStat(boolean hasReadingStat) {
        this.hasReadingStat = hasReadingStat;
    }

    public void setHasParsingStat(boolean hasParsingStat) {
        this.hasParsingStat = hasParsingStat;
    }

    public void setHasParsingJsonStat(boolean hasParsingJsonStat) {
        this.hasParsingJsonStat = hasParsingJsonStat;
    }

    public void setReadTotalCycles(long readTotalCycles) {
        this.readTotalCycles = readTotalCycles;
    }

    public void setReadTotalNanos(long readTotalNanos) {
        this.readTotalNanos = readTotalNanos;
    }

    public void setReadMem(long readMem) {
        this.readMem = readMem;
    }

    public void setParseTotalCycles(long parseTotalCycles) {
        this.parseTotalCycles = parseTotalCycles;
    }

    public void setParseJsonTotalCycles(long parseJsonTotalCycles) {
        this.parseJsonTotalCycles = parseJsonTotalCycles;
    }

    public void setParseJsonTotalNanos(long parseJsonTotalNanos) {
        this.parseJsonTotalNanos = parseJsonTotalNanos;
    }

    public void setParseJsonMem(long parseJsonMem) {
        this.parseJsonMem = parseJsonMem;
    }

    public void setParseTotalNanos(long parseTotalNanos) {
        this.parseTotalNanos = parseTotalNanos;
    }

    public void setParseMem(long parseMem) {
        this.parseMem = parseMem;
    }

    public double getObjectSizeMB() {
        if (this.objectSizeMB > 0.0)
            return this.objectSizeMB;

        return (this.objectSizeMB = this.objectSize / (double)this.BYTE_TO_MEGABYTE);
    }

    public String getObjectSizeMBStr() {
        if (this.objectSizeMBStr != null)
            return this.objectSizeMBStr;

        return (this.objectSizeMBStr = String.format("%.3f", getObjectSizeMB()));
    }
    public double getReadCPB() {
        if ((this.hasReadingStat) && (this.readCPB < 0.0) && (this.objectSize > 0))
            this.readCPB = ((double) this.readTotalCycles / (double) this.objectSize);

        return this.readCPB;
    }

    public String getReadCPBStr() {
        return ((this.readCPBStr != null)?this.readCPBStr:(this.readCPBStr = String.format("%.2f", getReadCPB())));
    }

    public double getParseCPB() {
        if ((this.hasParsingStat) && (this.parseCPB < 0.0) && (this.objectSize > 0))
            this.parseCPB = ((double) this.parseTotalCycles / (double) this.objectSize);

        return this.parseCPB;
    }

    public String getParseCPBStr() {
        return ((this.parseCPBStr != null)?this.parseCPBStr:(this.parseCPBStr = String.format("%.2f", getParseCPB())));
    }

    private double getParseJsonCPB() {
        if ((this.hasParsingJsonStat) && (this.parseJsonCPB < 0.0) && (this.objectSize > 0))
            this.parseJsonCPB = ((double) this.parseJsonTotalCycles / (double) this.objectSize);

        return this.parseJsonCPB;
    }

    private String getParseJsonCPBStr() {
        return ((this.parseJsonCPBStr != null)?this.parseJsonCPBStr:(this.parseJsonCPBStr = String.format("%.2f", getParseJsonCPB())));
    }

    public double getMeanCPB() {
        if (this.meanCPB >= 0.0)
            return this.meanCPB;

        int c = 0;
        double d = 0.0;
        if (this.hasParsingStat) {
            ++c;
            d = getParseCPB();
        }
        if (this.hasReadingStat) {
            ++c;
            d += getReadCPB();
        }

        if (this.hasParsingJsonStat) {
            ++c;
            d += getParseJsonCPB();
        }

        if (c > 0)
            this.meanCPB = d / (double) c;

        return this.meanCPB;
    }

    public String getMeanCPBStr() {
        return ((this.meanCPBStr != null)?this.meanCPBStr:(this.meanCPBStr = String.format("%.2f", getMeanCPB())));
    }

    public double getWitsmlThroughputMBs() {
        if (this.hasReadingStat && this.witsmlThroughputMBs < 0) {
            double seconds = this.readTotalNanos / this.NANO_TO_SECONDS;
            if (seconds > 0.0)
                this.witsmlThroughputMBs = (this.objectSize / (double)BYTE_TO_MEGABYTE) / seconds;
        }

        return this.witsmlThroughputMBs;
    }
    public String getWitsmlThroughputMBsStr() {
        return (this.witsmlThroughputMBsStr != null)?this.witsmlThroughputMBsStr:(this.witsmlThroughputMBsStr = String.format("%.2f", getWitsmlThroughputMBs()));
    }

    public double getReadMemMB() {
        if (this.readMemMB > 0.0)
            return this.readMemMB;

        return (this.readMemMB = this.readMem / (double)this.BYTE_TO_MEGABYTE);
    }

    public String getReadMemMBStr() {
        if (this.readMemMBStr != null)
            return this.readMemMBStr;

        return (this.readMemMBStr = String.format("%.3f", getReadMemMB()));
    }

    public double getParseThroughputMBs() {
        if (this.hasParsingStat && this.parseThroughputMBs < 0.0) {
            // Time PHASE 1 + Time PHASE 2
            double seconds = (this.parseTotalNanos + this.readTotalNanos)/this.NANO_TO_SECONDS;
            if (seconds > 0.0)
                this.parseThroughputMBs = getObjectSizeMB() / seconds;
        }

        return this.parseThroughputMBs;
    }

    public String getParseThroughputMBStr() {
        if (this.parseThroughputMBsStr != null)
            return this.parseThroughputMBsStr;
        return (this.parseThroughputMBsStr = String.format("%.3f", getParseThroughputMBs()));
    }

    public double getParseMemMB() {
        if (this.hasParsingStat && this.parseMBs < 0.0)
            this.parseMBs = (double) this.parseMem / (double)this.BYTE_TO_MEGABYTE;

        return this.parseMBs;
    }

    public String getParseMemMBStr() {
        if (this.parseMBsStr != null)
            return this.parseMBsStr;
        return (this.parseMBsStr = String.format("%.3f", getParseMemMB()));
    }

    private double getParseJsonMemMB() {
        if (this.hasParsingJsonStat && this.parseJsonMBs < 0.0)
            this.parseJsonMBs = (double) this.parseJsonMem / (double)this.BYTE_TO_MEGABYTE;

        return this.parseJsonMBs;
    }

    private String getParseJsonMemMBStr() {
        if (this.parseJsonMemMBStr != null)
            return this.parseJsonMemMBStr;
        return (this.parseJsonMemMBStr = String.format("%.3f", getParseJsonMemMB()));
    }


    public double getParseJsonThroughputMBs() {
        if (this.hasParsingJsonStat && this.throughputJsonMBs < 0.0) {
            // Time PHASE 1 + Time PHASE 2
            double seconds = (this.parseJsonTotalNanos + this.readTotalNanos)/this.NANO_TO_SECONDS;
            if (seconds > 0.0)
                this.throughputJsonMBs = getObjectSizeMB() / seconds;
        }

        return this.throughputJsonMBs;
    }

    public String getParseJsonThroughputMBStr() {
        if (this.parseJsonThroughputMBsStr != null)
            return this.parseJsonThroughputMBsStr;
        return (this.parseJsonThroughputMBsStr = String.format("%.3f", getParseJsonThroughputMBs()));
    }

    public String toString() {

        if (this.toStr != null)
            return this.toStr;

        StringBuilder sb = new StringBuilder();

        sb.append("=== WITSML 2.1 STATISTICS SUMMARY FOR ");
        sb.append(this.objectName);
        sb.append(" ===\n│");

        if (this.hasReadingStat) {
            sb.append("\n└──[INPUT] - WITSML 2.1 XML Stream or File");
            sb.append("\n   │");
            sb.append(String.format("\n   ├─%-15s : %s MB (%d bytes)", "Stream or File Size", this.getObjectSizeMBStr(), this.objectSize));
            sb.append("\n   │");
            sb.append("\n   ├─[PHASE 1] - WITSML 2.1 to C pointer structs");
            sb.append(String.format("\n   │  ├─CPB.............: %s", getReadCPBStr()));
            sb.append(String.format("\n   │  ├─CPU Cycles......: %d", this.readTotalCycles));
            sb.append(String.format("\n   │  ├─Throughput......: %s MB/s", getWitsmlThroughputMBsStr()));
            sb.append(String.format("\n   │  ├─Total time......: %.3f ms (%d ns)", (double)this.readTotalNanos /(double)this.NANO_TO_MILLI, this.readTotalNanos));
            sb.append(String.format("\n   │  └─Used memory.....: %s MB (%d bytes)", getReadMemMBStr(), this.readMem));
            sb.append("\n   │");
            boolean showPhase2 = this.hasParsingStat || this.hasParsingJsonStat;

            if (showPhase2) {
                sb.append("\n   ├─[PHASE 2] - C pointer structs to BSON Object | JSON String");
                sb.append("\n   │  │");
            }

            if (this.hasParsingStat) {
                sb.append("\n   │  ├─[BSON OBJECT]");
                sb.append(String.format("\n   │  │  ├─CPB.............: %s", this.getParseCPBStr()));
                sb.append(String.format("\n   │  │  ├─CPU Cycles......: %d", this.parseTotalCycles));
                sb.append(String.format("\n   │  │  ├─Throughput......: %s MB/s (Phase1 + Phase2)", this.getParseThroughputMBStr()));
                sb.append(String.format("\n   │  │  ├─Total time......: %.3f ms (%d ns)", (double)this.parseTotalNanos / (double) this.NANO_TO_MILLI, this.parseTotalNanos));
                sb.append(String.format("\n   │  │  └─Used memory.....: %s MB (%d bytes)", this.getParseMemMBStr(), this.parseMem));
            }

            if (this.hasParsingJsonStat) {
                sb.append("\n   │  ├─[JSON String]");
                sb.append(String.format("\n   │  │  ├─CPB.............: %s", this.getParseJsonCPBStr()));
                sb.append(String.format("\n   │  │  ├─CPU Cycles......: %d", this.parseJsonTotalCycles));
                sb.append(String.format("\n   │  │  ├─Throughput......: %s MB/s (Phase1 + Phase2)", this.getParseJsonThroughputMBStr()));
                sb.append(String.format("\n   │  │  ├─Total time......: %.3f ms (%d ns)", (double)this.parseJsonTotalNanos / (double) this.NANO_TO_MILLI, this.parseJsonTotalNanos));
                sb.append(String.format("\n   │  │  └─Used memory.....: %s MB (%d bytes)", this.getParseJsonMemMBStr(), this.parseJsonMem));
            }

            if (showPhase2) {
                sb.append("\n   │  │");
                sb.append("\n   │  └─[BSON/JSON DOCUMENT STATISTICS]");
                sb.append("\n   │     │");
                sb.append("\n   │     ├─[INVENTORY: COMPLEX TYPES]");
                sb.append(String.format("\n   │     │  ├─Measures........: %d", this.measures));
                sb.append(String.format("\n   │     │  ├─Date times......: %d", this.dateTimes));
                sb.append(String.format("\n   │     │  ├─Arrays..........: %d", this.arrays));
                sb.append(String.format("\n   │     │  ├─Costs...........: %d", this.costs));
                sb.append(String.format("\n   │     │  └─Event types.....: %d", this.eventTypes));
                sb.append("\n   │     │");
                sb.append("\n   │     ├─[INVENTORY: PRIMITIVES]");
                sb.append(String.format("\n   │     │  ├─Strings.........: %d", this.strings));
                sb.append(String.format("\n   │     │  ├─Enums...........: %d", this.enums));
                sb.append(String.format("\n   │     │  ├─Booleans........: %d", this.booleans));
                sb.append(String.format("\n   │     │  ├─Long64's........: %d", this.long64s));
                sb.append(String.format("\n   │     │  ├─Doubles.........: %d", this.doubles));
                sb.append(String.format("\n   │     │  ├─Ints............: %d", this.ints));
                sb.append(String.format("\n   │     │  └─Shorts..........: %d", this.shorts));
                sb.append("\n   │     │");
                sb.append(String.format("\n   │     └─TOTAL OBJECTS.......: %d into %s object", this.total, this.objectName));
            }
        }
        sb.append("\n   │");
        sb.append("\n   └─[SUMMARY TOTAL (Phase 1 + Phase 2)]");
        sb.append(String.format("\n      ├─CPB.............: %s", this.getSummaryCPBStr()));
        sb.append(String.format("\n      ├─CPU Cycles......: %d", this.getSummaryTotalCycles()));
        sb.append(String.format("\n      ├─Throughput......: %s MB/s (Phase1 + Phase2)", this.getSummaryThroughputMBsStr()));
        sb.append(String.format("\n      ├─Total time......: %.3f ms (%d ns)", (double)this.getSummaryTotalNanos() / (double) this.NANO_TO_MILLI, this.getSummaryTotalNanos()));
        sb.append(String.format("\n      └─Used memory.....: %s MB (%d bytes)", this.getSummaryTotalMemMBStr(), this.getSummaryTotalMem()));

        return (this.toStr = sb.toString());
    }

    private BsonDocument bsonStat = null;

    public BsonDocument getTelemetrySummary() {

        if (this.bsonStat != null)
            return this.bsonStat;

        this.bsonStat = new BsonDocument();

        if (this.hasReadingStat) {
            bsonStat.put("Object name", new BsonString(this.objectName));
            bsonStat.put("Object size", new BsonInt64(this.objectSize));

            BsonDocument readStat = new BsonDocument();
            readStat.put("CPB", new BsonDouble(this.getReadCPB()));
            readStat.put("CPU Cycles", new BsonInt64(this.readTotalCycles));
            readStat.put("Throughput", new BsonDouble(getWitsmlThroughputMBs()));
            readStat.put("Total time", new BsonInt64(this.readTotalNanos));
            readStat.put("Used memory", new BsonInt64(this.readMem));
            this.bsonStat.put("Phase1", readStat);
        }

        if (this.hasParsingStat) {
            BsonDocument parseBsonStat = new BsonDocument();
            parseBsonStat.put("CPB", new BsonDouble(this.getParseCPB()));
            parseBsonStat.put("CPU Cycles", new BsonInt64(this.parseTotalCycles));
            parseBsonStat.put("Throughput - (Phase 1 + Phase 2)", new BsonDouble(this.getParseThroughputMBs()));
            parseBsonStat.put("Total time", new BsonInt64(this.parseTotalNanos));
            parseBsonStat.put("Used memory", new BsonInt64(this.parseMem));
            this.bsonStat.put("Phase 2 - BSON", parseBsonStat);
        }

        if (this.hasParsingJsonStat) {
            BsonDocument parseJsonStat = new BsonDocument();
            parseJsonStat.put("CPB", new BsonDouble(this.getParseJsonCPB()));
            parseJsonStat.put("CPU Cycles", new BsonInt64(this.parseJsonTotalCycles));
            parseJsonStat.put("Throughput - (Phase 1 + Phase 2)", new BsonDouble(this.getParseJsonThroughputMBs()));
            parseJsonStat.put("Total time", new BsonInt64(this.parseJsonTotalNanos));
            parseJsonStat.put("Used memory", new BsonInt64(this.parseJsonMem));
            this.bsonStat.put("Phase 2 - JSON", parseJsonStat);
        }

        if (this.hasParsingStat || this.hasParsingJsonStat) {
            BsonDocument document = new BsonDocument();
            BsonDocument complexTypes = new BsonDocument();
            BsonDocument primitiveTypes = new BsonDocument();

            complexTypes.put("Measures", new BsonInt64(this.measures));
            complexTypes.put("Date times", new BsonInt64(this.dateTimes));
            complexTypes.put("Arrays", new BsonInt64(this.arrays));
            complexTypes.put("Costs", new BsonInt64(this.costs));
            complexTypes.put("Event types", new BsonInt64(this.eventTypes));

            primitiveTypes.put("Strings", new BsonInt64(this.strings));
            primitiveTypes.put("Enums", new BsonInt64(this.enums));
            primitiveTypes.put("Booleans", new BsonInt64(this.booleans));
            primitiveTypes.put("Long64's", new BsonInt64(this.long64s));
            primitiveTypes.put("Doubles", new BsonInt64(this.doubles));
            primitiveTypes.put("Ints", new BsonInt64(this.ints));
            primitiveTypes.put("Shorts", new BsonInt64(this.shorts));

            document.put("Complex Types", complexTypes);
            document.put("Primitive Types", primitiveTypes);
            document.put("TOTAL", new BsonInt64(this.total));

            this.bsonStat.put(this.objectName + " document statistics", document);
        }

        BsonDocument summary = new BsonDocument();

        summary.put("CPB", new BsonDouble(this.getSummaryCPB()));
        summary.put("CPU Cycles", new BsonInt64(this.getSummaryTotalCycles()));
        summary.put("Throughput - (Phase 1 + Phase 2)", new BsonDouble(this.getSummaryThroughputMBs()));
        summary.put("Total time", new BsonInt64(this.getSummaryTotalNanos()));
        summary.put("Used memory", new BsonInt64(this.getSummaryTotalMem()));

        this.bsonStat.put("Total", summary);

        return this.bsonStat;
    }

    public int getCosts() {
        return costs;
    }

    public void setCosts(int costs) {
        this.costs = costs;
    }

    public int getStrings() {
        return strings;
    }

    public void setStrings(int strings) {
        this.strings = strings;
    }

    public int getShorts() {
        return shorts;
    }

    public void setShorts(int shorts) {
        this.shorts = shorts;
    }

    public int getInts() {
        return ints;
    }

    public void setInts(int ints) {
        this.ints = ints;
    }

    public int getLong64s() {
        return long64s;
    }

    public void setLong64s(int long64s) {
        this.long64s = long64s;
    }

    public int getEnums() {
        return enums;
    }

    public void setEnums(int enums) {
        this.enums = enums;
    }

    public int getArrays() {
        return arrays;
    }

    public void setArrays(int arrays) {
        this.arrays = arrays;
    }

    public int getBooleans() {
        return booleans;
    }

    public void setBooleans(int booleans) {
        this.booleans = booleans;
    }

    public int getDoubles() {
        return doubles;
    }

    public void setDoubles(int doubles) {
        this.doubles = doubles;
    }

    public int getDateTimes() {
        return dateTimes;
    }

    public void setDateTimes(int dateTimes) {
        this.dateTimes = dateTimes;
    }

    public int getMeasures() {
        return measures;
    }

    public void setMeasures(int measures) {
        this.measures = measures;
    }

    public int getEventTypes() {
        return eventTypes;
    }

    public void setEventTypes(int eventTypes) {
        this.eventTypes = eventTypes;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }
}
