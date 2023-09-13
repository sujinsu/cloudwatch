package com.example.cloudwatch.type;

import software.amazon.awssdk.services.cloudwatch.model.Statistic;

import java.util.Collection;
import java.util.Collections;

public enum MetricStatistic {
    AVERAGE("AVERAGE"),
    SUM("SUM"),
    MINIMUM("MINIMUM"),
    MAXIMUM("MAXIMUM"),
    SAMPLE_COUNT("SampleCount");

    private final String value;

    MetricStatistic(String value) {
        this.value = value;
    }

    public Collection<Statistic> getValue() {
        return Collections.singleton(Statistic.valueOf(value));
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
