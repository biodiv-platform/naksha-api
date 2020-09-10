package com.strandls.naksha.style.json;

import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@XmlRootElement
@JsonIgnoreProperties
public class FillLayerPaint implements StylePaint {

	@JsonProperty("fill-outline-color")
	private String fillOutlineColor;
	@JsonProperty("fill-opacity")
	private Double fillOpacity;
	@JsonProperty("fill-color")
	private StyleColor fillColor;

	public FillLayerPaint() {
		super();
	}

	public FillLayerPaint(String fillOutlineColor, Double fillOpacity, StyleColor fillColor) {
		super();
		this.fillOutlineColor = fillOutlineColor;
		this.fillOpacity = fillOpacity;
		this.fillColor = fillColor;
	}

	public String getFillOutlineColor() {
		return fillOutlineColor;
	}

	public void setFillOutlineColor(String fillOutlineColor) {
		this.fillOutlineColor = fillOutlineColor;
	}

	public Double getFillOpacity() {
		return fillOpacity;
	}

	public void setFillOpacity(Double fillOpacity) {
		this.fillOpacity = fillOpacity;
	}

	public StyleColor getFillColor() {
		return fillColor;
	}

	public void setFillColor(StyleColor fillColor) {
		this.fillColor = fillColor;
	}
}
