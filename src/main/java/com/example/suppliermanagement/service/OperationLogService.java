package com.example.suppliermanagement.service;

import com.example.suppliermanagement.model.OperationLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

public interface OperationLogService {

    OperationLog createOperationLog(String operationType, String content, String operator, String ipAddress);

    Page<OperationLog> getOperationLogs(
            String operationType, String operator,
            LocalDateTime startDate, LocalDateTime endDate,
            Pageable pageable);
            
    Map<String, Object> getOperationLogStats();
    
    void exportOperationLogs(String operationType, String operator, 
                           LocalDateTime startDate, LocalDateTime endDate, 
                           HttpServletResponse response) throws IOException;
}
    