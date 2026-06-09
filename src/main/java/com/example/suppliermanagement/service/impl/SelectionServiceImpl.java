package com.example.suppliermanagement.service.impl;

import com.example.suppliermanagement.model.GradedSelectionRule;
import com.example.suppliermanagement.model.SelectionResult;
import com.example.suppliermanagement.model.Supplier;
import com.example.suppliermanagement.repository.SelectionResultRepository;
import com.example.suppliermanagement.service.OperationLogService;
import com.example.suppliermanagement.service.SelectionService;
import com.example.suppliermanagement.service.SupplierService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SelectionServiceImpl implements SelectionService {

    private final SelectionResultRepository selectionResultRepository;
    private final SupplierService supplierService;
    private final OperationLogService logService;
    private final ObjectMapper objectMapper;

    @Autowired
    public SelectionServiceImpl(
            SelectionResultRepository selectionResultRepository,
            SupplierService supplierService,
            OperationLogService logService,
            ObjectMapper objectMapper) {
        this.selectionResultRepository = selectionResultRepository;
        this.supplierService = supplierService;
        this.logService = logService;
        this.objectMapper = objectMapper;
    }

    @Override
    public SelectionResult randomSelectSuppliers(
            int count, String qualification, String industry,
            String operator, String ipAddress) {
        try {
            // 执行智能随机抽取
            List<Supplier> selectedSuppliers = performSmartRandomSelection(qualification, industry, count);
            
            // 准备条件和结果的JSON字符串
            Map<String, Object> conditions = new HashMap<>();
            conditions.put("count", count);
            conditions.put("qualification", qualification);
            conditions.put("industry", industry);
            conditions.put("selectionMethod", "智能随机抽取");
            conditions.put("algorithm", "加权随机算法");
            String conditionsJson = objectMapper.writeValueAsString(conditions);
            
            List<Long> supplierIds = selectedSuppliers.stream()
                    .map(Supplier::getId)
                    .collect(Collectors.toList());
            String resultsJson = objectMapper.writeValueAsString(supplierIds);
            
            // 创建并保存抽取结果
            SelectionResult result = new SelectionResult();
            result.setSelectionType("random");
            result.setTotalCount(count);
            result.setConditions(conditionsJson);
            result.setResults(resultsJson);
            result.setOperator(operator);
            result.setIpAddress(ipAddress);
            
            SelectionResult savedResult = selectionResultRepository.save(result);
            
            // 记录操作日志
            logService.createOperationLog(
                    "智能随机抽取", 
                    String.format("智能随机抽取了%d家供应商，结果ID: %d，算法: 加权随机算法", count, savedResult.getId()),
                    operator, 
                    ipAddress);
            
            return savedResult;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to process selection data", e);
        }
    }

    /**
     * 执行智能随机抽取
     * 使用加权随机算法，考虑供应商的资质等级、历史表现等因素
     */
    private List<Supplier> performSmartRandomSelection(String qualification, String industry, int count) {
        // 获取符合条件的供应商
        List<Supplier> eligibleSuppliers = supplierService.getEligibleForRandomSelection(qualification, industry, count * 3);
        
        if (eligibleSuppliers.isEmpty()) {
            throw new RuntimeException("没有找到符合条件的供应商");
        }
        
        // 计算每个供应商的权重
        Map<Supplier, Double> supplierWeights = calculateSupplierWeights(eligibleSuppliers);
        
        // 使用加权随机算法选择供应商
        List<Supplier> selectedSuppliers = new ArrayList<>();
        Random random = new Random();
        
        for (int i = 0; i < count; i++) {
            if (eligibleSuppliers.isEmpty()) break;
            
            // 计算总权重
            double totalWeight = eligibleSuppliers.stream()
                    .mapToDouble(supplierWeights::get)
                    .sum();
            
            // 生成随机数
            double randomValue = random.nextDouble() * totalWeight;
            
            // 选择供应商
            Supplier selectedSupplier = null;
            double currentWeight = 0;
            
            for (Supplier supplier : eligibleSuppliers) {
                currentWeight += supplierWeights.get(supplier);
                if (currentWeight >= randomValue) {
                    selectedSupplier = supplier;
                    break;
                }
            }
            
            if (selectedSupplier != null) {
                selectedSuppliers.add(selectedSupplier);
                eligibleSuppliers.remove(selectedSupplier);
            }
        }
        
        return selectedSuppliers;
    }

    /**
     * 计算供应商权重
     * 考虑资质等级、地区分布、行业多样性等因素
     */
    private Map<Supplier, Double> calculateSupplierWeights(List<Supplier> suppliers) {
        Map<Supplier, Double> weights = new HashMap<>();
        
        // 资质等级权重 - 与数据库中存储的格式保持一致（A/B/C/D）
        Map<String, Double> qualificationWeights = Map.of(
            "A", 1.0,
            "B", 0.8,
            "C", 0.6,
            "D", 0.4
        );
        
        // 地区分布权重（避免过度集中）
        Map<String, Long> regionCounts = suppliers.stream()
                .collect(Collectors.groupingBy(Supplier::getRegion, Collectors.counting()));
        
        // 行业多样性权重
        Map<String, Long> industryCounts = suppliers.stream()
                .collect(Collectors.groupingBy(Supplier::getIndustry, Collectors.counting()));
        
        for (Supplier supplier : suppliers) {
            double weight = 1.0;
            
            // 资质等级权重
            weight *= qualificationWeights.getOrDefault(supplier.getQualification(), 0.5);
            
            // 地区分布权重（地区供应商越多，权重越低）
            long regionCount = regionCounts.get(supplier.getRegion());
            weight *= (1.0 / Math.sqrt(regionCount));
            
            // 行业多样性权重
            long industryCount = industryCounts.get(supplier.getIndustry());
            weight *= (1.0 / Math.sqrt(industryCount));
            
            // 经营状态权重
            if ("正常".equals(supplier.getStatus())) {
                weight *= 1.2;
            } else if ("停业".equals(supplier.getStatus())) {
                weight *= 0.5;
            }
            
            weights.put(supplier, weight);
        }
        
        return weights;
    }

    @Override
    public SelectionResult reRandomSelectSuppliers(
            Long originalResultId, String reason,
            String operator, String ipAddress) {
        // 获取原始抽取结果
        SelectionResult originalResult = selectionResultRepository.findById(originalResultId)
                .orElseThrow(() -> new EntityNotFoundException("Selection result not found with id: " + originalResultId));
        
        if (!"random".equals(originalResult.getSelectionType())) {
            throw new IllegalArgumentException("This is not a random selection result");
        }
        
        try {
            // 解析原始抽取条件
            Map<String, Object> conditions = objectMapper.readValue(originalResult.getConditions(), Map.class);
            int count = (Integer) conditions.get("count");
            String qualification = (String) conditions.get("qualification");
            String industry = (String) conditions.get("industry");
            
            // 执行重新抽取
            List<Supplier> selectedSuppliers = performSmartRandomSelection(qualification, industry, count);
            
            // 准备新的结果JSON
            List<Long> supplierIds = selectedSuppliers.stream()
                    .map(Supplier::getId)
                    .collect(Collectors.toList());
            String resultsJson = objectMapper.writeValueAsString(supplierIds);
            
            // 更新重试次数和原因
            int newRetryCount = originalResult.getRetryCount() + 1;
            
            List<String> reasons = new ArrayList<>();
            if (originalResult.getReasons() != null && !originalResult.getReasons().isEmpty()) {
                reasons = objectMapper.readValue(originalResult.getReasons(), List.class);
            }
            reasons.add(String.format("第%d次重新抽取，原因: %s，时间: %s", 
                    newRetryCount, reason, new Date().toString()));
            String reasonsJson = objectMapper.writeValueAsString(reasons);
            
            // 更新结果
            originalResult.setResults(resultsJson);
            originalResult.setRetryCount(newRetryCount);
            originalResult.setReasons(reasonsJson);
            
            SelectionResult updatedResult = selectionResultRepository.save(originalResult);
            
            // 记录操作日志
            logService.createOperationLog(
                    "重新智能随机抽取", 
                    String.format("第%d次重新智能随机抽取，结果ID: %d，原因: %s", 
                            newRetryCount, updatedResult.getId(), reason),
                    operator, 
                    ipAddress);
            
            return updatedResult;
        } catch (Exception e) {
            throw new RuntimeException("Failed to re-select suppliers", e);
        }
    }

    @Override
    public SelectionResult gradedSelectSuppliers(
            List<GradedSelectionRule> rules,
            String operator, String ipAddress) {
        try {
            // 验证规则
            if (rules == null || rules.isEmpty()) {
                throw new IllegalArgumentException("Selection rules cannot be empty");
            }
            
            // 执行智能分级抽取
            Map<String, List<Supplier>> gradedResults = performSmartGradedSelection(rules);
            int totalCount = gradedResults.values().stream().mapToInt(List::size).sum();
            
            // 准备条件和结果的JSON字符串
            String conditionsJson = objectMapper.writeValueAsString(rules);
            
            Map<String, List<Long>> resultIds = new HashMap<>();
            for (Map.Entry<String, List<Supplier>> entry : gradedResults.entrySet()) {
                List<Long> ids = entry.getValue().stream()
                        .map(Supplier::getId)
                        .collect(Collectors.toList());
                resultIds.put(entry.getKey(), ids);
            }
            String resultsJson = objectMapper.writeValueAsString(resultIds);
            
            // 创建并保存抽取结果
            SelectionResult result = new SelectionResult();
            result.setSelectionType("graded");
            result.setTotalCount(totalCount);
            result.setConditions(conditionsJson);
            result.setResults(resultsJson);
            result.setOperator(operator);
            result.setIpAddress(ipAddress);
            
            SelectionResult savedResult = selectionResultRepository.save(result);
            
            // 记录操作日志
            logService.createOperationLog(
                    "智能分级抽取", 
                    String.format("智能分级抽取了%d家供应商，结果ID: %d，算法: 多维度评估算法", totalCount, savedResult.getId()),
                    operator, 
                    ipAddress);
            
            return savedResult;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to process selection data", e);
        }
    }

    /**
     * 执行智能分级抽取
     * 使用多维度评估算法，考虑供应商的综合实力
     */
    private Map<String, List<Supplier>> performSmartGradedSelection(List<GradedSelectionRule> rules) {
        Map<String, List<Supplier>> gradedResults = new HashMap<>();
        
        for (GradedSelectionRule rule : rules) {
            // 获取符合条件的供应商
            List<Supplier> eligibleSuppliers = supplierService.getEligibleForGradedSelection(
                    rule.getQualification(), rule.getIndustry(), rule.getCount() * 2);
            
            if (eligibleSuppliers.isEmpty()) {
                continue;
            }
            
            // 使用多维度评估算法选择供应商
            List<Supplier> selectedSuppliers = selectSuppliersByMultiDimensionalEvaluation(
                    eligibleSuppliers, rule.getCount(), rule);
            
            gradedResults.put(rule.getQualification(), selectedSuppliers);
        }
        
        return gradedResults;
    }

    /**
     * 多维度评估算法选择供应商
     * 考虑资质等级、地区分布、行业匹配度、历史表现等多个维度
     */
    private List<Supplier> selectSuppliersByMultiDimensionalEvaluation(
            List<Supplier> suppliers, int count, GradedSelectionRule rule) {
        
        // 计算每个供应商的综合评分
        Map<Supplier, Double> supplierScores = new HashMap<>();
        
        for (Supplier supplier : suppliers) {
            double score = 0.0;
            
            // 资质等级评分
            score += calculateQualificationScore(supplier.getQualification());
            
            // 地区分布评分（避免过度集中）
            score += calculateRegionalDistributionScore(supplier, suppliers);
            
            // 行业匹配度评分
            score += calculateIndustryMatchScore(supplier, rule);
            
            // 经营状态评分
            score += calculateBusinessStatusScore(supplier);
            
            // 企业规模评分
            score += calculateCompanyScaleScore(supplier);
            
            supplierScores.put(supplier, score);
        }
        
        // 按评分排序并选择前N个
        return suppliers.stream()
                .sorted((s1, s2) -> Double.compare(supplierScores.get(s2), supplierScores.get(s1)))
                .limit(count)
                .collect(Collectors.toList());
    }

    private double calculateQualificationScore(String qualification) {
        // 与数据库中存储的格式保持一致（A/B/C/D）
        Map<String, Double> scores = Map.of(
            "A", 100.0,
            "B", 80.0,
            "C", 60.0,
            "D", 40.0
        );
        return scores.getOrDefault(qualification, 50.0);
    }

    private double calculateRegionalDistributionScore(Supplier supplier, List<Supplier> allSuppliers) {
        // 计算该地区供应商数量
        long regionCount = allSuppliers.stream()
                .filter(s -> s.getRegion().equals(supplier.getRegion()))
                .count();
        
        // 地区供应商越少，评分越高（鼓励地区多样性）
        return Math.max(0, 50 - regionCount * 5);
    }

    private double calculateIndustryMatchScore(Supplier supplier, GradedSelectionRule rule) {
        if (rule.getIndustry() == null || rule.getIndustry().isEmpty()) {
            return 50.0; // 无行业限制时给中等评分
        }
        
        if (rule.getIndustry().equals(supplier.getIndustry())) {
            return 100.0; // 完全匹配
        } else if (supplier.getIndustry().contains(rule.getIndustry()) || 
                   rule.getIndustry().contains(supplier.getIndustry())) {
            return 80.0; // 部分匹配
        } else {
            return 30.0; // 不匹配
        }
    }

    private double calculateBusinessStatusScore(Supplier supplier) {
        Map<String, Double> scores = Map.of(
            "正常", 100.0,
            "停业", 30.0,
            "注销", 0.0
        );
        return scores.getOrDefault(supplier.getStatus(), 50.0);
    }

    private double calculateCompanyScaleScore(Supplier supplier) {
        Map<String, Double> scores = Map.of(
            "大型", 100.0,
            "中型", 80.0,
            "小型", 60.0,
            "微型", 40.0
        );
        return scores.getOrDefault(supplier.getScale(), 50.0);
    }

    @Override
    public SelectionResult reGradedSelectSuppliers(
            Long originalResultId, String reason,
            String operator, String ipAddress) {
        // 获取原始抽取结果
        SelectionResult originalResult = selectionResultRepository.findById(originalResultId)
                .orElseThrow(() -> new EntityNotFoundException("Selection result not found with id: " + originalResultId));
        
        if (!"graded".equals(originalResult.getSelectionType())) {
            throw new IllegalArgumentException("This is not a graded selection result");
        }
        
        try {
            // 解析原始抽取规则
            List<GradedSelectionRule> rules = Arrays.asList(
                    objectMapper.readValue(originalResult.getConditions(), GradedSelectionRule[].class));
            
            // 执行重新抽取
            Map<String, List<Supplier>> gradedResults = performSmartGradedSelection(rules);
            int totalCount = gradedResults.values().stream().mapToInt(List::size).sum();
            
            // 准备新的结果JSON
            Map<String, List<Long>> resultIds = new HashMap<>();
            for (Map.Entry<String, List<Supplier>> entry : gradedResults.entrySet()) {
                List<Long> ids = entry.getValue().stream()
                        .map(Supplier::getId)
                        .collect(Collectors.toList());
                resultIds.put(entry.getKey(), ids);
            }
            String resultsJson = objectMapper.writeValueAsString(resultIds);
            
            // 更新重试次数和原因
            int newRetryCount = originalResult.getRetryCount() + 1;
            
            List<String> reasons = new ArrayList<>();
            if (originalResult.getReasons() != null && !originalResult.getReasons().isEmpty()) {
                reasons = objectMapper.readValue(originalResult.getReasons(), List.class);
            }
            reasons.add(String.format("第%d次重新抽取，原因: %s，时间: %s", 
                    newRetryCount, reason, new Date().toString()));
            String reasonsJson = objectMapper.writeValueAsString(reasons);
            
            // 更新结果
            originalResult.setResults(resultsJson);
            originalResult.setRetryCount(newRetryCount);
            originalResult.setReasons(reasonsJson);
            
            SelectionResult updatedResult = selectionResultRepository.save(originalResult);
            
            // 记录操作日志
            logService.createOperationLog(
                    "重新智能分级抽取", 
                    String.format("第%d次重新智能分级抽取了%d家供应商，结果ID: %d，原因: %s", 
                            newRetryCount, totalCount, updatedResult.getId(), reason),
                    operator, 
                    ipAddress);
            
            return updatedResult;
        } catch (Exception e) {
            throw new RuntimeException("Failed to re-select suppliers", e);
        }
    }

    @Override
    public Page<SelectionResult> getRandomSelectionHistory(Pageable pageable) {
        return selectionResultRepository.findBySelectionTypeOrderByCreatedAtDesc("random", pageable);
    }

    @Override
    public Page<SelectionResult> getGradedSelectionHistory(Pageable pageable) {
        return selectionResultRepository.findBySelectionTypeOrderByCreatedAtDesc("graded", pageable);
    }

    @Override
    public SelectionResult getSelectionResultById(Long id) {
        return selectionResultRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Selection result not found with id: " + id));
    }

    @Override
    public SelectionResult saveSelectionResult(Map<String, Object> requestData) {
        try {
            SelectionResult result = new SelectionResult();
            result.setSelectionType("manual");
            result.setTotalCount((Integer) requestData.get("totalExtract"));
            result.setConditions(objectMapper.writeValueAsString(requestData));
            result.setResults(objectMapper.writeValueAsString(requestData.get("selected_suppliers")));
            result.setOperator("admin");
            result.setIpAddress("127.0.0.1");
            result.setCreatedAt(java.time.LocalDateTime.now());
            
            return selectionResultRepository.save(result);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save selection result", e);
        }
    }

    @Override
    public List<SelectionResult> getSelectionResultsByProject(Long projectId) {
        // 这里应该根据项目ID查询，暂时返回所有结果
        return selectionResultRepository.findAll();
    }

    @Override
    public List<SelectionResult> getAllSelectionResults() {
        return selectionResultRepository.findAll();
    }

    /**
     * 优化版本：执行智能随机抽取
     * 使用本地数据库中的供应商数据进行抽取
     */
    private List<Supplier> performOptimizedRandomSelection(String qualification, String industry, int count) {
        try {
            // 获取所有符合条件的供应商
            List<Supplier> allSuppliers = supplierService.getAllSuppliers();
            
            if (allSuppliers.isEmpty()) {
                throw new RuntimeException("数据库中没有供应商数据");
            }
            
            // 过滤符合条件的供应商
            List<Supplier> eligibleSuppliers = allSuppliers.stream()
                    .filter(supplier -> {
                        // 资质等级过滤
                        if (qualification != null && !qualification.isEmpty() && !"all".equals(qualification)) {
                            if (!qualification.equals(supplier.getQualification())) {
                                return false;
                            }
                        }
                        
                        // 行业过滤
                        if (industry != null && !industry.isEmpty() && !"all".equals(industry)) {
                            if (!industry.equals(supplier.getIndustry())) {
                                return false;
                            }
                        }
                        
                        // 只选择经营状态正常的供应商
                        return "正常".equals(supplier.getStatus());
                    })
                    .collect(Collectors.toList());
            
            if (eligibleSuppliers.isEmpty()) {
                throw new RuntimeException("没有找到符合条件的供应商");
            }
            
            // 如果符合条件的供应商数量不足，给出提示
            if (eligibleSuppliers.size() < count) {
                System.out.println("符合条件的供应商只有" + eligibleSuppliers.size() + "家，少于要求的" + count + "家");
                count = eligibleSuppliers.size();
            }
            
            // 计算每个供应商的权重
            Map<Supplier, Double> supplierWeights = calculateOptimizedSupplierWeights(eligibleSuppliers);
            
            // 使用加权随机算法选择供应商
            List<Supplier> selectedSuppliers = new ArrayList<>();
            Random random = new Random();
            
            for (int i = 0; i < count; i++) {
                if (eligibleSuppliers.isEmpty()) break;
                
                // 计算总权重
                double totalWeight = eligibleSuppliers.stream()
                        .mapToDouble(supplierWeights::get)
                        .sum();
                
                // 生成随机数
                double randomValue = random.nextDouble() * totalWeight;
                
                // 选择供应商
                Supplier selectedSupplier = null;
                double currentWeight = 0;
                
                for (Supplier supplier : eligibleSuppliers) {
                    currentWeight += supplierWeights.get(supplier);
                    if (currentWeight >= randomValue) {
                        selectedSupplier = supplier;
                        break;
                    }
                }
                
                if (selectedSupplier != null) {
                    selectedSuppliers.add(selectedSupplier);
                    eligibleSuppliers.remove(selectedSupplier);
                }
            }
            
            return selectedSuppliers;
            
        } catch (Exception e) {
            System.err.println("执行优化抽取时发生错误: " + e.getMessage());
            throw new RuntimeException("抽取失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 优化版本：计算供应商权重
     * 考虑资质等级、地区分布、行业多样性等因素
     */
    private Map<Supplier, Double> calculateOptimizedSupplierWeights(List<Supplier> suppliers) {
        Map<Supplier, Double> weights = new HashMap<>();
        
        // 资质等级权重 - 与数据库中存储的格式保持一致（A/B/C/D）
        Map<String, Double> qualificationWeights = Map.of(
            "A", 1.0,
            "B", 0.8,
            "C", 0.6,
            "D", 0.4
        );
        
        // 地区分布权重（避免过度集中）
        Map<String, Long> regionCounts = suppliers.stream()
                .collect(Collectors.groupingBy(Supplier::getRegion, Collectors.counting()));
        
        // 行业多样性权重
        Map<String, Long> industryCounts = suppliers.stream()
                .collect(Collectors.groupingBy(Supplier::getIndustry, Collectors.counting()));
        
        for (Supplier supplier : suppliers) {
            double weight = 1.0;
            
            // 资质等级权重
            weight *= qualificationWeights.getOrDefault(supplier.getQualification(), 0.5);
            
            // 地区分布权重（地区供应商越多，权重越低）
            long regionCount = regionCounts.getOrDefault(supplier.getRegion(), 1L);
            weight *= (1.0 / Math.sqrt(regionCount));
            
            // 行业多样性权重
            long industryCount = industryCounts.getOrDefault(supplier.getIndustry(), 1L);
            weight *= (1.0 / Math.sqrt(industryCount));
            
            // 经营状态权重
            if ("正常".equals(supplier.getStatus())) {
                weight *= 1.2;
            } else if ("停业".equals(supplier.getStatus())) {
                weight *= 0.5;
            }
            
            // 注册资金权重（如果有的话）
            if (supplier.getRegisteredCapital() != null) {
                try {
                    double capital = supplier.getRegisteredCapital().doubleValue();
                    if (capital > 10000000) { // 1000万以上
                        weight *= 1.1;
                    } else if (capital > 1000000) { // 100万以上
                        weight *= 1.05;
                    }
                } catch (Exception e) {
                    // 忽略资金解析错误
                }
            }
            
            weights.put(supplier, weight);
        }
        
        return weights;
    }
}
    