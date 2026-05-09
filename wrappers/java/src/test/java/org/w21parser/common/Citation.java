package org.w21parser.common;

import org.bson.BsonDocument;

import java.util.List;

import static org.w21parser.common.Utils.*;

public class Citation {
    private final String title;
    private final String originator;
    private final String creation;
    private final String format;
    private final String editor;
    private final String lastUpdate;
    private final String description;
    private final List<String> editorHistory;
    private final String descriptiveKeywords;
    private final BsonDocument citation;

    private Citation(String title, String originator, String creation, String format, String editor, String lastUpdate, String description, List<String> editorHistory, String descriptiveKeywords, BsonDocument citation) {
        this.title = title;
        this.originator = originator;
        this.creation = creation;
        this.format = format;
        this.editor = editor;
        this.lastUpdate = lastUpdate;
        this.description = description;
        this.editorHistory = editorHistory;
        this.descriptiveKeywords = descriptiveKeywords;
        this.citation = citation;
    }

    public static Citation build(String title, String originator, String creation, String format, String editor, String lastUpdate, String description, List<String> editorHistory, String descriptiveKeywords, BsonDocument citation) {
        return new Citation(title, originator, creation, format, editor, lastUpdate, description, editorHistory, descriptiveKeywords, citation);
    }

    public void test() throws Exception {
        testString(title, "Title", citation);
        testString(originator, "Originator", citation);
        testDateTime(creation, "Creation", citation);
        testString(format, "Format", citation);
        testString(editor, "Editor", citation);
        testDateTime(lastUpdate, "LastUpdate", citation);
        testString(description, "Description", citation);
        testStringList(editorHistory, "EditorHistory", citation);
        testString(descriptiveKeywords, "DescriptiveKeywords", citation);
    }
}
