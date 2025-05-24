package com.example.formscanner.service.ocr.impl.baidu;

import com.alibaba.fastjson2.JSON;
import com.example.formscanner.model.FormData;
import com.example.formscanner.model.baidu.BaiduResponseData;
import com.example.formscanner.model.baidu.recognise.RetItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BaiduRecogniseOcrRecoginzer extends AbstractBaiduOcrRecognizer {
    private static final String URL = "https://aip.baidubce.com/rest/2.0/solution/v1/iocr/recognise?access_token=";

    private static final Map<String, String> WORD_MAP = new HashMap<>();

    static {
        WORD_MAP.put("工程名称", "projectName");
        WORD_MAP.put("施工单位", "constructionUnit");
        WORD_MAP.put("工程地址", "projectAddress");
        WORD_MAP.put("施工部位", "constructionPart");
        WORD_MAP.put("强度等级", "strengthLevel");
        WORD_MAP.put("本车方量(m3)", "currentVolume");
    }

    @Override
    public String getRecognizerType() {
        return "baidu_ocr";
    }

    @Override
    protected String getUrl() {
        return URL;
    }

    @Override
    protected Map<String, String> getBodyParams() {
        HashMap<String, String> map = new HashMap<>();
        map.put("templateSign", "4e3ebcfef734b683b040ea6f489eb9e7");
        return map;
    }

    @Override
    public FormData getFormData(String recognizedText) {
        BaiduResponseData response = JSON.parseObject(recognizedText, BaiduResponseData.class);

        FormData formData = new FormData();
        formData.setLogId(response.getLogId());

        Map<String, String> responseMap =
                response.getData().getRet().stream().collect(Collectors.toMap(RetItem::getWordName, RetItem::getWord));
        log.info("Log id: {}. responseMap: {}", response.getLogId(), responseMap);

        responseMap.forEach((key, value) -> {
            Field fieldValue;
            try {
                String wordField = WORD_MAP.get(key);
                if (wordField == null) {
                    return;
                }

                fieldValue = formData.getClass().getDeclaredField(wordField);
            } catch (NoSuchFieldException e) {
                return;
            }

            fieldValue.setAccessible(true);
            try {
                fieldValue.set(formData, String.valueOf(value));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });

        return formData;
    }
}