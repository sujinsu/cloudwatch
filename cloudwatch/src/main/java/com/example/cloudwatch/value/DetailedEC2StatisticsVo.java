package com.example.cloudwatch.value;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DetailedEC2StatisticsVo {

    //TODO 데이터 추가
    private String instanceId;

    private String instanceType;

    private int coreCount;

    private String tagName;

    private double mem_used_percent;

    private double disk_used_percent;

    private double CPUUtilization;
    private String  statisticsType;


    private String unit;
}
