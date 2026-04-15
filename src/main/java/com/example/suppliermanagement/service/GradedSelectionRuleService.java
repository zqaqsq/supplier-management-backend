package com.example.suppliermanagement.service;

import com.example.suppliermanagement.model.GradedSelectionRule;
import java.util.List;

public interface GradedSelectionRuleService {

    /**
     * 创建分级抽取规则
     */
    GradedSelectionRule createRule(GradedSelectionRule rule);

    /**
     * 更新分级抽取规则
     */
    GradedSelectionRule updateRule(Long id, GradedSelectionRule rule);

    /**
     * 删除分级抽取规则
     */
    void deleteRule(Long id);

    /**
     * 根据ID获取规则
     */
    GradedSelectionRule getRuleById(Long id);

    /**
     * 获取所有规则
     */
    List<GradedSelectionRule> getAllRules();

    /**
     * 获取所有活跃规则
     */
    List<GradedSelectionRule> getActiveRules();

    /**
     * 根据资质等级获取规则
     */
    List<GradedSelectionRule> getRulesByQualification(String qualification);

    /**
     * 激活/停用规则
     */
    GradedSelectionRule toggleRuleStatus(Long id);
}
