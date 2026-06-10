package com.example.suppliermanagement.service;

import com.example.suppliermanagement.dto.AiQueryRequest;
import com.example.suppliermanagement.dto.AiQueryResponse;

public interface AiQueryService {

    AiQueryResponse querySuppliers(AiQueryRequest request);

    String generateSummary(String question, int count);
}
