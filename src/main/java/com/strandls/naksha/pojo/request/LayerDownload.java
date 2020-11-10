package com.strandls.naksha.pojo.request;

import java.util.List;

public class LayerDownload {
	private String layerName;
	private List<String> attributeList;
	private List<String> filterArray;

	public LayerDownload() {
		super();
	}

	public LayerDownload(String layerName, List<String> attributeList, List<String> filterArray) {
		super();
		this.layerName = layerName;
		this.attributeList = attributeList;
		this.filterArray = filterArray;
	}

	public String getLayerName() {
		return layerName;
	}

	public void setLayerName(String layerName) {
		this.layerName = layerName;
	}

	public List<String> getAttributeList() {
		return attributeList;
	}

	public void setAttributeList(List<String> attributeList) {
		this.attributeList = attributeList;
	}

	public List<String> getFilterArray() {
		return filterArray;
	}

	public void setFilterArray(List<String> filterArray) {
		this.filterArray = filterArray;
	}

}
