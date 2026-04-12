#include <jni.h>
#include <w21_config.h>
#include <w21_input.h>
#include <w21_deserializer.h>
#include <w21_validator.h>
#include <time.h>
#include <pthread.h>
#include <malloc.h>
#include <stdlib.h>
#include <unistd.h>
#include <parser_macros.h>
#include <parser_errors.h>

#define W21_PARSER_MAGIC 0x574954534D4C3231ULL // "WITSML21"
#define W21_PROJECT_CLASS_PATH "org/w21parser/"
#define EXCEPTION_CLASS "java/lang/Exception"
#define W21_PARSER_LOADER W21_PROJECT_CLASS_PATH "W21ParserLoader"
#define W21EXCEPTION_CLASS W21_PROJECT_CLASS_PATH "W21Exception"
#define BYTE_BUFFER_CLASS "java/nio/ByteBuffer"
#define JNI_W21_LOCKER_MAX_LIMIT 150

#define JNI_W21_BYTE_BUFFER_SIGNATURE "Ljava/nio/ByteBuffer;"

// if exists (thread_ctx != NULL) then exists (thread_ctx->soap != NULL) and (thread_ctx->soap->user)
#define DECLARE_JNI_W21_CONFIG W21_CONFIG *config = ((W21_CONFIG *)locker->thread_ctx->soap->user);
#define GET_W21_JNI_SOAP locker->thread_ctx->soap

#define W21_SECURE_PTR_CONST "WITSML-2.1-SECURE-POINTER"
#define W21_SECURE_PTR_CONST_LEN (sizeof(W21_SECURE_PTR_CONST)-1)
#define SET_SECURE_DIRECT_BUFFER_TO_BSON set_direct_buffer(env, thisObject, gW21_bbBson, (void *)W21_SECURE_PTR, W21_SECURE_PTR_CONST_LEN)
#define SET_SECURE_DIRECT_BUFFER_TO_JSON set_direct_buffer(env, thisObject, gW21_bbJson, (void *)W21_SECURE_PTR, W21_SECURE_PTR_CONST_LEN)

static const char *W21_SECURE_PTR = W21_SECURE_PTR_CONST;
static bool gW21_JVM_Initialized = false;
static jfieldID gW21_JniHandler = NULL;
static jfieldID gW21_JniHandlerCheck = NULL;
static jfieldID gW21_bbBson = NULL;
static jfieldID gW21_bbJson = NULL;
static jfieldID gW21_bbXml = NULL;
static jmethodID gW21_ByteBuffer_ReadOnlyMethod = NULL;
static uint64_t gW21_XorSeed = 0;

static jclass gW21Exception = NULL;
static jclass gExceptionClass = NULL;
static jclass gJW21ParserLoaderClass = NULL;
static jclass gJW21ByteBufferClass = NULL;
static jmethodID gW21ExceptionCtor = NULL;

_Static_assert(sizeof(uint64_t) >= sizeof(void *), "Wrong uint64_t size");
_Static_assert(sizeof(uint64_t) >= sizeof(jlong), "Incompatible jlong size");
_Static_assert(sizeof(jlong) >= sizeof(void *), "Review code for jlong size");

enum JNI_W21_ParserContext_status_e {
  JNI_W21_Closed = 0,
  JNI_W21_Init,
  JNI_w21_CloseRequest
};

typedef struct {
  uint64_t magic;
  volatile enum JNI_W21_ParserContext_status_e status;
  struct soap *soap;
} JNI_W21_ParserContext;

/*
typedef struct {
  bool isUsed; // This lock is used?
  pthread_mutex_t lock; // This initialized lock
  JNI_W21_ParserContext *thread_ctx; // Locker does not touch this. It is only used for thread

} JNI_W21_Locker_List;
*/
typedef struct {
    union {
        struct {
            bool isUsed; // This lock is used?
            pthread_mutex_t lock; // This initialized lock
            JNI_W21_ParserContext *thread_ctx; // Locker does not touch this. It is only used for thread
            bool withResourceStats; // For hardware and document parsing telemetry
            bool isResourceStatsParseFinished; // Hardware / parsing telemetry parse Finished ?
            bool isResourceStatsParseJsonFinished; // Hardware / parsing JSON telemetry parse Finished ?
            bool isResourceStatsReadFinished; // Hardware / read WITSML 2.1 document telemetry read Finished ?
        };
        uint8_t pad_to_64[64];
    };
} JNI_W21_Locker_List;

_Static_assert(sizeof(JNI_W21_Locker_List) == 64, "Error JNI_W21_Locker_List size");

typedef struct {
  pthread_mutex_t master_lock; // used in JNI_W21_locker_take, JNI_W21_locker_give, JNI_W21_locker_free
  ssize_t limit; // Max limit lock per each thread. TODO: find gsoap max thread limit
  ssize_t index; // index <= limit. Must start with 1 pthread_mutex_t
  JNI_W21_Locker_List *list; // Alloc'd List with limit size and index elements initialized
} JNI_W21_Locker;

static JNI_W21_Locker *jni_w21_locker = NULL;

static int JNI_W21_Locker_init(JNI_W21_Locker **locker)
{

  if ((*locker))
    return JNI_W21_ERROR_INIT_ALREADY_STARTED;

  if (posix_memalign((void **)locker, 64, sizeof(JNI_W21_Locker))) {
  //if (((*locker) = (JNI_W21_Locker *)malloc(sizeof(JNI_W21_Locker))) == NULL)
    return JNI_W21_ERROR_INIT_LOCKER;
  }

  memset((void *)(*locker), 0, sizeof(JNI_W21_Locker));

  int err;
  if (pthread_mutex_init(&(*locker)->master_lock, NULL)) {
    err = JNI_W21_ERROR_INIT_MASTER_MUTEX;
    goto JNI_W21_Locker_init_exit1;
  }

#define JNI_W21_LOCKER_LIST_MAX_SIZE (JNI_W21_LOCKER_MAX_LIMIT*sizeof(JNI_W21_Locker_List))
 
  //(*locker)->list = (JNI_W21_Locker_List *)malloc(JNI_W21_LOCKER_LIST_MAX_SIZE);
  if (posix_memalign((void **)&(*locker)->list, 64, JNI_W21_LOCKER_LIST_MAX_SIZE)) {
  //if ((*locker)->list == NULL) {
    err = JNI_W21_ERROR_INIT_LOCKER_LIST;
    goto JNI_W21_Locker_init_exit2;
  }

  memset((*locker)->list, 0, JNI_W21_LOCKER_LIST_MAX_SIZE);

  if (pthread_mutex_init(&(&(*locker)->list[0])->lock, NULL)) {
    err = JNI_W21_ERROR_INIT_FIRST_LOCK_MUTEX;
    goto JNI_W21_Locker_init_exit3;
  }

  (*locker)->limit = JNI_W21_LOCKER_LIST_MAX_SIZE;
  (*locker)->index = 1; // At least one is initialized. First is initialized ready to be used

  return 0;

JNI_W21_Locker_init_exit3:
  free((void *)(*locker)->list);

JNI_W21_Locker_init_exit2:
  pthread_mutex_destroy(&(*locker)->master_lock);

JNI_W21_Locker_init_exit1:
  free((void *)(*locker));
  (*locker) = NULL;

  fprintf(stderr, CONST_ERROR_STR("Fail to init JNI_W21_Locker_init %d"), err);
  return err;
#undef JNI_W21_LOCKER_LIST_MAX_SIZE
}

// SHOULD BE ONLY USED IN JNI_OnUnload
static void JNI_W21_Locker_free(JNI_W21_Locker **locker)
{
  if (*locker) {

    ssize_t index = (*locker)->index;

    int err;
    JNI_W21_Locker_List *list = (*locker)->list;

    while (index > 0)
      if ((err = pthread_mutex_destroy(&(&list[--index])->lock)))
        fprintf(stderr, CONST_ERROR_STR("Fail to destroy locker at index %d with err %d. Skipping"), (int)index, err);

    free((void *)(*locker)->list);
    (*locker)->list = NULL;

    if ((err = pthread_mutex_destroy(&(*locker)->master_lock)))
      fprintf(stderr, CONST_ERROR_STR("Fail to destroy master locker %d. Skipping"), err);

    free((void *)(*locker));
    (*locker) = NULL;
  }
}

static int JNI_W21_locker_take(JNI_W21_Locker_List **lockInListPtr, JNI_W21_Locker *locker)
{

  (*lockInListPtr) = NULL;

  if (!locker)
    return JNI_W21_ERROR_LOCK_IN_LIST_NOT_INIT;

  if (pthread_mutex_lock(&locker->master_lock))
    return JNI_W21_ERROR_LOCK_TAKE;

  int err = 0;
  JNI_W21_Locker_List *list_tmp = &locker->list[0];
  ssize_t index = locker->index;

  while (index > 0) {
    if (!list_tmp->isUsed) {
      list_tmp->isUsed = true;
      *lockInListPtr = list_tmp;
      goto JNI_W21_locker_take_exit1;
    }
    ++list_tmp;
    --index;
  }

  index = locker->index;
  if (index < locker->limit) {
    list_tmp = &(locker->list[(size_t)index]);
    if (pthread_mutex_init(&(list_tmp->lock), NULL) == 0) {
      ++locker->index;
      list_tmp->isUsed = true;
      *lockInListPtr = list_tmp;
    } else
      err = JNI_W21_ERROR_ADD_NEW_TAKE;
  } else
    err = JNI_W21_ERROR_LOCKER_FULL_TAKE;

JNI_W21_locker_take_exit1:
  if (pthread_mutex_unlock(&locker->master_lock)) {
    if (*lockInListPtr) {
      (*lockInListPtr)->isUsed = false;
      (*lockInListPtr) = NULL;
    }
    return JNI_W21_ERROR_UNLOCK_TAKE;
  }

  return err;
}

