# 抽取功能调试指南

## 问题描述
项目主页面中抽取部分界面不显示的问题。

## 问题分析
经过检查，发现以下问题：

1. **CSS样式缺失**：主页面缺少`.section`类的CSS样式定义
2. **默认显示设置**：供应商管理section没有设置`active`类作为默认显示

## 已修复的问题

### 1. 添加了缺失的CSS样式
```css
.section {
    display: none;
    padding: 20px;
}
.section.active {
    display: block;
}
```

### 2. 设置了默认显示的section
```html
<div id="supplier-management" class="section active">
```

## 测试步骤

### 1. 测试主页面抽取功能
1. 打开主页面 `index.html`
2. 点击左侧导航栏的"智能随机抽取"
3. 检查是否正常显示抽取界面
4. 点击"智能分级抽取"测试分级抽取界面
5. 点击"抽取规则"测试规则管理界面

### 2. 使用调试页面测试
如果主页面仍有问题，可以使用专门的调试页面：
1. 打开 `debug-extraction.html`
2. 测试所有抽取功能是否正常
3. 检查浏览器控制台是否有错误信息

### 3. 检查浏览器控制台
1. 按F12打开开发者工具
2. 查看Console标签页
3. 检查是否有JavaScript错误
4. 查看页面切换的日志信息

## 预期结果

修复后，您应该能够：
- 正常显示供应商管理页面（默认页面）
- 点击导航栏切换到抽取相关页面
- 看到完整的抽取配置界面
- 使用抽取动画功能
- 查看抽取结果

## 如果问题仍然存在

### 1. 检查浏览器兼容性
- 确保使用现代浏览器（Chrome、Firefox、Edge等）
- 检查是否启用了JavaScript

### 2. 检查文件路径
- 确保所有HTML、CSS、JS文件都在正确的位置
- 检查静态资源是否正确加载

### 3. 检查网络请求
- 查看Network标签页是否有失败的请求
- 确保后端服务正常运行

### 4. 清除浏览器缓存
- 按Ctrl+F5强制刷新页面
- 清除浏览器缓存和Cookie

## 联系支持

如果按照以上步骤仍然无法解决问题，请：
1. 提供浏览器控制台的错误信息
2. 描述具体的操作步骤和现象
3. 提供浏览器版本和操作系统信息

## 相关文件

- `src/main/resources/static/index.html` - 主页面
- `src/main/resources/static/debug-extraction.html` - 调试页面
- `EXTRACTION_DEBUG_GUIDE.md` - 本调试指南





