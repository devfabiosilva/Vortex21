#ifndef W21_INPUT_MACROS_H
 #define W21_INPUT_MACROS_H

#define IN_CW21RD_OBJECT_READ_FROM_STRING_BEGIN(obj) \
\
extern const WISML21_CONTRACT witsml21_contract_##obj; \
\
int cw21rd_##obj(struct soap *soap, const char *xml, size_t xml_len) \
{ \
\
  DECLARE_W21_CONFIG \
\
  if (config->error == 0) { \
    SET_W21_ERROR_ARGS_AND_EXIT( \
      config->in_object, \
      E_21_ERROR_IN_OBJECT_ALREADY_ALLOC, #obj, w21_get_object_name((config->object_type != W21_OBJECT_AutoDetect)?config->object_type:config->object_subtype) \
    ) \
\
    SET_W21_ERROR_ARGS_AND_EXIT( \
      config->in_bson, \
      E_W21_ERROR_BSON_ALREADY_ALLOC, #obj \
    ) \
\
    SET_W21_ERROR_ARGS_AND_EXIT( \
      (xml == NULL) || (xml_len == 0), \
      E_W21_NULL_OR_EMPTY_XML_STRING, #obj \
    ) \
\
    SET_W21_ERROR_ARGS_AND_EXIT( \
      (config->in_object = malloc(sizeof(struct cw21rd__##obj##Contract))) == NULL, \
      E_W21_ERROR_ALLOC_W21_OBJECT_STRUCT, #obj \
    ) \
\
    soap_default_cw21rd__##obj##Contract(soap, (struct cw21rd__##obj##Contract *)config->in_object); \
\
    soap->namespaces = namespaces; \
\
    config->object_type = W21_OBJECT_##obj; \
    /* config->object_subtype = W21_OBJECT_NONE; // on recycle will be set to W21_OBJECT_NONE */\
\
    soap->fsvalidate = w21_validate; \
    soap->frecv = w21_frecv_from_string; \
    /* config->finrootelement = NULL; // on recycle will be set to null */

#define IN_CW21RD_OBJECT_READ_FROM_STRING_END(obj) \
    memcpy((void *)&config->witsml21_contract, &witsml21_contract_##obj, sizeof(config->witsml21_contract)); \
\
    config->witsml21_contract.in_wistml21_xml = xml; \
    config->witsml21_contract.in_wistml21_xml_len = xml_len; \
\
    if (soap_begin_recv(soap) != SOAP_OK || soap_get_cw21rd__##obj##Contract(soap, (struct cw21rd__##obj##Contract *)config->in_object, NULL, NULL) == NULL || (soap_end_recv(soap) != SOAP_OK) || ((soap)->error) != SOAP_OK) { \
      free(config->in_object); /* Free failed object */ \
      config->in_object = NULL; \
\
      catch_gsoap_error(soap); /* Must be called before soap destroy */ \
\
      soap_destroy(soap); \
      soap_end(soap); \
\
    } \
\
  } \
\
  W21_RETURN \
}

#define IN_CW21RD_OBJECT_READ_FROM_STRING_BUILDER(obj) \
IN_CW21RD_OBJECT_READ_FROM_STRING_BEGIN(obj) \
IN_CW21RD_OBJECT_READ_FROM_STRING_END(obj)

#define IN_CW21RD_OBJECT_READ_FROM_FILE_BEGIN(obj) \
\
int cw21rd_##obj##_from_file(struct soap *soap, const char *xml_file) \
{ \
  DECLARE_W21_CONFIG \
\
  if (config->error == 0) { \
    SET_W21_ERROR_ARGS_AND_EXIT( \
      config->in_object, \
      E_21_ERROR_IN_OBJECT_ALREADY_ALLOC, #obj, w21_get_object_name((config->object_type != W21_OBJECT_AutoDetect)?config->object_type:config->object_subtype) \
    ) \
\
    SET_W21_ERROR_ARGS_AND_EXIT( \
      config->in_bson, \
      E_W21_ERROR_BSON_ALREADY_ALLOC, #obj \
    ) \
\
    SET_W21_ERROR_ARGS_AND_EXIT( \
      (xml_file == NULL) || (xml_file[0] == 0), \
      E_W21_REQUIRE_PATH_AND_FILENAME, #obj \
    ) \
\
    SET_W21_ERROR_ARGS_AND_EXIT( \
      (config->file = fopen(xml_file, "r")) == NULL, \
      E_W21_ERROR_OPENING_FILE, w21_message_sanitize(soap, (char *)xml_file) \
    ) \
\
    SET_W21_ERROR_ARGS_AND_EXIT( \
      (config->in_object = malloc(sizeof(struct cw21rd__##obj##Contract))) == NULL, \
      E_W21_ERROR_ALLOC_W21_OBJECT_STRUCT, #obj \
    ) \
\
    soap_default_cw21rd__##obj##Contract(soap, (struct cw21rd__##obj##Contract *)config->in_object); \
\
    config->in_filename = xml_file; \
\
    soap->namespaces = namespaces; \
\
    config->object_type = W21_OBJECT_##obj; \
    /* config->object_subtype = W21_OBJECT_NONE; // on recycle will be set to W21_OBJECT_NONE */\
\
    soap->fsvalidate = w21_validate; \
    soap->frecv = w21_frecv_from_file; \
    /* config->finrootelement = NULL; // on recycle will be set to null */

#define IN_CW21RD_OBJECT_READ_FROM_FILE_END(obj) \
    memcpy((void *)&config->witsml21_contract, &witsml21_contract_##obj, sizeof(config->witsml21_contract)); \
\
    if (soap_begin_recv(soap) != SOAP_OK || soap_get_cw21rd__##obj##Contract(soap, (struct cw21rd__##obj##Contract *)config->in_object, NULL, NULL) == NULL || (soap_end_recv(soap) != SOAP_OK) || ((soap)->error) != SOAP_OK) { \
      free(config->in_object); /* Free failed object */ \
      config->in_object = NULL; \
\
      catch_gsoap_error(soap); /* Must be called before soap destroy */ \
\
      soap_destroy(soap); \
      soap_end(soap); \
\
    } \
\
    fclose(config->file); \
    config->file = NULL; \
  } \
\
  W21_RETURN \
}

#define IN_CW21RD_OBJECT_READ_FROM_FILE_BUILDER(obj) \
IN_CW21RD_OBJECT_READ_FROM_FILE_BEGIN(obj) \
IN_CW21RD_OBJECT_READ_FROM_FILE_END(obj)

#endif