package com.strandls.naksha.pojo;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "layer_portal_mapping")
@JsonIgnoreProperties(ignoreUnknown = true)
public class layerPortalMapping implements Serializable {
	private static final long serialVersionUID = -7235401478350962938L;
	private Long layerId;
	private Long portalId;

	public layerPortalMapping() {
		super();
	}

	public layerPortalMapping(Long layerId, Long portalId) {
		super();
		this.layerId = layerId;
		this.portalId = portalId;
	}

	@Id
	@Column(name = "layer_id")
	public Long getLayerId() {
		return layerId;
	}

	public void setLayerId(Long layerId) {
		this.layerId = layerId;
	}

	@Id
	@Column(name = "portal_id")
	public Long getPortalId() {
		return portalId;
	}

	public void setPortalId(Long portalId) {
		this.portalId = portalId;
	}

}
