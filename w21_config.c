#include <w21_config.h>
#include <w21_errors.h>
#include <w21_messages.h>
#include <time.h>
#include <malloc.h>

_Static_assert(W21_OBJECT_NONE == 0, "W21_OBJECT_NONE must be zero");
_Static_assert(W21_OBJECT_AutoDetect == 1, "W21_OBJECT_AutoDetect must be 1");
#define W21_OBJ_ASSERT(obj) \
  _Static_assert(W21_OBJECT_##obj > W21_OBJECT_AutoDetect, "W21_OBJECT_" #obj " must be greater than W21_OBJECT_AutoDetect");

W21_OBJ_ASSERT(BhaRun)
W21_OBJ_ASSERT(CementJob)
W21_OBJ_ASSERT(CementJobEvaluation)
W21_OBJ_ASSERT(Channel)
W21_OBJ_ASSERT(ChannelKind)
W21_OBJ_ASSERT(ChannelKindDictionary)
W21_OBJ_ASSERT(ChannelSet)
W21_OBJ_ASSERT(CuttingsGeology)
W21_OBJ_ASSERT(CuttingsGeologyInterval)
W21_OBJ_ASSERT(DepthRegImage)
W21_OBJ_ASSERT(DownholeComponent)
W21_OBJ_ASSERT(DrillReport)
W21_OBJ_ASSERT(ErrorTerm)
W21_OBJ_ASSERT(ErrorTermDictionary)
W21_OBJ_ASSERT(FluidsReport)
W21_OBJ_ASSERT(InterpretedGeology)
W21_OBJ_ASSERT(InterpretedGeologyInterval)
W21_OBJ_ASSERT(Log)
W21_OBJ_ASSERT(LoggingToolKind)
W21_OBJ_ASSERT(LoggingToolKindDictionary)
W21_OBJ_ASSERT(MudLogReport)
W21_OBJ_ASSERT(MudlogReportInterval)
W21_OBJ_ASSERT(OpsReport)
W21_OBJ_ASSERT(PPFGChannel)
W21_OBJ_ASSERT(PPFGChannelSet)
W21_OBJ_ASSERT(PPFGLog)
W21_OBJ_ASSERT(Rig)
W21_OBJ_ASSERT(RigUtilization)
W21_OBJ_ASSERT(Risk)
W21_OBJ_ASSERT(ShowEvaluation)
W21_OBJ_ASSERT(ShowEvaluationInterval)
W21_OBJ_ASSERT(StimJob)
W21_OBJ_ASSERT(StimJobStage)
W21_OBJ_ASSERT(StimPerforationCluster)
W21_OBJ_ASSERT(SurveyProgram)
W21_OBJ_ASSERT(Target)
W21_OBJ_ASSERT(ToolErrorModel)
W21_OBJ_ASSERT(ToolErrorModelDictionary)
W21_OBJ_ASSERT(Trajectory)
W21_OBJ_ASSERT(TrajectoryStation)
W21_OBJ_ASSERT(Tubular)
W21_OBJ_ASSERT(WeightingFunction)
W21_OBJ_ASSERT(WeightingFunctionDictionary)
W21_OBJ_ASSERT(Well)
W21_OBJ_ASSERT(Wellbore)
W21_OBJ_ASSERT(WellboreCompletion)
W21_OBJ_ASSERT(WellboreGeology)
W21_OBJ_ASSERT(WellboreGeometry)
W21_OBJ_ASSERT(WellboreGeometrySection)
W21_OBJ_ASSERT(WellboreMarker)
W21_OBJ_ASSERT(WellboreMarkerSet)
W21_OBJ_ASSERT(WellCMLedger)
W21_OBJ_ASSERT(WellCompletion)

#undef W21_OBJ_ASSERT

#define CONTRACT_BEGIN_EVAL(x) "<?xml version=\"1.0\" encoding=\"UTF-8\"?><"#x"Contract xmlns:cw21rd=\"http://tempuri.org/cw21rd.xsd\">"
#define CONTRACT_END_EVAL(x) "</"#x"Contract>"

#define SET_CONTRACT(contract_name) \
const WISML21_CONTRACT witsml21_contract_##contract_name = { \
    W21_CONTRACT_##contract_name, \
    #contract_name, \
    CONTRACT_BEGIN_EVAL(contract_name), sizeof(CONTRACT_BEGIN_EVAL(contract_name)) - 1, 0, \
    NULL, 0, 0, \
    CONTRACT_END_EVAL(contract_name), sizeof(CONTRACT_END_EVAL(contract_name)) - 1, 0 \
};

