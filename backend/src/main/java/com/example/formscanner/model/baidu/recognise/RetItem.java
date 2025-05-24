package com.example.formscanner.model.baidu.recognise;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

@Data
public class RetItem{
	@JSONField(name="probability")
	private Probability probability;

	@JSONField(name="location")
	private Location location;

	@JSONField(name="word_name")
	private String wordName;

	@JSONField(name="word")
	private String word;
}