package com.example.suppliermanagement.repository;

import com.example.suppliermanagement.model.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long>, JpaSpecificationExecutor<Supplier> {

    // 根据统一社会信用代码查找供应商
    Optional<Supplier> findByCreditCode(String creditCode);

    // 高级搜索查询
    @Query("SELECT s FROM Supplier s WHERE " +
           "(:name IS NULL OR s.name LIKE %:name%) AND " +
           "(:creditCode IS NULL OR s.creditCode LIKE %:creditCode%) AND " +
           "(:qualification IS NULL OR s.qualification = :qualification) AND " +
           "(:region IS NULL OR s.region LIKE %:region%) AND " +
           "(:industry IS NULL OR s.industry LIKE %:industry%) AND " +
           "(:status IS NULL OR s.status = :status) AND " +
           "(:scale IS NULL OR s.scale = :scale) AND " +
           "(:businessScope IS NULL OR s.businessScope LIKE %:businessScope%)")
    Page<Supplier> findByAdvancedSearch(
            @Param("name") String name,
            @Param("creditCode") String creditCode,
            @Param("qualification") String qualification,
            @Param("region") String region,
            @Param("industry") String industry,
            @Param("status") String status,
            @Param("scale") String scale,
            @Param("businessScope") String businessScope,
            Pageable pageable);

    // 随机抽取符合条件的供应商
    @Query("SELECT s FROM Supplier s WHERE " +
           "(:qualification IS NULL OR s.qualification = :qualification) AND " +
           "(:industry IS NULL OR s.industry LIKE %:industry%) AND " +
           "s.status = 'Normal'")
    Page<Supplier> findEligibleForRandomSelection(
            @Param("qualification") String qualification,
            @Param("industry") String industry,
            Pageable pageable);

    // 按等级抽取符合条件的供应商
    @Query("SELECT s FROM Supplier s WHERE " +
           "s.qualification = :qualification AND " +
           "(:industry IS NULL OR s.industry LIKE %:industry%) AND " +
           "s.status = 'Normal'")
    Page<Supplier> findEligibleForGradedSelection(
            @Param("qualification") String qualification,
            @Param("industry") String industry,
            Pageable pageable);

    // 获取所有资质等级
    @Query("SELECT DISTINCT s.qualification FROM Supplier s ORDER BY s.qualification")
    List<String> findDistinctQualifications();

    // 获取所有地区
    @Query("SELECT DISTINCT s.region FROM Supplier s ORDER BY s.region")
    List<String> findDistinctRegions();

    // 获取所有行业
    @Query("SELECT DISTINCT s.industry FROM Supplier s ORDER BY s.industry")
    List<String> findDistinctIndustries();

    // 获取所有经营状态
    @Query("SELECT DISTINCT s.status FROM Supplier s ORDER BY s.status")
    List<String> findDistinctStatuses();

    // 获取所有企业规模
    @Query("SELECT DISTINCT s.scale FROM Supplier s ORDER BY s.scale")
    List<String> findDistinctScales();
}
    