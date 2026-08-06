using Xunit;
using W21CSparser.parser;
using W21CSparser.exceptions;

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

}