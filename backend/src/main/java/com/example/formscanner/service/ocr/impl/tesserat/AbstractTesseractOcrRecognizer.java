package com.example.formscanner.service.ocr.impl.tesserat;

import com.example.formscanner.mapper.FormDataMapper;
import com.example.formscanner.model.FormData;
import com.example.formscanner.service.ocr.OcrRecognizer;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
//@Service
public abstract class AbstractTesseractOcrRecognizer implements OcrRecognizer {
    @Resource
    private FormDataMapper formDataMapper;

    @Value("${form.scanner.tesseract.data-path:./tessdata}")
    private String tesseractDataPath;

    @Value("${form.scanner.tesseract.language:chi_sim}")
    private String tesseractLanguage;

    protected abstract void setTessVariable(ITesseract tesseract);

    protected abstract File preprocessImage(File imageFile) throws IOException;

    @Override
    public String recognizeText(File imageFile) throws IOException {
        ITesseract tesseract = new Tesseract();
        tesseract.setDatapath(tesseractDataPath);
        tesseract.setLanguage(tesseractLanguage);

        this.setTessVariable(tesseract);

        try {
            // 对图像进行预处理后再识别
            File preprocessedImage = preprocessImage(imageFile);
            return tesseract.doOCR(preprocessedImage);
        } catch (TesseractException e) {
            log.error("{} OCR识别失败", getRecognizerType(), e);
            return "";
        }
    }

    @Override
    public FormData getFormData(String recognizedText) {
        // 解析识别的文本，提取表单字段
        Map<String, String> extractedFields = extractFormFields(recognizedText);

        // 创建表单数据对象
        FormData formData = FormData.builder()
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // 保存到数据库
        return formData;
    }

    /**
     * 从识别的文本中提取表单字段
     * @param text 识别到的文本
     * @return 提取的字段映射
     */
    private Map<String, String> extractFormFields(String text) {
        Map<String, String> fields = new HashMap<>();

        log.info("开始提取表单字段，识别文本长度: {}", text.length());

        // 首先尝试使用表格式提取（根据图片中的表格结构）
        extractFromTableFormat(text, fields);

        // 如果表格式提取没有找到所有字段，尝试使用正则表达式提取
        if (fields.size() < 7) {
            // 定义要提取的字段和对应的正则表达式
            Map<String, Pattern> patterns = new HashMap<>();
            patterns.put("姓名", Pattern.compile("姓[\\s]*名[：:：\\s]*([^\\n\\r]+)"));
            patterns.put("身份证号", Pattern.compile("身[\\s]*份[\\s]*证[\\s]*号[：:：\\s]*([^\\n\\r]+)"));
            patterns.put("联系电话", Pattern.compile("联[\\s]*系[\\s]*电[\\s]*话[：:：\\s]*([^\\n\\r]+)"));
            patterns.put("住址", Pattern.compile("住[\\s]*址[：:：\\s]*([^\\n\\r]+)"));
            patterns.put("工作单位", Pattern.compile("工[\\s]*作[\\s]*单[\\s]*位[：:：\\s]*([^\\n\\r]+)"));
            patterns.put("职务", Pattern.compile("职[\\s]*务[：:：\\s]*([^\\n\\r]+)"));
            patterns.put("备注", Pattern.compile("备[\\s]*注[：:：\\s]*([^\\n\\r]+)"));

            // 提取字段
            for (Map.Entry<String, Pattern> entry : patterns.entrySet()) {
                String fieldName = entry.getKey();
                if (!fields.containsKey(fieldName)) {
                    Matcher matcher = entry.getValue().matcher(text);
                    if (matcher.find()) {
                        fields.put(fieldName, matcher.group(1).trim());
                    }
                }
            }
        }

        // 特殊处理：尝试直接从文本中提取身份证号和电话号码
        if (!fields.containsKey("身份证号")) {
            Pattern idPattern = Pattern.compile("\\d{17}[0-9Xx]|\\d{15}");
            Matcher idMatcher = idPattern.matcher(text);
            if (idMatcher.find()) {
                fields.put("身份证号", idMatcher.group().trim());
            }
        }

        if (!fields.containsKey("联系电话")) {
            Pattern phonePattern = Pattern.compile("\\d{11}|\\d{3}[\\s-]?\\d{4}[\\s-]?\\d{4}");
            Matcher phoneMatcher = phonePattern.matcher(text);
            if (phoneMatcher.find()) {
                fields.put("联系电话", phoneMatcher.group().replaceAll("[\\s-]", "").trim());
            }
        }

        // 记录提取结果
        log.info("表单字段提取结果: {}", fields);

        return fields;
    }

