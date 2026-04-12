#ifndef W21_VALIDATOR_H
 #define W21_VALIDATOR_H

#include <regex.h>
#include <stdbool.h>
#include <stdsoap2.h>

typedef int (*fw21_validator_constructor)(struct soap *, regex_t **, const char *, const char *);
typedef bool (*fw21_validator)(struct soap *, char *, const char *);
typedef void (*fw21_validator_destructor)(struct soap *, regex_t **);

struct w21_validation_t {
    const char *type; // Type descritive required (non null)
    const char *tag; // Info only. Can be null
    const char *pattern; // Pattern. NOT NULL
    regex_t *regex; // regex pointer initialized, must be free
    fw21_validator_constructor validator_constructor; // Can be null if don't need compile
    fw21_validator w21_validator; // Not null
    fw21_validator_destructor validator_destructor; // Can be null if don't need compile
};

int w21_enable_input_rules_validator(struct soap *);
void w21_disable_input_rules_validator(struct soap *);
void w21_input_rules_validator_destroy(struct soap *);

const char *rdw212__regex(struct soap *, const char *, int);

//const char *rdw212__UuidString_regex(struct soap *, const char *);
#define rdw212__UuidString_regex(s, t) rdw212__regex(s, t, 0)

//const char *rdw212__QualifiedType_regex(struct soap *, const char *);
#define rdw212__QualifiedType_regex(s, t) rdw212__regex(s, t, 1)

//const char *rdw212__TimeZone_regex(struct soap *, const char *);
#define rdw212__TimeZone_regex(s, t) rdw212__regex(s, t, 2)

#endif
