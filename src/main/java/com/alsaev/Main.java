package com.alsaev;

public class Main {
    public static void main(String[] args) {
        try (TaskA taskA = new TaskA("brooklyn_sales_map.csv")) {
            taskA.launch();
        }

        // Метка класса: Fire Alarm. Класс отрицательный – 0 (нет огня), класс положительный – 1 (пожар).
        String mark = "Fire Alarm";
        String[] exclude = {mark, "CNT", "_c0"};

        try (TaskB taskB = new TaskB("smoke_detection_iot.csv", "Fire Alarm", exclude)) {
            taskB.launch();
        }

    }
}