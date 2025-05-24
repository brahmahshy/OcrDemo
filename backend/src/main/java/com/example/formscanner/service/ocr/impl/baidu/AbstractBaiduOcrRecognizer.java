package com.example.formscanner.service.ocr.impl.baidu;

import com.example.formscanner.auth.BaiduOcrAuth;
import com.example.formscanner.service.ocr.OcrRecognizer;
import com.example.formscanner.util.baidu.Base64Util;
import com.example.formscanner.util.baidu.FileUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Map;

@Slf4j
@Component
public abstract class AbstractBaiduOcrRecognizer implements OcrRecognizer {
    @Resource
    private OkHttpClient okHttpClient;

    @Resource
    private BaiduOcrAuth baiduOcrAuth;

    @Override
    public String recognizeText(File imageFile) throws IOException {
        FormBody body = buildFormBody(imageFile);

        Request request = new Request.Builder()
                .url(getUrl() + baiduOcrAuth.getAccessToken())
                .post(body)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("Accept", "application/json")
                .build();

        try (Response response = okHttpClient.newCall(request).execute()){
            return response.body().string();
        }
    }

    private FormBody buildFormBody(File imageFile) throws IOException {
        byte[] bytes = FileUtil.readFileByBytes(imageFile);
        String encode = Base64Util.encode(bytes);
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("image", encode);

        Map<String, String> params = getBodyParams();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            builder.add(entry.getKey(), entry.getValue());
        }

        return builder.build();
    }

    protected abstract String getUrl();

    protected abstract Map<String, String> getBodyParams();
}
