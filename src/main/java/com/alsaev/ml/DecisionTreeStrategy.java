package com.alsaev.ml;

import org.apache.spark.ml.PipelineStage;
import org.apache.spark.ml.classification.DecisionTreeClassifier;
import org.apache.spark.ml.param.ParamMap;
import org.apache.spark.ml.tuning.ParamGridBuilder;

public class DecisionTreeStrategy implements MLStrategy {
    private final DecisionTreeClassifier dt;

    public DecisionTreeStrategy(String label, String features) {
        dt = new DecisionTreeClassifier()
                .setLabelCol(label)
                .setFeaturesCol(features);
    }

    @Override
    public String getName() {
        return "Дерево решений";
    }

    @Override
    public PipelineStage getModel() {
        return dt;
    }

    @Override
    public ParamMap[] getParamGrid() {
        return new ParamGridBuilder()
                .addGrid(dt.maxDepth(), new int[]{3, 5, 9, 12, 15})
                .build();
    }
}
