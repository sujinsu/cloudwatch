package com.example.cloudwatch.service;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

import com.example.cloudwatch.value.MetricDataResponse;
import com.example.cloudwatch.type.MetricStatistic;

import com.example.cloudwatch.value.EC2StatisticsVo;
import com.example.cloudwatch.value.MetricVo;
import com.example.cloudwatch.value.RdsStatisticsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.Datapoint;
import software.amazon.awssdk.services.cloudwatch.model.Dimension;
import software.amazon.awssdk.services.cloudwatch.model.GetMetricDataRequest;
import software.amazon.awssdk.services.cloudwatch.model.GetMetricDataResponse;
import software.amazon.awssdk.services.cloudwatch.model.GetMetricStatisticsRequest;
import software.amazon.awssdk.services.cloudwatch.model.GetMetricStatisticsResponse;
import software.amazon.awssdk.services.cloudwatch.model.ListMetricsRequest;
import software.amazon.awssdk.services.cloudwatch.model.ListMetricsResponse;
import software.amazon.awssdk.services.cloudwatch.model.Metric;
import software.amazon.awssdk.services.cloudwatch.model.MetricDataQuery;
import software.amazon.awssdk.services.cloudwatch.model.MetricDataResult;
import software.amazon.awssdk.services.cloudwatch.model.MetricStat;


import software.amazon.awssdk.services.cloudwatch.model.Statistic;
import software.amazon.awssdk.services.ec2.model.Instance;
import software.amazon.awssdk.services.ec2.model.Tag;
import software.amazon.awssdk.services.rds.model.DBInstance;




@Service
public class CloudWatchService {

    private final CloudWatchClient cloudWatchClient;

    @Autowired
    private EC2Service ec2Service;

    @Autowired
    private RdsService rdsService;

    /* CloudWatch 수집 쿼리 실행 시 탐색 시간 범위 */
    private final ZoneId ZONE_ID = ZoneId.of("Asia/Seoul");

    public CloudWatchService(AwsCredentialsProvider credentialsProvider) {

        this.cloudWatchClient = CloudWatchClient.builder().region(Region.AP_NORTHEAST_2).credentialsProvider(credentialsProvider).build();


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

    public List<EC2StatisticsVo> getEC2MetricStatistics(Instant startTime, Instant endTime, String namespace, String metricName) {


        List<Instance> instances = ec2Service.describeEC2Instances();
        List<EC2StatisticsVo> result =  new ArrayList<>();

//        String namespace = "AWS/EC2";
//        String metricName = "CPUUtilization"; // "StatusCheckFailed_System"
        Collection<Statistic> statisticType;
        if(Objects.equals(metricName, "StatusCheckFailed_System")){
            statisticType = MetricStatistic.SAMPLE_COUNT.getValue();
        }else{
            statisticType = MetricStatistic.MAXIMUM.getValue();
        }



        for (Instance instance : instances) {
//            instance.blockDeviceMappings().stream().forEach(
//
//                blockDevice ->
//                    ec2Service.describeEbsVolumes(blockDevice.ebs().volumeId())
////                    System.out.println("blockDevice.toString() = " + )
//            );


            
            Tag tagName = instance.tags().stream()
                    .filter(o -> o.key().equals("Name"))
                    .findFirst()
                    .orElse(Tag.builder().key("Name").value("name not found").build());

            Dimension dimension = Dimension.builder()
                    .name("InstanceId")
                    .value(instance.instanceId())
                    .build();



            GetMetricStatisticsRequest request = GetMetricStatisticsRequest.builder()
                    .namespace(namespace)
                    .metricName(metricName)
                    .startTime(startTime)
                    .endTime(endTime)
                    .period(300)
                    .dimensions(dimension)
                    .statistics(statisticType)
                    .build();

            GetMetricStatisticsResponse response = cloudWatchClient.getMetricStatistics(request);

            if (!response.datapoints().isEmpty()) {
                for (Datapoint datapoint : response.datapoints()) {
                    EC2StatisticsVo vo = new EC2StatisticsVo();
//                    System.out.println("datapoint sampleCount= " + datapoint.sampleCount());
                    vo.setInstanceId(instance.instanceId()); // Setting the instance ID here.
                    vo.setTimestamp(datapoint.timestamp().toString());
                    vo.setTagName(tagName.value());
                    vo.setMetricName(metricName);
                    vo.setNamespace(namespace);
                    vo.setInstanceType(String.valueOf(instance.instanceType()));
                    vo.setUnit(datapoint.unit().toString());
                    vo.setStatisticsType(statisticType.toString());
                    if(statisticType.equals(MetricStatistic.SAMPLE_COUNT.getValue())){
                        vo.setStatisticsValue(datapoint.sampleCount());
                    }else{
                        vo.setStatisticsValue(datapoint.maximum());
                    }

                    result.add(vo);
                }
            }
        }
        return result;
    }
    public List<RdsStatisticsVo> getRdsMetricStatistics(Instant startTime, Instant endTime, String namespace, String metricName) {
        List<DBInstance> instances = rdsService.describeRdsInstances();
        List<RdsStatisticsVo> result = new ArrayList<>();

//        String namespace = "AWS/RDS";
//        String metricName = "CPUUtilization";
        Collection<Statistic> statisticType = MetricStatistic.AVERAGE.getValue();

        for (DBInstance instance : instances) {


            Dimension dimension = Dimension.builder()
                    .name("DBInstanceIdentifier")
                    .value(instance.dbInstanceIdentifier())
                    .build();

            GetMetricStatisticsRequest request = GetMetricStatisticsRequest.builder()
                    .namespace(namespace)
                    .metricName(metricName)
                    .startTime(startTime)
                    .endTime(endTime)
                    .period(300)
                    .dimensions(dimension)
                    .statistics(statisticType)
                    .build();

            GetMetricStatisticsResponse response = cloudWatchClient.getMetricStatistics(request);

            if (!response.datapoints().isEmpty()) {
                for (Datapoint datapoint : response.datapoints()) {
                    RdsStatisticsVo vo = new RdsStatisticsVo();
                    vo.setMetricName(metricName);
                    vo.setNamespace(namespace);
                    vo.setEngine(instance.engine());
                    vo.setEndpoint(instance.endpoint());
                    vo.setDbName(instance.dbName());
                    vo.setStatisticsType(statisticType.toString());
                    vo.setStatisticsValue(datapoint.maximum());
                    result.add(vo);
                }
            }
        }
        return result;
    }



    public MetricVo getMetricList(String namespace) {
        MetricVo result = new MetricVo();
        result.setNamespace(namespace);

        ListMetricsRequest request = ListMetricsRequest.builder()
                .namespace(namespace)
                .build();

        String nextToken = null;
        List<String> metrics = new ArrayList<>();
        do {
            ListMetricsResponse response = cloudWatchClient.listMetrics(request);

            for (Metric metric : response.metrics()) {
                metrics.add(metric.metricName());
//                System.out.println("metric.toString() = " + metric.toString());
//                System.out.printf(
//                        "Retrieved metric %s with dimensions %s%n",
//                        metric.metricName(), metric.dimensions());
            }

            nextToken = response.nextToken();
            request = request.toBuilder().nextToken(nextToken).build();

        } while (nextToken != null);

        result.setMetrics(new ArrayList<>(new LinkedHashSet<>(metrics)));
        return result;
    }
}
