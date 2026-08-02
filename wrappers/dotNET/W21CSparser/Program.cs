using W21CSparser.exceptions;
using W21CSparser.parser;
using static W21CSparser.parser.W21Parser;

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

        Console.WriteLine($"Before BSON/JSON parsing {parser.GetObjectName()}");

        byte[] stream = File.ReadAllBytes("bin/Debug/net8.0/Log.xml");

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

        Console.WriteLine($"After BSON/JSON parsing {parser.GetObjectName()}");
        Console.WriteLine(result);
        Console.WriteLine($"Version: {parser.GetVersionString()}");
        Console.WriteLine($"Build date: {parser.GetBuildDateString()}");
        parser.Recycle();

        var (res3, ex3) = parser.ReadFromFile("bin/Debug/net8.0/OpsReport.xml");

        if (ex3 != null)
        {
            if (ex3 is W21Exception)
            {
                Console.WriteLine(ex3?.Message);
                Console.WriteLine($"From file: Fault string message: {((W21Exception?)ex3)?.FaultString}");
                Console.WriteLine($"From file: Detailed fault string message: {((W21Exception?)ex3)?.XmlFaultDetail}");
            } else
                throw ex3;
        }

        W21Parser.W21ReadStatistics? readStatistics = parser.readWITSML21Statistics();
        if (readStatistics != null) {
            Console.WriteLine($"Stats CPB {readStatistics?.CPB}");
            Console.WriteLine($"Stats CPU Cycles {readStatistics?.CPUCycles}");
            Console.WriteLine($"Stats Throughput MB/s {readStatistics?.Throughput}");
            Console.WriteLine($"Stats Total Time (ms) {readStatistics?.TotalTime}");
            Console.WriteLine($"Stats Used Memory MB {readStatistics?.TotalMem/(1024*1024.0)} (Bytes) {readStatistics?.TotalMem}");
        } else
        {
            Console.WriteLine("Unable to get hardware statistics for phase 1");
        }

        var (result4, ex4) = parser.ParseAsJSON();

        if (ex4 != null)
        {
            Console.WriteLine(ex4?.Message);
            Console.WriteLine($"Fault string message: {ex4?.FaultString}");
            Console.WriteLine($"Detailed fault string message: {ex4?.XmlFaultDetail}");
        }

        W21Parser.W21ReadStatistics? parseStatistics = parser.parseStatistics();
        if (parseStatistics != null) {
            Console.WriteLine($"Stats BSON CPB {parseStatistics?.CPB}");
            Console.WriteLine($"Stats BSON CPU Cycles {parseStatistics?.CPUCycles}");
            Console.WriteLine($"Stats BSON Throughput MB/s {parseStatistics?.Throughput}");
            Console.WriteLine($"Stats BSON Total Time (ms) {parseStatistics?.TotalTime}");
            Console.WriteLine($"Stats BSON Used Memory MB {parseStatistics?.TotalMem/(1024*1024.0)} (Bytes) {parseStatistics?.TotalMem}");
        } else
        {
            Console.WriteLine("Unable to get hardware statistics for phase 2 (BSON)");
        }

        W21DocumentStatistics? documentStatistics = parser.documentStatistics();

        if (documentStatistics != null)
        {
            Console.WriteLine(
                "\nDocument Statistics:" +
                $"\n\tArays: {documentStatistics?.arrays}" +
                $"\n\tBooleans: {documentStatistics?.booleans}" +
                $"\n\tCosts: {documentStatistics?.costs}" +
                $"\n\tDate times: {documentStatistics?.date_times}" +
                $"\n\tDoubles: {documentStatistics?.doubles}" +
                $"\n\tEnums: {documentStatistics?.enums}" +
                $"\n\tEvent types: {documentStatistics?.event_types}" +
                $"\n\tInts: {documentStatistics?.ints}" +
                $"\n\tLong64s: {documentStatistics?.long64s}" +
                $"\n\tMeasures: {documentStatistics?.measures}" +
                $"\n\tShorts: {documentStatistics?.shorts}" +
                $"\n\tStrings: {documentStatistics?.strings}" +
                $"\n\tTotal: {documentStatistics?.total}"
            );            
        } else
        {
            Console.WriteLine("\nDocument Statistics: Error or not parsed yet\n");
        }

        W21Parser.W21ReadStatistics? totalStatistics = parser.totalStatistics();
        if (parseStatistics != null) {
            Console.WriteLine($"Total Stats BSON CPB {totalStatistics?.CPB}");
            Console.WriteLine($"Total Stats BSON CPU Cycles {totalStatistics?.CPUCycles}");
            Console.WriteLine($"Total Stats BSON Throughput MB/s {totalStatistics?.Throughput}");
            Console.WriteLine($"Total Stats BSON Total Time (ms) {totalStatistics?.TotalTime}");
            Console.WriteLine($"Total Stats BSON Used Memory MB {totalStatistics?.TotalMem/(1024*1024.0)} (Bytes) {totalStatistics?.TotalMem}");
        } else
        {
            Console.WriteLine("Unable to get hardware total statistics for phase 2 (BSON)");
        }
        
    }

}