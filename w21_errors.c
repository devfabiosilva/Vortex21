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
    SET_W21_ERROR_DETAIL(E_W21_ERROR_ALLOC_W21_OBJECT_STRUCT, "Unable to alloc memory to WITSML 2.1 object")
    SET_W21_ERROR_DETAIL(E_21_ERROR_IN_OBJECT_ALREADY_ALLOC, "WITSML 2.1 object already alloc'd")
    SET_W21_ERROR_DETAIL(E_W21_ERROR_CREATE_IN_BSON_OBJECT, "Input mode: Error on allocate BSON object. Unable to parse WITSML 2.1 object")
    SET_W21_ERROR_DETAIL(E_W21_ERROR_OBJECT_NULL_OR_PARSER_ERROR, "Input mode: WITSML 2.1 object is NULL or parsing error")
    SET_W21_ERROR_DETAIL(E_W21_ERROR_SET_UTF8_STRING_IN_BSON, "Unable to set UTF-8 string in BSON")
    SET_W21_ERROR_DETAIL(E_W21_ERROR_BEGIN_BSON_ROOT_OBJECT, "Input mode: Unable to begin BSON root object")
    SET_W21_ERROR_DETAIL(E_W21_ERROR_END_BSON_ROOT_OBJECT, "Input mode: Unable to end BSON root object")
    SET_W21_ERROR_DETAIL(E_W21_ERROR_COULD_NOT_BUILD_UTF8_STRING_ARRAY, "Input mode: Unable to build UTF-8 string array")
    SET_W21_ERROR_DETAIL(E_W21_ERROR_COULD_NOT_ADD_UTF8_STRING_IN_ARRAY, "Input mode: Could not add UTF-8 string into an array")
    SET_W21_ERROR_DETAIL(E_W21_ERROR_UTF8_STRING_IN_ARRAY_NULL, "Input mode: NULL UTF-8. Could not add in array")
    SET_W21_ERROR_DETAIL(E_W21_ERROR_UTF8_STRING_END_ARRAY, "In: array of UTF-8 string could be not finished")
    SET_W21_ERROR_DETAIL(E_W21_ERROR_BSON_ALREADY_ALLOC, "Input: BSON object is already alloc'd")
    SET_W21_ERROR_DETAIL(E_W21_ERROR_REFERENCE_IN_BSON_OBJECT, "In: Could not reference BSON object")
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
