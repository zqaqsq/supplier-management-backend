package com.example.suppliermanagement.service;

import com.example.suppliermanagement.model.GradedSelectionRule;
import com.example.suppliermanagement.model.SelectionResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface SelectionService {

    SelectionResult randomSelectSuppliers(
            int count, String qualification, String industry,
            String operator, String ipAddress);

    SelectionResult reRandomSelectSuppliers(
            Long originalResultId, String reason,
            String operator, String ipAddress);

    SelectionResult gradedSelectSuppliers(
            List<GradedSelectionRule> rules,
            String operator, String ipAddress);

    SelectionResult reGradedSelectSuppliers(
            Long originalResultId, String reason,
            String operator, String ipAddress);

    Page<SelectionResult> getRandomSelectionHistory(Pageable pageable);

    Page<SelectionResult> getGradedSelectionHistory(Pageable pageable);

    SelectionResult getSelectionResultById(Long id);

    /**
     * 保存抽取结果
     */
    SelectionResult saveSelectionResult(Map<String, Object> requestData);

    /**
     * 根据项目ID获取抽取结果
     */
    List<SelectionResult> getSelectionResultsByProject(Long projectId);

    /**
     * 获取所有抽取结果
     */
    List<SelectionResult> getAllSelectionResults();
}
    