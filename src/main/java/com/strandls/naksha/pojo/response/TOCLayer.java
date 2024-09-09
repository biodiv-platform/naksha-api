package com.strandls.naksha.pojo.response;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.strandls.naksha.pojo.MetaLayer;
import com.strandls.naksha.pojo.enumtype.LayerStatus;
import com.strandls.naksha.pojo.enumtype.LayerType;
import com.strandls.user.pojo.UserIbp;

public class TOCLayer {

	private Long id;
	private String name;
	private String title;
	private String description;
	private LayerType layerType;

	private String pdfLink;
	private String url;
	private UserIbp author;
	private String attribution;
	private List<String> tags;
	private String license;
	private Long pageId;

	private String createdBy;
	private Timestamp createdDate;
	private String modifiedBy;
	private Timestamp modifiedDate;

	private String downloadAccess;

	private List<List<Double>> bbox;
	private String thumbnail;
	private LayerStatus layerStatus;
	private String uploaderUserId;
	private String portalId;

	public TOCLayer() {
		super();
	}

	public TOCLayer(MetaLayer metaLayer, UserIbp userIbp, String downloadAccess, List<List<Double>> bbox,
			String thumbnail, String uploaderUserId, String portalId) {
		this.id = metaLayer.getId();
		this.name = metaLayer.getLayerTableName();
		this.title = metaLayer.getLayerName();
		this.description = metaLayer.getLayerDescription();
		this.layerType = metaLayer.getLayerType();
		this.pdfLink = metaLayer.getPdfLink();
		this.url = metaLayer.getUrl();
		this.author = userIbp;
		this.attribution = metaLayer.getAttribution();
		this.tags = new ArrayList<>();
		for (String tag : metaLayer.getTags().split(",")) {
			if (tag == null || "".equals(tag))
				continue;
			this.tags.add(tag.trim());
		}
		this.license = metaLayer.getLicense();
		this.pageId = metaLayer.getPageId();
		this.createdBy = metaLayer.getCreatedBy();
		this.createdDate = metaLayer.getCreatedDate();
		this.modifiedBy = metaLayer.getModifiedBy();
		this.modifiedDate = metaLayer.getModifiedDate();

		this.downloadAccess = downloadAccess;
		this.bbox = bbox;
		this.thumbnail = thumbnail;
		this.layerStatus = metaLayer.getLayerStatus();
		this.uploaderUserId = uploaderUserId;
		this.portalId = portalId;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public LayerType getLayerType() {
		return layerType;
	}

	public void setLayerType(LayerType layerType) {
		this.layerType = layerType;
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

	public UserIbp getAuthor() {
		return author;
	}

	public void setAuthor(UserIbp author) {
		this.author = author;
	}

	public String getAttribution() {
		return attribution;
	}

	public void setAttribution(String attribution) {
		this.attribution = attribution;
	}

	public List<String> getTags() {
		return tags;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}

	public String getLicense() {
		return license;
	}

	public void setLicense(String license) {
		this.license = license;
	}

	public Long getPageId() {
		return pageId;
	}

	public void setPageId(Long pageId) {
		this.pageId = pageId;
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

	public String getDownloadAccess() {
		return downloadAccess;
	}

	public void setDownloadAccess(String downloadAccess) {
		this.downloadAccess = downloadAccess;
	}

	public List<List<Double>> getBbox() {
		return bbox;
	}

	public void setBbox(List<List<Double>> bbox) {
		this.bbox = bbox;
	}

	public String getThumbnail() {
		return thumbnail;
	}

	public void setThumbnail(String thumbnail) {
		this.thumbnail = thumbnail;
	}

	public LayerStatus getLayerStatus() {
		return layerStatus;
	}

	public void setLayerStatus(LayerStatus layerStatus) {
		this.layerStatus = layerStatus;
	}

	public String getUploaderUserId() {
		return uploaderUserId;
	}

	public void setUploaderUserId(String uploaderUserId) {
		this.uploaderUserId = uploaderUserId;
	}

	public String getPortalId() {
		return portalId;
	}

	public void setPortalId(String portalId) {
		this.portalId = portalId;
	}

}
