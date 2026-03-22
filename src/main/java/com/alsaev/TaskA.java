package com.alsaev;

import org.apache.spark.sql.SparkSession;

import java.net.URL;
import java.util.Objects;

public class TaskA implements AutoCloseable {
    private final SparkSession spark;

    public TaskA(String resource) {
        this.spark = SparkSession.builder()
                .appName("Real Estate Analysis")
                .master("local[*]")
                .getOrCreate();

        this.spark.sparkContext().setLogLevel("ERROR");

        URL url = getClass().getClassLoader().getResource(resource);
        String path = Objects.requireNonNull(url).getPath();

        this.spark.read()
                .option("header", "true")
                .option("inferSchema", "true")
                .csv(path)
                .createOrReplaceTempView("table");
    }

    public void launch() {
        System.out.println("--- Отклонение от среднего года постройки ---");
        query(
                "SELECT year_built, " +
                        "(SELECT AVG(year_built) FROM table) - year_built as diff" +
                        " FROM table"
        );

        System.out.println("--- Количество домов на каждую улицу ---");
        query(
                "SELECT neighborhood, COUNT(DISTINCT address9) as count " +
                        "FROM table " +
                        "GROUP BY neighborhood " +
                        "ORDER BY count DESC"
        );

        System.out.println("--- Сортировка по цене и индексу ---");
        query(
                "SELECT address9, neighborhood, sale_price, zip_code " +
                        "FROM table " +
                        "ORDER BY sale_price ASC, zip_code DESC"
        );

        System.out.println("--- Максимальные цены по категориям ---");
        query(
                "SELECT neighborhood, building_class_category, " +
                        "MAX(sale_price) as max_price, " +
                        "COUNT(*) as count " +
                        "FROM table GROUP BY neighborhood, building_class_category " +
                        "ORDER BY max_price DESC"
        );

    }

    private void query(String sql) {
        spark.sql(sql).show();
    }

    @Override
    public void close() {
        spark.stop();
    }
}
