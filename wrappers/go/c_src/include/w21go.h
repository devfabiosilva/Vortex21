#ifndef W21GO_H
 #define W21GO_H

#include <w21_config.h>

int go_w21_config_new(struct soap **, uint64_t, uint64_t);
int go_get_w21_error(struct soap *);
int go_w21_enable_input_rules_validator(struct soap *);
int go_w21_can_parse(struct soap *soap);

#endif

