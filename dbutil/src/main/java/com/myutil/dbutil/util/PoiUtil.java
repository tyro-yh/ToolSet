package com.myutil.dbutil.util;

import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * poi导出工具类
 * @author yh
 */
public class PoiUtil {

    /**
     * 默认表头
     */
    public static final List<String> NAME_LIST = new ArrayList<String>() {
        {
            add("字段名");
            add("数据类型");
            add("可为空");
            add("默认值");
            add("注释");
        }
    };

    /**
     * 创建表单样式
     * @param workbook
     * @return
     */
    public static CellStyle createCellStyle(Workbook workbook) {
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setBorderTop(BorderStyle.THIN);
        cellStyle.setBorderLeft(BorderStyle.THIN);
        cellStyle.setBorderRight(BorderStyle.THIN);
        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setWrapText(true);
        return cellStyle;
    }

    /**
     * 创建超链接样式
     * @param workbook
     * @return
     */
    public static CellStyle createLinkCellStyle(Workbook workbook) {
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setBorderTop(BorderStyle.THIN);
        cellStyle.setBorderLeft(BorderStyle.THIN);
        cellStyle.setBorderRight(BorderStyle.THIN);
        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setWrapText(true);
        Font font = workbook.createFont();
        font.setUnderline((byte)1);
        font.setColor(HSSFColor.BLUE.index);
        cellStyle.setFont(font);
        return cellStyle;
    }

    /**
     * 创建表头样式
     * @param workbook
     * @return
     */
    public static CellStyle createHeadCellStyle(Workbook workbook) {
        CellStyle headCellStyle = createCellStyle(workbook);
        headCellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return headCellStyle;
    }

    /**
     * 创建主键表单样式
     * @param workbook
     * @return
     */
    public static CellStyle createKeyCellStyle(Workbook workbook) {
        CellStyle headCellStyle = createCellStyle(workbook);
        headCellStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
        headCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return headCellStyle;
    }

    /**
     * 设置表头
     * @param sheet
     * @param tableName
     * @param tableComment
     * @param maxWidths
     * @param cellStyle
     * @param headCellStyle
     */
    public static void setHead(Sheet sheet,String tableName,String tableComment,Map<Integer,Integer> maxWidths,CellStyle cellStyle,CellStyle headCellStyle) {
        //设置默认列数
        sheet.setDefaultColumnWidth((short)5);
        sheet.addMergedRegion(new CellRangeAddress(0,0,0,4));
        Row row0 = sheet.createRow(0);
        Cell cel0 = row0.createCell(0);
        cel0.setCellValue(tableName+" "+tableComment);
        cel0.setCellStyle(cellStyle);
        Row row1 = sheet.createRow(1);
        //创建表头
        for(int i=0;i<NAME_LIST.size();i++){
            Cell cell = row1.createCell(i);
            cell.setCellValue(NAME_LIST.get(i));
            cell.setCellStyle(headCellStyle);
            int cellLen = cell.getStringCellValue().getBytes().length*256+200;
            maxWidths.put(i,cellLen);
        }
    }