static int JNI_W21_locker_give(JNI_W21_Locker_List **lockInListPtr, JNI_W21_Locker *locker)
{
  if (!locker)
    return JNI_W21_ERROR_LOCK_IN_LIST_NOT_INIT;

  if ((*lockInListPtr) == NULL)
    return JNI_W21_ERROR_LOCKER_GIVE_INVALID_LOCKER;

  if (pthread_mutex_lock(&locker->master_lock))
    return JNI_W21_ERROR_LOCK_GIVE;

  (*lockInListPtr)->isUsed = false;

  if (pthread_mutex_unlock(&locker->master_lock) == 0) {
    (*lockInListPtr) = NULL;
    return 0;
  }

  // if *lockInListPtr != null (*lockInListPtr)->isUsed is always true
  // If an error occurs this slot is marked as true forever or until unlock is released even context is null
  (*lockInListPtr)->isUsed = true;
  return JNI_W21_ERROR_UNLOCK_GIVE;
}

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved)
{
  if (gW21_JVM_Initialized) {
    fprintf(stderr, CONST_ERROR_STR("INIT: WITSML 2.1 parser already loaded"));
    return JNI_VERSION_1_8;
  }

  if (vm == NULL) {
    fprintf(stderr, CONST_ERROR_STR("INIT: Unable to initialize. VM is NULL"));
    return JNI_ERR;    
  }

  JNIEnv *env = NULL;

  if ((*vm)->GetEnv(vm, (void **)&env, JNI_VERSION_1_8) != JNI_OK) {
    fprintf(stderr, CONST_ERROR_STR("INIT: Could not find Java Environment. Abort"));
    return JNI_ERR;
  }

  if (!env) {
    fprintf(stderr, CONST_ERROR_STR("INIT: Unexpected null environment. Abort"));
    return JNI_ERR;
  }

  jclass local;
  
  if ((local = (*env)->FindClass(env, W21_PARSER_LOADER)) == NULL) {
    fprintf(stderr, CONST_ERROR_STR("INIT: Could not find class " W21_PARSER_LOADER));
    return JNI_ERR;
  }

  gJW21ParserLoaderClass = (*env)->NewGlobalRef(env, local);
  (*env)->DeleteLocalRef(env, local);

  if (gJW21ParserLoaderClass == NULL) {
    fprintf(stderr, CONST_ERROR_STR("INIT: Fail to set global initialization class " W21_PARSER_LOADER));
    return JNI_ERR;
  }

  jint err = JNI_ERR;
  if ((local = (*env)->FindClass(env, W21EXCEPTION_CLASS)) == NULL) {
    fprintf(stderr, CONST_ERROR_STR("INIT: Could not find class " W21EXCEPTION_CLASS));
    goto JNI_OnLoad_exit1;
  }

  gW21Exception = (*env)->NewGlobalRef(env, local);
  (*env)->DeleteLocalRef(env, local);

  if (gW21Exception == NULL) {
    fprintf(stderr, CONST_ERROR_STR("INIT: Fail to set global initialization class " W21EXCEPTION_CLASS));
    goto JNI_OnLoad_exit1;
  }

  if ((local = (*env)->FindClass(env, EXCEPTION_CLASS)) == NULL) {
    fprintf(stderr, CONST_ERROR_STR("INIT: Could not find class " EXCEPTION_CLASS));
    goto JNI_OnLoad_exit2;
  }

  gExceptionClass =(*env)->NewGlobalRef(env, local);
  (*env)->DeleteLocalRef(env, local);

  if (gExceptionClass == NULL) {
    fprintf(stderr, CONST_ERROR_STR("INIT: Fail to set global initialization class " EXCEPTION_CLASS));
    goto JNI_OnLoad_exit2;
  }

  if ((local = (*env)->FindClass(env, BYTE_BUFFER_CLASS)) == NULL) {
    fprintf(stderr, CONST_ERROR_STR("INIT: Could not find class " BYTE_BUFFER_CLASS));
    goto JNI_OnLoad_exit3;
  }

  gJW21ByteBufferClass =(*env)->NewGlobalRef(env, local);
  (*env)->DeleteLocalRef(env, local);

  if (gJW21ByteBufferClass == NULL) {
    fprintf(stderr, CONST_ERROR_STR("INIT: Fail to set global initialization class " BYTE_BUFFER_CLASS));
    goto JNI_OnLoad_exit3;
  }

  if ((gW21ExceptionCtor = (*env)->GetMethodID(
    env, gW21Exception,
    "<init>",
    "(Ljava/lang/String;ILjava/lang/String;Ljava/lang/String;)V")) == NULL
  ) {
    fprintf(stderr, CONST_ERROR_STR("INIT: Fail to init constructor with class " W21EXCEPTION_CLASS));
    goto JNI_OnLoad_exit4;
  }

  if ((gW21_JniHandler = (*env)->GetFieldID(env, gJW21ParserLoaderClass, "jniHandler", "J")) == NULL) {
    fprintf(stderr, CONST_ERROR_STR("INIT: Fail to get field jniHandler"));
    goto JNI_OnLoad_exit4;
  }

  if ((gW21_JniHandlerCheck = (*env)->GetFieldID(env, gJW21ParserLoaderClass, "jniHandlerCheck", "J")) == NULL) {
    fprintf(stderr, CONST_ERROR_STR("INIT: Fail to get field jniHandlerCheck"));
    goto JNI_OnLoad_exit4;
  }

  if ((gW21_bbBson = (*env)->GetFieldID(env, gJW21ParserLoaderClass, "bbBson", JNI_W21_BYTE_BUFFER_SIGNATURE)) == NULL) {
    fprintf(stderr, CONST_ERROR_STR("INIT: Fail to get field bbBson"));
    goto JNI_OnLoad_exit4;
  }

  if ((gW21_bbJson = (*env)->GetFieldID(env, gJW21ParserLoaderClass, "bbJson", JNI_W21_BYTE_BUFFER_SIGNATURE)) == NULL) {
    fprintf(stderr, CONST_ERROR_STR("INIT: Fail to get field bbJson"));
    goto JNI_OnLoad_exit4;
  }

  if ((gW21_bbXml = (*env)->GetFieldID(env, gJW21ParserLoaderClass, "bbXml", JNI_W21_BYTE_BUFFER_SIGNATURE)) == NULL) {
    fprintf(stderr, CONST_ERROR_STR("INIT: Fail to get field bbXml"));
    goto JNI_OnLoad_exit4;
  }

  if ((gW21_ByteBuffer_ReadOnlyMethod = (*env)->GetMethodID(env, gJW21ByteBufferClass, "asReadOnlyBuffer", "()Ljava/nio/ByteBuffer;")) == NULL) {
    fprintf(stderr, CONST_ERROR_STR("INIT: Fail to read method asReadOnlyBuffer in ByteBuffer"));
    goto JNI_OnLoad_exit4;
  }

  if (JNI_W21_Locker_init(&jni_w21_locker))
    goto JNI_OnLoad_exit4;

  gW21_XorSeed = ((uint64_t)time(NULL))^((uint64_t)&gW21_JVM_Initialized)^((uint64_t)W21_PARSER_MAGIC);
  gW21_JVM_Initialized = true;

  return JNI_VERSION_1_8;

JNI_OnLoad_exit4:
  gW21_ByteBuffer_ReadOnlyMethod = NULL;
  gW21_bbXml = NULL;
  gW21_bbJson = NULL;
  gW21_bbBson = NULL;
  gW21_JniHandlerCheck = NULL;
  gW21_JniHandler = NULL;
  gW21ExceptionCtor = NULL;

  (*env)->DeleteGlobalRef(env, gJW21ByteBufferClass);
  gJW21ByteBufferClass = NULL;

JNI_OnLoad_exit3:
  (*env)->DeleteGlobalRef(env, gExceptionClass);
  gExceptionClass = NULL;

JNI_OnLoad_exit2:
  (*env)->DeleteGlobalRef(env, gW21Exception);
  gW21Exception = NULL;

JNI_OnLoad_exit1:
  (*env)->DeleteGlobalRef(env, gJW21ParserLoaderClass);
  gJW21ParserLoaderClass = NULL;

  return err;
}

JNIEXPORT void JNICALL JNI_OnUnload(JavaVM* vm, void* reserved)
{
  if (!gW21_JVM_Initialized)
    return;

  JNI_W21_Locker_free(&jni_w21_locker);

  if (vm == NULL) {
    fprintf(stderr, CONST_ERROR_STR("DEINIT: Unable to deinitialize. VM is NULL"));
    return;
  }

  JNIEnv *env = NULL;

  if ((*vm)->GetEnv(vm, (void **)&env, JNI_VERSION_1_8) != JNI_OK) {
    fprintf(stderr, CONST_ERROR_STR("DEINIT: Could not find Java Environment. Abort"));
    return;
  }

  if (!env) {
    fprintf(stderr, CONST_ERROR_STR("DEINIT: Unexpected null environment. Abort"));
    return;
  }

  gW21_ByteBuffer_ReadOnlyMethod = NULL;
  gW21_bbXml = NULL;
  gW21_bbJson = NULL;
  gW21_bbBson = NULL;
  gW21_JniHandlerCheck = NULL;
  gW21_JniHandler = NULL;
  gW21ExceptionCtor = NULL;

  if (gJW21ByteBufferClass) {
    (*env)->DeleteGlobalRef(env, gJW21ByteBufferClass);
    gJW21ByteBufferClass = NULL;    
  }

  if (gExceptionClass) {
    (*env)->DeleteGlobalRef(env, gExceptionClass);
    gExceptionClass = NULL;
  }

  if (gW21Exception) {
    (*env)->DeleteGlobalRef(env, gW21Exception);
    gW21Exception = NULL;
  }

  if (gJW21ParserLoaderClass) {
    (*env)->DeleteGlobalRef(env, gJW21ParserLoaderClass);
    gJW21ParserLoaderClass = NULL;
  }

  gW21_JVM_Initialized = false;
  gW21_XorSeed = 0;

  return;
}
//locker NOT NULL
// Should be executed after lock thread instance to check consistences
static int jni_check_consistence(JNI_W21_Locker_List *locker)
{

  if (locker->thread_ctx) {
    if (locker->thread_ctx->magic == W21_PARSER_MAGIC)
      return 0;
    return JNI_W21_ERROR_CONSISTENCY_CHECK_MAGIC;
  }
  return JNI_W21_ERROR_CONSISTENCY_CHECK_THREAD_NULL;
}

static void throw_21_default_exception(JNIEnv *env, const char *message)
{
  if ((gExceptionClass == NULL) || ((*env)->ThrowNew(env, gExceptionClass, message) != 0))
    fprintf(stderr, CONST_ERROR_STR("Unable to throw_21_default_exception. Unexpected error"));
}

static void throw_w21_exception(
  JNIEnv *env,
  const char *message,
  int error,
  const char *faultstring,
  const char *XMLfaultdetail
)
{

  if ((gW21Exception != NULL) && (gW21ExceptionCtor != NULL)) {

    jstring
      jMessage, jFaultString, jXMLfaultdetail;

    if ((jMessage = (*env)->NewStringUTF(env, (message)?(message):"")) == NULL) {
      throw_21_default_exception(env, "Unable to initialize UTF-8 string in W21Exception message");
      return;
    }

    if ((jFaultString = (*env)->NewStringUTF(env, (faultstring)?(faultstring):"")) == NULL) {
      throw_21_default_exception(env, "Unable to initialize UTF-8 string in W21Exception faultstring");
      goto throw_w21_exception_exit1;
    }

    if ((jXMLfaultdetail = (*env)->NewStringUTF(env, (XMLfaultdetail)?(XMLfaultdetail):"")) == NULL) {
      throw_21_default_exception(env, "Unable to initialize UTF-8 string in W21Exception XMLfaultdetail");
      goto throw_w21_exception_exit2;
    }

    jobject exObj = (*env)->NewObject(
      env,
      gW21Exception,
      gW21ExceptionCtor,
      jMessage,
      (jint)error,
      jFaultString,
      jXMLfaultdetail
    );

    if (exObj) {
      if ((*env)->Throw(env, exObj) == 0)
        goto throw_w21_exception_exit3;

      throw_21_default_exception(env, "Unable to throw W21Exception");

      (*env)->DeleteLocalRef(env, exObj);
    } else
      throw_21_default_exception(env, "Unable to create W21Exception object");

throw_w21_exception_exit3:
  (*env)->DeleteLocalRef(env, jXMLfaultdetail);

throw_w21_exception_exit2:
    (*env)->DeleteLocalRef(env, jFaultString);

throw_w21_exception_exit1:
    (*env)->DeleteLocalRef(env, jMessage);

    return;
  }

  throw_21_default_exception(env, "Unable to throw_w21_exception. Unexpected error");
}

