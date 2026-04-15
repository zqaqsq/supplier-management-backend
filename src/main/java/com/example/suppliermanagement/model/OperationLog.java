package com.example.suppliermanagement.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "operation_logs")
@EntityListeners(AuditingEntityListener.class)
public class OperationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "操作类型不能为空")
    @Column(nullable = false, name = "operation_type")
    private String operationType;

    @NotBlank(message = "操作内容不能为空")
    @Column(nullable = false, name = "content")
    private String content;

    @NotBlank(message = "操作人不能为空")
    @Column(nullable = false, name = "operator")
    private String operator;

    @Column(name = "ip_address")
    private String ipAddress;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
    