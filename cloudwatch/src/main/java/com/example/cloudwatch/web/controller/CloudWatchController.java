package com.example.cloudwatch.web.controller;

import com.example.cloudwatch.value.MetricDataResponse;
import com.example.cloudwatch.service.CloudWatchService;
import com.example.cloudwatch.service.RdsService;
import com.example.cloudwatch.service.Route53Service;
import com.example.cloudwatch.value.EC2StatisticsVo;
import com.example.cloudwatch.value.MetricVo;
import com.example.cloudwatch.value.RdsStatisticsVo;
import com.example.cloudwatch.value.Route53HealthCheckVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.services.rds.model.DBInstance;
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

    @ApiOperation(value = "", notes = "CloudWatch 메트릭을 조회하기 위한 IAM 권한이 필요")
    @GetMapping("/ec2MetricStatistics")
    public List<EC2StatisticsVo> getEC2MetricStatistics(
//            @RequestParam Instant startTime,
//            @RequestParam Instant endTime
            @RequestParam String namespace,
            @RequestParam String metricName
    ) {
        Instant endTime = Instant.now(); // 현재 시간
//        Instant startTime = endTime.minus(7, ChronoUnit.DAYS); // 7일 전
        Instant startTime = endTime.minus(5, ChronoUnit.MINUTES); // 7일 전
        return cloudWatchService.getEC2MetricStatistics(startTime, endTime, namespace, metricName);
    }

    @ApiOperation(value = "", notes = "CloudWatch 메트릭을 조회하기 위한 IAM 권한이 필요")
    @GetMapping("/rdsMetricStatistics")
    public List<RdsStatisticsVo> getRdsMetricStatistics(
//            @RequestParam Instant startTime,
//            @RequestParam Instant endTime
            @RequestParam String namespace,
            @RequestParam String metricName
    ) {
        Instant endTime = Instant.now(); // 현재 시간
//        Instant startTime = endTime.minus(7, ChronoUnit.DAYS); // 7일 전
        Instant startTime = endTime.minus(5, ChronoUnit.MINUTES); // 7일 전
        return cloudWatchService.getRdsMetricStatistics(startTime, endTime, namespace, metricName);
    }



    @ApiOperation(value = "namespace별 상세 메트릭 조회", notes = "")
    @GetMapping("/metricList")
    public MetricVo getMetricList(
            @RequestParam String namespace
    ) {
        return cloudWatchService.getMetricList(namespace);
    }

}
