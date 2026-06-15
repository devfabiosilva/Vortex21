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
  DECLARE_W21_CONFIG
  return config->error;
}

uint64_t go_w21_is_validator_enabled(struct soap *soap)
{
  DECLARE_W21_CONFIG
  return config->in_config & ENABLE_REGEX_VALIDATOR;
}

bool go_w21_has_witsml21(struct soap *soap)
{
  DECLARE_W21_CONFIG
  return config->in_object != NULL;
}

struct hard_stat_t *go_w21_get_stat_phases(struct soap *soap)
{
  DECLARE_W21_CONFIG
  return &config->hardware_statistics;
}
