using Xunit;
using W21CSparser.parser;

namespace W21CSparser.Tests;

public class W21ParserTests
{
    [Fact]
    public void GivenValidLogXml_WhenParsedAsJson_ShouldSucceedAndReturnStatistics()
    {

        using W21Parser parser = new();
        var (initError, initEx) = parser.TryInit(W21Parser.XmlIgnoreNS | W21Parser.XmlStrict, 0);
        
        Assert.True(initError == 0, $"Parser init error: {initEx?.Message}");

        int validatorErr = parser.EnableInputValidator();
        Assert.Equal(0, validatorErr);
    
        string xmlPath = "Log.xml"; 
        Assert.True(File.Exists(xmlPath), $"Test file {xmlPath} not found.");

        byte[] stream = File.ReadAllBytes(xmlPath);

        // Act 
        var (readSuccess, readEx) = parser.ReadFromStream(stream);
        Assert.True(readSuccess, $"Stream error: {readEx?.Message}");

        Assert.Equal("Log", parser.GetObjectName());

        var (jsonResult, jsonEx) = parser.ParseAsJSON();

        // Assert (Validation)
        Assert.Null(jsonEx);
        Assert.False(string.IsNullOrEmpty(jsonResult), "JSON shoud not be empty");

        //
        W21Parser.W21ReadStatistics? stats = parser.readWITSML21Statistics();
        Assert.NotNull(stats);
        Assert.True(stats?.CPUCycles > 0, "CPU cycles should greater than zero");
        Assert.True(stats?.Throughput > 0, "Throughput should be greater than zero");
    }

    [Fact]
    public void GivenInvalidDocument_ShouldShowDetailed_DescriptionAndError()
    {
        using W21Parser parser = new();
        var (initError, initEx) = parser.TryInit(W21Parser.XmlIgnoreNS | W21Parser.XmlStrict, 0);
        
        Assert.True(initError == 0, $"Parser init error: {initEx?.Message}");

        int validatorErr = parser.EnableInputValidator();
        Assert.Equal(0, validatorErr);

        string xmlPath = "LogInvalid.xml"; 
        Assert.True(File.Exists(xmlPath), $"Test file {xmlPath} not found.");

        byte[] stream = File.ReadAllBytes(xmlPath);

        // Act 
        var (readFail, readEx) = parser.ReadFromStream(stream);
        Assert.False(readFail, $"Stream error: {readEx?.Message}");

        Assert.Equal(600, readEx?.Error);
        Assert.Equal("ReadFromStream error. See faultstring for details", readEx?.Message);
        Assert.Equal("Invalid value \"custom388.test\" for rdw212__QualifiedType type. Was expected value with pattern \"^(witsml|resqml|prodml|eml|custom)[1-9][0-9]\\.[A-Za-z0-9_]+$\" in tag or attribute rdw212:QualifiedType", readEx?.FaultString);
        Assert.Equal("<WITSML21_ERROR code=600>Invalid value \"custom388.test\" for rdw212__QualifiedType type. Was expected value with pattern \"^(witsml|resqml|prodml|eml|custom)[1-9][0-9]\\.[A-Za-z0-9_]+$\" in tag or attribute rdw212:QualifiedType</WITSML21_ERROR>", readEx?.XmlFaultDetail);
    }


    [Fact]
    public void VerifyCoreVersionAndBuildDate()
    {
        using W21Parser parser = new();
        
        Assert.Equal("0.1.0-beta", parser.GetVersionString());
        Assert.Equal("202607151739-GMT: -3", parser.GetBuildDateString());
    }

    [Fact]
    public void CheckValidFieldsInBson()
    {
        using W21Parser parser = new();
        var (initError, initEx) = parser.TryInit(W21Parser.XmlIgnoreNS | W21Parser.XmlStrict, 0);
        
        Assert.True(initError == 0, $"Parser init error: {initEx?.Message}");

        int validatorErr = parser.EnableInputValidator();
        Assert.Equal(0, validatorErr);

        string xmlPath = "Log.xml"; 
        Assert.True(File.Exists(xmlPath), $"Test file {xmlPath} not found.");

        byte[] stream = File.ReadAllBytes(xmlPath);

        // Act 
        var (success, readEx) = parser.ReadFromStream(stream);
        Assert.True(success, $"Stream error: {readEx?.Message}");
        Assert.Null(readEx);

        var (bson, bsonEx) = parser.ParseAsBSON();
        Assert.Null(bsonEx);
        Assert.NotNull(bson);

        string? res = (string?)BsonNavigator.Navigate(bson, "Log", "#attributes", "uuid");
        Assert.Equal("523e4568-e89b-12d3-a456-426614174000", res);
        string? citationTitle = (string?)BsonNavigator.Navigate(bson, "Log", "Citation", "Title");
        Assert.Equal("Citation title string (max 256 chars)", citationTitle?.Trim());
    }
}