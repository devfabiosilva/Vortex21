#ifndef CWS_BSON_UTILS_H
 #define CWS_BSON_UTILS_H
#include <bson/bson.h>

struct c_json_str_t {
  char *json;
  size_t json_len;
};

struct c_bson_serialized_t {
  uint8_t *bson;
  size_t bson_size;
};

#define CWS_CONST_BSON_KEY(str) (const char *)str, (int)(sizeof(str)-1)
#define KEY_USCORE_VALUE CWS_CONST_BSON_KEY("#value")
#define KEY_USCORE_ATTRIBUTES CWS_CONST_BSON_KEY("#attributes")
#define KEY_USCORE_ABSTRACT_TYPE CWS_CONST_BSON_KEY("#abstype")
#define SET_MULTIPLE_ATTRIBUTES(...) __VA_ARGS__, NULL, 0, NULL

#define CWS_BSON_NEW bson_new()

void cws_bson_free(bson_t **);
char *cws_data_to_json(size_t *, const uint8_t *, size_t);
int bson_serialize(uint8_t **, size_t *, bson_t *);
int json_to_bson_serialized(
  uint8_t **, size_t *,
  const char *, ssize_t,
  char *, size_t
);

int bson_put_single_attribute_required(bson_t *, const char *, int, char *, int);
int bson_put_multiple_attributes_if_they_exist(bson_t *, ...);
int bson_put_single_attribute_if_exists(bson_t *, const char *, int, char *, int);
int bson_put_two_attributes_if_exist(
  bson_t *bson,
  const char *, int, char *, int,
  const char *, int, char *, int
);
#endif
