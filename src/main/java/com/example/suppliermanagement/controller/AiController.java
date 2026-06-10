package com.example.suppliermanagement.controller;

import com.example.suppliermanagement.common.ApiResponse;
import com.example.suppliermanagement.dto.AiQueryRequest;
import com.example.suppliermanagement.dto.AiQueryResponse;
import com.example.suppliermanagement.service.AiQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    @Autowired
    private AiQueryService aiQueryService;

    @PostMapping("/query")
    public ApiResponse<AiQueryResponse> query(@RequestBody AiQueryRequest request) {
        if (request.getQuestion() == null || request.getQuestion().trim().isEmpty()) {
            return ApiResponse.error("问题不能为空");
        }

        AiQueryResponse response = aiQueryService.querySuppliers(request);
        return ApiResponse.success(response);
    }

    @GetMapping("/status")
    public ApiResponse<Object> status() {
        return ApiResponse.success(java.util.Map.of(
                "aiEnabled", Boolean.TRUE,
                "features", java.util.List.of("智能查询", "供应商筛选", "自然语言解析")
        ));
    }
}
