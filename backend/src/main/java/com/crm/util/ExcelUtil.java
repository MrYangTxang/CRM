package com.crm.util;

import org.apache.poi.ss.usermodel.*;

/**
 * Excel 工具类 — 提供单元格读取和表头样式的复用方法
 */
public class ExcelUtil {

    /** 读取单元格字符串值 */
    public static String getCellValue(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                double val = cell.getNumericCellValue();
                // 整数则不带小数点，小数保留原值（避免金额截断）
                if (val == Math.floor(val) && !Double.isInfinite(val)) {
                    return String.valueOf((long) val);
                }
                return String.valueOf(val);
            default:
                return "";
        }
    }

    /** 创建表头样式（加粗 + 灰色背景） */
    public static CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }
}
