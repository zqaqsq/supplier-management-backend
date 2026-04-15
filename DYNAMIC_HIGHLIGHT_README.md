# 动态高亮中间行抽取功能说明

## 功能概述

本次更新实现了动态高亮中间行的抽取功能，系统会根据滚动位置自动高亮中间行的供应商数据，并抽取变蓝的那行数据。

## 核心功能

### 1. 动态高亮中间行
- **滚动时高亮**: 所有数据滚动时，滚动到中间行的数据自动变蓝
- **实时更新**: 根据滚动位置动态计算并高亮中间行
- **视觉突出**: 中间行使用蓝色渐变背景，白色文字，增强视觉效果

### 2. 智能抽取逻辑
- **抽取变蓝行**: 系统只随机抽取变蓝的那行数据
- **数据关联**: 每个滚动项目都存储对应的供应商数据
- **备选方案**: 如果无法获取中间行数据，使用随机选择作为备选

## 技术实现

### 1. 滚动动画优化
```javascript
// 动态更新中间行高亮
updateCenterHighlight();

// 停止时获取中间行的供应商数据
const selectedSupplier = getCenterSupplier();
```

### 2. 中间行高亮计算
```javascript
function updateCenterHighlight() {
    const centerPosition = rollingList.scrollTop + visibleHeight / 2;
    
    items.forEach((item, index) => {
        const itemTop = index * itemHeight;
        const itemBottom = itemTop + itemHeight;
        
        if (centerPosition >= itemTop && centerPosition < itemBottom) {
            item.classList.add('center-highlight');
        }
    });
}
```

### 3. 供应商数据获取
```javascript
function getCenterSupplier() {
    // 找到中心项目
    let centerItem = null;
    items.forEach((item, index) => {
        const itemTop = index * itemHeight;
        const itemBottom = itemTop + itemHeight;
        
        if (centerPosition >= itemTop && centerPosition < itemBottom) {
            centerItem = item;
        }
    });
    
    if (centerItem && centerItem.dataset.supplier) {
        return JSON.parse(centerItem.dataset.supplier);
    }
    
    return null;
}
```

## 用户体验

### 1. 视觉效果
- **中间行高亮**: 蓝色渐变背景，白色文字
- **放大效果**: 中间行放大1.1倍，突出显示
- **阴影增强**: 增强的阴影效果，提升视觉层次
- **平滑过渡**: 0.3秒的平滑过渡动画

### 2. 交互体验
- **实时反馈**: 滚动过程中实时显示中间行高亮
- **清晰指示**: 用户清楚知道哪行数据将被抽取
- **流畅动画**: 60fps的流畅滚动动画

## 工作流程

### 1. 抽取开始
1. 创建滚动列表，每个项目存储供应商数据
2. 清除之前的高亮状态
3. 开始滚动动画

### 2. 滚动过程
1. 数据持续滚动
2. 实时计算中间行位置
3. 动态高亮中间行（变蓝）
4. 逐渐加速滚动

### 3. 抽取完成
1. 随机停止滚动
2. 获取中间行的供应商数据
3. 将变蓝行的供应商添加到结果中
4. 继续下一个抽取或完成

## 技术特点

### 1. 性能优化
- **requestAnimationFrame**: 使用RAF确保60fps流畅动画
- **事件委托**: 减少DOM操作，提升性能
- **内存管理**: 及时清理高亮状态，避免内存泄漏

### 2. 兼容性
- **现代浏览器**: 支持Chrome、Firefox、Safari、Edge
- **响应式设计**: 适配不同屏幕尺寸
- **CSS3特性**: 使用现代CSS特性，优雅降级

### 3. 可维护性
- **模块化设计**: 功能分离，易于维护
- **清晰命名**: 函数和变量命名清晰明了
- **错误处理**: 完善的错误处理和备选方案

## 使用说明

### 1. 启动抽取
- 点击"开始智能抽取"按钮
- 系统显示抽取动画模态框
- 滚动列表开始滚动

### 2. 观察高亮
- 滚动过程中，中间行始终变蓝
- 蓝色行表示当前将被抽取的数据
- 高亮效果实时跟随滚动位置

### 3. 抽取结果
- 滚动停止后，变蓝行的供应商被选中
- 结果在右侧面板显示
- 可以继续抽取或查看完整结果

## 注意事项

### 1. 数据要求
- 确保供应商数据完整
- 每个滚动项目必须包含供应商信息
- 数据格式需要符合JSON标准

### 2. 性能考虑
- 大量数据时滚动性能可能受影响
- 建议控制单次抽取的供应商数量
- 监控内存使用情况

### 3. 用户体验
- 滚动速度适中，避免过快或过慢
- 高亮效果明显，便于用户识别
- 提供清晰的视觉反馈

## 未来改进

### 1. 功能增强
- 支持自定义高亮颜色
- 添加更多动画效果
- 支持键盘控制滚动

### 2. 性能优化
- 虚拟滚动支持大数据量
- WebGL加速滚动动画
- 智能预加载机制

### 3. 用户体验
- 可调节滚动速度
- 支持暂停/继续功能
- 添加音效反馈

