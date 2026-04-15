-- 更新现有供应商经营状态的SQL脚本
-- 这个脚本将更新现有供应商的经营状态，展示更多的经营状态选项

USE supplier_management;

-- 首先查看当前数据库中的经营状态
SELECT DISTINCT status FROM supplier;

-- 更新现有供应商的经营状态，添加更多状态选项
UPDATE supplier SET status = '正常经营' WHERE id = 1;  -- 北京科技有限公司
UPDATE supplier SET status = '正常经营' WHERE id = 2;  -- 上海制造集团
UPDATE supplier SET status = '正常经营' WHERE id = 3;  -- 广州贸易有限公司
UPDATE supplier SET status = '正常经营' WHERE id = 4;  -- 深圳电子科技
UPDATE supplier SET status = '正常经营' WHERE id = 5;  -- 杭州网络科技
UPDATE supplier SET status = '暂停营业' WHERE id = 6;  -- 成都建筑公司
UPDATE supplier SET status = '正常经营' WHERE id = 7;  -- 武汉物流公司
UPDATE supplier SET status = '正常经营' WHERE id = 8;  -- 西安软件公司
UPDATE supplier SET status = '经营异常' WHERE id = 9;  -- 南京咨询公司
UPDATE supplier SET status = '重组中' WHERE id = 10;   -- 重庆制造公司

-- 查看更新后的经营状态
SELECT DISTINCT status FROM supplier ORDER BY status;

-- 查看各经营状态的供应商数量
SELECT status, COUNT(*) as count FROM supplier GROUP BY status ORDER BY count DESC;

-- 查看更新后的供应商列表
SELECT id, name, status FROM supplier ORDER BY id;
