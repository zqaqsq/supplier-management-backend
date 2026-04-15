package com.example.suppliermanagement.dto;

import lombok.Data;

@Data
public class RetrySelectionRequest {
    
    private String reason; // 重新抽取原因
    private String operator; // 操作人
    
    // 默认构造函数
    public RetrySelectionRequest() {}
    
    // 带参数构造函数
    public RetrySelectionRequest(String reason, String operator) {
        this.reason = reason;
        this.operator = operator;
    }
}
