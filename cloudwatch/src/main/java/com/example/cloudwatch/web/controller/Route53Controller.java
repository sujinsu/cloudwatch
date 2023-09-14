package com.example.cloudwatch.web.controller;

import com.example.cloudwatch.service.RdsService;
import com.example.cloudwatch.service.Route53Service;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.services.rds.model.DBInstance;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Api(value = "Route53", tags = "Route53")
@RequestMapping("/route53")
public class Route53Controller {


    private final Route53Service route53Service;


    @ApiOperation(value = "도메인 리스트", notes = "")
    @GetMapping("/listDomains")
    public List<String> listDomains(
    ) {

        return route53Service.listDomains();
    }

}