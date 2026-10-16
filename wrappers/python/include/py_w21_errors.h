#ifndef PY_W21_ERRORS_H
 #define PY_W21_ERRORS_H

#include <w21_errors.h>

#define PY_E_UNABLE_TO_PARSE_PARAMETERS 3000
#define PY_E_UNABLE_TO_INITIALIZE_CWS_21_INSTANCE 3001

W21_ERROR_DETAIL *py_w21_error_detail(int);

#endif