static int jni_get_handler(JNI_W21_Locker_List **locker, JNIEnv *env, jobject thisObject)
{
  *locker = NULL;
  if ((gW21_JniHandler != NULL) && (gW21_JniHandlerCheck != NULL)) {
    uint64_t jniHandler = (*env)->GetLongField(env, thisObject, gW21_JniHandler);
    uint64_t JniHandlerCheck = (*env)->GetLongField(env, thisObject, gW21_JniHandlerCheck);

    if (jniHandler) {
      if ((gW21_XorSeed ^ JniHandlerCheck) == jniHandler) {
        *locker = (JNI_W21_Locker_List *)jniHandler;
        return 0;
      }

      return JNI_W21_ERROR_GET_HANDLER_CHECK_ERROR;
    } 

    if (JniHandlerCheck == 0)
      return 0;

    return JNI_W21_ERROR_GET_HANDLER_INCONSISTENT;
  }

  return JNI_W21_ERROR_GET_HANDLER_UNITIALIZED;
}

static int jni_set_handler(JNIEnv *env, jobject thisObject, JNI_W21_Locker_List *locker)
{
  if ((gW21_JniHandler != NULL) && (gW21_JniHandlerCheck != NULL)) {
    uint64_t jniHandler = (uint64_t)locker;
    uint64_t jniHandlerCheck = (jniHandler)?(gW21_XorSeed ^ jniHandler):0;

    (*env)->SetLongField(env, thisObject, gW21_JniHandler, (jlong)jniHandler);

    if ((*env)->ExceptionCheck(env) == JNI_FALSE) {
      (*env)->SetLongField(env, thisObject, gW21_JniHandlerCheck, (jlong)jniHandlerCheck);

      if ((*env)->ExceptionCheck(env) == JNI_FALSE)
        return 0;
    } else
      return JNI_W21_ERROR_SET_HANDLER;

    return JNI_W21_ERROR_SET_HANDLER_CHECK;
  }

  return JNI_W21_ERROR_SET_HANDLER_UNITIALIZED;
}

