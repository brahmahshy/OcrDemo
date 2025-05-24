package com.example.formscanner.service.ocr.impl.baidu;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class BaiduRecogniseOcrRecoginzer extends AbstractBaiduOcrRecognizer {
    private static final String URL = "https://aip.baidubce.com/rest/2.0/solution/v1/iocr/recognise?access_token=";

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
}