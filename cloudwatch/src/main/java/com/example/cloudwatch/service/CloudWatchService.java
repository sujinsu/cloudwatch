package com.example.cloudwatch.service;

import com.example.cloudwatch.domain.MetricDataResponse;

import com.example.cloudwatch.type.MetricStatistic;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.Datapoint;
import software.amazon.awssdk.services.cloudwatch.model.GetMetricDataRequest;
import software.amazon.awssdk.services.cloudwatch.model.GetMetricDataResponse;
import software.amazon.awssdk.services.cloudwatch.model.GetMetricStatisticsRequest;
import software.amazon.awssdk.services.cloudwatch.model.GetMetricStatisticsResponse;
import software.amazon.awssdk.services.cloudwatch.model.Metric;
import software.amazon.awssdk.services.cloudwatch.model.MetricDataQuery;
import software.amazon.awssdk.services.cloudwatch.model.MetricDataResult;
import software.amazon.awssdk.services.cloudwatch.model.MetricStat;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;

import static com.fasterxml.jackson.datatype.jsr310.deser.JSR310StringParsableDeserializer.ZONE_ID;

@Service
public class CloudWatchService {

    private final CloudWatchClient cloudWatchClient;

    /* CloudWatch 수집 쿼리 실행 시 탐색 시간 범위 */
    private final ZoneId ZONE_ID = ZoneId.of("Asia/Seoul");

    public CloudWatchService() {
        /* Provider 생성을 위한 Credentials */
        String accessKey = "xxxxxxxxxx";
        String secretKey = "xxxxxxxxxxxxxxxxxxxx";
        AwsBasicCredentials awsBasicCredentials = AwsBasicCredentials.create(accessKey, secretKey);
        AwsCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(awsBasicCredentials);
        this.cloudWatchClient = CloudWatchClient.builder().region(Region.US_WEST_2).credentialsProvider(credentialsProvider).build();
//        this.cloudWatchClient = CloudWatchClient.create();
    }

    public MetricDataResponse getMetricData(Instant startTime, Instant endTime) {
        GetMetricDataRequest request = GetMetricDataRequest.builder()
                .startTime(startTime)
                .endTime(endTime)
                .metricDataQueries(
                        Collections.singletonList(
                                MetricDataQuery.builder()
                                        .id("m1")
                                        .metricStat(
                                                MetricStat.builder()
                                                        .metric(
                                                                Metric.builder()
                                                                        .namespace("AWS/EC2")
                                                                        .metricName("CPUUtilization")
                                                                        .build())
                                                        .period(300)
                                                        .stat("Average")
                                                        .build())
                                        .returnData(true)
                                        .build()))
                .build();

        GetMetricDataResponse response = cloudWatchClient.getMetricData(request);

        List<Instant> timestamps = null;
        List<Double> values = null;
        for(MetricDataResult result : response.metricDataResults()) {
            timestamps = result.timestamps();
            values = result.values();
            System.out.println(String.format("id : %s", result.id()));
            for (int i=values.size()-1; i>=0; i--) {
                System.out.println(String.format("timestamp : %s, value : %s", timestamps.get(i).atZone(ZONE_ID), values.get(i)));
            }
        }
        return new MetricDataResponse(response.metricDataResults());
    }

    public Datapoint getMetricStatistics(Instant startTime, Instant endTime) {
        GetMetricStatisticsRequest request = GetMetricStatisticsRequest.builder()
                .namespace("AWS/EC2")
                .metricName("CPUUtilization")
                .startTime(startTime)
                .endTime(endTime)
                .period(300)
                .statistics(MetricStatistic.AVERAGE.getValue())
                .build();

        GetMetricStatisticsResponse response = cloudWatchClient.getMetricStatistics(request);

        return response.datapoints().isEmpty() ? null : response.datapoints().get(0);
    }
}
