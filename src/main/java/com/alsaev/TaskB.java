package com.alsaev;


import com.alsaev.ml.DecisionTreeStrategy;
import com.alsaev.ml.LogisticRegressionStrategy;
import com.alsaev.ml.MLStrategy;
import com.alsaev.ml.RandomForestStrategy;
import org.apache.spark.ml.Pipeline;
import org.apache.spark.ml.PipelineStage;
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator;
import org.apache.spark.ml.feature.StringIndexer;
import org.apache.spark.ml.feature.VectorAssembler;
import org.apache.spark.ml.param.ParamMap;
import org.apache.spark.ml.tuning.CrossValidator;
import org.apache.spark.ml.tuning.CrossValidatorModel;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.classic.SparkSession;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class TaskB implements AutoCloseable {
    private static final String LABEL = "label";
    private static final String FEATURES = "features";
    private final String mark;
    private final String[] excludeColumns;
    private final SparkSession spark;
    private final Dataset<Row> dataset;

    public TaskB(String resource, String mark, String... excludeColumns) {
        this.spark = SparkSession.builder()
                .appName("ML")
                .master("local[*]")
                .getOrCreate();

        this.mark = mark;
        this.excludeColumns = excludeColumns;
        this.spark.sparkContext().setLogLevel("ERROR");


        URL url = getClass().getClassLoader().getResource(resource);
        String path = Objects.requireNonNull(url).getPath();

        this.dataset = spark.read()
                .option("header", "true")
                .option("inferSchema", "true")
                .csv(path);
    }

    public void launch() {
        PreparedData data = prepare();

        List<MLStrategy> strategies = List.of(
                new LogisticRegressionStrategy(LABEL, FEATURES),
                new DecisionTreeStrategy(LABEL, FEATURES),
                new RandomForestStrategy(LABEL, FEATURES)
        );

        for (var strategy : strategies) {
            runTask(strategy.getName(), strategy.getModel(), strategy.getParamGrid(),
                    data.assembler, data.indexer, data.train, data.test);
        }
    }

    @Override
    public void close() {
        spark.stop();
    }


    private record PreparedData(
            VectorAssembler assembler,
            StringIndexer indexer,
            Dataset<Row> train,
            Dataset<Row> test
    ) {
    }

    private PreparedData prepare() {
        List<String> excludeColumns = new ArrayList<>(Arrays.asList(this.excludeColumns));
        excludeColumns.add(mark);

        String[] inputColumns = Arrays.stream(dataset.columns())
                .filter(col -> !excludeColumns.contains(col))
                .map(col -> col.contains(".") ? "`" + col + "`" : col)
                .toArray(String[]::new);

        VectorAssembler assembler = new VectorAssembler()
                .setInputCols(inputColumns)
                .setOutputCol(FEATURES);

        StringIndexer indexer = new StringIndexer()
                .setInputCol(mark)
                .setOutputCol(LABEL);

        Dataset<Row>[] splits = dataset.randomSplit(new double[]{0.7, 0.3});
        Dataset<Row> train = splits[0];
        Dataset<Row> test = splits[1];

        return new PreparedData(assembler, indexer, train, test);
    }

    private void runTask(
            String name, PipelineStage model, ParamMap[] grid,
            VectorAssembler assembler, StringIndexer indexer,
            Dataset<Row> train, Dataset<Row> test
    ) {
        Pipeline pipeline = new Pipeline().setStages(new PipelineStage[]{assembler, indexer, model});

        CrossValidator cv = new CrossValidator()
                .setEstimator(pipeline)
                .setEvaluator(new MulticlassClassificationEvaluator().setMetricName("accuracy"))
                .setEstimatorParamMaps(grid)
                .setNumFolds(3);

        CrossValidatorModel bestModel = cv.fit(train);
        Dataset<Row> predictions = bestModel.transform(test);

        System.out.println("\n=== Результаты для " + name + " ===");
        MulticlassClassificationEvaluator mce = new MulticlassClassificationEvaluator().setLabelCol(LABEL);
        System.out.println("Accuracy (Верность): " + mce.setMetricName("accuracy").evaluate(predictions));
        System.out.println("Precision (Точность): " + mce.setMetricName("weightedPrecision").evaluate(predictions));
        System.out.println("Recall (Полнота): " + mce.setMetricName("weightedRecall").evaluate(predictions));

        System.out.println("Матрица ошибок:");
        predictions.groupBy("label", "prediction").count().show();
        /*
            По поводу матрицы ошибок:
            1. Был пожар и модель сказала пожар
            2. Пожара не было, но модель сказала, что был
            3. Пожар был, но модель сказала, что нет
            4. Пожара не было, модель сказала нет
         */
    }


}
