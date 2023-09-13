package com.example.cloudwatch.value;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Route53HealthCheckVo {

    private String healthCheckId;


    private Long healthThreshold;


    private String servicePrincipal;


    private String type;


    private String domainName;
}
