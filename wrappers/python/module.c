#define PY_SSIZE_T_CLEAN
#include <Python.h>

typedef struct {
  PyObject_HEAD
  struct soap *soap;
  int err;
} Py_W21_CONFIG;

#ifdef PY_SOAP_DEBUG
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

static PyObject *c_obj_new(PyTypeObject *type, PyObject *args, PyObject *kwds)
{
  PyObject *self;

  if (!(self=type->tp_alloc(type, 0)))
    Py_W21_ERROR("Could not alloc Py_WITSML2.1 object", NULL)

  ERR=0;
  PyGET_CWS_CONFIG(self)=NULL;

  Py_W21_ERROR(stdout, "New object self created at %p\n", self)

  return (PyObject *)self;
}

static int c_obj_init(PyTypeObject *self, PyObject *args, PyObject *kwds)
{
  char
   *kwlist[] = {"input rule", "output rule", "regex validator enable", NULL},
   msg[128];

  const char *act;

  if (!PyArg_ParseTupleAndKeywords(
    args, kwds, "|ip", kwlist,
    &rule, &versionCheckDisable
  )) Py_W21_ERROR("Error on parse WITSML2.1 BSON parser", -498)

  if (!(act=py_cws_check_action(rule)))
    Py_W21_ERROR("Invalid action rule", -499)
  else if ((void *)act!=(void *)CONST_TYPE[0].name) {
    snprintf(msg, sizeof(msg), "Rule %s not implemented yet", act);
    Py_W21_ERROR(msg, -500)
  }

  Py_W21_DEBUG(stdout, "Initializing config %s\n", py_cws_check_action(rule))

  if (versionCheckDisable)
    versionCheckDisable=CWS_FLAG_CHECK_VERSION_DISABLE;

  if (!(PyGET_CWS_CONFIG(self)=cws_config_new("WITSML2.1 BSON parser", NULL, CWS_FLAG_RECYCLABLE|versionCheckDisable, rule)))
    Py_W21_ERROR("Could not initialize WITSML2.1 config", -501)

  Py_W21_DEBUG(stdout, "Initializing config %p\n", PyGET_CWS_CONFIG(self))

  return 0;
}

static void c_obj_dealloc(PyTypeObject *self)
{
  Py_W21_DEBUG(stdout, "Destroying config %p\n", PyGET_CWS_CONFIG(self))
  cws_config_free(&PyGET_CWS_CONFIG(self));
  Py_W21_DEBUG(stdout, "Destroyed config %p\n", PyGET_CWS_CONFIG(self))
}

