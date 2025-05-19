package com.example.formscanner.service;

import com.example.formscanner.model.FormData;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

/**
 * 表单数据服务接口
 * 提供表单数据的业务逻辑处理
 */
public interface FormDataService {
    
    /**
     * 处理上传的表单图片
     * @param file 上传的图片文件
     * @return 处理后的表单数据
     */
    FormData processFormImage(MultipartFile file);
    
    /**
     * 获取所有表单数据
     * @return 表单数据列表
     */
    List<FormData> getAllFormData();
    
    /**
     * 根据ID获取表单数据
     * @param id 表单数据ID
     * @return 表单数据
     */
    Optional<FormData> getFormDataById(Long id);
    
    /**
     * 保存表单数据
     * @param formData 表单数据
     * @return 保存后的表单数据
     */
    FormData saveFormData(FormData formData);
    
    /**
     * 删除表单数据
     * @param id 表单数据ID
     */
    void deleteFormData(Long id);
    
    /**
     * 导出表单数据为Excel
     * @return Excel文件的字节数组
     */
    byte[] exportToExcel();
}