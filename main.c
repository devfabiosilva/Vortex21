#include <w21_config.h>
#include <w21_input.h>
#include <w21_deserializer.h>
#include <w21_validator.h>
#include <malloc.h>
//TODO move to core
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

//#define USE_FILE

int main(int argc, char **argv)
{
  struct mallinfo2 mi1, mi2;
  mi1 = mallinfo2();

//display_mallinfo();
  if (argc < 2) {
    printf("\nSelect one file\n");
    return 0;
  } else if (argc > 2) {
    printf("\nToo many arguments\n");
    return -10;
  }

  struct soap *soap;
  int err;
  bool summary_read, summary_parse;

  //SOAP_XML_STRICT
  if ((err = w21_config_new(&soap, SOAP_C_UTFSTRING|SOAP_XML_STRICT|SOAP_XML_IGNORENS, SOAP_IO_BUFFER | SOAP_XML_NIL | SOAP_XML_INDENT | SOAP_XML_DEFAULTNS))) {
    printf("\nUnable to initialize config %d\n", err);
    return err;
  }

#ifndef USE_FILE
  const char *text = NULL;
  size_t text_len = 0;
#endif

  DECLARE_W21_CONFIG

  if (w21_enable_input_rules_validator(soap)) {
    printf("Regex rule init error\n");
    printf("\nError\n%.*s\n", (int)config->detail_message_xml_len, config->detail_message_xml);
    goto main_end;
  }

/*
  if ((err = cw21rd_AutoDetect_from_file(soap, argv[1]))) {
  //if ((err = cw21rd_BhaRun_from_file(soap, "BhaRun.xml"))) {
    printf("Serialize error\n");
    printf("\nError\n%.*s\n", (int)config->detail_message_xml_len, config->detail_message_xml);
    goto main_end;
  }
*/

#ifdef USE_FILE
  summary_read = (err = w21_hard_summary_read_begin(soap)) == 0;

  if (!summary_read)
    printf("\nError w21_hard_summary_read_begin %d. Ignoring ...", err);

  if ((err = cw21rd_BhaRun_from_file(soap, argv[1]))) {
#else

  if ((err = readText(&text, &text_len, (const char *)argv[1]))) {
    printf("\nUnable to open file %s %d\n", argv[1], err);
    goto main_end;
  }

  printf("\nOpened file with text with size %ld: %.*s\n", text_len, (int)text_len, text);

  summary_read = (err = w21_hard_summary_read_begin(soap)) == 0;

  if (!summary_read)
    printf("\nError w21_hard_summary_read_begin %d. Ignoring ...", err);

  if ((err = cw21rd_AutoDetect(soap, text, text_len))) {
#endif
    printf("Serialize error 2\n");
    printf("\nError 2\n%.*s\n", (int)config->detail_message_xml_len, config->detail_message_xml);
    goto main_end;
  }

  summary_read = (err = w21_hard_summary_read_end(soap)) == 0;

  if (!summary_read)
    printf("\nError w21_hard_summary_read_end %d. Ignoring ...", err);

  summary_parse = (err = w21_hard_summary_parse_begin(soap)) == 0;

  if (!summary_parse)
    printf("\nError w21_hard_summary_parse_begin %d. Ignoring ...", err);


  if ((err = bson_read_AutoDetect21(soap))) {
  //if ((err = bson_read_CementJob21(soap))) {
  //if ((err = bson_read_CementJobEvaluation21(soap))) {
  //if ((err = bson_read_PPFGChannel21(soap))) {
  //if ((err = bson_read_Channel21(soap))) {
  //if ((err = bson_read_ChannelKind21(soap))) {
  //if ((err = bson_read_StimJob21(soap))) {
  //if ((err = bson_read_Tubular21(soap))) {
  //if ((err = bson_read_AutoDetect21(soap))) {
    printf("\nError\n%.*s\n", (int)config->detail_message_xml_len, config->detail_message_xml);
    goto main_end;
  }

  w21_bson_serialize(soap);
  struct c_json_str_t *json_str = w21_get_json(soap);

  summary_parse = (err = w21_hard_summary_parse_end(soap)) == 0;

  if (!summary_parse)
    printf("\nError w21_hard_summary_parse_end %d. Ignoring ...", err);

  if (json_str) {
    printf("\nJSON: %.*s\n", (int)json_str->json_len, json_str->json);
#ifdef WITH_STATISTICS
    struct statistics_t *stats = w21_get_statistics(soap);
    printf(
      "\nSTATISTICS:\n\nCost: %d\nStrings: %d\nShorts: %d\nInts: %d\n",
      stats->costs, stats->strings, stats->shorts, stats->ints
    );
    printf(
      "Long64s: %d\nEnums: %d\nArrays: %d\nBooleans: %d\n",
      stats->long64s, stats->enums, stats->arrays, stats->booleans
    );
    printf(
      "Doubles: %d\nDate times: %d\nEvent types: %d\nMeasures: %d\n\nTOTAL: %d\n\n",
      stats->doubles, stats->date_times, stats->event_types, stats->measures, stats->total
    );
#endif
  } else {
    printf("\nJSON: Unable to parse WITSML 2.1 to JSON");
    printf("\nError A\n%.*s\n", (int)config->detail_message_xml_len, config->detail_message_xml);
  }

#ifdef WITH_STATISTICS
    if (summary_read) {
      printf("Read WITSML 2.1\n\tTotal Cycles: %lu\n\tTotal (ns): %lu\n\tTotal mem: %lu\n\n",
        config->hardware_statistics.in_total_cycles,
        config->hardware_statistics.in_total_nanos,
        config->hardware_statistics.in_mem_delta
      );
    }

    if (summary_parse) {
      printf("Parse BSON and JSON string\n\tTotal Cycles: %lu\n\tTotal (ns): %lu\n\tTotal mem: %lu\n\n",
        config->hardware_statistics.in_parse_total_cycles,
        config->hardware_statistics.in_parse_total_nanos,
        config->hardware_statistics.in_parse_mem_delta
      );
    }
#endif

main_end:
#ifndef USE_FILE
  readTextFree(&text);
#endif
  w21_config_free(&soap);

  mi2 = mallinfo2();

  printf("\nBefore:");
  display_mallinfo(&mi1);

  printf("\nAfter:");
  display_mallinfo(&mi2);


//malloc_stats();

  return err;
}
