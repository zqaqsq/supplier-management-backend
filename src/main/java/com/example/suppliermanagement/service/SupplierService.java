package com.example.suppliermanagement.service;

import com.example.suppliermanagement.dto.PageResponse;
import com.example.suppliermanagement.dto.SelectionResultDTO;
import com.example.suppliermanagement.dto.SupplierSearchDTO;
import com.example.suppliermanagement.dto.SupplierSelectionDTO;
import com.example.suppliermanagement.model.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface SupplierService {

    /**
     * 创建供应商
     */
    Supplier createSupplier(Supplier supplier);

    /**
     * 更新供应商
     */
    Supplier updateSupplier(Long id, Supplier supplier);

    /**
     * 删除供应商
     */
    void deleteSupplier(Long id);

    /**
     * 根据ID获取供应商
     */
    Supplier getSupplierById(Long id);

    /**
     * 根据统一社会信用代码获取供应商
     */
    Supplier getSupplierByCreditCode(String creditCode);

    /**
     * 分页查询供应商
     */
    Page<Supplier> getSuppliers(int page, int size, String sortBy, String sortDirection);

    /**
     * 高级搜索供应商
     */
    PageResponse<Supplier> searchSuppliers(SupplierSearchDTO searchDTO);

    /**
     * 导入供应商数据
     */
    List<Supplier> importSuppliers(MultipartFile file);

    /**
     * 导出供应商数据
     */
    byte[] exportSuppliers(List<Supplier> suppliers);

    /**
     * 随机抽取供应商
     */
    SelectionResultDTO randomSelectSuppliers(SupplierSelectionDTO selectionDTO);

    /**
     * 分级抽取供应商
     */
    SelectionResultDTO gradedSelectSuppliers(SupplierSelectionDTO selectionDTO);

    /**
     * 重新抽取供应商
     */
    SelectionResultDTO retrySelection(Long resultId, String reason, String operator);

    /**
     * 获取所有资质等级
     */
    List<String> getAllQualifications();

    /**
     * 获取所有地区
     */
    List<String> getAllRegions();

    /**
     * 获取所有行业
     */


    /**
     * 获取所有经营状态
     */
    List<String> getAllStatuses();

    /**
     * 获取所有企业规模
     */


    /**
     * 获取所有供应商（用于抽取）
     */
    List<Supplier> getAllSuppliers();

    /**
     * 获取符合条件的供应商用于随机抽取
     */
    List<Supplier> getEligibleForRandomSelection(String qualification, String industry, int count);

    /**
     * 获取符合条件的供应商用于分级抽取
     */
    List<Supplier> getEligibleForGradedSelection(String qualification, String industry, Integer count);
}
    