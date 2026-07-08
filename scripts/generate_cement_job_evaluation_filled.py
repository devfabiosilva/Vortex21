#!/usr/bin/env python3
from pathlib import Path
import re

ROOT = Path(__file__).resolve().parent.parent
SRC = ROOT / "xmls" / "CementJobEvaluation.xml"
DST = ROOT / "xmls" / "CementJobEvaluationFilled.xml"

TEXT_VALUES = {
    "Identifier": "identifier-001",
    "IdentifierKind": "identifier-kind-001",
    "Description": "description-001",
    "Title": "title-001",
    "Originator": "originator-001",
    "Format": "format-001",
    "Editor": "editor-001",
    "EditorHistory": "editor-history-001",
    "DescriptiveKeywords": "descriptive-keywords-001",
    "ObjectVersionReason": "object-version-reason-001",
    "BusinessActivityHistory": "business-activity-history-001",
    "ID": "lineage-id-001",
    "OwnerGroup": "owner-group-001",
    "ViewerGroup": "viewer-group-001",
    "LegalTags": "legal-tags-001",
    "OSDUGeoJSON": '{"type":"Point","coordinates":[0,0]}',
    "QuantitativeAccuracyBand": "quantitative-accuracy-band-001",
    "QualitativeSpatialAccuracyType": "qualitative-spatial-accuracy-type-001",
    "CoordinateQualityCheckPerformedBy": "checker-001",
    "CoordinateQualityCheckRemark": "remark-001",
    "AppliedOperation": "applied-operation-001",
    "Field": "field-001",
    "Country": "country-001",
    "State": "state-001",
    "County": "county-001",
    "City": "city-001",
    "Region": "region-001",
    "District": "district-001",
    "Block": "block-001",
    "Prospect": "prospect-001",
    "Play": "play-001",
    "Basin": "basin-001",
    "Name": "name-001",
    "Value": "value-001",
    "ToolCompanyPit": "tool-company-pit-001",
    "TopCementMethod": "top-cement-method-001",
    "JobRating": "job-rating-001",
    "FailureMethod": "failure-method-001",
    "TestNegativeTool": "test-negative-tool-001",
    "TestPositiveTool": "test-positive-tool-001",
    "Existence": "exists",
}

ATTR_VALUES = {
    "authority": "authority-001",
    "uom": "m",
}


def local_name(tag: str) -> str:
    return tag.split("}")[-1].split(":")[-1]


def fill_empty_text_nodes(xml_text: str) -> str:
    def repl(match: re.Match[str]) -> str:
        tag = match.group("tag")
        content = match.group("content")
        if not content.strip():
            name = local_name(tag)
            if name in TEXT_VALUES:
                return match.group(0).replace(content, TEXT_VALUES[name], 1)
            return match.group(0).replace(content, f"{name.lower()}-filled", 1)
        return match.group(0)

    pattern = re.compile(r"(<(?P<tag>[^>/\s]+)(?:[^>]*)>)(?P<content>.*?)</(?P=tag)>", re.S)
    return pattern.sub(repl, xml_text)


def fill_empty_attributes(xml_text: str) -> str:
    def repl(match: re.Match[str]) -> str:
        attr_name = match.group("name")
        if attr_name in ATTR_VALUES:
            return match.group(0).replace(match.group("value"), ATTR_VALUES[attr_name], 1)
        return match.group(0)

    pattern = re.compile(r"(?P<name>\b[a-zA-Z_:.-]+)=\"(?P<value>\")")
    return pattern.sub(repl, xml_text)


