package com.strandls.naksha.pojo;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "portal")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Portal implements Serializable {

	private static final long serialVersionUID = -7336349045473475719L;
	private Long portalId;
	private String portalName;
	private String portalDomain;
	private String apiKey;

	public Portal() {
		super();
	}

	public Portal(Long portalId, String portalName, String portalDomain, String apiKey) {
		super();
		this.portalId = portalId;
		this.portalName = portalName;
		this.portalDomain = portalDomain;
		this.apiKey = apiKey;
	}

	@Id
	@GeneratedValue
	@Column(name = "portal_id")
	public Long getPortalId() {
		return portalId;
	}

	public void setPortalId(Long portalId) {
		this.portalId = portalId;
	}

	@Column(name = "portal_name")
	public String getPortalName() {
		return portalName;
	}

	public void setPortalName(String portalName) {
		this.portalName = portalName;
	}

	@Column(name = "portal_domain")
	public String getPortalDomain() {
		return portalDomain;
	}

	public void setPortalDomain(String portalDomain) {
		this.portalDomain = portalDomain;
	}

	@Column(name = "api_key", columnDefinition = "TEXT")
	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

}
