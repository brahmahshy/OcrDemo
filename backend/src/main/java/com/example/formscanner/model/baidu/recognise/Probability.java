package com.example.formscanner.model.baidu.recognise;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

@Data
public class Probability{
	@JSONField(name="average")
	private Object average;

	@JSONField(name="min")
	private Object min;

	@JSONField(name="variance")
	private Object variance;
}