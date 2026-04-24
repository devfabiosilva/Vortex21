#ifndef W21_DESERIALIZER_MACROS_H
 #define W21_DESERIALIZER_MACROS_H
#include <w21_config.h>
/* 
#define W21_BSON_FREE \
  cws_bson_free(&(config)->in_bson);
*/

#define W21_BSON_REINIT \
  config->in_bson = NULL;

#ifdef WITH_STATISTICS
 #define CAPTURE_STAT(txt) (((W21_CONFIG *)soap->user)->statistics).txt##s++;
 #define CAPTURE_STAT_A(n, txt) (((W21_CONFIG *)soap->user)->statistics).txt##s+=n;
 #define CAPTURE_STAT_ON_SUCCESS_A(n, txt) (((W21_CONFIG *)soap->user)->statistics).txt##s+=n;
 #define CAPTURE_STAT_ON_SUCCESS(txt) (((W21_CONFIG *)soap->user)->statistics).txt##s++;
#define DECLARE_CAPTURE_STAT int n = numberOfObjectsInArray;
#else
 #define CAPTURE_STAT(txt)
 #define CAPTURE_STAT_A(n, txt)
 #define CAPTURE_STAT_ON_SUCCESS_A(n, txt)
 #define CAPTURE_STAT_ON_SUCCESS(txt)
 #define DECLARE_CAPTURE_STAT
#endif

#define MSG_ERR_REFERENCE_BSON_CONFIG "Could not reference BSON at "

