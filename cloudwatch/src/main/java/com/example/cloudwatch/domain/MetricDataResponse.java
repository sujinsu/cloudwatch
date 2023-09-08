package com.example.cloudwatch.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import software.amazon.awssdk.services.cloudwatch.model.MetricDataResult;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class MetricDataResponse {
    private List<MetricDataResult> metricDataResults;
    public MetricDataResponse(List<MetricDataResult> metricDataResults){
        this.metricDataResults = metricDataResults;
    }
}
