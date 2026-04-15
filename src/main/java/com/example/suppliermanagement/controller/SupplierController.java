package com.example.suppliermanagement.controller;

import com.example.suppliermanagement.dto.*;
import com.example.suppliermanagement.model.Supplier;
import com.example.suppliermanagement.service.SupplierService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/suppliers")
@Tag(name = "供应商管理")
public class SupplierController {

    @Autowired
    private SupplierService supplierService;

    @PostMapping
    @Operation(summary = "创建供应商")
    public ResponseEntity<ApiResponse<Supplier>> createSupplier(
            @Valid @RequestBody Supplier supplier) {
        Supplier created = supplierService.createSupplier(supplier);
        return ResponseEntity.ok(ApiResponse.success("供应商创建成功", created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新供应商")
    public ResponseEntity<ApiResponse<Supplier>> updateSupplier(
            @PathVariable Long id,
            @Valid @RequestBody Supplier supplier) {
        Supplier updated = supplierService.updateSupplier(id, supplier);
        return ResponseEntity.ok(ApiResponse.success("供应商更新成功", updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除供应商")
    public ResponseEntity<ApiResponse<Void>> deleteSupplier(@PathVariable Long id) {
        supplierService.deleteSupplier(id);
        return ResponseEntity.ok(ApiResponse.success("供应商删除成功", null));
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取供应商")
    public ResponseEntity<ApiResponse<Supplier>> getSupplierById(@PathVariable Long id) {
        Supplier supplier = supplierService.getSupplierById(id);
        return ResponseEntity.ok(ApiResponse.success(supplier));
    }

    @GetMapping("/credit-code/{creditCode}")
    @Operation(summary = "根据统一社会信用代码获取供应商")
    public ResponseEntity<ApiResponse<Supplier>> getSupplierByCreditCode(
            @PathVariable String creditCode) {
        Supplier supplier = supplierService.getSupplierByCreditCode(creditCode);
        return ResponseEntity.ok(ApiResponse.success(supplier));
    }

    @GetMapping
    @Operation(summary = "分页查询供应商")
    public ResponseEntity<ApiResponse<Page<Supplier>>> getSuppliers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        Page<Supplier> suppliers = supplierService.getSuppliers(page, size, sortBy, sortDirection);
        return ResponseEntity.ok(ApiResponse.success(suppliers));
    }

    @PostMapping("/search")
    @Operation(summary = "高级搜索供应商")
    public ResponseEntity<ApiResponse<PageResponse<Supplier>>> searchSuppliers(
            @RequestBody SupplierSearchDTO searchDTO) {
        PageResponse<Supplier> result = supplierService.searchSuppliers(searchDTO);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/import")
    @Operation(summary = "导入供应商数据")
    public ResponseEntity<ApiResponse<List<Supplier>>> importSuppliers(
            @RequestParam("file") MultipartFile file) {
        List<Supplier> suppliers = supplierService.importSuppliers(file);
        return ResponseEntity.ok(ApiResponse.success("导入成功", suppliers));
    }

    @PostMapping("/export")
    @Operation(summary = "导出供应商数据")
    public ResponseEntity<byte[]> exportSuppliers(@RequestBody List<Supplier> suppliers) {
        byte[] data = supplierService.exportSuppliers(suppliers);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "suppliers.xlsx");
        
        return new ResponseEntity<>(data, headers, HttpStatus.OK);
    }

    @GetMapping("/export")
    @Operation(summary = "导出供应商数据（GET方式）")
    public ResponseEntity<byte[]> exportSuppliersGet(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String qualification,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String industry,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String scale) {
        
        // 构建搜索条件
        SupplierSearchDTO searchDTO = new SupplierSearchDTO();
        searchDTO.setPage(0);
        searchDTO.setSize(10000); // 导出所有符合条件的记录
        
        if (name != null) searchDTO.setName(name);
        if (qualification != null) searchDTO.setQualification(qualification);
        if (region != null) searchDTO.setRegion(region);
        if (industry != null) searchDTO.setIndustry(industry);
        if (status != null) searchDTO.setStatus(status);
        if (scale != null) searchDTO.setName(scale);
        
        // 搜索符合条件的供应商
        PageResponse<Supplier> result = supplierService.searchSuppliers(searchDTO);
        List<Supplier> suppliers = result.getContent();
        
        // 导出数据
        byte[] data = supplierService.exportSuppliers(suppliers);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", 
            String.format("供应商数据_%s.xlsx", java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ISO_DATE)));
        
        return new ResponseEntity<>(data, headers, HttpStatus.OK);
    }

    @PostMapping("/random-select")
    @Operation(summary = "随机抽取供应商")
    public ResponseEntity<ApiResponse<SelectionResultDTO>> randomSelectSuppliers(
            @RequestBody SupplierSelectionDTO selectionDTO,
            HttpServletRequest request) {
        // 设置IP地址
        selectionDTO.setIpAddress(getClientIpAddress(request));
        SelectionResultDTO result = supplierService.randomSelectSuppliers(selectionDTO);
        return ResponseEntity.ok(ApiResponse.success("随机抽取成功", result));
    }

    @PostMapping("/graded-select")
    @Operation(summary = "分级抽取供应商")
    public ResponseEntity<ApiResponse<SelectionResultDTO>> gradedSelectSuppliers(
            @RequestBody SupplierSelectionDTO selectionDTO,
            HttpServletRequest request) {
        // 设置IP地址
        selectionDTO.setIpAddress(getClientIpAddress(request));
        SelectionResultDTO result = supplierService.gradedSelectSuppliers(selectionDTO);
        return ResponseEntity.ok(ApiResponse.success("分级抽取成功", result));
    }
    
    @PostMapping("/retry-selection/{resultId}")
    @Operation(summary = "重新抽取供应商")
    public ResponseEntity<ApiResponse<SelectionResultDTO>> retrySelection(
            @PathVariable Long resultId,
            @RequestParam String reason,
            @RequestParam String operator,
            HttpServletRequest request) {
        
        SelectionResultDTO result = supplierService.retrySelection(resultId, reason, operator);
        return ResponseEntity.ok(ApiResponse.success("重新抽取成功", result));
    }

    @GetMapping("/all")
    @Operation(summary = "获取所有供应商（用于抽取）")
    public ResponseEntity<ApiResponse<List<Supplier>>> getAllSuppliers() {
        List<Supplier> suppliers = supplierService.getAllSuppliers();
        return ResponseEntity.ok(ApiResponse.success(suppliers));
    }

    @GetMapping("/qualifications")
    @Operation(summary = "获取所有资质等级")
    public ResponseEntity<ApiResponse<List<String>>> getAllQualifications() {
        List<String> qualifications = supplierService.getAllQualifications();
        return ResponseEntity.ok(ApiResponse.success(qualifications));
    }

    @GetMapping("/regions")
    @Operation(summary = "获取所有地区")
    public ResponseEntity<ApiResponse<List<String>>> getAllRegions() {
        List<String> regions = supplierService.getAllRegions();
        return ResponseEntity.ok(ApiResponse.success(regions));
    }



    @GetMapping("/statuses")
    @Operation(summary = "获取所有经营状态")
    public ResponseEntity<ApiResponse<List<String>>> getAllStatuses() {
        List<String> statuses = supplierService.getAllStatuses();
        return ResponseEntity.ok(ApiResponse.success(statuses));
    }



    /**
     * 获取客户端IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0];
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}
    