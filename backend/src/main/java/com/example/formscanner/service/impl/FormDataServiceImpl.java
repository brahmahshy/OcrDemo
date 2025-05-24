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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 表单数据服务实现类
 * 提供表单数据的业务逻辑处理，包括OCR识别和数据处理
 */
@Slf4j
@Service
@RequiredArgsConstructor
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

            FormData formData = recognizer.getFormData(recognizedText);
            formData.setCreatedAt(LocalDateTime.now());
            formData.setUpdatedAt(LocalDateTime.now());
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
     *
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
            headerRow.createCell(1).setCellValue("工程名称");
            headerRow.createCell(2).setCellValue("施工单位");
            headerRow.createCell(3).setCellValue("工程地址");
            headerRow.createCell(4).setCellValue("施工部位");
            headerRow.createCell(5).setCellValue("强度等级");
            headerRow.createCell(6).setCellValue("本车方量(m³)");
            headerRow.createCell(8).setCellValue("创建时间");
            
            // 填充数据
            int rowNum = 1;
            for (FormData formData : formDataList) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(formData.getId());
                row.createCell(1).setCellValue(formData.getProjectName());
                row.createCell(2).setCellValue(formData.getConstructionUnit());
                row.createCell(3).setCellValue(formData.getProjectAddress());
                row.createCell(4).setCellValue(formData.getConstructionPart());
                row.createCell(5).setCellValue(formData.getStrengthLevel());
                row.createCell(6).setCellValue(formData.getCurrentVolume());
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
}