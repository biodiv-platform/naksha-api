package com.strandls.naksha.style.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@JsonIgnoreProperties
public class CircleLayerPaint implements StylePaint {

	@JsonProperty("circle-radius")
	private Double circleRadius;

	@JsonProperty("circle-opacity")
	private Double circleOpacity;

	@JsonProperty("circle-color")
	private StyleColor circleColor;

	public CircleLayerPaint() {
		super();
	}

	public CircleLayerPaint(Double circleRadius, Double circleOpacity, StyleColor circleColor) {
		super();
		this.circleRadius = circleRadius;
		this.circleOpacity = circleOpacity;
		this.circleColor = circleColor;
	}

	public Double getCircleRadius() {
		return circleRadius;
	}

	public void setCircleRadius(Double circleRadius) {
		this.circleRadius = circleRadius;
	}

	public Double getCircleOpacity() {
		return circleOpacity;
	}

	public void setCircleOpacity(Double circleOpacity) {
		this.circleOpacity = circleOpacity;
	}

	public StyleColor getCircleColor() {
		return circleColor;
	}

	public void setCircleColor(StyleColor circleColor) {
		this.circleColor = circleColor;
	}
}
