#ifndef W21GO_H
 #define W21GO_H

#include <w21_config.h>

int go_w21_config_new(struct soap **, uint64_t, uint64_t);
int go_get_w21_error(struct soap *);
uint64_t go_is_validator_enabled(struct soap *);

#endif

