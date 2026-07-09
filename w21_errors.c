#include <w21_errors.h>
#include <stddef.h>

#define SET_W21_ERROR_DETAIL(tag, descr) \
    { tag, #tag, descr },

static W21_ERROR_DETAIL w21_error_list[] = 
{
    SET_W21_ERROR_DETAIL(E_W21_ERROR_MESSAGE_NOT_INITIALIZED, "Message error handle was not initialized correctly")
    SET_W21_ERROR_DETAIL(E_W21_UNABLE_TO_CATCH_GSOAP_ERROR, "gSoap message was not loaded into WITSML 2.1 parser")
    SET_W21_ERROR_DETAIL(E_W21_UNABLE_TO_CREATE_CONFIG, "Unable to initialize config. Allocate not succeed. Check memory")
    SET_W21_ERROR_DETAIL(E_W21_UNABLE_TO_ALLOC_GSOAP_INSTANCE, "Could not initialize gSoap instance")
    SET_W21_ERROR_DETAIL(E_W21_INVALID_REFERENCE_POINTER, "C WITSML 2.1 pointer is invalid")
    SET_W21_ERROR_DETAIL(E_W21_CONFIG_ALREADY_ALLOCD, "Pointer has alread alloc'd object")
    SET_W21_ERROR_DETAIL(E_W21_REQUIRE_PATH_AND_FILENAME, "Input Witsml 2.1: Is require path and file name")
    SET_W21_ERROR_DETAIL(E_W21_ERROR_OPENING_FILE, "Input WITSML 2.1 document: Could not open document")
    // TODO Add error tag and description to allow human understand what 
    // is happening without undestand parser structure
    {0, NULL, NULL}
};
#undef SET_W21_ERROR_DETAIL

W21_ERROR_DETAIL *w21_get_error_detail(int error)
{
    W21_ERROR_DETAIL *list = &w21_error_list[0];

    while (list->tag) {
        if (list->err != error)
            list++;
        else
            return list;
    }

    return NULL;
}
