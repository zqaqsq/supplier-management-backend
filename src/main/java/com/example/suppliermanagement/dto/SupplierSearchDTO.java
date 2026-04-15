package com.example.suppliermanagement.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class SupplierSearchDTO {
    
    private String name; // 供应商名称（模糊查询）
    private String creditCode; // 统一社会信用代码
    private String qualification; // 资质等级（单个值）
    private String region; // 地区（单个值）
    private String status; // 经营状态（单个值）
    private String businessScope; // 经营范围（模糊查询）
    private String performance; // 过往业绩（模糊查询）
    private LocalDate establishDateFrom; // 成立时间起始
    private LocalDate establishDateTo; // 成立时间结束
    private String legalPerson; // 法人
    private BigDecimal registeredCapitalFrom; // 注册资本起始
    private BigDecimal registeredCapitalTo; // 注册资本结束
    private LocalDate certificationDateFrom; // 资质认证日期起始
    private LocalDate certificationDateTo; // 资质认证日期结束
    private LocalDate expiryDateFrom; // 资质到期日期起始
    private LocalDate expiryDateTo; // 资质到期日期结束
    
    // 分页参数
    private Integer page = 0;
    private Integer size = 20;
    private String sortBy = "createdAt";
    private String sortDirection = "DESC";
    public void setIndustry(String industry) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setIndustry'");
    }
}
