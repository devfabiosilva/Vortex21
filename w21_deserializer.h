#ifndef W21_DESERIALIZER_H
 #define W21_DESERIALIZER_H

#include <stdsoap2.h>

int bson_read_BhaRun21(struct soap *);
int bson_read_CementJob21(struct soap *);
int bson_read_CementJobEvaluation21(struct soap *);
int bson_read_Channel21(struct soap *);
int bson_read_ChannelKind21(struct soap *);
int bson_read_ChannelKindDictionary21(struct soap *);
int bson_read_ChannelSet21(struct soap *);
int bson_read_CuttingsGeology21(struct soap *);
int bson_read_CuttingsGeologyInterval21(struct soap *);
int bson_read_DepthRegImage21(struct soap *);
int bson_read_DownholeComponent21(struct soap *);
int bson_read_DrillReport21(struct soap *);
int bson_read_ErrorTerm21(struct soap *);
int bson_read_ErrorTermDictionary21(struct soap *);
int bson_read_FluidsReport21(struct soap *);
int bson_read_InterpretedGeology21(struct soap *);
int bson_read_InterpretedGeologyInterval21(struct soap *);
int bson_read_Log21(struct soap *);
int bson_read_LoggingToolKind21(struct soap *);
int bson_read_LoggingToolKindDictionary21(struct soap *);
int bson_read_MudLogReport21(struct soap *);
int bson_read_MudlogReportInterval21(struct soap *);
int bson_read_OpsReport21(struct soap *);
int bson_read_PPFGChannel21(struct soap *);
int bson_read_PPFGChannelSet21(struct soap *);
int bson_read_PPFGLog21(struct soap *);
int bson_read_Rig21(struct soap *);
int bson_read_RigUtilization21(struct soap *);
int bson_read_Risk21(struct soap *);
int bson_read_ShowEvaluation21(struct soap *);
int bson_read_ShowEvaluationInterval21(struct soap *);
int bson_read_StimJob21(struct soap *);
int bson_read_StimJobStage21(struct soap *);
int bson_read_StimPerforationCluster21(struct soap *);
int bson_read_SurveyProgram21(struct soap *);
int bson_read_Target21(struct soap *);
int bson_read_ToolErrorModel21(struct soap *);
int bson_read_ToolErrorModelDictionary21(struct soap *);
int bson_read_Trajectory21(struct soap *);
int bson_read_TrajectoryStation21(struct soap *);
int bson_read_Tubular21(struct soap *);
int bson_read_WeightingFunction21(struct soap *);
int bson_read_WeightingFunctionDictionary21(struct soap *);
int bson_read_Wellbore21(struct soap *);
int bson_read_WellboreCompletion21(struct soap *);
int bson_read_WellboreGeology21(struct soap *);
int bson_read_WellboreGeometry21(struct soap *);
int bson_read_WellboreGeometrySection21(struct soap *);
int bson_read_WellboreMarker21(struct soap *);
int bson_read_WellboreMarkerSet21(struct soap *);
int bson_read_Well21(struct soap *);
int bson_read_WellCMLedger21(struct soap *);
int bson_read_WellCompletion21(struct soap *);

int bson_read_AutoDetect21(struct soap *);

#endif
