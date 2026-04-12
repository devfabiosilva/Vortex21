#include <bson/bson.h>
#include <cws_utils.h>

int readText(const char **text, size_t *text_len, const char *filename)
{
  int err;
  FILE *f;
  long l;

  (*text)=NULL;
  (*text_len)=0;

  if (!(f=fopen(filename, "r")))
    return -1;

  if (fseek(f, 0L, SEEK_END)<0) {
    err=-2;
    goto readText_exit1;
  }

  if ((l=ftell(f))<0) {
    err=-3;
    goto readText_exit1;
  }

  if (!((*text)=(char *)malloc((size_t)(l+1)))) {
    err=-4;
    goto readText_exit1;
  }

  (*text_len)=(size_t)l;

  err=0;
  if ((*text_len)==0) {
    ((char *)(*text))[0]=0;
    goto readText_exit1;
  }

  rewind(f);

  if (fread((void *)(*text), sizeof(const char), (*text_len), f)==(*text_len)) {
    ((char *)(*text))[(*text_len)]=0;
    goto readText_exit1;
  }

  free((void *)(*text));

  err=-5;
  (*text)=NULL;
  (*text_len)=0;

readText_exit1:
  fclose(f);

  return err;
}

inline
void readTextFree(const char **text)
{
  if (*text) {
    free((void *)(*text));
    (*text)=NULL;
  }
}

#ifndef VERGEN

void cws_version(struct cws_version_t *version)
{
//https://www.devever.net/~hl/incbin
  extern uint8_t _binary_version_bson_start[] asm("_binary_version_bson_start");
  extern uint8_t _binary_version_bson_end[] asm("_binary_version_bson_end");

  version->version=_binary_version_bson_start;
  version->versionSize=_binary_version_bson_end - _binary_version_bson_start;

}
#endif