SET_CONTRACT(AutoDetect)
SET_CONTRACT(BhaRun)
SET_CONTRACT(CementJob)
SET_CONTRACT(CementJobEvaluation)
SET_CONTRACT(Channel)
SET_CONTRACT(ChannelKind)
SET_CONTRACT(ChannelKindDictionary)
SET_CONTRACT(ChannelSet)
SET_CONTRACT(CuttingsGeology)
SET_CONTRACT(CuttingsGeologyInterval)
SET_CONTRACT(DepthRegImage)
SET_CONTRACT(DownholeComponent)
SET_CONTRACT(DrillReport)
SET_CONTRACT(ErrorTerm)
SET_CONTRACT(ErrorTermDictionary)
SET_CONTRACT(FluidsReport)
SET_CONTRACT(InterpretedGeology)
SET_CONTRACT(InterpretedGeologyInterval)
SET_CONTRACT(Log)
SET_CONTRACT(LoggingToolKind)
SET_CONTRACT(LoggingToolKindDictionary)
SET_CONTRACT(MudLogReport)
SET_CONTRACT(MudlogReportInterval)
SET_CONTRACT(OpsReport)
SET_CONTRACT(PPFGChannel)
SET_CONTRACT(PPFGChannelSet)
SET_CONTRACT(PPFGLog)
SET_CONTRACT(Rig)
SET_CONTRACT(RigUtilization)
SET_CONTRACT(Risk)
SET_CONTRACT(ShowEvaluation)
SET_CONTRACT(ShowEvaluationInterval)
SET_CONTRACT(StimJob)
SET_CONTRACT(StimJobStage)
SET_CONTRACT(StimPerforationCluster)
SET_CONTRACT(SurveyProgram)
SET_CONTRACT(Target)
SET_CONTRACT(ToolErrorModel)
SET_CONTRACT(ToolErrorModelDictionary)
SET_CONTRACT(Trajectory)
SET_CONTRACT(TrajectoryStation)
SET_CONTRACT(Tubular)
SET_CONTRACT(WeightingFunction)
SET_CONTRACT(WeightingFunctionDictionary)
SET_CONTRACT(Well)
SET_CONTRACT(Wellbore)
SET_CONTRACT(WellboreCompletion)
SET_CONTRACT(WellboreGeology)
SET_CONTRACT(WellboreGeometry)
SET_CONTRACT(WellboreGeometrySection)
SET_CONTRACT(WellboreMarker)
SET_CONTRACT(WellboreMarkerSet)
SET_CONTRACT(WellCMLedger)
SET_CONTRACT(WellCompletion)

#undef SET_CONTRACT
#undef CONTRACT_END_EVAL
#undef CONTRACT_BEGIN_EVAL

#define SET_OBJECT_NAME(obj) { W21_OBJECT_##obj, #obj}

struct _w21_object_names_t {
  enum w21_object_e type;
  const char *obj_name;
};

