package com.example.cloudwatch.web.controller;

import com.example.cloudwatch.service.RdsService;
import com.example.cloudwatch.service.Route53Service;
import com.example.cloudwatch.value.DetailedRdsVo;
import com.example.cloudwatch.value.ResourceRecordVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<List<String>>  listDomains(
    ) {

        return new ResponseEntity<>(route53Service.listDomains(), HttpStatus.OK);
    }

    @ApiOperation(value = "", notes = "")
    @GetMapping("/listResourceRecord")
    public ResponseEntity<List<ResourceRecordVo>>  listResourceRecord(
    ) {
        return new ResponseEntity<>(route53Service.listResourceRecord(), HttpStatus.OK);
    }

}
