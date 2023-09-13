package com.example.cloudwatch.service;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rds.RdsClient;
import software.amazon.awssdk.services.rds.model.DBInstance;
import software.amazon.awssdk.services.rds.model.DescribeDbInstancesResponse;
import software.amazon.awssdk.services.rds.model.RdsException;

import java.util.ArrayList;
import java.util.List;

@Service
public class RdsService {
    private final RdsClient rdsClient;


    public RdsService(AwsCredentialsProvider credentialsProvider){

        this.rdsClient = RdsClient.builder()
                .region(Region.AP_NORTHEAST_2)
                .credentialsProvider(credentialsProvider)
                .build();

    }

    public List<DBInstance> describeRdsInstances() {
        List<DBInstance> instanceList = new ArrayList<>();
        try {
            DescribeDbInstancesResponse response = rdsClient.describeDBInstances();
            System.out.println("response.toString() = " + response.toString());
            instanceList = response.dbInstances();

        } catch (RdsException e) {
            System.out.println(e.getLocalizedMessage());
        }

        return instanceList;
    }
}
