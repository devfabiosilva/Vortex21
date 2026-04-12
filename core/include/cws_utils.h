#ifndef CWS_UTILS_H
 #define CWS_UTILS_H

#ifdef WITH_STATISTICS
struct statistics_t {
  //Statistics in objects
  int32_t costs;
  int32_t strings;
  int32_t shorts;
  int32_t ints;
  int32_t long64s;
  int32_t enums;
  int32_t arrays;
  int32_t booleans;
  int32_t doubles;
  int32_t date_times;
  int32_t measures;
  int32_t event_types;
  int32_t total;
};
#endif

int readText(const char **, size_t *, const char *);
void readTextFree(const char **);

struct cws_version_t {
  uint8_t *version;
  size_t versionSize;
};

#ifndef VERGEN
void cws_version(struct cws_version_t *);
#endif

#endif

