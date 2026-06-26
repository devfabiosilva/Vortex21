#ifndef W21GO_H
 #define W21GO_H

#include <w21_config.h>

int go_w21_config_new(struct soap **, uint64_t, uint64_t);
int go_get_w21_error(struct soap *);
uint64_t go_w21_is_validator_enabled(struct soap *);
bool go_w21_has_witsml21(struct soap *);
struct hard_stat_t *go_w21_get_stat_phases(struct soap *);
char *go_w21_get_msg(size_t *, struct soap *);
char *go_w21_get_xml_msg(size_t *, struct soap *);

#endif

