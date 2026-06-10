package main

/*
#include <stdio.h>

void hello_from_c() {
    printf("Hello from the C world!\n");
}
*/
import "C"

func main() {
    // Call the C function via the pseudo-package
    C.hello_from_c()
}

