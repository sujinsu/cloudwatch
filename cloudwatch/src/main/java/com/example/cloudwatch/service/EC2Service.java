package com.example.cloudwatch.service;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesRequest;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesResponse;
import software.amazon.awssdk.services.ec2.model.DescribeVolumesRequest;
import software.amazon.awssdk.services.ec2.model.DescribeVolumesResponse;
import software.amazon.awssdk.services.ec2.model.Ec2Exception;
import software.amazon.awssdk.services.ec2.model.Instance;
import software.amazon.awssdk.services.ec2.model.Reservation;
import software.amazon.awssdk.services.ec2.model.Tag;
import software.amazon.awssdk.services.ec2.model.Volume;

import java.util.ArrayList;
import java.util.List;

@Service
public class EC2Service {
    private Ec2Client ec2Client;

    public EC2Service(AwsCredentialsProvider credentialsProvider){
        this.ec2Client = Ec2Client.builder()
                .region(Region.AP_NORTHEAST_2)
                .credentialsProvider(credentialsProvider)
                .build();

    }

    public List<Instance> describeEC2Instances() {
        List<Instance> instances = new ArrayList<>();
        String nextToken = null;
        try {
            do {
                DescribeInstancesRequest request = DescribeInstancesRequest.builder()
                        .maxResults(6)
                        .nextToken(nextToken)
                        .build();

                DescribeInstancesResponse response = ec2Client.describeInstances(request);
                for (Reservation reservation : response.reservations()) {
                    for (Instance instance : reservation.instances()) {
                        instances.add(instance);

//                        System.out.println("Found instance with ID: " + instance.instanceId()
//                            + ", NAME: " + tagName.value()
//                            + ", TYPE: " + instance.instanceType());
                    }
                }
                nextToken = response.nextToken();
            } while (nextToken != null);
            return instances;
        } catch (Ec2Exception e) {
            System.err.println(e.awsErrorDetails().errorCode());
            throw new RuntimeException("Failed to describe EC2 instances", e);
        }
    }

    public Instance getEC2Instance(String instanceId){
        DescribeInstancesRequest request = DescribeInstancesRequest.builder()
                .instanceIds(instanceId)
                .build();

        DescribeInstancesResponse response = ec2Client.describeInstances(request);

        return response.reservations().get(0).instances().get(0);
    }
    
    public void describeEbsVolumes(String volumeId){
        DescribeVolumesRequest describeVolumesRequest = DescribeVolumesRequest.builder()
                .volumeIds(volumeId)
                .build();
        DescribeVolumesResponse volumesResponse = ec2Client.describeVolumes(describeVolumesRequest);


        for (Volume volume : volumesResponse.volumes()) {
            System.out.println("volume.toString() = " + volume.toString());
            System.out.printf(
                    "Found volume with id %s, state %s, and size %d GB%n",
                    volume.volumeId(),
                    volume.state(),
                    volume.size()
            );
        }
    }
}
