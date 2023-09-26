package com.example.cloudwatch.value;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ResourceRecordVo {

    private String name;
    private String type;
    private long ttl;
    private List<ResourceRecordValueVo> resourceRecords;  // List로 변경

    @Getter
    @Setter
    @NoArgsConstructor
    public static class ResourceRecordValueVo {  // 내부 클래스로 ResourceRecordValueVo 정의
        private String value;
    }
}
