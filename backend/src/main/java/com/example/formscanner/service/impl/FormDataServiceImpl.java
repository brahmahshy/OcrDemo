package com.example.formscanner.service.impl;

import com.example.formscanner.model.FormData;
import com.example.formscanner.mapper.FormDataMapper;
import com.example.formscanner.service.FormDataService;
import com.example.formscanner.service.ocr.OcrRecognizer;
import com.example.formscanner.service.ocr.OcrRecognizerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;

/**
 * 表单数据服务实现类
 * 提供表单数据的业务逻辑处理，包括OCR识别和数据处理
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FormDataServiceImpl implements FormDataService {

    private final FormDataMapper formDataMapper;
    private final OcrRecognizerFactory ocrRecognizerFactory;
    
    @Value("${form.scanner.upload-dir}")
    private String uploadDir;
    
    // 默认OCR识别器类型
    @Value("${form.scanner.default-ocr-type:tesseract}")
    private String defaultOcrType;
    
    /**
     * 处理上传的表单图片
     * @param file 上传的图片文件
     * @return 处理后的表单数据
     */
    @Override
    public FormData processFormImage(MultipartFile file) {
        return processFormImage(file, defaultOcrType);
    }
    
    /**
     * 处理上传的表单图片，使用指定的OCR识别器
     * @param file 上传的图片文件
     * @param ocrType OCR识别器类型
     * @return 处理后的表单数据
     */
    public FormData processFormImage(MultipartFile file, String ocrType) {
        try {
            // 保存上传的图片
            String imagePath = saveImage(file);
            
            // 使用指定的OCR识别器识别图片内容
            OcrRecognizer recognizer = ocrRecognizerFactory.getRecognizer(ocrType);
            log.info("使用OCR识别器: {}", recognizer.getRecognizerType());
            String recognizedText = recognizer.recognizeText(new File(imagePath));
            log.info("识别到的文本内容: {}\n", recognizedText);
            
            // 解析识别的文本，提取表单字段
            Map<String, String> extractedFields = extractFormFields(recognizedText);
            
            // 创建表单数据对象
            FormData formData = FormData.builder()
                    .name(extractedFields.getOrDefault("姓名", ""))
                    .idNumber(extractedFields.getOrDefault("身份证号", ""))
                    .phoneNumber(extractedFields.getOrDefault("联系电话", ""))
                    .address(extractedFields.getOrDefault("住址", ""))
                    .workUnit(extractedFields.getOrDefault("工作单位", ""))
                    .position(extractedFields.getOrDefault("职务", ""))
                    .remark(extractedFields.getOrDefault("备注", ""))
                    .imagePath(imagePath)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            
            // 保存到数据库
            formDataMapper.insert(formData);
            return formData;
            
        } catch (Exception e) {
            log.error("处理表单图片时发生错误", e);
            throw new RuntimeException("处理表单图片失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 获取所有表单数据
     * @return 表单数据列表
     */
    @Override
    public List<FormData> getAllFormData() {
        return formDataMapper.selectAll();
    }
    
    /**
     * 根据ID获取表单数据
     * @param id 表单数据ID
     * @return 表单数据
     */
    @Override
    public Optional<FormData> getFormDataById(Long id) {
        FormData formData = formDataMapper.selectById(id);
        return Optional.ofNullable(formData);
    }
    
    /**
     * 保存表单数据
     * @param formData 表单数据
     * @return 保存后的表单数据
     */
    @Override
    public FormData saveFormData(FormData formData) {
        formData.setUpdatedAt(LocalDateTime.now());
        if (formData.getCreatedAt() == null) {
            formData.setCreatedAt(LocalDateTime.now());
        }
        formDataMapper.update(formData);
        return formData;
    }
    
    /**
     * 删除表单数据
     * @param id 表单数据ID
     */
    @Override
    public void deleteFormData(Long id) {
        formDataMapper.deleteById(id);
    }
    
    /**
     * 导出表单数据为Excel
     * @return Excel文件的字节数组
     */
    @Override
    public byte[] exportToExcel() {
        List<FormData> formDataList = formDataMapper.selectAll();
        
        try (Workbook workbook = new XSSFWorkbook(); 
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.createSheet("表单数据");
            
            // 创建表头
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("ID");
            headerRow.createCell(1).setCellValue("姓名");
            headerRow.createCell(2).setCellValue("身份证号");
            headerRow.createCell(3).setCellValue("联系电话");
            headerRow.createCell(4).setCellValue("住址");
            headerRow.createCell(5).setCellValue("工作单位");
            headerRow.createCell(6).setCellValue("职务");
            headerRow.createCell(7).setCellValue("备注");
            headerRow.createCell(8).setCellValue("创建时间");
            
            // 填充数据
            int rowNum = 1;
            for (FormData formData : formDataList) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(formData.getId());
                row.createCell(1).setCellValue(formData.getName());
                row.createCell(2).setCellValue(formData.getIdNumber());
                row.createCell(3).setCellValue(formData.getPhoneNumber());
                row.createCell(4).setCellValue(formData.getAddress());
                row.createCell(5).setCellValue(formData.getWorkUnit());
                row.createCell(6).setCellValue(formData.getPosition());
                row.createCell(7).setCellValue(formData.getRemark());
                row.createCell(8).setCellValue(formData.getCreatedAt().toString());
            }
            
            // 自动调整列宽
            for (int i = 0; i < 9; i++) {
                sheet.autoSizeColumn(i);
            }
            
            workbook.write(outputStream);
            return outputStream.toByteArray();
            
        } catch (IOException e) {
            log.error("导出Excel时发生错误", e);
            throw new RuntimeException("导出Excel失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 保存上传的图片
     * @param file 上传的图片文件
     * @return 保存后的图片路径
     * @throws IOException 如果保存失败
     */
    private String saveImage(MultipartFile file) throws IOException {
        // 创建上传目录（如果不存在）
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
        String filename = UUID.randomUUID() + fileExtension;
        
        // 保存文件
        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath);
        
        return filePath.toString();
    }
    
    /**
     * 获取所有可用的OCR识别器类型
     * @return OCR识别器类型列表
     */
    public List<String> getAvailableOcrTypes() {
        return ocrRecognizerFactory.getAvailableRecognizerTypes();
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