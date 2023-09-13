package com.example.cloudwatch.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rds.RdsClient;
import software.amazon.awssdk.services.rds.model.DBInstance;
import software.amazon.awssdk.services.rds.model.DescribeDbInstancesResponse;
import software.amazon.awssdk.services.rds.model.RdsException;

import javax.transaction.Transactional;
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

    public void describeRdsInstances() {
        try {
            DescribeDbInstancesResponse response = rdsClient.describeDBInstances();
            System.out.println("response.toString() = " + response.toString());
            List<DBInstance> instanceList = response.dbInstances();
            for (DBInstance instance: instanceList) {
                System.out.println("The Engine is " + instance.engine());
                System.out.println("Connection endpoint is " + instance.endpoint().address());
                System.out.println("Connection endpoint is " + instance.dbName());
            }
        } catch (RdsException e) {
            System.out.println(e.getLocalizedMessage());
        }
    }
}
