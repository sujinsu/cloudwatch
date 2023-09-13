package com.example.cloudwatch.service;

import com.example.cloudwatch.value.Route53HealthCheckVo;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.route53.Route53Client;
import software.amazon.awssdk.services.route53.model.HealthCheck;
import software.amazon.awssdk.services.route53.model.ListHealthChecksResponse;
import software.amazon.awssdk.services.route53.model.Route53Exception;
import software.amazon.awssdk.services.route53domains.Route53DomainsClient;
import software.amazon.awssdk.services.route53domains.model.ListDomainsResponse;

import java.util.ArrayList;
import java.util.List;

@Service
public class Route53Service {

    private final Route53Client route53Client;
    private final Route53DomainsClient route53DomainsClient;

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

    public List<String> listDomains(Route53DomainsClient route53DomainsClient) {
        List<String> domainNames = new ArrayList<>();
        try {
//            ListDomainsIterable listRes = route53DomainsClient.listDomainsPaginator();
            ListDomainsResponse listDomainsResponse = route53DomainsClient.listDomains();
            System.out.println("listDomainsResponse.toString() = " + listDomainsResponse.toString());
            listDomainsResponse.domains().forEach(domain -> {
                String domainName = domain.domainName();
                domainNames.add(domainName);
                System.out.println("Checking health status for: " + domainName);
//                checkHealthStatus(route53Client, domainName);
            });

//            // 가격 호출
//            Date currentDate = new Date();
//            LocalDateTime localDateTime = currentDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
//            ZoneOffset zoneOffset = ZoneOffset.of("+01:00");
//            LocalDateTime localDateTime2 = localDateTime.minusYears(1);
//            Instant myStartTime = localDateTime2.toInstant(zoneOffset);
//            Instant myEndTime = localDateTime.toInstant(zoneOffset);
//
//            ViewBillingRequest viewBillingRequest = ViewBillingRequest.builder()
//                    .start(myStartTime)
//                    .end(myEndTime)
//                    .build();
//
//            ViewBillingIterable listRes = route53DomainsClient.viewBillingPaginator(viewBillingRequest);
//            System.out.println("listRes.billingRecords().stream().count() = " + listRes.billingRecords().stream().count());
//            listRes.stream()
//                    .flatMap(r -> r.billingRecords().stream())
//                    .forEach(content -> System.out.println(" Bill Date:: " + content.billDate() +
//                            " Operation: " + content.operationAsString() +
//                            " Price: "+content.price()));

            // 도메인 디테일
//            GetDomainDetailRequest detailRequest = GetDomainDetailRequest.builder()
//                    .domainName("dh.digitalkds.co.kr")
//                    .build();
//
//            GetDomainDetailResponse response = route53DomainsClient.getDomainDetail(detailRequest);
//            System.out.println("The contact first name is " + response.registrantContact().firstName());
//            System.out.println("The contact last name is " + response.registrantContact().lastName());
//            System.out.println("The contact org name is " + response.registrantContact().organizationName());

        } catch (Route53Exception e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        return domainNames;
    }

    public List<Route53HealthCheckVo> listRoute53HealthChecks() {

        // 각각 다 호출
        List<Route53HealthCheckVo> result = new ArrayList<>();

        // 도메인 호출
        List<String> domainNames = listDomains(route53DomainsClient);

//
//        for (String domainName : domainNames) {
//            try {
        ListHealthChecksResponse checksResponse = route53Client.listHealthChecks();
        List<HealthCheck> checklist = checksResponse.healthChecks();
        System.out.println("checklist = " + checklist);
//                for (HealthCheck check : checklist) {
//                    // We'll assume health checks with a domain name in their fully qualified domain name are associated
//                    if (check.healthCheckConfig().fullyQualifiedDomainName().contains(domainName)) {
//                        System.out.println(check.healthCheckConfig().fullyQualifiedDomainName());
//
//                        Route53HealthCheckVo vo = new Route53HealthCheckVo();
//                        vo.setHealthCheckId(check.id());
//                        vo.setHealthThreshold(Long.valueOf(check.healthCheckConfig().healthThreshold()));
//                        vo.setServicePrincipal(check.linkedService().servicePrincipal());
//                        vo.setType(check.healthCheckConfig().typeAsString());
//                        vo.setDomainName(domainName); // This associates the health check with a domain
//
//                        result.add(vo);
//                    }
//                }
//            } catch (Route53Exception e) {
//                System.err.println(e.getMessage());
//            }
//        }

        return result;
    }
}
