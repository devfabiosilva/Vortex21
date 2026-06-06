package org.w21parser.common;

import org.bson.BsonArray;
import org.bson.BsonDocument;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.w21parser.common.Utils.testString;
import static org.w21parser.strictObject.BhaRunTest.navigate;

public class PointMetadata implements BsonDeserializable {
    public String name;
    public String dataKind;
    public String description;
    public String uom;
    public DataObjectReference metadataPropertyKind;
    public ArrayOf<LogChannelAxis> axisDefinition;
    public DataObjectReference datum;
    private BsonDocument doc;

    private PointMetadata(String name, String dataKind, String description, String uom, DataObjectReference metadataPropertyKind, ArrayOf<LogChannelAxis> axisDefinition, DataObjectReference datum, BsonDocument doc) {
        this.name = name;
        this.dataKind = dataKind;
        this.description = description;
        this.uom = uom;
        this.metadataPropertyKind = metadataPropertyKind;
        this.axisDefinition = axisDefinition;
        this.datum = datum;
        this.doc = doc;
    }

    public static PointMetadata build(String name, String dataKind, String description, String uom, DataObjectReference metadataPropertyKind, ArrayOf<LogChannelAxis> axisDefinition, DataObjectReference datum, BsonDocument doc) {
        return new PointMetadata(name, dataKind, description, uom, metadataPropertyKind, axisDefinition, datum, doc);
    }

    public static PointMetadata build(String name, String dataKind, String description, String uom, DataObjectReference metadataPropertyKind, ArrayOf<LogChannelAxis> axisDefinition, DataObjectReference datum) {
        return new PointMetadata(name, dataKind, description, uom, metadataPropertyKind, axisDefinition, datum, null);
    }

    @Override
    public void test() throws Exception {
        testString(this.name, "Name", this.doc);
        testString(this.dataKind, "DataKind", this.doc);
        testString(this.description, "Description", this.doc);
        testString(this.uom, "Uom", this.doc);

        BsonDocument metadataPropertyKind = (BsonDocument) navigate(this.doc, "MetadataPropertyKind");
        if (this.metadataPropertyKind != null) {
            assertNotNull(metadataPropertyKind);
            this.metadataPropertyKind.setDataObjectReference(metadataPropertyKind);
            this.metadataPropertyKind.test();
        } else
            assertNull(metadataPropertyKind);

        BsonArray axisDefinition = (BsonArray) navigate(this.doc, "AxisDefinition");
        if (this.axisDefinition != null) {
            assertNotNull(axisDefinition);
            this.axisDefinition.setBsonArray(axisDefinition);
            this.axisDefinition.test();
        } else
            assertNull(axisDefinition);

        BsonDocument datum = (BsonDocument) navigate(this.doc, "Datum");
        if (this.datum != null) {
            assertNotNull(datum);
            this.datum.setDataObjectReference(datum);
            this.datum.test();
        } else
            assertNull(datum);
    }

    @Override
    public void setBsonDocument(BsonDocument doc) {
        this.doc = doc;
    }
}
