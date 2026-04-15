-- Supplier Management System Database Initialization Script
-- Create database
CREATE DATABASE IF NOT EXISTS supplier_management 
CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE supplier_management;

-- Drop existing tables if they exist
DROP TABLE IF EXISTS operation_log;
DROP TABLE IF EXISTS selection_result;
DROP TABLE IF EXISTS graded_selection_rule;
DROP TABLE IF EXISTS supplier;

-- Create supplier table
CREATE TABLE supplier (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL COMMENT 'Supplier Name',
    credit_code VARCHAR(50) UNIQUE NOT NULL COMMENT 'Credit Code',
    qualification VARCHAR(50) NOT NULL COMMENT 'Qualification Level',
    region VARCHAR(100) NOT NULL COMMENT 'Region',
    industry VARCHAR(100) NOT NULL COMMENT 'Industry',
    address VARCHAR(500) NOT NULL COMMENT 'Address',
    contact_person VARCHAR(100) NOT NULL COMMENT 'Contact Person',
    contact_phone VARCHAR(20) NOT NULL COMMENT 'Contact Phone',
    contact_email VARCHAR(100) COMMENT 'Contact Email',
    business_scope TEXT COMMENT 'Business Scope',
    performance TEXT COMMENT 'Performance',
    establish_date DATE COMMENT 'Establish Date',
    legal_person VARCHAR(100) COMMENT 'Legal Person',
    registered_capital DECIMAL(15,2) COMMENT 'Registered Capital',
    status VARCHAR(50) DEFAULT 'Normal' COMMENT 'Status',
    scale VARCHAR(50) COMMENT 'Scale',
    qualification_materials TEXT COMMENT 'Qualification Materials',
    certification_date DATE COMMENT 'Certification Date',
    expiry_date DATE COMMENT 'Expiry Date',
    remark TEXT COMMENT 'Remark',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Created At',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated At'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Supplier Information Table';

-- Create graded selection rule table
CREATE TABLE graded_selection_rule (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    rule_name VARCHAR(200) NOT NULL COMMENT 'Rule Name',
    qualification VARCHAR(50) NOT NULL COMMENT 'Qualification Level',
    percentage INTEGER NOT NULL COMMENT 'Percentage',
    min_count INTEGER NOT NULL COMMENT 'Min Count',
    max_count INTEGER NOT NULL COMMENT 'Max Count',
    industry VARCHAR(100) COMMENT 'Industry Limit',
    region VARCHAR(100) COMMENT 'Region Limit',
    is_active BOOLEAN DEFAULT TRUE COMMENT 'Is Active',
    description TEXT COMMENT 'Description',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Created At',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated At'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Graded Selection Rule Table';

-- Create selection result table
CREATE TABLE selection_result (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    selection_type VARCHAR(50) NOT NULL COMMENT 'Selection Type',
    total_count INTEGER NOT NULL COMMENT 'Total Count',
    conditions TEXT COMMENT 'Conditions',
    results TEXT COMMENT 'Results',
    operator VARCHAR(100) COMMENT 'Operator',
    retry_count INTEGER DEFAULT 0 COMMENT 'Retry Count',
    reasons TEXT COMMENT 'Reasons',
    ip_address VARCHAR(50) COMMENT 'IP Address',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Created At'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Selection Result Table';

-- Create operation log table
CREATE TABLE operation_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    operation_type VARCHAR(100) NOT NULL COMMENT 'Operation Type',
    operation_desc TEXT COMMENT 'Operation Description',
    operator VARCHAR(100) COMMENT 'Operator',
    ip_address VARCHAR(50) COMMENT 'IP Address',
    request_data TEXT COMMENT 'Request Data',
    response_data TEXT COMMENT 'Response Data',
    status VARCHAR(20) DEFAULT 'SUCCESS' COMMENT 'Status',
    error_message TEXT COMMENT 'Error Message',
    execution_time BIGINT COMMENT 'Execution Time',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Created At'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Operation Log Table';

-- Create indexes
CREATE INDEX idx_supplier_qualification ON supplier(qualification);
CREATE INDEX idx_supplier_region ON supplier(region);
CREATE INDEX idx_supplier_industry ON supplier(industry);
CREATE INDEX idx_supplier_status ON supplier(status);
CREATE INDEX idx_supplier_scale ON supplier(scale);
CREATE INDEX idx_supplier_establish_date ON supplier(establish_date);
CREATE INDEX idx_supplier_created_at ON supplier(created_at);

CREATE INDEX idx_graded_rule_qualification ON graded_selection_rule(qualification);
CREATE INDEX idx_graded_rule_active ON graded_selection_rule(is_active);

CREATE INDEX idx_selection_result_type ON selection_result(selection_type);
CREATE INDEX idx_selection_result_created_at ON selection_result(created_at);

CREATE INDEX idx_operation_log_type ON operation_log(operation_type);
CREATE INDEX idx_operation_log_operator ON operation_log(operator);
CREATE INDEX idx_operation_log_created_at ON operation_log(created_at);

-- Insert sample data

-- Insert graded selection rules
INSERT INTO graded_selection_rule (rule_name, qualification, percentage, min_count, max_count, industry, region, description) VALUES
('A Level Rule', 'A', 60, 2, 3, 'Manufacturing', 'National', 'A level suppliers 60%, count 2-3'),
('B Level Rule', 'B', 30, 1, 2, 'Manufacturing', 'National', 'B level suppliers 30%, count 1-2'),
('C Level Rule', 'C', 10, 0, 1, 'Manufacturing', 'National', 'C level suppliers 10%, count 0-1');

-- Insert sample supplier data
INSERT INTO supplier (name, credit_code, qualification, region, industry, address, contact_person, contact_phone, contact_email, business_scope, performance, establish_date, legal_person, registered_capital, status, scale, qualification_materials, certification_date, expiry_date, remark) VALUES
('Beijing Technology Co Ltd', '91110000123456789X', 'A', 'Beijing', 'Manufacturing', 'Beijing Chaoyang Tech Park 123', 'Zhang San', '13800138001', 'zhangsan@tech.com', 'Software Development, System Integration, Technical Consulting', 'Successfully completed multiple large government projects, customer satisfaction above 95%', '2010-05-15', 'Li Si', 10000000.00, 'Normal', 'Large', '["ISO9001","CMMI5","High-Tech Enterprise"]', '2023-01-01', '2026-01-01', 'Strong technical strength, good service attitude'),
('Shanghai Manufacturing Group', '91310000123456789Y', 'A', 'Shanghai', 'Manufacturing', 'Shanghai Pudong Industrial Park 456', 'Wang Wu', '13800138002', 'wangwu@manufacture.com', 'Mechanical Manufacturing, Equipment Production, Industrial Automation', 'Annual output value over 500 million, products exported overseas', '2008-03-20', 'Zhao Liu', 50000000.00, 'Normal', 'Large', '["ISO9001","CE","High-Tech Enterprise"]', '2023-02-01', '2026-02-01', 'Stable product quality, timely delivery'),
('Guangzhou Trading Co Ltd', '91440000123456789Z', 'B', 'Guangdong', 'Trading', 'Guangzhou Tianhe Business Center 789', 'Sun Qi', '13800138003', 'sunqi@trade.com', 'Import Export Trade, Supply Chain Management, Logistics Services', 'Annual trade volume over 200 million, many partners', '2012-08-10', 'Zhou Ba', 20000000.00, 'Normal', 'Medium', '["Foreign Trade License","Customs Class A","ISO9001"]', '2023-03-01', '2026-03-01', 'Rich trading experience, stable channels'),
('Shenzhen Electronics Tech', '91440300123456789A', 'B', 'Guangdong', 'Manufacturing', 'Shenzhen Nanshan Tech Park 321', 'Wu Jiu', '13800138004', 'wujiu@electronics.com', 'Electronics R&D, Production, Sales', 'Products obtained multiple patents, good market response', '2015-12-01', 'Zheng Shi', 15000000.00, 'Normal', 'Medium', '["High-Tech Enterprise","ISO9001","Patent"]', '2023-04-01', '2026-04-01', 'Strong innovation capability, advanced technology'),
('Hangzhou Network Tech', '91330000123456789B', 'C', 'Zhejiang', 'Service', 'Hangzhou Xihu Software Park 654', 'Qian Yi', '13800138005', 'qianyi@network.com', 'Network Technical Services, Software Development, Cloud Services', 'Served over 100 clients, professional technical team', '2018-06-15', 'Sun Er', 5000000.00, 'Normal', 'Small', '["Software Enterprise","ISO9001"]', '2023-05-01', '2026-05-01', 'Good service attitude, fast response'),
('Chengdu Construction Co', '91510000123456789C', 'C', 'Sichuan', 'Construction', 'Chengdu High-tech Construction Park 987', 'Li San', '13800138006', 'lisan@construction.com', 'Construction Engineering, Decoration, Municipal Engineering', 'Completed multiple quality projects, industry recognition', '2016-09-20', 'Wang Si', 8000000.00, 'Normal', 'Small', '["Construction License","Safety License","ISO9001"]', '2023-06-01', '2026-06-01', 'Reliable project quality, good safety record'),
('Wuhan Logistics Co', '91420000123456789D', 'B', 'Hubei', 'Logistics', 'Wuhan Dongxihu Logistics Park 147', 'Zhang Wu', '13800138007', 'zhangwu@logistics.com', 'Logistics Distribution, Warehouse Services, Supply Chain Management', 'Covering Central China, complete distribution network', '2014-04-12', 'Zhao Liu', 12000000.00, 'Normal', 'Medium', '["Transport License","Warehouse License","ISO9001"]', '2023-07-01', '2026-07-01', 'Complete logistics network, wide service coverage'),
('Xian Software Co', '91610000123456789E', 'A', 'Shaanxi', 'Service', 'Xian High-tech Software Park 258', 'Liu Qi', '13800138008', 'liuqi@software.com', 'Software Development, System Integration, Technical Training', 'Multiple independent intellectual property rights, leading technology', '2011-11-08', 'Sun Ba', 25000000.00, 'Normal', 'Large', '["High-Tech Enterprise","Software Enterprise","CMMI4"]', '2023-08-01', '2026-08-01', 'Strong technical strength, strong innovation capability'),
('Nanjing Consulting Co', '91320000123456789F', 'C', 'Jiangsu', 'Service', 'Nanjing Jianye Business District 369', 'Chen Jiu', '13800138009', 'chenjiu@consulting.com', 'Management Consulting, Training Services, Project Evaluation', 'Served multiple well-known enterprises, good reputation', '2017-02-28', 'Zhou Shi', 3000000.00, 'Normal', 'Small', '["Consulting License","ISO9001"]', '2023-09-01', '2026-09-01', 'High professional level, meticulous service'),
('Chongqing Manufacturing Co', '91500000123456789G', 'B', 'Chongqing', 'Manufacturing', 'Chongqing Yubei Industrial Park 741', 'Yang Yi', '13800138010', 'yangyi@manufacture2.com', 'Auto Parts, Mechanical Processing, Mold Manufacturing', 'Supporting multiple auto manufacturers, stable quality', '2013-07-14', 'Zheng Er', 18000000.00, 'Normal', 'Medium', '["ISO9001","TS16949","High-Tech Enterprise"]', '2023-10-01', '2026-10-01', 'Reliable product quality, mature technology');

-- Show created table structure
SHOW TABLES;

-- Show record count for each table
SELECT 'supplier' as table_name, COUNT(*) as record_count FROM supplier
UNION ALL
SELECT 'graded_selection_rule' as table_name, COUNT(*) as record_count FROM graded_selection_rule
UNION ALL
SELECT 'selection_result' as table_name, COUNT(*) as record_count FROM selection_result
UNION ALL
SELECT 'operation_log' as table_name, COUNT(*) as record_count FROM operation_log;
