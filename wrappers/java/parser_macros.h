#ifndef PARSER_MACROS_H
 #define PARSER_MACROS_H

#define W21PARSER_TAG "[w21parser]"
#define CONST_ERROR_STR(msg) (W21PARSER_TAG " " msg "\n")

#define JNI_W21_EXECUTE_ACTION_VOID(func, textCode) \
  JNI_W21_Locker_List *locker; \
  int err = jni_get_handler(&locker, env, thisObject); \
  if (err == 0) { \
    if (locker) { \
      if ((err = pthread_mutex_lock(&locker->lock)) == 0) { \
        if ((err = jni_check_consistence(locker)) == 0) { \
          enum JNI_W21_ParserContext_status_e status = __atomic_load_n(&locker->thread_ctx->status, __ATOMIC_SEQ_CST); \
  \
          if (status == JNI_W21_Init) { \
            textCode \
          } else { \
            fprintf(stderr, CONST_ERROR_STR(#func " status (%d) not init"), (int)status); \
            throw_21_default_exception(env, #func " status not init"); \
          } \
        } else { \
          fprintf(stderr, CONST_ERROR_STR(#func " inconsistence: Unable to execute jni_check_consistence %d"), err); \
          throw_w21_exception(env, #func " inconsistence: Unable to execute", err, NULL, NULL); \
        } \
        goto func##_unlock; \
      } else { \
        fprintf(stderr, CONST_ERROR_STR(#func ": Unexpected unable to unlock. thread_mutex_lock: %d. Ignoring"), err); \
        throw_w21_exception(env, #func ": Unexpected unable to unlock", err, NULL, NULL); \
      } \
    } else { \
      fprintf(stderr, CONST_ERROR_STR(#func " unexpected: locker not found or not initialized")); \
      throw_21_default_exception(env, #func " unexpected: locker not found or not initialized. Unable to execute"); \
    } \
  } else { \
    fprintf(stderr, CONST_ERROR_STR(#func " unexpected: Unable to load C pointer at jni_get_handler: %d. Ignoring ..."), err); \
    throw_w21_exception(env, #func " unexpected: Unable to load C pointer", err, NULL, NULL); \
  } \
\
  return; \
func##_unlock: \
  bool close = (__atomic_load_n(&locker->thread_ctx->status, __ATOMIC_SEQ_CST) == JNI_w21_CloseRequest); \
  if ((err = pthread_mutex_unlock(&locker->lock)) == 0) { \
    if (close) \
      if ((err = (int)Java_org_w21parser_W21ParserLoader_jniClose(env, thisObject))) \
        fprintf(stderr, CONST_ERROR_STR(#func " unexpected: Call jniClose = %d"), err); \
\
    return; \
  } \
\
  fprintf(stderr, CONST_ERROR_STR(#func " unexpected: unable to unlock thread_mutex_unlock: %d. Ignoring ..."), err); \
  return;

  // Don't throw exception here. Maybe thrown later
  //throw_21_default_exception(env, #func " unexpected: unable to unlock");


#define JNI_W21_EXECUTE_ACTION_RET(func, textCode, ret) \
  JNI_W21_Locker_List *locker; \
  int err = jni_get_handler(&locker, env, thisObject); \
  if (err == 0) { \
    if (locker) { \
      if ((err = pthread_mutex_lock(&locker->lock)) == 0) { \
        if ((err = jni_check_consistence(locker)) == 0) { \
          enum JNI_W21_ParserContext_status_e status = __atomic_load_n(&locker->thread_ctx->status, __ATOMIC_SEQ_CST); \
  \
          if (status == JNI_W21_Init) { \
            textCode \
          } else { \
            fprintf(stderr, CONST_ERROR_STR(#func " status (%d) not init"), (int)status); \
            throw_21_default_exception(env, #func " status not init"); \
          } \
        } else { \
          fprintf(stderr, CONST_ERROR_STR(#func " inconsistence: Unable to execute jni_check_consistence %d"), err); \
          throw_w21_exception(env, #func " inconsistence: Unable to execute", err, NULL, NULL); \
        } \
        goto func##_unlock; \
      } else { \
        fprintf(stderr, CONST_ERROR_STR(#func ": Unexpected unable to unlock. thread_mutex_lock: %d. Ignoring"), err); \
        throw_w21_exception(env, #func ": Unexpected unable to unlock", err, NULL, NULL); \
      } \
    } else { \
      fprintf(stderr, CONST_ERROR_STR(#func " unexpected: locker not found or not initialized")); \
      throw_21_default_exception(env, #func " unexpected: locker not found or not initialized. Unable to execute"); \
    } \
  } else { \
    fprintf(stderr, CONST_ERROR_STR(#func " unexpected: Unable to load C pointer at jni_get_handler: %d. Ignoring ..."), err); \
    throw_w21_exception(env, #func " unexpected: Unable to load C pointer", err, NULL, NULL); \
  } \
\
  return ret; \
func##_unlock: \
  bool close = (__atomic_load_n(&locker->thread_ctx->status, __ATOMIC_SEQ_CST) == JNI_w21_CloseRequest); \
  if ((err = pthread_mutex_unlock(&locker->lock)) == 0) { \
    if (close) \
      if ((err = (int)Java_org_w21parser_W21ParserLoader_jniClose(env, thisObject))) \
        fprintf(stderr, CONST_ERROR_STR(#func " unexpected: Call jniClose = %d"), err); \
\
    return ret; \
  } \
\
  fprintf(stderr, CONST_ERROR_STR(#func " unexpected: unable to unlock thread_mutex_unlock: %d. Ignoring ..."), err); \
  return ret;

#define W21_JNI_PARSE(functionName, bsonReadFunction) \
    DECLARE_JNI_W21_CONFIG \
    if (config->in_bson_serialized.bson != NULL) { \
      throw_w21_exception( \
        env, #functionName ": BSON serialized already parsed", \
        JNI_W21_BSON_OBJECT_ALREADY_PARSED, \
        NULL, \
        NULL \
      ); \
    } else if ((err = SET_SECURE_DIRECT_BUFFER_TO_BSON) == 0) { \
      if (locker->withResourceStats) { \
        locker->isResourceStatsParseFinished = false; \
        w21_hard_summary_parse_begin(GET_W21_JNI_SOAP); \
      } \
\
      /* config->in_bson in_bson exists only if bson_read_<object>21 is success and executed at once*/ \
      if ((config->in_bson != NULL) || ((err = bson_read_##bsonReadFunction##21(GET_W21_JNI_SOAP)) == 0)) { \
        struct c_bson_serialized_t *bson_ser = w21_bson_serialize(GET_W21_JNI_SOAP); \
        if (bson_ser) { \
          if ((err = set_direct_buffer(env, thisObject, gW21_bbBson, (void *)bson_ser->bson, bson_ser->bson_size)) == 0) { \
            if (locker->withResourceStats) { \
              locker->isResourceStatsParseFinished = true; \
              w21_hard_summary_parse_end(GET_W21_JNI_SOAP); \
            } \
          } else \
            throw_w21_exception( \
              env, #functionName ": Could not set BSON serialized to Java Direct Buffer", \
              err, \
              NULL, \
              NULL \
            ); \
        } else { \
          throw_w21_exception( \
            env, #functionName ": Could not parse object to BSON serialized. See details", \
            err, \
            config->detail_message, \
            config->detail_message_xml \
          ); \
        } \
      } else { \
        throw_w21_exception( \
          env, #functionName ": Could not parse object to BSON. See details", \
          err, \
          config->detail_message, \
          config->detail_message_xml \
        ); \
      } \
    } else \
      throw_w21_exception( \
        env, #functionName ": Unable to reset BSON Direct Buffer", \
        err, \
        NULL, \
        NULL \
      );


#define W21_JNI_PARSE_JSON(functionName, bsonReadFunction) \
    DECLARE_JNI_W21_CONFIG \
    if (config->in_json_str.json != NULL) { \
      throw_w21_exception( \
        env, #functionName ": JSON string already parsed", \
        JNI_W21_JSON_STRING_ALREADY_PARSED, \
        NULL, \
        NULL \
      ); \
    } else if ((err = SET_SECURE_DIRECT_BUFFER_TO_JSON) == 0) { \
      if (locker->withResourceStats) { \
        locker->isResourceStatsParseJsonFinished = false; \
        w21_hard_summary_parse_json_begin(GET_W21_JNI_SOAP); \
      } \
\
      /* config->in_bson in_bson exists only if bson_read_<object>21 is success and executed at once*/ \
      if ((config->in_bson != NULL) || ((err = bson_read_##bsonReadFunction##21(GET_W21_JNI_SOAP)) == 0)) { \
        struct c_json_str_t *c_json = w21_get_json(GET_W21_JNI_SOAP); \
        if (c_json) { \
          if ((err = set_direct_buffer(env, thisObject, gW21_bbJson, (void *)c_json->json, c_json->json_len)) == 0) { \
            if (locker->withResourceStats) { \
              locker->isResourceStatsParseJsonFinished = true; \
              w21_hard_summary_parse_json_end(GET_W21_JNI_SOAP); \
            } \
          } else \
            throw_w21_exception( \
              env, #functionName ": Could not set JSON string to Java Direct Buffer", \
              err, \
              NULL, \
              NULL \
            ); \
        } else { \
          throw_w21_exception( \
            env, #functionName ": Could not parse object to JSON string. See details", \
            err, \
            config->detail_message, \
            config->detail_message_xml \
          ); \
        } \
      } else { \
        throw_w21_exception( \
          env, #functionName ": Could not parse object to JSON string. See details", \
          err, \
          config->detail_message, \
          config->detail_message_xml \
        ); \
      } \
    } else \
      throw_w21_exception( \
        env, #functionName ": Unable to reset JSON Direct Buffer", \
        err, \
        NULL, \
        NULL \
      );

#define JNI_READ_FROM_BYTE_BUFFER(functionName, cw21rdFunction) \
    if (bbXml) { \
      if ((limit > 0) && (limit <= (*env)->GetDirectBufferCapacity(env, bbXml))) { \
        const char *xml_ptr = (const char *)(*env)->GetDirectBufferAddress(env, bbXml); \
        if (xml_ptr) { \
          if (locker->withResourceStats) { \
            locker->isResourceStatsReadFinished = false; \
            w21_hard_summary_read_begin(GET_W21_JNI_SOAP); \
          } \
\
          DECLARE_JNI_W21_CONFIG \
          if ((err = cw21rd_##cw21rdFunction(GET_W21_JNI_SOAP, xml_ptr, (size_t)limit)) == 0) { \
            if (locker->withResourceStats) { \
              locker->isResourceStatsReadFinished = true; \
              w21_hard_summary_read_end(GET_W21_JNI_SOAP); \
            } \
          } else \
            throw_w21_exception( \
              env, \
              #functionName ": Stream auto detect mode error. See details for complete error description", \
              err, \
              config->detail_message, \
              config->detail_message_xml \
            ); \
        } else \
          throw_w21_exception(env, #functionName ": Missing xml in Java Byte buffer", JNI_W21_DIRECT_BUFFER_INPUT_MISSING_XML_STREAM, NULL, NULL); \
      } else \
        throw_w21_exception(env, #functionName ": Invalid ByteBuffer size bbXml", JNI_W21_DIRECT_BUFFER_INPUT_BUFFER_INVALID_SIZE, NULL, NULL); \
    } else \
      throw_w21_exception(env, #functionName ": Missing ByteBuffer object bbXml", JNI_W21_DIRECT_BUFFER_INPUT_BUFFER, NULL, NULL);

#define W21_JNI_STAT_READ(functionName, fieldName) \
  jlong longRet = 0; \
  JNI_W21_EXECUTE_ACTION_RET( \
    functionName, \
    if (locker->withResourceStats) { \
      if (locker->isResourceStatsReadFinished) { \
        DECLARE_JNI_W21_CONFIG \
        if ((err = config->hardware_statistics.in_err) == 0) \
          longRet = (jlong)config->hardware_statistics.fieldName; \
        else  \
          throw_w21_exception( \
            env, \
            #functionName ": Error in hardware statistics on loading WITSML 2.1 document", \
            err, \
            NULL, \
            NULL \
          ); \
      } else \
        throw_w21_exception( \
          env, \
          #functionName ": resource is not finished correctly", \
          JNI_W21_RESOURCE_STAT_NOT_FINISHED_CORRECTLY, \
          NULL, \
          NULL \
        ); \
    } else \
      throw_w21_exception( \
        env, \
        #functionName ": resource statistics is disabled", \
        JNI_W21_RESOURCE_STAT_DISABLE, \
        NULL, \
        NULL \
      ); \
    , \
    longRet \
  )

#define W21_JNI_STAT_PARSE(functionName, fieldName) \
  jlong longRet = 0; \
  JNI_W21_EXECUTE_ACTION_RET( \
    functionName, \
    if (locker->withResourceStats) { \
      if (locker->isResourceStatsParseFinished) { \
        DECLARE_JNI_W21_CONFIG \
        if ((err = config->hardware_statistics.in_parse_err) == 0) \
          longRet = (jlong)config->hardware_statistics.fieldName; \
        else \
          throw_w21_exception( \
            env, \
            #functionName ": Error in hardware statistics on parsing WITSML 2.1 to BSON document", \
            err, \
            NULL, \
            NULL \
          ); \
      } else \
        throw_w21_exception( \
          env, \
          #functionName ": resource is not finished correctly or BSON object not parsed", \
          JNI_W21_RESOURCE_STAT_NOT_FINISHED_CORRECTLY_OR_NOT_PARSED, \
          NULL, \
          NULL \
        ); \
    } else \
      throw_w21_exception( \
        env, \
        #functionName ": resource statistics is disabled", \
        JNI_W21_RESOURCE_STAT_DISABLE, \
        NULL, \
        NULL \
      ); \
    , \
    longRet \
  )

#define W21_JNI_STAT_PARSE_JSON(functionName, fieldName) \
  jlong longRet = 0; \
  JNI_W21_EXECUTE_ACTION_RET( \
    functionName, \
    if (locker->withResourceStats) { \
      if (locker->isResourceStatsParseJsonFinished) { \
        DECLARE_JNI_W21_CONFIG \
        if ((err = config->hardware_statistics.in_parse_json_err) == 0) \
          longRet = (jlong)config->hardware_statistics.fieldName; \
        else \
          throw_w21_exception( \
            env, \
            #functionName ": Error in hardware statistics on parsing WITSML 2.1 to JSON document", \
            err, \
            NULL, \
            NULL \
          ); \
      } else \
        throw_w21_exception( \
          env, \
          #functionName ": resource is not finished correctly or JSON string not parsed", \
          JNI_W21_RESOURCE_STAT_JSON_NOT_FINISHED_CORRECTLY_OR_NOT_PARSED, \
          NULL, \
          NULL \
        ); \
    } else \
      throw_w21_exception( \
        env, \
        #functionName ": resource statistics is disabled", \
        JNI_W21_RESOURCE_STAT_DISABLE, \
        NULL, \
        NULL \
      ); \
    , \
    longRet \
  )

#define W21_JNI_DOC_STAT_PARSE(functionName, fieldName) \
  jint intRet = 0; \
  JNI_W21_EXECUTE_ACTION_RET( \
    functionName, \
    DECLARE_JNI_W21_CONFIG \
    if (config->error == 0) { \
      if (locker->withResourceStats) \
        intRet = w21_get_statistics(GET_W21_JNI_SOAP)->fieldName; \
      else \
        throw_w21_exception( \
          env, \
          #functionName ": document statistics is disabled", \
          JNI_W21_DOCUMENT_STAT_DISABLE, \
          NULL, \
          NULL \
        ); \
    } else \
      throw_w21_exception( \
        env, \
        #functionName ": Unable to check document statistics. Parse or WITSML 2.1 error", \
        JNI_W21_DOCUMENT_STAT_ERROR, \
        NULL, \
        NULL \
      ); \
    , \
    intRet \
  )
#endif 
