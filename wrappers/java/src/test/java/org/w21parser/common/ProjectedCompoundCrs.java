package org.w21parser.common;

import org.bson.BsonArray;
import org.bson.BsonDocument;

import java.util.List;

import static org.junit.Assert.*;
import static org.w21parser.common.Utils.testString;
import static org.w21parser.common.Utils.testStringAttribute;
import static org.w21parser.strictObject.BhaRunTest.navigate;

public class ProjectedCompoundCrs implements BsonDeserializable {
    public String uuid = null;
    public String schemaVersion = null;
    public String objectVersion = null;
    public ArrayOf<Alias> aliases = null;
    public Citation citation = null;
    public String existence = null;
    public List<String> businessActivityHistory = null;
    public OSDUIntegration osduIntegration = null;
    public CustomData customData = null;
    public ArrayOf<ExtensionNameValue> extensionNameValueArray = null;
    public DataObjectReference verticalCrs = null;
    public DataObjectReference projectedCrs = null;
    private BsonDocument doc;

    private ProjectedCompoundCrs(String uuid, String schemaVersion, String objectVersion, ArrayOf<Alias> aliases, Citation citation, String existence, List<String> businessActivityHistory, OSDUIntegration osduIntegration, CustomData customData, ArrayOf<ExtensionNameValue> extensionNameValueArray, DataObjectReference verticalCrs, DataObjectReference projectedCrs, BsonDocument doc) {
        this.uuid = uuid;
        this.schemaVersion = schemaVersion;
        this.objectVersion = objectVersion;
        this.aliases = aliases;
        this.citation = citation;
        this.existence = existence;
        this.businessActivityHistory = businessActivityHistory;
        this.osduIntegration = osduIntegration;
        this.customData = customData;
        this.extensionNameValueArray = extensionNameValueArray;
        this.verticalCrs = verticalCrs;
        this.projectedCrs = projectedCrs;
        this.doc = doc;
    }

    public static ProjectedCompoundCrs build(String uuid, String schemaVersion, String objectVersion, ArrayOf<Alias> aliases, Citation citation, String existence, List<String> businessActivityHistory, OSDUIntegration osduIntegration, CustomData customData, ArrayOf<ExtensionNameValue> extensionNameValueArray, DataObjectReference verticalCrs, DataObjectReference projectedCrs, BsonDocument doc) {
        return new ProjectedCompoundCrs(uuid, schemaVersion, objectVersion, aliases, citation, existence, businessActivityHistory, osduIntegration, customData, extensionNameValueArray, verticalCrs, projectedCrs, doc);
    }

    public static ProjectedCompoundCrs build(String uuid, String schemaVersion, String objectVersion, ArrayOf<Alias> aliases, Citation citation, String existence, List<String> businessActivityHistory, OSDUIntegration osduIntegration, CustomData customData, ArrayOf<ExtensionNameValue> extensionNameValueArray, DataObjectReference verticalCrs, DataObjectReference projectedCrs) {
        return new ProjectedCompoundCrs(uuid, schemaVersion, objectVersion, aliases, citation, existence, businessActivityHistory, osduIntegration, customData, extensionNameValueArray, verticalCrs, projectedCrs, null);
    }

    @Override
    public void test() throws Exception {
        testStringAttribute(this.uuid, "Uuid", this.doc);
        testStringAttribute(this.schemaVersion, "SchemaVersion", this.doc);
        testStringAttribute(this.objectVersion, "ObjectVersion", this.doc);

        BsonArray aliases = (BsonArray) navigate(this.doc, "Aliases", this.doc);
        if (this.aliases != null) {
            assertNotNull(aliases);
            assertEquals(this.aliases.list.size(), aliases.size());
            for (int i = 0; i < this.aliases.list.size(); i++) {
                Alias item = this.aliases.list.get(i);
                item.setBsonDocument(aliases.get(i).asDocument());
                item.test();
            }
        } else
            assertNull(aliases);

        BsonDocument citation = (BsonDocument) navigate(this.doc, "Citation");
        if (this.citation != null) {
            assertNotNull(citation);
            this.citation.setBsonDocument(citation);
            this.citation.test();
        } else
            assertNull(citation);

        testString(this.existence, "Existence", this.doc);

        BsonArray businessActivityHistory = (BsonArray) navigate(this.doc, "BusinessActivityHistory");
        if (this.businessActivityHistory != null) {
            assertNotNull(businessActivityHistory);
            assertEquals(this.businessActivityHistory.size(), businessActivityHistory.size());
            for (int i = 0; i < this.businessActivityHistory.size(); i++)
                assertEquals(this.businessActivityHistory.get(i),  businessActivityHistory.get(i).asString().getValue());
        } else
            assertNull(businessActivityHistory);

        BsonDocument osduIntegration = (BsonDocument)navigate(this.doc, "OSDUIntegration");
        if (this.osduIntegration != null) {
            assertNotNull(osduIntegration);
            this.osduIntegration.setBsonDocument(osduIntegration);
            this.osduIntegration.test();
        } else
            assertNull(osduIntegration);

        BsonDocument customData = (BsonDocument) navigate(this.doc, "CustomData");
        if (this.customData != null) {
            assertNotNull(customData);
            this.customData.setBsonDocument(customData);
            this.customData.test();
        } else
            assertNull(customData);

        BsonArray extensionNameValueArray = (BsonArray) navigate(this.doc, "ExtensionNameValue");
        if (this.extensionNameValueArray != null) {
            assertNotNull(extensionNameValueArray);
            assertEquals(this.extensionNameValueArray.list.size(), extensionNameValueArray.size());
            for (int i = 0; i < this.extensionNameValueArray.list.size(); i++) {
                ExtensionNameValue item = this.extensionNameValueArray.list.get(i);
                item.setBsonDocument(extensionNameValueArray.get(i).asDocument());
                item.test();
            }
        } else
            assertNull(extensionNameValueArray);

        BsonDocument verticalCrs = (BsonDocument) navigate(this.doc, "VerticalCrs");
        if (this.verticalCrs != null) {
            assertNotNull(verticalCrs);
            this.verticalCrs.setBsonDocument(verticalCrs);
        }

        BsonDocument projectedCrs = (BsonDocument) navigate(this.doc, "ProjectedCrs");
        if (this.projectedCrs != null) {
            assertNotNull(projectedCrs);
            this.projectedCrs.setBsonDocument(projectedCrs);
        }
    }

    @Override
    public void setBsonDocument(BsonDocument doc) {
        this.doc = doc;
    }
}
