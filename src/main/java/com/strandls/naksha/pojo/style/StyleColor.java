package com.strandls.naksha.pojo.style;

import java.util.List;

public class StyleColor {

	private String property;
	private String type;
	private List<List<Object>> stops;

	public StyleColor() {
		super();
	}

	public StyleColor(String property, String type, List<List<Object>> stops) {
		super();
		this.property = property;
		this.type = type;
		this.stops = stops;
	}

	public String getProperty() {
		return property;
	}

	public void setProperty(String property) {
		this.property = property;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public List<List<Object>> getStops() {
		return stops;
	}

	public void setStops(List<List<Object>> stops) {
		this.stops = stops;
	}
}
