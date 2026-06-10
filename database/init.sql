-- 供应商管理系统数据库初始化脚本
-- 创建数据库
CREATE DATABASE IF NOT EXISTS supplier_management 
CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE supplier_management;

-- 删除已存在的表（如果存在）
DROP TABLE IF EXISTS operation_log;
DROP TABLE IF EXISTS selection_result;
DROP TABLE IF EXISTS graded_selection_rules;
DROP TABLE IF EXISTS supplier;

-- 创建供应商表
CREATE TABLE supplier (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL COMMENT '供应商名称',
    credit_code VARCHAR(50) UNIQUE NOT NULL COMMENT '统一社会信用代码',
    qualification VARCHAR(50) NOT NULL COMMENT '资质等级',
    region VARCHAR(100) NOT NULL COMMENT '地区',
    industry VARCHAR(100) NOT NULL COMMENT '行业',
    address VARCHAR(500) NOT NULL COMMENT '注册地址',
    contact_person VARCHAR(100) NOT NULL COMMENT '联系人',
    contact_phone VARCHAR(20) NOT NULL COMMENT '联系电话',
    contact_email VARCHAR(100) COMMENT '联系邮箱',
    business_scope TEXT COMMENT '经营范围',
    performance TEXT COMMENT '过往业绩',
    establish_date DATE COMMENT '成立时间',
    legal_person VARCHAR(100) COMMENT '法人',
    registered_capital DECIMAL(15,2) COMMENT '注册资本',
    status VARCHAR(50) DEFAULT '正常' COMMENT '经营状态',
    scale VARCHAR(50) COMMENT '企业规模',
    qualification_materials TEXT COMMENT '资质材料（JSON格式）',
    certification_date DATE COMMENT '认证日期',
    expiry_date DATE COMMENT '到期日期',
    remark TEXT COMMENT '备注',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='供应商信息表';

-- 创建分级选择规则表
CREATE TABLE graded_selection_rules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    rule_name VARCHAR(200) NOT NULL COMMENT '规则名称',
    qualification VARCHAR(50) NOT NULL COMMENT '资质等级',
    percentage INTEGER NOT NULL COMMENT '占比百分比',
    min_count INTEGER NOT NULL COMMENT '最小数量',
    max_count INTEGER NOT NULL COMMENT '最大数量',
    industry VARCHAR(100) COMMENT '行业限制',
    region VARCHAR(100) COMMENT '地区限制',
    is_active BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    description TEXT COMMENT '规则描述',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分级选择规则表';

-- 创建选择结果表
CREATE TABLE selection_result (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    selection_type VARCHAR(50) NOT NULL COMMENT '选择类型：random-随机，graded-分级',
    total_count INTEGER NOT NULL COMMENT '总选择数量',
    conditions TEXT COMMENT '选择条件（JSON格式）',
    results TEXT COMMENT '选择结果（JSON格式）',
    operator VARCHAR(100) COMMENT '操作人',
    retry_count INTEGER DEFAULT 0 COMMENT '重试次数',
    reasons TEXT COMMENT '重试原因',
    ip_address VARCHAR(50) COMMENT 'IP地址',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='选择结果表';

-- 创建操作日志表
CREATE TABLE operation_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    operation_type VARCHAR(100) NOT NULL COMMENT '操作类型',
    operation_desc TEXT COMMENT '操作描述',
    operator VARCHAR(100) COMMENT '操作人',
    ip_address VARCHAR(50) COMMENT 'IP地址',
    request_data TEXT COMMENT '请求数据',
    response_data TEXT COMMENT '响应数据',
    status VARCHAR(20) DEFAULT 'SUCCESS' COMMENT '操作状态',
    error_message TEXT COMMENT '错误信息',
    execution_time BIGINT COMMENT '执行时间（毫秒）',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

-- 创建索引
CREATE INDEX idx_supplier_qualification ON supplier(qualification);
CREATE INDEX idx_supplier_region ON supplier(region);
CREATE INDEX idx_supplier_industry ON supplier(industry);
CREATE INDEX idx_supplier_status ON supplier(status);
CREATE INDEX idx_supplier_scale ON supplier(scale);
CREATE INDEX idx_supplier_establish_date ON supplier(establish_date);
CREATE INDEX idx_supplier_created_at ON supplier(created_at);

CREATE INDEX idx_graded_rule_qualification ON graded_selection_rules(qualification);
CREATE INDEX idx_graded_rule_active ON graded_selection_rules(is_active);

CREATE INDEX idx_selection_result_type ON selection_result(selection_type);
CREATE INDEX idx_selection_result_created_at ON selection_result(created_at);

CREATE INDEX idx_operation_log_type ON operation_log(operation_type);
CREATE INDEX idx_operation_log_operator ON operation_log(operator);
CREATE INDEX idx_operation_log_created_at ON operation_log(created_at);

-- 插入示例数据

-- 插入分级选择规则
INSERT INTO graded_selection_rules (rule_name, qualification, percentage, min_count, max_count, industry, region, description) VALUES
('A级供应商规则', 'A级', 60, 2, 3, '制造业', '全国', 'A级供应商占比60%，数量2-3家'),
('B级供应商规则', 'B级', 30, 1, 2, '制造业', '全国', 'B级供应商占比30%，数量1-2家'),
('C级供应商规则', 'C级', 10, 0, 1, '制造业', '全国', 'C级供应商占比10%，数量0-1家');

-- 插入示例供应商数据
INSERT INTO supplier (name, credit_code, qualification, region, industry, address, contact_person, contact_phone, contact_email, business_scope, performance, establish_date, legal_person, registered_capital, status, scale, qualification_materials, certification_date, expiry_date, remark) VALUES
('北京科技有限公司', '91110000123456789X', 'A级', '北京市', '制造业', '北京市朝阳区科技园区123号', '张三', '13800138001', 'zhangsan@tech.com', '软件开发、系统集成、技术咨询', '成功完成多个大型政府项目，客户满意度95%以上', '2010-05-15', '李四', 10000000.00, '正常', '大型', '["ISO9001认证","CMMI5级认证","高新技术企业证书"]', '2023-01-01', '2026-01-01', '技术实力强，服务态度好'),
('上海制造集团', '91310000123456789Y', 'A级', '上海市', '制造业', '上海市浦东新区工业园456号', '王五', '13800138002', 'wangwu@manufacture.com', '机械制造、设备生产、工业自动化', '年产值超过5亿元，产品远销海外', '2008-03-20', '赵六', 50000000.00, '正常', '大型', '["ISO9001认证","CE认证","高新技术企业证书"]', '2023-02-01', '2026-02-01', '产品质量稳定，交付及时'),
('广州贸易有限公司', '91440000123456789Z', 'B级', '广东省', '贸易业', '广州市天河区商务中心789号', '孙七', '13800138003', 'sunqi@trade.com', '进出口贸易、供应链管理、物流服务', '年贸易额超过2亿元，合作伙伴众多', '2012-08-10', '周八', 20000000.00, '正常', '中型', '["对外贸易经营者备案","海关A类企业","ISO9001认证"]', '2023-03-01', '2026-03-01', '贸易经验丰富，渠道稳定'),
('深圳电子科技', '91440300123456789A', 'B级', '广东省', '制造业', '深圳市南山区科技园321号', '吴九', '13800138004', 'wujiu@electronics.com', '电子产品研发、生产、销售', '产品获得多项专利，市场反响良好', '2015-12-01', '郑十', 15000000.00, '正常', '中型', '["高新技术企业证书","ISO9001认证","专利证书"]', '2023-04-01', '2026-04-01', '创新能力强，技术先进'),
('杭州网络科技', '91330000123456789B', 'C级', '浙江省', '服务业', '杭州市西湖区软件园654号', '钱一', '13800138005', 'qianyi@network.com', '网络技术服务、软件开发、云服务', '服务客户超过100家，技术团队专业', '2018-06-15', '孙二', 5000000.00, '正常', '小型', '["软件企业认定证书","ISO9001认证"]', '2023-05-01', '2026-05-01', '服务态度好，响应速度快'),
('成都建筑公司', '91510000123456789C', 'C级', '四川省', '建筑业', '成都市高新区建筑园区987号', '李三', '13800138006', 'lisan@construction.com', '建筑工程、装修装饰、市政工程', '完成多个优质工程，获得行业认可', '2016-09-20', '王四', 8000000.00, '正常', '小型', '["建筑资质证书","安全生产许可证","ISO9001认证"]', '2023-06-01', '2026-06-01', '工程质量可靠，安全记录良好'),
('武汉物流公司', '91420000123456789D', 'B级', '湖北省', '物流业', '武汉市东西湖区物流园147号', '张五', '13800138007', 'zhangwu@logistics.com', '物流配送、仓储服务、供应链管理', '覆盖华中地区，配送网络完善', '2014-04-12', '赵六', 12000000.00, '正常', '中型', '["道路运输许可证","仓储服务许可证","ISO9001认证"]', '2023-07-01', '2026-07-01', '物流网络完善，服务覆盖广'),
('西安软件公司', '91610000123456789E', 'A级', '陕西省', '服务业', '西安市高新区软件园258号', '刘七', '13800138008', 'liuqi@software.com', '软件开发、系统集成、技术培训', '拥有多项自主知识产权，技术领先', '2011-11-08', '孙八', 25000000.00, '正常', '大型', '["高新技术企业证书","软件企业认定证书","CMMI4级认证"]', '2023-08-01', '2026-08-01', '技术实力雄厚，创新能力强'),
('南京咨询公司', '91320000123456789F', 'C级', '江苏省', '服务业', '南京市建邺区商务区369号', '陈九', '13800138009', 'chenjiu@consulting.com', '管理咨询、培训服务、项目评估', '服务过多个知名企业，口碑良好', '2017-02-28', '周十', 3000000.00, '正常', '小型', '["咨询资质证书","ISO9001认证"]', '2023-09-01', '2026-09-01', '专业水平高，服务细致'),
('重庆制造公司', '91500000123456789G', 'B级', '重庆市', '制造业', '重庆市渝北区工业园741号', '杨一', '13800138010', 'yangyi@manufacture2.com', '汽车零部件、机械加工、模具制造', '为多家汽车厂商配套，质量稳定', '2013-07-14', '郑二', 18000000.00, '正常', '中型', '["ISO9001认证","TS16949认证","高新技术企业证书"]', '2023-10-01', '2026-10-01', '产品质量可靠，技术成熟');

-- 显示创建的表结构
SHOW TABLES;

-- 显示各表的记录数
SELECT 'supplier' as table_name, COUNT(*) as record_count FROM supplier
UNION ALL
SELECT 'graded_selection_rules' as table_name, COUNT(*) as record_count FROM graded_selection_rules
UNION ALL
SELECT 'selection_result' as table_name, COUNT(*) as record_count FROM selection_result
UNION ALL
SELECT 'operation_log' as table_name, COUNT(*) as record_count FROM operation_log;
