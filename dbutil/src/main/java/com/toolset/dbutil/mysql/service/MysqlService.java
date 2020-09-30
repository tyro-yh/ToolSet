package com.toolset.dbutil.mysql.service;

import com.toolset.dbutil.mysql.dao.MysqlDao;
import com.toolset.dbutil.mysql.entity.Explain;
import com.toolset.dbutil.mysql.entity.WarnExplain;
import com.toolset.dbutil.util.PoiUtil;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

/**
 * mysql工具集实现类
 * @author yh
 */
@Service
public class MysqlService {
    @Value("${TableSchema}")
    private String tableSchema;

    @Resource
    private MysqlDao mysqlDao;

    /**
     * mysql数据结构导出实现方法
     * @param resp
     */
    public void exportMysqlTablesDoc(HttpServletResponse resp) {
        //获取所有表
        List<Map> tables = mysqlDao.getAllTables(tableSchema);
        //创建poi工作空间
        Workbook workbook = new HSSFWorkbook();
        CreationHelper creationHelper = workbook.getCreationHelper();
        CellStyle cellStyle = PoiUtil.createCellStyle(workbook);
        CellStyle headCellStyle = PoiUtil.createHeadCellStyle(workbook);
        CellStyle keyCellStyle = PoiUtil.createKeyCellStyle(workbook);
        CellStyle linkCellStyle = PoiUtil.createLinkCellStyle(workbook);
        String fileName = tableSchema+"库表结构";
        try {
            //创建汇总页
            Sheet sheetCoolect = workbook.createSheet("汇总页");
            for (Map table : tables) {
                //获取对应表的所有字段
                List<Map> columns = mysqlDao.getAllColumnForTable(tableSchema,table.get("TABLE_NAME").toString());
                //创建sheet页
                Sheet sheet = workbook.createSheet(table.get("TABLE_NAME").toString());
                String tableComment = table.get("TABLE_COMMENT") != null ? table.get("TABLE_COMMENT").toString():"";
                Map<Integer,Integer> maxWidth = new HashMap<Integer,Integer>(5) {{
                    put(0,15000);
                    put(1,15000);
                    put(2,15000);
                    put(3,15000);
                    put(4,15000);
                }};
                //设置表头
                PoiUtil.setHead(sheet,table.get("TABLE_NAME").toString(),tableComment,maxWidth,cellStyle,headCellStyle);
                int index = 1;
                for(Map column : columns) {
                    index++;
                    Row row = sheet.createRow(index);
                    //设置表单
                    PoiUtil.setColumnsMysql(row,column,cellStyle,maxWidth,keyCellStyle);
                }

                //设置返回按钮
                Row row = sheet.createRow(index+2);
                Cell cellBack = row.createCell(0);
                cellBack.setCellValue("返回");
                cellBack.setCellStyle(linkCellStyle);
                Hyperlink hyperlink = creationHelper.createHyperlink(HyperlinkType.DOCUMENT);
                hyperlink.setAddress("#汇总页!A1");
                cellBack.setHyperlink(hyperlink);

                //设置列宽
                PoiUtil.setAutoColumnWidth(sheet,maxWidth);
            }
            //设置汇总页超链接
            for (int i=1;i<workbook.getNumberOfSheets();i++) {
                String sheetName = workbook.getSheetName(i);
                Row row = sheetCoolect.createRow(i-1);
                Cell cel0 = row.createCell(0);
                cel0.setCellValue(sheetName);
                cel0.setCellStyle(linkCellStyle);
                Hyperlink hyperlink = creationHelper.createHyperlink(HyperlinkType.DOCUMENT);
                hyperlink.setAddress("#"+sheetName+"!A1");
                cel0.setHyperlink(hyperlink);
            }
            sheetCoolect.setColumnWidth(0,15000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        PoiUtil.exportExcel(fileName,workbook,resp);
    }

    /**
     * mysql导出数据分析并提供改进建议
     * @param sql
     * @param resp
     */
    public void exportSqlAnalyse(String sql,HttpServletResponse resp) {
        if (StringUtils.isEmpty(sql)) {
            throw new RuntimeException("sql不能为空");
        }
        Workbook workbook = new HSSFWorkbook();
        String fileName = "SQL执行计划过程";
        CellStyle cellStyle = PoiUtil.createCellStyle(workbook);
        CellStyle headCellStyle = PoiUtil.createHeadCellStyle(workbook);
        try {
            Sheet sheet = workbook.createSheet("执行计划");
            Row row0 = sheet.createRow(0);
            Map<Integer, Integer> maxWidths = new HashMap<Integer, Integer>(9) {{
                put(0, 15000);put(1, 15000);put(2, 15000);
                put(3, 15000);put(4, 15000);put(5, 15000);
                put(6, 15000);put(7, 15000);put(8, 15000);
            }};
            List<String> nameList = new ArrayList<String>(9) {{
                add("id");add("select_type");add("table");
                add("type");add("possible_keys");add("key");
                add("rows");add("Extra");add("备注");
            }};
            //创建表头
            for (int i = 0; i < nameList.size(); i++) {
                Cell cell = row0.createCell(i);
                cell.setCellValue(nameList.get(i));
                cell.setCellStyle(headCellStyle);
                int cellLen = cell.getStringCellValue().getBytes().length * 256 + 200;
                maxWidths.put(i, cellLen);
            }
            List<Explain> list = mysqlDao.getExplain(sql);
            int i = 0;
            for (Explain explain : list) {
                i++;
                Row row = sheet.createRow(i);
                List<Cell> cells = new ArrayList<>();
                int j = 0;
                for (Field field : explain.getClass().getDeclaredFields()) {
                    Method m = explain.getClass().getMethod("get"+field.getName().substring(0,1).toUpperCase()+field.getName().substring(1));
                    Cell cellj = row.createCell(j);
                    cellj.setCellValue(m.invoke(explain).toString());
                    cells.add(cellj);
                }
                StringBuffer text = sqlAnalyseHandler(explain);
                if (!StringUtils.isEmpty(text)) {
                    Cell cell8 = row.createCell(8);
                    cell8.setCellValue(text.toString());
                    cells.add(cell8);
                }
                int index = 0;
                for (Cell cell : cells) {
                    cell.setCellStyle(cellStyle);
                    //计算表单宽度
                    int cellLen = cell.getStringCellValue().getBytes().length * 256 + 200;
                    Integer maxWidth = maxWidths.get(index);
                    if (cellLen > maxWidth) {
                        maxWidths.put(index, cellLen);
                    }
                    index++;
                }
            }
            //设置列宽
            PoiUtil.setAutoColumnWidth(sheet, maxWidths);
        } catch (Exception e) {
            e.printStackTrace();
        }
        PoiUtil.exportExcel(fileName,workbook,resp);
    }

    /**
     * sql执行计划分析
     * @param explain
     * @return
     */
    private StringBuffer sqlAnalyseHandler(Explain explain) {
        StringBuffer text = new StringBuffer();
        if (WarnExplain.ALL.getCode().equals(explain.getType())) {
            text.append(WarnExplain.ALL.getMessage());
        }
        if (WarnExplain.index.getCode().equals(explain.getType())) {
            text.append(WarnExplain.ALL.getMessage());
        }
        if (StringUtils.isEmpty(explain.getPossibleKeys())) {
            text.append("T1-条件列没有相关索引 建议优化\n");
        }
        if (!StringUtils.isEmpty(explain.getExtra())) {
            List<String> extras = Arrays.asList(explain.getExtra().split(";"));
            if (extras.contains(WarnExplain.join.getCode())) {
                text.append(WarnExplain.join.getMessage());
            }
        }
        return text;
    }
}
