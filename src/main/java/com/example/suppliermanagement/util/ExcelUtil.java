package com.example.suppliermanagement.util;

import com.example.suppliermanagement.model.Supplier;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
public class ExcelUtil {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 导入供应商数据
     */
    public List<Supplier> importSuppliers(MultipartFile file) throws IOException {
        List<Supplier> suppliers = new ArrayList<>();
        
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            
            // 跳过标题行
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                Supplier supplier = new Supplier();
                supplier.setName(getCellValue(row.getCell(0)));
                supplier.setCreditCode(getCellValue(row.getCell(1)));
                supplier.setQualification(getCellValue(row.getCell(2)));
                supplier.setRegion(getCellValue(row.getCell(3)));
                supplier.setIndustry(getCellValue(row.getCell(4)));
                supplier.setAddress(getCellValue(row.getCell(5)));
                supplier.setContactPerson(getCellValue(row.getCell(6)));
                supplier.setContactPhone(getCellValue(row.getCell(7)));
                supplier.setContactEmail(getCellValue(row.getCell(8)));
                supplier.setBusinessScope(getCellValue(row.getCell(9)));
                supplier.setPerformance(getCellValue(row.getCell(10)));
                supplier.setEstablishDate(parseDate(getCellValue(row.getCell(11))));
                supplier.setLegalPerson(getCellValue(row.getCell(12)));
                supplier.setRegisteredCapital(parseBigDecimal(getCellValue(row.getCell(13))));
                supplier.setStatus(getCellValue(row.getCell(14)));
                supplier.setScale(getCellValue(row.getCell(15)));
                supplier.setQualificationMaterials(getCellValue(row.getCell(16)));
                supplier.setCertificationDate(parseDate(getCellValue(row.getCell(17))));
                supplier.setExpiryDate(parseDate(getCellValue(row.getCell(18))));
                supplier.setRemark(getCellValue(row.getCell(19)));
                
                suppliers.add(supplier);
            }
        }
        
        return suppliers;
    }

    /**
     * 导出供应商数据
     */
    public byte[] exportSuppliers(List<Supplier> suppliers) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("供应商信息");
            
            // 创建标题行
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                "供应商名称", "统一社会信用代码", "资质等级", "地区", "行业", "注册地址", 
                "联系人", "联系电话", "联系邮箱", "经营范围", "过往业绩", "成立时间", 
                "法人", "注册资本", "经营状态", "企业规模", "资质材料", "认证日期", 
                "到期日期", "备注"
            };
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }
            
            // 填充数据
            for (int i = 0; i < suppliers.size(); i++) {
                Supplier supplier = suppliers.get(i);
                Row row = sheet.createRow(i + 1);
                
                row.createCell(0).setCellValue(supplier.getName());
                row.createCell(1).setCellValue(supplier.getCreditCode());
                row.createCell(2).setCellValue(supplier.getQualification());
                row.createCell(3).setCellValue(supplier.getRegion());
                row.createCell(4).setCellValue(supplier.getIndustry());
                row.createCell(5).setCellValue(supplier.getAddress());
                row.createCell(6).setCellValue(supplier.getContactPerson());
                row.createCell(7).setCellValue(supplier.getContactPhone());
                row.createCell(8).setCellValue(supplier.getContactEmail());
                row.createCell(9).setCellValue(supplier.getBusinessScope());
                row.createCell(10).setCellValue(supplier.getPerformance());
                row.createCell(11).setCellValue(formatDate(supplier.getEstablishDate()));
                row.createCell(12).setCellValue(supplier.getLegalPerson());
                row.createCell(13).setCellValue(supplier.getRegisteredCapital() != null ? supplier.getRegisteredCapital().toString() : "");
                row.createCell(14).setCellValue(supplier.getStatus());
                row.createCell(15).setCellValue(supplier.getScale());
                row.createCell(16).setCellValue(supplier.getQualificationMaterials());
                row.createCell(17).setCellValue(formatDate(supplier.getCertificationDate()));
                row.createCell(18).setCellValue(formatDate(supplier.getExpiryDate()));
                row.createCell(19).setCellValue(supplier.getRemark());
            }
            
            // 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                }
                return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return "";
        }
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(dateStr.trim(), DATE_FORMATTER);
        } catch (Exception e) {
            return null;
        }
    }

    private BigDecimal parseBigDecimal(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(value.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private String formatDate(LocalDate date) {
        return date != null ? date.format(DATE_FORMATTER) : "";
    }
}
