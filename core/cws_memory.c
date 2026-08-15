#include <stdlib.h>
#include <stddef.h>

void *cws_malloc(size_t size)
{
  void *ptr;

  if (posix_memalign(&ptr, 64, size) == 0)
    return ptr;

  return NULL;
}

