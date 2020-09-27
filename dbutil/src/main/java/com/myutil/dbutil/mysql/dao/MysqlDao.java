package com.myutil.dbutil.mysql.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * @author yh
 */
@Mapper
public interface MysqlDao {

    /**
     * 获取对应域的所有表
     * @param tableSchema
     * @return
     */
    @Select("SELECT * FROM information_schema.TABLES WHERE TABLE_SCHEMA =#{tableSchema}")
    List<Map> getAllTables(@Param("tableSchema") String tableSchema);

    /**
     * 获取对应表的所有字段
     * @param tableSchema
     * @param tableName
     * @return
     */
    @Select("SELECT * FROM information_schema.COLUMNS WHERE TABLE_SCHEMA =#{tableSchema} AND TABLE_NAME =#{tableName}")
    List<Map> getAllColumnForTable(@Param("tableSchema")String tableSchema,@Param("tableName")String tableName);
}
