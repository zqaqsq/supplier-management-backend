package com.example.suppliermanagement.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "selection_results")
@EntityListeners(AuditingEntityListener.class)
public class SelectionResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "抽取类型不能为空")
    @Column(nullable = false, name = "selection_type")
    private String selectionType; // random: 随机抽取, graded: 分级抽取

    @NotNull(message = "抽取数量不能为空")
    @Column(nullable = false, name = "total_count")
    private Integer totalCount;

    @Column(name = "conditions", length = 1000)
    private String conditions; // 抽取条件，JSON格式

    @Column(name = "results", length = 2000)
    private String results; // 抽取结果，JSON格式

    @NotBlank(message = "操作人不能为空")
    @Column(nullable = false, name = "operator")
    private String operator;

    @Column(name = "retry_count")
    private Integer retryCount = 0;

    @Column(name = "reasons", length = 2000)
    private String reasons; // 重新抽取原因，JSON格式

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "ip_address")
    private String ipAddress;
}
    