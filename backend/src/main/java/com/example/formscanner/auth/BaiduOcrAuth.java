package com.example.formscanner.auth;

import com.alibaba.fastjson2.JSONObject;
import jakarta.annotation.Resource;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class BaiduOcrAuth {
    @Resource
    private OkHttpClient okHttpClient;

    public String accessToken;

    public static final String API_KEY = "1ILlL6o1MxiqYDf6NxzdLP1l";

    public static final String SECRET_KEY = "OAmLnUmycfoTH3qm4yWwtSiWen49LjOf";

    /**
     * 从用户的AK，SK生成鉴权签名（Access Token）
     *
     * @return 鉴权签名（Access Token）
     * @throws IOException IO异常
     */
    public String getAccessToken() throws IOException {
        if (accessToken != null && !accessToken.isEmpty()) {
            return accessToken;
        }

        FormBody body = new FormBody.Builder()
                .add("grant_type", "client_credentials")
                .add("client_id", API_KEY)
                .add("client_secret", SECRET_KEY)
                .build();

        Request request = new Request.Builder()
                .url("https://aip.baidubce.com/oauth/2.0/token")
                .post(body)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();
        try (Response response = okHttpClient.newCall(request).execute()){
            accessToken = JSONObject.parse(response.body().string()).getString("access_token");
            return accessToken;
        }
    }
}