static struct _w21_object_names_t _W21_OBJECT_NAMES[] = {
  SET_OBJECT_NAME(NONE),
  SET_OBJECT_NAME(AutoDetect),
  SET_OBJECT_NAME(BhaRun),
  SET_OBJECT_NAME(CementJob),
  SET_OBJECT_NAME(CementJobEvaluation),
  SET_OBJECT_NAME(Channel),
  SET_OBJECT_NAME(ChannelKind),
  SET_OBJECT_NAME(ChannelKindDictionary),
  SET_OBJECT_NAME(ChannelSet),
  SET_OBJECT_NAME(CuttingsGeology),
  SET_OBJECT_NAME(CuttingsGeologyInterval),
  SET_OBJECT_NAME(DepthRegImage),
  SET_OBJECT_NAME(DownholeComponent),
  SET_OBJECT_NAME(DrillReport),
  SET_OBJECT_NAME(ErrorTerm),
  SET_OBJECT_NAME(ErrorTermDictionary),
  SET_OBJECT_NAME(FluidsReport),
  SET_OBJECT_NAME(InterpretedGeology),
  SET_OBJECT_NAME(InterpretedGeologyInterval),
  SET_OBJECT_NAME(Log),
  SET_OBJECT_NAME(LoggingToolKind),
  SET_OBJECT_NAME(LoggingToolKindDictionary),
  SET_OBJECT_NAME(MudLogReport),
  SET_OBJECT_NAME(MudlogReportInterval),
  SET_OBJECT_NAME(OpsReport),
  SET_OBJECT_NAME(PPFGChannel),
  SET_OBJECT_NAME(PPFGChannelSet),
  SET_OBJECT_NAME(PPFGLog),
  SET_OBJECT_NAME(Rig),
  SET_OBJECT_NAME(RigUtilization),
  SET_OBJECT_NAME(Risk),
  SET_OBJECT_NAME(ShowEvaluation),
  SET_OBJECT_NAME(ShowEvaluationInterval),
  SET_OBJECT_NAME(StimJob),
  SET_OBJECT_NAME(StimJobStage),
  SET_OBJECT_NAME(StimPerforationCluster),
  SET_OBJECT_NAME(SurveyProgram),
  SET_OBJECT_NAME(Target),
  SET_OBJECT_NAME(ToolErrorModel),
  SET_OBJECT_NAME(ToolErrorModelDictionary),
  SET_OBJECT_NAME(Trajectory),
  SET_OBJECT_NAME(TrajectoryStation),
  SET_OBJECT_NAME(Tubular),
  SET_OBJECT_NAME(WeightingFunction),
  SET_OBJECT_NAME(WeightingFunctionDictionary),
  SET_OBJECT_NAME(Well),
  SET_OBJECT_NAME(Wellbore),
  SET_OBJECT_NAME(WellboreCompletion),
  SET_OBJECT_NAME(WellboreGeology),
  SET_OBJECT_NAME(WellboreGeometry),
  SET_OBJECT_NAME(WellboreGeometrySection),
  SET_OBJECT_NAME(WellboreMarker),
  SET_OBJECT_NAME(WellboreMarkerSet),
  SET_OBJECT_NAME(WellCMLedger),
  SET_OBJECT_NAME(WellCompletion),
  {0, NULL}
};
#undef SET_OBJECT_NAME

const char *w21_get_object_name(enum w21_object_e type)
{
  struct _w21_object_names_t *_w21_object_names_ptr = &_W21_OBJECT_NAMES[0];

  while ((type != _w21_object_names_ptr->type) && (_w21_object_names_ptr->obj_name != NULL))
    ++_w21_object_names_ptr;

  if (_w21_object_names_ptr->obj_name)
    return _w21_object_names_ptr->obj_name;

  return "Object unknown";
}

const char *w21_get_input_object_name(struct soap *soap)
{
    DECLARE_W21_CONFIG
    if (config->error == 0) { 
        if (config->object_type != W21_OBJECT_NONE) {
            if (config->object_type != W21_OBJECT_AutoDetect)
                return w21_get_object_name(config->object_type);
            
            return w21_get_object_name(config->object_subtype);
        }
    }
    return NULL;
}

int w21_config_new(struct soap **soap, uint64_t in_config, uint64_t out_config)
{
    *soap = NULL;
    W21_CONFIG *config = (W21_CONFIG *)malloc(sizeof(W21_CONFIG));

    if (!config)
        return E_W21_UNABLE_TO_CREATE_CONFIG;

    memset((void *)config, 0, sizeof(W21_CONFIG));

    config->in_config = in_config;
    config->out_config = out_config;

    *soap = soap_new2((int)in_config, (int)out_config);

    if (!(*soap)) {
        free((void *)config);
        return E_W21_UNABLE_TO_ALLOC_GSOAP_INSTANCE;
    }

    //novo
    if ((config->in_bson_object_alloc = CWS_BSON_NEW) == NULL) {
        soap_free(*soap);
        free((void *)config);
        return E_W21_ERROR_CREATE_IN_BSON_OBJECT;
    }

    (*soap)->is = NULL;
    (*soap)->os = NULL;

    (*soap)->frecv = NULL;
    (*soap)->fsvalidate = NULL;
    (*soap)->fsend = NULL;
/*
    (*soap)->sendfd = 0;
    (*soap)->recvfd = 0;
*/
    (*soap)->user = (void *)config;

    return 0;
}

