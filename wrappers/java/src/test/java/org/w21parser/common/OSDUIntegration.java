package org.w21parser.common;

import org.bson.BsonArray;
import org.bson.BsonDocument;

import java.util.List;

import static org.w21parser.common.Utils.testString;
import static org.w21parser.common.Utils.testStringList;
import static org.w21parser.strictObject.BhaRunTest.navigate;
import static org.junit.Assert.*;

public class OSDUIntegration {
    public final List<OSDULineageAssertion> lineageAssertions;
    public final List<String> ownerGroup;
    public final List<String> viewerGroup;
    public final List<String> legalTags;
    public final String oSDUGeoJSON;
    public final PlaneAngleMeasure wGS84Latitude;
    public final PlaneAngleMeasure wGS84Longitude;
    public final OSDUSpatialLocationIntegration wGS84LocationMetadata;
    public final String field;
    public final String country;
    public final String state;
    public final String county;
    public final String city;
    public final String region;
    public final String district;
    public final String block;
    public final String prospect;
    public final String play;
    public final String basin;
    private BsonDocument doc;

    private OSDUIntegration(List<OSDULineageAssertion> lineageAssertions, List<String> ownerGroup, List<String> viewerGroup, List<String> legalTags, String oSDUGeoJSON, PlaneAngleMeasure wGS84Latitude, PlaneAngleMeasure wGS84Longitude, OSDUSpatialLocationIntegration wGS84LocationMetadata, String field, String country, String state, String county, String city, String region, String district, String block, String prospect, String play, String basin, BsonDocument doc) {
        this.lineageAssertions = lineageAssertions;
        this.ownerGroup = ownerGroup;
        this.viewerGroup = viewerGroup;
        this.legalTags = legalTags;
        this.oSDUGeoJSON = oSDUGeoJSON;
        this.wGS84Latitude = wGS84Latitude;
        this.wGS84Longitude = wGS84Longitude;
        this.wGS84LocationMetadata = wGS84LocationMetadata;
        this.field = field;
        this.country = country;
        this.state = state;
        this.county = county;
        this.city = city;
        this.region = region;
        this.district = district;
        this.block = block;
        this.prospect = prospect;
        this.play = play;
        this.basin = basin;
        this.doc = doc;
    }

    public static OSDUIntegration build(List<OSDULineageAssertion> lineageAssertions, List<String> ownerGroup, List<String> viewerGroup, List<String> legalTags, String oSDUGeoJSON, PlaneAngleMeasure wGS84Latitude, PlaneAngleMeasure wGS84Longitude, OSDUSpatialLocationIntegration wGS84LocationMetadata, String field, String country, String state, String county, String city, String region, String district, String block, String prospect, String play, String basin, BsonDocument doc) {
        return new OSDUIntegration(lineageAssertions, ownerGroup, viewerGroup, legalTags, oSDUGeoJSON, wGS84Latitude, wGS84Longitude, wGS84LocationMetadata, field, country, state, county, city, region, district, block, prospect, play, basin, doc);
    }

    public void test() throws Exception {
        BsonArray documentArr = (BsonArray) navigate(this.doc, "LineageAssertions");
        BsonDocument document;
        if (this.lineageAssertions != null) {
            assertNotNull(documentArr);
            assertEquals(this.lineageAssertions.size(), documentArr.size());

            for (int i = 0; i < this.lineageAssertions.size(); i++) {
                document = (BsonDocument)documentArr.get(i);
                OSDULineageAssertion lineageAssertionLocal = this.lineageAssertions.get(i);
                lineageAssertionLocal.setOSDULineageAssertion(document);
                lineageAssertionLocal.test();
            }
        } else
            assertNull(documentArr);

        testStringList(this.ownerGroup, "OwnerGroup", this.doc);
        testStringList(this.viewerGroup, "ViewerGroup", this.doc);
        testStringList(this.legalTags, "LegalTags", this.doc);
        testString(this.oSDUGeoJSON, "OSDUGeoJSON", this.doc);

        document = (BsonDocument) navigate(this.doc, "WGS84Latitude");
        if (this.wGS84Latitude != null) {
            assertNotNull(document);
            this.wGS84Latitude.setPlaneAngleMeasure(document);
            this.wGS84Latitude.test();
        } else
            assertNull(document);

        document = (BsonDocument) navigate(this.doc, "WGS84Longitude");
        if (this.wGS84Longitude != null) {
            assertNotNull(document);
            this.wGS84Longitude.setPlaneAngleMeasure(document);
            this.wGS84Longitude.test();
        } else
            assertNull(document);

        document = (BsonDocument)navigate(this.doc, "WGS84LocationMetadata");
        if (this.wGS84LocationMetadata != null) {
            assertNotNull(document);
            this.wGS84LocationMetadata.setOSDUSpatialLocationIntegration(document);
            this.wGS84LocationMetadata.test();
        } else
            assertNull(document);

        testString(this.field, "Field", this.doc);
        testString(this.country, "Country", this.doc);
        testString(this.state, "State", this.doc);
        testString(this.county, "County", this.doc);
        testString(this.city, "City", this.doc);
        testString(this.region, "Region", this.doc);
        testString(this.district, "District", this.doc);
        testString(this.block, "Block", this.doc);
        testString(this.prospect, "Prospect", this.doc);
        testString(this.play, "Play", this.doc);
        testString(this.basin, "Basin", this.doc);
    }

    public void setOSDUIntegration(BsonDocument doc) {
        this.doc = doc;
    }
}
