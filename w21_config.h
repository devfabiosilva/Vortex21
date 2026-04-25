#ifndef W21_CONFIG_H
 #define W21_CONFIG_H

#include <stdint.h>
#include <stddef.h>
#include <stdsoap2.h>
#include <stdbool.h>
#include <core/include/cws_bson_utils.h>
#include <core/include/cws_utils.h>
#include <w21_validator.h>
#include <witsml21Stub.h>

#define SET_W21_ENUM_OBJ(obj) W21_OBJECT_##obj = SOAP_TYPE_rdw211__##obj

enum w21_object_e {
  W21_OBJECT_NONE = 0,
  W21_OBJECT_AutoDetect = 1,
  SET_W21_ENUM_OBJ(BhaRun),
  SET_W21_ENUM_OBJ(CementJob),
  SET_W21_ENUM_OBJ(CementJobEvaluation),
  SET_W21_ENUM_OBJ(Channel),
  SET_W21_ENUM_OBJ(ChannelKind),
  SET_W21_ENUM_OBJ(ChannelKindDictionary),
  SET_W21_ENUM_OBJ(ChannelSet),
  SET_W21_ENUM_OBJ(CuttingsGeology),
  SET_W21_ENUM_OBJ(CuttingsGeologyInterval),
  SET_W21_ENUM_OBJ(DepthRegImage),
  SET_W21_ENUM_OBJ(DownholeComponent),
  SET_W21_ENUM_OBJ(DrillReport),
  SET_W21_ENUM_OBJ(ErrorTerm),
  SET_W21_ENUM_OBJ(ErrorTermDictionary),
  SET_W21_ENUM_OBJ(FluidsReport),
  SET_W21_ENUM_OBJ(InterpretedGeology),
  SET_W21_ENUM_OBJ(InterpretedGeologyInterval),
  SET_W21_ENUM_OBJ(Log),
  SET_W21_ENUM_OBJ(LoggingToolKind),
  SET_W21_ENUM_OBJ(LoggingToolKindDictionary),
  SET_W21_ENUM_OBJ(MudLogReport),
  SET_W21_ENUM_OBJ(MudlogReportInterval),
  SET_W21_ENUM_OBJ(OpsReport),
  SET_W21_ENUM_OBJ(PPFGChannel),
  SET_W21_ENUM_OBJ(PPFGChannelSet),
  SET_W21_ENUM_OBJ(PPFGLog),
  SET_W21_ENUM_OBJ(Rig),
  SET_W21_ENUM_OBJ(RigUtilization),
  SET_W21_ENUM_OBJ(Risk),
  SET_W21_ENUM_OBJ(ShowEvaluation),
  SET_W21_ENUM_OBJ(ShowEvaluationInterval),
  SET_W21_ENUM_OBJ(StimJob),
  SET_W21_ENUM_OBJ(StimJobStage),
  SET_W21_ENUM_OBJ(StimPerforationCluster),
  SET_W21_ENUM_OBJ(SurveyProgram),
  SET_W21_ENUM_OBJ(Target),
  SET_W21_ENUM_OBJ(ToolErrorModel),
  SET_W21_ENUM_OBJ(ToolErrorModelDictionary),
  SET_W21_ENUM_OBJ(Trajectory),
  SET_W21_ENUM_OBJ(TrajectoryStation),
  SET_W21_ENUM_OBJ(Tubular),
  SET_W21_ENUM_OBJ(WeightingFunction),
  SET_W21_ENUM_OBJ(WeightingFunctionDictionary),
  SET_W21_ENUM_OBJ(Well),
  SET_W21_ENUM_OBJ(Wellbore),
  SET_W21_ENUM_OBJ(WellboreCompletion),
  SET_W21_ENUM_OBJ(WellboreGeology),
  SET_W21_ENUM_OBJ(WellboreGeometry),
  SET_W21_ENUM_OBJ(WellboreGeometrySection),
  SET_W21_ENUM_OBJ(WellboreMarker),
  SET_W21_ENUM_OBJ(WellboreMarkerSet),
  SET_W21_ENUM_OBJ(WellCMLedger),
  SET_W21_ENUM_OBJ(WellCompletion)
};

#undef SET_W21_ENUM_OBJ

