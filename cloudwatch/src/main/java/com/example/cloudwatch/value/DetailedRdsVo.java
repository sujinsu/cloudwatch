package com.example.cloudwatch.value;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import software.amazon.awssdk.services.rds.model.Endpoint;


@Getter
@Setter
@NoArgsConstructor
public class DetailedRdsVo {
    private String dbInstanceIdentifier;
    private String storageType;
    private Integer allocatedStorage;
    private String engine;
    private Endpoint endpoint;
    private String dbName;
}
