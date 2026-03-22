package com.alsaev.ml;

import org.apache.spark.ml.PipelineStage;
import org.apache.spark.ml.classification.LogisticRegression;
import org.apache.spark.ml.param.ParamMap;
import org.apache.spark.ml.tuning.ParamGridBuilder;

public class LogisticRegressionStrategy implements MLStrategy {
    private final LogisticRegression lr;

    public LogisticRegressionStrategy(String label, String features) {
        lr = new LogisticRegression()
                .setLabelCol(label)
                .setFeaturesCol(features);
    }

    @Override
    public String getName() {
        return "Логистическая регрессия";
    }

    @Override
    public PipelineStage getModel() {
        return lr;
    }

    @Override
    public ParamMap[] getParamGrid() {
        return new ParamGridBuilder()
                .addGrid(lr.maxIter(), new int[]{10, 100, 1000, 10000}) // maxIter=10…10000,
                .addGrid(lr.regParam(), new double[]{0.1, 0.5, 1, 1.5}) // regParam>0 (0.1, 0.5, 1, …),
                .addGrid(lr.elasticNetParam(), new double[]{0, 0.5, 1}) // elasticNetParam=0…1.
                .build();
    }
}
