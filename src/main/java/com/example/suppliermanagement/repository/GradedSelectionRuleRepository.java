package com.example.suppliermanagement.repository;

import com.example.suppliermanagement.model.GradedSelectionRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GradedSelectionRuleRepository extends JpaRepository<GradedSelectionRule, Long> {

    /**
     * 根据资质等级查找规则
     */
    List<GradedSelectionRule> findByQualification(String qualification);

    /**
     * 查找所有活跃规则
     */
    List<GradedSelectionRule> findByIsActiveTrue();

    /**
     * 根据资质等级和活跃状态查找规则
     */
    List<GradedSelectionRule> findByQualificationAndIsActiveTrue(String qualification);

    /**
     * 根据行业查找规则
     */
    List<GradedSelectionRule> findByIndustryAndIsActiveTrue(String industry);

    /**
     * 根据地区查找规则
     */
    List<GradedSelectionRule> findByRegionAndIsActiveTrue(String region);
}
