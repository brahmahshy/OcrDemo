package com.example.formscanner.mapper;

import com.example.formscanner.model.FormData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 表单数据Mapper接口
 * 提供表单数据的CRUD操作
 */
@Mapper
public interface FormDataMapper {
    
    /**
     * 插入表单数据
     * @param formData 表单数据
     * @return 影响的行数
     */
    int insert(FormData formData);
    
    /**
     * 更新表单数据
     * @param formData 表单数据
     * @return 影响的行数
     */
    int update(FormData formData);
    
    /**
     * 根据ID删除表单数据
     * @param id 表单数据ID
     * @return 影响的行数
     */
    int deleteById(@Param("id") Long id);
    
    /**
     * 根据ID查询表单数据
     * @param id 表单数据ID
     * @return 表单数据
     */
    FormData selectById(@Param("id") Long id);
    
    /**
     * 查询所有表单数据
     * @return 表单数据列表
     */
    List<FormData> selectAll();
}