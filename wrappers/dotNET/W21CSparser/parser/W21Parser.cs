using System.Runtime.InteropServices;

namespace W21CSparser.parser;

public partial class W21Parser: IDisposable
{

    nint _soap = nint.Zero;
    public void Test()
    {
        Console.WriteLine($"Teste {this.ToString()}");
    }

    [LibraryImport("w21cs", EntryPoint = "cs_w21_config_new")]
    private static partial int W21ConfigNew(ref nint soap, ulong in_opts, ulong out_opts);

    [LibraryImport("w21cs", EntryPoint = "w21_config_free")]
    private static partial void W21ConfigFree(ref nint soap);

    [LibraryImport("w21cs", EntryPoint = "w21_recycle")]
    private static partial void W21Recycle(nint soap);

    ~W21Parser() => CleanUp();
    public void Init()
    {
        int err = W21ConfigNew(ref _soap, 0, 0);
        Console.WriteLine("Initializing C Witsml 2.1 ...");
        if (err != 0) {
            throw new Exception($"C WITSML 2.1 init fail {err}.");
        }
    }

    public void Recycle()
    {
        if (_soap != nint.Zero)
        {
            W21Recycle(_soap);
            return;
        }

        throw new Exception("Instance is closed or invalid");
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