package com.example.suppliermanagement.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;

@Component
public class DatabaseUpdater {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    private static int counter = 0;

    @PostConstruct
    @Transactional
    public void updateDatabase() {
        try {
            System.out.println("开始更新数据库...");
            
            // 清空现有数据
            jdbcTemplate.execute("TRUNCATE TABLE suppliers");
            System.out.println("已清空现有供应商数据");
            
            // 插入新的中文数据
            insertChineseSuppliers();
            
            System.out.println("数据库更新完成！");
            
        } catch (Exception e) {
            System.err.println("数据库更新失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void insertChineseSuppliers() {
        // 插入A级供应商
        insertSupplier("北京科技创新有限公司", "A", "北京市", "科技创新");
        insertSupplier("上海智能制造集团", "A", "上海市", "智能制造");
        insertSupplier("深圳电子科技股份公司", "A", "深圳市", "电子科技");
        insertSupplier("杭州互联网科技有限公司", "A", "杭州市", "互联网科技");
        insertSupplier("广州生物医药有限公司", "A", "广州市", "生物医药");
        
        // 插入B级供应商
        insertSupplier("成都软件服务有限公司", "B", "成都市", "软件开发");
        insertSupplier("武汉建筑工程公司", "B", "武汉市", "建筑工程");
        insertSupplier("西安教育科技公司", "B", "西安市", "教育培训");
        insertSupplier("南京环保设备公司", "B", "南京市", "环保设备");
        insertSupplier("青岛海洋科技公司", "B", "青岛市", "海洋科技");
        
        // 插入C级供应商
        insertSupplier("重庆物流运输公司", "C", "重庆市", "物流运输");
        insertSupplier("天津食品加工公司", "C", "天津市", "食品加工");
        insertSupplier("大连船舶制造公司", "C", "大连市", "船舶制造");
        insertSupplier("厦门旅游服务公司", "C", "厦门市", "旅游服务");
        insertSupplier("苏州纺织服装公司", "C", "苏州市", "纺织服装");

        System.out.println("已插入15家中文供应商数据");
    }

    private void insertSupplier(String name, String qualification, String region, String industry) {
        String sql = "INSERT INTO suppliers (name, credit_code, qualification, region, industry, status, scale, " +
                     "contact_person, contact_phone, address, business_scope, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())";
        
        // 使用静态计数器确保唯一性
        counter++;
        
        String creditCode = "91" + String.format("%08d", counter);
        String contactPerson = "联系人" + counter;
        String contactPhone = "138" + String.format("%08d", counter);
        
        jdbcTemplate.update(sql, name, creditCode, qualification, region, industry, "正常", "中型",
                          contactPerson, contactPhone, region + "市某区某街道", 
                          industry + "相关业务");
    }
}
