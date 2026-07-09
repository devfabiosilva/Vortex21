using System.Runtime.InteropServices;
using W21CSparser.exceptions;

namespace W21CSparser.parser;

public partial class W21Parser: IDisposable
{
    nint _soap = nint.Zero;
    public void Test()
    {
        Console.WriteLine($"Teste {this.ToString()}");
    }

    [LibraryImport("w21cs", EntryPoint = "cs_w21_get_xml_strict")]
    private static partial ulong GetXmlStrict();

    [LibraryImport("w21cs", EntryPoint = "cs_w21_get_xml_ignorens")]
    private static partial ulong GetXmlIgnoreNS();

    [LibraryImport("w21cs", EntryPoint = "cs_w21_config_new")]
    private static partial int W21ConfigNew(ref nint soap, ulong in_opts, ulong out_opts);

    [LibraryImport("w21cs", EntryPoint = "w21_config_free")]
    private static partial void W21ConfigFree(ref nint soap);

    [LibraryImport("w21cs", EntryPoint = "w21_recycle")]
    private static partial void W21Recycle(nint soap);

    public static readonly ulong XmlStrict = GetXmlStrict();
    public static readonly ulong XmlIgnoreNS = GetXmlIgnoreNS();

    ~W21Parser() => CleanUp();

    /// <summary>
    /// Initializes native C WITSML 2.1 instance for validating and parsing WITSML documents.
    /// </summary>
    /// <exception cref="W21Exception">Exceptions is created with detailed message when native method fails (error != 0). </exception>
    public (int error, W21Exception? ErrorEx) TryInit(ulong inputOptions, ulong outputOptions)
    {
        int err = W21ConfigNew(ref _soap, inputOptions, outputOptions);
        Console.WriteLine("Initializing C Witsml 2.1 ...");

        W21Exception? ex = null;

        if (err != 0) {
            string errMsg = $"Native function cs_w21_config_new failed with error code {err}. Possible causes: check correct in options, out options, already initialized or memory available";
            ex = new W21Exception(
                message: "C WITSML 2.1 memory allocate init failed. See faultstring and xmlfaultdetail for details", 
                error: err,
                faultstring: errMsg,
                xmlfaultdetail: $"<WITSML21_ERROR code={err}>{errMsg}</WITSML21_ERROR>"
            );
        }

        return (err, ex);
    }

    public void Recycle()
    {
        if (_soap != nint.Zero)
        {
            W21Recycle(_soap);
        }

        // Fail safe. Do nothing if instance is already closed or uninitialized
    }
    public void Dispose()
    {
        CleanUp();
        GC.SuppressFinalize(this);
    }

    private void CleanUp()
    {
        if (_soap != nint.Zero)
        {
            Console.WriteLine("Closing C Witsml 2.1 ...");
            W21ConfigFree(ref _soap);
            _soap = nint.Zero;
        }
    }
}