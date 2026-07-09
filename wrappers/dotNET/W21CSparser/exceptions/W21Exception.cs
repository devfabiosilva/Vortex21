namespace W21CSparser.exceptions;

public class W21Exception : Exception
{
    public int Error { get; }
    public string FaultString { get; }
    public string XmlFaultDetail { get; }

    public W21Exception(string message, int error, string? faultstring, string? xmlfaultdetail) 
        : base(message)
    {
        Error = error;
        
        FaultString = faultstring ?? string.Empty;
        XmlFaultDetail = xmlfaultdetail ?? string.Empty;
    }
}