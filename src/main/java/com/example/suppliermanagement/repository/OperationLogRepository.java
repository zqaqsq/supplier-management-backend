package com.example.suppliermanagement.repository;

import com.example.suppliermanagement.model.OperationLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface OperationLogRepository extends JpaRepository<OperationLog, Long> {

    // 按条件查询日志
    @Query("SELECT o FROM OperationLog o WHERE " +
           "(:operationType IS NULL OR o.operationType = :operationType) AND " +
           "(:operator IS NULL OR o.operator LIKE %:operator%) AND " +
           "(:startDate IS NULL OR o.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR o.createdAt <= :endDate) " +
           "ORDER BY o.createdAt DESC")
    Page<OperationLog> findByConditions(
            @Param("operationType") String operationType,
            @Param("operator") String operator,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);
            
    // 统计指定时间范围内的日志数量
    @Query("SELECT COUNT(o) FROM OperationLog o WHERE o.createdAt BETWEEN :startDate AND :endDate")
    long countByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
    