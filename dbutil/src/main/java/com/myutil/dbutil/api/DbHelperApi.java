package com.myutil.dbutil.api;

import com.myutil.dbutil.mysql.service.MysqlService;
import com.myutil.dbutil.oracle.service.OracleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;

/**
 * 数据库结构导出工具API
 * @author yh
 */
@Controller
public class DbHelperApi {
    private MysqlService mysqlService;
    private OracleService oracleService;

    @Autowired
    public DbHelperApi(MysqlService mysqlService,OracleService oracleService) {
        this.mysqlService = mysqlService;
        this.oracleService = oracleService;
    }

    /**
     * 导出mysql数据库的表结构
     * @param resp
     */
    @RequestMapping("/exportMysqlTablesDoc")
    @ResponseBody
    public void exportMysqlTablesDoc(HttpServletResponse resp) {
        mysqlService.exportMysqlTablesDoc(resp);
    }

    /**
     * 导出oracle数据库的表结构
     * @param resp
     */
    @RequestMapping("/exportOracleTablesDoc")
    @ResponseBody
    public void exportOracleTablesDoc(HttpServletResponse resp) {
        oracleService.exportOracleTablesDoc(resp);
    }
}
