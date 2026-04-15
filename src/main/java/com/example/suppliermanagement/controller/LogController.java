package com.example.suppliermanagement.controller;

import com.example.suppliermanagement.model.OperationLog;
import com.example.suppliermanagement.service.OperationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class LogController {

    private final OperationLogService logService;

    @Autowired
    public LogController(OperationLogService logService) {
        this.logService = logService;
    }

    @GetMapping("/logs")
    public ResponseEntity<Page<OperationLog>> getOperationLogs(
            @RequestParam(required = false) String operationType,
            @RequestParam(required = false) String operator,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @PageableDefault(size = 10) Pageable pageable) {
        
        try {
            Page<OperationLog> logs = logService.getOperationLogs(operationType, operator, startDate, endDate, pageable);
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/operation-logs")
    public ResponseEntity<Page<OperationLog>> getOperationLogsAlternative(
            @RequestParam(required = false) String operationType,
            @RequestParam(required = false) String operator,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @PageableDefault(size = 10) Pageable pageable) {
        
        try {
            Page<OperationLog> logs = logService.getOperationLogs(operationType, operator, startDate, endDate, pageable);
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/operation-logs/stats")
    public ResponseEntity<Map<String, Object>> getOperationLogStats() {
        try {
            Map<String, Object> stats = logService.getOperationLogStats();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", stats);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取统计信息失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/operation-logs/export")
    public void exportOperationLogs(
            @RequestParam(required = false) String operationType,
            @RequestParam(required = false) String operator,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            HttpServletResponse response) throws IOException {
        
        try {
            logService.exportOperationLogs(operationType, operator, startDate, endDate, response);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("导出失败: " + e.getMessage());
        }
    }
}
    