package org.w21parser.common;

import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.BsonString;
import org.bson.BsonValue;

import java.util.List;
import static org.junit.Assert.*;
import static org.w21parser.strictObject.BhaRunTest.navigate;

public class DataObjectReference {
    private final String uuid;
    private final String objectVersion;
    private final String qualifiedType;
    private final String title;
    private final String energisticsUri;
    private final List<String> locatorUrl;
    private final List<ExtensionNameValue> extensionNameValues;
    private final BsonDocument dataObjectReference;

    public DataObjectReference(
            String uuid,
            String objectVersion,
            String qualifiedType,
            String title,
            String energisticsUri,
            List<String> locatorUrl,
            List<ExtensionNameValue> extensionNameValues,
            BsonDocument dataObjectReference
    ) {

        this.uuid = uuid;
        this.objectVersion = objectVersion;
        this.qualifiedType = qualifiedType;
        this.title = title;
        this.energisticsUri = energisticsUri;
        this.locatorUrl = locatorUrl;
        this.extensionNameValues = extensionNameValues;
        this.dataObjectReference = dataObjectReference;
    }

    public void test() throws Exception {
        String actualUuid = (String)navigate(dataObjectReference, "Uuid");
        if (uuid != null) {
            assertNotNull(actualUuid);
            assertEquals(uuid, actualUuid);
        } else
            assertNull(actualUuid);

        String actualObjectVersion = (String)navigate(dataObjectReference, "ObjectVersion");
        if (objectVersion != null) {
            assertNotNull(actualObjectVersion);
            assertEquals(objectVersion, actualObjectVersion);
        } else
            assertNull(actualObjectVersion);

        String actualQualifiedType = (String)navigate(dataObjectReference, "QualifiedType");
        if (qualifiedType != null) {
            assertNotNull(actualQualifiedType);
            assertEquals(qualifiedType, actualQualifiedType);
        } else
            assertNull(actualQualifiedType);

        String actualTitle = (String)navigate(dataObjectReference, "Title");
        if (title != null) {
            assertNotNull(actualTitle);
            assertEquals(title, actualTitle);
        } else
            assertNull(actualTitle);

        String actualEnergisticsUri = (String)navigate(dataObjectReference, "EnergisticsUri");
        if (energisticsUri != null) {
            assertNotNull(actualEnergisticsUri);
            assertEquals(energisticsUri, actualEnergisticsUri);
        } else
            assertNull(actualEnergisticsUri);

        BsonArray locatorUrlArray = (BsonArray)navigate(dataObjectReference, "LocatorUrl");

        if (locatorUrl != null) {
            assertNotNull(locatorUrlArray);
            assertEquals(locatorUrl.size(), locatorUrlArray.size());

            for (int i = 0; i < locatorUrl.size(); i++) {
                assertEquals(locatorUrl.get(i), ((BsonString)locatorUrlArray.get(i)).asString().getValue());
            }
        } else
            assertNull(locatorUrlArray);

        BsonArray actualExtensionNameValues = (BsonArray) navigate(dataObjectReference, "ExtensionNameValue");
        if (extensionNameValues != null) {
            assertNotNull(actualExtensionNameValues);
            assertEquals(extensionNameValues.size(), actualExtensionNameValues.size());
            for (int i = 0; i < extensionNameValues.size(); i++) {
                BsonValue extensionNameValue = actualExtensionNameValues.get(i);
                ExtensionNameValue extensionNameValueTmp = extensionNameValues.get(i);
                extensionNameValueTmp.setExtensionNameValue(extensionNameValue);
                extensionNameValueTmp.test();
            }
        } else
            assertNull(actualExtensionNameValues);
    }
}
