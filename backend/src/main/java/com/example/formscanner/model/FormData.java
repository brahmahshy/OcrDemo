package com.example.formscanner.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 表单数据实体类
 * 根据图片中识别的表单字段设计
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormData {

    private Long id;
    
    // 姓名
    private String name;
    
    // 身份证号
    private String idNumber;
    
    // 联系电话
    private String phoneNumber;
    
    // 住址
    private String address;
    
    // 工作单位
    private String workUnit;
    
    // 职务
    private String position;
    
    // 备注
    private String remark;
    
    // 原始图片路径
    private String imagePath;
    
    // 创建时间
    private LocalDateTime createdAt;
    
    // 更新时间
    private LocalDateTime updatedAt;
}