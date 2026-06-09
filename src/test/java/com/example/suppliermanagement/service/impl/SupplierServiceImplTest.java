package com.example.suppliermanagement.service.impl;

import com.example.suppliermanagement.model.Supplier;
import com.example.suppliermanagement.repository.OperationLogRepository;
import com.example.suppliermanagement.repository.SelectionResultRepository;
import com.example.suppliermanagement.repository.SupplierRepository;
import com.example.suppliermanagement.util.ExcelUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * SupplierServiceImpl 单元测试
 * 测试供应商创建、更新、资质等级映射等核心功能
 */
@ExtendWith(MockitoExtension.class)
class SupplierServiceImplTest {

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private SelectionResultRepository selectionResultRepository;

    @Mock
    private OperationLogRepository operationLogRepository;

    @Mock
    private ExcelUtil excelUtil;

    private SupplierServiceImpl supplierService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        supplierService = new SupplierServiceImpl();

        // 通过反射注入依赖
        try {
            var supplierRepoField = SupplierServiceImpl.class.getDeclaredField("supplierRepository");
            supplierRepoField.setAccessible(true);
            supplierRepoField.set(supplierService, supplierRepository);

            var selectionRepoField = SupplierServiceImpl.class.getDeclaredField("selectionResultRepository");
            selectionRepoField.setAccessible(true);
            selectionRepoField.set(supplierService, selectionResultRepository);

            var logRepoField = SupplierServiceImpl.class.getDeclaredField("operationLogRepository");
            logRepoField.setAccessible(true);
            logRepoField.set(supplierService, operationLogRepository);

            var excelUtilField = SupplierServiceImpl.class.getDeclaredField("excelUtil");
            excelUtilField.setAccessible(true);
            excelUtilField.set(supplierService, excelUtil);

            var objectMapperField = SupplierServiceImpl.class.getDeclaredField("objectMapper");
            objectMapperField.setAccessible(true);
            objectMapperField.set(supplierService, objectMapper);
        } catch (Exception e) {
            fail("依赖注入失败: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("创建供应商：'A级' 应转换为 'A' 存储")
    void testCreateSupplier_QualificationMapping_A() {
        Supplier supplier = createSupplier("测试供应商A", "A级");
        supplier.setCreditCode("91110000MA01ABCD12");

        when(supplierRepository.findByCreditCode(anyString()))
                .thenReturn(Optional.empty());
        when(supplierRepository.save(any(Supplier.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(operationLogRepository.save(any()))
                .thenReturn(null);

        Supplier saved = supplierService.createSupplier(supplier);

        assertEquals("A", saved.getQualification(),
                "'A级' 应被转换为 'A' 存储");
        verify(supplierRepository).save(argThat(s ->
                "A".equals(s.getQualification())));
    }

    @Test
    @DisplayName("创建供应商：'B级' 应转换为 'B' 存储")
    void testCreateSupplier_QualificationMapping_B() {
        Supplier supplier = createSupplier("测试供应商B", "B级");
        supplier.setCreditCode("91110000MA02EFGH34");

        when(supplierRepository.findByCreditCode(anyString()))
                .thenReturn(Optional.empty());
        when(supplierRepository.save(any(Supplier.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(operationLogRepository.save(any()))
                .thenReturn(null);

        Supplier saved = supplierService.createSupplier(supplier);

        assertEquals("B", saved.getQualification(),
                "'B级' 应被转换为 'B' 存储");
    }

    @Test
    @DisplayName("创建供应商：'C级' 应转换为 'C' 存储")
    void testCreateSupplier_QualificationMapping_C() {
        Supplier supplier = createSupplier("测试供应商C", "C级");
        supplier.setCreditCode("91110000MA03IJKL56");

        when(supplierRepository.findByCreditCode(anyString()))
                .thenReturn(Optional.empty());
        when(supplierRepository.save(any(Supplier.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(operationLogRepository.save(any()))
                .thenReturn(null);

        Supplier saved = supplierService.createSupplier(supplier);

        assertEquals("C", saved.getQualification(),
                "'C级' 应被转换为 'C' 存储");
    }

    @Test
    @DisplayName("创建供应商：已是 'A' 格式应保持不变")
    void testCreateSupplier_QualificationMapping_AlreadyA() {
        Supplier supplier = createSupplier("测试供应商", "A");
        supplier.setCreditCode("91110000MA04MNOP78");

        when(supplierRepository.findByCreditCode(anyString()))
                .thenReturn(Optional.empty());
        when(supplierRepository.save(any(Supplier.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(operationLogRepository.save(any()))
                .thenReturn(null);

        Supplier saved = supplierService.createSupplier(supplier);

        assertEquals("A", saved.getQualification(),
                "'A' 格式应保持不变");
    }

    @Test
    @DisplayName("创建供应商：已是 'B' 格式应保持不变")
    void testCreateSupplier_QualificationMapping_AlreadyB() {
        Supplier supplier = createSupplier("测试供应商", "B");
        supplier.setCreditCode("91110000MA05MNOP90");

        when(supplierRepository.findByCreditCode(anyString()))
                .thenReturn(Optional.empty());
        when(supplierRepository.save(any(Supplier.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(operationLogRepository.save(any()))
                .thenReturn(null);

        Supplier saved = supplierService.createSupplier(supplier);

        assertEquals("B", saved.getQualification(),
                "'B' 格式应保持不变");
    }

    @Test
    @DisplayName("创建供应商：信用代码重复应抛出异常")
    void testCreateSupplier_DuplicateCreditCode() {
        Supplier supplier = createSupplier("测试供应商", "A级");
        supplier.setCreditCode("91110000MADUPLICATE11");

        when(supplierRepository.findByCreditCode("91110000MADUPLICATE11"))
                .thenReturn(Optional.of(new Supplier()));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> supplierService.createSupplier(supplier));

        assertEquals("统一社会信用代码已存在", exception.getMessage());
    }

    @Test
    @DisplayName("更新供应商：资质等级应正确映射")
    void testUpdateSupplier_QualificationMapping() {
        Supplier existingSupplier = createSupplier("原有供应商", "A");
        existingSupplier.setId(1L);
        existingSupplier.setCreditCode("91110000MA06QRST34");

        Supplier updateData = createSupplier("更新供应商", "B级");
        updateData.setCreditCode("91110000MA06QRST34");

        when(supplierRepository.findById(1L))
                .thenReturn(Optional.of(existingSupplier));
        when(supplierRepository.findByCreditCode(anyString()))
                .thenReturn(Optional.of(existingSupplier));
        when(supplierRepository.save(any(Supplier.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(operationLogRepository.save(any()))
                .thenReturn(null);

        Supplier updated = supplierService.updateSupplier(1L, updateData);

        assertEquals("B", updated.getQualification(),
                "'B级' 应被转换为 'B' 存储");
    }

    @Test
    @DisplayName("DTO 转换：Entity 转 DTO 应正确映射所有字段")
    void testConvertToDTO() {
        Supplier supplier = createSupplier("DTO测试供应商", "A");
        supplier.setId(100L);
        supplier.setAddress("北京市朝阳区");
        supplier.setContactPerson("张三");
        supplier.setContactPhone("13800138000");
        supplier.setBusinessScope("软件开发");
        supplier.setScale("大型");

        var dto = supplierService.convertToDTO(supplier);

        assertNotNull(dto);
        assertEquals(100L, dto.getId());
        assertEquals("DTO测试供应商", dto.getName());
        assertEquals("A", dto.getQualification());
        assertEquals("北京市朝阳区", dto.getAddress());
        assertEquals("张三", dto.getContactPerson());
        assertEquals("13800138000", dto.getContactPhone());
        assertEquals("软件开发", dto.getBusinessScope());
        assertEquals("大型", dto.getScale());

        // 测试前端别名
        assertEquals("张三", dto.getContact(),
                "getContact() 应返回 contactPerson 的值");
        assertEquals("13800138000", dto.getPhone(),
                "getPhone() 应返回 contactPhone 的值");
    }

    @Test
    @DisplayName("获取供应商：不存在应抛出异常")
    void testGetSupplierById_NotFound() {
        when(supplierRepository.findById(999L))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> supplierService.getSupplierById(999L));

        assertEquals("供应商不存在", exception.getMessage());
    }

    /**
     * 创建测试用 Supplier 对象
     */
    private Supplier createSupplier(String name, String qualification) {
        Supplier supplier = new Supplier();
        supplier.setName(name);
        supplier.setQualification(qualification);
        supplier.setStatus("正常");
        supplier.setRegion("北京");
        supplier.setIndustry("IT");
        supplier.setCreditCode("TEST-" + System.currentTimeMillis());
        supplier.setAddress("测试地址");
        supplier.setEstablishDate(LocalDate.of(2020, 1, 1));
        supplier.setRegisteredCapital(new BigDecimal("1000000"));
        return supplier;
    }
}
