package main

//go mod init parser_test
//go build -asan -o parser_test witsml21.go
//-fsanitize=address,leak
//nm -D libw21parser.so | grep " T " | wc -l

/*
#cgo CFLAGS: -I${SRCDIR}/../.. -I${SRCDIR}/../../core/include -fsanitize=address,leak
#cgo LDFLAGS: -L${SRCDIR}/c_src/lib -lw21go
#include "w21_config.h"
*/
import "C"
import (
	"fmt"
	"unsafe"
)

type W21Config struct {
	cSoapPtr *C.struct_soap
}

func W21ConfigNew(inOptions int, outOptions int) (*W21Config, error) {
	var nativeSoap *C.struct_soap
	status := C.w21_config_new(
		(**C.struct_soap)(unsafe.Pointer(&nativeSoap)),
		C.uint64_t(inOptions),
		C.uint64_t(outOptions),
	)

	if status == 0 {
		return &W21Config{cSoapPtr: nativeSoap}, nil
	}

	return nil, fmt.Errorf("Unable to load config: parser status %d", status)
}

func main() {
	w21Conf, err := W21ConfigNew(0, 0)
	if err != nil {
		fmt.Println("Err:", err)
		return
	}

	defer func() {
		C.w21_config_free((**C.struct_soap)(unsafe.Pointer(&w21Conf.cSoapPtr)))
	}()

	// TODO: implement it
	fmt.Println("Loaded!")
}
