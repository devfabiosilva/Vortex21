using System;
using W21CSparser.parser;

namespace W21CSparser;

public class MainApp {
    public static int Main(string[] args)
    {
        
        InstanceInitAndFinishTest();
        //GC.Collect();
        //GC.WaitForPendingFinalizers();

        Console.WriteLine("End of main.");
        return 0;
    }

    private static void InstanceInitAndFinishTest()
    {
        using W21Parser parser = new();
        var (error, errorException) = parser.TryInit(1+W21Parser.XmlIgnoreNS|W21Parser.XmlStrict, 0);

        if (error != 0)
        {
            Console.WriteLine(errorException?.Message);
            Console.WriteLine($"Fault string message: {errorException?.FaultString}");
            Console.WriteLine($"Detailed fault string message: {errorException?.XmlFaultDetail}");
            return;
        }

        parser.Recycle();
    }

}