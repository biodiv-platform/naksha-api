package com.strandls.naksha.pojo.response;

import java.util.HashMap;
import java.util.Map;

public class MBStyle {

	private Map<String, String> style;
	
	public MBStyle() {
		super();
	}
	
	public MBStyle(String name, String fileName, String format) {
		super();
		style = new HashMap<>();
		style.put("name", name);
		style.put("filename", fileName);
		style.put("format", format);
	}
	public MBStyle(Map<String, String> style) {
		super();
		this.style = style;
	}
	public Map<String, String> getStyle() {
		return style;
	}
	public void setStyle(Map<String, String> style) {
		this.style = style;
	}
}
