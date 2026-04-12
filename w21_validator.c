#include <w21_config.h>
#include <w21_messages.h>
#include <w21_errors.h>

#define RESOLVE_TAG (config->current_validation->tag)?config->current_validation->tag:"Unknown Tag"

// 1- MUST check ENABLE_REGEX_VALIDATOR is ON before call
// 2- MUST check config->current_validation is not NULL
// 3- This function MUST be only implemented in w21_validate function
static bool validate_rdw212__regex(struct soap *soap, char *value, const char *type)
{
  DECLARE_W21_CONFIG
  if (value) {
    if (config->current_validation->regex) {
      if (regexec(config->current_validation->regex, value, 0, NULL, 0) == 0) {
        config->current_validation = NULL;
        return true;
      }

      set_w21_error_message(
        soap, E_21_ERROR_REGEX,
        "Invalid value %s for %s type. Was expected value with pattern \"%s\" in tag or attribute %s",
        w21_message_sanitize_fmt(soap, "\"%s\"", value), type, config->current_validation->pattern, RESOLVE_TAG
      );
    } else {
      set_w21_error_message(
        soap, E_21_ERROR_REGEX_UNITIALIZED, 
        "Unitialized regex for %s type with pattern \"%s\". Unable to check validity value for %s for tag or attribute %s",
        type, config->current_validation->pattern, w21_message_sanitize_fmt(soap, "\"%s\"", value), RESOLVE_TAG
      );
    }
  } else {
    set_w21_error_message(
      soap, E_21_ERROR_REGEX_NULL, 
      "Null value for %s type. Was expected value with pattern \"%s\" in tag or attribute %s",
      type, config->current_validation->pattern, RESOLVE_TAG
    );
  }

  config->current_validation = NULL;
  return false;
}

static int constructor_rdw212__regex(struct soap *soap, regex_t **regex, const char *pattern, const char *type)
{

  if ((*regex = (regex_t *)malloc(sizeof(regex_t)))) {
    int ret = regcomp(*regex, pattern, REG_EXTENDED | REG_NOSUB);
    if (ret == 0) W21_RETURN

    regerror(ret, *regex, soap->tmpbuf, sizeof(soap->tmpbuf));

    free((void *)*regex);
    *regex = NULL;

    set_w21_error_message(
      soap, E_21_ERROR_REGEX_COMPILE, 
      "Could not compile regex for %s type with pattern \"%s\". Regex error response: %d. Regex error description: %s",
      type, pattern, ret, soap->tmpbuf
    );

  } else {
    set_w21_error_message(
      soap, E_21_ERROR_REGEX_INITIALIZE_COMPILE,
      "Could not initialize compile regex for %s type with pattern \"%s\". No memory",
      type, pattern
    );
  }

  W21_RETURN
}

static void destructor_rdw212__regex(struct soap *soap, regex_t **regex)
{
  if (*regex) {
    regfree(*regex);
    free((void *)*regex);
    *regex = NULL;
  }
}

static struct w21_validation_t VALIDATORS_LIST[] = {
  //0: typedef rdw212__AbstractString rdw212__UuidString "[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}";
  {
    "rdw212__UuidString",
    NULL, 
    "^[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}$",
    NULL,
    constructor_rdw212__regex,
    validate_rdw212__regex,
    destructor_rdw212__regex
  },
  //1: typedef rdw212__String256 rdw212__QualifiedType "(witsml|resqml|prodml|eml|custom)[1-9]\\d\\.\\w+";
  {
    "rdw212__QualifiedType",
    NULL, 
    "^(witsml|resqml|prodml|eml|custom)[1-9][0-9]\\.[A-Za-z0-9_]+$", // Regex POSIX C
    NULL,
    constructor_rdw212__regex,
    validate_rdw212__regex,
    destructor_rdw212__regex
  },
  //2: typedef rdw212__String64 rdw212__TimeZone "[Z]|([\\-+](([01][0-9])|(2[0-3])):[0-5][0-9])";
  {
    "rdw212__TimeZone",
    NULL, 
    "^(Z|[+-](([01][0-9])|(2[0-3])):[0-5][0-9])$", // Regex POSIX C
    NULL,
    constructor_rdw212__regex,
    validate_rdw212__regex,
    destructor_rdw212__regex
  }
};

