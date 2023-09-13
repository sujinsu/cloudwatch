package com.example.cloudwatch.web.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;

@Configuration
public class AwsConfig {
    @Autowired
    private AwsProperties awsProperties;
    @Bean
    public AwsCredentialsProvider awsCredentialsProvider() {

        AwsBasicCredentials awsBasicCredentials = AwsBasicCredentials.create(awsProperties.getAccessKey(), awsProperties.getSecretKey());
        return StaticCredentialsProvider.create(awsBasicCredentials);
    }
}
