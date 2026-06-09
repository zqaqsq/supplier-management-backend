package com.example.suppliermanagement.controller;

import com.example.suppliermanagement.dto.ApiResponse;
import com.example.suppliermanagement.model.SelectionResult;
import com.example.suppliermanagement.service.SelectionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/selection-results")
@Tag(name = "抽取结果管理")
public class SelectionController {

    @Autowired
    private SelectionService selectionService;

    @PostMapping
    @Operation(summary = "保存抽取结果")
    public ResponseEntity<ApiResponse<SelectionResult>> saveSelectionResult(@RequestBody Map<String, Object> requestData) {
        SelectionResult result = selectionService.saveSelectionResult(requestData);
        return ResponseEntity.ok(ApiResponse.success("抽取结果保存成功", result));
    }

    @GetMapping
    @Operation(summary = "获取抽取结果列表")
    public ResponseEntity<ApiResponse<List<SelectionResult>>> getSelectionResults(@RequestParam(required = false) Long pid) {
        List<SelectionResult> results;
        if (pid != null) {
            results = selectionService.getSelectionResultsByProject(pid);
        } else {
            results = selectionService.getAllSelectionResults();
        }
        return ResponseEntity.ok(ApiResponse.success(results));
    }

    @GetMapping("/projects")
    @Operation(summary = "获取项目列表")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getProjects() {
        // 返回模拟项目数据，实际项目中应该从项目表获取
        List<Map<String, Object>> projects = List.of(
            Map.of("id", 1L, "projectName", "2025.08测试项目", "totalExtract", 5),
            Map.of("id", 2L, "projectName", "2025.09正式项目", "totalExtract", 8),
            Map.of("id", 3L, "projectName", "2025.10招标项目", "totalExtract", 3)
        );
        return ResponseEntity.ok(ApiResponse.success(projects));
    }
}
    