package com.strandls.naksha.style.json;

import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@XmlRootElement
@JsonIgnoreProperties
public class LineLayerPaint implements StylePaint {

	@JsonProperty("line-width")
	private Double lineWidth;
	@JsonProperty("line-color")
	private StyleColor lineColor;

	public LineLayerPaint() {
		super();
	}

	public LineLayerPaint(Double lineWidth, StyleColor lineColor) {
		super();
		this.lineWidth = lineWidth;
		this.lineColor = lineColor;
	}

	public Double getLineWidth() {
		return lineWidth;
	}

	public void setLineWidth(Double lineWidth) {
		this.lineWidth = lineWidth;
	}

	public StyleColor getLineColor() {
		return lineColor;
	}

	public void setLineColor(StyleColor lineColor) {
		this.lineColor = lineColor;
	}
}
