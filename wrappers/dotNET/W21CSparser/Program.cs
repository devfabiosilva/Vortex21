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
        parser.Init();
        parser.Recycle();
    }

}