#ifndef W21_EVENTS_H
 #define W21_EVENTS_H

#include <stddef.h>
#include <stdsoap2.h>

size_t w21_frecv_from_file(struct soap *, char *, size_t);
size_t w21_frecv_from_string(struct soap *, char *, size_t);
int w21_validate(struct soap *, const char *, const char *);
int w21_fsend(struct soap *, const char *, size_t);
int froot_element_detect(struct soap *, int);
#endif
