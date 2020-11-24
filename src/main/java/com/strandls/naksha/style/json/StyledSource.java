package com.strandls.naksha.style.json;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@XmlRootElement
@JsonIgnoreProperties
public class StyledSource {
	private String type;
	private String scheme;
	private List<String> tiles;

	public StyledSource() {
		super();
	}

	public StyledSource(String type, String scheme, List<String> tiles) {
		super();
		this.type = type;
		this.scheme = scheme;
		this.tiles = tiles;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getScheme() {
		return scheme;
	}

	public void setScheme(String scheme) {
		this.scheme = scheme;
	}

	public List<String> getTiles() {
		return tiles;
	}

	public void setTiles(List<String> tiles) {
		this.tiles = tiles;
	}
}
