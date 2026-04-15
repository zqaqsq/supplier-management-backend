package com.example.suppliermanagement.service.impl;

import com.example.suppliermanagement.model.GradedSelectionRule;
import com.example.suppliermanagement.repository.GradedSelectionRuleRepository;
import com.example.suppliermanagement.service.GradedSelectionRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class GradedSelectionRuleServiceImpl implements GradedSelectionRuleService {

    @Autowired
    private GradedSelectionRuleRepository ruleRepository;

    @Override
    public GradedSelectionRule createRule(GradedSelectionRule rule) {
        // 验证规则名称是否重复
        if (ruleRepository.findByQualification(rule.getQualification()).stream()
                .anyMatch(r -> r.getRuleName().equals(rule.getRuleName()))) {
            throw new RuntimeException("规则名称已存在");
        }
        
        return ruleRepository.save(rule);
    }

    @Override
    public GradedSelectionRule updateRule(Long id, GradedSelectionRule rule) {
        GradedSelectionRule existingRule = getRuleById(id);
        
        // 更新字段
        existingRule.setRuleName(rule.getRuleName());
        existingRule.setQualification(rule.getQualification());
        existingRule.setCount(rule.getCount());
        existingRule.setPercentage(rule.getPercentage());
        existingRule.setIndustry(rule.getIndustry());
        existingRule.setRegion(rule.getRegion());
        existingRule.setMinCount(rule.getMinCount());
        existingRule.setMaxCount(rule.getMaxCount());
        existingRule.setIsActive(rule.getIsActive());
        existingRule.setDescription(rule.getDescription());
        
        return ruleRepository.save(existingRule);
    }

    @Override
    public void deleteRule(Long id) {
        if (!ruleRepository.existsById(id)) {
            throw new RuntimeException("规则不存在");
        }
        ruleRepository.deleteById(id);
    }

    @Override
    public GradedSelectionRule getRuleById(Long id) {
        return ruleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("规则不存在"));
    }

    @Override
    public List<GradedSelectionRule> getAllRules() {
        return ruleRepository.findAll();
    }

    @Override
    public List<GradedSelectionRule> getActiveRules() {
        return ruleRepository.findByIsActiveTrue();
    }

    @Override
    public List<GradedSelectionRule> getRulesByQualification(String qualification) {
        return ruleRepository.findByQualificationAndIsActiveTrue(qualification);
    }

    @Override
    public GradedSelectionRule toggleRuleStatus(Long id) {
        GradedSelectionRule rule = getRuleById(id);
        rule.setIsActive(!rule.getIsActive());
        return ruleRepository.save(rule);
    }
}
