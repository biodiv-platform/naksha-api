package com.strandls.naksha.pojo.request;

public class LayerFileDescription {

	private String fileType;
	private String encoding;
	private String latColumnName;
	private String lonColumnName;
	private String field;
	private String geoColumnType;
	private String layerSRS;

	public LayerFileDescription() {
		super();
	}

	public LayerFileDescription(String fileType, String encoding, String latColumnName, String lonColumnName,
			String field, String geoColumnType, String layerSRS) {
		super();
		this.fileType = fileType;
		this.encoding = encoding;
		this.latColumnName = latColumnName;
		this.lonColumnName = lonColumnName;
		this.field = field;
		this.geoColumnType = geoColumnType;
		this.layerSRS = layerSRS;
	}

	public String getFileType() {
		return fileType;
	}

	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public String getLatColumnName() {
		return latColumnName;
	}

	public void setLatColumnName(String latColumnName) {
		this.latColumnName = latColumnName;
	}

	public String getLonColumnName() {
		return lonColumnName;
	}

	public void setLonColumnName(String lonColumnName) {
		this.lonColumnName = lonColumnName;
	}

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

	public String getGeoColumnType() {
		return geoColumnType;
	}

	public void setGeoColumnType(String geoColumnType) {
		this.geoColumnType = geoColumnType;
	}

	public String getLayerSRS() {
		return layerSRS;
	}

	public void setLayerSRS(String layerSRS) {
		this.layerSRS = layerSRS;
	}
}
