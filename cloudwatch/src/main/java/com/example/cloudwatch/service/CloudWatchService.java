package com.example.cloudwatch.service;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

import com.example.cloudwatch.value.DetailedEC2StatisticsVo;
import com.example.cloudwatch.value.DetailedRdsStatisticsVo;
import com.example.cloudwatch.value.MetricDataResponse;
import com.example.cloudwatch.type.MetricStatistic;

import com.example.cloudwatch.value.EC2StatisticsVo;
import com.example.cloudwatch.value.MetricVo;
import com.example.cloudwatch.value.RdsStatisticsVo;
import org.modelmapper.ModelMapper;
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
    ModelMapper mapper;
    @Autowired
    private EC2Service ec2Service;

    @Autowired
    private RdsService rdsService;

    /* CloudWatch 수집 쿼리 실행 시 탐색 시간 범위 */
    private final ZoneId ZONE_ID = ZoneId.of("Asia/Seoul");

    public CloudWatchService(AwsCredentialsProvider credentialsProvider) {

        this.cloudWatchClient = CloudWatchClient.builder()
                .region(Region.AP_NORTHEAST_2)
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

    public List<EC2StatisticsVo> getEC2ListMetricStatistics(Instant startTime, Instant endTime, String namespace, String metricName,MetricStatistic statisticsType) {


        List<Instance> instances = ec2Service.describeEC2Instances();
        List<EC2StatisticsVo> result =  new ArrayList<>();

//        String namespace = "AWS/EC2";
//        String metricName = "CPUUtilization"; // "StatusCheckFailed_System"
        Collection<Statistic> statisticType = statisticsType.getValue();


        for (Instance instance : instances) {
//            instance.blockDeviceMappings().stream().forEach(
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
                    vo.setInstanceId(instance.instanceId()); // Setting the instance ID here.
                    vo.setTagName(tagName.value());
                    vo.setInstanceType(String.valueOf(instance.instanceType()));
                    vo.setUnit(datapoint.unit().toString());
                    vo.setStatisticsType(statisticType.toString());
                    double data = getStatisticsType(statisticsType, datapoint);
                    vo.setStatisticsValue(data);
                    result.add(vo);
                }
            }
        }
        return result;
    }

    /**
     * 기간동안의 해당 EC2 인스턴스의 상세 정보 (볼륨, CPU, Memory, ... ) <- cloudwatch agent 설치 및 설정 이후 가능
     *
     * @param startTime
     * @param endTime
     * @param statisticsType
     * @return
     */
    public DetailedEC2StatisticsVo getEC2MetricStatistics(Instant startTime, Instant endTime, String instanceId,String namespace, MetricStatistic statisticsType){

        Instance instance = ec2Service.getEC2Instance(instanceId);
        List<String> metricNames = Arrays.asList("CPUUtilization", "mem_used_percent", "disk_used_percent");
        DetailedEC2StatisticsVo vo = new DetailedEC2StatisticsVo();
        vo.setInstanceId(instance.instanceId()); // Setting the instance ID here.
        vo.setInstanceType(String.valueOf(instance.instanceType()));

        for (String metricName : metricNames) {
            GetMetricStatisticsRequest request = GetMetricStatisticsRequest.builder()
                    .namespace(namespace)
                    .metricName(metricName)
                    .dimensions(Dimension.builder().name("InstanceId").value(instanceId).build())
                    .startTime(startTime) // 예: 1시간 전부터 현재까지
                    .endTime(endTime)
                    .period(300) // 5분 단위로 데이터 가져오기
                    .statistics(statisticsType.getValue()) // 평균값 조회
                    .build();



            GetMetricStatisticsResponse response = cloudWatchClient.getMetricStatistics(request);
            if (!response.datapoints().isEmpty()) {
                Datapoint datapoint = response.datapoints().get(0); // Assuming you want the first datapoint, adjust if needed
                double data = getStatisticsType(statisticsType,  datapoint);
                switch (metricName) {
                    case "CPUUtilization":
                        vo.setCPUUtilization(data);
                        break;
                    case "mem_used_percent":
                        vo.setMem_used_percent(data);
                        break;
                    case "disk_used_percent":
                        vo.setDisk_used_percent(data);
                        break;
                }
            }
        }

//        // EBS 관련 볼륨 정보도 필요할 시
//        Instance instance = ec2Service.getEC2Instance(instanceId);
//        instance.blockDeviceMappings().stream().forEach(
//            blockDevice ->
//                ec2Service.describeEbsVolumes(blockDevice.ebs().volumeId())
//        );
        return vo;
    }




    public List<RdsStatisticsVo> getRdsListMetricStatistics(Instant startTime, Instant endTime, String metricName,MetricStatistic statisticsType) {
        List<DBInstance> instances = rdsService.describeRdsInstancesList();
        List<RdsStatisticsVo> result = new ArrayList<>();

        String namespace = "AWS/RDS";
        Collection<Statistic> statisticType = statisticsType.getValue();

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
                    vo.setEngine(instance.engine());
                    vo.setEndpoint(instance.endpoint());
                    vo.setDbName(instance.dbName());
                    vo.setStatisticsType(statisticType.toString());
                    double data = getStatisticsType(statisticsType, datapoint);
                    vo.setStatisticsValue(data);
                    result.add(vo);
                }
            }
        }
        return result;
    }

    public DetailedRdsStatisticsVo getRdsMetricStatistics(Instant startTime, Instant endTime, String metricName,MetricStatistic statisticsType, String dbInstanceIdentifier){
        String namespace = "AWS/RDS";
        DBInstance instance = rdsService.describeRdsInstances(dbInstanceIdentifier);
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
                .statistics(statisticsType.getValue())
                .build();

        GetMetricStatisticsResponse response = cloudWatchClient.getMetricStatistics(request);
        DetailedRdsStatisticsVo vo = mapper.map(instance, DetailedRdsStatisticsVo.class);

        if (!response.datapoints().isEmpty()) {
            Datapoint datapoint = response.datapoints().get(0);
            vo.setStatisticsType(String.valueOf(statisticsType));
            double data = getStatisticsType(statisticsType, datapoint);
            vo.setStatisticsValue(data);
        }

        return vo;
    }

    /**
     * statisticsType 에 따른 값 설정
     * @param statisticsType
     * @param datapoint
     * @return
     */
    public double getStatisticsType(MetricStatistic statisticsType, Datapoint datapoint){
        switch (statisticsType) {
            case AVERAGE:
                return datapoint.average();
            case SUM:
                return datapoint.sum();
            case MINIMUM:
                return datapoint.minimum();
            case MAXIMUM:
                return datapoint.maximum();
            case SAMPLE_COUNT:
                return datapoint.sampleCount();
            default:
                throw new IllegalArgumentException("Invalid statisticsType: " + statisticsType);
        }
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
