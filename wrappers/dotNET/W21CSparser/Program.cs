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
        var (error, errorException) = parser.TryInit(W21Parser.XmlIgnoreNS|W21Parser.XmlStrict, 0);

        if (error != 0)
        {
            Console.WriteLine(errorException?.Message);
            Console.WriteLine($"Fault string message: {errorException?.FaultString}");
            Console.WriteLine($"Detailed fault string message: {errorException?.XmlFaultDetail}");
            return;
        }

        int err = parser.EnableInputValidator();
        if (err != 0)
        {
            Console.WriteLine($"Enable validator error {err}");
            return;
        }

        byte[] stream = File.ReadAllBytes("bin/Debug/net8.0/OpsReport.xml");
        var (res, ex) = parser.ReadFromStream(stream);
        if (res == false)
        {
            Console.WriteLine(ex?.Message);
            Console.WriteLine($"Fault string message: {ex?.FaultString}");
            Console.WriteLine($"Detailed fault string message: {ex?.XmlFaultDetail}");
        }

        var (result, ex1) = parser.ParseAsJSON();

        if (ex1 != null)
        {
            Console.WriteLine(ex1?.Message);
            Console.WriteLine($"Fault string message: {ex1?.FaultString}");
            Console.WriteLine($"Detailed fault string message: {ex1?.XmlFaultDetail}");
        }

        Console.WriteLine(result);
        parser.Recycle();
    }

}