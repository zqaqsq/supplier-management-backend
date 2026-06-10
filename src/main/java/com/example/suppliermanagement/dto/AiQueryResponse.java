package com.example.suppliermanagement.dto;

import java.util.List;
import java.util.Map;

public class AiQueryResponse {
    private String sql;
    private Map<String, Object> conditions;
    private List<SupplierDTO> results;
    private String summary;
    private int totalCount;

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public Map<String, Object> getConditions() {
        return conditions;
    }

    public void setConditions(Map<String, Object> conditions) {
        this.conditions = conditions;
    }

    public List<SupplierDTO> getResults() {
        return results;
    }

    public void setResults(List<SupplierDTO> results) {
        this.results = results;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }
}
