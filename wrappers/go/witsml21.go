package main

//go mod init parser_test
//go build -asan -o parser_test witsml21.go
//-fsanitize=address,leak
//nm -D libw21parser.so | grep " T " | wc -l

/*
#cgo CFLAGS: -I${SRCDIR}/c_src/include/ -I${SRCDIR}/../.. -I${SRCDIR}/../../core/include -fsanitize=address,leak
#cgo LDFLAGS: -L${SRCDIR}/c_src/lib -lw21go
#include <w21go.h>
#include <go_w21_errors.h>
#include <w21_input.h>
#include <w21_deserializer.h>
*/
import "C"
import (
	"fmt"
	"unsafe"
	"os"
	"sync"
	"go.mongodb.org/mongo-driver/v2/bson"
)

type W21Config struct {
	mu       sync.Mutex
	cSoapPtr *C.struct_soap
	cBsonPtr *C.struct_c_bson_serialized_t
}

func W21ConfigNew(inOptions int, outOptions int) (*W21Config, error) {
	var nativeSoap *C.struct_soap
	status := C.go_w21_config_new(
		(**C.struct_soap)(unsafe.Pointer(&nativeSoap)),
		C.uint64_t(inOptions),
		C.uint64_t(outOptions),
	)

	if status == 0 {
		return &W21Config{cSoapPtr: nativeSoap}, nil
	}

	return nil, fmt.Errorf("Unable to load config: parser status %d", status)
}

func (w21 *W21Config) ReadFromStream(witsml21Data []byte) (int) {
	w21.mu.Lock()
	defer w21.mu.Unlock()

	var cPtr *C.char
    if len(witsml21Data) > 0 {
        cPtr = (*C.char)(unsafe.Pointer(&witsml21Data[0]))
    }

	return int(C.cw21rd_AutoDetect(
		w21.cSoapPtr,
		cPtr,
		C.size_t(len(witsml21Data)),
	))
}

func (w21 *W21Config)ToBsonBytes() (int ,[]byte) {
	w21.mu.Lock()
	defer w21.mu.Unlock()

	if w21.cSoapPtr != nil {
		w21.cBsonPtr = C.w21_bson_serialize(w21.cSoapPtr)

		if w21.cBsonPtr != nil {
			// unsafe.Slice = ZERO COPY from C
			return int(C.go_get_w21_error(w21.cSoapPtr)), unsafe.Slice((*byte)(unsafe.Pointer(w21.cBsonPtr.bson)), int(w21.cBsonPtr.bson_size))
		}

		return int(C.go_get_w21_error(w21.cSoapPtr)), nil
	}

	return int(C.E_GO_W21_ERROR_INVALID_W21_HANDLER), nil
}

func (w21 *W21Config)EnableValidator() (int) {
	w21.mu.Lock()
	defer w21.mu.Unlock()

	return int(C.go_w21_enable_input_rules_validator(w21.cSoapPtr))
}

func (w21 *W21Config)Parse() (int) {
	w21.mu.Lock()
	defer w21.mu.Unlock()

	if (w21.cSoapPtr != nil) {
		return int(C.bson_read_AutoDetect21(w21.cSoapPtr))
	}

	return int(C.E_GO_W21_ERROR_INVALID_W21_HANDLER)
}

func main() {
	// Testing wrapper
	w21Conf, err := W21ConfigNew(C.SOAP_XML_STRICT|C.SOAP_XML_IGNORENS, 0)
	if err != nil {
		fmt.Println("Err:", err)
		return
	}

	defer func() {
		fmt.Printf("\nDestroying C WITSML 2.1 instance at %p ...", w21Conf.cSoapPtr)
		C.w21_config_free((**C.struct_soap)(unsafe.Pointer(&w21Conf.cSoapPtr)))
		fmt.Printf("\nPointer after free: C WITSML 2.1 instance at %p ...", w21Conf.cSoapPtr)
	}()

	status := w21Conf.EnableValidator()
	if (status != 0) {
		fmt.Printf("\nUnable to enable validator %d", status)
		return
	}

	xmlContent, err := os.ReadFile("../java/TestFiles/xmls/strict_valid/OpsReport.xml")
	if err != nil {
		fmt.Printf("\nRead file error: %v", err)
		return
	}

	status = w21Conf.ReadFromStream(xmlContent)
	if (status != 0) {
		fmt.Printf("Error %d", status)
		return
	}

	status = w21Conf.Parse()
	if (status != 0) {
		fmt.Printf("BSON parsing error %d", status)
		return
	}

	status, bsonBytes := w21Conf.ToBsonBytes()
	if status == 0 {
		fmt.Printf("Bytes loaded %d\n", len(bsonBytes))

        var bsonDocumentoGo bson.D

        err = bson.Unmarshal(bsonBytes, &bsonDocumentoGo)
        if err != nil {
            fmt.Printf("Bytes to BSON error: %v\n", err)
            return
        }

        jsonBytes, err := bson.MarshalExtJSON(bsonDocumentoGo, true, false)
        if err != nil {
            fmt.Printf("JSON format error: %v\n", err)
            return
        }

        fmt.Println("\n--- WITSML 2.1 IN JSON ---")
        fmt.Println(string(jsonBytes))
        fmt.Println("---------------------------------------")
	} else {
		fmt.Printf("Error bytes %d", status)
	}
	// TODO: implement it
	fmt.Println("Loaded!")
}
