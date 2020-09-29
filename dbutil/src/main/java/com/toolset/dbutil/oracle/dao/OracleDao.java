package com.toolset.dbutil.oracle.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * @author yh
 */
@Mapper
public interface OracleDao {

    /**
     * 获取对应域的所有表
     * @return
     */
    @Select("SELECT * FROM user_tables")
    List<Map> getAllTables();

    /**
     * 获取表注释
     * @param tableName
     * @return
     */
    @Select("SELECT COMMENTS FROM user_tab_comments WHERE TABLE_NAME =#{tableName}")
    String getTableComment(@Param("tableName")String tableName);

    /**
     * 获取对应表的所有字段
     * @param tableName
     * @return
     */
    @Select("SELECT * FROM user_tab_columns WHERE TABLE_NAME =#{tableName}")
    List<Map> getAllColumnForTable(@Param("tableName")String tableName);

    /**
     * 获取字段注释
     * @param tableName
     * @param columnName
     * @return
     */
    @Select("SELECT COMMENTS FROM user_col_comments WHERE TABLE_NAME =#{tableName} AND COLUMN_NAME =#{columnName}")
    String getColumnComment(@Param("tableName")String tableName,@Param("tableName")String columnName);

    /**
     * 获取对应表的主键
     * @param tableName
     * @return
     */
    @Select("SELECT b.COLUMN_NAME FROM user_constraints a,user_cons_columns b " +
            "WHERE a.CONSTRAINT_NAME = b.CONSTRAINT_NAME AND a.CONSTRAINT_TYPE = 'P' AND b.TABLE_NAME =#{tableName}")
    String getPrimaryKey(@Param("tableName")String tableName);
}
