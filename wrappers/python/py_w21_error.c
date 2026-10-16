#include <py_w21_errors.h>
#include <stddef.h>

static W21_ERROR_DETAIL py_w21_error_detail_list[] =
{
    SET_W21_ERROR_DETAIL(PY_E_UNABLE_TO_PARSE_PARAMETERS, "In/Out: Could not initialize instance due to wrong input parameters")
    SET_W21_ERROR_DETAIL(PY_E_UNABLE_TO_INITIALIZE_CWS_21_INSTANCE, "In/Out: Unable to initialize WITSML 2.1 instance")
    {0, NULL, NULL}
};

static W21_ERROR_DETAIL _py_w21_unknown_error = {
    .err = 0,
    .tag = "<NOT FOUND TAG>",
    .detail = "Unknown error"
};

W21_ERROR_DETAIL *py_w21_error_detail(int error)
{
    W21_ERROR_DETAIL *res = w21_get_error_detail(error);

    if (res)
        return res;

    res = &py_w21_error_detail_list[0];

    do {
        if (res->err == error)
            return res;
    } while ((++res)->tag);

    return &_py_w21_unknown_error;
}
