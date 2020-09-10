package com.strandls.naksha.style.json;

import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@XmlRootElement
@JsonIgnoreProperties
public class JsonStyle {

	private Integer version;
	private Map<String, StyledSource> sources;
	private List<StyledLayer> layers;

	public JsonStyle() {
		super();
	}

	public JsonStyle(Integer version, Map<String, StyledSource> sources, List<StyledLayer> layers) {
		super();
		this.version = version;
		this.sources = sources;
		this.layers = layers;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	public Map<String, StyledSource> getSources() {
		return sources;
	}

	public void setSources(Map<String, StyledSource> sources) {
		this.sources = sources;
	}

	public List<StyledLayer> getLayers() {
		return layers;
	}

	public void setLayers(List<StyledLayer> layers) {
		this.layers = layers;
	}
}
