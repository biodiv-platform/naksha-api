package com.strandls.naksha.pojo.request;

import java.sql.Timestamp;
import java.util.Map;

import com.strandls.naksha.pojo.MetaLayer;
import com.strandls.naksha.pojo.enumtype.DownloadAccess;
import com.strandls.naksha.pojo.enumtype.EditAccess;
import com.strandls.naksha.pojo.enumtype.LayerType;

public class MetaLayerEdit {

	private Long id;
	private String layerName;
	private String layerDescription;
	private LayerType layerType;
	private String attribution;
	private String tags;
	private String license;
	private String summaryColumns;
	private DownloadAccess downloadAccess;
	private EditAccess editAccess;
	private String colorBy;
	private String titleColumn;
	private String modifiedBy;
	private Timestamp modifiedDate;
	private Map<String, String> layerColumnDescription;

	public MetaLayerEdit() {
		super();
	}

	public MetaLayer update(MetaLayerEdit metaLayerEdit, MetaLayer metaLayer) {
		if (metaLayerEdit.getLayerName() != null && !"".equals(metaLayerEdit.getLayerName()))
			metaLayer.setLayerName(metaLayerEdit.getLayerName());
		if (metaLayerEdit.getLayerDescription() != null && !"".equals(metaLayerEdit.getLayerDescription()))
			metaLayer.setLayerDescription(metaLayerEdit.getLayerDescription());
		if (metaLayerEdit.getLayerType() != null)
			metaLayer.setLayerType(metaLayerEdit.getLayerType());
		if (metaLayerEdit.getAttribution() != null)
			metaLayer.setAttribution(metaLayerEdit.getAttribution());
		if (metaLayerEdit.getTags() != null)
			metaLayer.setTags(metaLayerEdit.getTags());
		if (metaLayerEdit.getLicense() != null)
			metaLayer.setLicense(metaLayerEdit.getLicense());
		if (metaLayerEdit.getSummaryColumns() != null)
			metaLayer.setSummaryColumns(metaLayerEdit.getSummaryColumns());
		if (metaLayerEdit.getDownloadAccess() != null)
			metaLayer.setDownloadAccess(metaLayerEdit.getDownloadAccess());
		if (metaLayerEdit.getEditAccess() != null)
			metaLayer.setEditAccess(metaLayerEdit.getEditAccess());
		if (metaLayerEdit.getColorBy() != null)
			metaLayer.setColorBy(metaLayerEdit.getColorBy());
		if (metaLayerEdit.getTitleColumn() != null)
			metaLayer.setTitleColumn(metaLayerEdit.getTitleColumn());
		if (metaLayerEdit.getModifiedBy() != null)
			metaLayer.setModifiedBy(metaLayerEdit.getModifiedBy());
		if (metaLayerEdit.getModifiedDate() != null)
			metaLayer.setModifiedDate(metaLayerEdit.getModifiedDate());
		return metaLayer;
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

	public Map<String, String> getLayerColumnDescription() {
		return layerColumnDescription;
	}

	public void setLayerColumnDescription(Map<String, String> layerColumnDescription) {
		this.layerColumnDescription = layerColumnDescription;
	}
}
