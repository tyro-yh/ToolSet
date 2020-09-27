package com.myutil.dbutil.oracle.service;

import com.myutil.dbutil.oracle.dao.OracleDao;
import com.myutil.dbutil.util.PoiUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * oracle工具集实现类
 * @author yh
 */
@Service
public class OracleService {
    @Value("${TableSchema}")
    private String tableSchema;

    @Resource
    private OracleDao oracleDao;

    /**
     * oracle数据结构导出实现方法
     * @param resp
     */
    public void exportOracleTablesDoc(HttpServletResponse resp) {
        //获取所有表
        List<Map> tables = oracleDao.getAllTables();
        //创建poi工作空间
        Workbook workbook = new HSSFWorkbook();
        CellStyle cellStyle = PoiUtil.createCellStyle(workbook);
        CellStyle headCellStyle = PoiUtil.createHeadCellStyle(workbook);
        CellStyle keyCellStyle = PoiUtil.createKeyCellStyle(workbook);
        String fileName = tableSchema+"库表结构";
        try {
            for (Map table : tables) {
                //获取对应表的所有字段
                List<Map> columns = oracleDao.getAllColumnForTable(table.get("TABLE_NAME").toString());
                //获取对应表的主键
                String primaryKey = oracleDao.getPrimaryKey(table.get("TABLE_NAME").toString());
                //创建sheet页
                Sheet sheet = workbook.createSheet(table.get("TABLE_NAME").toString());
                String tableComment = oracleDao.getTableComment(table.get("TABLE_NAME").toString());
                Map<Integer,Integer> maxWidth = new HashMap<>(5);
                //设置表头
                PoiUtil.setHead(sheet,table.get("TABLE_NAME").toString(),tableComment,maxWidth,cellStyle,headCellStyle);
                int index = 1;
                for(Map column : columns) {
                    index++;
                    Row row = sheet.createRow(index);
                    String columnComment = oracleDao.getColumnComment(table.get("TABLE_NAME").toString(),column.get("COLUMN_NAME").toString());
                    //设置表单
                    PoiUtil.setColumnsOracle(row,column,cellStyle,maxWidth,columnComment,keyCellStyle,primaryKey);
                }

                //设置列宽
                PoiUtil.setAutoColumnWidth(sheet,maxWidth);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        PoiUtil.exportExcel(fileName,workbook,resp);
    }
}
