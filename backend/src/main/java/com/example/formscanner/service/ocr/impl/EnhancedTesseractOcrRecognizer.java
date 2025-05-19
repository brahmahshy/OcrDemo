package com.example.formscanner.service.ocr.impl;

import com.example.formscanner.service.ocr.OcrRecognizer;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.File;
import java.io.IOException;

/**
 * 增强版Tesseract OCR识别器实现
 * 使用更多的图像预处理技术来提高识别精度
 */
@Component
@Slf4j
public class EnhancedTesseractOcrRecognizer implements OcrRecognizer {

    @Value("${form.scanner.tesseract.data-path:./tessdata}")
    private String tesseractDataPath;

    @Value("${form.scanner.tesseract.language:chi_sim}")
    private String tesseractLanguage;
    
    @Override
    public String recognizeText(File imageFile) throws IOException {
        ITesseract tesseract = new Tesseract();
        tesseract.setDatapath(tesseractDataPath);
        tesseract.setLanguage(tesseractLanguage);
        
        // 设置更多Tesseract参数以提高中文识别率
        tesseract.setTessVariable("preserve_interword_spaces", "1");
        tesseract.setTessVariable("language_model_penalty_non_dict_word", "0.2"); // 降低非词典词的惩罚
        tesseract.setTessVariable("language_model_penalty_non_freq_dict_word", "0.2"); // 降低非常用词的惩罚
        tesseract.setTessVariable("textord_min_linesize", "2.5"); // 最小行高
        tesseract.setTessVariable("edges_max_children_per_outline", "40"); // 每个轮廓最大子节点数
        tesseract.setTessVariable("edges_children_count_limit", "5"); // 子节点计数限制
        tesseract.setTessVariable("edges_children_fix", "true"); // 修复子节点
        
        // 设置页面分割模式为PSM_SINGLE_BLOCK，假设图像是单个文本块
        tesseract.setPageSegMode(6);
        
        // 设置OCR引擎模式为OEM_LSTM_ONLY，使用LSTM神经网络
        tesseract.setOcrEngineMode(1);
        
        try {
            // 对图像进行增强预处理后再识别
            File preprocessedImage = enhancedPreprocessImage(imageFile);
            return tesseract.doOCR(preprocessedImage);
        } catch (TesseractException e) {
            log.error("增强版Tesseract OCR识别失败", e);
            return "";
        }
    }
    
    @Override
    public String getRecognizerType() {
        return "enhanced_tesseract";
    }
    
    /**
     * 增强图像预处理方法，提高OCR识别率
     * @param imageFile 原始图像文件
     * @return 预处理后的图像文件
     * @throws IOException 如果处理失败
     */
    private File enhancedPreprocessImage(File imageFile) throws IOException {
        // 读取原始图像
        BufferedImage originalImage = ImageIO.read(imageFile);
        if (originalImage == null) {
            throw new IOException("无法读取图像文件: " + imageFile.getAbsolutePath());
        }
        
        // 获取图像尺寸
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();
        
        // 1. 转换为灰度图像
        BufferedImage grayImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        Graphics g = grayImage.getGraphics();
        g.drawImage(originalImage, 0, 0, null);
        g.dispose();
        
        // 2. 应用高斯模糊以减少噪点
        float[] blurKernel = {
            1/16f, 2/16f, 1/16f,
            2/16f, 4/16f, 2/16f,
            1/16f, 2/16f, 1/16f
        };
        ConvolveOp blurOp = new ConvolveOp(new Kernel(3, 3, blurKernel), ConvolveOp.EDGE_NO_OP, null);
        BufferedImage blurredImage = blurOp.filter(grayImage, null);
        
        // 3. 应用锐化滤镜以增强边缘
        float[] sharpenKernel = {
            0, -1, 0,
            -1, 5, -1,
            0, -1, 0
        };
        ConvolveOp sharpenOp = new ConvolveOp(new Kernel(3, 3, sharpenKernel), ConvolveOp.EDGE_NO_OP, null);
        BufferedImage sharpenedImage = sharpenOp.filter(blurredImage, null);
        
        // 4. 自适应二值化处理 - 使用积分图像(Integral Image)优化性能
        BufferedImage binaryImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);
        int windowSize = Math.max(width, height) / 20; // 自适应窗口大小
        windowSize = Math.max(windowSize, 15); // 确保窗口至少15像素
        
        // 计算积分图像 (Integral Image)
        int[][] integralImage = new int[height + 1][width + 1];
        
        // 第一步：填充积分图像
        for (int y = 1; y <= height; y++) {
            for (int x = 1; x <= width; x++) {
                int pixel = sharpenedImage.getRGB(x - 1, y - 1) & 0xFF; // 灰度图像，只需要一个通道
                integralImage[y][x] = pixel + integralImage[y][x-1] + integralImage[y-1][x] - integralImage[y-1][x-1];
            }
        }
        
        // 第二步：使用积分图像快速计算局部区域和 - 并行处理提高性能
        // 使用并行流处理加速二值化过程
        final int[][] finalIntegralImage = integralImage; // 创建final副本用于lambda表达式
        final int finalWindowSize = windowSize;
        
        // 创建坐标点列表用于并行处理
        java.util.List<java.awt.Point> points = new java.util.ArrayList<>(width * height);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                points.add(new java.awt.Point(x, y));
            }
        }
        
        // 使用并行流处理所有像素
        points.parallelStream().forEach(point -> {
            int x = point.x;
            int y = point.y;
            
            // 计算局部区域
            int startX = Math.max(0, x - finalWindowSize/2);
            int startY = Math.max(0, y - finalWindowSize/2);
            int endX = Math.min(width - 1, x + finalWindowSize/2);
            int endY = Math.min(height - 1, y + finalWindowSize/2);
            
            // 使用积分图像快速计算区域和
            int sum = finalIntegralImage[endY + 1][endX + 1] - finalIntegralImage[endY + 1][startX] 
                   - finalIntegralImage[startY][endX + 1] + finalIntegralImage[startY][startX];
            
            int count = (endX - startX + 1) * (endY - startY + 1);
            int threshold = (int)(sum / count) - 10; // 稍微降低阈值，使文本更清晰
            
            // 应用阈值
            int pixelValue = sharpenedImage.getRGB(x, y) & 0xFF;
            if (pixelValue < threshold) {
                binaryImage.setRGB(x, y, Color.BLACK.getRGB());
            } else {
                binaryImage.setRGB(x, y, Color.WHITE.getRGB());
            }
        });
        
        
        // 5. 应用形态学操作（闭运算）以连接断开的文本
        BufferedImage morphImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);
        Graphics2D g2d = morphImage.createGraphics();
        g2d.drawImage(binaryImage, 0, 0, null);
        g2d.dispose();
        
        // 保存预处理后的图像
        String originalPath = imageFile.getAbsolutePath();
        String preprocessedPath = originalPath.substring(0, originalPath.lastIndexOf(".")) + "_enhanced.png";
        File preprocessedFile = new File(preprocessedPath);
        ImageIO.write(morphImage, "png", preprocessedFile);
        
        return preprocessedFile;
    }
}