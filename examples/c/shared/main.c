#include <stdio.h>
#include <w21_config.h>
#include <cws_utils.h>
#include <w21_input.h>
#include <w21_deserializer.h>

int main(int argc, char **argv)
{
  printf("\nVortex 21 WITSML 2.1 BSON parser\n");

  if (argc > 2)
    printf("\nToo many arguments\n");
  else if (argc != 2)
    printf("\nUsage vortex21 file\n");
  else {

    const char *fileName = (const char *)argv[1];
    int err;
    struct soap *soap = NULL;

    if ((err = w21_config_new(&soap, SOAP_C_UTFSTRING|SOAP_XML_STRICT|SOAP_XML_IGNORENS, 0))) {
      printf("\nUnable to initialize instance %d\n", err);
      return err;
    }

    if ((err = w21_enable_input_rules_validator(soap))) {
      printf("\nCould not enable validator %d\n", err);
      goto main_exit1;
    }

    if ((err = cw21rd_AutoDetect_from_file(soap, fileName))) {
      printf("\nCould not parse document from file %s %d\n", fileName, err);
      goto main_exit1;
    }

    if ((err = bson_read_AutoDetect21(soap))) {
      printf("\nCould not parse from file %s %d\n", fileName, err);
      goto main_exit1;
    }

    struct c_json_str_t *c_json_str = w21_get_json(soap);

    if (c_json_str) {
      printf("\nValue %.*s\n", (int)c_json_str->json_len, c_json_str->json);
    } else
      printf("\nError. Could not parse JSON string\n");

main_exit1:
    w21_config_free(&soap);

    return err;
  }

  return 0;
}

