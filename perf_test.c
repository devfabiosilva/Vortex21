#include <w21_config.h>
#include <w21_input.h>
#include <w21_deserializer.h>
#include <w21_validator.h>
#include <malloc.h>

#define ITER 10000

void display_mallinfo(struct mallinfo2 *mi)
{
  //struct mallinfo2 mi;
  //mi = mallinfo2();

  printf("Total non-mmapped bytes (arena):       %ld\n", mi->arena);
  printf("# of free chunks (ordblks):            %ld\n", mi->ordblks);
  printf("# of free fastbin blocks (smblks):     %ld\n", mi->smblks);
  printf("# of mapped regions (hblks):           %ld\n", mi->hblks);
  printf("Bytes in mapped regions (hblkhd):      %ld\n", mi->hblkhd);
  printf("Max. total allocated space (usmblks):  %ld\n", mi->usmblks);
  printf("Free bytes held in fastbins (fsmblks): %ld\n", mi->fsmblks);
  printf("Total allocated space (uordblks):      %ld\n", mi->uordblks);
  printf("Total free space (fordblks):           %ld\n", mi->fordblks);
  printf("Topmost releasable block (keepcost):   %ld\n", mi->keepcost);
}

int main(int argc, char **argv)
{

  struct mallinfo2 mi1, mi2;
  mi1 = mallinfo2();

  if (argc < 2) {
    printf("\nSelect one file\n");
    return 0;
  } else if (argc > 2) {
    printf("\nToo many arguments\n");
    return -10;
  }

  struct soap *soap;
  int err, i;

  //SOAP_XML_STRICT
  if ((err = w21_config_new(&soap, SOAP_C_UTFSTRING|SOAP_XML_STRICT|SOAP_XML_IGNORENS, SOAP_IO_BUFFER | SOAP_XML_NIL | SOAP_XML_INDENT | SOAP_XML_DEFAULTNS))) {
    printf("\nUnable to initialize config %d\n", err);
    return err;
  }

  const char *text = NULL;
  size_t text_len = 0;

  DECLARE_W21_CONFIG

  if (w21_enable_input_rules_validator(soap)) {
    printf("Regex rule init error\n");
    printf("\nError\n%.*s\n", (int)config->detail_message_xml_len, config->detail_message_xml);
    goto main_end;
  }

  if ((err = readText(&text, &text_len, (const char *)argv[1]))) {
    printf("\nUnable to open file %s %d\n", argv[1], err);
    goto main_end;
  }

  for (i = 0; i < ITER; ++i) {

    w21_recycle(soap);

    if ((err = cw21rd_OpsReport(soap, text, text_len))) {
      printf("Serialize error 2\n");
      printf("\nError 2\n%.*s\n", (int)config->detail_message_xml_len, config->detail_message_xml);
      goto main_end;
    }

    if ((err = bson_read_OpsReport21(soap))) {
      printf("\nError\n%.*s\n", (int)config->detail_message_xml_len, config->detail_message_xml);
      goto main_end;
    }

    w21_bson_serialize(soap);
    struct c_json_str_t *json_str = w21_get_json(soap);

    if (!json_str) {
      printf("\nJSON: Unable to parse WITSML 2.1 to JSON");
      printf("\nError A\n%.*s\n", (int)config->detail_message_xml_len, config->detail_message_xml);
      goto main_end;
    }
  }

main_end:
  readTextFree(&text);
  w21_config_free(&soap);

  mi2 = mallinfo2();

  printf("\nBefore:");
  display_mallinfo(&mi1);

  printf("\nAfter:");
  display_mallinfo(&mi2);

  return err;
}
