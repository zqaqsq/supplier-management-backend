package com.example.suppliermanagement.controller;

import com.example.suppliermanagement.dto.ApiResponse;
import com.example.suppliermanagement.model.GradedSelectionRule;
import com.example.suppliermanagement.service.GradedSelectionRuleService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/graded-selection-rules")
@Tag(name = "分级抽取规则管理")
public class GradedSelectionRuleController {

    @Autowired
    private GradedSelectionRuleService ruleService;

    @PostMapping
    @Operation(summary = "创建分级抽取规则")
    public ResponseEntity<ApiResponse<GradedSelectionRule>> createRule(
            @Valid @RequestBody GradedSelectionRule rule) {
        GradedSelectionRule created = ruleService.createRule(rule);
        return ResponseEntity.ok(ApiResponse.success("规则创建成功", created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新分级抽取规则")
    public ResponseEntity<ApiResponse<GradedSelectionRule>> updateRule(
            @PathVariable Long id,
            @Valid @RequestBody GradedSelectionRule rule) {
        GradedSelectionRule updated = ruleService.updateRule(id, rule);
        return ResponseEntity.ok(ApiResponse.success("规则更新成功", updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除分级抽取规则")
    public ResponseEntity<ApiResponse<Void>> deleteRule(@PathVariable Long id) {
        ruleService.deleteRule(id);
        return ResponseEntity.ok(ApiResponse.success("规则删除成功", null));
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取规则")
    public ResponseEntity<ApiResponse<GradedSelectionRule>> getRuleById(@PathVariable Long id) {
        GradedSelectionRule rule = ruleService.getRuleById(id);
        return ResponseEntity.ok(ApiResponse.success(rule));
    }

    @GetMapping
    @Operation(summary = "获取所有规则")
    public ResponseEntity<ApiResponse<List<GradedSelectionRule>>> getAllRules() {
        List<GradedSelectionRule> rules = ruleService.getAllRules();
        return ResponseEntity.ok(ApiResponse.success(rules));
    }

    @GetMapping("/active")
    @Operation(summary = "获取所有活跃规则")
    public ResponseEntity<ApiResponse<List<GradedSelectionRule>>> getActiveRules() {
        List<GradedSelectionRule> rules = ruleService.getActiveRules();
        return ResponseEntity.ok(ApiResponse.success(rules));
    }

    @GetMapping("/qualification/{qualification}")
    @Operation(summary = "根据资质等级获取规则")
    public ResponseEntity<ApiResponse<List<GradedSelectionRule>>> getRulesByQualification(
            @PathVariable String qualification) {
        List<GradedSelectionRule> rules = ruleService.getRulesByQualification(qualification);
        return ResponseEntity.ok(ApiResponse.success(rules));
    }

    @PostMapping("/{id}/toggle-status")
    @Operation(summary = "激活/停用规则")
    public ResponseEntity<ApiResponse<GradedSelectionRule>> toggleRuleStatus(@PathVariable Long id) {
        GradedSelectionRule rule = ruleService.toggleRuleStatus(id);
        String message = rule.getIsActive() ? "规则已激活" : "规则已停用";
        return ResponseEntity.ok(ApiResponse.success(message, rule));
    }
}
