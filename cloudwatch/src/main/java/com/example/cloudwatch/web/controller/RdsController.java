package com.example.cloudwatch.web.controller;

import com.example.cloudwatch.service.RdsService;
import com.example.cloudwatch.value.DetailedRdsStatisticsVo;
import com.example.cloudwatch.value.DetailedRdsVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.services.rds.model.DBInstance;

import java.lang.reflect.Type;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Api(value = "Rds", tags = "Rds")
@RequestMapping("/rds")
public class RdsController {

    private final RdsService rdsService;


    private final ModelMapper mapper;

    @ApiOperation(value = "RDS 인스턴스 목록 상세", notes = "")
    @GetMapping("/describeList")
    public ResponseEntity<List<DetailedRdsVo>> describeRdsInstancesList() {
        try {
            List<DBInstance> dbInstances = rdsService.describeRdsInstancesList();

            if (dbInstances == null || dbInstances.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }

            Type listType = new TypeToken<List<DetailedRdsVo>>() {}.getType();
            List<DetailedRdsVo> vos = mapper.map(dbInstances, listType);

            return new ResponseEntity<>(vos, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "RDS 인스턴스 상세", notes = "")
    @GetMapping("/describe")
    public ResponseEntity<DetailedRdsVo> describeRdsInstances(
            @RequestParam String dbInstanceIdentifier
    ) {
        try {
            DBInstance instance = rdsService.describeRdsInstances(dbInstanceIdentifier);
            if (instance == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            DetailedRdsVo vo = mapper.map(instance, DetailedRdsVo.class);
            return new ResponseEntity<>(vo, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}
