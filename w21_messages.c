#include <w21_config.h>
#include <w21_messages.h>
#include <w21_errors.h>
#include <stdarg.h>

#define CW21_ERROR_MESSAGE_DETAILED "<WITSML21_ERROR code=%d>%.*s</WITSML21_ERROR>"

void set_w21_error_message(struct soap *soap, int error, char *fmt, ...)
{
  DECLARE_W21_CONFIG
  va_list args;

  if (config->error)
    return; // Error already exists

  config->error = error;

  va_start(args, fmt);
  config->detail_message_len = vsnprintf(config->detail_message, DEFAULT_DETAIL_MESSAGE_LEN, fmt, args);
  va_end(args);

  if (config->detail_message_len > DEFAULT_DETAIL_MESSAGE_LEN) {
    config->detail_message[DEFAULT_DETAIL_MESSAGE_LEN] = 0;
    config->detail_message_len = DEFAULT_DETAIL_MESSAGE_LEN;
  }

  config->detail_message_xml_len = snprintf(
    config->detail_message_xml, DEFAULT_DETAIL_MESSAGE_XML_LEN,
    CW21_ERROR_MESSAGE_DETAILED,
    error, (int)config->detail_message_len, config->detail_message
  );

  if (config->detail_message_xml_len > DEFAULT_DETAIL_MESSAGE_XML_LEN) {
    config->detail_message_xml[DEFAULT_DETAIL_MESSAGE_XML_LEN] = 0;
    config->detail_message_xml_len = DEFAULT_DETAIL_MESSAGE_XML_LEN;
  }
}

#define W21_MESG_SANITIZE \
  if (len >= W21_MESSAGE_SANITIZE_SIZE) \
    strcpy(&soap->tmpbuf[W21_MESSAGE_SANITIZE_SIZE - sizeof(STR_CONTINUE)], STR_CONTINUE); \
\
  return soap->tmpbuf;

#define STR_CONTINUE " ..."
_Static_assert(SOAP_TMPLEN > W21_MESSAGE_SANITIZE_SIZE, "W21_MESSAGE_SANITIZE_SIZE too long");
_Static_assert(W21_MESSAGE_SANITIZE_SIZE > sizeof(STR_CONTINUE), "Inconsistent W21_MESSAGE_SANITIZE_SIZE size");

char *w21_message_sanitize(struct soap *soap, char *message)
{
  int len = snprintf(soap->tmpbuf, W21_MESSAGE_SANITIZE_SIZE, "\"%s\"", message);

  W21_MESG_SANITIZE
}

char *w21_message_sanitize_fmt(struct soap *soap, char *fmt, ...)
{
  va_list args;

  va_start(args, fmt);
  int len = vsnprintf(soap->tmpbuf, W21_MESSAGE_SANITIZE_SIZE, fmt, args);
  va_end(args);

  W21_MESG_SANITIZE
}

#undef STR_CONTINUE
#undef W21_MESG_SANITIZE

// WARNING. It must be called on return Soap Error only
void catch_gsoap_error(struct soap *soap)
{
  // RULE: if config->error != 0 there a message in detail_message
  // Else it must be set with detailed gSoap description
  DECLARE_W21_CONFIG

  if (config->error)
    return;

  int err = soap->error;

  if ((soap->state != 1) && (soap->state != 2))
    set_w21_error_message(soap, E_W21_ERROR_MESSAGE_NOT_INITIALIZED, "Error on Witsml21 error message: soap struct state not initialized");
  else if (err) {
    // Block from soap_print_fault in stdsoap2.c
    const char **c, *v = NULL, *s, *d;
    c = soap_faultcode(soap);
    if (!*c)
    {
      soap_set_fault(soap);
      c = soap_faultcode(soap);
    }
    if (soap->version == 2)
      v = soap_fault_subcode(soap);
    s = soap_fault_string(soap);
    d = soap_fault_detail(soap);
    set_w21_error_message(soap, err, "%s%d fault %s [%s] \"%s\" Detail: %s", soap->version ? "SOAP 1." : "Error ", soap->version ? (int)soap->version : err, *c, v ? v : "no subcode", s ? s : "[no reason]", d ? d : "[no detail]");
  } else
    set_w21_error_message(soap, E_W21_UNABLE_TO_CATCH_GSOAP_ERROR, "W21: Unknown gsoap error code");
}