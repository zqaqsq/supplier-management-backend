package com.example.suppliermanagement.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "suppliers")
@EntityListeners(AuditingEntityListener.class)
public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "供应商名称不能为空")
    @Column(nullable = false, length = 200)
    private String name;

    @NotBlank(message = "统一社会信用代码不能为空")
    @Column(nullable = false, unique = true, name = "credit_code", length = 50)
    private String creditCode;

    @NotBlank(message = "资质等级不能为空")
    @Column(nullable = false, name = "qualification", length = 20)
    private String qualification; // A级、B级、C级、D级

    @NotBlank(message = "地区不能为空")
    @Column(nullable = false, length = 100)
    private String region;

    @NotBlank(message = "行业不能为空")
    @Column(nullable = false, length = 100)
    private String industry;

    @Column(name = "address", length = 500)
    private String address;

    @Column(name = "contact_person", length = 50)
    private String contactPerson;

    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    @Column(name = "contact_email", length = 100)
    private String contactEmail;

    @Column(name = "business_scope", length = 2000)
    private String businessScope;

    @Column(name = "performance", length = 2000)
    private String performance;

    @Column(name = "establish_date")
    private LocalDate establishDate;

    @Column(name = "legal_person", length = 50)
    private String legalPerson;

    @Column(name = "registered_capital", precision = 15, scale = 2)
    private BigDecimal registeredCapital;

    @NotBlank(message = "经营状态不能为空")
    @Column(nullable = false, name = "status", length = 20)
    private String status; // 正常、停业、注销

    @Column(name = "scale", length = 20)
    private String scale; // 大型、中型、小型、微型

    @Column(name = "qualification_materials", length = 1000)
    private String qualificationMaterials; // 资质材料文件路径，JSON格式

    @Column(name = "certification_date")
    private LocalDate certificationDate; // 资质认证日期

    @Column(name = "expiry_date")
    private LocalDate expiryDate; // 资质到期日期

    @Column(name = "remark", length = 1000)
    private String remark; // 备注信息

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
    