#include <cws_bson_utils.h>
#include <cws_errors.h>

inline
void cws_bson_free(bson_t **bson)
{
  bson_destroy(*bson); // Does nothing if *bson is NULL. See http://mongoc.org/libbson/current/bson_destroy.html
  *bson=NULL;
}

char *cws_data_to_json(size_t *str_len, const uint8_t *data, size_t data_size)
{
  char *str;
  bson_t *bson=bson_new_from_data(data, data_size);

  if (!bson)
    return NULL;

  str=bson_as_relaxed_extended_json((const bson_t *)bson, str_len);

  bson_destroy(bson);

  return str;
}

//bson_t *
//bson_new_from_json (const uint8_t *data, ssize_t len, bson_error_t *error);

int json_to_bson(bson_t **bson, const char *json, ssize_t json_len, char *errMsg, size_t errMsgSize)
{
  int err;
  bson_error_t bson_error;

#define JSON_NULL_OR_EMPTY_ERR (int)-2
#define BSON_NEW_ERR (int)-1

  if ((json!=NULL)&&(json_len!=0))
    err=((*bson=bson_new_from_json((const uint8_t *)json, json_len, &bson_error)))?0:BSON_NEW_ERR;
  else {
    err=JSON_NULL_OR_EMPTY_ERR;
    *bson=NULL;
  }

  if (errMsg) {
    if (!err)
      errMsg[0]=0;
    else if (err==BSON_NEW_ERR)
      snprintf(errMsg, errMsgSize, "Json to Bson ERROR: %d.%d with message \"%s\"", bson_error.domain, bson_error.code, bson_error.message);
    else
      snprintf(errMsg, errMsgSize, "Json parse error NULL or empty. Parsed json(%p) with length = %ld", json, json_len);
  }

#undef BSON_NEW_ERR
#undef JSON_NULL_OR_EMPTY_ERR
  return err;
}
/*
int bson_serialize(uint8_t **buf, size_t *bufsize, const char *value, int value_len, bson_t *bson)
{
  int err;
  bson_writer_t *writer;
  bson_t *doc;

  (*buf)=NULL;
  (*bufsize)=0;
//http://mongoc.org/libbson/current/bson_writer_t.html
  if (!(writer=bson_writer_new(buf, bufsize, 0, bson_realloc_ctx, NULL))) {
    (*buf)=NULL;
    (*bufsize)=0;
    return -1;
  }

  if (!bson_writer_begin(writer, &doc)) {
    err=-2;
    goto bson_serialize_resume;
  }

  err=(bson_append_document(doc, value, value_len, (const bson_t *)bson))?0:-3;

  bson_writer_end(writer);

  if (err==0)
    (*bufsize)=bson_writer_get_length(writer);
  else {
bson_serialize_resume:
    bson_free((void *)*buf);
    (*buf)=NULL;
    (*bufsize)=0;
  }

  bson_writer_destroy(writer);
  return err;
}
*/
int bson_serialize_copy(uint8_t **buf, size_t *bufsize, bson_t *bson)
{
  int err;
  bson_writer_t *writer;
  bson_t *doc;

  (*buf)=NULL;
  (*bufsize)=0;

  if (!(writer=bson_writer_new(buf, bufsize, 0, bson_realloc_ctx, NULL))) {
    (*buf)=NULL;
    (*bufsize)=0;
    return -21;
  }

  if (!bson_writer_begin(writer, &doc)) {
    err=-22;
    goto bson_serialize_copy_resume;
  }

  err=(bson_concat(doc, (const bson_t *)bson))?0:-23;

  bson_writer_end(writer);

  if (err==0)
    (*bufsize)=bson_writer_get_length(writer);
  else {
bson_serialize_copy_resume:
    bson_free((void *)*buf);
    (*buf)=NULL;
    (*bufsize)=0;
  }

  bson_writer_destroy(writer);
  return err;
}

int bson_serialize(uint8_t **buf, size_t *bufsize, bson_t *bson)
{
  int err;
  bson_writer_t *writer;
  bson_t *doc;

  (*buf)=NULL;
  (*bufsize)=0;
//http://mongoc.org/libbson/current/bson_writer_t.html
  if (!(writer=bson_writer_new(buf, bufsize, 0, bson_realloc_ctx, NULL))) {
    (*buf)=NULL;
    (*bufsize)=0;
    return -1;
  }

  if (!bson_writer_begin(writer, &doc)) {
    err=-2;
    goto bson_serialize_resume;
  }

  err=(bson_concat(doc, (const bson_t *)bson))?0:-3;

  bson_writer_end(writer);

  if (err==0)
    (*bufsize)=bson_writer_get_length(writer);
  else {
bson_serialize_resume:
    bson_free((void *)*buf);
    (*buf)=NULL;
    (*bufsize)=0;
  }

  bson_writer_destroy(writer);
  return err;
}

/*
bson_destroy (bson_t *bson);
bool
bson_init_from_json (bson_t *bson,
                     const char *data,
                     ssize_t len,
                     bson_error_t *error);*/
