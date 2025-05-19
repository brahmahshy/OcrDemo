package com.example.formscanner.controller;

import com.example.formscanner.model.FormData;
import com.example.formscanner.service.FormDataService;
import com.example.formscanner.service.impl.FormDataServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 表单数据控制器
 * 提供表单数据的HTTP接口
 */
@RestController
@RequestMapping("/forms")
@RequiredArgsConstructor
@Slf4j
public class FormDataController {

    private final FormDataServiceImpl formDataService;
    
    /**
     * 上传并处理表单图片
     * @param file 上传的图片文件
     * @param ocrType OCR识别器类型（可选）
     * @return 处理后的表单数据
     */
    @PostMapping("/upload")
    public ResponseEntity<FormData> uploadFormImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "ocrType", required = false) String ocrType) {
        log.info("接收到表单图片上传请求，文件名: {}, OCR类型: {}", file.getOriginalFilename(), ocrType);
        FormData formData;
        if (ocrType != null && !ocrType.isEmpty()) {
            formData = formDataService.processFormImage(file, ocrType);
        } else {
            formData = formDataService.processFormImage(file);
        }
        return ResponseEntity.ok(formData);
    }
    
    /**
     * 获取所有表单数据
     * @return 表单数据列表
     */
    @GetMapping
    public ResponseEntity<List<FormData>> getAllFormData() {
        List<FormData> formDataList = formDataService.getAllFormData();
        return ResponseEntity.ok(formDataList);
    }
    
    /**
     * 根据ID获取表单数据
     * @param id 表单数据ID
     * @return 表单数据
     */
    @GetMapping("/{id}")
    public ResponseEntity<FormData> getFormDataById(@PathVariable Long id) {
        return formDataService.getFormDataById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 更新表单数据
     * @param id 表单数据ID
     * @param formData 更新的表单数据
     * @return 更新后的表单数据
     */
    @PutMapping("/{id}")
    public ResponseEntity<FormData> updateFormData(@PathVariable Long id, @RequestBody FormData formData) {
        return formDataService.getFormDataById(id)
                .map(existingFormData -> {
                    formData.setId(id);
                    formData.setImagePath(existingFormData.getImagePath());
                    formData.setCreatedAt(existingFormData.getCreatedAt());
                    return ResponseEntity.ok(formDataService.saveFormData(formData));
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 删除表单数据
     * @param id 表单数据ID
     * @return 响应状态
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFormData(@PathVariable Long id) {
        return formDataService.getFormDataById(id)
                .map(formData -> {
                    formDataService.deleteFormData(id);
                    return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 导出表单数据为Excel
     * @return Excel文件
     */
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportToExcel() {
        byte[] excelBytes = formDataService.exportToExcel();
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "form_data.xlsx");
        
        return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
    }
    
    /**
     * 获取所有可用的OCR识别器类型
     * @return OCR识别器类型列表
     */
    @GetMapping("/ocr-types")
    public ResponseEntity<Map<String, Object>> getAvailableOcrTypes() {
        List<String> ocrTypes = formDataService.getAvailableOcrTypes();
        Map<String, Object> response = new HashMap<>();
        response.put("ocrTypes", ocrTypes);
        return ResponseEntity.ok(response);
    }
}