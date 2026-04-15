package com.example.suppliermanagement.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "graded_selection_rules")
@EntityListeners(AuditingEntityListener.class)
public class GradedSelectionRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "规则名称不能为空")
    @Column(nullable = false, length = 100)
    private String ruleName;

    @NotBlank(message = "供应商等级不能为空")
    @Column(nullable = false, length = 20)
    private String qualification;

    @NotNull(message = "抽取数量不能为空")
    @Min(value = 1, message = "抽取数量不能小于1")
    @Column(nullable = false, name = "count")
    private Integer count;

    @Min(value = 0, message = "占比不能小于0")
    @NotNull(message = "占比不能为空")
    @Column(nullable = false, name = "percentage")
    private Integer percentage;

    @Column(name = "industry", length = 100)
    private String industry;

    @Column(name = "region", length = 100)
    private String region;

    @Column(name = "min_count")
    private Integer minCount;

    @Column(name = "max_count")
    private Integer maxCount;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "description", length = 500)
    private String description;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
    