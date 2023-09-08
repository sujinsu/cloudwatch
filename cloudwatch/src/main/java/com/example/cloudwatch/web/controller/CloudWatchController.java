package com.example.cloudwatch.web.controller;

import com.example.cloudwatch.domain.MetricDataResponse;
import com.example.cloudwatch.service.CloudWatchService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.services.cloudwatch.model.Datapoint;

import java.time.Instant;

@RestController
@RequiredArgsConstructor
@Api("CloudWatch")
public class CloudWatchController {

    private final CloudWatchService cloudWatchService;

    @ApiOperation(value = "지정된 기간 동안의 통계 데이터를 반환", notes = "CloudWatch 메트릭을 조회하기 위한 IAM 권한이 필요")
    @GetMapping("/getMetricData")
    public MetricDataResponse getMetricData(
            @RequestParam Instant startTime,
            @RequestParam Instant endTime) {
        return cloudWatchService.getMetricData(startTime, endTime);
    }

    @ApiOperation(value = "GetMetricStatistics보다 더 유연하게 여러 메트릭의 데이터를 한 번에 조회", notes = "CloudWatch 메트릭을 조회하기 위한 IAM 권한이 필요")
    @GetMapping("/getMetricStatistics")
    public Datapoint getMetricStatistics(
            @RequestParam Instant startTime,
            @RequestParam Instant endTime) {
        return cloudWatchService.getMetricStatistics(startTime, endTime);
    }
}
