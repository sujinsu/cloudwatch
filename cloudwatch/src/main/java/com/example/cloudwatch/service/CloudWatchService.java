package com.example.cloudwatch.service;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesRequest;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesResponse;
import software.amazon.awssdk.services.ec2.model.Instance;
import software.amazon.awssdk.services.ec2.model.Reservation;
import software.amazon.awssdk.services.ec2.model.Ec2Exception;
import software.amazon.awssdk.services.ec2.model.Tag;

import software.amazon.awssdk.services.rds.RdsClient;
import software.amazon.awssdk.services.rds.model.DescribeDbInstancesResponse;
import software.amazon.awssdk.services.rds.model.DBInstance;
import software.amazon.awssdk.services.rds.model.RdsException;

import software.amazon.awssdk.services.route53.Route53Client;
import software.amazon.awssdk.services.route53.model.HealthCheck;
import software.amazon.awssdk.services.route53.model.Route53Exception;
import software.amazon.awssdk.services.route53.model.ListHealthChecksResponse;

import javax.transaction.Transactional;

@Service
@Transactional
public class CloudWatchService {

    private final CloudWatchClient cloudWatchClient;
    private final Ec2Client ec2Client;
    private final RdsClient rdsClient;

    private final Route53Client route53Client;

    /* CloudWatch 수집 쿼리 실행 시 탐색 시간 범위 */
    private final ZoneId ZONE_ID = ZoneId.of("Asia/Seoul");

    public CloudWatchService() {
        /* Provider 생성을 위한 Credentials */
        // TODO accessKey secretKey 분리 및 Region 설정
        String accessKey = "xxxxxxxxxx";
        String secretKey = "xxxxxxxxxxxxxxxxxxxx";
        AwsBasicCredentials awsBasicCredentials = AwsBasicCredentials.create(accessKey, secretKey);
        AwsCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(awsBasicCredentials);
        this.cloudWatchClient = CloudWatchClient.builder().region(Region.US_WEST_2).credentialsProvider(credentialsProvider).build();
        this.ec2Client = Ec2Client.builder().region(Region.US_WEST_2).credentialsProvider(credentialsProvider).build();
        this.rdsClient = RdsClient.builder().region(Region.US_WEST_2).credentialsProvider(credentialsProvider).build();
        this.route53Client = Route53Client.builder()
                .region(Region.US_WEST_2)
                .credentialsProvider(credentialsProvider)
                .build();
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
        if (!response.datapoints().isEmpty()) {
            for (Datapoint datapoint : response.datapoints()) {
                System.out.println("Timestamp: " + datapoint.timestamp().atZone(ZONE_ID));
                System.out.println("Average: " + datapoint.average());
                System.out.println("Unit: " + datapoint.unit());
            }
        }
        return response.datapoints().isEmpty() ? null : response.datapoints().get(0);
    }

    public List<Instance> describeEC2Instances() {
        List<Instance> instances = new ArrayList<>();
        String nextToken = null;
        try {
            do {
                DescribeInstancesRequest request = DescribeInstancesRequest.builder().maxResults(6).nextToken(nextToken).build();
                DescribeInstancesResponse response = ec2Client.describeInstances(request);
                for (Reservation reservation : response.reservations()) {
                    for (Instance instance : reservation.instances()) {
                        instances.add(instance);
                        instance.instanceId();
                        Tag tagName = instance.tags().stream()
                                .filter(o -> o.key().equals("Name"))
                                .findFirst()
                                .orElse(Tag.builder().key("Name").value("name not found").build());

                        System.out.println("Found instance with ID: " + instance.instanceId()
                                + ", NAME: " + tagName.value()
                                + ", TYPE: " + instance.instanceType());
                    }
                }
                nextToken = response.nextToken();
            } while (nextToken != null);
            return instances;
        } catch (Ec2Exception e) {
            System.err.println(e.awsErrorDetails().errorCode());
            throw new RuntimeException("Failed to describe EC2 instances", e);
        }
    }

    public void describeRdsInstances() {
        try {
            DescribeDbInstancesResponse response = rdsClient.describeDBInstances();
            List<DBInstance> instanceList = response.dbInstances();
            for (DBInstance instance: instanceList) {
                System.out.println("The Engine is " + instance.engine());
                System.out.println("Connection endpoint is " + instance.endpoint().address());
                System.out.println("Connection endpoint is " + instance.dbName());
            }
        } catch (RdsException e) {
            System.out.println(e.getLocalizedMessage());
            System.exit(1);
        }
    }

    public void listRoute53HealthChecks() {
        try {
            ListHealthChecksResponse checksResponse = route53Client.listHealthChecks();
            List<HealthCheck> checklist = checksResponse.healthChecks();
            for (HealthCheck check: checklist) {
                System.out.println("The health check id is: "+check.id());
                System.out.println("The health threshold is: "+check.healthCheckConfig().healthThreshold());
                System.out.println("The health threshold is: "+check.linkedService().servicePrincipal());
                System.out.println("The type is: "+check.healthCheckConfig().typeAsString());
            }
        } catch (Route53Exception e) {
            System.err.println(e.getMessage());
        }
    }
}