#define W21_COMMON_IN_FREE \
if (config->in_json_str.json) { \
    bson_free(config->in_json_str.json); \
    config->in_json_str.json = NULL; \
    config->in_json_str.json_len = 0; \
} \
\
if (config->in_bson_serialized.bson) { \
    bson_free((void *)config->in_bson_serialized.bson); \
    config->in_bson_serialized.bson = NULL; \
    config->in_bson_serialized.bson_size = 0; \
} \
\
cws_bson_free(&config->in_bson_object_alloc); \
config->in_bson = NULL; \
\
if (config->in_object) { \
    free(config->in_object); \
    config->in_object = NULL; \
}

void w21_config_free(struct soap **soap)
{
    if (*soap) {
        W21_CONFIG *config = (W21_CONFIG *)(*soap)->user;

        W21_COMMON_IN_FREE

        soap_destroy(*soap);
        soap_end(*soap);

        w21_input_rules_validator_destroy(*soap);

        soap_free(*soap);

        free((void *)config);
        *soap = NULL;
    }
}

void w21_recycle(struct soap *soap)
{
    DECLARE_W21_CONFIG

    // Reset all BSON and free JSON  and IN CONFIG
    if (config->in_json_str.json) {
        bson_free(config->in_json_str.json);
        config->in_json_str.json = NULL;
        config->in_json_str.json_len = 0;
    }

    if (config->in_bson_serialized.bson) {
        bson_free((void *)config->in_bson_serialized.bson);
        config->in_bson_serialized.bson = NULL;
        config->in_bson_serialized.bson_size = 0; 
    }

    // Reinitialize BSON
    if (config->in_bson_object_alloc)
        bson_reinit(config->in_bson_object_alloc);

    config->in_bson = NULL;

    if (config->in_object) {
        free(config->in_object);
        config->in_object = NULL;
    }

    // Free current passed object and messages
    soap_destroy(soap);
    soap_end(soap);

    soap->frecv = NULL;
    soap->fsvalidate = NULL;
    soap->fsend = NULL;

    config->error = 0; // Remove all errors
     //config->file -> We don't need free or null this parameter. Parsers always release resource on success/error 
    config->in_filename = NULL; // File name reset
    config->in_file_stream_done = false; // Filestream flag to false
    config->finrootelement = NULL; // AutoDetect function reset
    config->object_type = W21_OBJECT_NONE; // Set object to NONE
    config->object_subtype = W21_OBJECT_NONE; // Set subobject to NONE
    config->current_validation = NULL; // Set current validator to NULL
    memset(&config->witsml21_contract, 0, sizeof(config->witsml21_contract)); // Reset all contracts

#ifdef WITH_STATISTICS
    memset(&config->statistics, 0, sizeof(config->statistics));
    memset(&config->hardware_statistics, 0, sizeof(config->hardware_statistics));
#endif

    // Reset all message details
    config->detail_message[0] = 0;
    config->detail_message_len = 0;

    // Reset all XML message details
    config->detail_message_xml[0] = 0;
    config->detail_message_xml_len = 0;
}

struct c_json_str_t *w21_get_json(struct soap *soap)
{
    DECLARE_W21_CONFIG

