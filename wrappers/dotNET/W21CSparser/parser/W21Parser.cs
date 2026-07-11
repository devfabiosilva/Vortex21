using System.Runtime.CompilerServices;
using System.Runtime.InteropServices;
using System.Text;
using W21CSparser.exceptions;

[assembly: DisableRuntimeMarshalling]

namespace W21CSparser.parser;

public partial class W21Parser: IDisposable
{
    nint _soap = nint.Zero;
    private readonly SemaphoreSlim _mutex = new(1, 1);

    [StructLayout(LayoutKind.Sequential)]
    private struct W21ErrorFieldsNative
    {
        public int error;
        public nint Tag;
        public nint Message;
    }

    public struct W21ErrorFields
    {
        public int error;
        public string Tag;
        public string Message;
    }

    [LibraryImport("w21cs", EntryPoint = "cs_w21_get_error_detail")]
    private static partial nint GetErrorDetailNative(int error);

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

    [LibraryImport("w21cs", EntryPoint = "cw21rd_AutoDetect")]
    public static unsafe partial int ReadFromStreamNative(nint soapCtx, byte *bufferPtr, nint bufferSize);

    [LibraryImport("w21cs", EntryPoint = "cs_w21_get_fault_string")]
    private static partial nint GetFaultDetailNative(nint soap);

    [LibraryImport("w21cs", EntryPoint = "cs_w21_get_fault_string_xml")]
    private static partial nint GetFaultDetailXMLNative(nint soap);

    [LibraryImport("w21cs", EntryPoint = "w21_enable_input_rules_validator")]
    private static partial int EnableInputValidatorNative(nint soap);

    public static readonly ulong XmlStrict = GetXmlStrict();
    public static readonly ulong XmlIgnoreNS = GetXmlIgnoreNS();


    public int EnableInputValidator()
    {
        _mutex.Wait();
        try
        {
            return (_soap != nint.Zero)?EnableInputValidatorNative(_soap):-1;
        } finally
        {
            _mutex.Release();
        }
    }

    private string _GetFaultDetail()
    {
        // _soap MUST BE NOT NULL
        string faultDetail;

        unsafe
        {
            var spanTag = MemoryMarshal.CreateReadOnlySpanFromNullTerminated((byte*)(GetFaultDetailNative(_soap)));
            faultDetail = Encoding.UTF8.GetString(spanTag);
        }

        return faultDetail;
    }

    public string? GetFaultDetail()
    {
        _mutex.Wait();
        try {
            return (_soap != nint.Zero)?_GetFaultDetail():null;
        } finally
        {
            _mutex.Release();
        }
    }


    private string _GetFaultDetailXML()
    {
        // _soap MUST BE NOT NULL
        string faultDetailXml;

        unsafe
        {
            var spanTag = MemoryMarshal.CreateReadOnlySpanFromNullTerminated((byte*)(GetFaultDetailXMLNative(_soap)));
            faultDetailXml = Encoding.UTF8.GetString(spanTag);
        }

        return faultDetailXml;
    }

    public string? GetFaultDetailXML()
    {
        _mutex.Wait();
        try {
            return (_soap != nint.Zero)?_GetFaultDetailXML():null;
        } finally
        {
            _mutex.Release();
        }
    }
    ~W21Parser() => CleanUp();

    /// <summary>
    /// Initializes native C WITSML 2.1 instance for validating and parsing WITSML documents.
    /// </summary>
    /// <exception cref="W21Exception">Exceptions is created with detailed message when native method fails (error != 0). </exception>
    public (int error, W21Exception? ErrorEx) TryInit(ulong inputOptions, ulong outputOptions)
    {
        _mutex.Wait();
        try {
            int err = W21ConfigNew(ref _soap, inputOptions, outputOptions);
            Console.WriteLine("Initializing C Witsml 2.1 ...");

            W21Exception? ex = null;

            if (err != 0) {
                W21ErrorFields w21ErrorFields = GetErrorDetail(err);
                string errMsg = $"Native function cs_w21_config_new failed with error code {err}. Possible causes: check correct in options, out options, already initialized or memory available";
                ex = new W21Exception(
                    message: "C WITSML 2.1 memory allocate init failed. See faultstring and xmlfaultdetail for details", 
                    error: err,
                    faultstring: errMsg,
                    xmlfaultdetail: $"<WITSML21_ERROR code={err}>\n{errMsg}\nError tag: {w21ErrorFields.Tag}\nDetail: {w21ErrorFields.Message}</WITSML21_ERROR>"
                );
            }

            return (err, ex);
        } finally
        {
            _mutex.Release();
        }
    }

    public void Recycle()
    {
        _mutex.Wait();
        try {
            if (_soap != nint.Zero)
            {
                W21Recycle(_soap);
            }
        } finally
        {
            _mutex.Release();            
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

    public W21ErrorFields GetErrorDetail(int err)
    {
        nint structPtr = GetErrorDetailNative(err); // Return always non null
        W21ErrorFields w21ErrorFields = new();
    
        w21ErrorFields.error = err;

        W21ErrorFieldsNative nativeFields = Marshal.PtrToStructure<W21ErrorFieldsNative>(structPtr);
        unsafe
        {
            var spanTag = MemoryMarshal.CreateReadOnlySpanFromNullTerminated((byte*)(nativeFields.Tag));
            w21ErrorFields.Tag = Encoding.UTF8.GetString(spanTag);

            var spanMsg = MemoryMarshal.CreateReadOnlySpanFromNullTerminated((byte*)(nativeFields.Message));
            w21ErrorFields.Message = Encoding.UTF8.GetString(spanMsg);
        }

        return w21ErrorFields;
    }

    public (bool success, W21Exception? ErrorEx) ReadFromStream(byte []stream)
    {

        _mutex.Wait();

        W21Exception? ex = null;
        try
        {
            if (_soap != nint.Zero)
            {
                if ((stream != null) && (stream.Length > 0))
                {
                    unsafe
                    {
                        fixed (byte *pByte = stream)
                        {
                            int err = ReadFromStreamNative(_soap, pByte, (nint)stream.Length);

                            if (err != 0)
                            {
                                ex = new W21Exception(
                                    message: "ReadFromStream error. See faultstring for details", 
                                    error: err,
                                    faultstring: _GetFaultDetail(),
                                    xmlfaultdetail: _GetFaultDetailXML()
                                );
                            }
                        }
                    }
                } else
                {
                    ex = new W21Exception(
                        message: "Unable to read from stream. Null or empty utf-8 stream", 
                        error: -2,
                        faultstring: "",
                        xmlfaultdetail: ""
                    );
                }
            } else
            {
                ex = new W21Exception(
                    message: "Unable to read from stream. Parser already closed or not initialized", 
                    error: -1,
                    faultstring: "",
                    xmlfaultdetail: ""
                );
            }
        } finally
        {
            _mutex.Release();
        }

        return (ex == null, ex);
    }
}