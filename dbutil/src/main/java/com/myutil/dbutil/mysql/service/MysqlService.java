package com.myutil.dbutil.mysql.service;

import com.myutil.dbutil.mysql.dao.MysqlDao;
import com.myutil.dbutil.util.PoiUtil;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
}
