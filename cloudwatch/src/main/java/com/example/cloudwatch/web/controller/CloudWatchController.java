package com.example.cloudwatch.web.controller;

import com.example.cloudwatch.type.MetricStatistic;
import com.example.cloudwatch.value.MetricDataResponse;
import com.example.cloudwatch.service.CloudWatchService;
import com.example.cloudwatch.value.EC2StatisticsVo;
import com.example.cloudwatch.value.MetricVo;
import com.example.cloudwatch.value.RdsStatisticsVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Api(value = "CloudWatch", tags = "CloudWatch")
public class CloudWatchController {

    private final CloudWatchService cloudWatchService;

    @ApiIgnore
    @ApiOperation(value = "지정된 기간 동안의 통계 데이터를 반환", notes = "유료")
    @GetMapping("/getMetricData")
    public MetricDataResponse getMetricData(
            @RequestParam Instant startTime,
            @RequestParam Instant endTime) {
        return cloudWatchService.getMetricData(startTime, endTime);
    }

    @ApiOperation(value = "기간동안 EC2 인스턴스 목록 통계 조회", notes = "CloudWatch 메트릭을 조회하기 위한 IAM 권한이 필요")
    @GetMapping("/metricStatistics/ec2")
    public List<EC2StatisticsVo> getEC2MetricStatistics(
//            @RequestParam Instant startTime,
//            @RequestParam Instant endTime
            @ApiParam(example = "CPUUtilization") @RequestParam String metricName,
            @ApiParam(value = "통계 내고자 하는 타입 : MAXIMUM(최대), MINIMUM(최소), AVERAGE(평균), SAMPLE_COUNT, SUM ", example = "AVERAGE") @RequestParam MetricStatistic statisticsType
    ) {
        Instant endTime = Instant.now(); // 현재 시간
        Instant startTime = endTime.minus(5, ChronoUnit.MINUTES); // 7일 전
        return cloudWatchService.getEC2MetricStatistics(startTime, endTime, metricName, statisticsType);
    }



    @ApiOperation(value = "기간동안 RDS 인스턴스 목록 통계 조회", notes = "CloudWatch 메트릭을 조회하기 위한 IAM 권한이 필요")
    @GetMapping("/metricStatistics/rds")
    public List<RdsStatisticsVo> getRdsMetricStatistics(
//            @RequestParam Instant startTime,
//            @RequestParam Instant endTime
            @ApiParam(example = "CPUUtilization") @RequestParam String metricName,
            @ApiParam(value = "통계 내고자 하는 타입 : MAXIMUM(최대), MINIMUM(최소), AVERAGE(평균), SAMPLE_COUNT, SUM ", example = "AVERAGE") @RequestParam MetricStatistic statisticsType

    ) {
        Instant endTime = Instant.now(); // 현재 시간
        Instant startTime = endTime.minus(5, ChronoUnit.MINUTES); // 7일 전
        return cloudWatchService.getRdsMetricStatistics(startTime, endTime, metricName, statisticsType);
    }


    @ApiOperation(value = "namespace별 상세 메트릭 조회", notes = "")
    @GetMapping("/metricList")
    public MetricVo getMetricList(
            @ApiParam(example = "AWS/EC2") @RequestParam String namespace
    ) {
        return cloudWatchService.getMetricList(namespace);
    }



}
