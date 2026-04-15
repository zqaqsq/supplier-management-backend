package com.example.suppliermanagement.service.impl;

import com.example.suppliermanagement.model.OperationLog;
import com.example.suppliermanagement.repository.OperationLogRepository;
import com.example.suppliermanagement.service.OperationLogService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OperationLogServiceImpl implements OperationLogService {

    private final OperationLogRepository operationLogRepository;

    @Autowired
    public OperationLogServiceImpl(OperationLogRepository operationLogRepository) {
        this.operationLogRepository = operationLogRepository;
    }

    @Override
    public OperationLog createOperationLog(String operationType, String content, String operator, String ipAddress) {
        OperationLog log = new OperationLog();
        log.setOperationType(operationType);
        log.setContent(content);
        log.setOperator(operator);
        log.setIpAddress(ipAddress);
        return operationLogRepository.save(log);
    }

    @Override
    public Page<OperationLog> getOperationLogs(
            String operationType, String operator,
            LocalDateTime startDate, LocalDateTime endDate,
            Pageable pageable) {
        return operationLogRepository.findByConditions(operationType, operator, startDate, endDate, pageable);
    }
    
    @Override
    public Map<String, Object> getOperationLogStats() {
        Map<String, Object> stats = new HashMap<>();
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayStart = now.toLocalDate().atStartOfDay();
        LocalDateTime weekStart = now.minusDays(7).toLocalDate().atStartOfDay();
        LocalDateTime monthStart = now.minusDays(30).toLocalDate().atStartOfDay();
        
        // 今日操作数
        long todayCount = operationLogRepository.countByCreatedAtBetween(todayStart, now);
        stats.put("todayCount", todayCount);
        
        // 本周操作数
        long weekCount = operationLogRepository.countByCreatedAtBetween(weekStart, now);
        stats.put("weekCount", weekCount);
        
        // 本月操作数
        long monthCount = operationLogRepository.countByCreatedAtBetween(monthStart, now);
        stats.put("monthCount", monthCount);
        
        // 总操作数
        long totalCount = operationLogRepository.count();
        stats.put("totalCount", totalCount);
        
        return stats;
    }
    
    @Override
    public void exportOperationLogs(String operationType, String operator, 
                                  LocalDateTime startDate, LocalDateTime endDate, 
                                  HttpServletResponse response) throws IOException {
        
        // 获取所有符合条件的日志（不分页）
        Page<OperationLog> logsPage = operationLogRepository.findByConditions(
            operationType, operator, startDate, endDate, 
            Pageable.unpaged()
        );
        
        List<OperationLog> logs = logsPage.getContent();
        
        // 创建工作簿
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("操作日志");
            
            // 创建标题行样式
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            
            // 创建标题行
            Row headerRow = sheet.createRow(0);
            String[] headers = {"序号", "时间", "操作类型", "操作内容", "操作人", "IP地址", "状态"};
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 15 * 256); // 设置列宽
            }
            
            // 填充数据
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            for (int i = 0; i < logs.size(); i++) {
                OperationLog log = logs.get(i);
                Row row = sheet.createRow(i + 1);
                
                row.createCell(0).setCellValue(i + 1);
                row.createCell(1).setCellValue(log.getCreatedAt() != null ? 
                    log.getCreatedAt().format(formatter) : "");
                row.createCell(2).setCellValue(log.getOperationType());
                row.createCell(3).setCellValue(log.getContent());
                row.createCell(4).setCellValue(log.getOperator());
                row.createCell(5).setCellValue(log.getIpAddress() != null ? log.getIpAddress() : "");
                row.createCell(6).setCellValue("成功");
            }
            
            // 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=操作日志.xlsx");
            
            // 写入响应流
            workbook.write(response.getOutputStream());
        }
    }
}
    