package com.example.formscanner.model.baidu.recognise;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

@Data
public class Location{
	@JSONField(name="top")
	private int top;

	@JSONField(name="left")
	private int left;

	@JSONField(name="width")
	private int width;

	@JSONField(name="height")
	private int height;
}