    /**
     * 设置表单 mysql
     * @param row
     * @param column
     * @param cellStyle
     * @param maxWidths
     * @param keyCellStyle
     */
    public static void setColumnsMysql(Row row, Map column,CellStyle cellStyle,Map<Integer,Integer> maxWidths,CellStyle keyCellStyle) {
        List<Cell> cells = new ArrayList<>();
        //字段名
        Cell cell0 = row.createCell(0);
        cell0.setCellValue(column.get("COLUMN_NAME").toString());
        cells.add(cell0);
        //数据类型
        Cell cell1 = row.createCell(1);
        cell1.setCellValue(column.get("COLUMN_TYPE").toString());
        cells.add(cell1);
        //可为空
        Cell cell2 = row.createCell(2);
        cell2.setCellValue(column.get("IS_NULLABLE").toString());
        cells.add(cell2);
        //默认值
        Cell cell3 = row.createCell(3);
        cell3.setCellValue(column.get("COLUMN_DEFAULT") != null ? column.get("COLUMN_DEFAULT").toString():"");
        cells.add(cell3);
        //注释
        Cell cell4 = row.createCell(4);
        cell4.setCellValue(column.get("COLUMN_COMMENT") != null ? column.get("COLUMN_COMMENT").toString():"");
        cells.add(cell4);
        int index = 0;
        for (Cell cell : cells) {
            //主键字段则置黄
            if ("PRI".equals(column.get("COLUMN_KEY"))) {
                cell.setCellStyle(keyCellStyle);
            } else {
                cell.setCellStyle(cellStyle);
            }
            //计算表单宽度
            int cellLen = cell.getStringCellValue().getBytes().length*256+200;
            Integer maxWidth = maxWidths.get(index);
            if (cellLen > maxWidth) {
                maxWidths.put(index,cellLen);
            }
            index++;
        }
    }

    /**
     * 设置表单 oracle
     * @param row
     * @param column
     * @param cellStyle
     * @param maxWidths
     * @param columnComment
     * @param keyCellStyle
     * @param primaryKey
     */
    public static void setColumnsOracle(Row row, Map column,CellStyle cellStyle,Map<Integer,Integer> maxWidths,String columnComment,CellStyle keyCellStyle,String primaryKey) {
        List<Cell> cells = new ArrayList<>();
        //字段名
        Cell cell0 = row.createCell(0);
        cell0.setCellValue(column.get("COLUMN_NAME").toString());
        cells.add(cell0);
        //数据类型
        Cell cell1 = row.createCell(1);
        cell1.setCellValue(column.get("DATA_TYPE").toString());
        cells.add(cell1);
        //可为空
        Cell cell2 = row.createCell(2);
        cell2.setCellValue(column.get("NULLABLE").toString());
        cells.add(cell2);
        //默认值
        Cell cell3 = row.createCell(3);
        cell3.setCellValue(column.get("DATA_DEFAULT") != null ? column.get("DATA_DEFAULT").toString():"");
        cells.add(cell3);
        //注释
        Cell cell4 = row.createCell(4);
        cell4.setCellValue(columnComment);
        cells.add(cell4);
        int index = 0;
        for (Cell cell : cells) {
            //主键字段则置黄
            if (primaryKey.equals(column.get("COLUMN_NAME"))) {
                cell.setCellStyle(keyCellStyle);
            } else {
                cell.setCellStyle(cellStyle);
            }
            //计算表单宽度
            int cellLen = cell.getStringCellValue().getBytes().length*256+200;
            Integer maxWidth = maxWidths.get(index);
            if (cellLen > maxWidth) {
                maxWidths.put(index,cellLen);
            }
            index++;
        }
    }

    /**
     * 设置自适应列宽
     * @param sheet
     * @param maxWidth
     */
    public static void setAutoColumnWidth(Sheet sheet,Map<Integer,Integer> maxWidth) {
        //设置列宽
        for(int i=0;i<NAME_LIST.size();i++){
            int cellLen = maxWidth.get(i);
            if (cellLen > 15000) {
                cellLen = 15000;
            }
            sheet.setColumnWidth(i,cellLen);
        }
    }

    /**
     * 导出excel
     * @param fileName
     * @param workbook
     * @param resp
     */
    public static void exportExcel(String fileName,Workbook workbook,HttpServletResponse resp) {
        BufferedOutputStream fos = null;
        try{
            fileName =  fileName  +".xls";
            resp.setContentType("application/msexcel;charset=UTF-8");
            resp.addHeader("Content-Disposition", "attachment;filename=" + new String( fileName.getBytes("gb2312"), "ISO8859-1" ));
            fos = new BufferedOutputStream(resp.getOutputStream());
            workbook.write(fos);
        }catch (Exception e){
            e.printStackTrace();
        }finally{
            if (fos != null) {
                try {
                    fos.flush();
                    fos.close();
                    workbook.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
