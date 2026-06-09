package com.example.suppliermanagement.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 供应商列表数据传输对象（简化版）
 * 用于列表展示，不包含大字段
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupplierListDTO {

    private Long id;

    private String name;

    @JsonProperty("creditCode")
    private String creditCode;

    private String qualification;

    private String region;

    private String industry;

    private String status;

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

    private String scale;

    private LocalDate certificationDate;

    private LocalDate expiryDate;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
