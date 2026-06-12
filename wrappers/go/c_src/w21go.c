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
  return (soap)?w21_enable_input_rules_validator(soap):E_GO_W21_ERROR_INVALID_W21_HANDLER;
}

// 0 is OK, else (can not parse or already parsed). struct soap *soap MUST BE NOT NULL
int go_w21_can_parse(struct soap *soap)
{
  DECLARE_W21_CONFIG
  if (config->error == 0) {
    if (config->in_object) {
      if (config->in_bson == NULL)
        return 0;
      
      return E_GO_W21_ERROR_BSON_OBJECT_ALREADY_ALLOC;
    }
    
    return E_GO_W21_ERROR_STREAM_OR_FILE_NOT_PARSED_YET;  
  }

  return config->error;
}
