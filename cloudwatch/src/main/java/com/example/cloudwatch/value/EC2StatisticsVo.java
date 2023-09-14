package com.example.cloudwatch.value;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class EC2StatisticsVo {

    private String instanceId;

    private String instanceType;

    private String tagName;

    private String  statisticsType;

    private double statisticsValue;

    private String unit;

}
