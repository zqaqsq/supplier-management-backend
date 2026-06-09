package com.example.suppliermanagement.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 供应商数据传输对象
 * 支持前端期望的字段别名（通过 @JsonProperty）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupplierDTO {

    private Long id;

    private String name;

    @JsonProperty("creditCode")
    private String creditCode;

    /**
     * 资质等级，数据库存储格式为 A/B/C/D
     */
    private String qualification;

    private String region;

    private String industry;

    private String address;

    // 标准字段名
    private String contactPerson;

    // 前端别名
    @JsonProperty("contact")
    public String getContact() {
        return contactPerson;
    }

    public void setContact(String contact) {
        this.contactPerson = contact;
    }

    // 标准字段名
    private String contactPhone;

    // 前端别名
    @JsonProperty("phone")
    public String getPhone() {
        return contactPhone;
    }

    public void setPhone(String phone) {
        this.contactPhone = phone;
    }

    private String contactEmail;

    private String businessScope;

    private String performance;

    private LocalDate establishDate;

    private String legalPerson;

    private BigDecimal registeredCapital;

    private String status;

    private String scale;

    private String qualificationMaterials;

    private LocalDate certificationDate;

    private LocalDate expiryDate;

    // 标准字段名
    private String remark;

    // 前端别名 - 表格导出用
    @JsonProperty("description")
    public String getDescription() {
        return remark;
    }

    // 前端别名 - 导出功能用
    @JsonProperty("notes")
    public String getNotes() {
        return remark;
    }

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