// ALWAYS READ ONLY
static int set_direct_buffer(JNIEnv *env, jobject thisObject, jfieldID fieldId, void *data, size_t data_size)
{
  if (gW21_ByteBuffer_ReadOnlyMethod) {
    jobject directBuffer = (*env)->NewDirectByteBuffer(env, data, data_size);

    if (directBuffer) {
      jobject readOnlyBuffer = (*env)->CallObjectMethod(env, directBuffer, gW21_ByteBuffer_ReadOnlyMethod);
  
      (*env)->DeleteLocalRef(env, directBuffer);
  
      if (readOnlyBuffer) {
        (*env)->SetObjectField(env, thisObject, fieldId, readOnlyBuffer);
        int err = ((*env)->ExceptionCheck(env) == JNI_FALSE)?0:JNI_W21_DIRECT_BUFFER_SET_OBJECT;
        (*env)->DeleteLocalRef(env, readOnlyBuffer);
        return err;
      }

      return JNI_W21_DIRECT_BUFFER_SET_READ_ONLY_ATTRIBUTE;
    }
    return JNI_W21_DIRECT_BUFFER_CREATE_NEW;
  }

  return JNI_W21_DIRECT_BUFFER_METHOD_ID_UNINITIALIZED;
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniInit
 * Signature: (JJZ)V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniInit(
  JNIEnv *env, jobject thisObject, jlong inputConfig, jlong outputConfig, jboolean hasResourceStats
)
{

  if ((~(SOAP_XML_STRICT|SOAP_XML_IGNORENS)) & inputConfig) {
    throw_w21_exception(env, "jniInit: Invalid input", JNI_W21_ERROR_INIT_INPUT, NULL, NULL);
    return;
  }

  if (outputConfig) {
    throw_w21_exception(env, "jniInit: Invalid output. NOT IMPLEMENTED YET", JNI_W21_ERROR_INIT_OUTPUT, NULL, NULL);
    return;
  }

  JNI_W21_Locker_List *locker;

  int err = jni_get_handler(&locker, env, thisObject);

  if (err == 0) {
    if (locker == NULL) {
      if ((err = JNI_W21_locker_take(&locker, jni_w21_locker)) == 0) {
        if (locker->thread_ctx == NULL) {
          if (posix_memalign((void **)&locker->thread_ctx, 64, sizeof(JNI_W21_ParserContext)) == 0) {
            memset(locker->thread_ctx, 0, sizeof(JNI_W21_ParserContext));
            if ((err = w21_config_new(&locker->thread_ctx->soap, SOAP_C_UTFSTRING|(uint64_t)inputConfig, SOAP_IO_BUFFER | SOAP_XML_NIL | (uint64_t)outputConfig)) == 0) {
              locker->thread_ctx->magic = W21_PARSER_MAGIC;
              locker->thread_ctx->status = JNI_W21_Init;
              locker->withResourceStats = (bool)hasResourceStats;
              if ((err = jni_set_handler(env, thisObject, locker))) {
                w21_config_free(&locker->thread_ctx->soap);
                fprintf(stderr, CONST_ERROR_STR("jniInit: Unexpected. Unable to set handler jni_set_handler: %d"), err);
                throw_21_default_exception(env, "jniInit: Unable to set handler");
                goto Java_org_w21parser_W21ParserLoader_jniInit_exit2;
              }
            } else {
              fprintf(stderr, CONST_ERROR_STR("jniInit: No memory to allocate Witsml 21 parser %d. Abort"), err);
              throw_21_default_exception(env, "jniInit: No memory to allocate Witsml 21 parser");
              goto Java_org_w21parser_W21ParserLoader_jniInit_exit2;
            }
          } else {
            locker->thread_ctx = NULL;
            fprintf(stderr, CONST_ERROR_STR("jniInit: No memory to allocate JNI_W21_ParserContext struct"));
            throw_21_default_exception(env, "jniInit: Unexpected. No mem to allocate JNI_W21_ParserContext");
            goto Java_org_w21parser_W21ParserLoader_jniInit_exit1;
          }
        } else {
          fprintf(stderr, CONST_ERROR_STR("jniInit: Unexpected non null thread context locker->thread_ctx"));
          throw_21_default_exception(env, "jniInit: Unexpected non null context");
          goto Java_org_w21parser_W21ParserLoader_jniInit_exit1;
        }

        return;
      }

      fprintf(stderr, CONST_ERROR_STR("jniInit: Unexpected lock take at JNI_W21_locker_take %d"), err);
      throw_21_default_exception(env, "jniInit: Unexpected lock take");

      return;
    }
    throw_w21_exception(env, "jniInit: Parser already initialized", JNI_W21_ERROR_INIT_ALREADY_INITIALIZED, NULL, NULL);
  } else {
    fprintf(stderr, CONST_ERROR_STR("jniInit: Unexpected memory violation check at jni_get_handler %d"), err);
    throw_21_default_exception(env, "jniInit: Unexpected memory violation check");
  }

  return;

Java_org_w21parser_W21ParserLoader_jniInit_exit2:
  locker->withResourceStats = false;
  locker->thread_ctx->status = JNI_W21_Closed;
  locker->thread_ctx->magic = 0;
  free(locker->thread_ctx);
  locker->thread_ctx = NULL;

Java_org_w21parser_W21ParserLoader_jniInit_exit1:
  if ((err = JNI_W21_locker_give(&locker, jni_w21_locker))) {
    fprintf(stderr, CONST_ERROR_STR("jniInit: Unexpected error. Unable to give locker JNI_W21_locker_give %d"), err);
    // Don't throw error here. Maybe previous error as released before.
  }

  return;
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniClose
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_w21parser_W21ParserLoader_jniClose
(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_Locker_List *locker;
  int err = jni_get_handler(&locker, env, thisObject);

  if (err == 0) {
    if (locker) {
      if ((err = pthread_mutex_trylock(&locker->lock)) == 0) {
        if (locker->thread_ctx) {
          w21_config_free(&locker->thread_ctx->soap);
          __atomic_store_n(&locker->thread_ctx->status, JNI_W21_Closed, __ATOMIC_RELEASE);
          locker->thread_ctx->magic = 0;
          free(locker->thread_ctx);
          locker->thread_ctx = NULL;
          if ((err = SET_SECURE_DIRECT_BUFFER_TO_BSON))
            fprintf(stderr, CONST_ERROR_STR("jniClose: Could not set secure direct buffer ptr BSON %d. Ignoring"), err);
          if ((err = SET_SECURE_DIRECT_BUFFER_TO_JSON))
            fprintf(stderr, CONST_ERROR_STR("jniClose: Could not set secure direct buffer ptr JSON %d. Ignoring"), err);
        } else {
          fprintf(stderr, CONST_ERROR_STR("jniClose: Unexpected thread ctx. Instance closed but open locker. Ignoring"));
          //throw_21_default_exception(env, "jniClose: Unexpected thread lock error. Instance already closed with open locker");
        }
        goto Java_org_w21parser_W21ParserLoader_close_unlock;
      } else if (err == EBUSY) {
        usleep(600);
        if (locker->thread_ctx)
          __atomic_store_n(&locker->thread_ctx->status, JNI_w21_CloseRequest, __ATOMIC_RELEASE);
        else {
          err = JNI_W21_ERROR_SCHEDULE_CLOSE_REQUEST;
          fprintf(stderr, CONST_ERROR_STR("jniClose: Unexpected. Unable to schedule context. Instance not initializad %d."), err);
        }
      } else {
        fprintf(stderr, CONST_ERROR_STR("jniClose: Unexpected thread lock error. Unable to close this instance. pthread_mutex_trylock error: %d. Memory may be leaked. Try later"), err);
        //throw_21_default_exception(env, "jniInit: Unexpected thread lock error. Unable to close this instance. Try later");
      }
      return err;
    }

    //throw_w21_exception(env, "jniClose: Instance already closed", JNI_W21_ERROR_INIT_ALREADY_CLOSED, "", "");
    return JNI_W21_ERROR_INIT_ALREADY_CLOSED;
  }

  fprintf(stderr, CONST_ERROR_STR("jniClose: Unexpected error. Unable to close this instance. jni_get_handler error: %d. Memory may be leaked. Try later"), err);
  //throw_21_default_exception(env, "jniClose: Unexpected error. Unable to close this instance. Try later");
  return err;

Java_org_w21parser_W21ParserLoader_close_unlock:

  if ((err = pthread_mutex_unlock(&locker->lock))) {
    fprintf(stderr, CONST_ERROR_STR("jniClose: Unexpected unlock error. Instance closed but open locker pthread_mutex_unlock: %d. Ignoring"), err);
    return err;
  }

  if ((err = jni_set_handler(env, thisObject, NULL))) {
    fprintf(stderr, CONST_ERROR_STR("jniClose: Unexpected set handler error. Instance closed but open locker jni_set_handler: %d. Ignoring"), err);
    return err;
  }

  if ((err = JNI_W21_locker_give(&locker, jni_w21_locker)))
    fprintf(stderr, CONST_ERROR_STR("jniClose: Unexpected locker give error. Instance closed but open locker JNI_W21_locker_give: %d. Ignoring"), err);

  return err;
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniInGetObjectName
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_org_w21parser_W21ParserLoader_jniInGetObjectName(
  JNIEnv *env, jobject thisObject
)
{
  jstring jstr = NULL;
  JNI_W21_EXECUTE_ACTION_RET(
    jniInGetObjectName,
    const char *str = w21_get_input_object_name(GET_W21_JNI_SOAP);
    if (str) {
      if ((jstr = (*env)->NewStringUTF(env, str)) == NULL)
        throw_w21_exception(
          env, 
          "jniInGetObjectName: Unable to create new Java UTF-8 String for object name",
          JNI_W21_UNABLE_TO_GET_UTF8_OBJECT_NAME_STR,
          NULL,
          NULL
        );
    } else
        throw_w21_exception(
          env, 
          "jniInGetObjectName: Object not parsed, not existent or parser has error",
          JNI_W21_OBJECT_NOT_PARSED_OR_HAS_ERROR,
          NULL,
          NULL
        );
    ,
    jstr
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniLoadSoapXmlStrict
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_w21parser_W21ParserLoader_jniLoadSoapXmlStrict
(
  JNIEnv *env, jobject thisObject
)
{
  return (jint)SOAP_XML_STRICT;
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniLoadSoapXmlIgnoreNS
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_w21parser_W21ParserLoader_jniLoadSoapXmlIgnoreNS
(
  JNIEnv *env, jobject thisObject
)
{
  return (jint)SOAP_XML_IGNORENS;
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniInputRulesValidatorEnable
 * Signature: ()V
 */
JNIEXPORT void JNICALL
Java_org_w21parser_W21ParserLoader_jniInputRulesValidatorEnable(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniInputRulesValidatorEnable,
    DECLARE_JNI_W21_CONFIG
    if ((err = w21_enable_input_rules_validator(GET_W21_JNI_SOAP)))
      throw_w21_exception(
        env, 
        "jniInputRulesValidatorEnable init validator: See details for complete error description",
        err,
        config->detail_message,
        config->detail_message_xml
      );
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniInputRulesValidatorDisable
 * Signature: ()V
 */
JNIEXPORT void JNICALL
Java_org_w21parser_W21ParserLoader_jniInputRulesValidatorDisable(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniInputRulesValidatorDisable,
    w21_disable_input_rules_validator(GET_W21_JNI_SOAP);
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniReadFromByteBuffer
 * Signature: (Ljava/nio/ByteBuffer;J)V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniReadFromByteBuffer
(
  JNIEnv *env, jobject thisObject, jobject bbXml, jlong limit
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniReadFromByteBuffer,
    JNI_READ_FROM_BYTE_BUFFER(jniReadFromByteBuffer, AutoDetect)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniReadBhaRunFromByteBuffer
 * Signature: (Ljava/nio/ByteBuffer;J)V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniReadBhaRunFromByteBuffer(
  JNIEnv *env, jobject thisObject, jobject bbXml, jlong limit
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniReadBhaRunFromByteBuffer,
    JNI_READ_FROM_BYTE_BUFFER(jniReadBhaRunFromByteBuffer, BhaRun)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniReadCementJobFromByteBuffer
 * Signature: (Ljava/nio/ByteBuffer;J)V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniReadCementJobFromByteBuffer(
  JNIEnv *env, jobject thisObject, jobject bbXml, jlong limit
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniReadCementJobFromByteBuffer,
    JNI_READ_FROM_BYTE_BUFFER(jniReadCementJobFromByteBuffer, CementJob)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniReadCementJobEvaluationFromByteBuffer
 * Signature: (Ljava/nio/ByteBuffer;J)V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniReadCementJobEvaluationFromByteBuffer(
  JNIEnv *env, jobject thisObject, jobject bbXml, jlong limit
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniReadCementJobEvaluationFromByteBuffer,
    JNI_READ_FROM_BYTE_BUFFER(jniReadCementJobEvaluationFromByteBuffer, CementJobEvaluation)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniReadChannelFromByteBuffer
 * Signature: (Ljava/nio/ByteBuffer;J)V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniReadChannelFromByteBuffer(
  JNIEnv *env, jobject thisObject, jobject bbXml, jlong limit
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniReadChannelFromByteBuffer,
    JNI_READ_FROM_BYTE_BUFFER(jniReadChannelFromByteBuffer, Channel)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniReadChannelKindFromByteBuffer
 * Signature: (Ljava/nio/ByteBuffer;J)V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniReadChannelKindFromByteBuffer(
  JNIEnv *env, jobject thisObject, jobject bbXml, jlong limit
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniReadChannelKindFromByteBuffer,
    JNI_READ_FROM_BYTE_BUFFER(jniReadChannelKindFromByteBuffer, ChannelKind)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniReadChannelKindDictionaryFromByteBuffer
 * Signature: (Ljava/nio/ByteBuffer;J)V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniReadChannelKindDictionaryFromByteBuffer(
  JNIEnv *env, jobject thisObject, jobject bbXml, jlong limit
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniReadChannelKindDictionaryFromByteBuffer,
    JNI_READ_FROM_BYTE_BUFFER(jniReadChannelKindDictionaryFromByteBuffer, ChannelKindDictionary)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniReadChannelSetFromByteBuffer
 * Signature: (Ljava/nio/ByteBuffer;J)V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniReadChannelSetFromByteBuffer(
  JNIEnv *env, jobject thisObject, jobject bbXml, jlong limit
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniReadChannelSetFromByteBuffer,
    JNI_READ_FROM_BYTE_BUFFER(jniReadChannelSetFromByteBuffer, ChannelSet)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniReadCuttingsGeologyFromByteBuffer
 * Signature: (Ljava/nio/ByteBuffer;J)V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniReadCuttingsGeologyFromByteBuffer(
  JNIEnv *env, jobject thisObject, jobject bbXml, jlong limit
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniReadCuttingsGeologyFromByteBuffer,
    JNI_READ_FROM_BYTE_BUFFER(jniReadCuttingsGeologyFromByteBuffer, CuttingsGeology)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniReadCuttingsGeologyIntervalFromByteBuffer
 * Signature: (Ljava/nio/ByteBuffer;J)V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniReadCuttingsGeologyIntervalFromByteBuffer(
  JNIEnv *env, jobject thisObject, jobject bbXml, jlong limit
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniReadCuttingsGeologyIntervalFromByteBuffer,
    JNI_READ_FROM_BYTE_BUFFER(jniReadCuttingsGeologyIntervalFromByteBuffer, CuttingsGeologyInterval)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniReadDepthRegImageFromByteBuffer
 * Signature: (Ljava/nio/ByteBuffer;J)V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniReadDepthRegImageFromByteBuffer(
  JNIEnv *env, jobject thisObject, jobject bbXml, jlong limit
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniReadDepthRegImageFromByteBuffer,
    JNI_READ_FROM_BYTE_BUFFER(jniReadDepthRegImageFromByteBuffer, DepthRegImage)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniReadDownholeComponentFromByteBuffer
 * Signature: (Ljava/nio/ByteBuffer;J)V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniReadDownholeComponentFromByteBuffer(
  JNIEnv *env, jobject thisObject, jobject bbXml, jlong limit
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniReadDownholeComponentFromByteBuffer,
    JNI_READ_FROM_BYTE_BUFFER(jniReadDownholeComponentFromByteBuffer, DownholeComponent)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniReadDrillReportFromByteBuffer
 * Signature: (Ljava/nio/ByteBuffer;J)V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniReadDrillReportFromByteBuffer(
  JNIEnv *env, jobject thisObject, jobject bbXml, jlong limit
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniReadDrillReportFromByteBuffer,
    JNI_READ_FROM_BYTE_BUFFER(jniReadDrillReportFromByteBuffer, DrillReport)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniReadErrorTermFromByteBuffer
 * Signature: (Ljava/nio/ByteBuffer;J)V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniReadErrorTermFromByteBuffer(
  JNIEnv *env, jobject thisObject, jobject bbXml, jlong limit
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniReadErrorTermFromByteBuffer,
    JNI_READ_FROM_BYTE_BUFFER(jniReadErrorTermFromByteBuffer, ErrorTerm)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniReadErrorTermDictionaryFromByteBuffer
 * Signature: (Ljava/nio/ByteBuffer;J)V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniReadErrorTermDictionaryFromByteBuffer(
  JNIEnv *env, jobject thisObject, jobject bbXml, jlong limit
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniReadErrorTermDictionaryFromByteBuffer,
    JNI_READ_FROM_BYTE_BUFFER(jniReadErrorTermDictionaryFromByteBuffer, ErrorTermDictionary)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniReadFluidsReportFromByteBuffer
 * Signature: (Ljava/nio/ByteBuffer;J)V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniReadFluidsReportFromByteBuffer(
  JNIEnv *env, jobject thisObject, jobject bbXml, jlong limit
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniReadFluidsReportFromByteBuffer,
    JNI_READ_FROM_BYTE_BUFFER(jniReadFluidsReportFromByteBuffer, FluidsReport)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniReadInterpretedGeologyFromByteBuffer
 * Signature: (Ljava/nio/ByteBuffer;J)V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniReadInterpretedGeologyFromByteBuffer(
  JNIEnv *env, jobject thisObject, jobject bbXml, jlong limit
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniReadInterpretedGeologyFromByteBuffer,
    JNI_READ_FROM_BYTE_BUFFER(jniReadInterpretedGeologyFromByteBuffer, InterpretedGeology)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniReadInterpretedGeologyIntervalFromByteBuffer
 * Signature: (Ljava/nio/ByteBuffer;J)V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniReadInterpretedGeologyIntervalFromByteBuffer(
  JNIEnv *env, jobject thisObject, jobject bbXml, jlong limit
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniReadInterpretedGeologyIntervalFromByteBuffer,
    JNI_READ_FROM_BYTE_BUFFER(jniReadInterpretedGeologyIntervalFromByteBuffer, InterpretedGeologyInterval)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniReadLogFromByteBuffer
 * Signature: (Ljava/nio/ByteBuffer;J)V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniReadLogFromByteBuffer(
  JNIEnv *env, jobject thisObject, jobject bbXml, jlong limit
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniReadLogFromByteBuffer,
    JNI_READ_FROM_BYTE_BUFFER(jniReadLogFromByteBuffer, Log)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniReadLoggingToolKindFromByteBuffer
 * Signature: (Ljava/nio/ByteBuffer;J)V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniReadLoggingToolKindFromByteBuffer(
  JNIEnv *env, jobject thisObject, jobject bbXml, jlong limit
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniReadLoggingToolKindFromByteBuffer,
    JNI_READ_FROM_BYTE_BUFFER(jniReadLoggingToolKindFromByteBuffer, LoggingToolKind)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniReadLoggingToolKindDictionaryFromByteBuffer
 * Signature: (Ljava/nio/ByteBuffer;J)V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniReadLoggingToolKindDictionaryFromByteBuffer(
  JNIEnv *env, jobject thisObject, jobject bbXml, jlong limit
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniReadLoggingToolKindDictionaryFromByteBuffer,
    JNI_READ_FROM_BYTE_BUFFER(jniReadLoggingToolKindDictionaryFromByteBuffer, LoggingToolKindDictionary)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniReadMudLogReportFromByteBuffer
 * Signature: (Ljava/nio/ByteBuffer;J)V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniReadMudLogReportFromByteBuffer(
  JNIEnv *env, jobject thisObject, jobject bbXml, jlong limit
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniReadMudLogReportFromByteBuffer,
    JNI_READ_FROM_BYTE_BUFFER(jniReadMudLogReportFromByteBuffer, MudLogReport)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniReadMudlogReportIntervalFromByteBuffer
 * Signature: (Ljava/nio/ByteBuffer;J)V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniReadMudlogReportIntervalFromByteBuffer(
  JNIEnv *env, jobject thisObject, jobject bbXml, jlong limit
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniReadMudlogReportIntervalFromByteBuffer,
    JNI_READ_FROM_BYTE_BUFFER(jniReadMudlogReportIntervalFromByteBuffer, MudlogReportInterval)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniReadOpsReportFromByteBuffer
 * Signature: (Ljava/nio/ByteBuffer;J)V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniReadOpsReportFromByteBuffer(
  JNIEnv *env, jobject thisObject, jobject bbXml, jlong limit
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniReadOpsReportFromByteBuffer,
    JNI_READ_FROM_BYTE_BUFFER(jniReadOpsReportFromByteBuffer, OpsReport)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniReadPPFGChannelFromByteBuffer
 * Signature: (Ljava/nio/ByteBuffer;J)V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniReadPPFGChannelFromByteBuffer(
  JNIEnv *env, jobject thisObject, jobject bbXml, jlong limit
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniReadPPFGChannelFromByteBuffer,
    JNI_READ_FROM_BYTE_BUFFER(jniReadPPFGChannelFromByteBuffer, PPFGChannel)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniReadPPFGChannelSetFromByteBuffer
 * Signature: (Ljava/nio/ByteBuffer;J)V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniReadPPFGChannelSetFromByteBuffer(
  JNIEnv *env, jobject thisObject, jobject bbXml, jlong limit
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniReadPPFGChannelSetFromByteBuffer,
    JNI_READ_FROM_BYTE_BUFFER(jniReadPPFGChannelSetFromByteBuffer, PPFGChannelSet)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniReadPPFGLogFromByteBuffer
 * Signature: (Ljava/nio/ByteBuffer;J)V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniReadPPFGLogFromByteBuffer(
  JNIEnv *env, jobject thisObject, jobject bbXml, jlong limit
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniReadPPFGLogFromByteBuffer,
    JNI_READ_FROM_BYTE_BUFFER(jniReadPPFGLogFromByteBuffer, PPFGLog)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniReadRigFromByteBuffer
 * Signature: (Ljava/nio/ByteBuffer;J)V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniReadRigFromByteBuffer(
  JNIEnv *env, jobject thisObject, jobject bbXml, jlong limit
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniReadRigFromByteBuffer,
    JNI_READ_FROM_BYTE_BUFFER(jniReadRigFromByteBuffer, Rig)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniReadRigUtilizationFromByteBuffer
 * Signature: (Ljava/nio/ByteBuffer;J)V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniReadRigUtilizationFromByteBuffer(
  JNIEnv *env, jobject thisObject, jobject bbXml, jlong limit
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniReadRigUtilizationFromByteBuffer,
    JNI_READ_FROM_BYTE_BUFFER(jniReadRigUtilizationFromByteBuffer, RigUtilization)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniReadRiskFromByteBuffer
 * Signature: (Ljava/nio/ByteBuffer;J)V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniReadRiskFromByteBuffer(
  JNIEnv *env, jobject thisObject, jobject bbXml, jlong limit
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniReadRiskFromByteBuffer,
    JNI_READ_FROM_BYTE_BUFFER(jniReadRiskFromByteBuffer, Risk)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniReadShowEvaluationFromByteBuffer
 * Signature: (Ljava/nio/ByteBuffer;J)V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniReadShowEvaluationFromByteBuffer(
  JNIEnv *env, jobject thisObject, jobject bbXml, jlong limit
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniReadShowEvaluationFromByteBuffer,
    JNI_READ_FROM_BYTE_BUFFER(jniReadShowEvaluationFromByteBuffer, ShowEvaluation)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniReadShowEvaluationIntervalFromByteBuffer
 * Signature: (Ljava/nio/ByteBuffer;J)V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniReadShowEvaluationIntervalFromByteBuffer(
  JNIEnv *env, jobject thisObject, jobject bbXml, jlong limit
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniReadShowEvaluationIntervalFromByteBuffer,
    JNI_READ_FROM_BYTE_BUFFER(jniReadShowEvaluationIntervalFromByteBuffer, ShowEvaluationInterval)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniReadStimJobFromByteBuffer
 * Signature: (Ljava/nio/ByteBuffer;J)V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniReadStimJobFromByteBuffer(
  JNIEnv *env, jobject thisObject, jobject bbXml, jlong limit
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniReadStimJobFromByteBuffer,
    JNI_READ_FROM_BYTE_BUFFER(jniReadStimJobFromByteBuffer, StimJob)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniReadStimJobStageFromByteBuffer
 * Signature: (Ljava/nio/ByteBuffer;J)V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniReadStimJobStageFromByteBuffer(
  JNIEnv *env, jobject thisObject, jobject bbXml, jlong limit
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniReadStimJobStageFromByteBuffer,
    JNI_READ_FROM_BYTE_BUFFER(jniReadStimJobStageFromByteBuffer, StimJobStage)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniReadStimPerforationClusterFromByteBuffer
 * Signature: (Ljava/nio/ByteBuffer;J)V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniReadStimPerforationClusterFromByteBuffer(
  JNIEnv *env, jobject thisObject, jobject bbXml, jlong limit
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniReadStimPerforationClusterFromByteBuffer,
    JNI_READ_FROM_BYTE_BUFFER(jniReadStimPerforationClusterFromByteBuffer, StimPerforationCluster)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniReadSurveyProgramFromByteBuffer
 * Signature: (Ljava/nio/ByteBuffer;J)V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniReadSurveyProgramFromByteBuffer(
  JNIEnv *env, jobject thisObject, jobject bbXml, jlong limit
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniReadSurveyProgramFromByteBuffer,
    JNI_READ_FROM_BYTE_BUFFER(jniReadSurveyProgramFromByteBuffer, SurveyProgram)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniReadTargetFromByteBuffer
 * Signature: (Ljava/nio/ByteBuffer;J)V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniReadTargetFromByteBuffer(
  JNIEnv *env, jobject thisObject, jobject bbXml, jlong limit
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniReadTargetFromByteBuffer,
    JNI_READ_FROM_BYTE_BUFFER(jniReadTargetFromByteBuffer, Target)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniReadToolErrorModelFromByteBuffer
 * Signature: (Ljava/nio/ByteBuffer;J)V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniReadToolErrorModelFromByteBuffer(
  JNIEnv *env, jobject thisObject, jobject bbXml, jlong limit
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniReadToolErrorModelFromByteBuffer,
    JNI_READ_FROM_BYTE_BUFFER(jniReadToolErrorModelFromByteBuffer, ToolErrorModel)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniReadToolErrorModelDictionaryFromByteBuffer
 * Signature: (Ljava/nio/ByteBuffer;J)V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniReadToolErrorModelDictionaryFromByteBuffer(
  JNIEnv *env, jobject thisObject, jobject bbXml, jlong limit
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniReadToolErrorModelDictionaryFromByteBuffer,
    JNI_READ_FROM_BYTE_BUFFER(jniReadToolErrorModelDictionaryFromByteBuffer, ToolErrorModelDictionary)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniReadTrajectoryFromByteBuffer
 * Signature: (Ljava/nio/ByteBuffer;J)V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniReadTrajectoryFromByteBuffer(
  JNIEnv *env, jobject thisObject, jobject bbXml, jlong limit
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniReadTrajectoryFromByteBuffer,
    JNI_READ_FROM_BYTE_BUFFER(jniReadTrajectoryFromByteBuffer, Trajectory)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniReadTrajectoryStationFromByteBuffer
 * Signature: (Ljava/nio/ByteBuffer;J)V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniReadTrajectoryStationFromByteBuffer(
  JNIEnv *env, jobject thisObject, jobject bbXml, jlong limit
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniReadTrajectoryStationFromByteBuffer,
    JNI_READ_FROM_BYTE_BUFFER(jniReadTrajectoryStationFromByteBuffer, TrajectoryStation)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniReadTubularFromByteBuffer
 * Signature: (Ljava/nio/ByteBuffer;J)V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniReadTubularFromByteBuffer(
  JNIEnv *env, jobject thisObject, jobject bbXml, jlong limit
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniReadTubularFromByteBuffer,
    JNI_READ_FROM_BYTE_BUFFER(jniReadTubularFromByteBuffer, Tubular)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniReadWeightingFunctionFromByteBuffer
 * Signature: (Ljava/nio/ByteBuffer;J)V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniReadWeightingFunctionFromByteBuffer(
  JNIEnv *env, jobject thisObject, jobject bbXml, jlong limit
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniReadWeightingFunctionFromByteBuffer,
    JNI_READ_FROM_BYTE_BUFFER(jniReadWeightingFunctionFromByteBuffer, WeightingFunction)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniReadWeightingFunctionDictionaryFromByteBuffer
 * Signature: (Ljava/nio/ByteBuffer;J)V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniReadWeightingFunctionDictionaryFromByteBuffer(
  JNIEnv *env, jobject thisObject, jobject bbXml, jlong limit
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniReadWeightingFunctionDictionaryFromByteBuffer,
    JNI_READ_FROM_BYTE_BUFFER(jniReadWeightingFunctionDictionaryFromByteBuffer, WeightingFunctionDictionary)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniReadWellFromByteBuffer
 * Signature: (Ljava/nio/ByteBuffer;J)V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniReadWellFromByteBuffer(
  JNIEnv *env, jobject thisObject, jobject bbXml, jlong limit
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniReadWellFromByteBuffer,
    JNI_READ_FROM_BYTE_BUFFER(jniReadWellFromByteBuffer, Well)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniReadWellboreFromByteBuffer
 * Signature: (Ljava/nio/ByteBuffer;J)V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniReadWellboreFromByteBuffer(
  JNIEnv *env, jobject thisObject, jobject bbXml, jlong limit
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniReadWellboreFromByteBuffer,
    JNI_READ_FROM_BYTE_BUFFER(jniReadWellboreFromByteBuffer, Wellbore)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniReadWellboreCompletionFromByteBuffer
 * Signature: (Ljava/nio/ByteBuffer;J)V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniReadWellboreCompletionFromByteBuffer(
  JNIEnv *env, jobject thisObject, jobject bbXml, jlong limit
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniReadWellboreCompletionFromByteBuffer,
    JNI_READ_FROM_BYTE_BUFFER(jniReadWellboreCompletionFromByteBuffer, WellboreCompletion)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniReadWellboreGeologyFromByteBuffer
 * Signature: (Ljava/nio/ByteBuffer;J)V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniReadWellboreGeologyFromByteBuffer(
  JNIEnv *env, jobject thisObject, jobject bbXml, jlong limit
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniReadWellboreGeologyFromByteBuffer,
    JNI_READ_FROM_BYTE_BUFFER(jniReadWellboreGeologyFromByteBuffer, WellboreGeology)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniReadWellboreGeometryFromByteBuffer
 * Signature: (Ljava/nio/ByteBuffer;J)V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniReadWellboreGeometryFromByteBuffer(
  JNIEnv *env, jobject thisObject, jobject bbXml, jlong limit
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniReadWellboreGeometryFromByteBuffer,
    JNI_READ_FROM_BYTE_BUFFER(jniReadWellboreGeometryFromByteBuffer, WellboreGeometry)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniReadWellboreGeometrySectionFromByteBuffer
 * Signature: (Ljava/nio/ByteBuffer;J)V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniReadWellboreGeometrySectionFromByteBuffer(
  JNIEnv *env, jobject thisObject, jobject bbXml, jlong limit
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniReadWellboreGeometrySectionFromByteBuffer,
    JNI_READ_FROM_BYTE_BUFFER(jniReadWellboreGeometrySectionFromByteBuffer, WellboreGeometrySection)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniReadWellboreMarkerFromByteBuffer
 * Signature: (Ljava/nio/ByteBuffer;J)V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniReadWellboreMarkerFromByteBuffer(
  JNIEnv *env, jobject thisObject, jobject bbXml, jlong limit
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniReadWellboreMarkerFromByteBuffer,
    JNI_READ_FROM_BYTE_BUFFER(jniReadWellboreMarkerFromByteBuffer, WellboreMarker)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniReadWellboreMarkerSetFromByteBuffer
 * Signature: (Ljava/nio/ByteBuffer;J)V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniReadWellboreMarkerSetFromByteBuffer(
  JNIEnv *env, jobject thisObject, jobject bbXml, jlong limit
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniReadWellboreMarkerSetFromByteBuffer,
    JNI_READ_FROM_BYTE_BUFFER(jniReadWellboreMarkerSetFromByteBuffer, WellboreMarkerSet)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniReadWellCMLedgerFromByteBuffer
 * Signature: (Ljava/nio/ByteBuffer;J)V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniReadWellCMLedgerFromByteBuffer(
  JNIEnv *env, jobject thisObject, jobject bbXml, jlong limit
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniReadWellCMLedgerFromByteBuffer,
    JNI_READ_FROM_BYTE_BUFFER(jniReadWellCMLedgerFromByteBuffer, WellCMLedger)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniReadWellCompletionFromByteBuffer
 * Signature: (Ljava/nio/ByteBuffer;J)V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniReadWellCompletionFromByteBuffer(
  JNIEnv *env, jobject thisObject, jobject bbXml, jlong limit
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniReadWellCompletionFromByteBuffer,
    JNI_READ_FROM_BYTE_BUFFER(jniReadWellCompletionFromByteBuffer, WellCompletion)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniW21Recycle
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniW21Recycle
(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniW21Recycle,
    err = SET_SECURE_DIRECT_BUFFER_TO_BSON;
    if (err == 0) {
      if ((err = SET_SECURE_DIRECT_BUFFER_TO_JSON) == 0) {
        locker->isResourceStatsReadFinished = false;
        locker->isResourceStatsParseFinished = false;
        locker->isResourceStatsParseJsonFinished = false;
        w21_recycle(GET_W21_JNI_SOAP);
      } else
        throw_w21_exception(env, "jniW21Recycle: Could not set DirectBuffer secure to JSON", err, NULL, NULL);
    } else
      throw_w21_exception(env, "jniW21Recycle: Could not set DirectBuffer secure to BSON", err, NULL, NULL);
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParse
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParse(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParse,
    W21_JNI_PARSE(jniParse, AutoDetect) \
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParseBhaRun
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParseBhaRun(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParseBhaRun,
    W21_JNI_PARSE(jniParseBhaRun, BhaRun)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParseCementJob
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParseCementJob(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParseCementJob,
    W21_JNI_PARSE(jniParseCementJob, CementJob)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParseCementJobEvaluation
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParseCementJobEvaluation(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParseCementJobEvaluation,
    W21_JNI_PARSE(jniParseCementJobEvaluation, CementJobEvaluation)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParseChannel
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParseChannel(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParseChannel,
    W21_JNI_PARSE(jniParseChannel, Channel)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParseChannelKind
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParseChannelKind(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParseChannelKind,
    W21_JNI_PARSE(jniParseChannelKind, ChannelKind)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParseChannelKindDictionary
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParseChannelKindDictionary(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParseChannelKindDictionary,
    W21_JNI_PARSE(jniParseChannelKindDictionary, ChannelKindDictionary)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParseChannelSet
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParseChannelSet(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParseChannelSet,
    W21_JNI_PARSE(jniParseChannelSet, ChannelSet)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParseCuttingsGeology
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParseCuttingsGeology(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParseCuttingsGeology,
    W21_JNI_PARSE(jniParseCuttingsGeology, CuttingsGeology)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParseCuttingsGeologyInterval
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParseCuttingsGeologyInterval(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParseCuttingsGeologyInterval,
    W21_JNI_PARSE(jniParseCuttingsGeologyInterval, CuttingsGeologyInterval)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParseDepthRegImage
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParseDepthRegImage(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParseDepthRegImage,
    W21_JNI_PARSE(jniParseDepthRegImage, DepthRegImage)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParseDownholeComponent
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParseDownholeComponent(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParseDownholeComponent,
    W21_JNI_PARSE(jniParseDownholeComponent, DownholeComponent)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParseDrillReport
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParseDrillReport(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParseDrillReport,
    W21_JNI_PARSE(jniParseDrillReport, DrillReport)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParseErrorTerm
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParseErrorTerm(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParseErrorTerm,
    W21_JNI_PARSE(jniParseErrorTerm, ErrorTerm)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParseErrorTermDictionary
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParseErrorTermDictionary(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParseErrorTermDictionary,
    W21_JNI_PARSE(jniParseErrorTermDictionary, ErrorTermDictionary)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParseFluidsReport
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParseFluidsReport(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParseFluidsReport,
    W21_JNI_PARSE(jniParseFluidsReport, FluidsReport)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParseInterpretedGeology
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParseInterpretedGeology(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParseInterpretedGeology,
    W21_JNI_PARSE(jniParseInterpretedGeology, InterpretedGeology)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParseInterpretedGeologyInterval
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParseInterpretedGeologyInterval(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParseInterpretedGeologyInterval,
    W21_JNI_PARSE(jniParseInterpretedGeologyInterval, InterpretedGeologyInterval)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParseLog
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParseLog(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParseLog,
    W21_JNI_PARSE(jniParseLog, Log)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParseLoggingToolKind
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParseLoggingToolKind(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParseLoggingToolKind,
    W21_JNI_PARSE(jniParseLoggingToolKind, LoggingToolKind)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParseLoggingToolKindDictionary
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParseLoggingToolKindDictionary(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParseLoggingToolKindDictionary,
    W21_JNI_PARSE(jniParseLoggingToolKindDictionary, LoggingToolKindDictionary)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParseMudLogReport
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParseMudLogReport(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParseMudLogReport,
    W21_JNI_PARSE(jniParseMudLogReport, MudLogReport)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParseMudlogReportInterval
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParseMudlogReportInterval(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParseMudlogReportInterval,
    W21_JNI_PARSE(jniParseMudlogReportInterval, MudlogReportInterval)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParseOpsReport
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParseOpsReport(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParseOpsReport,
    W21_JNI_PARSE(jniParseOpsReport, OpsReport)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParsePPFGChannel
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParsePPFGChannel(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParsePPFGChannel,
    W21_JNI_PARSE(jniParsePPFGChannel, PPFGChannel)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParsePPFGChannelSet
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParsePPFGChannelSet(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParsePPFGChannelSet,
    W21_JNI_PARSE(jniParsePPFGChannelSet, PPFGChannelSet)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParsePPFGLog
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParsePPFGLog(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParsePPFGLog,
    W21_JNI_PARSE(jniParsePPFGLog, PPFGLog)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParseRig
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParseRig(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParseRig,
    W21_JNI_PARSE(jniParseRig, Rig)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParseRigUtilization
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParseRigUtilization(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParseRigUtilization,
    W21_JNI_PARSE(jniParseRigUtilization, RigUtilization)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParseRisk
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParseRisk(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParseRisk,
    W21_JNI_PARSE(jniParseRisk, Risk)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParseShowEvaluation
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParseShowEvaluation(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParseShowEvaluation,
    W21_JNI_PARSE(jniParseShowEvaluation, ShowEvaluation)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParseShowEvaluationInterval
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParseShowEvaluationInterval(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParseShowEvaluationInterval,
    W21_JNI_PARSE(jniParseShowEvaluationInterval, ShowEvaluationInterval)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParseStimJob
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParseStimJob(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParseStimJob,
    W21_JNI_PARSE(jniParseStimJob, StimJob)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParseStimJobStage
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParseStimJobStage(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParseStimJobStage,
    W21_JNI_PARSE(jniParseStimJobStage, StimJobStage)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParseStimPerforationCluster
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParseStimPerforationCluster(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParseStimPerforationCluster,
    W21_JNI_PARSE(jniParseStimPerforationCluster, StimPerforationCluster)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParseSurveyProgram
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParseSurveyProgram(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParseSurveyProgram,
    W21_JNI_PARSE(jniParseSurveyProgram, SurveyProgram)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParseTarget
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParseTarget(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParseTarget,
    W21_JNI_PARSE(jniParseTarget, Target)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParseToolErrorModel
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParseToolErrorModel(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParseToolErrorModel,
    W21_JNI_PARSE(jniParseToolErrorModel, ToolErrorModel)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParseToolErrorModelDictionary
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParseToolErrorModelDictionary(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParseToolErrorModelDictionary,
    W21_JNI_PARSE(jniParseToolErrorModelDictionary, ToolErrorModelDictionary)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParseTrajectory
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParseTrajectory(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParseTrajectory,
    W21_JNI_PARSE(jniParseTrajectory, Trajectory)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParseTrajectoryStation
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParseTrajectoryStation(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParseTrajectoryStation,
    W21_JNI_PARSE(jniParseTrajectoryStation, TrajectoryStation)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParseTubular
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParseTubular(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParseTubular,
    W21_JNI_PARSE(jniParseTubular, Tubular)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParseWeightingFunction
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParseWeightingFunction(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParseWeightingFunction,
    W21_JNI_PARSE(jniParseWeightingFunction, WeightingFunction)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParseWeightingFunctionDictionary
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParseWeightingFunctionDictionary(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParseWeightingFunctionDictionary,
    W21_JNI_PARSE(jniParseWeightingFunctionDictionary, WeightingFunctionDictionary)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParseWell
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParseWell(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParseWell,
    W21_JNI_PARSE(jniParseWell, Well)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParseWellbore
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParseWellbore(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParseWellbore,
    W21_JNI_PARSE(jniParseWellbore, Wellbore)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParseWellboreCompletion
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParseWellboreCompletion(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParseWellboreCompletion,
    W21_JNI_PARSE(jniParseWellboreCompletion, WellboreCompletion)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParseWellboreGeology
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParseWellboreGeology(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParseWellboreGeology,
    W21_JNI_PARSE(jniParseWellboreGeology, WellboreGeology)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParseWellboreGeometry
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParseWellboreGeometry(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParseWellboreGeometry,
    W21_JNI_PARSE(jniParseWellboreGeometry, WellboreGeometry)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParseWellboreGeometrySection
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParseWellboreGeometrySection(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParseWellboreGeometrySection,
    W21_JNI_PARSE(jniParseWellboreGeometrySection, WellboreGeometrySection)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParseWellboreMarker
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParseWellboreMarker(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParseWellboreMarker,
    W21_JNI_PARSE(jniParseWellboreMarker, WellboreMarker)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParseWellboreMarkerSet
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParseWellboreMarkerSet(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParseWellboreMarkerSet,
    W21_JNI_PARSE(jniParseWellboreMarkerSet, WellboreMarkerSet)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParseWellCMLedger
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParseWellCMLedger(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParseWellCMLedger,
    W21_JNI_PARSE(jniParseWellCMLedger, WellCMLedger)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParseWellCompletion
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParseWellCompletion(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParseWellCompletion,
    W21_JNI_PARSE(jniParseWellCompletion, WellCompletion)
  )
}

//aqui

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParseJson
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParseJson(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParseJson,
    W21_JNI_PARSE_JSON(jniParseJson, AutoDetect)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParseJsonBhaRun
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParseJsonBhaRun(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParseJsonBhaRun,
    W21_JNI_PARSE_JSON(jniParseJsonBhaRun, BhaRun)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParseJsonCementJob
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParseJsonCementJob(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParseJsonCementJob,
    W21_JNI_PARSE_JSON(jniParseJsonCementJob, CementJob)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParseJsonCementJobEvaluation
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParseJsonCementJobEvaluation(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParseJsonCementJobEvaluation,
    W21_JNI_PARSE_JSON(jniParseJsonCementJobEvaluation, CementJobEvaluation)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParseJsonChannel
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParseJsonChannel(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParseJsonChannel,
    W21_JNI_PARSE_JSON(jniParseJsonChannel, Channel)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParseJsonChannelKind
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParseJsonChannelKind(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParseJsonChannelKind,
    W21_JNI_PARSE_JSON(jniParseJsonChannelKind, ChannelKind)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParseJsonChannelKindDictionary
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParseJsonChannelKindDictionary(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParseJsonChannelKindDictionary,
    W21_JNI_PARSE_JSON(jniParseJsonChannelKindDictionary, ChannelKindDictionary)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParseJsonChannelSet
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParseJsonChannelSet(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParseJsonChannelSet,
    W21_JNI_PARSE_JSON(jniParseJsonChannelSet, ChannelSet)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParseJsonCuttingsGeology
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParseJsonCuttingsGeology(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParseJsonCuttingsGeology,
    W21_JNI_PARSE_JSON(jniParseJsonCuttingsGeology, CuttingsGeology)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParseJsonCuttingsGeologyInterval
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParseJsonCuttingsGeologyInterval(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParseJsonCuttingsGeologyInterval,
    W21_JNI_PARSE_JSON(jniParseJsonCuttingsGeologyInterval, CuttingsGeologyInterval)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParseJsonDepthRegImage
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParseJsonDepthRegImage(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParseJsonDepthRegImage,
    W21_JNI_PARSE_JSON(jniParseJsonDepthRegImage, DepthRegImage)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParseJsonDownholeComponent
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParseJsonDownholeComponent(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParseJsonDownholeComponent,
    W21_JNI_PARSE_JSON(jniParseJsonDownholeComponent, DownholeComponent)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParseJsonDrillReport
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParseJsonDrillReport(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParseJsonDrillReport,
    W21_JNI_PARSE_JSON(jniParseJsonDrillReport, DrillReport)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParseJsonErrorTerm
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParseJsonErrorTerm(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParseJsonErrorTerm,
    W21_JNI_PARSE_JSON(jniParseJsonErrorTerm, ErrorTerm)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParseJsonErrorTermDictionary
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParseJsonErrorTermDictionary(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParseJsonErrorTermDictionary,
    W21_JNI_PARSE_JSON(jniParseJsonErrorTermDictionary, ErrorTermDictionary)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParseJsonFluidsReport
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParseJsonFluidsReport(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParseJsonFluidsReport,
    W21_JNI_PARSE_JSON(jniParseJsonFluidsReport, FluidsReport)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParseJsonInterpretedGeology
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParseJsonInterpretedGeology(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParseJsonInterpretedGeology,
    W21_JNI_PARSE_JSON(jniParseJsonInterpretedGeology, InterpretedGeology)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParseJsonInterpretedGeologyInterval
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParseJsonInterpretedGeologyInterval(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParseJsonInterpretedGeologyInterval,
    W21_JNI_PARSE_JSON(jniParseJsonInterpretedGeologyInterval, InterpretedGeologyInterval)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParseJsonLog
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParseJsonLog(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParseJsonLog,
    W21_JNI_PARSE_JSON(jniParseJsonLog, Log)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParseJsonLoggingToolKind
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParseJsonLoggingToolKind(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParseJsonLoggingToolKind,
    W21_JNI_PARSE_JSON(jniParseJsonLoggingToolKind, LoggingToolKind)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParseJsonLoggingToolKindDictionary
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParseJsonLoggingToolKindDictionary(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParseJsonLoggingToolKindDictionary,
    W21_JNI_PARSE_JSON(jniParseJsonLoggingToolKindDictionary, LoggingToolKindDictionary)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParseJsonMudLogReport
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParseJsonMudLogReport(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParseJsonMudLogReport,
    W21_JNI_PARSE_JSON(jniParseJsonMudLogReport, MudLogReport)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParseJsonMudlogReportInterval
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParseJsonMudlogReportInterval(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParseJsonMudlogReportInterval,
    W21_JNI_PARSE_JSON(jniParseJsonMudlogReportInterval, MudlogReportInterval)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParseJsonOpsReport
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParseJsonOpsReport(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParseJsonOpsReport,
    W21_JNI_PARSE_JSON(jniParseJsonOpsReport, OpsReport)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParseJsonPPFGChannel
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParseJsonPPFGChannel(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParseJsonPPFGChannel,
    W21_JNI_PARSE_JSON(jniParseJsonPPFGChannel, PPFGChannel)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParseJsonPPFGChannelSet
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParseJsonPPFGChannelSet(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParseJsonPPFGChannelSet,
    W21_JNI_PARSE_JSON(jniParseJsonPPFGChannelSet, PPFGChannelSet)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParseJsonPPFGLog
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParseJsonPPFGLog(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParseJsonPPFGLog,
    W21_JNI_PARSE_JSON(jniParseJsonPPFGLog, PPFGLog)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParseJsonRig
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParseJsonRig(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParseJsonRig,
    W21_JNI_PARSE_JSON(jniParseJsonRig, Rig)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParseJsonRigUtilization
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParseJsonRigUtilization(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParseJsonRigUtilization,
    W21_JNI_PARSE_JSON(jniParseJsonRigUtilization, RigUtilization)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParseJsonRisk
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParseJsonRisk(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParseJsonRisk,
    W21_JNI_PARSE_JSON(jniParseJsonRisk, Risk)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParseJsonShowEvaluation
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParseJsonShowEvaluation(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParseJsonShowEvaluation,
    W21_JNI_PARSE_JSON(jniParseJsonShowEvaluation, ShowEvaluation)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParseJsonShowEvaluationInterval
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParseJsonShowEvaluationInterval(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParseJsonShowEvaluationInterval,
    W21_JNI_PARSE_JSON(jniParseJsonShowEvaluationInterval, ShowEvaluationInterval)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParseJsonStimJob
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParseJsonStimJob(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParseJsonStimJob,
    W21_JNI_PARSE_JSON(jniParseJsonStimJob, StimJob)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParseJsonStimJobStage
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParseJsonStimJobStage(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParseJsonStimJobStage,
    W21_JNI_PARSE_JSON(jniParseJsonStimJobStage, StimJobStage)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParseJsonStimPerforationCluster
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParseJsonStimPerforationCluster(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParseJsonStimPerforationCluster,
    W21_JNI_PARSE_JSON(jniParseJsonStimPerforationCluster, StimPerforationCluster)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParseJsonSurveyProgram
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParseJsonSurveyProgram(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParseJsonSurveyProgram,
    W21_JNI_PARSE_JSON(jniParseJsonSurveyProgram, SurveyProgram)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParseJsonTarget
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParseJsonTarget(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParseJsonTarget,
    W21_JNI_PARSE_JSON(jniParseJsonTarget, Target)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParseJsonToolErrorModel
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParseJsonToolErrorModel(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParseJsonToolErrorModel,
    W21_JNI_PARSE_JSON(jniParseJsonToolErrorModel, ToolErrorModel)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParseJsonToolErrorModelDictionary
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParseJsonToolErrorModelDictionary(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParseJsonToolErrorModelDictionary,
    W21_JNI_PARSE_JSON(jniParseJsonToolErrorModelDictionary, ToolErrorModelDictionary)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParseJsonTrajectory
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParseJsonTrajectory(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParseJsonTrajectory,
    W21_JNI_PARSE_JSON(jniParseJsonTrajectory, Trajectory)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParseJsonTrajectoryStation
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParseJsonTrajectoryStation(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParseJsonTrajectoryStation,
    W21_JNI_PARSE_JSON(jniParseJsonTrajectoryStation, TrajectoryStation)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParseJsonTubular
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParseJsonTubular(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParseJsonTubular,
    W21_JNI_PARSE_JSON(jniParseJsonTubular, Tubular)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParseJsonWeightingFunction
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParseJsonWeightingFunction(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParseJsonWeightingFunction,
    W21_JNI_PARSE_JSON(jniParseJsonWeightingFunction, WeightingFunction)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParseJsonWeightingFunctionDictionary
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParseJsonWeightingFunctionDictionary(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParseJsonWeightingFunctionDictionary,
    W21_JNI_PARSE_JSON(jniParseJsonWeightingFunctionDictionary, WeightingFunctionDictionary)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParseJsonWell
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParseJsonWell(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParseJsonWell,
    W21_JNI_PARSE_JSON(jniParseJsonWell, Well)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParseJsonWellbore
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParseJsonWellbore(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParseJsonWellbore,
    W21_JNI_PARSE_JSON(jniParseJsonWellbore, Wellbore)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParseJsonWellboreCompletion
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParseJsonWellboreCompletion(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParseJsonWellboreCompletion,
    W21_JNI_PARSE_JSON(jniParseJsonWellboreCompletion, WellboreCompletion)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParseJsonWellboreGeology
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParseJsonWellboreGeology(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParseJsonWellboreGeology,
    W21_JNI_PARSE_JSON(jniParseJsonWellboreGeology, WellboreGeology)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParseJsonWellboreGeometry
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParseJsonWellboreGeometry(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParseJsonWellboreGeometry,
    W21_JNI_PARSE_JSON(jniParseJsonWellboreGeometry, WellboreGeometry)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParseJsonWellboreGeometrySection
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParseJsonWellboreGeometrySection(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParseJsonWellboreGeometrySection,
    W21_JNI_PARSE_JSON(jniParseJsonWellboreGeometrySection, WellboreGeometrySection)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParseJsonWellboreMarker
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParseJsonWellboreMarker(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParseJsonWellboreMarker,
    W21_JNI_PARSE_JSON(jniParseJsonWellboreMarker, WellboreMarker)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParseJsonWellboreMarkerSet
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParseJsonWellboreMarkerSet(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParseJsonWellboreMarkerSet,
    W21_JNI_PARSE_JSON(jniParseJsonWellboreMarkerSet, WellboreMarkerSet)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParseJsonWellCMLedger
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParseJsonWellCMLedger(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParseJsonWellCMLedger,
    W21_JNI_PARSE_JSON(jniParseJsonWellCMLedger, WellCMLedger)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniParseJsonWellCompletion
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_w21parser_W21ParserLoader_jniParseJsonWellCompletion(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_EXECUTE_ACTION_VOID(
    jniParseJsonWellCompletion,
    W21_JNI_PARSE_JSON(jniParseJsonWellCompletion, WellCompletion)
  )
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniBeginRead
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_w21parser_W21ParserLoader_jniBeginRead(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_Locker_List *locker;
  int err = jni_get_handler(&locker, env, thisObject);
  if (err == 0) {
    if (locker)
      return pthread_mutex_lock(&locker->lock);

    return JNI_W21_DIRECT_BUFFER_LOCKER_BEGIN_UNINITIALIZED;
  }

  return err;
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniEndRead
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_w21parser_W21ParserLoader_jniEndRead(
  JNIEnv *env, jobject thisObject
)
{
  JNI_W21_Locker_List *locker;
  int err = jni_get_handler(&locker, env, thisObject);
  if (err == 0) {
    if (locker)
      return pthread_mutex_unlock(&locker->lock);

    return JNI_W21_DIRECT_BUFFER_LOCKER_END_UNINITIALIZED;
  }

  return err;
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniStatReadTotalCycles
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_org_w21parser_W21ParserLoader_jniStatReadTotalCycles(
  JNIEnv *env, jobject thisObject
)
{
  W21_JNI_STAT_READ(jniStatReadTotalCycles, in_total_cycles)
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniStatReadTotalNanos
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_org_w21parser_W21ParserLoader_jniStatReadTotalNanos(
  JNIEnv *env, jobject thisObject
)
{
  W21_JNI_STAT_READ(jniStatReadTotalNanos, in_total_nanos)
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniStatReadMem
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_org_w21parser_W21ParserLoader_jniStatReadMem(
  JNIEnv *env, jobject thisObject
)
{
  W21_JNI_STAT_READ(jniStatReadMem, in_mem_delta)
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniStatParseTotalCycles
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_org_w21parser_W21ParserLoader_jniStatParseTotalCycles(
  JNIEnv *env, jobject thisObject
)
{
  W21_JNI_STAT_PARSE(jniStatParseTotalCycles, in_parse_total_cycles)
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniStatParseTotalNanos
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_org_w21parser_W21ParserLoader_jniStatParseTotalNanos(
  JNIEnv *env, jobject thisObject
)
{
  W21_JNI_STAT_PARSE(jniStatParseTotalNanos, in_parse_total_nanos)
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniStatParseMem
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_org_w21parser_W21ParserLoader_jniStatParseMem(
  JNIEnv *env, jobject thisObject
)
{
  W21_JNI_STAT_PARSE(jniStatParseMem, in_parse_mem_delta)
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniStatParseJsonTotalCycles
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_org_w21parser_W21ParserLoader_jniStatParseJsonTotalCycles(
  JNIEnv *env, jobject thisObject
)
{
  W21_JNI_STAT_PARSE_JSON(jniStatParseJsonTotalCycles, in_parse_json_total_cycles)
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniStatParseJsonTotalNanos
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_org_w21parser_W21ParserLoader_jniStatParseJsonTotalNanos(
  JNIEnv *env, jobject thisObject
)
{
  W21_JNI_STAT_PARSE_JSON(jniStatParseJsonTotalNanos, in_parse_json_total_nanos)
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniStatParseJsonMem
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_org_w21parser_W21ParserLoader_jniStatParseJsonMem(
  JNIEnv *env, jobject thisObject
)
{
  W21_JNI_STAT_PARSE_JSON(jniStatParseJsonMem, in_parse_json_mem_delta)
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniStatCosts
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_w21parser_W21ParserLoader_jniStatCosts(
  JNIEnv *env, jobject thisObject
)
{
  W21_JNI_DOC_STAT_PARSE(jniStatCosts, costs)
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniStatStrings
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_w21parser_W21ParserLoader_jniStatStrings(
  JNIEnv *env, jobject thisObject
)
{
  W21_JNI_DOC_STAT_PARSE(jniStatStrings, strings)
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniStatShorts
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_w21parser_W21ParserLoader_jniStatShorts(
  JNIEnv *env, jobject thisObject
)
{
  W21_JNI_DOC_STAT_PARSE(jniStatShorts, shorts)
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniStatInts
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_w21parser_W21ParserLoader_jniStatInts(
  JNIEnv *env, jobject thisObject
)
{
  W21_JNI_DOC_STAT_PARSE(jniStatInts, ints)
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniStatLong64s
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_w21parser_W21ParserLoader_jniStatLong64s(
  JNIEnv *env, jobject thisObject
)
{
  W21_JNI_DOC_STAT_PARSE(jniStatLong64s, long64s)
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniStatEnums
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_w21parser_W21ParserLoader_jniStatEnums(
  JNIEnv *env, jobject thisObject
)
{
  W21_JNI_DOC_STAT_PARSE(jniStatEnums, enums)
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniStatArrays
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_w21parser_W21ParserLoader_jniStatArrays(
  JNIEnv *env, jobject thisObject
)
{
  W21_JNI_DOC_STAT_PARSE(jniStatArrays, arrays)
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniStatBooleans
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_w21parser_W21ParserLoader_jniStatBooleans(
  JNIEnv *env, jobject thisObject
)
{
  W21_JNI_DOC_STAT_PARSE(jniStatBooleans, booleans)
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniStatDoubles
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_w21parser_W21ParserLoader_jniStatDoubles(
  JNIEnv *env, jobject thisObject
)
{
  W21_JNI_DOC_STAT_PARSE(jniStatDoubles, doubles)
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniStatDateTimes
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_w21parser_W21ParserLoader_jniStatDateTimes(
  JNIEnv *env, jobject thisObject
)
{
  W21_JNI_DOC_STAT_PARSE(jniStatDateTimes, date_times)
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniStatMeasures
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_w21parser_W21ParserLoader_jniStatMeasures(
  JNIEnv *env, jobject thisObject
)
{
  W21_JNI_DOC_STAT_PARSE(jniStatMeasures, measures)
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniStatEventTypes
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_w21parser_W21ParserLoader_jniStatEventTypes(
  JNIEnv *env, jobject thisObject
)
{
  W21_JNI_DOC_STAT_PARSE(jniStatEventTypes, event_types)
}

/*
 * Class:     org_w21parser_W21ParserLoader
 * Method:    jniStatTotal
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_w21parser_W21ParserLoader_jniStatTotal(
  JNIEnv *env, jobject thisObject
)
{
  W21_JNI_DOC_STAT_PARSE(jniStatTotal, total)
}

#undef W21_PARSER_MAGIC
