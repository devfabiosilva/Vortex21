#define PY_SSIZE_T_CLEAN
#include <Python.h>
#include <modules_macros.h>
#include <w21_config.h>
#include <py_w21_errors.h>

typedef struct {
  PyObject_HEAD
  struct soap *soap;
  //int err;
} Py_W21_CONFIG;

static PyObject *c_obj_new(PyTypeObject *type, PyObject *args, PyObject *kwds)
{
  PyObject *self;

  if (!(self=type->tp_alloc(type, 0)))
    Py_W21_ERROR("Could not alloc Py_WITSML2.1 object", NULL)

  //PyW21_ERR = 0;
  PyGET_CWS_SOAP = NULL;

  Py_W21_DEBUG(stdout, "New object self created at %p\n", self)

  return (PyObject *)self;
}

static int c_obj_init(PyTypeObject *self, PyObject *args, PyObject *kwds)
{
  char *kwlist[] = {"input rule", "output rule", "regex validator enable", NULL};

  uint64_t input_rule = 0, output_rule = 0, bool regex_validator = true;

  if (!PyArg_ParseTupleAndKeywords(
    args, kwds, "|ip", kwlist,
    &input_rule, &output_rule, &regex_validator
  )) Py_W21_ERROR("Error on parse WITSML2.1 BSON parser", PY_E_UNABLE_TO_PARSE_PARAMETERS)

  Py_W21_DEBUG(stdout, "Initializing config %p\n", PyGET_CWS_SOAP)

  int err =  w21_config_new(&PyGET_CWS_SOAP, input_rule, output_rule)

  if (err)
    Py_W21_ERROR("Could not initialize WITSML2.1 config (w21_config_new)", PY_E_UNABLE_TO_INITIALIZE_CWS_21_INSTANCE)

  return 0;
}

static void c_obj_dealloc(PyTypeObject *self)
{
  Py_W21_DEBUG(stdout, "Destroying config %p\n", PyGET_CWS_SOAP)
  w21_config_free(&PyGET_CWS_SOAP);
  Py_W21_DEBUG(stdout, "Destroyed config %p\n", PyGET_CWS_SOAP)
}
