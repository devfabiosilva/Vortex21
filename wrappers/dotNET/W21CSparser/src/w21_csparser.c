#include <w21_cserrors.h>
#include <w21_config.h>
#include <w21_errors.h>

#define SET_CS_ERROR_LIST(tag, message) \
    { tag, #tag, message },

static W21_ERROR_DETAIL cs_w21_err_list[] = {
    SET_CS_ERROR_LIST(E_CS_W21_ERROR_INVALID_INPUT_OPTION, "Unable to initialize. Check if input option is valid")
    SET_CS_ERROR_LIST(E_CS_W21_ERROR_INVALID_NOT_IMPLEMENTED_YET, "Output option: Feature not implemented yet. Set out_options = 0")
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

char *cs_w21_get_fault_string(struct soap *soap)
{
  DECLARE_W21_CONFIG
  return &config->detail_message[0];
}

char *cs_w21_get_fault_string_xml(struct soap *soap)
{
  DECLARE_W21_CONFIG
  return &config->detail_message_xml[0];
}

uint32_t cs_w21_get_fault_error(struct soap *soap)
{
    DECLARE_W21_CONFIG
    return (uint32_t)config->error;
}

int32_t cs_w21_get_bson_ser(struct soap *soap, uint8_t **bson)
{
    int32_t res;
    struct c_bson_serialized_t *bson_ser = w21_bson_serialize(soap);
    if (bson_ser != NULL) {
        *bson = bson_ser->bson;
        res = (int32_t)bson_ser->bson_size;
    } else {
        *bson = NULL;
        res = -1;
    }

    return res;
}

const char *cs_w21_get_input_object_name(struct soap *soap)
{
    const char *res = w21_get_input_object_name(soap);

    if (res)
        return res;

    return "Object not parsed yet or unknown object";
}

const char *cs_w21_version_str()
{
    return w21_version_str(NULL);
}

const char *cs_w21_build_date_str()
{
    return w21_build_date_str(NULL);
}

int cs_w21_hard_summary_read_end(
    struct soap *soap,
    uint64_t *in_total_cycles,
    uint64_t *in_total_nanos,
    uint64_t *in_mem_delta
)
{
    int err = w21_hard_summary_read_end(soap);

    DECLARE_W21_CONFIG

    if (err == 0) {
        *in_total_cycles = config->hardware_statistics.in_total_cycles;
        *in_total_nanos = config->hardware_statistics.in_total_nanos;
        *in_mem_delta = config->hardware_statistics.in_mem_delta;
    } else {
        *in_total_cycles = 0;
        *in_total_nanos = 0;
        *in_mem_delta = 0;
    }

    return err;
}

int cs_w21_hard_summary_parse_end(
    struct soap *soap,
    uint64_t *in_total_cycles,
    uint64_t *in_total_nanos,
    uint64_t *in_mem_delta
)
{
    int err = w21_hard_summary_parse_end(soap);

    DECLARE_W21_CONFIG

    if (err == 0) {
        *in_total_cycles = config->hardware_statistics.in_parse_total_cycles;
        *in_total_nanos = config->hardware_statistics.in_parse_total_nanos;
        *in_mem_delta = config->hardware_statistics.in_parse_mem_delta;
    } else {
        *in_total_cycles = 0;
        *in_total_nanos = 0;
        *in_mem_delta = 0;
    }

    return err;
}

/*
int cs_w21_hard_summary_parse_json_end(
    struct soap *soap,
    uint64_t *in_total_cycles,
    uint64_t *in_total_nanos,
    uint64_t *in_mem_delta
)
{
    int err = w21_hard_summary_parse_json_end(soap);

    DECLARE_W21_CONFIG

    if (err == 0) {
        *in_total_cycles = config->hardware_statistics.in_parse_json_total_cycles;
        *in_total_nanos = config->hardware_statistics.in_parse_json_total_nanos;
        *in_mem_delta = config->hardware_statistics.in_parse_json_mem_delta;
    } else {
        *in_total_cycles = 0;
        *in_total_nanos = 0;
        *in_mem_delta = 0;
    }

    return err;
}
*/