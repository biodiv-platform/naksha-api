package com.strandls.naksha.pojo;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.annotations.Type;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.strandls.naksha.pojo.enumtype.DownloadAccess;
import com.strandls.naksha.pojo.enumtype.EditAccess;
import com.strandls.naksha.pojo.enumtype.LayerStatus;
import com.strandls.naksha.pojo.enumtype.LayerType;

@Entity
@Table(name = "`Meta_Layer_Table`")
@XmlRootElement
@JsonIgnoreProperties
public class MetaLayer implements Serializable {

	/**
	 * TODO : activity design for the comments
	 */

	/**
	* 
	*/
	private static final long serialVersionUID = -2888813985275939359L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "meta_layer_id_generator")
	@SequenceGenerator(name = "meta_layer_id_generator", sequenceName = "meta_layer_id_seq", allocationSize = 1)
	@Column(name = "id", updatable = false, nullable = false)
	private Long id;

	@Column(name = "layer_name")
	private String layerName;

	@Column(name = "layer_table_name")
	private String layerTableName;

	@Column(name = "layer_description")
	@Type(type = "text")
	private String layerDescription;

	@Column(name = "layer_type")
	@Enumerated(EnumType.STRING)
	private LayerType layerType;

	// 0 - Inactive, 1 - active
	@Column(name = "layer_status")
	@Enumerated(EnumType.STRING)
	private LayerStatus layerStatus;

	@Column(name = "geo_column")
	private String geo_column;

	@Column(name = "min_scale")
	private Double minScale;

	@Column(name = "max_scale")
	private Double maxScale;

	@Column(name = "pdf_link")
	private String pdfLink;

	@Column(name = "url")
	private String url;
	
	@Column(name = "shapeFiles")
	private String dirPath;

	// Person who upload the layer.
	@Column(name = "upload_user_id")
	private Long uploaderUserId;

	// Person who has contributed this layer.
	@Column(name = "attribution")
	private String attribution;

	// Tags for layer
	@Column(name = "tags")
	private String tags;

	// CC license
	@Column(name = "license")
	private String license;

	// Attribute columns to be shown on mouse hover.
	@Column(name = "summary_columns")
	private String summaryColumns;

	@Column(name = "download_access")
	@Enumerated(EnumType.STRING)
	private DownloadAccess downloadAccess;

	@Column(name = "edit_access")
	@Enumerated(EnumType.STRING)
	private EditAccess editAccess;

	// Default attribute column to color by
	@Column(name = "color_by")
	private String colorBy;

	@Column(name = "title_column")
	private String titleColumn;

	@Column(name = "size_by")
	private String sizeBy;

	@Column(name = "media_columns")
	private String mediaColumns;

	@Column(name = "page_id")
	private Long pageId;

	@Column(name = "italics_columns")
	private String italicsColumns;

	@Column(name = "create_by")
	private String createdBy;

	@Column(name = "created_date")
	private Timestamp createdDate;

	@Column(name = "modified_by")
	private String modifiedBy;

	@Column(name = "modified_date")
	private Timestamp modifiedDate;

	public MetaLayer() {
	}

	public MetaLayer(Long id, String layerName, String layerTableName, String layerDescription, LayerType layerType,
			LayerStatus layerStatus, String geo_column, Double minScale, Double maxScale, String pdfLink, String url,
			String dirPath, Long uploaderUserId, String attribution, String tags, String license, String summaryColumns,
			DownloadAccess downloadAccess, EditAccess editAccess, String colorBy, String titleColumn, String sizeBy,
			String mediaColumns, Long pageId, String italicsColumns, String createdBy, Timestamp createdDate,
			String modifiedBy, Timestamp modifiedDate) {
		super();
		this.id = id;
		this.layerName = layerName;
		this.layerTableName = layerTableName;
		this.layerDescription = layerDescription;
		this.layerType = layerType;
		this.layerStatus = layerStatus;
		this.geo_column = geo_column;
		this.minScale = minScale;
		this.maxScale = maxScale;
		this.pdfLink = pdfLink;
		this.url = url;
		this.dirPath = dirPath;
		this.uploaderUserId = uploaderUserId;
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
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getLayerName() {
		return layerName;
	}

	public void setLayerName(String layerName) {
		this.layerName = layerName;
	}

	public String getLayerTableName() {
		return layerTableName;
	}

	public void setLayerTableName(String layerTableName) {
		this.layerTableName = layerTableName;
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

	public LayerStatus getLayerStatus() {
		return layerStatus;
	}

	public void setLayerStatus(LayerStatus layerStatus) {
		this.layerStatus = layerStatus;
	}

	public String getGeo_column() {
		return geo_column;
	}

	public void setGeo_column(String geo_column) {
		this.geo_column = geo_column;
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

	public String getDirPath() {
		return dirPath;
	}

	public void setDirPath(String dirPath) {
		this.dirPath = dirPath;
	}

	public Long getUploaderUserId() {
		return uploaderUserId;
	}

	public void setUploaderUserId(Long uploaderUserId) {
		this.uploaderUserId = uploaderUserId;
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
}