// To be used in gsoap regex
//const char *rdw212__UuidString_regex(struct soap *soap, const char *tag)
const char *rdw212__regex(struct soap *soap, const char *tag, int index)
{
  DECLARE_W21_CONFIG

  if (config->in_config & ENABLE_REGEX_VALIDATOR) {
    if (config->validators) {
      config->current_validation = &config->validators[(size_t)index]; // Index  (allocated)
      config->current_validation->tag = tag;
      return config->current_validation->pattern;
    }
    //if config->validators == NULL then fail safe. fsvalidate will throw error after
    config->current_validation = NULL;
    return VALIDATORS_LIST[(size_t)index].pattern; // Index (static)
  }

  config->current_validation = NULL;
  return NULL;
}

#define VALIDATORS_LIST_SIZE sizeof(VALIDATORS_LIST)

_Static_assert(VALIDATORS_LIST_SIZE > 0, "VALIDATORS_LIST must not be empty");

#define NUMBER_OF_VALIDATORS_LIST_ELEMENTS (sizeof(VALIDATORS_LIST) / sizeof(VALIDATORS_LIST[0]))

void w21_input_rules_validator_destroy(struct soap *soap)
{
  DECLARE_W21_CONFIG

  if (config->validators) {
    ssize_t n = NUMBER_OF_VALIDATORS_LIST_ELEMENTS;
    struct w21_validation_t *validator_ptr = config->validators;

    do {

      if (validator_ptr->validator_destructor)
        validator_ptr->validator_destructor(soap, &validator_ptr->regex);

      ++validator_ptr;
    } while (--n > 0);

    free((void *)config->validators);
    config->validators = NULL;
  }

  config->in_config &= ~(ENABLE_REGEX_VALIDATOR);
}

int w21_enable_input_rules_validator(struct soap *soap)
{
  DECLARE_W21_CONFIG

  if (config->validators == NULL) {

    if ((config->validators = (struct w21_validation_t *)malloc(VALIDATORS_LIST_SIZE))) {
      ssize_t n = NUMBER_OF_VALIDATORS_LIST_ELEMENTS;

      memcpy((void *)config->validators, (void *)&VALIDATORS_LIST, VALIDATORS_LIST_SIZE);

      struct w21_validation_t *validator_ptr = config->validators;
      do {
        if ((validator_ptr->validator_constructor != NULL) &&
          (validator_ptr->validator_constructor(soap, &validator_ptr->regex, validator_ptr->pattern, validator_ptr->type) != 0))
          goto w21_enable_input_rules_validator_resume;
            
        ++validator_ptr;
      } while (--n > 0);

      config->in_config |= ENABLE_REGEX_VALIDATOR;
    } else {
      set_w21_error_message(
        soap, E_21_ERROR_ENABLE_INPUT_VALIDATOR, 
        "Could not enable regex validator in WITSML 2.1. Regex validator is disabled"
      );
      config->in_config &= ~(ENABLE_REGEX_VALIDATOR);
    }

    W21_RETURN
  }

  config->in_config |= ENABLE_REGEX_VALIDATOR;
  W21_RETURN

w21_enable_input_rules_validator_resume:
  w21_input_rules_validator_destroy(soap);

  W21_RETURN
}

void w21_disable_input_rules_validator(struct soap *soap)
{
  DECLARE_W21_CONFIG
  config->in_config &= ~(ENABLE_REGEX_VALIDATOR);
}

#undef NUMBER_OF_VALIDATORS_LIST_ELEMENTS
#undef RESOLVE_TAG
