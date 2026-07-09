#include <w21_cserrors.h>
#include <w21_config.h>
#include <w21_errors.h>

#define SET_CS_ERROR_LIST(tag, message) \
    { tag, #tag, message },

static W21_ERROR_DETAIL cs_w21_err_list[] = {
    SET_CS_ERROR_LIST(E_CS_W21_ERROR_INVALID_INPUT_OPTION, "Unable to initialize. Check if input option is valid")
    SET_CS_ERROR_LIST(E_CS_W21_ERROR_INVALID_NOT_IMPLEMENTED_YET, "Output option: Feature not implemented yet")
    SET_CS_ERROR_LIST(E_CS_W21_INSTANCE_ALREADY_INITIALIZED, "C WITSML 2.1 instance is already initialized in .NET class")
    {0, NULL, NULL}
};

static W21_ERROR_DETAIL cs_w21_error_UNKNOWN = {
    E_CS_W21_UNKNOWN_NATIVE_ERROR,
    "E_CS_W21_UNKNOWN_NATIVE_ERROR",
    "An unmapped or unknown native error occurred in the core engine"
};

#undef SET_CS_ERROR_LIST

int cs_w21_config_new(struct soap **soap, uint64_t in_config, uint64_t out_config)
{
    if ((*soap) == NULL) {
        if (out_config == 0) {
            if (((~(SOAP_XML_STRICT|SOAP_XML_IGNORENS)) & in_config) == 0)
                return w21_config_new(soap, SOAP_C_UTFSTRING|in_config, SOAP_IO_BUFFER|SOAP_XML_NIL|out_config);

            return E_CS_W21_ERROR_INVALID_INPUT_OPTION;
        }

        return E_CS_W21_ERROR_INVALID_NOT_IMPLEMENTED_YET;
    }

    return E_CS_W21_INSTANCE_ALREADY_INITIALIZED;
}

uint64_t cs_w21_get_xml_strict()
{
    return (uint64_t)SOAP_XML_STRICT;
}

uint64_t cs_w21_get_xml_ignorens()
{
    return (uint64_t)SOAP_XML_IGNORENS;
}

W21_ERROR_DETAIL *cs_w21_get_error_detail(int err)
{
    W21_ERROR_DETAIL *list = &cs_w21_err_list[0];

    while (list->tag) {
        if (list->err != err)
            list++;
        else
            return list;
    }

    list = w21_get_error_detail(err);

    return (list)?list:(&cs_w21_error_UNKNOWN);
}
