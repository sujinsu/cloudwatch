package com.example.cloudwatch.web.controller;

import com.example.cloudwatch.domain.MetricDataResponse;
import com.example.cloudwatch.service.CloudWatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.services.cloudwatch.model.Datapoint;

import java.time.Instant;

@RestController
@RequiredArgsConstructor
public class CloudWatchController {

    private final CloudWatchService cloudWatchService;


    @GetMapping("/getMetricData")
    public MetricDataResponse getMetricData(
            @RequestParam Instant startTime,
            @RequestParam Instant endTime) {
        return cloudWatchService.getMetricData(startTime, endTime);
    }

    @GetMapping("/getMetricStatistics")
    public Datapoint getMetricStatistics(
            @RequestParam Instant startTime,
            @RequestParam Instant endTime) {
        return cloudWatchService.getMetricStatistics(startTime, endTime);
    }
}
