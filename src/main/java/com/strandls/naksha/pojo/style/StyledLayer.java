package com.strandls.naksha.pojo.style;

import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@XmlRootElement
@JsonIgnoreProperties
public class StyledLayer {

	private String id;
	private String type;
	private String source;

	@JsonProperty("source-layer")
	private String sourceLayer;
	private StylePaint paint;
	
	public StyledLayer() {
		super();
	}
	
	public StyledLayer(String id, String type, String source, String sourceLayer, StylePaint paint) {
		super();
		this.id = id;
		this.type = type;
		this.source = source;
		this.sourceLayer = sourceLayer;
		this.paint = paint;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getSourceLayer() {
		return sourceLayer;
	}

	public void setSourceLayer(String sourceLayer) {
		this.sourceLayer = sourceLayer;
	}

	public StylePaint getPaint() {
		return paint;
	}

	public void setPaint(StylePaint paint) {
		this.paint = paint;
	}
}
