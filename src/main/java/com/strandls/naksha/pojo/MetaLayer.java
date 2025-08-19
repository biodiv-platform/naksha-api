package com.strandls.naksha.pojo;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Locale;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.strandls.naksha.pojo.enumtype.DownloadAccess;
import com.strandls.naksha.pojo.enumtype.EditAccess;
import com.strandls.naksha.pojo.enumtype.LayerStatus;
import com.strandls.naksha.pojo.enumtype.LayerType;
import com.strandls.naksha.pojo.request.MetaData;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.xml.bind.annotation.XmlRootElement;

@Entity
@Table(name = "`Meta_Layer_Table`")
@XmlRootElement
@JsonIgnoreProperties
@Schema(name = "MetaLayer", description = "Metadata information for vector/raster layers")
public class MetaLayer implements Serializable {

	private static final long serialVersionUID = -2888813985275939359L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "meta_layer_id_generator")
	@SequenceGenerator(name = "meta_layer_id_generator", sequenceName = "meta_layer_id_seq", allocationSize = 1)
	@Column(name = "id", updatable = false, nullable = false)
	@Schema(description = "Unique identifier for the layer", example = "123")
	private Long id;

	@Column(name = "layer_name", columnDefinition = "text")
	@Schema(description = "Display name of the layer", example = "Elevation Map")
	private String layerName;

	@Column(name = "layer_table_name", columnDefinition = "text")
	@Schema(description = "Table name in the database for this layer", example = "elevation_layer")
	private String layerTableName;

	@Column(name = "layer_description", columnDefinition = "text")
	@Schema(description = "Description about the layer", example = "Raster data for elevations")
	private String layerDescription;

	@Column(name = "layer_type")
	@Enumerated(EnumType.STRING)
	@Schema(description = "Type of the layer", implementation = LayerType.class)
	private LayerType layerType;

	@Column(name = "layer_status")
	@Enumerated(EnumType.STRING)
	@Schema(description = "Status of the layer", implementation = LayerStatus.class)
	private LayerStatus layerStatus;

	@Column(name = "geo_column")
	@Schema(description = "Geometry column name", example = "geom")
	private String geoColumn;

	@Column(name = "min_scale")
	@Schema(description = "Minimum scale", example = "1000")
	private Double minScale;

	@Column(name = "max_scale")
	@Schema(description = "Maximum scale", example = "100000")
	private Double maxScale;

	@Column(name = "pdf_link", columnDefinition = "text")
	@Schema(description = "Link to a PDF resource", example = "http://example.com/manual.pdf")
	private String pdfLink;

	@Column(name = "url", columnDefinition = "text")
	@Schema(description = "URL to the layer resource", example = "http://gis.example.com/layers/1")
	private String url;

	@Column(name = "shapeFiles", columnDefinition = "text")
	@Schema(description = "Directory path to shapefiles on disk", example = "/data/shapefiles/layer1")
	private String dirPath;

	@Column(name = "upload_user_id")
	@Schema(description = "User ID of who uploaded the layer", example = "445")
	private Long uploaderUserId;

	@Column(name = "attribution", columnDefinition = "text")
	@Schema(description = "Attribution or contributor information", example = "Survey Department")
	private String attribution;

	@Column(name = "tags", columnDefinition = "text")
	@Schema(description = "Tags related to the layer", example = "elevation, raster")
	private String tags;

	@Column(name = "license", columnDefinition = "text")
	@Schema(description = "License information", example = "CC-BY-SA 4.0")
	private String license;

	@Column(name = "summary_columns", columnDefinition = "text")
	@Schema(description = "Attribute columns summary", example = "elev,min,max")
	private String summaryColumns;

	@Column(name = "download_access")
	@Enumerated(EnumType.STRING)
	@Schema(description = "Download access policy", implementation = DownloadAccess.class)
	private DownloadAccess downloadAccess;

	@Column(name = "edit_access")
	@Enumerated(EnumType.STRING)
	@Schema(description = "Edit access policy", implementation = EditAccess.class)
	private EditAccess editAccess;

	@Column(name = "color_by")
	@Schema(description = "Name of the attribute used for coloring", example = "elevation")
	private String colorBy;

	@Column(name = "title_column")
	@Schema(description = "Title column name", example = "site_name")
	private String titleColumn;

	@Column(name = "size_by")
	@Schema(description = "Size column name", example = "area")
	private String sizeBy;

	@Column(name = "media_columns", columnDefinition = "text")
	@Schema(description = "Media columns", example = "photo")
	private String mediaColumns;

	@Column(name = "page_id")
	@Schema(description = "Connected Page ID", example = "77")
	private Long pageId;

	@Column(name = "italics_columns", columnDefinition = "text")
	@Schema(description = "Italics columns", example = "notes")
	private String italicsColumns;

	@Column(name = "create_by", columnDefinition = "text")
	@Schema(description = "Username who created", example = "admin")
	private String createdBy;

	@Column(name = "created_date")
	@Schema(description = "Timestamp when created", example = "2024-12-21T13:45:00.123Z")
	private Timestamp createdDate;

	@Column(name = "modified_by", columnDefinition = "text")
	@Schema(description = "Username who last modified", example = "admin2")
	private String modifiedBy;

	@Column(name = "modified_date")
	@Schema(description = "Timestamp when last modified", example = "2025-01-16T08:21:00.000Z")
	private Timestamp modifiedDate;

	public MetaLayer() {
		super();
	}

	public MetaLayer(MetaData metaData, Long uploaderUserId, String dirPath) {
		this.layerName = metaData.getLayerName();
		this.layerDescription = metaData.getLayerDescription();
		this.layerType = metaData.getLayerType();
		this.layerStatus = LayerStatus.PENDING;
		this.geoColumn = metaData.getGeoColumn();
		this.minScale = metaData.getMinScale();
		this.maxScale = metaData.getMaxScale();
		this.pdfLink = metaData.getPdfLink();
		this.url = metaData.getUrl();
		this.dirPath = dirPath;
		this.uploaderUserId = uploaderUserId;
		this.attribution = metaData.getAttribution();
		this.tags = metaData.getTags();
		this.license = metaData.getLicense();
		if (metaData.getSummaryColumns() != null)
			this.summaryColumns = metaData.getSummaryColumns().toLowerCase(Locale.ROOT);
		this.downloadAccess = metaData.getDownloadAccess();
		this.editAccess = metaData.getEditAccess();
		this.colorBy = metaData.getColorBy();
		this.titleColumn = metaData.getTitleColumn();
		this.sizeBy = metaData.getSizeBy();
		this.mediaColumns = metaData.getMediaColumns();
		this.pageId = metaData.getPageId();
		this.italicsColumns = metaData.getItalicsColumns();
		this.createdBy = metaData.getCreatedBy();
		this.createdDate = metaData.getCreatedDate();
		this.modifiedBy = metaData.getModifiedBy();
		this.modifiedDate = metaData.getModifiedDate();
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
