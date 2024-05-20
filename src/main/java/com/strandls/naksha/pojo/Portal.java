package com.strandls.naksha.pojo;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "portal")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Portal implements Serializable {

	private static final long serialVersionUID = -7336349045473475719L;
	private String portalId;
	private String portalName;
	private String portalDomain;

	@Column(name = "portal_id")
	public String getPortalId() {
		return portalId;
	}

	public void setPortalId(String portalId) {
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

}
