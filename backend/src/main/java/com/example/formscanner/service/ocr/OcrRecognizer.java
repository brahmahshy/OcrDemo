package com.example.formscanner.service.ocr;

import java.io.File;
import java.io.IOException;

/**
 * OCR识别器接口
 * 定义OCR识别的通用方法，支持不同的OCR实现策略
 */
public interface OcrRecognizer {
    
    /**
     * 识别图片中的文本
     * @param imageFile 图片文件
     * @return 识别到的文本
     * @throws IOException 如果处理失败
     */
    String recognizeText(File imageFile) throws IOException;
    
    /**
     * 获取识别器类型名称
     * @return 识别器类型名称
     */
    String getRecognizerType();
}