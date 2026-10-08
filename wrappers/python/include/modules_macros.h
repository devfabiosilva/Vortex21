#ifndef MODULES_MACROS_H
 #define MODULES_MACROS_H

#ifdef PY_DEBUG_W21
 #define Py_W21_DEBUG(std, ...) \
    fprintf(std, __VA_ARGS__);
#else
 #define Py_W21_DEBUG(std, ...)
#endif

#define Py_W21_ERROR(err_msg, errNumber) \
 {\
   PyErr_SetString(PyExc_Exception, err_msg);\
    return errNumber;\
 }

#endif

