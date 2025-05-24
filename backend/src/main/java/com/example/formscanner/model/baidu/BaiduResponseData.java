package com.example.formscanner.model.baidu;

import com.alibaba.fastjson2.annotation.JSONField;
import com.example.formscanner.model.baidu.recognise.RecogniseData;
import lombok.Data;

@Data
public class BaiduResponseData {
	@JSONField(name="log_id")
	private String logId;

	@JSONField(name="error_msg")
	private String errorMsg;

	@JSONField(name="data")
	private RecogniseData data;

	@JSONField(name="error_code")
	private int errorCode;
}