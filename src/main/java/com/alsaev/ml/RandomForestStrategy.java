package com.alsaev.ml;

import org.apache.spark.ml.PipelineStage;
import org.apache.spark.ml.classification.RandomForestClassifier;
import org.apache.spark.ml.param.ParamMap;
import org.apache.spark.ml.tuning.ParamGridBuilder;

public class RandomForestStrategy implements MLStrategy {
    private final RandomForestClassifier rf;

    public RandomForestStrategy(String label, String features) {
        rf = new RandomForestClassifier()
                .setLabelCol(label)
                .setFeaturesCol(features);
    }

    @Override
    public String getName() {
        return "Случайный лес";
    }

    @Override
    public PipelineStage getModel() {
        return rf;
    }

    @Override
    public ParamMap[] getParamGrid() {
        return new ParamGridBuilder()
                .addGrid(rf.maxDepth(), new int[]{3, 5, 9, 12})
                .addGrid(rf.numTrees(), new int[]{5, 11, 25})
                .build();
    }
}