    /**
     * 从表格式文本中提取字段
     * @param text 识别到的文本
     * @param fields 提取的字段映射
     */


    private void extractFromTableFormat(String text, Map<String, String> fields) {
        // 根据图片中的表格结构，尝试提取字段
        String[] lines = text.split("\\n");

        // 存储可能的字段名和值的行
        List<String> fieldLines = new ArrayList<>();
        for (String line : lines) {
            if (line.trim().length() > 3) { // 忽略太短的行
                fieldLines.add(line.trim());
            }
        }

        // 尝试识别表格结构
        Map<String, String> fieldMapping = new HashMap<>();
        fieldMapping.put("姓名", "姓[\\s]*名");
        fieldMapping.put("身份证号", "身[\\s]*份[\\s]*证[\\s]*号");
        fieldMapping.put("联系电话", "联[\\s]*系[\\s]*电[\\s]*话");
        fieldMapping.put("住址", "住[\\s]*址");
        fieldMapping.put("工作单位", "工[\\s]*作[\\s]*单[\\s]*位");
        fieldMapping.put("职务", "职[\\s]*务");
        fieldMapping.put("备注", "备[\\s]*注");

        // 遍历所有行，查找字段名和对应的值
        for (int i = 0; i < fieldLines.size(); i++) {
            String line = fieldLines.get(i);

            // 检查行是否包含字段名
            for (Map.Entry<String, String> entry : fieldMapping.entrySet()) {
                String fieldName = entry.getKey();
                String fieldPattern = entry.getValue();

                if (Pattern.compile(fieldPattern).matcher(line).find() && !fields.containsKey(fieldName)) {
                    // 尝试从当前行提取值
                    extractFieldFromLine(line, fieldName, fields);

                    // 如果当前行没有提取到值，尝试从下一行提取
                    if (!fields.containsKey(fieldName) && i + 1 < fieldLines.size()) {
                        String nextLine = fieldLines.get(i + 1);
                        // 如果下一行不包含其他字段名，可能是当前字段的值
                        boolean containsOtherField = false;
                        for (String pattern : fieldMapping.values()) {
                            if (Pattern.compile(pattern).matcher(nextLine).find()) {
                                containsOtherField = true;
                                break;
                            }
                        }

                        if (!containsOtherField) {
                            fields.put(fieldName, nextLine.trim());
                        }
                    }
                }
            }
        }

        // 特殊处理：尝试识别表格中的数据
        // 根据图片中的表格格式，可能需要特殊处理某些字段
        for (String line : fieldLines) {
            // 尝试识别身份证号（通常是15-18位数字，可能包含X）
            if (!fields.containsKey("身份证号")) {
                Pattern idPattern = Pattern.compile("\\d{15}|\\d{17}[0-9Xx]");
                Matcher idMatcher = idPattern.matcher(line);
                if (idMatcher.find()) {
                    fields.put("身份证号", idMatcher.group().trim());
                }
            }

            // 尝试识别电话号码（通常是11位数字）
            if (!fields.containsKey("联系电话")) {
                Pattern phonePattern = Pattern.compile("\\d{11}|\\d{3}[\\s-]?\\d{4}[\\s-]?\\d{4}");
                Matcher phoneMatcher = phonePattern.matcher(line);
                if (phoneMatcher.find()) {
                    fields.put("联系电话", phoneMatcher.group().replaceAll("[\\s-]", "").trim());
                }
            }
        }
    }

    /**
     * 从行中提取字段值
     * @param line 文本行
     * @param fieldName 字段名
     * @param fields 提取的字段映射
     */
    private void extractFieldFromLine(String line, String fieldName, Map<String, String> fields) {
        // 尝试提取字段值，假设字段名和值之间有分隔符（如空格、制表符、冒号等）
        int index = -1;

        // 尝试不同的模式查找字段名位置
        Pattern fieldPattern = Pattern.compile(fieldName.replaceAll(".", "[\\s]*$0"));
        Matcher fieldMatcher = fieldPattern.matcher(line);
        if (fieldMatcher.find()) {
            index = fieldMatcher.end();
        } else {
            index = line.indexOf(fieldName);
            if (index < 0) {
                // 尝试查找没有空格的字段名
                index = line.indexOf(fieldName.replaceAll("\\s", ""));
            }
        }

        if (index >= 0) {
            // 提取字段名后面的内容作为值
            String value = line.substring(index).trim();

            // 移除可能的分隔符（冒号、空格等）
            value = value.replaceAll("^[：:：\\s]+", "").trim();

            // 如果值不为空，添加到字段映射中
            if (!value.isEmpty()) {
                fields.put(fieldName, value);
            }
        }
    }
}
