package com.example.cloudwatch.service;

import com.example.cloudwatch.value.Route53HealthCheckVo;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.route53.Route53Client;
import software.amazon.awssdk.services.route53.model.HealthCheck;
import software.amazon.awssdk.services.route53.model.HostedZone;
import software.amazon.awssdk.services.route53.model.ListHealthChecksResponse;
import software.amazon.awssdk.services.route53.model.ListHostedZonesResponse;
import software.amazon.awssdk.services.route53.model.Route53Exception;
import software.amazon.awssdk.services.route53domains.Route53DomainsClient;
import software.amazon.awssdk.services.route53domains.model.ListDomainsResponse;

import java.util.ArrayList;
import java.util.List;

@Service
public class Route53Service {

    private final Route53Client route53Client;
//    private final Route53DomainsClient route53DomainsClient;

    public Route53Service(AwsCredentialsProvider credentialsProvider){
        this.route53Client = Route53Client.builder()
                .region(Region.AP_NORTHEAST_2)
                .credentialsProvider(credentialsProvider)
                .build();
//        this.route53DomainsClient = Route53DomainsClient.builder()
////                .region(Region.AP_NORTHEAST_2)
////                .region(Region.AWS_GLOBAL)
//                .region(Region.US_EAST_1)
//                .credentialsProvider(credentialsProvider)
//                .build();
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
}
