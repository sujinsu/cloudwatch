package com.example.cloudwatch.value;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RdsStatisticsVo {

    private String engine;

    private Long endpoint;

    private String dbName;

    private double average;


}