enum w21_contract_e {
  W21_CONTRACT_NONE = 0,
  W21_CONTRACT_AutoDetect,
  W21_CONTRACT_BhaRun,
  W21_CONTRACT_CementJob,
  W21_CONTRACT_CementJobEvaluation,
  W21_CONTRACT_Channel,
  W21_CONTRACT_ChannelKind,
  W21_CONTRACT_ChannelKindDictionary,
  W21_CONTRACT_ChannelSet,
  W21_CONTRACT_CuttingsGeology,
  W21_CONTRACT_CuttingsGeologyInterval,
  W21_CONTRACT_DepthRegImage,
  W21_CONTRACT_DownholeComponent,
  W21_CONTRACT_DrillReport,
  W21_CONTRACT_ErrorTerm,
  W21_CONTRACT_ErrorTermDictionary,
  W21_CONTRACT_FluidsReport,
  W21_CONTRACT_InterpretedGeology,
  W21_CONTRACT_InterpretedGeologyInterval,
  W21_CONTRACT_Log,
  W21_CONTRACT_LoggingToolKind,
  W21_CONTRACT_LoggingToolKindDictionary,
  W21_CONTRACT_MudLogReport,
  W21_CONTRACT_MudlogReportInterval,
  W21_CONTRACT_OpsReport,
  W21_CONTRACT_PPFGChannel,
  W21_CONTRACT_PPFGChannelSet,
  W21_CONTRACT_PPFGLog,
  W21_CONTRACT_Rig,
  W21_CONTRACT_RigUtilization,
  W21_CONTRACT_Risk,
  W21_CONTRACT_ShowEvaluation,
  W21_CONTRACT_ShowEvaluationInterval,
  W21_CONTRACT_StimJob,
  W21_CONTRACT_StimJobStage,
  W21_CONTRACT_StimPerforationCluster,
  W21_CONTRACT_SurveyProgram,
  W21_CONTRACT_Target,
  W21_CONTRACT_ToolErrorModel,
  W21_CONTRACT_ToolErrorModelDictionary,
  W21_CONTRACT_Trajectory,
  W21_CONTRACT_TrajectoryStation,
  W21_CONTRACT_Tubular,
  W21_CONTRACT_WeightingFunction,
  W21_CONTRACT_WeightingFunctionDictionary,
  W21_CONTRACT_Well,
  W21_CONTRACT_Wellbore,
  W21_CONTRACT_WellboreCompletion,
  W21_CONTRACT_WellboreGeology,
  W21_CONTRACT_WellboreGeometry,
  W21_CONTRACT_WellboreGeometrySection,
  W21_CONTRACT_WellboreMarker,
  W21_CONTRACT_WellboreMarkerSet,
  W21_CONTRACT_WellCMLedger,
  W21_CONTRACT_WellCompletion
};

_Static_assert(W21_CONTRACT_NONE == 0, "W21_CONTRACT_NONE must be zero");

typedef struct witsml21_contract_t {
  enum w21_contract_e contract_type; // Contract type
  const char *object_name; // Name of object
  const char *contract_begin; // Contract begin
  size_t contract_begin_len; // Contract begin string length
  size_t contract_begin_copied; // Bytes copied from Contract begin to gSoap buffer
  const char *in_wistml21_xml; // Pointer buffer input (WITSML 2.1 xml object)
  size_t in_wistml21_xml_len; // Length of input (WITSML 2.1 xml object) length
  size_t in_wistml21_xml_copied; // Total copied in_wistml21_xml into gSoap buffer
  const char *contract_end; // Contract end
  size_t contract_end_len; // Contract end string length
  size_t contract_end_copied; // Total copied contract end string
} WISML21_CONTRACT;

#define DEFAULT_DETAIL_MESSAGE_LEN 312 // Default message on error
#define DEFAULT_DETAIL_MESSAGE_XML_LEN (DEFAULT_DETAIL_MESSAGE_LEN + 96) // Formatted default message on error in XML schema
#define W21_MESSAGE_SANITIZE_SIZE (DEFAULT_DETAIL_MESSAGE_LEN / 2)

typedef int (*f_in_root_element_cb)(struct soap *, int);

// BEGIN IN CONFIG
#define ENABLE_REGEX_VALIDATOR 0x8000000000000000

// END IN CONFIG

#ifdef WITH_STATISTICS

int w21_hard_summary_read_begin(struct soap *);
int w21_hard_summary_read_end(struct soap *);
int w21_hard_summary_parse_begin(struct soap *);
int w21_hard_summary_parse_end(struct soap *);
int w21_hard_summary_parse_json_begin(struct soap *);
int w21_hard_summary_parse_json_end(struct soap *);

struct hard_stat_t {
    // WITSML 2.1 XML deserialization
    int in_err;
    uint64_t in_start_cycles;
    uint64_t in_total_cycles;
    struct timespec in_start_time;
    uint64_t in_total_nanos;
    size_t in_mem_start;
    ssize_t in_mem_delta;
    // BSON PARSE
    int in_parse_err;
    uint64_t in_parse_start_cycles;
    uint64_t in_parse_total_cycles;
    struct timespec in_parse_start_time;
    uint64_t in_parse_total_nanos;
    size_t in_parse_mem_start;
    ssize_t in_parse_mem_delta;
    // JSON PARSE
    int in_parse_json_err;
    uint64_t in_parse_json_start_cycles;
    uint64_t in_parse_json_total_cycles;
    struct timespec in_parse_json_start_time;
    uint64_t in_parse_json_total_nanos;
    size_t in_parse_json_mem_start;
    ssize_t in_parse_json_mem_delta;
};

