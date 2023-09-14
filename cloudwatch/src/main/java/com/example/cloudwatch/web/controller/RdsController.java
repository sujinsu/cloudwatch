package com.example.cloudwatch.web.controller;

import com.example.cloudwatch.service.RdsService;
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
@Api(value = "Rds", tags = "Rds")
@RequestMapping("/rds")
public class RdsController {

    private final RdsService rdsService;

    @ApiOperation(value = "RDS 인스턴스 상세", notes = "")
    @GetMapping("/describeInstances")
    public List<DBInstance> describeRdsInstances(
    ) {
        return rdsService.describeRdsInstances();
    }


}
