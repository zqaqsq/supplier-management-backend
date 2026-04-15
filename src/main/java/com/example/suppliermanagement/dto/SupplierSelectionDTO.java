package com.example.suppliermanagement.dto;

import lombok.Data;
import java.util.List;

@Data
public class SupplierSelectionDTO {
    
    private String selectionType; // random: 随机抽取, graded: 分级抽取
    private Integer totalCount; // 总抽取数量
    private Object conditions; // 抽取条件，可以是JSON字符串或对象
    private String operator; // 操作人
    private String ipAddress; // IP地址
    
    // 分级抽取专用字段
    private List<GradedSelectionRuleDTO> rules;
    
    @Data
    public static class GradedSelectionRuleDTO {
        private String qualification; // 资质等级
        private Integer count; // 抽取数量
        private Integer percentage; // 占比
        private String industry; // 行业限制
        private String region; // 地区限制
    }
}
