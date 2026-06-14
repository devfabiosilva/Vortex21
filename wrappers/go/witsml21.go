package main

//go mod init parser_test
//go build -asan -o parser_test witsml21.go
//-fsanitize=address,leak
//nm -D libw21parser.so | grep " T " | wc -l

/*
#cgo CFLAGS: -O3 -march=native -DWITH_STATISTICS -I${SRCDIR}/c_src/include/ -I${SRCDIR}/../.. -I${SRCDIR}/../../core/include -fsanitize=address,leak
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

type W21Statistics struct {
	total int
    costs int
	strings int
	shorts int
	ints int
	long64s int
	enums int
	arrays int
	booleans int
	doubles int
	date_times int
	event_types int
	measures int
}

type W21Config struct {
	mu       			sync.Mutex
	cSoapPtr 			*C.struct_soap
	cBsonPtr 			*C.struct_c_bson_serialized_t
	cJsonPtr 			*C.struct_c_json_str_t

	withStatistics 		bool
	errorStatRead 		int
	errorStatParse 		int
	errorStatParseJson 	int

	statistics 			*W21Statistics
}

func W21ConfigNew(inOptions int, outOptions int) (*W21Config, error) {
	var nativeSoap *C.struct_soap
	status := int(C.go_w21_config_new(
		(**C.struct_soap)(unsafe.Pointer(&nativeSoap)),
		C.uint64_t(inOptions),
		C.uint64_t(outOptions),
	))

	if status == 0 {
		return &W21Config{cSoapPtr: nativeSoap, withStatistics: true}, nil
	}

	return nil, fmt.Errorf("Unable to load config: parser status %d", status)
}

func (w21 *W21Config) ReadFromStream(witsml21Data []byte) (int) {
	w21.mu.Lock()
	defer w21.mu.Unlock()

	if (w21.cSoapPtr != nil) {
		var cPtr *C.char
		if witsml21Data != nil {
			cPtr = (*C.char)(unsafe.Pointer(&witsml21Data[0]))
		}

		if (w21.withStatistics) {
			w21.errorStatRead = int(C.w21_hard_summary_read_begin(w21.cSoapPtr))
		}

		ret := int(C.cw21rd_AutoDetect(
			w21.cSoapPtr,
			cPtr,
			C.size_t(len(witsml21Data)),
		))

		if (w21.withStatistics && w21.errorStatRead == 0) {
			if (ret == 0) {
				w21.errorStatRead = int(C.w21_hard_summary_read_end(w21.cSoapPtr))
			} else {
				w21.errorStatRead = ret
			}
		}

		return ret
	}

	return int(C.E_GO_W21_ERROR_INVALID_W21_HANDLER)
}

func (w21 *W21Config)EnableValidator() (int) {
	w21.mu.Lock()
	defer w21.mu.Unlock()

	if (w21.cSoapPtr != nil) {
		return int(C.w21_enable_input_rules_validator(w21.cSoapPtr))
	}

	return int(C.E_GO_W21_ERROR_INVALID_W21_HANDLER)
}

func (w21 *W21Config)DisableValidator() (int) {
	w21.mu.Lock()
	defer w21.mu.Unlock()

	if (w21.cSoapPtr != nil) {
		C.w21_disable_input_rules_validator(w21.cSoapPtr)
		return 0
	}

	return int(C.E_GO_W21_ERROR_INVALID_W21_HANDLER)
}

func (w21 *W21Config)IsValidatorEnabled() (bool) {
	w21.mu.Lock()
	defer w21.mu.Unlock()

	if (w21.cSoapPtr != nil) {
		return C.go_is_validator_enabled(w21.cSoapPtr) != 0
	}

	return false // fail safe
}

func (w21 *W21Config)Parse() (int, []byte) {
	w21.mu.Lock()
	defer w21.mu.Unlock()

	if w21.cSoapPtr != nil {
		if w21.cBsonPtr != nil {
			// unsafe.Slice = ZERO COPY from C
			return 0, unsafe.Slice((*byte)(unsafe.Pointer(w21.cBsonPtr.bson)), int(w21.cBsonPtr.bson_size))
		}

		if w21.withStatistics {
			w21.errorStatParse = int(C.w21_hard_summary_parse_begin(w21.cSoapPtr))
		}

		var ret int

		if w21.cJsonPtr == nil {
			ret = int(C.bson_read_AutoDetect21(w21.cSoapPtr))
		}

		if ret == 0 {
			w21.cBsonPtr = C.w21_bson_serialize(w21.cSoapPtr)

			ret = int(C.go_get_w21_error(w21.cSoapPtr))
		}

		var cByteArray []byte
		if w21.cBsonPtr != nil {
			// unsafe.Slice = ZERO COPY from C
			cByteArray = unsafe.Slice((*byte)(unsafe.Pointer(w21.cBsonPtr.bson)), int(w21.cBsonPtr.bson_size))
		}

		if (w21.withStatistics && w21.errorStatParse == 0) {
			if (ret == 0) {
				w21.errorStatParse = int(C.w21_hard_summary_parse_end(w21.cSoapPtr))
			} else {
				w21.errorStatParse = ret
			}
		}

		return ret, cByteArray
	}

	return int(C.E_GO_W21_ERROR_INVALID_W21_HANDLER), nil
}

func (w21 *W21Config)ParseJson() (int, []byte) {
	w21.mu.Lock()
	defer w21.mu.Unlock()

	if w21.cSoapPtr != nil {
		if w21.cJsonPtr != nil {
			// unsafe.Slice = ZERO COPY from C
			return 0, unsafe.Slice((*byte)(unsafe.Pointer(w21.cJsonPtr.json)), int(w21.cJsonPtr.json_len))
		}

		if w21.withStatistics {
			w21.errorStatParseJson = int(C.w21_hard_summary_parse_json_begin(w21.cSoapPtr))
		}

		var ret int

		if w21.cBsonPtr == nil {
			ret = int(C.bson_read_AutoDetect21(w21.cSoapPtr))
		}

		if ret == 0 {
			w21.cJsonPtr = C.w21_get_json(w21.cSoapPtr)

			ret = int(C.go_get_w21_error(w21.cSoapPtr))
		}

		var cByteArray []byte
		// unsafe.Slice = ZERO COPY from C
		if (w21.cJsonPtr != nil) {
			cByteArray = unsafe.Slice((*byte)(unsafe.Pointer(w21.cJsonPtr.json)), int(w21.cJsonPtr.json_len))
		}

		if (w21.withStatistics && w21.errorStatParseJson == 0) {
			if (ret == 0) {
				w21.errorStatParseJson = int(C.w21_hard_summary_parse_json_end(w21.cSoapPtr))
			} else {
				w21.errorStatParseJson = ret
			}
		}

		return ret, cByteArray
	}

	return int(C.E_GO_W21_ERROR_INVALID_W21_HANDLER), nil
}

func (w21 *W21Config)GetStatistics() (int, *W21Statistics) {
	w21.mu.Lock()
	defer w21.mu.Unlock()

	if (!w21.withStatistics) {
		return int(C.E_GO_W21_ERROR_STATISTICS_DISABLED), nil
	}

	if w21.statistics != nil {
		return 0, w21.statistics
	}

	if w21.cSoapPtr != nil {
		if w21.cBsonPtr != nil || w21.cJsonPtr != nil {
			cStatistics	:= C.w21_get_statistics(w21.cSoapPtr)
			w21.statistics = &W21Statistics{
				costs: int(cStatistics.costs),
				strings: int(cStatistics.strings),
				shorts: int(cStatistics.shorts),
				ints: int(cStatistics.ints),
				long64s: int(cStatistics.long64s),
				enums: int(cStatistics.enums),
				arrays: int(cStatistics.arrays),
				booleans: int(cStatistics.booleans),
				doubles: int(cStatistics.doubles),
				date_times: int(cStatistics.date_times),
				event_types: int(cStatistics.event_types),
				measures: int(cStatistics.measures),
				total: int(cStatistics.total),
			}

			return 0, w21.statistics
		}

		return int(C.E_GO_W21_ERROR_BSON_OBJECT_NOT_PARSED_YET), nil
	}

	return int(C.E_GO_W21_ERROR_INVALID_W21_HANDLER), nil
}

func (w21 *W21Config)EnableStatistics() {
	w21.mu.Lock()
	defer w21.mu.Unlock()

	w21.withStatistics = true
}

func (w21 *W21Config)DisableStatistics() {
	w21.mu.Lock()
	defer w21.mu.Unlock()

	w21.withStatistics = false
}

func (w21 *W21Config)Recycle() {
	w21.mu.Lock()
	defer w21.mu.Unlock()

	if (w21.cSoapPtr != nil) {
		C.w21_recycle(w21.cSoapPtr)
	}

	w21.cBsonPtr = nil
	w21.cJsonPtr = nil

	w21.errorStatRead = 0
	w21.errorStatParse = 0
	w21.errorStatParseJson = 0

	w21.statistics = nil
}

func (w21 *W21Config)GetObjectName() (int, string) {
	w21.mu.Lock()
	defer w21.mu.Unlock()

	var ret *C.char
	if (w21.cSoapPtr != nil) {
		ret = C.w21_get_input_object_name(w21.cSoapPtr)
	}

	if (ret != nil) {
		return 0, C.GoString(ret)
	}

	return int(C.E_GO_W21_ERROR_UNABLE_TO_GET_OBJECT_NAME_STRING), ""

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

	status, bsonBytes := w21Conf.Parse()
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

	status, jsonBytes := w21Conf.ParseJson()
	if (status == 0) {
		fmt.Printf("STRING %s", string(jsonBytes))
	} else {
		fmt.Printf("Error @ JSON STRING %d", status)
	}

	a, ret := w21Conf.GetObjectName()
	if (a == 0) {
		fmt.Println("\n" + ret + "\n")
	} else {
		fmt.Println("Error %d", a)
	}

	_, stat := w21Conf.GetStatistics()
	fmt.Printf("\nStatistics %v\n", stat)
	fmt.Printf("\nStruct %v\n", w21Conf)
}
