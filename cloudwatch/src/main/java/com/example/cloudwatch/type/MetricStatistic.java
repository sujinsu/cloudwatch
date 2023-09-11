package com.example.cloudwatch.type;

import software.amazon.awssdk.services.cloudwatch.model.Statistic;

public enum MetricStatistic {
    AVERAGE("AVERAGE"),
    SUM("Sum"),
    MINIMUM("Minimum"),
    MAXIMUM("Maximum"),
    SAMPLE_COUNT("SampleCount");

    private final String value;

    MetricStatistic(String value) {
        this.value = value;
    }

    public Statistic getValue() {
        return Statistic.valueOf(value);
    }

    public static MetricStatistic fromValue(String value) {
        for (MetricStatistic statistic : MetricStatistic.values()) {
            if (statistic.getValue().equals(value)) {
                return statistic;
            }
        }
        throw new IllegalArgumentException("Unknown metric statistic: " + value);
    }
}