//int json_to_bson(bson_t **bson, const char *json, ssize_t json_len, char *errMsg, size_t errMsgSize)
//int bson_serialize_copy(uint8_t **buf, size_t *bufsize, bson_t *bson)
int json_to_bson_serialized(
  uint8_t **bson, size_t *bson_size,
  const char *json, ssize_t json_len,
  char *errMsg, size_t errMsgSize
)
{
  int err;
  bson_t *bson_tmp;

  *bson=NULL;
  *bson_size=0;

  if ((err=json_to_bson(&bson_tmp, json, json_len, errMsg, errMsgSize)))
    return err;

  err=bson_serialize_copy(bson, bson_size, bson_tmp);

  if (errMsg) {
    if (!err)
      errMsg[0]=0;
    else
      snprintf(errMsg, errMsgSize, "Json to bson serialize error %d. Could not copy %p", err, bson_tmp);
  }

  bson_destroy(bson_tmp);

  return err;
}

int bson_put_single_attribute_required(bson_t *bson, const char *key, int key_length, char *value, int value_length)
{

  bson_t childAttributes;

  if (value) {
    if (!bson_append_document_begin(bson, KEY_USCORE_ATTRIBUTES, &childAttributes))
      return E_CWS_ERROR_APPEND_SINGLE_ATRIBUTE_REQUIRED_BEGIN;

    int err = 0;

    if (!bson_append_utf8(&childAttributes, key, key_length, (const char *)value, value_length))
      err = E_CWS_ERROR_APPEND_SINGLE_ATRIBUTE_REQUIRED_UTF8;

    if (!bson_append_document_end(bson, &childAttributes) && (err == 0))
        err = E_CWS_ERROR_APPEND_SINGLE_ATRIBUTE_REQUIRED_END;

    return err;
  }

  return E_CWS_ERROR_APPEND_SINGLE_ATRIBUTE_NULL;
}

int bson_put_multiple_attributes_if_they_exist(bson_t *bson, ...)
{
  int err = 0;
  const char *key;
  size_t key_length;
  char *value;
  va_list args;
  bson_t
    *childAttributesPtr=NULL,
    childAttributes;

  va_start(args, bson);
  while ((key=(const char *)va_arg(args, const char *))) {
    key_length=(size_t)va_arg(args, size_t);
    if ((value=(char *)va_arg(args, char *))) {

      if (childAttributesPtr) {

bson_put_multiple_attributes_if_they_exist_add:
        if (!bson_append_utf8(childAttributesPtr, key, key_length, value, -1)) {
          err = E_CWS_ERROR_APPEND_UTF8_MULTIPLE_ATTRIBUTES;
          break;
        }

      } else if (bson_append_document_begin(bson, KEY_USCORE_ATTRIBUTES, childAttributesPtr = &childAttributes))
        goto bson_put_multiple_attributes_if_they_exist_add;
      else {
        va_end(args);
        return E_CWS_ERROR_APPEND_MULTIPLE_ATTRIBUTE_BEGIN_DOCUMENT;
      }
    }
  }

  if ((childAttributesPtr != NULL) && (!bson_append_document_end(bson, childAttributesPtr)) && (err == 0))
    err = E_CWS_ERROR_APPEND_MULTIPLE_ATTRIBUTE_END_DOCUMENT;

  va_end(args);

  return err;
}

int bson_put_single_attribute_if_exists(bson_t *bson, const char *key, int key_length, char *value, int value_length)
{
  bson_t childAttributes;

  if (value) {
    if (!bson_append_document_begin(bson, KEY_USCORE_ATTRIBUTES, &childAttributes))
      return E_CWS_ERROR_BEGIN_APPEND_SINGLE_ATTRIBUTE;

    int err = 0;

    if (!bson_append_utf8(&childAttributes, key, key_length, (const char *)value, value_length))
      err = E_CWS_ERROR_APPEND_UTF8_IN_SINGLE_ATTRIBUTE;

    if (bson_append_document_end(bson, &childAttributes))
      return err;

    return E_CWS_ERROR_END_APPEND_SINGLE_ATTRIBUTE;  
  }

  return 0;
}

int bson_put_two_attributes_if_exist(
  bson_t *bson,
  const char *key1, int key1_length, char *value1, int value1_length,
  const char *key2, int key2_length, char *value2, int value2_length
)
{

  bson_t childAttributes;

  if ((value1 != NULL) || (value2 != NULL)) {
    if (!bson_append_document_begin(bson, KEY_USCORE_ATTRIBUTES, &childAttributes))
      return E_CWS_ERROR_APPEND_TWO_ATTRIBUTES;

    int err = 0;
    if ((value1 != NULL) && (!bson_append_utf8(&childAttributes, key1, key1_length, (const char *)value1, value1_length)))
      err = E_CWS_ERROR_APPEND_FIRST_ATTRIBUTE;
    else if ((value2 != NULL) && (!bson_append_utf8(&childAttributes, key2, key2_length, (const char *)value2, value2_length)))
      err = E_CWS_ERROR_APPEND_SECOND_ATTRIBUTE;

    if (!bson_append_document_end(bson, &childAttributes) && (err == 0))
        err = E_CWS_ERROR_END_APPEND_TWO_ATTRIBUTES;

    return err;
  }

  return 0;
}
