package com.strandls.naksha.pojo.request;

import java.sql.Timestamp;
import java.util.Map;

import com.strandls.naksha.pojo.enumtype.DownloadAccess;
import com.strandls.naksha.pojo.enumtype.EditAccess;
import com.strandls.naksha.pojo.enumtype.LayerType;

public class MetaData {

	private String layerName;
	private String layerDescription;
	private LayerType layerType;
	private String geoColumn;
	private Double minScale;
	private Double maxScale;
	private String pdfLink;
	private String url;
	private String attribution;
	private String tags;
	private String license;
	private String summaryColumns;
	private DownloadAccess downloadAccess;
	private EditAccess editAccess;
	private String colorBy;
	private String titleColumn;
	private String sizeBy;
	private String mediaColumns;
	private Long pageId;
	private String italicsColumns;
	private String createdBy;
	private Timestamp createdDate;
	private String modifiedBy;
	private Timestamp modifiedDate;
	private LayerFileDescription layerFileDescription;
	private Map<String, String> layerColumnDescription;

	public MetaData() {
		super();
	}

	public MetaData(String layerName, String layerDescription, LayerType layerType, String geoColumn, Double minScale,
			Double maxScale, String pdfLink, String url, String attribution, String tags,
			String license, String summaryColumns, DownloadAccess downloadAccess, EditAccess editAccess, String colorBy,
			String titleColumn, String sizeBy, String mediaColumns, Long pageId, String italicsColumns,
			String createdBy, Timestamp createdDate, String modifiedBy, Timestamp modifiedDate,
			LayerFileDescription layerFileDescription, Map<String, String> layerColumnDescription) {
		super();
		this.layerName = layerName;
		this.layerDescription = layerDescription;
		this.layerType = layerType;
		this.geoColumn = geoColumn;
		this.minScale = minScale;
		this.maxScale = maxScale;
		this.pdfLink = pdfLink;
		this.url = url;
		this.attribution = attribution;
		this.tags = tags;
		this.license = license;
		this.summaryColumns = summaryColumns;
		this.downloadAccess = downloadAccess;
		this.editAccess = editAccess;
		this.colorBy = colorBy;
		this.titleColumn = titleColumn;
		this.sizeBy = sizeBy;
		this.mediaColumns = mediaColumns;
		this.pageId = pageId;
		this.italicsColumns = italicsColumns;
		this.createdBy = createdBy;
		this.createdDate = createdDate;
		this.modifiedBy = modifiedBy;
		this.modifiedDate = modifiedDate;
		this.layerFileDescription = layerFileDescription;
		this.layerColumnDescription = layerColumnDescription;
	}

	public String getLayerName() {
		return layerName;
	}

	public void setLayerName(String layerName) {
		this.layerName = layerName;
	}

	public String getLayerDescription() {
		return layerDescription;
	}

	public void setLayerDescription(String layerDescription) {
		this.layerDescription = layerDescription;
	}

	public LayerType getLayerType() {
		return layerType;
	}

	public void setLayerType(LayerType layerType) {
		this.layerType = layerType;
	}

	public String getGeoColumn() {
		return geoColumn;
	}

	public void setGeoColumn(String geoColumn) {
		this.geoColumn = geoColumn;
	}

	public Double getMinScale() {
		return minScale;
	}

	public void setMinScale(Double minScale) {
		this.minScale = minScale;
	}

	public Double getMaxScale() {
		return maxScale;
	}

	public void setMaxScale(Double maxScale) {
		this.maxScale = maxScale;
	}

	public String getPdfLink() {
		return pdfLink;
	}

	public void setPdfLink(String pdfLink) {
		this.pdfLink = pdfLink;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getAttribution() {
		return attribution;
	}

	public void setAttribution(String attribution) {
		this.attribution = attribution;
	}

	public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}

	public String getLicense() {
		return license;
	}

	public void setLicense(String license) {
		this.license = license;
	}

	public String getSummaryColumns() {
		return summaryColumns;
	}

	public void setSummaryColumns(String summaryColumns) {
		this.summaryColumns = summaryColumns;
	}

	public DownloadAccess getDownloadAccess() {
		return downloadAccess;
	}

	public void setDownloadAccess(DownloadAccess downloadAccess) {
		this.downloadAccess = downloadAccess;
	}

	public EditAccess getEditAccess() {
		return editAccess;
	}

	public void setEditAccess(EditAccess editAccess) {
		this.editAccess = editAccess;
	}

	public String getColorBy() {
		return colorBy;
	}

	public void setColorBy(String colorBy) {
		this.colorBy = colorBy;
	}

	public String getTitleColumn() {
		return titleColumn;
	}

	public void setTitleColumn(String titleColumn) {
		this.titleColumn = titleColumn;
	}

	public String getSizeBy() {
		return sizeBy;
	}

	public void setSizeBy(String sizeBy) {
		this.sizeBy = sizeBy;
	}

	public String getMediaColumns() {
		return mediaColumns;
	}

	public void setMediaColumns(String mediaColumns) {
		this.mediaColumns = mediaColumns;
	}

	public Long getPageId() {
		return pageId;
	}

	public void setPageId(Long pageId) {
		this.pageId = pageId;
	}

	public String getItalicsColumns() {
		return italicsColumns;
	}

	public void setItalicsColumns(String italicsColumns) {
		this.italicsColumns = italicsColumns;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public Timestamp getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Timestamp createdDate) {
		this.createdDate = createdDate;
	}

	public String getModifiedBy() {
		return modifiedBy;
	}

	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	public Timestamp getModifiedDate() {
		return modifiedDate;
	}

	public void setModifiedDate(Timestamp modifiedDate) {
		this.modifiedDate = modifiedDate;
	}

	public LayerFileDescription getLayerFileDescription() {
		return layerFileDescription;
	}

	public void setLayerFileDescription(LayerFileDescription layerFileDescription) {
		this.layerFileDescription = layerFileDescription;
	}

	public Map<String, String> getLayerColumnDescription() {
		return layerColumnDescription;
	}

	public void setLayerColumnDescription(Map<String, String> layerColumnDescription) {
		this.layerColumnDescription = layerColumnDescription;
	}
}