    if (config->error == 0) {

        struct c_json_str_t *c_json_str = &(config)->in_json_str;

        if (c_json_str->json)
            return c_json_str;

        if (config->in_bson != NULL) {
            if ((c_json_str->json = bson_as_relaxed_extended_json((const bson_t *)config->in_bson, &c_json_str->json_len)))
                return c_json_str;
  
            set_w21_error_message(soap, E_21_ERROR_UNABLE_TO_PARSE_XML_OBJECT_TO_JSON_STRING, "Could not parse WITSML 2.1 object %s to JSON string", W21_OBJ_TYPE_STR);

            return NULL;
        }

        set_w21_error_message(
            soap,
            E_21_ERROR_UNABLE_TO_PARSE_XML_OBJECT_TO_JSON_STRING_NO_OBJECT,
            "Could not parse WITSML 2.1 %s to JSON string object. BSON object is null", W21_OBJ_TYPE_STR
        );
    }

    // Don't overlaps last error message
    return NULL;
}

struct c_bson_serialized_t *w21_bson_serialize(struct soap *soap)
{
    DECLARE_W21_CONFIG

    if (config->error == 0) {
        struct c_bson_serialized_t *ptr = &config->in_bson_serialized;

        if (ptr->bson)
            return ptr;

        if (config->in_bson) {
            int err;
            if ((err = bson_serialize(&(ptr->bson), &(ptr->bson_size), config->in_bson)) == 0)
                return ptr;

            set_w21_error_message(
                soap, E_21_ERROR_UNABLE_TO_PARSE_XML_OBJECT_TO_BSON_SERIALIZED,
                "Could not parse WITSML 2.1 object %s to BSON serialized. bson_serialize() returned err = %d",
                W21_OBJ_TYPE_STR, err
            );

            return NULL;
        }

        set_w21_error_message(
            soap,
            E_21_ERROR_UNABLE_TO_PARSE_XML_OBJECT_TO_BSON_SERIALIZED_NO_OBJECT,
            "Could not parse WITSML 2.1 %s to BSON serialized. BSON object is null", W21_OBJ_TYPE_STR
        );
    }

    // Don't overlaps last error message
    return NULL;
}

#ifdef WITH_STATISTICS
#ifdef IS_ARM
  #error "Not implemented"
#else
static inline uint64_t get_cycles() {
    uint32_t lo, hi;
    // RDTSCP
    __asm__ __volatile__ ("rdtscp" : "=a" (lo), "=d" (hi) :: "rcx");
    return ((uint64_t)hi << 32) | lo;
}
#endif
struct statistics_t *w21_get_statistics(struct soap *soap)
{

    DECLARE_W21_CONFIG
    struct statistics_t *statistics=&config->statistics;

    if (statistics->total < 1)
        statistics->total=
        statistics->costs +
        statistics->strings +
        statistics->shorts +
        statistics->ints +
        statistics->long64s +
        statistics->enums +
        statistics->arrays +
        statistics->booleans +
        statistics->doubles +
        statistics->date_times +
        statistics->event_types +
        statistics->measures;

    return statistics;
}

int w21_hard_summary_read_begin(struct soap *soap)
{
    DECLARE_W21_CONFIG
    if ((config->hardware_statistics.in_err = clock_gettime(CLOCK_MONOTONIC, &config->hardware_statistics.in_start_time)) == 0) {
        struct mallinfo2 mi;
        mi = mallinfo2();

        config->hardware_statistics.in_mem_start = mi.uordblks;
        config->hardware_statistics.in_start_cycles = get_cycles();
    }

    return config->hardware_statistics.in_err;
}

int w21_hard_summary_read_end(struct soap *soap)
{
    DECLARE_W21_CONFIG
    if (config->hardware_statistics.in_err == 0) {
        struct timespec end_time;
        config->hardware_statistics.in_err = clock_gettime(CLOCK_MONOTONIC, &end_time);

        if (config->hardware_statistics.in_err == 0) {
            config->hardware_statistics.in_total_nanos = 
                (end_time.tv_sec - config->hardware_statistics.in_start_time.tv_sec) * 1E9 + 
                (end_time.tv_nsec - config->hardware_statistics.in_start_time.tv_nsec);

            struct mallinfo2 mi;
            mi = mallinfo2();

            config->hardware_statistics.in_mem_delta = mi.uordblks - config->hardware_statistics.in_mem_start;
            if (config->hardware_statistics.in_mem_delta < 0)
                config->hardware_statistics.in_mem_delta = 0;
            config->hardware_statistics.in_total_cycles = get_cycles() - config->hardware_statistics.in_start_cycles;
        }
    }

    return config->hardware_statistics.in_err;
}

