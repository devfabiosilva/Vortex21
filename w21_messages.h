#ifndef W21_MESSAGES_H
 #define W21_MESSAGES_H


void set_w21_error_message(struct soap *, int, char *, ...);
char *w21_message_sanitize(struct soap *, char *);
char *w21_message_sanitize_fmt(struct soap *, char *, ...);
void catch_gsoap_error(struct soap *);

#define SET_W21_ERROR_AND_EXIT(condTrue, err_msg)\
if (condTrue) { \
  set_w21_error_message(soap, err_msg, err_msg##_MSG); \
  return err_msg; \
}

#define SET_W21_ERROR_ARGS_AND_EXIT(condTrue, err_msg, ...)\
if (condTrue) { \
  set_w21_error_message(soap, err_msg, err_msg##_MSG, __VA_ARGS__); \
  return err_msg; \
}

#define CLEAR_W21_ERROR_MESSAGES \
config->detail_message[0] = 0; \
config->detail_message_len = 0; \
config->detail_message_xml[0] = 0; \
config->detail_message_xml_len = 0;

#endif