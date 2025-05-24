package com.example.formscanner.model.baidu.recognise;

import java.util.List;
import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

@Data
public class RecogniseData {
	@JSONField(name="ret")
	private List<RetItem> ret;

	@JSONField(name="templateSign")
	private String templateSign;

	@JSONField(name="scores")
	private Object scores;

	@JSONField(name="templateName")
	private String templateName;

	@JSONField(name="isStructured")
	private boolean isStructured;

	@JSONField(name="logId")
	private String logId;

	@JSONField(name="templateMatchDegree")
	private Object templateMatchDegree;

	@JSONField(name="clockwiseAngle")
	private Object clockwiseAngle;
}