#define W21_HARD_SUMMARY_PARSE_BUILD(name) \
int w21_hard_summary_##name##_begin(struct soap *soap) \
{ \
    DECLARE_W21_CONFIG \
    if ((config->hardware_statistics.in_##name##_err = clock_gettime(CLOCK_MONOTONIC, &config->hardware_statistics.in_##name##_start_time)) == 0) { \
        struct mallinfo2 mi; \
        mi = mallinfo2(); \
\
        config->hardware_statistics.in_##name##_mem_start = mi.uordblks; \
        config->hardware_statistics.in_##name##_start_cycles = get_cycles(); \
    } \
\
    return config->hardware_statistics.in_##name##_err; \
} \
\
int w21_hard_summary_##name##_end(struct soap *soap) \
{ \
    DECLARE_W21_CONFIG \
    if (config->hardware_statistics.in_##name##_err == 0) { \
        struct timespec end_time; \
        config->hardware_statistics.in_##name##_err = clock_gettime(CLOCK_MONOTONIC, &end_time); \
\
        if (config->hardware_statistics.in_##name##_err == 0) { \
            config->hardware_statistics.in_##name##_total_nanos = \
                (end_time.tv_sec - config->hardware_statistics.in_##name##_start_time.tv_sec) * 1E9 + \
                (end_time.tv_nsec - config->hardware_statistics.in_##name##_start_time.tv_nsec); \
\
            struct mallinfo2 mi; \
            mi = mallinfo2(); \
\
            config->hardware_statistics.in_##name##_mem_delta = mi.uordblks - config->hardware_statistics.in_##name##_mem_start; \
            if (config->hardware_statistics.in_##name##_mem_delta < 0) \
                config->hardware_statistics.in_##name##_mem_delta = 0; \
            config->hardware_statistics.in_##name##_total_cycles = get_cycles() - config->hardware_statistics.in_##name##_start_cycles; \
        } \
    } \
\
    return config->hardware_statistics.in_##name##_err; \
}

W21_HARD_SUMMARY_PARSE_BUILD(parse)
W21_HARD_SUMMARY_PARSE_BUILD(parse_json)

/*
int w21_hard_summary_parse_begin(struct soap *soap)
{
    DECLARE_W21_CONFIG
    if ((config->hardware_statistics.in_parse_err = clock_gettime(CLOCK_MONOTONIC, &config->hardware_statistics.in_parse_start_time)) == 0) {
        struct mallinfo2 mi;
        mi = mallinfo2();

        config->hardware_statistics.in_parse_mem_start = mi.uordblks;
        config->hardware_statistics.in_parse_start_cycles = get_cycles();
    }

    return config->hardware_statistics.in_parse_err;
}

int w21_hard_summary_parse_end(struct soap *soap)
{
    DECLARE_W21_CONFIG
    if (config->hardware_statistics.in_parse_err == 0) {
        struct timespec end_time;
        config->hardware_statistics.in_parse_err = clock_gettime(CLOCK_MONOTONIC, &end_time);

        if (config->hardware_statistics.in_parse_err == 0) {
            config->hardware_statistics.in_parse_total_nanos = 
                (end_time.tv_sec - config->hardware_statistics.in_parse_start_time.tv_sec) * 1E9 +
                (end_time.tv_nsec - config->hardware_statistics.in_parse_start_time.tv_nsec);

            struct mallinfo2 mi;
            mi = mallinfo2();

            config->hardware_statistics.in_parse_mem_delta = mi.uordblks - config->hardware_statistics.in_parse_mem_start;
            if (config->hardware_statistics.in_parse_mem_delta < 0)
                config->hardware_statistics.in_parse_mem_delta = 0;
            config->hardware_statistics.in_parse_total_cycles = get_cycles() - config->hardware_statistics.in_parse_start_cycles;
        }
    }

    return config->hardware_statistics.in_parse_err;
}
*/
#endif