def main() -> None:
    if not SRC.exists():
        raise FileNotFoundError(f"Input file not found: {SRC}")

    xml_text = SRC.read_text(encoding="utf-8")
    xml_text = fill_empty_text_nodes(xml_text)
    xml_text = fill_empty_attributes(xml_text)
    xml_text = xml_text.replace('authority=""', 'authority="authority-001"')
    xml_text = xml_text.replace('uom=""', 'uom="m"')
    xml_text = xml_text.replace('<rdw212:ObjectVersion xsi:type="rdw212:String64"></rdw212:ObjectVersion>', '<rdw212:ObjectVersion xsi:type="rdw212:String64">object-version-001</rdw212:ObjectVersion>')
    xml_text = xml_text.replace('<rdw212:Title xsi:type="rdw212:String2000"></rdw212:Title>', '<rdw212:Title xsi:type="rdw212:String2000">title-001</rdw212:Title>')
    xml_text = xml_text.replace('<rdw212:Name xsi:type="rdw212:String64"></rdw212:Name>', '<rdw212:Name xsi:type="rdw212:String64">name-001</rdw212:Name>')
    xml_text = xml_text.replace('<rdw212:Value xsi:type="rdw212:StringMeasure" uom=""></rdw212:Value>', '<rdw212:Value xsi:type="rdw212:StringMeasure" uom="m">value-001</rdw212:Value>')
    xml_text = xml_text.replace('<rdw212:Description xsi:type="rdw212:String2000"></rdw212:Description>', '<rdw212:Description xsi:type="rdw212:String2000">description-001</rdw212:Description>')
    xml_text = xml_text.replace('<rdw212:EditorHistory xsi:type="rdw212:String64"></rdw212:EditorHistory>', '<rdw212:EditorHistory xsi:type="rdw212:String64">editor-history-001</rdw212:EditorHistory>')
    xml_text = xml_text.replace('<rdw212:DescriptiveKeywords xsi:type="rdw212:String2000"></rdw212:DescriptiveKeywords>', '<rdw212:DescriptiveKeywords xsi:type="rdw212:String2000">descriptive-keywords-001</rdw212:DescriptiveKeywords>')
    xml_text = xml_text.replace('<rdw212:BusinessActivityHistory xsi:type="rdw212:String64"></rdw212:BusinessActivityHistory>', '<rdw212:BusinessActivityHistory xsi:type="rdw212:String64">business-activity-history-001</rdw212:BusinessActivityHistory>')
    xml_text = xml_text.replace('<rdw212:OwnerGroup xsi:type="rdw212:String256"></rdw212:OwnerGroup>', '<rdw212:OwnerGroup xsi:type="rdw212:String256">owner-group-001</rdw212:OwnerGroup>')
    xml_text = xml_text.replace('<rdw212:ViewerGroup xsi:type="rdw212:String256"></rdw212:ViewerGroup>', '<rdw212:ViewerGroup xsi:type="rdw212:String256">viewer-group-001</rdw212:ViewerGroup>')
    xml_text = xml_text.replace('<rdw212:LegalTags xsi:type="rdw212:String256"></rdw212:LegalTags>', '<rdw212:LegalTags xsi:type="rdw212:String256">legal-tags-001</rdw212:LegalTags>')
    xml_text = xml_text.replace('<rdw212:Field xsi:type="rdw212:String64"></rdw212:Field>', '<rdw212:Field xsi:type="rdw212:String64">field-001</rdw212:Field>')
    xml_text = xml_text.replace('<rdw212:Country xsi:type="rdw212:String64"></rdw212:Country>', '<rdw212:Country xsi:type="rdw212:String64">country-001</rdw212:Country>')
    xml_text = xml_text.replace('<rdw212:State xsi:type="rdw212:String64"></rdw212:State>', '<rdw212:State xsi:type="rdw212:String64">state-001</rdw212:State>')
    xml_text = xml_text.replace('<rdw212:County xsi:type="rdw212:String64"></rdw212:County>', '<rdw212:County xsi:type="rdw212:String64">county-001</rdw212:County>')
    xml_text = xml_text.replace('<rdw212:Block xsi:type="rdw212:String64"></rdw212:Block>', '<rdw212:Block xsi:type="rdw212:String64">block-001</rdw212:Block>')
    xml_text = xml_text.replace('<rdw212:Prospect xsi:type="rdw212:String64"></rdw212:Prospect>', '<rdw212:Prospect xsi:type="rdw212:String64">prospect-001</rdw212:Prospect>')
    xml_text = xml_text.replace('<rdw212:Play xsi:type="rdw212:String64"></rdw212:Play>', '<rdw212:Play xsi:type="rdw212:String64">play-001</rdw212:Play>')
    xml_text = xml_text.replace('<rdw212:Basin xsi:type="rdw212:String64"></rdw212:Basin>', '<rdw212:Basin xsi:type="rdw212:String64">basin-001</rdw212:Basin>')
    xml_text = xml_text.replace('<rdw212:QuantitativeAccuracyBand xsi:type="rdw212:String64"></rdw212:QuantitativeAccuracyBand>', '<rdw212:QuantitativeAccuracyBand xsi:type="rdw212:String64">quantitative-accuracy-band-001</rdw212:QuantitativeAccuracyBand>')
    xml_text = xml_text.replace('<rdw212:QualitativeSpatialAccuracyType xsi:type="rdw212:String64"></rdw212:QualitativeSpatialAccuracyType>', '<rdw212:QualitativeSpatialAccuracyType xsi:type="rdw212:String64">qualitative-spatial-accuracy-type-001</rdw212:QualitativeSpatialAccuracyType>')
    xml_text = xml_text.replace('<rdw212:CoordinateQualityCheckPerformedBy xsi:type="rdw212:String64"></rdw212:CoordinateQualityCheckPerformedBy>', '<rdw212:CoordinateQualityCheckPerformedBy xsi:type="rdw212:String64">checker-001</rdw212:CoordinateQualityCheckPerformedBy>')
    xml_text = xml_text.replace('<rdw212:CoordinateQualityCheckRemark xsi:type="rdw212:String256"></rdw212:CoordinateQualityCheckRemark>', '<rdw212:CoordinateQualityCheckRemark xsi:type="rdw212:String256">remark-001</rdw212:CoordinateQualityCheckRemark>')
    xml_text = xml_text.replace('<rdw212:AppliedOperation xsi:type="rdw212:String256"></rdw212:AppliedOperation>', '<rdw212:AppliedOperation xsi:type="rdw212:String256">applied-operation-001</rdw212:AppliedOperation>')
    xml_text = xml_text.replace('<rdw211:ToolCompanyPit xsi:type="rdw212:String64"></rdw211:ToolCompanyPit>', '<rdw211:ToolCompanyPit xsi:type="rdw212:String64">tool-company-pit-001</rdw211:ToolCompanyPit>')
    xml_text = xml_text.replace('<rdw211:TopCementMethod xsi:type="rdw212:String64"></rdw211:TopCementMethod>', '<rdw211:TopCementMethod xsi:type="rdw212:String64">top-cement-method-001</rdw211:TopCementMethod>')
    xml_text = xml_text.replace('<rdw211:JobRating xsi:type="rdw212:String64"></rdw211:JobRating>', '<rdw211:JobRating xsi:type="rdw212:String64">job-rating-001</rdw211:JobRating>')
    xml_text = xml_text.replace('<rdw211:FailureMethod xsi:type="rdw212:String64"></rdw211:FailureMethod>', '<rdw211:FailureMethod xsi:type="rdw212:String64">failure-method-001</rdw211:FailureMethod>')
    xml_text = xml_text.replace('<rdw211:TestNegativeTool xsi:type="rdw212:String64"></rdw211:TestNegativeTool>', '<rdw211:TestNegativeTool xsi:type="rdw212:String64">test-negative-tool-001</rdw211:TestNegativeTool>')
    xml_text = xml_text.replace('<rdw211:TestPositiveTool xsi:type="rdw212:String64"></rdw211:TestPositiveTool>', '<rdw211:TestPositiveTool xsi:type="rdw212:String64">test-positive-tool-001</rdw211:TestPositiveTool>')

    DST.write_text(xml_text, encoding="utf-8")
    print(f"Generated {DST}")


if __name__ == "__main__":
    main()
