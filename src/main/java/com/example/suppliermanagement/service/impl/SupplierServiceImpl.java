package com.example.suppliermanagement.service.impl;

import com.example.suppliermanagement.config.CacheConfig;
import com.example.suppliermanagement.dto.PageResponse;
import com.example.suppliermanagement.dto.SelectionResultDTO;
import com.example.suppliermanagement.dto.SupplierDTO;
import com.example.suppliermanagement.dto.SupplierListDTO;
import com.example.suppliermanagement.dto.SupplierSearchDTO;
import com.example.suppliermanagement.dto.SupplierSelectionDTO;
import com.example.suppliermanagement.model.OperationLog;
import com.example.suppliermanagement.model.SelectionResult;
import com.example.suppliermanagement.model.Supplier;
import com.example.suppliermanagement.repository.OperationLogRepository;
import com.example.suppliermanagement.repository.SelectionResultRepository;
import com.example.suppliermanagement.repository.SupplierRepository;
import com.example.suppliermanagement.service.SupplierService;
import com.example.suppliermanagement.util.ExcelUtil;
import com.example.suppliermanagement.util.RequestContextUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.criteria.Predicate;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class SupplierServiceImpl implements SupplierService {

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private SelectionResultRepository selectionResultRepository;

    @Autowired
    private OperationLogRepository operationLogRepository;

    @Autowired
    private ExcelUtil excelUtil;

    @Autowired
    private ObjectMapper objectMapper;

    // 允许排序的字段白名单
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id", "name", "creditCode", "qualification", "region", "industry",
            "status", "scale", "contactPerson", "contactPhone",
            "establishDate", "registeredCapital", "certificationDate", "expiryDate",
            "createdAt", "updatedAt"
    );

    @Override
    @CacheEvict(value = {
            CacheConfig.QUALIFICATIONS_CACHE,
            CacheConfig.REGIONS_CACHE,
            CacheConfig.STATUSES_CACHE
    }, allEntries = true)
    public Supplier createSupplier(Supplier supplier) {
        // 检查统一社会信用代码是否已存在
        if (supplierRepository.findByCreditCode(supplier.getCreditCode()).isPresent()) {
            throw new RuntimeException("统一社会信用代码已存在");
        }
        // 规范化资质等级（将“A级/Β级/...”映射为“A/B/...”) 保存前统一
        if (supplier.getQualification() != null) {
            supplier.setQualification(mapQualification(supplier.getQualification()));
        }

        Supplier savedSupplier = supplierRepository.save(supplier);
        
        // 记录操作日志
        logOperation("创建供应商", savedSupplier.getName(), "CREATE");
        
        return savedSupplier;
    }

    @Override
    @CacheEvict(value = {
            CacheConfig.QUALIFICATIONS_CACHE,
            CacheConfig.REGIONS_CACHE,
            CacheConfig.STATUSES_CACHE
    }, allEntries = true)
    public Supplier updateSupplier(Long id, Supplier supplier) {
        Supplier existingSupplier = getSupplierById(id);
        
        // 检查统一社会信用代码是否被其他供应商使用
        if (!existingSupplier.getCreditCode().equals(supplier.getCreditCode())) {
            if (supplierRepository.findByCreditCode(supplier.getCreditCode()).isPresent()) {
                throw new RuntimeException("统一社会信用代码已存在");
            }
        }
        
        // 更新字段
        existingSupplier.setName(supplier.getName());
        // 规范化资质等级（将“A级/Β级/...”映射为“A/B/...”) 保存前统一
        existingSupplier.setQualification(
                supplier.getQualification() != null ? mapQualification(supplier.getQualification()) : null);
        existingSupplier.setRegion(supplier.getRegion());
        existingSupplier.setIndustry(supplier.getIndustry());
        existingSupplier.setAddress(supplier.getAddress());
        existingSupplier.setContactPerson(supplier.getContactPerson());
        existingSupplier.setContactPhone(supplier.getContactPhone());
        existingSupplier.setContactEmail(supplier.getContactEmail());
        existingSupplier.setBusinessScope(supplier.getBusinessScope());
        existingSupplier.setPerformance(supplier.getPerformance());
        existingSupplier.setEstablishDate(supplier.getEstablishDate());
        existingSupplier.setLegalPerson(supplier.getLegalPerson());
        existingSupplier.setRegisteredCapital(supplier.getRegisteredCapital());
        existingSupplier.setStatus(supplier.getStatus());
        existingSupplier.setScale(supplier.getScale());
        existingSupplier.setQualificationMaterials(supplier.getQualificationMaterials());
        existingSupplier.setCertificationDate(supplier.getCertificationDate());
        existingSupplier.setExpiryDate(supplier.getExpiryDate());
        existingSupplier.setRemark(supplier.getRemark());
        
        Supplier updatedSupplier = supplierRepository.save(existingSupplier);
        
        // 记录操作日志
        logOperation("更新供应商", updatedSupplier.getName(), "UPDATE");
        
        return updatedSupplier;
    }

    @Override
    @CacheEvict(value = {
            CacheConfig.QUALIFICATIONS_CACHE,
            CacheConfig.REGIONS_CACHE,
            CacheConfig.STATUSES_CACHE
    }, allEntries = true)
    public void deleteSupplier(Long id) {
        Supplier supplier = getSupplierById(id);
        supplierRepository.deleteById(id);
        
        // 记录操作日志
        logOperation("删除供应商", supplier.getName(), "DELETE");
    }

    @Override
    public Supplier getSupplierById(Long id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("供应商不存在"));
    }

    @Override
    public Supplier getSupplierByCreditCode(String creditCode) {
        return supplierRepository.findByCreditCode(creditCode)
                .orElseThrow(() -> new RuntimeException("供应商不存在"));
    }

    @Override
    public Page<Supplier> getSuppliers(int page, int size, String sortBy, String sortDirection) {
        // 校验 sortBy 字段是否在白名单中
        if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
            throw new IllegalArgumentException("不允许的排序字段: " + sortBy);
        }
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        return supplierRepository.findAll(pageable);
    }

    @Override
    public PageResponse<Supplier> searchSuppliers(SupplierSearchDTO searchDTO) {
        // 校验 sortBy 字段是否在白名单中
        if (!ALLOWED_SORT_FIELDS.contains(searchDTO.getSortBy())) {
            throw new IllegalArgumentException("不允许的排序字段: " + searchDTO.getSortBy());
        }
        Specification<Supplier> spec = createSearchSpecification(searchDTO);
        
        Sort sort = Sort.by(Sort.Direction.fromString(searchDTO.getSortDirection()), searchDTO.getSortBy());
        Pageable pageable = PageRequest.of(searchDTO.getPage(), searchDTO.getSize(), sort);
        
        Page<Supplier> page = supplierRepository.findAll(spec, pageable);
        return PageResponse.from(page);
    }

    @Override
    public List<Supplier> importSuppliers(MultipartFile file) {
        try {
            List<Supplier> suppliers = excelUtil.importSuppliers(file);
            
            // 批量保存供应商
            List<Supplier> savedSuppliers = supplierRepository.saveAll(suppliers);
            
            // 记录操作日志
            logOperation("批量导入供应商", "导入" + savedSuppliers.size() + "家供应商", "IMPORT");
            
            return savedSuppliers;
        } catch (Exception e) {
            throw new RuntimeException("导入失败: " + e.getMessage());
        }
    }

    @Override
    public byte[] exportSuppliers(List<Supplier> suppliers) {
        try {
            return excelUtil.exportSuppliers(suppliers);
        } catch (Exception e) {
            throw new RuntimeException("导出失败: " + e.getMessage());
        }
    }

    @Override
    public SelectionResultDTO randomSelectSuppliers(SupplierSelectionDTO selectionDTO) {
        // 构建查询条件
        Specification<Supplier> spec = createSelectionSpecification(selectionDTO.getConditions());
        
        // 获取符合条件的供应商总数
        long totalCount = supplierRepository.count(spec);
        if (totalCount < selectionDTO.getTotalCount()) {
            throw new RuntimeException("符合条件的供应商数量不足，需要" + selectionDTO.getTotalCount() + "家，但只有" + totalCount + "家");
        }
        
        // 随机抽取
        List<Supplier> selectedSuppliers = supplierRepository.findAll(spec);
        Collections.shuffle(selectedSuppliers);
        selectedSuppliers = selectedSuppliers.subList(0, Math.min(selectionDTO.getTotalCount(), selectedSuppliers.size()));
        
        // 保存抽取结果
        SelectionResult result = saveSelectionResult(selectionDTO, selectedSuppliers, "random");
        
        // 记录操作日志
        logOperation("随机抽取供应商", "抽取" + selectedSuppliers.size() + "家供应商", "SELECT");
        
        return convertToSelectionResultDTO(result, selectedSuppliers);
    }

    @Override
    public SelectionResultDTO gradedSelectSuppliers(SupplierSelectionDTO selectionDTO) {
        try {
            List<Supplier> allSelectedSuppliers = new ArrayList<>();
            int totalRequested = 0;
            
            // 验证分级规则
            if (selectionDTO.getRules() == null || selectionDTO.getRules().isEmpty()) {
                throw new RuntimeException("分级抽取规则不能为空");
            }
            
            // 根据分级规则抽取
            for (SupplierSelectionDTO.GradedSelectionRuleDTO rule : selectionDTO.getRules()) {
                if (rule == null || rule.getQualification() == null || rule.getCount() == null) {
                    throw new RuntimeException("分级抽取规则参数不完整");
                }
                
                totalRequested += rule.getCount();
                Specification<Supplier> spec = createGradedSelectionSpecification(rule);
                
                List<Supplier> qualifiedSuppliers = supplierRepository.findAll(spec);
                if (qualifiedSuppliers.size() < rule.getCount()) {
                    throw new RuntimeException(rule.getQualification() + "级供应商数量不足，需要" + rule.getCount() + "家，但只有" + qualifiedSuppliers.size() + "家");
                }
                
                // 随机抽取指定数量
                Collections.shuffle(qualifiedSuppliers);
                List<Supplier> selected = qualifiedSuppliers.subList(0, rule.getCount());
                allSelectedSuppliers.addAll(selected);
            }
            
            // 保存抽取结果
            SelectionResult result = saveSelectionResult(selectionDTO, allSelectedSuppliers, "graded");
            
            // 记录操作日志
            logOperation("分级抽取供应商", "成功抽取" + allSelectedSuppliers.size() + "家供应商，总计" + totalRequested + "家", "GRADED_SELECTION");
            
            return convertToSelectionResultDTO(result, allSelectedSuppliers);
            
        } catch (Exception e) {
            // 记录错误并返回错误信息
            logOperation("分级抽取失败", "错误: " + e.getMessage(), "ERROR");
            throw new RuntimeException("分级抽取失败: " + e.getMessage());
        }
    }

    @Override
    public SelectionResultDTO retrySelection(Long resultId, String reason, String operator) {
        try {
            SelectionResult originalResult = selectionResultRepository.findById(resultId)
                    .orElseThrow(() -> new RuntimeException("抽取结果不存在"));
            
            // 增加重试次数
            originalResult.setRetryCount(originalResult.getRetryCount() + 1);
            
            // 记录重试原因
            try {
                List<String> reasons = new ArrayList<>();
                if (StringUtils.hasText(originalResult.getReasons())) {
                    reasons = objectMapper.readValue(originalResult.getReasons(), List.class);
                }
                reasons.add(reason);
                originalResult.setReasons(objectMapper.writeValueAsString(reasons));
            } catch (JsonProcessingException e) {
                // 如果解析失败，使用简单字符串
                originalResult.setReasons("[\"" + reason + "\"]");
            }
            
            // 根据原始抽取类型重新执行抽取逻辑
            List<Supplier> newSelectedSuppliers = null;
            if ("random".equals(originalResult.getSelectionType())) {
                // 重新执行随机抽取
                newSelectedSuppliers = performRandomSelectionRetry(originalResult);
            } else if ("graded".equals(originalResult.getSelectionType())) {
                // 重新执行分级抽取
                newSelectedSuppliers = performGradedSelectionRetry(originalResult);
            }
            
            // 更新抽取结果
            if (newSelectedSuppliers != null) {
                originalResult.setResults(objectMapper.writeValueAsString(newSelectedSuppliers));
                originalResult.setTotalCount(newSelectedSuppliers.size());
            }
            
            // 保存更新
            selectionResultRepository.save(originalResult);
            
            // 记录操作日志
            logOperation("重新抽取供应商", "重试原因: " + reason + ", 新抽取数量: " + (newSelectedSuppliers != null ? newSelectedSuppliers.size() : 0), "RETRY");
            
            return convertToSelectionResultDTO(originalResult, newSelectedSuppliers);
            
        } catch (Exception e) {
            // 记录错误并返回错误信息
            logOperation("重新抽取失败", "错误: " + e.getMessage(), "ERROR");
            throw new RuntimeException("重新抽取失败: " + e.getMessage());
        }
    }
    
    /**
     * 重新执行随机抽取
     */
    private List<Supplier> performRandomSelectionRetry(SelectionResult originalResult) {
        try {
            // 解析原始条件
            Map<String, Object> originalConditions = objectMapper.readValue(originalResult.getConditions(), Map.class);
            
            // 创建新的DTO
            SupplierSelectionDTO retryDTO = new SupplierSelectionDTO();
            retryDTO.setSelectionType("random");
            retryDTO.setTotalCount(originalResult.getTotalCount());
            retryDTO.setConditions(originalConditions);
            retryDTO.setOperator(originalResult.getOperator());
            
            // 执行随机抽取
            return performRandomSelection(retryDTO);
            
        } catch (Exception e) {
            logOperation("重新随机抽取失败", "错误: " + e.getMessage(), "ERROR");
            throw new RuntimeException("重新随机抽取失败: " + e.getMessage());
        }
    }
    
    /**
     * 重新执行分级抽取
     */
    private List<Supplier> performGradedSelectionRetry(SelectionResult originalResult) {
        try {
            // 解析原始条件
            Map<String, Object> originalConditions = objectMapper.readValue(originalResult.getConditions(), Map.class);
            
            // 创建新的DTO
            SupplierSelectionDTO retryDTO = new SupplierSelectionDTO();
            retryDTO.setSelectionType("graded");
            retryDTO.setRules((List<SupplierSelectionDTO.GradedSelectionRuleDTO>) originalConditions.get("rules"));
            retryDTO.setOperator(originalResult.getOperator());
            
            // 执行分级抽取
            return performGradedSelection(retryDTO);
            
        } catch (Exception e) {
            logOperation("重新分级抽取失败", "错误: " + e.getMessage(), "ERROR");
            throw new RuntimeException("重新分级抽取失败: " + e.getMessage());
        }
    }
    
    // 私有辅助方法：执行分级抽取
    private List<Supplier> performGradedSelection(SupplierSelectionDTO selectionDTO) {
        List<Supplier> allSelectedSuppliers = new ArrayList<>();
        
        // 验证分级规则
        if (selectionDTO.getRules() == null || selectionDTO.getRules().isEmpty()) {
            throw new RuntimeException("分级抽取规则不能为空");
        }
        
        // 根据分级规则抽取
        for (SupplierSelectionDTO.GradedSelectionRuleDTO rule : selectionDTO.getRules()) {
            if (rule == null || rule.getQualification() == null || rule.getCount() == null) {
                throw new RuntimeException("分级抽取规则参数不完整");
            }
            
            Specification<Supplier> spec = createGradedSelectionSpecification(rule);
            
            List<Supplier> qualifiedSuppliers = supplierRepository.findAll(spec);
            if (qualifiedSuppliers.size() < rule.getCount()) {
                throw new RuntimeException(rule.getQualification() + "级供应商数量不足，需要" + rule.getCount() + "家，但只有" + qualifiedSuppliers.size() + "家");
            }
            
            // 随机抽取指定数量
            Collections.shuffle(qualifiedSuppliers);
            List<Supplier> selected = qualifiedSuppliers.subList(0, rule.getCount());
            allSelectedSuppliers.addAll(selected);
        }
        
        return allSelectedSuppliers;
    }
    
    // 私有辅助方法：执行随机抽取
    private List<Supplier> performRandomSelection(SupplierSelectionDTO selectionDTO) {
        // 构建查询条件
        Specification<Supplier> spec = createSelectionSpecification(selectionDTO.getConditions());
        
        // 获取符合条件的供应商总数
        long totalCount = supplierRepository.count(spec);
        if (totalCount < selectionDTO.getTotalCount()) {
            throw new RuntimeException("符合条件的供应商数量不足，需要" + selectionDTO.getTotalCount() + "家，但只有" + totalCount + "家");
        }
        
        // 随机抽取
        List<Supplier> selectedSuppliers = supplierRepository.findAll(spec);
        Collections.shuffle(selectedSuppliers);
        selectedSuppliers = selectedSuppliers.subList(0, Math.min(selectionDTO.getTotalCount(), selectedSuppliers.size()));
        
        return selectedSuppliers;
    }

    @Override
    @Cacheable(value = CacheConfig.QUALIFICATIONS_CACHE)
    public List<String> getAllQualifications() {
        return supplierRepository.findDistinctQualifications();
    }

    @Override
    @Cacheable(value = CacheConfig.REGIONS_CACHE)
    public List<String> getAllRegions() {
        return supplierRepository.findDistinctRegions();
    }



    @Override
    @Cacheable(value = CacheConfig.STATUSES_CACHE)
    public List<String> getAllStatuses() {
        return supplierRepository.findDistinctStatuses();
    }



    @Override
    public List<Supplier> getAllSuppliers() {
        return supplierRepository.findAll();
    }

    // 私有辅助方法
    private Specification<Supplier> createSearchSpecification(SupplierSearchDTO searchDTO) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (StringUtils.hasText(searchDTO.getName())) {
                predicates.add(criteriaBuilder.like(root.get("name"), "%" + searchDTO.getName() + "%"));
            }
            
            if (StringUtils.hasText(searchDTO.getCreditCode())) {
                predicates.add(criteriaBuilder.equal(root.get("creditCode"), searchDTO.getCreditCode()));
            }
            
            if (StringUtils.hasText(searchDTO.getQualification())) {
                String normalizedQualification = mapQualification(searchDTO.getQualification());
                predicates.add(criteriaBuilder.equal(root.get("qualification"), normalizedQualification));
            }
            
            // 修改地区搜索为模糊匹配
            if (StringUtils.hasText(searchDTO.getRegion())) {
                predicates.add(criteriaBuilder.like(root.get("region"), "%" + searchDTO.getRegion() + "%"));
            }
            
            // 添加经营状态字段处理
            if (StringUtils.hasText(searchDTO.getStatus())) {
                predicates.add(criteriaBuilder.equal(root.get("status"), searchDTO.getStatus()));
            }
            
            if (StringUtils.hasText(searchDTO.getBusinessScope())) {
                predicates.add(criteriaBuilder.like(root.get("businessScope"), "%" + searchDTO.getBusinessScope() + "%"));
            }
            
            if (StringUtils.hasText(searchDTO.getPerformance())) {
                predicates.add(criteriaBuilder.like(root.get("performance"), "%" + searchDTO.getPerformance() + "%"));
            }
            
            if (searchDTO.getEstablishDateFrom() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("establishDate"), searchDTO.getEstablishDateFrom()));
            }
            
            if (searchDTO.getEstablishDateTo() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("establishDate"), searchDTO.getEstablishDateTo()));
            }
            
            if (StringUtils.hasText(searchDTO.getLegalPerson())) {
                predicates.add(criteriaBuilder.like(root.get("legalPerson"), "%" + searchDTO.getLegalPerson() + "%"));
            }
            
            if (searchDTO.getRegisteredCapitalFrom() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("registeredCapital"), searchDTO.getRegisteredCapitalFrom()));
            }
            
            if (searchDTO.getRegisteredCapitalTo() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("registeredCapital"), searchDTO.getRegisteredCapitalTo()));
            }
            
            if (StringUtils.hasText(searchDTO.getStatus())) {
                predicates.add(criteriaBuilder.equal(root.get("status"), searchDTO.getStatus()));
            }
            
            if (searchDTO.getCertificationDateFrom() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("certificationDate"), searchDTO.getCertificationDateFrom()));
            }
            
            if (searchDTO.getCertificationDateTo() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("certificationDate"), searchDTO.getCertificationDateTo()));
            }
            
            if (searchDTO.getExpiryDateFrom() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("expiryDate"), searchDTO.getExpiryDateFrom()));
            }
            
            if (searchDTO.getExpiryDateTo() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("expiryDate"), searchDTO.getExpiryDateTo()));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Specification<Supplier> createSelectionSpecification(Object conditions) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // 只选择状态正常的供应商
            predicates.add(criteriaBuilder.equal(root.get("status"), "正常"));
            
            // 如果有其他条件，可以在这里添加
            if (conditions != null) {
                try {
                    Map<String, Object> conditionMap;
                    
                    // 如果conditions是字符串，尝试解析JSON
                    if (conditions instanceof String) {
                        String conditionsStr = (String) conditions;
                        if (StringUtils.hasText(conditionsStr)) {
                            conditionMap = objectMapper.readValue(conditionsStr, Map.class);
                        } else {
                            conditionMap = new HashMap<>();
                        }
                    } else if (conditions instanceof Map) {
                        // 如果conditions已经是Map，直接使用
                        conditionMap = (Map<String, Object>) conditions;
                    } else {
                        // 其他类型，转换为Map
                        conditionMap = objectMapper.convertValue(conditions, Map.class);
                    }
                    
                    // 资质等级条件 - 处理中文到英文的映射
                    Object qualificationObj = conditionMap.get("qualification");
                    if (qualificationObj != null && StringUtils.hasText(qualificationObj.toString())) {
                        String qualification = qualificationObj.toString();
                        String mappedQualification = mapQualification(qualification);
                        predicates.add(criteriaBuilder.equal(root.get("qualification"), mappedQualification));
                    }
                    
                    // 行业条件
                    Object industryObj = conditionMap.get("industry");
                    if (industryObj != null && StringUtils.hasText(industryObj.toString())) {
                        String industry = industryObj.toString();
                        predicates.add(criteriaBuilder.equal(root.get("industry"), industry));
                    }
                } catch (Exception e) {
                    // 如果条件解析失败，忽略条件
                    logOperation("条件解析失败", "条件解析错误: " + e.getMessage(), "WARNING");
                }
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * 映射中文资质等级到英文
     */
    private String mapQualification(String qualification) {
        if ("A级".equals(qualification)) {
            return "A";
        } else if ("B级".equals(qualification)) {
            return "B";
        } else if ("C级".equals(qualification)) {
            return "C";
        }
        return qualification; // 如果不是中文格式，直接返回原值
    }
    
    private Specification<Supplier> createGradedSelectionSpecification(SupplierSelectionDTO.GradedSelectionRuleDTO rule) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // 只选择状态正常的供应商
            predicates.add(criteriaBuilder.equal(root.get("status"), "正常"));
            
            // 资质等级条件 - 使用统一的映射方法
            String qualification = mapQualification(rule.getQualification());
            predicates.add(criteriaBuilder.equal(root.get("qualification"), qualification));
            
            // 行业条件
            if (StringUtils.hasText(rule.getIndustry())) {
                predicates.add(criteriaBuilder.equal(root.get("industry"), rule.getIndustry()));
            }
            
            // 地区条件
            if (StringUtils.hasText(rule.getRegion())) {
                predicates.add(criteriaBuilder.equal(root.get("region"), rule.getRegion()));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private SelectionResult saveSelectionResult(SupplierSelectionDTO selectionDTO, List<Supplier> suppliers, String type) {
        SelectionResult result = new SelectionResult();
        result.setSelectionType(type);
        result.setTotalCount(suppliers.size());
        result.setOperator(selectionDTO.getOperator());
        result.setIpAddress(selectionDTO.getIpAddress());
        result.setRetryCount(0);
        
        try {
            result.setConditions(objectMapper.writeValueAsString(selectionDTO));
            result.setResults(objectMapper.writeValueAsString(suppliers));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("保存抽取结果失败");
        }
        
        return selectionResultRepository.save(result);
    }

    private SelectionResultDTO convertToSelectionResultDTO(SelectionResult result, List<Supplier> suppliers) {
        SelectionResultDTO dto = new SelectionResultDTO();
        dto.setId(result.getId());
        dto.setSelectionType(result.getSelectionType());
        dto.setTotalCount(result.getTotalCount());
        dto.setConditions(result.getConditions());
        dto.setOperator(result.getOperator());
        dto.setRetryCount(result.getRetryCount());
        dto.setReasons(result.getReasons());
        dto.setCreatedAt(result.getCreatedAt());
        dto.setIpAddress(result.getIpAddress());
        
        if (suppliers != null) {
            dto.setResults(suppliers.stream().map(this::convertToSupplierInfo).collect(Collectors.toList()));
        }
        
        return dto;
    }

    private SelectionResultDTO.SupplierInfo convertToSupplierInfo(Supplier supplier) {
        SelectionResultDTO.SupplierInfo info = new SelectionResultDTO.SupplierInfo();
        info.setId(supplier.getId());
        info.setName(supplier.getName());
        info.setCreditCode(supplier.getCreditCode());
        info.setQualification(supplier.getQualification());
        info.setRegion(supplier.getRegion());
        info.setIndustry(supplier.getIndustry());
        info.setAddress(supplier.getAddress());
        info.setContactPerson(supplier.getContactPerson());
        info.setContactPhone(supplier.getContactPhone());
        info.setBusinessScope(supplier.getBusinessScope());
        info.setPerformance(supplier.getPerformance());
        info.setStatus(supplier.getStatus());
        info.setScale(supplier.getScale());
        return info;
    }

    private void logOperation(String action, String details, String type) {
        OperationLog log = new OperationLog();
        log.setOperationType(type);
        log.setContent(details);
        // 从请求上下文获取真实的用户名
        String username = RequestContextUtil.getCurrentUsername();
        log.setOperator(username != null ? username : "system");
        // 从请求上下文获取真实的 IP 地址
        String ipAddress = RequestContextUtil.getClientIpAddress();
        log.setIpAddress(ipAddress != null ? ipAddress : "unknown");
        operationLogRepository.save(log);
    }

    // ========== DTO 转换方法 ==========

    /**
     * 将 Supplier Entity 转换为 SupplierDTO
     */
    public SupplierDTO convertToDTO(Supplier supplier) {
        if (supplier == null) {
            return null;
        }
        SupplierDTO dto = new SupplierDTO();
        dto.setId(supplier.getId());
        dto.setName(supplier.getName());
        dto.setCreditCode(supplier.getCreditCode());
        dto.setQualification(supplier.getQualification());
        dto.setRegion(supplier.getRegion());
        dto.setIndustry(supplier.getIndustry());
        dto.setAddress(supplier.getAddress());
        dto.setContactPerson(supplier.getContactPerson());
        dto.setContactPhone(supplier.getContactPhone());
        dto.setContactEmail(supplier.getContactEmail());
        dto.setBusinessScope(supplier.getBusinessScope());
        dto.setPerformance(supplier.getPerformance());
        dto.setEstablishDate(supplier.getEstablishDate());
        dto.setLegalPerson(supplier.getLegalPerson());
        dto.setRegisteredCapital(supplier.getRegisteredCapital());
        dto.setStatus(supplier.getStatus());
        dto.setScale(supplier.getScale());
        dto.setQualificationMaterials(supplier.getQualificationMaterials());
        dto.setCertificationDate(supplier.getCertificationDate());
        dto.setExpiryDate(supplier.getExpiryDate());
        dto.setRemark(supplier.getRemark());
        dto.setCreatedAt(supplier.getCreatedAt());
        dto.setUpdatedAt(supplier.getUpdatedAt());
        return dto;
    }

    /**
     * 将 Supplier Entity 转换为 SupplierListDTO（简化版）
     */
    public SupplierListDTO convertToListDTO(Supplier supplier) {
        if (supplier == null) {
            return null;
        }
        SupplierListDTO dto = new SupplierListDTO();
        dto.setId(supplier.getId());
        dto.setName(supplier.getName());
        dto.setCreditCode(supplier.getCreditCode());
        dto.setQualification(supplier.getQualification());
        dto.setRegion(supplier.getRegion());
        dto.setIndustry(supplier.getIndustry());
        dto.setStatus(supplier.getStatus());
        dto.setContactPerson(supplier.getContactPerson());
        dto.setContactPhone(supplier.getContactPhone());
        dto.setScale(supplier.getScale());
        dto.setCertificationDate(supplier.getCertificationDate());
        dto.setExpiryDate(supplier.getExpiryDate());
        dto.setCreatedAt(supplier.getCreatedAt());
        dto.setUpdatedAt(supplier.getUpdatedAt());
        return dto;
    }

    @Override
    public List<Supplier> getEligibleForRandomSelection(String qualification, String industry, int count) {
        // 创建分页请求，获取指定数量的供应商
        Pageable pageable = PageRequest.of(0, count);
        
        // 调用repository方法获取符合条件的供应商
        Page<Supplier> supplierPage = supplierRepository.findEligibleForRandomSelection(
                qualification, industry, pageable);
        
        // 从分页结果中提取供应商列表
        List<Supplier> suppliers = supplierPage.getContent();
        
        // 如果获取的供应商数量不足，返回所有可用的
        if (suppliers.size() < count) {
            return suppliers;
        }
        
        // 如果获取的供应商数量超过要求，随机选择指定数量
        if (suppliers.size() > count) {
            Collections.shuffle(suppliers);
            return suppliers.subList(0, count);
        }
        
        return suppliers;
    }

    @Override
    public List<Supplier> getEligibleForGradedSelection(String qualification, String industry, Integer count) {
        // 创建分页请求，获取指定数量的供应商
        Pageable pageable = PageRequest.of(0, count != null ? count : Integer.MAX_VALUE);
        
        // 调用repository方法获取符合条件的供应商
        Page<Supplier> supplierPage = supplierRepository.findEligibleForGradedSelection(
                qualification, industry, pageable);
        
        // 从分页结果中提取供应商列表
        List<Supplier> suppliers = supplierPage.getContent();
        
        // 如果指定了数量且获取的供应商数量超过要求，随机选择指定数量
        if (count != null && suppliers.size() > count) {
            Collections.shuffle(suppliers);
            return suppliers.subList(0, count);
        }
        
        return suppliers;
    }
}
    