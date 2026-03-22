package com.alsaev.ml;

import org.apache.spark.ml.PipelineStage;
import org.apache.spark.ml.param.ParamMap;

public interface MLStrategy {
    String getName();

    PipelineStage getModel();

    ParamMap[] getParamGrid();
}
