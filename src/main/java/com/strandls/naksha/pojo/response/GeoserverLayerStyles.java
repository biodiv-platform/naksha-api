package com.strandls.naksha.pojo.response;

public class GeoserverLayerStyles {

	private String styleName;

	private String styleTitle;

	private String styleType;

	public GeoserverLayerStyles() {
		super();
	}

	public GeoserverLayerStyles(String styleName, String styleTitle) {
		super();
		this.styleName = styleName;
		this.styleTitle = styleTitle;
	}

	public GeoserverLayerStyles(String styleName, String styleTitle, String styleType) {
		super();
		this.styleName = styleName;
		this.styleTitle = styleTitle;
		this.styleType = styleType;
	}

	public String getStyleName() {
		return styleName;
	}

	public void setStyleName(String styleName) {
		this.styleName = styleName;
	}

	public String getStyleTitle() {
		return styleTitle;
	}

	public void setStyleTitle(String styleTitle) {
		this.styleTitle = styleTitle;
	}

	public String getStyleType() {
		return styleType;
	}

	public void setStyleType(String styleType) {
		this.styleType = styleType;
	}
}
