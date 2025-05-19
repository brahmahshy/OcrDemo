# 表单扫描识别系统

## 项目介绍

本系统是一个基于OCR技术的表单扫描识别系统，可以从上传的表单图片中自动识别并提取关键信息，如姓名、身份证号、联系电话等字段。系统支持多种OCR识别方式，用户可以根据需要选择不同的识别器以获得最佳识别效果。

## OCR识别器说明

系统目前支持以下几种OCR识别方式：

1. **Tesseract OCR**：基础的Tesseract识别器，使用开源的Tesseract引擎进行文字识别。
2. **增强版Tesseract OCR**：在基础Tesseract的基础上，增加了更多的图像预处理技术，如自适应二值化、锐化等，以提高识别精度。
3. **AI OCR识别**：使用第三方AI OCR API进行识别，需要配置API密钥。

## 配置说明

### OCR配置

在`application-ocr.yml`文件中可以配置OCR相关参数：

```yaml
form:
  scanner:
    # 默认OCR识别器类型
    default-ocr-type: tesseract
    
    # Tesseract OCR配置
    tesseract:
      # Tesseract数据路径
      data-path: ./tessdata
      # 识别语言
      language: chi_sim
    
    # AI OCR配置
    ai-ocr:
      # OCR.space API地址
      api-url: https://api.ocr.space/parse/image
      # OCR.space API密钥
      api-key: 您的API密钥
```

### 使用AI OCR

如果要使用AI OCR识别，需要：

1. 在[OCR.space](https://ocr.space/)注册并获取免费API密钥
2. 将API密钥填入`application-ocr.yml`文件的`form.scanner.ai-ocr.api-key`配置项

## 使用说明

1. 在前端页面上传表单图片时，可以从下拉菜单中选择不同的OCR识别方式
2. 系统会根据选择的识别方式处理图片并返回识别结果
3. 可以根据识别效果，选择最适合您的表单类型的识别方式

## 开发说明

系统使用了策略模式设计OCR识别器，如果需要添加新的识别方式，只需：

1. 实现`OcrRecognizer`接口
2. 将新的识别器类添加`@Component`注解以便自动注册
3. 实现`recognizeText`和`getRecognizerType`方法

新的识别器会自动被系统发现并可在前端选择。

## 注意事项

1. Tesseract OCR需要安装对应的语言包，默认使用中文简体(chi_sim)
2. AI OCR识别需要网络连接和有效的API密钥
3. 不同的识别方式对图片质量和格式有不同的要求，请根据实际情况选择# OcrDemo
