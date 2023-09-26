package com.example.cloudwatch.service;

import com.example.cloudwatch.value.ResourceRecordVo;
import com.example.cloudwatch.value.Route53HealthCheckVo;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.route53.Route53Client;
import software.amazon.awssdk.services.route53.model.HealthCheck;
import software.amazon.awssdk.services.route53.model.HostedZone;
import software.amazon.awssdk.services.route53.model.ListHealthChecksResponse;
import software.amazon.awssdk.services.route53.model.ListHostedZonesResponse;
import software.amazon.awssdk.services.route53.model.ListResourceRecordSetsRequest;
import software.amazon.awssdk.services.route53.model.ListResourceRecordSetsResponse;
import software.amazon.awssdk.services.route53.model.ResourceRecord;
import software.amazon.awssdk.services.route53.model.ResourceRecordSet;
import software.amazon.awssdk.services.route53.model.Route53Exception;
import software.amazon.awssdk.services.route53domains.Route53DomainsClient;
import software.amazon.awssdk.services.route53domains.model.BillingRecord;
import software.amazon.awssdk.services.route53domains.model.ListDomainsResponse;
import software.amazon.awssdk.services.route53domains.model.ViewBillingRequest;
import software.amazon.awssdk.services.route53domains.model.ViewBillingResponse;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class Route53Service {

    private final Route53Client route53Client;
    private final Route53DomainsClient route53DomainsClient;

    private final String HOSTED_ZONE_ID  = "Z39G178FHJVX6N";

    public Route53Service(AwsCredentialsProvider credentialsProvider){
        this.route53Client = Route53Client.builder()
                .region(Region.AP_NORTHEAST_2)
                .credentialsProvider(credentialsProvider)
                .build();
        this.route53DomainsClient = Route53DomainsClient.builder()
//                .region(Region.AP_NORTHEAST_2)
//                .region(Region.AWS_GLOBAL)
                .region(Region.US_EAST_1)
                .credentialsProvider(credentialsProvider)
                .build();
    }

    public List<String> listDomains() {
        List<String> domainNames = new ArrayList<>();
        ListHostedZonesResponse zonesResponse = route53Client.listHostedZones();
        List<HostedZone> hostedZones = zonesResponse.hostedZones();
        for (HostedZone hostedZone: hostedZones) {
//            System.out.println("The hostedZone is : "+hostedZone.toString());
            domainNames.add(hostedZone.name());
        }
        return domainNames;
    }

    // TODO health check 생성 후 조회 가능.
    public List<Route53HealthCheckVo> listRoute53HealthChecks() {

        // 각각 다 호출
        List<Route53HealthCheckVo> result = new ArrayList<>();

        List<String> domainNames = new ArrayList<>();
        ListHostedZonesResponse zonesResponse = route53Client.listHostedZones();
        List<HostedZone> hostedZones = zonesResponse.hostedZones();
        for (HostedZone hostedZone: hostedZones) {

//            System.out.println("The hostedZone is : "+hostedZone.toString());
            domainNames.add(hostedZone.name());
        }

        // health check 목록 호출
        ListHealthChecksResponse checksResponse = route53Client.listHealthChecks();
        List<HealthCheck> checklist = checksResponse.healthChecks();

        try {
            domainNames.forEach(domainName ->
                checklist.stream()
                    .filter(check -> check.healthCheckConfig().fullyQualifiedDomainName().contains(domainName))
                    .forEach(check -> {
                        System.out.println(check.healthCheckConfig().fullyQualifiedDomainName());

                        Route53HealthCheckVo vo = new Route53HealthCheckVo();
                        vo.setHealthCheckId(check.id());
                        vo.setHealthThreshold(Long.valueOf(check.healthCheckConfig().healthThreshold()));
                        vo.setServicePrincipal(check.linkedService().servicePrincipal());
                        vo.setType(check.healthCheckConfig().typeAsString());
                        vo.setDomainName(domainName);

                        result.add(vo);
                    })
            );
            } catch (Route53Exception e) {
                System.err.println(e.getMessage());
            }


        return result;
    }

    public void listBillingRecords() {
        try {
            Date currentDate = new Date();
            LocalDateTime localDateTime = currentDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            ZoneOffset zoneOffset = ZoneOffset.of("+05:30");
            LocalDateTime localDateTime2 = localDateTime.minusYears(1);
            Instant myStartTime = localDateTime2.toInstant(zoneOffset);
            Instant myEndTime = localDateTime.toInstant(zoneOffset);

            ViewBillingRequest viewBillingRequest = ViewBillingRequest.builder()
                    .maxItems(10)
                    .start(myStartTime)
                    .end(myEndTime)
                    .build();

            ViewBillingResponse response = route53DomainsClient.viewBilling(viewBillingRequest);
            List<BillingRecord> records = response.billingRecords();
            if (records.isEmpty()) {
                System.out.println("\tNo billing records found for the past year.");
            } else {
                for (BillingRecord record : records) {
                    System.out.println("Bill Date: "+record.billDate());
                    System.out.println("Operation: "+ record.operationAsString());
                    System.out.println("Price: "+record.price());
                }
            }

        } catch (Route53Exception e) {
            System.err.println(e.getMessage());
        }

    }

    public List<ResourceRecordVo> listResourceRecord() {
        List<ResourceRecordVo> recordVoList = new ArrayList<>();
        try {
            ListResourceRecordSetsRequest request = ListResourceRecordSetsRequest.builder()
                    .hostedZoneId(HOSTED_ZONE_ID)
                    .maxItems("12")
                    .build();

            ListResourceRecordSetsResponse listResourceRecordSets = route53Client.listResourceRecordSets(request);
            List<ResourceRecordSet> records = listResourceRecordSets.resourceRecordSets();

            for (ResourceRecordSet record : records) {
                ResourceRecordVo recordVo = new ResourceRecordVo();  // 각 record에 대해 ResourceRecordVo 인스턴스를 만듭니다.
                recordVo.setName(record.name());
                recordVo.setType(String.valueOf(record.type()));
                if (record.ttl() != null) {
                    recordVo.setTtl(record.ttl());
                }
                List<ResourceRecordVo.ResourceRecordValueVo> resourceRecordValueVoList = new ArrayList<>();  // 각 ResourceRecord의 value를 담을 리스트를 만듭니다.
                for (ResourceRecord resourceRecord : record.resourceRecords()) {
                    ResourceRecordVo.ResourceRecordValueVo resourceRecordValueVo = new ResourceRecordVo.ResourceRecordValueVo();  // 각 ResourceRecord에 대해 ResourceRecordValueVo 인스턴스를 만듭니다.
                    resourceRecordValueVo.setValue(resourceRecord.value());
                    resourceRecordValueVoList.add(resourceRecordValueVo);  // 리스트에 ResourceRecordValueVo 인스턴스를 추가합니다.
                }

                recordVo.setResourceRecords(resourceRecordValueVoList);  // ResourceRecordVo에 ResourceRecordValueVo 리스트를 설정합니다.
                recordVoList.add(recordVo);  // 최종적으로 ResourceRecordVo 리스트에 추가합니다.

            }

        } catch (Route53Exception e) {
            System.err.println(e.getMessage());
        }

        return recordVoList;
    }
}
