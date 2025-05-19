package com.example.formscanner.service.ocr;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * OCR识别器工厂
 * 负责管理和提供不同类型的OCR识别器
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OcrRecognizerFactory {

    private final List<OcrRecognizer> recognizers;
    private Map<String, OcrRecognizer> recognizerMap;
    
    /**
     * 初始化识别器映射
     */
    private void initRecognizerMap() {
        if (recognizerMap == null) {
            recognizerMap = recognizers.stream()
                    .collect(Collectors.toMap(
                            OcrRecognizer::getRecognizerType,
                            Function.identity()
                    ));
            log.info("已加载 {} 个OCR识别器: {}", recognizerMap.size(), 
                    recognizerMap.keySet().stream().collect(Collectors.joining(", ")));
        }
    }
    
    /**
     * 获取指定类型的OCR识别器
     * @param type 识别器类型
     * @return OCR识别器实例
     */
    public OcrRecognizer getRecognizer(String type) {
        initRecognizerMap();
        OcrRecognizer recognizer = recognizerMap.get(type);
        if (recognizer == null) {
            log.warn("未找到类型为 '{}' 的OCR识别器，将使用默认识别器", type);
            // 默认使用Tesseract识别器
            recognizer = recognizerMap.get("tesseract");
        }
        return recognizer;
    }
    
    /**
     * 获取所有可用的OCR识别器类型
     * @return 识别器类型列表
     */
    public List<String> getAvailableRecognizerTypes() {
        initRecognizerMap();
        return recognizerMap.keySet().stream().collect(Collectors.toList());
    }
}