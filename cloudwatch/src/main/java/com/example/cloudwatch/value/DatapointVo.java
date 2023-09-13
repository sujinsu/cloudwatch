package com.example.cloudwatch.value;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DatapointVo {

    private String instanceId;
    private String instanceType;
    private String tagName;

    private String namespace;

    private String metricName;
    private String timestamp;
    private String  statisticsType;

    private double statisticsValue;
    private String unit;

}
