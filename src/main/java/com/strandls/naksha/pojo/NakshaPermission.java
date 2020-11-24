package com.strandls.naksha.pojo;

import java.io.Serializable;

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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.strandls.naksha.pojo.enumtype.PermissionType;

@Entity
@Table(name = "naksha_permission")
@XmlRootElement
@JsonIgnoreProperties
public class NakshaPermission implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -5486710044415890517L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "naksha_permission_id_generator")
	@SequenceGenerator(name = "naksha_permission_id_generator", sequenceName = "naksha_permission_id_seq", allocationSize = 1)
	@Column(name = "id", updatable = false, nullable = false)
	private Long id;
	
	@Column(name = "user_id")
	private Long userId;
	
	@Column(name = "type")
	private String type;
	
	@Column(name = "resource_type")
	private String resourceType;
	
	@Column(name = "resource_id")
	private Long resourceId;
	
	@Column(name = "permission")
	@Enumerated(EnumType.STRING)
	private PermissionType permission;

	public NakshaPermission() {
		super();
	}

	public NakshaPermission(Long id, Long userId, String type, String resourceType, Long resourceId,
			PermissionType permission) {
		super();
		this.id = id;
		this.userId = userId;
		this.type = type;
		this.resourceType = resourceType;
		this.resourceId = resourceId;
		this.permission = permission;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getResourceType() {
		return resourceType;
	}

	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}

	public Long getResourceId() {
		return resourceId;
	}

	public void setResourceId(Long resourceId) {
		this.resourceId = resourceId;
	}

	public PermissionType getPermission() {
		return permission;
	}

	public void setPermission(PermissionType permission) {
		this.permission = permission;
	}
}
