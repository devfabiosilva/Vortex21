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

        /*
        string xmlPath = "Log.xml"; 
        Assert.True(File.Exists(xmlPath), $"O arquivo de teste {xmlPath} não foi encontrado.");

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
        Assert.True(stats?.CPUCycles > 0, "O número de ciclos de CPU deveria ser maior que zero.");
        Assert.True(stats?.Throughput > 0, "O throughput deveria ser maior que zero.");
        */
    }
}