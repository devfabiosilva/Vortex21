#include <w21_cserrors.h>
#include <w21_config.h>

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
