#include <w21go.h>
#include <w21_validator.h>
#include <go_w21_errors.h>

int go_w21_config_new(struct soap **soap, uint64_t in_config, uint64_t out_config)
{
  if ((*soap) == NULL) {
    if (out_config == 0) {
      if (((~(SOAP_XML_STRICT|SOAP_XML_IGNORENS)) & in_config) == 0)
        return w21_config_new(soap, SOAP_C_UTFSTRING|in_config, SOAP_IO_BUFFER|SOAP_XML_NIL|out_config);

      return E_GO_W21_ERROR_INVALID_INPUT_OPTION;
    }

    return E_GO_W21_ERROR_INVALID_NOT_IMPLEMENTED_YET;
  }

  return E_GO_W21_ERROR_INVALID_ALREADY_INITIALIZED;
}

int go_get_w21_error(struct soap *soap)
{
  if (soap != NULL) {
    DECLARE_W21_CONFIG
    if (config)
    	return config->error;
  }

  return E_GO_W21_ERROR_INVALID_ERROR_HANDLER;
}

int go_w21_enable_input_rules_validator(struct soap *soap)
{
  if (soap)
    return w21_enable_input_rules_validator(soap);

  return E_GO_W21_ERROR_INVALID_W21_HANDLER;
}

