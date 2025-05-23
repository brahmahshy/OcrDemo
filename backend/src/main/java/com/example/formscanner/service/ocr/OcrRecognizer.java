package com.example.formscanner.service.ocr;

import com.example.formscanner.model.FormData;

import java.io.File;
import java.io.IOException;

/**
 * OCR识别器接口
 * 定义OCR识别的通用方法，支持不同的OCR实现策略
 */
public interface OcrRecognizer {
    /**
     * 获取识别器类型名称
     *
     * @return 识别器类型名称
     */
    String getRecognizerType();

    /**
     * 识别图片中的文本
     *
     * @param imageFile 图片文件
     * @return 识别到的文本
     * @throws IOException 如果处理失败
     */
    String recognizeText(File imageFile) throws IOException;

    /**
     * 从识别到的文本中提取表单字段，并插入到数据库中
     *
     * @param recognizedText 识别到的文本
     * @return 插入数据库后的表单数据
     */
    FormData getFormData(String recognizedText);
}