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

    // 日志ID，用于跟踪识别过程
    private String logId;

    // 工程名称
    private String projectName;

    // 施工单位
    private String constructionUnit;

    // 工程地址
    private String projectAddress;

    // 施工部位
    private String constructionPart;

    // 强度等级
    private String strengthLevel;

    // 本车方量(m³)
    private String currentVolume;
    
    // 创建时间
    private LocalDateTime createdAt;
    
    // 更新时间
    private LocalDateTime updatedAt;
}