#include <w21_config.h>
#include <w21_messages.h>
#include <w21_errors.h>

// 0 success otherwise error. On soapfault return NOT ZERO is needed
// NOTE it is only used in AUTO DETECT MODE. Other mode config->finrootelement MUST BE NULL
// This function callback detects ROOT WITSML 21 object elements on AutoDetect mode
int froot_element_detect(struct soap *soap, int type)
{

  if (soap->level != 1)
    return 0;

  DECLARE_W21_CONFIG

  if (config->object_subtype == W21_OBJECT_NONE) {
    config->object_subtype = (enum w21_object_e)type;
    return 0;
  }

  soap_receiver_fault_subcode(
    soap, 
    "Auto detect WITSML 21 object error",
    "Repeated or invalid WITSML 2.1 object",
    w21_message_sanitize_fmt(
      soap, "Previous detected type (%d) %s. Current type (%d) %s", 
      config->object_subtype, w21_get_object_name(config->object_subtype),
      type, w21_get_object_name(type)
    )
  );

  return -1;
}

#define CONTRACT_COPY_BEGIN(func) \
  DECLARE_W21_CONFIG \
\
  ssize_t copied = 0; \
  ssize_t left = (ssize_t)sz; \
  ssize_t index = (ssize_t)config->witsml21_contract.contract_begin_copied; \
  ssize_t to_be_copied = config->witsml21_contract.contract_begin_len - index; \
\
  if (to_be_copied > 0) { \
    if (left < to_be_copied) \
      to_be_copied = left; \
\
    copied += to_be_copied; \
    left -= to_be_copied; \
\
    if (left < 0) /* Guard. Avoid noise */ \
      goto func##_fault; \
\
    memcpy(buf, &config->witsml21_contract.contract_begin[(size_t)index], to_be_copied); \
    config->witsml21_contract.contract_begin_copied += to_be_copied; \
\
    if (left == 0) \
      return copied; \
\
    buf = &buf[(size_t)to_be_copied]; \
  }

#define CONTRACT_COPY_END(func) \
  index = (ssize_t)config->witsml21_contract.contract_end_copied; \
  to_be_copied = config->witsml21_contract.contract_end_len - index; \
\
  if (to_be_copied > 0) { \
    if (left < to_be_copied) \
      to_be_copied = left; \
\
    copied += to_be_copied; \
    left -= to_be_copied; \
\
    if (left < 0) /* Guard. Avoid noise */ \
      goto func##_fault; \
\
    memcpy(buf, &config->witsml21_contract.contract_end[(size_t)index], to_be_copied); \
    config->witsml21_contract.contract_end_copied += to_be_copied; \
\
  } \
\
  return (size_t)copied; \
\
func##_fault:

size_t w21_frecv_from_string(struct soap *soap, char *buf, size_t sz)
{
  CONTRACT_COPY_BEGIN(w21_frecv_from_string)

  ////////
  index = (ssize_t)config->witsml21_contract.in_wistml21_xml_copied;
  to_be_copied = config->witsml21_contract.in_wistml21_xml_len - index;

  if (to_be_copied > 0) {
    if (left < to_be_copied)
      to_be_copied = left;

    copied += to_be_copied;
    left -= to_be_copied;

    if (left < 0) // Guard. Avoid noise
      goto w21_frecv_from_string_fault;

    memcpy((void *)buf, (void *)&config->witsml21_contract.in_wistml21_xml[index], (size_t)to_be_copied);

    config->witsml21_contract.in_wistml21_xml_copied += to_be_copied;

    if (left == 0)
      return copied;

    buf = &buf[(size_t)to_be_copied];
  }
  ////////

  CONTRACT_COPY_END(w21_frecv_from_string)

  soap_receiver_fault_subcode(soap, 
    "Read XML string error",
    "Contract buffer overflow in C function w21_frecv_from_string()",
    w21_message_sanitize_fmt(
      soap, "Trying to reading string with object \"%s\" exceeds buffer in %lld bytes. Aborting",
      config->witsml21_contract.object_name, (long long int)(-left)
    )
  );

  return 0;
}

size_t w21_frecv_from_file(struct soap *soap, char *buf, size_t sz)
{

  CONTRACT_COPY_BEGIN(w21_frecv_from_file)

  /////
  if (!config->in_file_stream_done) {
  //fread guarantes that "to_be_copied" <= "left"
    to_be_copied = (ssize_t)fread((void *)buf, 1, (size_t)left, config->file);
    copied += to_be_copied;

    left -= to_be_copied;

    config->in_file_stream_done = (to_be_copied == 0);

    if (left == 0)
      return copied;

    if (left < 0) // Guard. Avoid noise
      goto w21_frecv_from_file_fault;

    buf = &buf[(size_t)to_be_copied];

  }
  ////

  CONTRACT_COPY_END(w21_frecv_from_file)

  soap_receiver_fault_subcode(soap, 
    "Read XML file stream error",
    "Contract buffer overflow in C function w21_frecv_from_file()",
    w21_message_sanitize_fmt(
      soap, "Trying to reading file \"%s\" with object \"%s\" exceeds buffer in %lld bytes. Aborting",
      config->in_filename, config->witsml21_contract.object_name, (long long int)(-left)
    )
  );

  return 0;
}


int w21_validate(struct soap *soap, const char *pattern, const char *value)
{

  DECLARE_W21_CONFIG
/*
  if (config->error)
    return SOAP_FAULT; // Does not overlap last error on event
*/
  if (config->in_config & ENABLE_REGEX_VALIDATOR) {
    if (config->current_validation) {
      if (config->current_validation->w21_validator(soap, (char *)value, config->current_validation->type))
        return SOAP_OK;

      return SOAP_PATTERN;
    }

    set_w21_error_message(
      soap, E_21_ERROR_ENABLE_INCONSISTENT_VALIDATOR_LIST_INITIALIZATION,
      "Unable to check WITSML 2.1 parameter regex, due to misuse of regex validator"
    );

    return SOAP_FAULT;
  }

  return SOAP_OK;

/*
  if (!pattern) {
    ++validated_objects;
    return SOAP_OK;
  }

  if (is_w21_regex_valid(value)) {
    ++validated_objects;
    return SOAP_OK;
  }

  snprintf(infoMessageDetail, sizeof(infoMessageDetail), "<w21_error>%s</w21_error>", infoMessage);
  soap_receiver_fault_subcode(soap, "WITSML2.1 parser", infoMessage, infoMessageDetail);

  return SOAP_FAULT;
*/
//SOAP_PATTERN
  ////SOAP_TYPE error according to https://www.genivia.com/doc/guide/html/group__group__callbacks.html#ga6a5e29b860d28f855c8d9a67c096d311;
  //return SOAP_OK;
}

int w21_fsend(struct soap *soap, const char *buf, size_t sz)
{
  if ((buf != NULL) && (sz > 0)) {
    printf("\nPRINT %.*s\n", (int)sz, buf);
  } else {
    printf("\nerr buffer send");
  }
  return SOAP_OK;
}

#undef CONTRACT_COPY_END
#undef CONTRACT_COPY_BEGIN
