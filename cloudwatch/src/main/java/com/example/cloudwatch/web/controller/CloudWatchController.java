package com.example.cloudwatch.web.controller;

import com.example.cloudwatch.value.MetricDataResponse;
import com.example.cloudwatch.service.CloudWatchService;
import com.example.cloudwatch.service.RdsService;
import com.example.cloudwatch.service.Route53Service;
import com.example.cloudwatch.value.DatapointVo;
import com.example.cloudwatch.value.RdsStatisticsVo;
import com.example.cloudwatch.value.Route53HealthCheckVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.services.rds.model.DBInstance;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Api("CloudWatch")
public class CloudWatchController {

    private final CloudWatchService cloudWatchService;
    private final RdsService rdsService;

    private final Route53Service route53Service;

    @ApiOperation(value = "지정된 기간 동안의 통계 데이터를 반환", notes = "CloudWatch 메트릭을 조회하기 위한 IAM 권한이 필요")
    @GetMapping("/getMetricData")
    public MetricDataResponse getMetricData(
            @RequestParam Instant startTime,
            @RequestParam Instant endTime) {
        return cloudWatchService.getMetricData(startTime, endTime);
    }

    @ApiOperation(value = "", notes = "CloudWatch 메트릭을 조회하기 위한 IAM 권한이 필요")
    @GetMapping("/getEC2MetricStatistics")
    public List<DatapointVo> getEC2MetricStatistics(
//            @RequestParam Instant startTime,
//            @RequestParam Instant endTime
    ) {
        Instant endTime = Instant.now(); // 현재 시간
//        Instant startTime = endTime.minus(7, ChronoUnit.DAYS); // 7일 전
        Instant startTime = endTime.minus(5, ChronoUnit.MINUTES); // 7일 전
        return cloudWatchService.getEC2MetricStatistics(startTime, endTime);
    }

    @ApiOperation(value = "", notes = "CloudWatch 메트릭을 조회하기 위한 IAM 권한이 필요")
    @GetMapping("/getRdsMetricStatistics")
    public List<RdsStatisticsVo> getRdsMetricStatistics(
//            @RequestParam Instant startTime,
//            @RequestParam Instant endTime
    ) {
        Instant endTime = Instant.now(); // 현재 시간
//        Instant startTime = endTime.minus(7, ChronoUnit.DAYS); // 7일 전
        Instant startTime = endTime.minus(5, ChronoUnit.MINUTES); // 7일 전
        return cloudWatchService.getRdsMetricStatistics(startTime, endTime);
    }

    @ApiOperation(value = "도메인 리스트 호출, 각각의 헬스체크", notes = "")
    @GetMapping("/listRoute53HealthChecks")
    public List<Route53HealthCheckVo> listRoute53HealthChecks(
    ) {

        return route53Service.listRoute53HealthChecks();
    }

    @ApiOperation(value = "RDS 인스턴스 상세", notes = "")
    @GetMapping("/describeRdsInstances")
    public List<DBInstance> describeRdsInstances(
    ) {
        return rdsService.describeRdsInstances();
//        return cloudWatchService.listRoute53HealthChecks();
    }
}
