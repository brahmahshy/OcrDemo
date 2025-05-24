package com.example.formscanner.service.ocr.impl.tesserat;

import com.example.formscanner.service.ocr.OcrRecognizer;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Tesseract OCR识别器实现
 * 使用Tesseract引擎进行OCR识别
 */
@Slf4j
//@Service
public class TesseractOcrRecognizer extends AbstractTesseractOcrRecognizer {
    @Override
    public String getRecognizerType() {
        return "tesseract";
    }

    @Override
    protected void setTessVariable(ITesseract tesseract) {
        // 设置Tesseract参数以提高中文识别率
        tesseract.setTessVariable("preserve_interword_spaces", "1");
        tesseract.setTessVariable("language_model_penalty_non_dict_word", "0.5");
        tesseract.setTessVariable("language_model_penalty_non_freq_dict_word", "0.5");

        // 设置页面分割模式为PSM_AUTO_OSD，自动检测方向和脚本
        tesseract.setPageSegMode(1);

        // 设置OCR引擎模式为OEM_LSTM_ONLY，使用LSTM神经网络
        tesseract.setOcrEngineMode(1);
    }

    /**
     * 图像预处理方法，提高OCR识别率
     * @param imageFile 原始图像文件
     * @return 预处理后的图像文件
     * @throws IOException 如果处理失败
     */
    @Override
    protected File preprocessImage(File imageFile) throws IOException {
        // 读取原始图像
        BufferedImage originalImage = ImageIO.read(imageFile);
        if (originalImage == null) {
            throw new IOException("无法读取图像文件: " + imageFile.getAbsolutePath());
        }
        
        // 获取图像尺寸
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();
        
        // 创建新的图像，用于预处理
        BufferedImage processedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = processedImage.createGraphics();
        
        // 设置渲染提示，提高质量
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // 填充白色背景
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, height);
        
        // 绘制原始图像
        g2d.drawImage(originalImage, 0, 0, null);
        g2d.dispose();
        
        // 二值化处理，增强文本与背景对比度
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = processedImage.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                
                // 计算灰度值
                int gray = (int) (0.299 * r + 0.587 * g + 0.114 * b);
                
                // 二值化处理，阈值设为127
                int newRgb;
                if (gray > 127) {
                    newRgb = Color.WHITE.getRGB();
                } else {
                    newRgb = Color.BLACK.getRGB();
                }
                
                processedImage.setRGB(x, y, newRgb);
            }
        }
        
        // 保存预处理后的图像
        String originalPath = imageFile.getAbsolutePath();
        String preprocessedPath = originalPath.substring(0, originalPath.lastIndexOf(".")) + "_preprocessed.png";
        File preprocessedFile = new File(preprocessedPath);
        ImageIO.write(processedImage, "png", preprocessedFile);
        
        return preprocessedFile;
    }
}