package com.example.formscanner.service.ocr.impl.tesserat;

import com.example.formscanner.service.ocr.OcrRecognizer;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Slf4j
@Service
public abstract class AbstractTesseractOcrRecognizer implements OcrRecognizer {
    @Value("${form.scanner.tesseract.data-path:./tessdata}")
    private String tesseractDataPath;

    @Value("${form.scanner.tesseract.language:chi_sim}")
    private String tesseractLanguage;

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

    protected abstract void setTessVariable(ITesseract tesseract);

    protected abstract File preprocessImage(File imageFile) throws IOException;
}
