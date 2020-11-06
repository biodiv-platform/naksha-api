package com.strandls.naksha.pojo.response;

import java.util.List;

public class LayerInfoOnClick {

	private String layerName;
	private String titleColumn;
	private List<String> summaryColumn;
	private List<GeoserverLayerStyles> styles;

	public LayerInfoOnClick() {
		super();
	}

	public LayerInfoOnClick(String layerName, String titleColumn, List<String> summaryColumn,
			List<GeoserverLayerStyles> styles) {
		super();
		this.layerName = layerName;
		this.titleColumn = titleColumn;
		this.summaryColumn = summaryColumn;
		this.styles = styles;
	}

	public String getLayerName() {
		return layerName;
	}

	public void setLayerName(String layerName) {
		this.layerName = layerName;
	}

	public String getTitleColumn() {
		return titleColumn;
	}

	public void setTitleColumn(String titleColumn) {
		this.titleColumn = titleColumn;
	}

	public List<String> getSummaryColumn() {
		return summaryColumn;
	}

	public void setSummaryColumn(List<String> summaryColumn) {
		this.summaryColumn = summaryColumn;
	}

	public List<GeoserverLayerStyles> getStyles() {
		return styles;
	}

	public void setStyles(List<GeoserverLayerStyles> styles) {
		this.styles = styles;
	}

}
