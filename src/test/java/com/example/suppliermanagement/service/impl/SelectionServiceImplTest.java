package com.example.suppliermanagement.service.impl;

import com.example.suppliermanagement.model.Supplier;
import com.example.suppliermanagement.repository.SelectionResultRepository;
import com.example.suppliermanagement.service.OperationLogService;
import com.example.suppliermanagement.service.SupplierService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * SelectionServiceImpl 单元测试
 * 测试智能抽取算法的核心逻辑
 */
@ExtendWith(MockitoExtension.class)
class SelectionServiceImplTest {

    @Mock
    private SelectionResultRepository selectionResultRepository;

    @Mock
    private SupplierService supplierService;

    @Mock
    private OperationLogService logService;

    private SelectionServiceImpl selectionService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        selectionService = new SelectionServiceImpl(
                selectionResultRepository,
                supplierService,
                logService,
                objectMapper
        );
    }

    @Test
    @DisplayName("资质等级权重计算：A级供应商权重应为1.0")
    void testQualificationWeight_A() {
        Supplier supplierA = createSupplier("A级供应商", "A", "正常", "北京", "IT");
        List<Supplier> suppliers = Collections.singletonList(supplierA);

        when(supplierService.getEligibleForRandomSelection(any(), any(), anyInt()))
                .thenReturn(suppliers);

        // 执行抽取
        try {
            var method = SelectionServiceImpl.class.getDeclaredMethod(
                    "calculateSupplierWeights", List.class);
            method.setAccessible(true);

            @SuppressWarnings("unchecked")
            var weights = (java.util.Map<Supplier, Double>) method.invoke(selectionService, suppliers);

            assertNotNull(weights);
            assertEquals(1.0, weights.get(supplierA), 0.001, "A级供应商权重应为1.0");
        } catch (Exception e) {
            fail("反射调用失败: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("资质等级权重计算：B级供应商权重应为0.8")
    void testQualificationWeight_B() {
        Supplier supplierB = createSupplier("B级供应商", "B", "正常", "上海", "IT");
        List<Supplier> suppliers = Collections.singletonList(supplierB);

        try {
            var method = SelectionServiceImpl.class.getDeclaredMethod(
                    "calculateSupplierWeights", List.class);
            method.setAccessible(true);

            @SuppressWarnings("unchecked")
            var weights = (java.util.Map<Supplier, Double>) method.invoke(selectionService, suppliers);

            assertNotNull(weights);
            assertEquals(0.8, weights.get(supplierB), 0.001, "B级供应商权重应为0.8");
        } catch (Exception e) {
            fail("反射调用失败: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("资质等级权重计算：未知等级应使用默认权重0.5")
    void testQualificationWeight_Unknown() {
        Supplier supplierUnknown = createSupplier("未知供应商", "X", "正常", "广州", "IT");
        List<Supplier> suppliers = Collections.singletonList(supplierUnknown);

        try {
            var method = SelectionServiceImpl.class.getDeclaredMethod(
                    "calculateSupplierWeights", List.class);
            method.setAccessible(true);

            @SuppressWarnings("unchecked")
            var weights = (java.util.Map<Supplier, Double>) method.invoke(selectionService, suppliers);

            assertNotNull(weights);
            assertEquals(0.5, weights.get(supplierUnknown), 0.001, "未知等级供应商权重应为0.5");
        } catch (Exception e) {
            fail("反射调用失败: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("资质等级评分计算：A级应为100分，B级应为80分")
    void testQualificationScore() {
        try {
            var method = SelectionServiceImpl.class.getDeclaredMethod(
                    "calculateQualificationScore", String.class);
            method.setAccessible(true);

            // 测试 A 级
            double scoreA = (double) method.invoke(selectionService, "A");
            assertEquals(100.0, scoreA, 0.001, "A级评分应为100");

            // 测试 B 级
            double scoreB = (double) method.invoke(selectionService, "B");
            assertEquals(80.0, scoreB, 0.001, "B级评分应为80");

            // 测试 C 级
            double scoreC = (double) method.invoke(selectionService, "C");
            assertEquals(60.0, scoreC, 0.001, "C级评分应为60");

            // 测试 D 级
            double scoreD = (double) method.invoke(selectionService, "D");
            assertEquals(40.0, scoreD, 0.001, "D级评分应为40");

            // 测试未知等级
            double scoreUnknown = (double) method.invoke(selectionService, "X");
            assertEquals(50.0, scoreUnknown, 0.001, "未知等级评分应为50");

        } catch (Exception e) {
            fail("反射调用失败: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("经营状态评分：正常状态权重应高于停业状态")
    void testBusinessStatusScore() {
        try {
            var method = SelectionServiceImpl.class.getDeclaredMethod(
                    "calculateBusinessStatusScore", Supplier.class);
            method.setAccessible(true);

            Supplier normalSupplier = createSupplier("正常供应商", "A", "正常", "北京", "IT");
            Supplier suspendedSupplier = createSupplier("停业供应商", "A", "停业", "北京", "IT");

            double normalScore = (double) method.invoke(selectionService, normalSupplier);
            double suspendedScore = (double) method.invoke(selectionService, suspendedSupplier);

            assertTrue(normalScore > suspendedScore,
                    "正常状态评分应高于停业状态");

        } catch (Exception e) {
            fail("反射调用失败: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("资质等级映射统一性：数据库存储的A/B/C/D格式应与权重表一致")
    void testQualificationMappingConsistency() {
        // 验证：数据库存 A，权重表 key 也应该是 A（而不是 A级）
        // 这个测试确保修改后的代码不再出现 "A级" 作为 key
        try {
            var method = SelectionServiceImpl.class.getDeclaredMethod(
                    "calculateSupplierWeights", List.class);
            method.setAccessible(true);

            Supplier supplierA = createSupplier("A级供应商", "A", "正常", "北京", "IT");
            List<Supplier> suppliers = Collections.singletonList(supplierA);

            @SuppressWarnings("unchecked")
            var weights = (java.util.Map<Supplier, Double>) method.invoke(selectionService, suppliers);

            // 如果 supplier.getQualification() 返回 "A"（不是 "A级"），
            // 且权重表 key 是 "A"（不是 "A级"），则权重应为 1.0
            assertEquals(1.0, weights.get(supplierA), 0.001,
                    "使用数据库格式 'A' 查询权重应命中，而不是命中默认值 0.5");

        } catch (Exception e) {
            fail("反射调用失败: " + e.getMessage());
        }
    }

    /**
     * 创建测试用 Supplier 对象
     */
    private Supplier createSupplier(String name, String qualification, String status,
                                    String region, String industry) {
        Supplier supplier = new Supplier();
        supplier.setName(name);
        supplier.setQualification(qualification);
        supplier.setStatus(status);
        supplier.setRegion(region);
        supplier.setIndustry(industry);
        supplier.setCreditCode("TEST-" + System.currentTimeMillis());
        supplier.setAddress("测试地址");
        supplier.setEstablishDate(LocalDate.of(2020, 1, 1));
        supplier.setRegisteredCapital(new BigDecimal("1000000"));
        return supplier;
    }
}
