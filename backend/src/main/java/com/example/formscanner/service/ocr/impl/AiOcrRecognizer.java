package com.example.formscanner.service.ocr.impl;

import com.example.formscanner.model.FormData;
import com.example.formscanner.service.ocr.OcrRecognizer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * AI OCR识别器实现
 * 使用免费的AI OCR API进行文本识别
 */
@Slf4j
//@Service
public class AiOcrRecognizer implements OcrRecognizer {

    private final RestTemplate restTemplate = new RestTemplate();
    
    // 可以配置不同的AI OCR API地址
    @Value("${form.scanner.ai-ocr.api-url:https://api.ocr.space/parse/image}")
    private String apiUrl;
    
    // API密钥，可以使用免费的OCR.space API
    @Value("${form.scanner.ai-ocr.api-key:}")
    private String apiKey;

    @Override
    public String getRecognizerType() {
        return "ai_ocr";
    }

    @Override
    public String recognizeText(File imageFile) throws IOException {
        try {
            // 如果没有配置API密钥，提示用户需要配置
            if (apiKey == null || apiKey.isEmpty()) {
                log.warn("未配置AI OCR API密钥，请在配置文件中设置form.scanner.ai-ocr.api-key属性");
                return "请配置AI OCR API密钥";
            }

            // 将图像转换为Base64编码
            String base64Image = encodeImageToBase64(imageFile);

            // 准备请求参数
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("apikey", apiKey);
            map.add("base64Image", "data:image/png;base64," + base64Image);
            map.add("language", "chs"); // 中文简体
            map.add("isOverlayRequired", "false");
            map.add("detectOrientation", "true");
            map.add("scale", "true");
            map.add("OCREngine", "2"); // 使用更高级的OCR引擎

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

            // 发送请求到OCR API
            ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, request, Map.class);

            // 解析响应
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                if (responseBody.containsKey("ParsedResults") && responseBody.get("ParsedResults") != null) {
                    Object[] parsedResults = (Object[]) responseBody.get("ParsedResults");
                    if (parsedResults.length > 0) {
                        Map<String, Object> firstResult = (Map<String, Object>) parsedResults[0];
                        if (firstResult.containsKey("ParsedText")) {
                            return (String) firstResult.get("ParsedText");
                        }
                    }
                }
                log.error("AI OCR API返回格式不正确: {}", responseBody);
                return "";
            } else {
                log.error("AI OCR API请求失败: {}", response.getStatusCode());
                return "";
            }
        } catch (Exception e) {
            log.error("AI OCR识别失败", e);
            return "";
        }
    }

    /**
     * 将图像文件编码为Base64字符串
     * @param imageFile 图像文件
     * @return Base64编码的字符串
     * @throws IOException 如果编码失败
     */
    private String encodeImageToBase64(File imageFile) throws IOException {
        BufferedImage image = ImageIO.read(imageFile);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.getEncoder().encodeToString(imageBytes);
    }

    @Override
    public FormData getFormData(String recognizedText) {
        return null;
    }
}