#endif

typedef struct w21_config_t {
  int error; // Global error (IN/OUT) 0 if success
  void *usr; // User pointer
  FILE *file; // in/out (file for input/output)
  const char *in_filename; // IN: Path/Path and name of input file
  bool in_file_stream_done; // IN: File stream done
  void *in_object; // In object pointer;
  f_in_root_element_cb finrootelement; // Experimental, callback to root element. Can be NULL
//  FILE *file_test; // TODO Remove. Only for test
  enum w21_object_e object_type; // Object type
  enum w21_object_e object_subtype; // Used only in AutoDetect mode type will be resolved on the fly
  bson_t *in_bson_object_alloc; // INPUT: Object parsed in BSON (WITSML 2.1 to BSON). Must be free with cws_bson_free
  bson_t *in_bson; // INPUT: Object parsed in BSON (WITSML 2.1 to BSON). Must be set to NULL on recycle
  struct c_bson_serialized_t in_bson_serialized; // INPUT: // Bson serialized (can be flushed to file). Must be free by bson library
  struct c_json_str_t in_json_str; // INPUT: C JSON parsed to string. *json can be NULL. SHOULD be free
  uint64_t in_config; //INPUT: (Witsml 2.1 config << 32) | (gSoap config)
  struct w21_validation_t *current_validation; // NULLABLE. If ENABLE_REGEX_VALIDATOR, on w21_validate will call validation. If valid, PASS else REGEX ERROR 
  struct w21_validation_t *validators; // NULLABLE. Not null if ENABLE_REGEX_VALIDATOR. Array of initialized validators. Must be free.
  WISML21_CONTRACT witsml21_contract; // INPUT: Input contract
#ifdef WITH_STATISTICS
  struct statistics_t statistics; // INPUT: Statistics of parsed document
  struct hard_stat_t hardware_statistics; // INPUT/OUTPUT hardware statistics
#endif
  uint64_t out_config; // OUTPUT: (Witsml 2.1 config << 32) | (gSoap config)
  char detail_message[DEFAULT_DETAIL_MESSAGE_LEN + 1]; // Last byte is +1 is NULL terminated
  size_t detail_message_len; // Length of detail_message
  char detail_message_xml[DEFAULT_DETAIL_MESSAGE_XML_LEN + 1]; // Last byte is +1 is NULL terminated
  size_t detail_message_xml_len; //Length of detail_message_xml
} W21_CONFIG;

int w21_config_new(struct soap **, uint64_t, uint64_t);
void w21_config_free(struct soap **);
void w21_recycle(struct soap *);
/*
int in_witsml21_BhaRun(struct soap *, const char *, size_t);
#define in_witsml21_BhaRunA(soap, xml) parseWitsml21_BhaRun(soap, xml, strlen(xml))
int in_witsml21_BhaRun_from_file(struct soap *, const char *);
*/

struct c_json_str_t *w21_get_json(struct soap *);
struct c_bson_serialized_t *w21_bson_serialize(struct soap *);
struct statistics_t *w21_get_statistics(struct soap *);

const char *w21_get_object_name(enum w21_object_e);
const char *w21_get_input_object_name(struct soap *);

#define DECLARE_W21_CONFIG W21_CONFIG *config = ((W21_CONFIG *)soap->user);

#define W21_ROOT_ELEMENT_CB(typ) \
DECLARE_W21_CONFIG \
if ((config->finrootelement != NULL) && (config->finrootelement(soap, SOAP_TYPE_rdw211__##typ) != 0)) return NULL;

#define W21_ABSTRACT_OBJECT_CHECK(type, tag) \
DECLARE_W21_CONFIG \
if ((config->in_config & SOAP_XML_STRICT) == 0) { \
    set_w21_error_message(soap, E_W21_ERROR_POLYMORPHISM_STRICT_MODE_REQUIRED, "%s", w21_message_sanitize_fmt(soap, "WITSML 2.1 polymorphism requires STRICT mode enabled in Vortex 21 at " #type " %s", tag)); \
    soap->error = SOAP_TAG_MISMATCH; \
    return NULL; \
}

#define W21_RETURN return ((W21_CONFIG *)soap->user)->error;
#define W21_NO_PREVIOUS_ERROR ((((W21_CONFIG *)soap->user)->error) == 0)
#define W21_HAS_PREVIOUS_ERROR ((((W21_CONFIG *)soap->user)->error) != 0)

#define W21_OBJ_TYPE_STR w21_get_object_name((config->object_type != W21_OBJECT_AutoDetect)?config->object_type:config->object_subtype)

#endif
