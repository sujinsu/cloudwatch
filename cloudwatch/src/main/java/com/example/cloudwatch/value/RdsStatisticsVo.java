package com.example.cloudwatch.value;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import software.amazon.awssdk.services.rds.model.Endpoint;

@Getter
@Setter
@NoArgsConstructor
public class RdsStatisticsVo {

    private String engine;

    private Endpoint endpoint;

    private String dbName;

    private String namespace;

    private String metricName;
    private String  statisticsType;

    private double statisticsValue;

}
