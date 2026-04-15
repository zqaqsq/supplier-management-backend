package com.example.suppliermanagement.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class SelectionResultDTO {
    
    private Long id;
    private String selectionType;
    private Integer totalCount;
    private String conditions;
    private List<SupplierInfo> results;
    private String operator;
    private Integer retryCount;
    private String reasons;
    private LocalDateTime createdAt;
    private String ipAddress;
    
    @Data
    public static class SupplierInfo {
        private Long id;
        private String name;
        private String creditCode;
        private String qualification;
        private String region;
        private String industry;
        private String address;
        private String contactPerson;
        private String contactPhone;
        private String businessScope;
        private String performance;
        private String status;
        private String scale;
    }
}