#define W21_CONSTRUCT_BSON(nameObj) \
  if (!((config)->in_bson=(config)->in_bson_object_alloc)) { \
    set_w21_error_message(soap, E_W21_ERROR_REFERENCE_IN_BSON_OBJECT, MSG_ERR_REFERENCE_BSON_CONFIG #nameObj); \
    W21_RETURN \
  } \
\
  bson_t root_document; \
\
  if (!bson_append_document_begin((config)->in_bson, #nameObj, (int)(sizeof(#nameObj)-1), &root_document)) { \
    set_w21_error_message(soap, E_W21_ERROR_BEGIN_BSON_ROOT_OBJECT, "Could not begin root BSON document at " #nameObj); \
    W21_BSON_REINIT \
    W21_RETURN \
  }


#define W21_CONSTRUCT_BSON_B(nameObj) \
  if (!((config)->in_bson=(config)->in_bson_object_alloc)) { \
    set_w21_error_message(soap, E_W21_ERROR_REFERENCE_IN_BSON_OBJECT, MSG_ERR_REFERENCE_BSON_CONFIG #nameObj); \
    W21_RETURN \
  }
//////////////////////////////////////// BEGIN ATTRIBUTES OBJECT/////////////////////////////////

//Default attributes for
#define READ_PUT_DEFAULT_ATTRIBUTES_21_OR_ELSE_GOTO_RESUME(bsonType, objectParent, onErrorGoto) \
  if (bson_put_multiple_attributes_if_they_exist(bsonType, SET_MULTIPLE_ATTRIBUTES( \
      CWS_CONST_BSON_KEY("uuid"), objectParent->uuid, \
      CWS_CONST_BSON_KEY("schemaVersion"), objectParent->schemaVersion, \
      CWS_CONST_BSON_KEY("objectVersion"), objectParent->objectVersion \
    ))) { \
      set_w21_error_message(soap, E_W21_ERROR_APPEND_ROOT_DEFAULT_ATTRIBUTES, "Could not set default attributes in root parent " #objectParent); \
      goto onErrorGoto##_resume; \
    }

#define READ_PUT_MULTIPLE_ATTRIBUTES_21_OR_ELSE_GOTO_RESUME(bsonType, objectParent, onErrorGoto, ...) \
  if (bson_put_multiple_attributes_if_they_exist(bsonType, __VA_ARGS__)) { \
    set_w21_error_message(soap, E_W21_ERROR_APPEND_ROOT_DEFAULT_ATTRIBUTES, "Multi attributes error. could not set default attributes in root parent " #objectParent); \
    goto onErrorGoto##_resume; \
  }

#define READ_W_PUT_MULTIPLE_ATTRIBUTES_21_OR_ELSE_GOTO_RESUME(objectParent, ...) \
  READ_PUT_MULTIPLE_ATTRIBUTES_21_OR_ELSE_GOTO_RESUME(&root_document, objectParent, bson_read_##objectParent##21, __VA_ARGS__)

#define READ_A_PUT_MULTIPLE_ATTRIBUTES_21_OR_ELSE_GOTO_RESUME(objectParent, ...) \
  READ_PUT_MULTIPLE_ATTRIBUTES_21_OR_ELSE_GOTO_RESUME(&document_in_array, objectParent, bson_read_array_of_##objectParent##_21, __VA_ARGS__)

#define READ_PUT_TWO_ATTRIBUTES_21_OR_ELSE_GOTO_RESUME(bsonType, objectParent, attr1, attr2, onErrorGoto) \
  if (bson_put_two_attributes_if_exist(bsonType, \
      CWS_CONST_BSON_KEY(#attr1), objectParent->attr1, -1,\
      CWS_CONST_BSON_KEY(#attr2), objectParent->attr2, -1\
    )) { \
      set_w21_error_message(soap, E_W21_ERROR_APPEND_TWO_ATTRIBUTES, "Could not set two attributes in " #objectParent); \
      goto onErrorGoto##_resume; \
    }

#define READ_A_PUT_TWO_ATTRIBUTES_21_OR_ELSE_GOTO_RESUME(objectParent, attr1, attr2) \
    READ_PUT_TWO_ATTRIBUTES_21_OR_ELSE_GOTO_RESUME(&document_in_array, objectParent, attr1, attr2, bson_read_array_of_##objectParent##_21)

#define READ_O_PUT_TWO_ATTRIBUTES_21_OR_ELSE_GOTO_RESUME(objectParent, attr1, attr2) \
  READ_PUT_TWO_ATTRIBUTES_21_OR_ELSE_GOTO_RESUME(&child, objectParent, attr1, attr2, bson_read_##objectParent##_21)

//used for ROOT Witsml object
#define READ_W_PUT_DEFAULT_ATTRIBUTES_21_OR_ELSE_GOTO_RESUME(objectParent) \
  READ_PUT_DEFAULT_ATTRIBUTES_21_OR_ELSE_GOTO_RESUME(&root_document, objectParent, bson_read_##objectParent##21)

#define READ_A_PUT_DEFAULT_ATTRIBUTES_21_OR_ELSE_GOTO_RESUME(objectParent) \
  READ_PUT_DEFAULT_ATTRIBUTES_21_OR_ELSE_GOTO_RESUME(&document_in_array, objectParent, bson_read_array_of_##objectParent##_21)

//used for ROOT Witsml object
#define READ_O_PUT_DEFAULT_ATTRIBUTES_21_OR_ELSE_GOTO_RESUME(objectParent) \
  READ_PUT_DEFAULT_ATTRIBUTES_21_OR_ELSE_GOTO_RESUME(&child, objectParent, bson_read_##objectParent##_21)
//////////////////////////////////////// END ATTRIBUTES OBJECT //////////////////////////////////

////////////////////////////////////// BEGIN STRING OBJECT //////////////////////////////////////
//Used for array function
#define ARRAY_OF_PREFIX ""

#define READ_ARRAY_OF_UTF8_OBJECT_21_OR_ELSE_GOTO_RESUME(bsonType, objectParent, objectName, onErrorGoto) \
  if (bson_read_arrayOfString21_util(soap, bsonType, CWS_CONST_BSON_KEY(ARRAY_OF_PREFIX #objectName), objectParent->__size##objectName, \
    objectParent->objectName)) goto onErrorGoto##_resume;

#define READ_W_ARRAY_OF_UTF8_OBJECT_21_OR_ELSE_GOTO_RESUME(objectParent, objectName) \
  READ_ARRAY_OF_UTF8_OBJECT_21_OR_ELSE_GOTO_RESUME(&root_document, objectParent, objectName, bson_read_##objectParent##21)

#define READ_ARRAY_OF_UTF8_OBJECT_21_OR_ELSE_GOTO_RESUME_B(ns, bsonType, objectParent, objectName, onErrorGoto) \
  if (bson_read_arrayOfString21_util(soap, bsonType, CWS_CONST_BSON_KEY(ARRAY_OF_PREFIX #objectName), objectParent->__size##objectName, \
    objectParent->ns##__##objectName)) goto onErrorGoto##_resume;

#define READ_O_ARRAY_OF_UTF8_OBJECT_21_OR_ELSE_GOTO_RESUME_B(ns, objectParent, objectName) \
    READ_ARRAY_OF_UTF8_OBJECT_21_OR_ELSE_GOTO_RESUME_B(ns, &child, objectParent, objectName, bson_read_##objectParent##_21)

//used for ROOT Witsml object
#define READ_W_ARRAY_OF_UTF8_OBJECT_21_OR_ELSE_GOTO_RESUME_B(ns, objectParent, objectName) \
  READ_ARRAY_OF_UTF8_OBJECT_21_OR_ELSE_GOTO_RESUME_B(ns, &root_document, objectParent, objectName, bson_read_##objectParent##21)

#define READ_A_ARRAY_OF_UTF8_OBJECT_21_OR_ELSE_GOTO_RESUME_B(ns, objectParent, objectName) \
  READ_ARRAY_OF_UTF8_OBJECT_21_OR_ELSE_GOTO_RESUME_B(ns, &document_in_array, objectParent, objectName, bson_read_array_of_##objectParent##_21)

#define READ_UTF8_OBJECT_21_OR_ELSE_GOTO_RESUME_B(ns, bsonType, objectParent, objectName, onErrorGoto) \
  if (objectParent->ns##__##objectName) { \
    if (!bson_append_utf8(bsonType, CWS_CONST_BSON_KEY(#objectName), (const char *)objectParent->ns##__##objectName, -1)) { \
      set_w21_error_message(soap, E_W21_ERROR_SET_UTF8_STRING_IN_BSON, #objectParent "Could not read " #objectName " of " #objectParent " object"); \
      goto onErrorGoto##_resume; \
    } \
    CAPTURE_STAT(string) \
  }

#define READ_O_UTF8_OBJECT_21_OR_ELSE_GOTO_RESUME_B(ns, objectParent, objectName) \
  READ_UTF8_OBJECT_21_OR_ELSE_GOTO_RESUME_B(ns, &child, objectParent, objectName, bson_read_##objectParent##_21)

#define READ_A_UTF8_OBJECT_21_OR_ELSE_GOTO_RESUME_B(ns, objectParent, objectName) \
  READ_UTF8_OBJECT_21_OR_ELSE_GOTO_RESUME_B(ns, &document_in_array, objectParent, objectName, bson_read_array_of_##objectParent##_21)

  // SPECIAL CASE CUSTOM DATA
#define READ_CUSTOM_DATA_NULLABLE_OBJECT_21_OR_ELSE_GOTO_RESUME_B(ns, bsonType, objectParent, objectName, onErrorGoto) \
    if (objectParent->ns##__##objectName) \
      if (bson_read_arrayOfString21_util(soap, bsonType, CWS_CONST_BSON_KEY(ARRAY_OF_PREFIX #objectName), objectParent->ns##__##objectName->__size, \
        objectParent->ns##__##objectName->__any)) goto onErrorGoto##_resume;

  // SPECIAL CASE CUSTOM DATA
#define READ_CUSTOM_DATA_NULLABLE_OBJECT_21_OR_ELSE_GOTO_RESUME(bsonType, objectParent, objectName, onErrorGoto) \
    if (objectParent->objectName) \
      if (bson_read_arrayOfString21_util(soap, bsonType, CWS_CONST_BSON_KEY(ARRAY_OF_PREFIX #objectName), objectParent->objectName->__size, \
        objectParent->objectName->__any)) goto onErrorGoto##_resume;
  // END ESPECIAL CASE

#define READ_O_CUSTOM_DATA_NULLABLE_OBJECT_21_OR_ELSE_GOTO_RESUME_B(ns, objectParent, objectName) \
  READ_CUSTOM_DATA_NULLABLE_OBJECT_21_OR_ELSE_GOTO_RESUME_B(ns, &child, objectParent, objectName, bson_read_##objectParent##_21)

#define READ_O_CUSTOM_DATA_NULLABLE_OBJECT_21_OR_ELSE_GOTO_RESUME(objectParent, objectName) \
  READ_CUSTOM_DATA_NULLABLE_OBJECT_21_OR_ELSE_GOTO_RESUME(&child, objectParent, objectName, bson_read_##objectParent##_21)

#define READ_UTF8_OBJECT_21_OR_ELSE_GOTO_RESUME(bsonType, objectParent, objectName, onErrorGoto) \
  if (objectParent->objectName) { \
    if (!bson_append_utf8(bsonType, CWS_CONST_BSON_KEY(#objectName), (const char *)objectParent->objectName, -1)) { \
      set_w21_error_message(soap, E_W21_ERROR_SET_UTF8_STRING_IN_BSON, "Could not add BSON utf-8 string at " #objectParent " in " #objectName); \
      goto onErrorGoto##_resume; \
    } \
    CAPTURE_STAT(string) \
  }

#define READ_UTF8_OBJECT_ALIAS_21_OR_ELSE_GOTO_RESUME(bsonType, objectParent, objectName, objectAlias, onErrorGoto) \
  if (objectParent->objectName) { \
    if (!bson_append_utf8(bsonType, CWS_CONST_BSON_KEY(#objectAlias), (const char *)objectParent->objectName, -1)) { \
      set_w21_error_message(soap, E_W21_ERROR_SET_UTF8_STRING_IN_BSON_ALIAS, "Could not add BSON utf-8 alias string at " #objectParent " in " #objectAlias); \
      goto onErrorGoto##_resume; \
    } \
    CAPTURE_STAT(string) \
  }

#define READ_W_UTF8_OBJECT_ALIAS_21_OR_ELSE_GOTO_RESUME(objectParent, objectName, objectAlias) \
  READ_UTF8_OBJECT_ALIAS_21_OR_ELSE_GOTO_RESUME(&root_document, objectParent, objectName, objectAlias, bson_read_##objectParent##21)

//used for ROOT Witsml object
#define READ_W_CUSTOM_DATA_NULLABLE_OBJECT_21_OR_ELSE_GOTO_RESUME_B(ns, objectParent, objectName) \
  READ_CUSTOM_DATA_NULLABLE_OBJECT_21_OR_ELSE_GOTO_RESUME_B(ns, &root_document, objectParent, objectName, bson_read_##objectParent##21)

#define READ_A_CUSTOM_DATA_NULLABLE_OBJECT_21_OR_ELSE_GOTO_RESUME_B(ns, objectParent, objectName) \
  READ_CUSTOM_DATA_NULLABLE_OBJECT_21_OR_ELSE_GOTO_RESUME_B(ns, &document_in_array, objectParent, objectName, bson_read_array_of_##objectParent##_21)
//used for ROOT Witsml object
#define READ_W_UTF8_OBJECT_21_OR_ELSE_GOTO_RESUME(objectParent, objectName) \
  READ_UTF8_OBJECT_21_OR_ELSE_GOTO_RESUME(&root_document, objectParent, objectName, bson_read_##objectParent##21)

//used for ROOT Witsml object
#define READ_W_UTF8_OBJECT_21_OR_ELSE_GOTO_RESUME_B(ns, objectParent, objectName) \
  READ_UTF8_OBJECT_21_OR_ELSE_GOTO_RESUME_B(ns, &root_document, objectParent, objectName, bson_read_##objectParent##21)

////////////////////////////////////// END STRING OBJECT //////////////////////////////////////

///////////////////////////////////// BEGIN DATE TIME OBJECT //////////////////////////////////

#define READ_TIME_NULLABLE_21_OR_ELSE_GOTO_RESUME(bsonType, objectParent, objectName, onErrorGoto) \
  if (objectParent->objectName) { \
    if (!bson_append_time_t(bsonType, CWS_CONST_BSON_KEY(#objectName), *(objectParent->objectName))) { \
      set_w21_error_message(soap, E_W21_ERROR_DATE_TIME_NULLABLE, "Could not set date time nullable " #objectName " in parent " #objectParent); \
      goto onErrorGoto##_resume; \
    } \
    CAPTURE_STAT(date_time) \
  }

#define READ_TIME_21_OR_ELSE_GOTO_RESUME(bson, objectParent, objectName, onErrorGoto) \
  if (!bson_append_time_t(bson, CWS_CONST_BSON_KEY(#objectName), objectParent->objectName)) { \
    set_w21_error_message(soap, E_W21_ERROR_DATE_TIME, "Could not set date time " #objectName " in parent " #objectParent); \
    goto onErrorGoto##_resume; \
  } \
  CAPTURE_STAT(date_time)

#define READ_O_TIME_21_OR_ELSE_GOTO_RESUME(objectParent, objectName) \
  READ_TIME_21_OR_ELSE_GOTO_RESUME(&child, objectParent, objectName, bson_read_##objectParent##_21)

#define READ_W_TIME_21_OR_ELSE_GOTO_RESUME(objectParent, objectName) \
  READ_TIME_21_OR_ELSE_GOTO_RESUME(&root_document, objectParent, objectName, bson_read_##objectParent##21)

#define READ_A_TIME_21_OR_ELSE_GOTO_RESUME(objectParent, objectName) \
  READ_TIME_21_OR_ELSE_GOTO_RESUME(&document_in_array, objectParent, objectName, bson_read_array_of_##objectParent##_21)

#define READ_O_TIME_NULLABLE_21_OR_ELSE_GOTO_RESUME(objectParent, objectName) \
  if (objectParent->objectName) { \
    if (!bson_append_time_t(&child, CWS_CONST_BSON_KEY(#objectName), *(objectParent->objectName))) { \
      set_w21_error_message(soap, E_W21_ERROR_DATE_TIME_NULLABLE, "Could not set date time nullable " #objectName " in parent " #objectParent); \
      goto bson_read_##objectParent##_21_resume; \
    } \
    CAPTURE_STAT(date_time) \
  }

#define READ_O_ARRAY_OF_UTF8_OBJECT_21_OR_ELSE_GOTO_RESUME(objectParent, objectName) \
  if (bson_read_arrayOfString21_util(soap, &child, CWS_CONST_BSON_KEY(ARRAY_OF_PREFIX #objectName), objectParent->__size##objectName, \
    objectParent->objectName)) goto bson_read_##objectParent##_21_resume;

#define READ_A_ARRAY_OF_UTF8_OBJECT_21_OR_ELSE_GOTO_RESUME(objectParent, objectName) \
  if (bson_read_arrayOfString21_util(soap, &document_in_array, CWS_CONST_BSON_KEY(ARRAY_OF_PREFIX #objectName), objectParent->__size##objectName, \
    objectParent->objectName)) goto bson_read_array_of_##objectParent##_21_resume;
/*
//Used for object function
#define READ_O_MEASURE_OBJECT_21_OR_ELSE_GOTO_RESUME_B(objectParent, objectName, measureType) \
  if (bson_read_##measureType##_21(soap, &child, CWS_CONST_BSON_KEY(#objectName), objectParent->objectName)) \
    goto bson_read_##objectParent##_21_resume;
*/
//used for ROOT Witsml object
#define READ_W_TIME_NULLABLE_21_OR_ELSE_GOTO_RESUME(objectParent, objectName) \
  READ_TIME_NULLABLE_21_OR_ELSE_GOTO_RESUME(&root_document, objectParent, objectName, bson_read_##objectParent##21)

///////////////////////////////////// END DATE TIME OBJECT //////////////////////////////////


///////////////////////////////////// BEGIN LONG 64 OBJECT //////////////////////////////////
#define READ_LONG64_NULLABLE_21_OR_ELSE_GOTO_RESUME(bsonType, objectParent, objectName, onErrorGoto) \
  if (objectParent->objectName) { \
    if (!bson_append_int64(bsonType, \
      CWS_CONST_BSON_KEY(#objectName), (int64_t)*(objectParent->objectName))) { \
        set_w21_error_message(soap, E_W21_ERROR_LONG64_NULLABLE, "Could not add NULLABLE LONG64 object " #objectName " in " #objectParent); \
      goto onErrorGoto##_resume; \
    } \
\
    CAPTURE_STAT(long64) \
  }

#define READ_O_LONG64_NULLABLE_21_OR_ELSE_GOTO_RESUME(objectParent, objectName) \
  READ_LONG64_NULLABLE_21_OR_ELSE_GOTO_RESUME(&child, objectParent, objectName, bson_read_##objectParent##_21)

//used for ROOT Witsml object
#define READ_W_LONG64_NULLABLE_21_OR_ELSE_GOTO_RESUME(objectParent, objectName) \
  READ_LONG64_NULLABLE_21_OR_ELSE_GOTO_RESUME(&root_document, objectParent, objectName, bson_read_##objectParent##21)


//////////////////////////////////// END LONG 64 OBJECT //////////////////////////////////

//////////////////////////////////// BEGIN ENUM OBJECT //////////////////////////////////
#define READ_OBJECT_ENUM_NULLABLE_21_OR_ELSE_GOTO_RESUME(ns, bsonType, objectParent, objectName, enumFunctionName, onErrorGoto) \
  if (objectParent->objectName) { \
    if (!bson_append_utf8(bsonType, CWS_CONST_BSON_KEY(#objectName), soap_##ns##__##enumFunctionName##2s(soap, *(objectParent->objectName)), -1)) { \
      set_w21_error_message(soap, E_W21_ERROR_ENUM_NULLABLE, "Could not add nullable enum object " #objectName " in " #objectParent); \
      goto onErrorGoto##_resume; \
    } \
    CAPTURE_STAT(enum) \
  }

//used for ROOT Witsml object
#define READ_W_OBJECT_ENUM_NULLABLE_21_OR_ELSE_GOTO_RESUME(ns, objectParent, objectName, enumFunctionName) \
  READ_OBJECT_ENUM_NULLABLE_21_OR_ELSE_GOTO_RESUME(ns, &root_document, objectParent, objectName, enumFunctionName, bson_read_##objectParent##21)

#define READ_OBJECT_ENUM_21_OR_ELSE_GOTO_RESUME(ns, thisBson, objectParent, objectName, enumFunctionName, onErrorGoto) \
    if (!bson_append_utf8(thisBson, CWS_CONST_BSON_KEY(#objectName), soap_##ns##__##enumFunctionName##2s(soap, (objectParent->objectName)), -1)) { \
      set_w21_error_message(soap, E_W21_ERROR_ENUM_REQUIRE, "Could not add enum required object " #objectName " in " #objectParent); \
      goto onErrorGoto##_resume; \
    } \
    CAPTURE_STAT(enum)

#define READ_O_OBJECT_ENUM_21_OR_ELSE_GOTO_RESUME(ns, objectParent, objectName, enumFunctionName) \
    READ_OBJECT_ENUM_21_OR_ELSE_GOTO_RESUME(ns, &child, objectParent, objectName, enumFunctionName, bson_read_##objectParent##_21)

#define READ_W_OBJECT_ENUM_21_OR_ELSE_GOTO_RESUME(ns, objectParent, objectName, enumFunctionName) \
    READ_OBJECT_ENUM_21_OR_ELSE_GOTO_RESUME(ns, &root_document, objectParent, objectName, enumFunctionName, bson_read_##objectParent##21)

#define READ_OBJECT_ENUM_21_OR_ELSE_GOTO_RESUME_B(ns, thisBson, objectParent, objectName, enumFunctionName, onErrorGoto) \
    if (!bson_append_utf8(thisBson, CWS_CONST_BSON_KEY(#objectName), soap_##ns##__##enumFunctionName##2s(soap, (objectParent->ns##__##objectName)), -1)) { \
      set_w21_error_message(soap, E_W21_ERROR_ENUM_REQUIRE, #ns ": could not add enum required object " #objectName " in " #objectParent); \
      goto onErrorGoto##_resume; \
    } \
    CAPTURE_STAT(enum)

#define READ_O_OBJECT_ENUM_21_OR_ELSE_GOTO_RESUME_B(ns, objectParent, objectName, enumFunctionName) \
    READ_OBJECT_ENUM_21_OR_ELSE_GOTO_RESUME_B(ns, &child, objectParent, objectName, enumFunctionName, bson_read_##objectParent##_21)

#define READ_W_OBJECT_ENUM_21_OR_ELSE_GOTO_RESUME_B(ns, objectParent, objectName, enumFunctionName) \
    READ_OBJECT_ENUM_21_OR_ELSE_GOTO_RESUME_B(ns, &root_document, objectParent, objectName, enumFunctionName, bson_read_##objectParent##21)

#define READ_A_OBJECT_ENUM_21_OR_ELSE_GOTO_RESUME_B(ns, objectParent, objectName, enumFunctionName) \
    READ_OBJECT_ENUM_21_OR_ELSE_GOTO_RESUME_B(ns, &document_in_array, objectParent, objectName, enumFunctionName, bson_read_array_of_##objectParent##_21)
//////////////////////////////////// END ENUM OBJECT //////////////////////////////////

//////////////////////////////// BEGIN BOOLEAN OBJECT /////////////////////////////////

//Used for object function

#define READ_BOOLEAN_NULLABLE_21_OR_ELSE_GOTO_RESUME(bsonType, objectParent, objectName, onErrorGoto) \
  if (objectParent->objectName) { \
    if (!bson_append_bool(bsonType, CWS_CONST_BSON_KEY(#objectName), (bool)*(objectParent->objectName))) { \
      set_w21_error_message(soap, E_W21_ERROR_BOOLEAN_NULLABLE, "Could not add boolean nullable in "  #objectParent " root object at " #objectName); \
      goto onErrorGoto##_resume; \
    } \
    CAPTURE_STAT(boolean) \
  }

#define READ_BOOLEAN_21_OR_ELSE_GOTO_RESUME(bsonType, objectParent, objectName, onErrorGoto) \
  if (!bson_append_bool(bsonType, CWS_CONST_BSON_KEY(#objectName), (bool)(objectParent->objectName))) { \
    set_w21_error_message(soap, E_W21_ERROR_BOOLEAN_REQUIRED, "Could not add boolean required in "  #objectParent " root object at " #objectName); \
    goto onErrorGoto##_resume; \
  } \
  CAPTURE_STAT(boolean)

#define READ_A_BOOLEAN_21_OR_ELSE_GOTO_RESUME(objectParent, objectName) \
  READ_BOOLEAN_21_OR_ELSE_GOTO_RESUME(&document_in_array, objectParent, objectName, bson_read_array_of_##objectParent##_21)

#define READ_O_BOOLEAN_21_OR_ELSE_GOTO_RESUME(objectParent, objectName) \
  READ_BOOLEAN_21_OR_ELSE_GOTO_RESUME(&child, objectParent, objectName, bson_read_##objectParent##_21)

#define READ_A_BOOLEAN_NULLABLE_21_OR_ELSE_GOTO_RESUME(objectParent, objectName) \
  READ_BOOLEAN_NULLABLE_21_OR_ELSE_GOTO_RESUME(&document_in_array, objectParent, objectName, bson_read_array_of_##objectParent##_21)

#define READ_O_BOOLEAN_NULLABLE_21_OR_ELSE_GOTO_RESUME(objectParent, objectName) \
  READ_BOOLEAN_NULLABLE_21_OR_ELSE_GOTO_RESUME(&child, objectParent, objectName, bson_read_##objectParent##_21)

//used for ROOT Witsml object
#define READ_W_BOOLEAN_NULLABLE_21_OR_ELSE_GOTO_RESUME(objectParent, objectName) \
  READ_BOOLEAN_NULLABLE_21_OR_ELSE_GOTO_RESUME(&root_document, objectParent, objectName, bson_read_##objectParent##21)

//////////////////////////////// END BOOLEAN OBJECT /////////////////////////////////

//////////////////////////////// BEGIN DOUBLE OBJECT /////////////////////////////////
#define READ_DOUBLE_21_OR_ELSE_GOTO_RESUME(bsonType, objectParent, objectName, onErrorGoto) \
  if (!bson_append_double(bsonType, CWS_CONST_BSON_KEY(#objectName), objectParent->objectName)) { \
    set_w21_error_message(soap, E_W21_ERROR_DOUBLE_REQUIRED, "Could not add required double in " #objectName " object at " #objectParent); \
    goto onErrorGoto##_resume; \
  } \
  CAPTURE_STAT(double)

#define READ_O_DOUBLE_21_OR_ELSE_GOTO_RESUME(objectParent, objectName) \
  READ_DOUBLE_21_OR_ELSE_GOTO_RESUME(&child, objectParent, objectName, bson_read_##objectParent##_21)

#define READ_A_DOUBLE_21_OR_ELSE_GOTO_RESUME(objectParent, objectName) \
  READ_DOUBLE_21_OR_ELSE_GOTO_RESUME(&document_in_array, objectParent, objectName, bson_read_array_of_##objectParent##_21)

//Used for array function
#define READ_DOUBLE_NULLABLE_21_OR_ELSE_GOTO_RESUME(thisBson, objectParent, objectName, onErrorGoto) \
  if (objectParent->objectName) { \
    if (!bson_append_double(thisBson, CWS_CONST_BSON_KEY(#objectName), *(objectParent->objectName))) \
    { \
      set_w21_error_message(soap, E_W21_ERROR_DOUBLE_NULLABLE, "Could not add double nullable in " #objectName " object at " #objectParent); \
      goto onErrorGoto##_resume; \
    } \
    CAPTURE_STAT(double) \
  }

#define READ_A_DOUBLE_NULLABLE_21_OR_ELSE_GOTO_RESUME(objectParent, objectName) \
  READ_DOUBLE_NULLABLE_21_OR_ELSE_GOTO_RESUME(&document_in_array, objectParent, objectName, bson_read_array_of_##objectParent##_21)

#define READ_W_DOUBLE_NULLABLE_21_OR_ELSE_GOTO_RESUME(objectParent, objectName) \
  READ_DOUBLE_NULLABLE_21_OR_ELSE_GOTO_RESUME(&root_document, objectParent, objectName, bson_read_##objectParent##21)

#define READ_O_DOUBLE_NULLABLE_21_OR_ELSE_GOTO_RESUME(objectParent, objectName) \
  READ_DOUBLE_NULLABLE_21_OR_ELSE_GOTO_RESUME(&child, objectParent, objectName, bson_read_##objectParent##_21)

//////////////////////////////// BEGIN DOUBLE OBJECT /////////////////////////////////

/////////////////////////// BEGIN MEASURE OBJECT  ////////////////////////////

//Used for array function
#define READ_A_OBJECT_ENUM_21_OR_ELSE_GOTO_RESUME(ns, objectParent, objectName, enumFunctionName) \
  if (!bson_append_utf8(&document_in_array, CWS_CONST_BSON_KEY(#objectName), soap_##ns##__##enumFunctionName##2s(soap, objectParent->objectName), -1)) { \
    set_w21_error_message(soap, E_W21_ERROR_ADD_ENUM_REQUIRED_IN_ARRAY_OBJECT, "Could not set enum required of function name " #enumFunctionName " at " #objectParent " in " #objectName); \
    goto bson_read_array_of_##objectParent##_21_resume; \
  } \
  CAPTURE_STAT(enum)

///////////////////////////// END MEASURE OBJECT  ////////////////////////////

/////////////////////// BEGIN MEASURE OBJECT BUILDER WITH ENUM ////////////////////////

#define BSON_READ_STRING_MEASURE_BUILDER_21(ns, type) \
static \
int bson_read_##type##_21( \
  struct soap *soap, \
  bson_t *bson, \
  const char *key, int key_length, \
  struct ns##__##type *type \
) \
{ \
  bson_t child; \
\
  if (!type) \
    W21_RETURN \
\
  if (!bson_append_document_begin(bson, key, key_length, &child)) { \
    set_w21_error_message(soap, E_W21_ERROR_STRING_MEASURE_BEGIN, "Could begin BSON string measure object of type " #type " in %s", key); \
    W21_RETURN \
  } \
\
  if (bson_put_single_attribute_if_exists( \
    &child, \
    CWS_CONST_BSON_KEY("uom"), \
    type->uom, -1)) { \
      set_w21_error_message(soap, E_W21_ERROR_STRING_MEASURE_UOM_ATTRIBUTE, "Could not set BSON measure " #type " UOM attribute in %s.", key); \
      goto bson_read_##type##_21_resume; \
  }\
\
  if ((type->__item != NULL) && (!bson_append_utf8(&child, KEY_USCORE_VALUE, type->__item, -1))) { \
    set_w21_error_message(soap, E_W21_ERROR_STRING_MEASURE_APPEND, "Could append BSON string value of type " #type " in %s", key); \
    goto bson_read_##type##_21_resume; \
  } \
\
  CAPTURE_STAT_ON_SUCCESS(measure) \
\
  if (!bson_append_document_end(bson, &child)) \
    set_w21_error_message(soap, E_W21_ERROR_STRING_MEASURE_END, "Could end BSON string measure object of type " #type " in %s", key); \
\
  W21_RETURN \
\
bson_read_##type##_21_resume: \
\
bson_append_document_end(bson, &child); \
\
  W21_RETURN \
}

//required uom and value (__item)
#define BSON_READ_MEASURE_BUILDER_21_D(ns, type, enumTypeSuffixFunction) \
static \
int bson_read_##type##_21( \
  struct soap *soap, \
  bson_t *bson, \
  const char *key, int key_length, \
  struct ns##__##type *type \
) \
{ \
  bson_t child; \
\
  if (!type) \
    W21_RETURN \
\
  if (!bson_append_document_begin(bson, key, key_length, &child)) { \
    set_w21_error_message(soap, E_W21_ERROR_MEASURE_WITH_ENUM, "Could begin BSON measure object of type " #type " in %s", key); \
    W21_RETURN \
  } \
\
  if (bson_put_single_attribute_required( \
    &child, \
    CWS_CONST_BSON_KEY("uom"), \
    (char *)soap_##ns##__##enumTypeSuffixFunction##2s(soap, type->uom), -1)) { \
      set_w21_error_message(soap, E_W21_ERROR_MEASURE_UOM_ATTRIBUTE_WITH_ENUM, "Could not set BSON required measure enum UOM attribute in %s.", key); \
      goto bson_read_##type##_21_resume; \
  }\
\
  if (!bson_append_double(&child, KEY_USCORE_VALUE, type->__item)) { \
    set_w21_error_message(soap, E_W21_ERROR_MEASURE_APPEND_DOUBLE, "Could not append BSON double value in %s", key); \
    goto bson_read_##type##_21_resume; \
  } \
\
  CAPTURE_STAT_ON_SUCCESS(measure) \
\
  if (!bson_append_document_end(bson, &child)) \
    set_w21_error_message(soap, E_W21_ERROR_END_APPEND_DOUBLE, "Could not end BSON measure of type " #type " in key %s", key); \
\
  W21_RETURN \
\
bson_read_##type##_21_resume: \
  bson_append_document_end(bson, &child); \
\
  W21_RETURN \
}

//required uom and value (__item)
#define BSON_READ_MEASURE_BUILDER_21_E(ns, type) \
static \
int bson_read_##type##_21( \
  struct soap *soap, \
  bson_t *bson, \
  const char *key, int key_length, \
  struct ns##__##type *type \
) \
{ \
  bson_t child; \
\
  if (!type) \
    W21_RETURN \
\
  if (!bson_append_document_begin(bson, key, key_length, &child)) { \
    set_w21_error_message(soap, E_W21_ERROR_MEASURE_NO_ENUM, "(ext) Could begin BSON measure object of type " #type " in %s", key); \
    W21_RETURN \
  } \
\
  if (bson_put_single_attribute_required( \
    &child, \
    CWS_CONST_BSON_KEY("uom"), \
    type->uom, -1)) { \
      set_w21_error_message(soap, E_W21_ERROR_MEASURE_UOM_ATTRIBUTE_TYPE_D, "(ext) Could not set BSON required measure UOM attribute in %s.", key); \
      goto bson_read_##type##_21_resume; \
  }\
\
  if (!bson_append_double(&child, KEY_USCORE_VALUE, type->__item)) { \
    set_w21_error_message(soap, E_W21_ERROR_MEASURE_APPEND_DOUBLE, "(ext) Could not append BSON double value in %s", key); \
    goto bson_read_##type##_21_resume; \
  } \
\
  CAPTURE_STAT_ON_SUCCESS(measure) \
\
  if (!bson_append_document_end(bson, &child)) \
    set_w21_error_message(soap, E_W21_ERROR_END_APPEND_DOUBLE, "(ext) Could not end BSON measure of type " #type " in key %s", key); \
\
  W21_RETURN \
\
bson_read_##type##_21_resume: \
  bson_append_document_end(bson, &child); \
\
  W21_RETURN \
}

//required uom and value (__item)
#define BSON_READ_MEASURE_BUILDER_21_C(ns, type) \
static \
int bson_read_##type##_21( \
  struct soap *soap, \
  bson_t *bson, \
  const char *key, int key_length, \
  struct ns##__##type *type \
) \
{ \
  bson_t child; \
\
  if (!type) \
    W21_RETURN \
\
  if (!bson_append_document_begin(bson, key, key_length, &child)) { \
    set_w21_error_message(soap, E_W21_ERROR_MEASURE, "Could begin BSON measure object of type " #type " in %s", key); \
    W21_RETURN \
  } \
\
  if (bson_put_single_attribute_required( \
    &child, \
    CWS_CONST_BSON_KEY("uom"), \
    type->uom, -1)) { \
      set_w21_error_message(soap, E_W21_ERROR_MEASURE_UOM_ATTRIBUTE_REQUIRED, "Could not set BSON required measure UOM attribute of type " #type " in %s.", key); \
      goto bson_read_##type##_21_resume; \
  }\
\
  if (!bson_append_double(&child, KEY_USCORE_VALUE, type->__item)) { \
    set_w21_error_message(soap, E_W21_ERROR_MEASURE_APPEND_DOUBLE, "Could not append BSON of type " #type " double value in %s", key); \
    goto bson_read_##type##_21_resume; \
  } \
\
  CAPTURE_STAT_ON_SUCCESS(measure) \
\
  if (!bson_append_document_end(bson, &child)) \
    set_w21_error_message(soap, E_W21_ERROR_END_APPEND_DOUBLE, "Could not end BSON measure of type " #type " in key %s", key); \
\
  W21_RETURN \
\
bson_read_##type##_21_resume: \
  bson_append_document_end(bson, &child); \
\
  W21_RETURN \
}

#define BSON_READ_ARRAY_OF_MEASURE_BUILDER_21_C(ns, type) \
static \
int bson_read_array_of_##type##_21( \
  struct soap *soap, \
  bson_t *bson, \
  const char *key, int key_length, \
  int numberOfObjectsInArray, \
  struct ns##__##type *type \
) \
{ \
\
  if ((numberOfObjectsInArray < 1) || (type == NULL)) \
    W21_RETURN; \
\
  bson_array_builder_t *bab; \
\
  DECLARE_CAPTURE_STAT \
\
  if (!bson_append_array_builder_begin(bson, key, key_length, &bab)) { \
    set_w21_error_message(soap, E_W21_ERROR_BEGIN_ARRAY_OF_MEASURE_OBJECT, "Could not build BSON array of measure document array at %s of type " #type, key); \
    W21_RETURN; \
  } \
\
  bson_t document_in_array; \
\
  do { \
    if (!type) { \
      set_w21_error_message(soap, E_W21_ERROR_UNEXPECTED_NULL_POINTER_IN_ARRAY_OF_MEASURE_OBJECT, "Unexpected null pointer in array of measure at %s of type " #type, key); \
      goto bson_read_array_of_##type##_21_resume_2; \
    } \
\
    if (!bson_array_builder_append_document_begin(bab, &document_in_array)) { \
      set_w21_error_message(soap, E_W21_ERROR_UNABLE_TO_CREATE_MEASURE_DOCUMENT_IN_ARRAY, "Could not initialize measure object document in array at %s of type " #type, key); \
      goto bson_read_array_of_##type##_21_resume_2; \
    } \
\
    if (bson_put_single_attribute_required( \
      &document_in_array, \
      CWS_CONST_BSON_KEY("uom"), \
      type->uom, -1)) { \
      set_w21_error_message(soap, E_W21_ERROR_ARRAY_OF_MEASURE_UOM_ATTRIBUTE_REQUIRED, "Could not set BSON required array of measure UOM attribute of type " #type " in %s.", key); \
      goto bson_read_array_of_##type##_21_resume; \
    }\
\
    if (!bson_append_double(&document_in_array, KEY_USCORE_VALUE, type->__item)) { \
      set_w21_error_message(soap, E_W21_ERROR_ARRAY_OF_MEASURE_APPEND_DOUBLE, "Could not append BSON array of measure type " #type " double value in %s", key); \
      goto bson_read_array_of_##type##_21_resume; \
    } \
\
    if (!bson_array_builder_append_document_end(bab, &document_in_array)) { \
      set_w21_error_message(soap, E_W21_ERROR_UNABLE_TO_END_MEASURE_DOCUMENT_IN_ARRAY, "Could not end measure object document in array at %s of type " #type, key); \
      goto bson_read_array_of_##type##_21_resume_2; \
    } \
\
    ++type; \
  } while ((--numberOfObjectsInArray) > 0); \
\
  CAPTURE_STAT_ON_SUCCESS_A(n, measure) \
\
  if (!bson_append_array_builder_end(bson, bab)) \
    set_w21_error_message(soap, E_W21_ERROR_END_ARRAY_OF_MEASURE_OBJECT, "Could not end BSON measure document array at %s of type " #type, key); \
\
  W21_RETURN \
\
bson_read_array_of_##type##_21_resume: \
  bson_array_builder_append_document_end(bab, &document_in_array); \
\
bson_read_array_of_##type##_21_resume_2: \
  bson_append_array_builder_end(bson, bab); \
\
  W21_RETURN \
}

//required uom and value (__item)
#define BSON_READ_UNITLESS_MEASURE_BUILDER_21(ns, type) \
static \
int bson_read_##type##_21( \
  struct soap *soap, \
  bson_t *bson, \
  const char *key, int key_length, \
  struct ns##__##type *type \
) \
{ \
  bson_t child; \
\
  if (!type) \
    W21_RETURN \
\
  if (!bson_append_document_begin(bson, key, key_length, &child)) { \
    set_w21_error_message(soap, E_W21_ERROR_UNITLESS_MEASURE, "Could begin BSON unitless measure object of type " #type " in %s", key); \
    W21_RETURN \
  } \
\
  if (!bson_append_double(&child, KEY_USCORE_VALUE, type->__item)) { \
    set_w21_error_message(soap, E_W21_ERROR_UNITLESS_MEASURE_APPEND_DOUBLE, "Could not append BSON unitless of type " #type " double value in %s", key); \
    goto bson_read_##type##_21_resume; \
  } \
\
  CAPTURE_STAT_ON_SUCCESS(measure) \
\
  if (!bson_append_document_end(bson, &child)) \
    set_w21_error_message(soap, E_W21_ERROR_END_APPEND_UNITLESS_DOUBLE, "Could not end BSON unitless measure of type " #type " in key %s", key); \
\
  W21_RETURN \
\
bson_read_##type##_21_resume: \
  bson_append_document_end(bson, &child); \
\
  W21_RETURN \
}

///////////////////////// END MEASURE OBJECT BUILDER WITH ENUM ///////////////////////////

//////////////////// BEGIN BSON_READ_ARRAY_OF_OBJECT_BUILDER_21 //////////////////////////
#define BSON_READ_ARRAY_OF_OBJECT_BUILDER_21_BEGIN(ns, type) \
static \
int bson_read_array_of_##type##_21( \
  struct soap *soap, \
  bson_t *bson, \
  const char *key, int key_length, \
  int numberOfObjectsInArray, \
  struct ns##__##type *type \
) \
{ \
\
  if ((numberOfObjectsInArray < 1) || (type == NULL)) \
    W21_RETURN; \
\
  bson_array_builder_t *bab; \
\
  DECLARE_CAPTURE_STAT \
\
  if (!bson_append_array_builder_begin(bson, key, key_length, &bab)) { \
    set_w21_error_message(soap, E_W21_ERROR_BEGIN_ARRAY_OF_COMPLEX_OBJECT, "Could not build BSON complex document array at %s of type " #type, key); \
    W21_RETURN; \
  } \
\
  bson_t document_in_array; \
\
  do { \
    if (!type) { \
      set_w21_error_message(soap, E_W21_ERROR_UNEXPECTED_NULL_POINTER_IN_COMPLEX_OBJECT_ARRAY, "Unexpected null pointer in complex array at %s of type " #type, key); \
      goto bson_read_array_of_##type##_21_resume_2; \
    } \
\
    if (!bson_array_builder_append_document_begin(bab, &document_in_array)) { \
      set_w21_error_message(soap, E_W21_ERROR_UNABLE_TO_CREATE_COMPLEX_DOCUMENT_IN_ARRAY, "Could not initialize complex object document in array at %s of type " #type, key); \
      goto bson_read_array_of_##type##_21_resume_2; \
    }

#define BSON_READ_ARRAY_OF_OBJECT_BUILDER_21_END(type) \
    if (!bson_array_builder_append_document_end(bab, &document_in_array)) { \
      set_w21_error_message(soap, E_W21_ERROR_UNABLE_TO_END_COMPLEX_DOCUMENT_IN_ARRAY, "Could not end complex object document in array at %s of type " #type, key); \
      goto bson_read_array_of_##type##_21_resume_2; \
    } \
    ++type; \
  } while ((--numberOfObjectsInArray) > 0); \
\
  CAPTURE_STAT_ON_SUCCESS_A(n, array) \
\
  if (!bson_append_array_builder_end(bson, bab)) \
    set_w21_error_message(soap, E_W21_ERROR_END_ARRAY_OF_COMPLEX_OBJECT, "Could not end BSON complex document array at %s of type " #type, key); \
\
  W21_RETURN \
\
bson_read_array_of_##type##_21_resume: \
  bson_array_builder_append_document_end(bab, &document_in_array); \
\
bson_read_array_of_##type##_21_resume_2: \
  bson_append_array_builder_end(bson, bab); \
\
  W21_RETURN \
}

//////////////////// END BSON_READ_ARRAY_OF_OBJECT_BUILDER_21 //////////////////////////

//////////////////////////// BEGIN BSON ATTRIBUTE IN COMPLEX ARRAY ///////////////////////////

#define READ_A_PUT_SINGLE_ATTR_21_OR_ELSE_GOTO_RESUME(objectParent, objectName) \
  if (bson_put_single_attribute_if_exists(&document_in_array, CWS_CONST_BSON_KEY(#objectName), objectParent->objectName, -1)) { \
    set_w21_error_message(soap, E_W21_ERROR_ADD_SINGLE_ATTRIBUTE_IN_ARRAY_OF_COMPLEX_OBJECT, "Could not set single attribute in complex array " #objectName " in " #objectParent); \
    goto bson_read_array_of_##objectParent##_21_resume; \
  }

//Use for array function
#define READ_A_OBJECT_21_OR_ELSE_GOTO_RESUME(objectParent, objectName, typeName) \
  if (bson_read_##typeName##_21(soap, &document_in_array, CWS_CONST_BSON_KEY(#objectName), objectParent->objectName)) \
    goto bson_read_array_of_##objectParent##_21_resume;

#define READ_OBJECT_21_OR_ELSE_GOTO_RESUME_B(ns, thisBson, objectParent, objectName, typeName, onErrorGoto) \
  if (bson_read_##typeName##_21(soap, thisBson, CWS_CONST_BSON_KEY(#objectName), objectParent->ns##__##objectName)) \
    goto onErrorGoto##_resume;

#define READ_A_OBJECT_21_OR_ELSE_GOTO_RESUME_B(ns, objectParent, objectName, typeName) \
  READ_OBJECT_21_OR_ELSE_GOTO_RESUME_B(ns, &document_in_array, objectParent, objectName, typeName, bson_read_array_of_##objectParent##_21)

//Use for array function
#define READ_A_ARRAY_OF_OBJECT_21_OR_ELSE_GOTO_RESUME(objectParent, objectName, arrayTypeName) \
  if (bson_read_array_of_##arrayTypeName##_21( \
    soap, \
    &document_in_array, \
    CWS_CONST_BSON_KEY(#objectName), \
    objectParent->__size##objectName, \
    objectParent->objectName)) goto bson_read_array_of_##objectParent##_21_resume;

//Use for array function
#define READ_A_ARRAY_OF_OBJECT_ALIAS_21_OR_ELSE_GOTO_RESUME(objectParent, objectName, objectAlias, arrayTypeName) \
  if (bson_read_array_of_##arrayTypeName##_21( \
    soap, \
    &document_in_array, \
    CWS_CONST_BSON_KEY(#objectAlias), \
    objectParent->__size##objectName, \
    objectParent->objectName)) goto bson_read_array_of_##objectParent##_21_resume;

#define READ_ARRAY_OF_OBJECT_21_OR_ELSE_GOTO_RESUME_B(ns, thisBson, objectParent, objectName, arrayTypeName, onErrorGoto) \
  if (bson_read_array_of_##arrayTypeName##_21( \
    soap, \
    thisBson, \
    CWS_CONST_BSON_KEY(#objectName), \
    objectParent->__size##objectName, \
    objectParent->ns##__##objectName)) goto onErrorGoto##_resume;

#define READ_A_ARRAY_OF_OBJECT_21_OR_ELSE_GOTO_RESUME_B(ns, objectParent, objectName, arrayTypeName) \
    READ_ARRAY_OF_OBJECT_21_OR_ELSE_GOTO_RESUME_B(ns, &document_in_array, objectParent, objectName, arrayTypeName, bson_read_array_of_##objectParent##_21)

/*
//Used for array function
#define READ_A_OBJECT_ENUM_NULLABLE_21_OR_ELSE_GOTO_RESUME(ns, objectParent, objectName, enumFunctionName) \
  if (objectParent->objectName) { \
    if (!bson_append_utf8(&document_in_array, CWS_CONST_BSON_KEY(#objectName), soap_##ns##__##enumFunctionName##2s(soap, *(objectParent->objectName)), -1)) { \
      set_w21_error_message(soap, E_W21_ERROR_ADD_ENUM_NULLABLE_IN_DOCUMENT_ARRAY, "Could not set enum nullable value in array document " #objectName " in " #objectParent); \
      goto bson_read_array_of_##objectParent##_21_resume; \
    } \
    CAPTURE_STAT(enum) \
  }
*/
#define READ_A_OBJECT_ENUM_NULLABLE_21_OR_ELSE_GOTO_RESUME(ns, objectParent, objectName, enumFunctionName) \
  READ_OBJECT_ENUM_NULLABLE_21_OR_ELSE_GOTO_RESUME(ns, &document_in_array, objectParent, objectName, enumFunctionName, bson_read_array_of_##objectParent##_21)

#define READ_O_OBJECT_ENUM_NULLABLE_21_OR_ELSE_GOTO_RESUME(ns, objectParent, objectName, enumFunctionName) \
  READ_OBJECT_ENUM_NULLABLE_21_OR_ELSE_GOTO_RESUME(ns, &child, objectParent, objectName, enumFunctionName, bson_read_##objectParent##_21)

#define READ_A_LONG64_NULLABLE_21_OR_ELSE_GOTO_RESUME(objectParent, objectName) \
  if (objectParent->objectName) { \
    if (!bson_append_int64(&document_in_array, CWS_CONST_BSON_KEY(#objectName), (int64_t)*(objectParent->objectName))) { \
      set_w21_error_message(soap, E_W21_ERROR_ADD_LONG64_NULLABLE_IN_DOCUMENT_ARRAY, "Could not set LONG 64 nullable value in array document " #objectName " in " #objectParent); \
      goto bson_read_array_of_##objectParent##_21_resume; \
    } \
    CAPTURE_STAT(long64) \
  }

#define READ_LONG64_21_OR_ELSE_GOTO_RESUME(bsonType, objectParent, objectName, onErrorGoto) \
  if (!bson_append_int64(bsonType, CWS_CONST_BSON_KEY(#objectName), (int64_t)objectParent->objectName)) { \
      set_w21_error_message(soap, E_W21_ERROR_ADD_LONG64_REQUIRED, "Could not set LONG 64 required value in " #objectName " in " #objectParent); \
    goto onErrorGoto##_resume; \
  } \
  CAPTURE_STAT(long64)

#define READ_O_LONG64_21_OR_ELSE_GOTO_RESUME(objectParent, objectName) \
  READ_LONG64_21_OR_ELSE_GOTO_RESUME(&child, objectParent, objectName, bson_read_##objectParent##_21)

#define READ_A_LONG64_21_OR_ELSE_GOTO_RESUME(objectParent, objectName) \
  READ_LONG64_21_OR_ELSE_GOTO_RESUME(&document_in_array, objectParent, objectName, bson_read_array_of_##objectParent##_21)

#define READ_W_LONG64_21_OR_ELSE_GOTO_RESUME(objectParent, objectName) \
  READ_LONG64_21_OR_ELSE_GOTO_RESUME(&root_document, objectParent, objectName, bson_read_##objectParent##21)

//////////////////////////// END BSON ATTRIBUTE IN COMPLEX ARRAY ///////////////////////////

///////////////////////////// BEGIN BSON SET UTF8 IN ARRAY FUNCTION ///////////////////////

//Used for array function
#define READ_A_UTF8_OBJECT_21_OR_ELSE_GOTO_RESUME(objectParent, objectName) \
  if (objectParent->objectName) { \
    if (!bson_append_utf8(&document_in_array, CWS_CONST_BSON_KEY(#objectName), (const char *)objectParent->objectName, -1)) { \
      set_w21_error_message(soap, E_W21_ERROR_ADD_UTF8_IN_ARRAY_OF_COMPLEX_OBJECT, "Could not set utf-8 value in array " #objectName " in " #objectParent); \
      goto bson_read_array_of_##objectParent##_21_resume; \
    } \
    CAPTURE_STAT(string) \
  }

///////////////////////////// END BSON SET UTF8 IN ARRAY FUNCTION ///////////////////////




/////////////////////////////// BEGIN TIME NULLABLE IN ARRAY /////////////////////////////

//Used for array function
#define READ_A_TIME_NULLABLE_21_OR_ELSE_GOTO_RESUME(objectParent, objectName) \
  if (objectParent->objectName) { \
    if (!bson_append_time_t(&document_in_array, CWS_CONST_BSON_KEY(#objectName), *(objectParent->objectName))) { \
      set_w21_error_message(soap, E_W21_ERROR_ADD_TIME_NULLABLE_IN_ARRAY_OF_COMPLEX_OBJECT, "Could not set date-time nullable value in array " #objectName " in " #objectParent); \
      goto bson_read_array_of_##objectParent##_21_resume; \
    } \
    CAPTURE_STAT(date_time) \
  }

#define READ_TIME_21_OR_ELSE_GOTO_RESUME_B(ns, thisBson, objectParent, objectName, onErrorGoto) \
  if (!bson_append_time_t(thisBson, CWS_CONST_BSON_KEY(#objectName), objectParent->ns##__##objectName)) { \
      set_w21_error_message(soap, E_W21_ERROR_ADD_TIME_REQUIRED_IN_ARRAY_OF_COMPLEX_OBJECT, "Could not set date-time required value in array " #objectName " in " #objectParent); \
    goto onErrorGoto##_resume; \
  } \
  CAPTURE_STAT(date_time)

#define READ_O_TIME_21_OR_ELSE_GOTO_RESUME_B(ns, objectParent, objectName) \
  READ_TIME_21_OR_ELSE_GOTO_RESUME_B(ns, &child, objectParent, objectName, bson_read_##objectParent##_21)

#define READ_W_TIME_21_OR_ELSE_GOTO_RESUME_B(ns, objectParent, objectName) \
  READ_TIME_21_OR_ELSE_GOTO_RESUME_B(ns, &root_document, objectParent, objectName, bson_read_##objectParent##21)

//Used for array function
#define READ_A_TIME_21_OR_ELSE_GOTO_RESUME_B(ns, objectParent, objectName) \
  READ_TIME_21_OR_ELSE_GOTO_RESUME_B(ns, &document_in_array, objectParent, objectName, bson_read_array_of_##objectParent##_21)
/////////////////////////////// END TIME NULLABLE IN ARRAY /////////////////////////////

#define READ_ARRAY_OF_OBJECT_21_OR_ELSE_GOTO_RESUME(bsonType, objectParent, objectName, arrayTypeName, onErrorGoto) \
  if (bson_read_array_of_##arrayTypeName##_21( \
    soap, \
    bsonType, \
    CWS_CONST_BSON_KEY(#objectName), \
    objectParent->__size##objectName, \
    objectParent->objectName)) goto onErrorGoto##_resume;

//used for ROOT Witsml object
#define READ_W_ARRAY_OF_OBJECT_21_OR_ELSE_GOTO_RESUME_B(ns, objectParent, objectName, arrayTypeName) \
  READ_ARRAY_OF_OBJECT_21_OR_ELSE_GOTO_RESUME_B(ns, &root_document, objectParent, objectName, arrayTypeName, bson_read_##objectParent##21)

//used for ROOT Witsml object
#define READ_W_ARRAY_OF_OBJECT_21_OR_ELSE_GOTO_RESUME(objectParent, objectName, arrayTypeName) \
  READ_ARRAY_OF_OBJECT_21_OR_ELSE_GOTO_RESUME(&root_document, objectParent, objectName, arrayTypeName, bson_read_##objectParent##21)

#define READ_O_ARRAY_OF_OBJECT_21_OR_ELSE_GOTO_RESUME_B(ns, objectParent, objectName, arrayTypeName) \
  READ_ARRAY_OF_OBJECT_21_OR_ELSE_GOTO_RESUME_B(ns, &child, objectParent, objectName, arrayTypeName, bson_read_##objectParent##_21)

///////////////////////////////// END ARRAY OF OBJECT //////////////////////////////////

/////////////////////////////// BEGIN COMPLEX OBJECT BUILDER ////////////////////////////

#define BSON_READ_OBJECT_BUILDER_21_BEGIN(ns, type) \
static \
int bson_read_##type##_21( \
  struct soap *soap, \
  bson_t *bson, \
  const char *key, int key_length, \
  struct ns##__##type *type \
) \
{ \
\
  if (!type) \
    W21_RETURN \
\
  bson_t \
    child; \
\
  if (!bson_append_document_begin(bson, key, key_length, &child)) { \
    set_w21_error_message(soap, E_W21_ERROR_COMPLEX_OBJECT_BEGIN, "Could not begin BSON in complex object " #type " %s", key); \
    W21_RETURN \
  }

#define BSON_READ_OBJECT_BUILDER_21_END(type) \
  if (!bson_append_document_end(bson, &child)) \
    set_w21_error_message(soap, E_W21_ERROR_COMPLEX_OBJECT_END, "Could not end BSON in complex object " #type " %s", key); \
\
  W21_RETURN \
\
bson_read_##type##_21_resume: \
\
  bson_append_document_end(bson, &child); \
\
  W21_RETURN \
}

#define BSON_READ_ABSTRACT_OBJECT_ROOT_BUILDER_21_BEGIN(ns, type) \
static \
int bson_read_abstract_root_##type##_21( \
  struct soap *soap, \
  bson_t *bson, \
  const char *key, int key_length, \
  struct ns##__##type *type \
) \
{ \
\
  if (!type) \
    W21_RETURN \
\
  bson_t \
    child; \
\
  if (!bson_append_document_begin(bson, key, key_length, &child)) { \
    set_w21_error_message(soap, E_W21_ERROR_ABSTRACT_ROOT_COMPLEX_OBJECT_BEGIN, "Could not begin BSON in abstract root complex object " #type " %s", key); \
    W21_RETURN \
  }

#define BSON_READ_ABSTRACT_OBJECT_ROOT_BUILDER_21_END(type) \
  if (!bson_append_document_end(bson, &child)) \
    set_w21_error_message(soap, E_W21_ERROR_COMPLEX_OBJECT_END, "Could not end BSON in abstract root complex object " #type " %s", key); \
\
  W21_RETURN \
\
bson_read_abstract_root_##type##_21_resume: \
\
  bson_append_document_end(bson, &child); \
\
  W21_RETURN \
}

/////////////////////////////// END COMPLEX OBJECT BUILDER ////////////////////////////

/////////////////////////////// BEGIN READ COMPLEX OBJECT //////////////////////////////

//Use for object function
#define READ_O_OBJECT_21_OR_ELSE_GOTO_RESUME(objectParent, objectName, typeName) \
  if (bson_read_##typeName##_21(soap, &child, CWS_CONST_BSON_KEY(#objectName), objectParent->objectName)) \
    goto bson_read_##objectParent##_21_resume;

//Used for object function
#define READ_O_ARRAY_OF_OBJECT_21_OR_ELSE_GOTO_RESUME(objectParent, objectName, arrayTypeName) \
  if (bson_read_array_of_##arrayTypeName##_21( \
    soap, \
    &child, \
    CWS_CONST_BSON_KEY(#objectName), \
    objectParent->__size##objectName, \
    objectParent->objectName)) goto bson_read_##objectParent##_21_resume;

//Used for object function
#define READ_O_UTF8_OBJECT_21_OR_ELSE_GOTO_RESUME(objectParent, objectName) \
  if (objectParent->objectName) { \
    if (!bson_append_utf8(&child, CWS_CONST_BSON_KEY(#objectName), (const char *)objectParent->objectName, -1)) { \
      set_w21_error_message(soap, E_W21_ERROR_END_BSON_ROOT_OBJECT, "Could add utf-8 in " #objectName " in " #objectParent); \
      goto bson_read_##objectParent##_21_resume; \
    } \
    CAPTURE_STAT(string) \
  }

//Used for abstract root object function
#define READ_O_UTF8_OBJECT_IN_ABSTRACT_ROOT_21_OR_ELSE_GOTO_RESUME(objectParent, objectName) \
  if (objectParent->objectName) { \
    if (!bson_append_utf8(&child, CWS_CONST_BSON_KEY(#objectName), (const char *)objectParent->objectName, -1)) { \
      set_w21_error_message(soap, E_W21_ERROR_END_BSON_ABSTRACT_ROOT_OBJECT, "Could add utf-8 in abstract root object " #objectName " in " #objectParent); \
      goto bson_read_abstract_root_##objectParent##_21_resume; \
    } \
    CAPTURE_STAT(string) \
  }

//used for ROOT Witsml object
#define READ_W_OBJECT_21_OR_ELSE_GOTO_RESUME_B(ns, objectParent, objectName, typeName) \
  READ_OBJECT_21_OR_ELSE_GOTO_RESUME_B(ns, &root_document, objectParent, objectName, typeName, bson_read_##objectParent##21)

#define READ_O_OBJECT_21_OR_ELSE_GOTO_RESUME_B(ns, objectParent, objectName, typeName) \
  READ_OBJECT_21_OR_ELSE_GOTO_RESUME_B(ns, &child, objectParent, objectName, typeName, bson_read_##objectParent##_21)

#define READ_OBJECT_21_OR_ELSE_GOTO_RESUME(bsonType, objectParent, objectName, typeName, onErrorGoto) \
  if (bson_read_##typeName##_21(soap, bsonType, CWS_CONST_BSON_KEY(#objectName), objectParent->objectName)) \
    goto onErrorGoto##_resume;

#define READ_ABSTRACT_OBJECT_ROOT_21_OR_ELSE_GOTO_RESUME(bsonType, objectParent, objectName, typeName, onErrorGoto) \
  if (bson_read_abstract_root_##typeName##_21(soap, bsonType, CWS_CONST_BSON_KEY(#objectName), objectParent->objectName)) \
    goto onErrorGoto##_resume;

#define BSON_READ_TRANSIENT_OBJECT_ROOT_BUILDER_21_BEGIN(ns, type) \
static \
int bson_read_transient_##type##_21( \
  struct soap *soap, \
  bson_t *bsonObject, \
  struct ns##__##type *type \
) \
{ \
\
  if (!type) \
    W21_RETURN

#define BSON_READ_TRANSIENT_OBJECT_ROOT_BUILDER_21_END(type) \
  W21_RETURN \
}

#define READ_T_TIME_21_OR_ELSE_GOTO_RESUME(objectParent, objectName) \
  if (!bson_append_time_t(bsonObject, CWS_CONST_BSON_KEY(#objectName), objectParent->objectName)) { \
      set_w21_error_message(soap, E_W21_ERROR_ADD_TIME_REQUIRED_TRANSIENT_OBJECT, "Could not set date-time required value in transient " #objectName " in " #objectParent); \
    W21_RETURN \
  } \
  CAPTURE_STAT(date_time)

#define READ_T_OBJECT_21_VOID(objectParent, objectName, typeName) \
  bson_read_##typeName##_21(soap, bsonObject, CWS_CONST_BSON_KEY(#objectName), objectParent->objectName);

#define READ_T_TRANSIENT_OBJECT_21_OR_ELSE_GOTO_RESUME(ns, objectParent, objectName, typeName) \
  if (objectParent->ns##__##objectName) \
    if (bson_read_transient_##typeName##_21(soap, bsonObject, objectParent->ns##__##objectName)) { \
      W21_RETURN \
    }

#define READ_T_TRANSIENT_OBJECT_21_VOID(ns, objectParent, objectName, typeName) \
  if (objectParent->ns##__##objectName) \
    bson_read_transient_##typeName##_21(soap, bsonObject, objectParent->ns##__##objectName);

#define READ_T_TRANSIENT_OBJECT_21_VOID_B(objectParent, objectName, typeName) \
  if (objectParent->objectName) \
    bson_read_transient_##typeName##_21(soap, bsonObject, objectParent->objectName);

#define READ_T_OBJECT_21_OR_ELSE_GOTO_RESUME(objectParent, objectName, typeName) \
  if (bson_read_##typeName##_21(soap, bsonObject, CWS_CONST_BSON_KEY(#objectName), objectParent->objectName)) \
    W21_RETURN

#define READ_T_UTF8_OBJECT_ALIAS_21_OR_ELSE_GOTO_RESUME(objectParent, objectName, objectAlias) \
  if (objectParent->objectName) { \
    if (!bson_append_utf8(bsonObject, CWS_CONST_BSON_KEY(#objectAlias), (const char *)objectParent->objectName, -1)) { \
      set_w21_error_message(soap, E_W21_ERROR_SET_UTF8_STRING_TRANSIENT_IN_BSON_ALIAS, "Could not add BSON utf-8 transient alias string at " #objectParent " in " #objectAlias); \
      W21_RETURN \
    } \
    CAPTURE_STAT(string) \
  }

#define READ_T_UTF8_OBJECT_21_OR_ELSE_GOTO_RESUME(objectParent, objectName) \
  if (objectParent->objectName) { \
    if (!bson_append_utf8(bsonObject, CWS_CONST_BSON_KEY(#objectName), (const char *)objectParent->objectName, -1)) { \
      set_w21_error_message(soap, E_W21_ERROR_SET_UTF8_STRING_IN_TRANSIENT_BSON, #objectParent "Could not read " #objectName " of transient " #objectParent " object"); \
      W21_RETURN \
    } \
    CAPTURE_STAT(string) \
  }

#define READ_T_DOUBLE_21_OR_ELSE_GOTO_RESUME(objectParent, objectName) \
  if (!bson_append_double(bsonObject, CWS_CONST_BSON_KEY(#objectName), objectParent->objectName)) { \
    set_w21_error_message(soap, E_W21_ERROR_DOUBLE_TRANSIENT_REQUIRED, "Could not add required double in " #objectName " object at transient " #objectParent); \
    W21_RETURN \
  } \
  CAPTURE_STAT(double)

#define READ_TRANSIENT_OBJECT_IN_ABSTRACT_ROOT_21_OR_ELSE_GOTO_RESUME(ns, bsonObj, objectParent, typeName, onErrorGoto) \
  if (bson_read_transient_##typeName##_21(soap, bsonObj, objectParent->ns##__##typeName)) \
    goto onErrorGoto##_resume;

#define READ_O_TRANSIENT_OBJECT_IN_ABSTRACT_ROOT_21_OR_ELSE_GOTO_RESUME(ns, objectParent, typeName) \
  READ_TRANSIENT_OBJECT_IN_ABSTRACT_ROOT_21_OR_ELSE_GOTO_RESUME(ns, &child, objectParent, typeName, bson_read_abstract_root_##objectParent##_21)

//used for ROOT Witsml object
#define READ_W_OBJECT_21_OR_ELSE_GOTO_RESUME(objectParent, objectName, typeName) \
  READ_OBJECT_21_OR_ELSE_GOTO_RESUME(&root_document, objectParent, objectName, typeName, bson_read_##objectParent##21)

//used for ROOT Witsml object
#define READ_W_ABSTRACT_OBJECT_ROOT_21_OR_ELSE_GOTO_RESUME(objectParent, objectName, typeName) \
  READ_ABSTRACT_OBJECT_ROOT_21_OR_ELSE_GOTO_RESUME(&root_document, objectParent, objectName, typeName, bson_read_##objectParent##21)

//aqui
#define READ_A_ABSTRACT_OBJECT_ROOT_21_OR_ELSE_GOTO_RESUME(objectParent, objectName, typeName) \
  READ_ABSTRACT_OBJECT_ROOT_21_OR_ELSE_GOTO_RESUME(&document_in_array, objectParent, objectName, typeName, bson_read_array_of_##objectParent##_21)

#define READ_O_ABSTRACT_OBJECT_ROOT_21_OR_ELSE_GOTO_RESUME(objectParent, objectName, typeName) \
  READ_ABSTRACT_OBJECT_ROOT_21_OR_ELSE_GOTO_RESUME(&child, objectParent, objectName, typeName, bson_read_##objectParent##_21)
/////////////////////////////// END READ COMPLEX OBJECT //////////////////////////////

#define WITSML21_OBJECT_BEGIN_BASE(ns, object) \
int bson_read_##object##21(struct soap *soap) \
{ \
  struct ns##__##object *object=(struct ns##__##object *)(auto_detect_resolver(soap, W21_OBJECT_##object)); \
\
  DECLARE_W21_CONFIG \
\
  if ((object == NULL) || (W21_HAS_PREVIOUS_ERROR)) {\
    set_w21_error_message(soap, E_W21_ERROR_OBJECT_NULL_OR_PARSER_ERROR, "Could not parse to BSON: Root object " #object " is null or has error: %d. Did you parsed \"" #object "\" or other WITSML 2.1 object?", config->error); \
    W21_RETURN \
  } \
\
  if (config->in_bson) { \
    set_w21_error_message(soap, E_W21_ERROR_BSON_ALREADY_ALLOC, "Could not parse " #object " to BSON: Bson object already alloc'd"); \
    W21_RETURN \
  }

// Parent object
#define WITSML21_OBJECT_BEGIN(ns, object) \
  WITSML21_OBJECT_BEGIN_BASE(ns, object) \
  W21_CONSTRUCT_BSON(object)

#define WITSML21_OBJECT_END(object) \
  if (!bson_append_document_end((config)->in_bson, &root_document)) \
    set_w21_error_message(soap, E_W21_ERROR_END_BSON_ROOT_OBJECT, "Could not end BSON root document at " #object); \
\
  W21_RETURN \
\
 bson_read_##object##21_resume: \
  bson_append_document_end((config)->in_bson, &root_document); \
  W21_BSON_REINIT \
\
  W21_RETURN \
}

// Parent object recycled special case
#define WITSML21_OBJECT_BUILDER(ns, object) \
WITSML21_OBJECT_BEGIN_BASE(ns, object) \
W21_CONSTRUCT_BSON_B(object)\
\
if (bson_read_##object##_21(soap, config->in_bson, CWS_CONST_BSON_KEY(#object), object)) { \
  W21_BSON_REINIT \
} \
W21_RETURN \
}


// SINGLE ATTRIBUTE
#define READ_PUT_SINGLE_ATTR_21_OR_ELSE_GOTO_RESUME(bson, objectParent, objectName, onErrorGoto) \
  if (bson_put_single_attribute_if_exists( \
    bson, \
    CWS_CONST_BSON_KEY(#objectName), \
    objectParent->objectName, -1)) { \
      set_w21_error_message(soap, E_W21_ERROR_SINGLE_ATTRIBUTE, "Could not set BSON single " #objectName " attribute in " #objectParent); \
      goto onErrorGoto##_resume; \
  }

#define READ_W_PUT_SINGLE_ATTR_21_OR_ELSE_GOTO_RESUME(objectParent, objectName) \
  READ_PUT_SINGLE_ATTR_21_OR_ELSE_GOTO_RESUME(&root_document, objectParent, objectName, bson_read_##objectParent##21)

#define READ_O_PUT_SINGLE_ATTR_21_OR_ELSE_GOTO_RESUME(objectParent, objectName) \
  READ_PUT_SINGLE_ATTR_21_OR_ELSE_GOTO_RESUME(&child, objectParent, objectName, bson_read_##objectParent##_21)

#define READ_PUT_SINGLE_ATTR_ENUM_REQUIRED_21_OR_ELSE_GOTO_RESUME(bson, ns, objectParent, objectName, enumTypeSuffixFunction, onErrorGoto) \
  if (bson_put_single_attribute_if_exists( \
    bson, \
    CWS_CONST_BSON_KEY(#objectName), \
    (char *)soap_##ns##__##enumTypeSuffixFunction##2s(soap, objectParent->objectName), -1)) { \
    set_w21_error_message(soap, E_W21_ERROR_SINGLE_ATTRIBUTE_ENUM_REQUIRED, "Could not set BSON single enum attribute required " #objectName " attribute in " #objectParent); \
    goto onErrorGoto##_resume; \
  }

#define READ_O_PUT_SINGLE_ATTR_ENUM_REQUIRED_21_OR_ELSE_GOTO_RESUME(ns, objectParent, objectName, enumTypeSuffixFunction) \
  READ_PUT_SINGLE_ATTR_ENUM_REQUIRED_21_OR_ELSE_GOTO_RESUME(&child, ns, objectParent, objectName, enumTypeSuffixFunction, bson_read_##objectParent##_21)

#define READ_PUT_TWO_ATTR_ENUM_REQUIRED1_OPTIONAL2_21_OR_ELSE_GOTO_RESUME(bson, ns, objectParent, objectName1, enumTypeSuffixFunction1, objectName2, enumTypeSuffixFunction2, onErrorGoto) \
  if (objectParent->objectName2) { \
    if (bson_put_two_attributes_if_exist( \
      bson, \
      CWS_CONST_BSON_KEY(#objectName1), \
      (char *)soap_##ns##__##enumTypeSuffixFunction1##2s(soap, objectParent->objectName1), -1, \
      CWS_CONST_BSON_KEY(#objectName2), \
      (char *)soap_##ns##__##enumTypeSuffixFunction2##2s(soap, *(objectParent->objectName2)), -1)) { \
      set_w21_error_message(soap, E_W21_ERROR_TWO_ATTRIBUTES_ENUM_REQUIRED1_OPTIONAL2, "Could not set BSON two enum attribute 1 required and 1 optional " #objectName1 " | " #objectName2 " attributes in " #objectParent); \
      goto onErrorGoto##_resume; \
    } \
  } else if (bson_put_single_attribute_if_exists( \
    bson, \
    CWS_CONST_BSON_KEY(#objectName1), \
    (char *)soap_##ns##__##enumTypeSuffixFunction1##2s(soap, objectParent->objectName1), -1)) { \
    set_w21_error_message(soap, E_W21_ERROR_TWO_ATTRIBUTES_ENUM_REQUIRED1_OPTIONAL2, "Could not set BSON single enum attribute required " #objectName1 " attribute in " #objectParent); \
    goto onErrorGoto##_resume; \
  }

#define READ_O_PUT_TWO_ATTR_ENUM_REQUIRED1_OPTIONAL2_21_OR_ELSE_GOTO_RESUME(ns, objectParent, objectName1, enumTypeSuffixFunction1, objectName2, enumTypeSuffixFunction2) \
  READ_PUT_TWO_ATTR_ENUM_REQUIRED1_OPTIONAL2_21_OR_ELSE_GOTO_RESUME(&child, ns, objectParent, objectName1, enumTypeSuffixFunction1, objectName2, enumTypeSuffixFunction2, bson_read_##objectParent##_21)

//////////////////////////////// COST BUILDER ////////////////////////////////////
//COST builder. Currency required
#define BSON_READ_COST_BUILDER_21(ns, type) \
static \
int bson_read_##type##_21( \
  struct soap *soap, \
  bson_t *bson, \
  const char *key, int key_length, \
  struct ns##__##type *type \
) \
{ \
\
  if (!type) \
    W21_RETURN \
\
  bson_t child; \
\
  if (!bson_append_document_begin(bson, key, key_length, &child)) { \
    set_w21_error_message(soap, E_W21_ERROR_BEGIN_COST_OBJECT, "Could not begin BSON in object " #type " in key %s", key); \
    W21_RETURN \
  } \
\
  if (bson_put_single_attribute_required( \
    &child, \
    CWS_CONST_BSON_KEY("currency"), \
    type->currency, -1)) {\
      set_w21_error_message(soap, E_W21_ERROR_SET_COST_ATTRIBUTE, "Could not set attribute at BSON in object " #type " in key %s", key); \
      goto bson_read_##type##_21_resume; \
    } \
\
  if (!bson_append_double(&child, KEY_USCORE_VALUE, type->__item)) { \
    set_w21_error_message(soap, E_W21_ERROR_SET_COST_DOUBLE, "Could not set double value at BSON in object " #type " in key %s", key); \
    goto bson_read_##type##_21_resume; \
  } \
\
  CAPTURE_STAT_ON_SUCCESS(cost) \
\
  if (!bson_append_document_end(bson, &child)) \
    set_w21_error_message(soap, E_W21_ERROR_END_COST_OBJECT, "Could not end BSON in object " #type " in key %s", key); \
\
  W21_RETURN \
\
bson_read_##type##_21_resume: \
  bson_append_document_end(bson, &child); \
\
  W21_RETURN \
}

//Special case date time dTim required
#define BSON_READ_TIMESTAMP_COMMENT_STRING_BUILDER_21(ns, type) \
static \
int bson_read_##type##_21( \
  struct soap *soap, \
  bson_t *bson, \
  const char *key, int key_length, \
  struct ns##__##type *type \
) \
{ \
  bson_t child; \
\
  if (!type) \
    W21_RETURN \
\
  if (!bson_append_document_begin(bson, key, key_length, &child)) { \
    set_w21_error_message(soap, E_W21_ERROR_BEGIN_BSON_COMMENTED_TIMESTAMP, "Could begin BSON in commented timestamp object " #type " in key %s", key); \
    W21_RETURN \
  } \
\
  if (!bson_append_time_t(&child, CWS_CONST_BSON_KEY("dTim"), type->dTim)) { \
    set_w21_error_message(soap, E_W21_ERROR_ADD_DTIM_COMMENTED_TIMESTAMP, "Could set BSON dTim in commented timestamp object " #type " in key %s", key); \
    goto bson_read_##type##_21_resume; \
  } \
\
  if ((type->__item != NULL)&&(!bson_append_utf8(&child, KEY_USCORE_VALUE, type->__item, -1))) { \
    set_w21_error_message(soap, E_W21_ERROR_ADD_VALUE_COMMENTED_TIMESTAMP, "Could set comment BSON dTim in commented timestamp object " #type " in key %s", key); \
    goto bson_read_##type##_21_resume; \
  } \
\
  CAPTURE_STAT_ON_SUCCESS(date_time) \
\
  if (!bson_append_document_end(bson, &child)) \
    set_w21_error_message(soap, E_W21_ERROR_END_BSON_COMMENTED_TIMESTAMP, "Could end BSON in commented timestamp object " #type " in key %s", key); \
\
  W21_RETURN \
bson_read_##type##_21_resume: \
  bson_append_document_end(bson, &child); \
\
  W21_RETURN \
}

//Special case
//////////////// STRING __ITEM ///////
//Used for array function
#define READ_UTF8_OBJECT_ITEM_21_OR_ELSE_GOTO_RESUME(bson, objectParent, onErrorGoto) \
  if (objectParent->__item) { \
    if (!bson_append_utf8(bson, KEY_USCORE_VALUE, (const char *)objectParent->__item, -1)) { \
      set_w21_error_message(soap, E_W21_ERROR_SET_UTF8_ITEM, "Could not set __item value in " #objectParent); \
      goto onErrorGoto##_resume; \
    } \
    CAPTURE_STAT(string) \
  }

//aqui
#define READ_DOUBLE_OBJECT_ITEM_21_OR_ELSE_GOTO_RESUME(bson, objectParent, onErrorGoto) \
  if (!bson_append_double(bson, KEY_USCORE_VALUE, objectParent->__item)) { \
    set_w21_error_message(soap, E_W21_ERROR_SET_DOUBLE_ITEM, "Could not set double __item value " #objectParent); \
    goto onErrorGoto##_resume; \
  } \
  CAPTURE_STAT(double)

#define READ_O_DOUBLE_OBJECT_ITEM_21_OR_ELSE_GOTO_RESUME(objectParent) \
  READ_DOUBLE_OBJECT_ITEM_21_OR_ELSE_GOTO_RESUME(&child, objectParent, bson_read_##objectParent##_21)

#define READ_A_UTF8_OBJECT_ITEM_21_OR_ELSE_GOTO_RESUME(objectParent) \
  READ_UTF8_OBJECT_ITEM_21_OR_ELSE_GOTO_RESUME(&document_in_array, objectParent, bson_read_array_of_##objectParent##_21)

#define READ_O_UTF8_OBJECT_ITEM_21_OR_ELSE_GOTO_RESUME(objectParent) \
  READ_UTF8_OBJECT_ITEM_21_OR_ELSE_GOTO_RESUME(&child, objectParent, bson_read_##objectParent##_21)

// ESPECIAL CASE, SECOND PARAMETER IS ENUM
//Second attribute is required enum ns2
#define READ_PUT_TWO_ATTR_21_OR_ELSE_GOTO_RESUME_B(bson, objectParent, objectName1, ns, objectName2, enumTypeSuffixFunction, onErrorGoto) \
  if (bson_put_two_attributes_if_exist(bson, CWS_CONST_BSON_KEY(#objectName1), objectParent->objectName1, -1, CWS_CONST_BSON_KEY(#objectName2), \
    (char *)soap_##ns##__##enumTypeSuffixFunction##2s(soap, objectParent->objectName2), -1))  {\
    set_w21_error_message(soap, E_W21_ERROR_SET_ENUM_ATTR_REQUIRED, "(enum attr. required): Could not set attribute in " #objectParent); \
    goto onErrorGoto##_resume; \
  }

#define READ_A_PUT_TWO_ATTR_21_OR_ELSE_GOTO_RESUME_B(objectParent, objectName1, ns, objectName2, enumTypeSuffixFunction) \
  READ_PUT_TWO_ATTR_21_OR_ELSE_GOTO_RESUME_B(&document_in_array, objectParent, objectName1, ns, objectName2, enumTypeSuffixFunction, bson_read_array_of_##objectParent##_21)

#define READ_O_PUT_TWO_ATTR_21_OR_ELSE_GOTO_RESUME_B(objectParent, objectName1, ns, objectName2, enumTypeSuffixFunction) \
  READ_PUT_TWO_ATTR_21_OR_ELSE_GOTO_RESUME_B(&child, objectParent, objectName1, ns, objectName2, enumTypeSuffixFunction, bson_read_##objectParent##_21)

// ESPECIAL CASE, SECOND PARAMETER
#define READ_PUT_TWO_ATTR_21_OR_ELSE_GOTO_RESUME(bson, objectParent, objectName1, objectName2, onErrorGoto) \
  if (bson_put_two_attributes_if_exist(bson, \
    CWS_CONST_BSON_KEY(#objectName1), objectParent->objectName1, -1, \
    CWS_CONST_BSON_KEY(#objectName2), \
    objectParent->objectName2, -1))  {\
    set_w21_error_message(soap, E_W21_ERROR_SET_TWO_ATTR_REQUIRED, "Could not set 2 attributes in " #objectParent); \
    goto onErrorGoto##_resume; \
  }

#define READ_A_PUT_TWO_ATTR_21_OR_ELSE_GOTO_RESUME(objectParent, objectName1, objectName2) \
  READ_PUT_TWO_ATTR_21_OR_ELSE_GOTO_RESUME(&document_in_array, objectParent, objectName1, objectName2, bson_read_array_of_##objectParent##_21)

#define READ_O_PUT_TWO_ATTR_21_OR_ELSE_GOTO_RESUME(objectParent, objectName1, objectName2) \
  READ_PUT_TWO_ATTR_21_OR_ELSE_GOTO_RESUME(&child, objectParent, objectName1, objectName2, bson_read_##objectParent##_21)

#define READ_W_PUT_TWO_ATTR_21_OR_ELSE_GOTO_RESUME(objectParent, objectName1, objectName2) \
  READ_PUT_TWO_ATTR_21_OR_ELSE_GOTO_RESUME(&root_document, objectParent, objectName1, objectName2, bson_read_##objectParent##21)


#define READ_ARRAY_OF_OBJECT_ENUM_21_OR_ELSE_GOTO_RESUME(ns, bson, objectParent, objectName, enumFunctionName, onErrorGoto) \
  if (bson_read_arrayOfEnum21_util(soap, bson, CWS_CONST_BSON_KEY(#objectName), \
    objectParent->__size##objectName, (int *)objectParent->objectName, (enum_caller_fn)soap_##ns##__##enumFunctionName##2s)) goto onErrorGoto##_resume;


#define READ_W_ARRAY_OF_OBJECT_ENUM_21_OR_ELSE_GOTO_RESUME(ns, objectParent, objectName, enumFunctionName) \
  READ_ARRAY_OF_OBJECT_ENUM_21_OR_ELSE_GOTO_RESUME(ns, &root_document, objectParent, objectName, enumFunctionName, bson_read_##objectParent##21)

#define READ_A_ARRAY_OF_OBJECT_ENUM_21_OR_ELSE_GOTO_RESUME(ns, objectParent, objectName, enumFunctionName) \
  READ_ARRAY_OF_OBJECT_ENUM_21_OR_ELSE_GOTO_RESUME(ns, &document_in_array, objectParent, objectName, enumFunctionName, bson_read_array_of_##objectParent##_21)

#endif
