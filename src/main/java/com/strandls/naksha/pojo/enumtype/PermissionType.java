package com.strandls.naksha.pojo.enumtype;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlType;

@XmlType(name = "PermissionType")
@XmlEnum
public enum PermissionType {
	@XmlEnumValue("LayerUploader")
	LAYER_UPLOADER("LAYER_UPLOADER"), @XmlEnumValue("LayerApprover")
	LAYER_APPROVER("LAYER_APPROVER"), @XmlEnumValue("LayerDownloader")
	LAYER_DOWNLOADER("LAYER_DOWNLOADER"), @XmlEnumValue("LayerEditor")
	LAYER_EDITOR("LAYER_EDITOR");

	private String value;

	PermissionType(String value) {
		this.value = value;
	}

	public static PermissionType fromValue(String value) {
		for (PermissionType layerStatus : PermissionType.values()) {
			if (layerStatus.value.equals(value))
				return layerStatus;
		}
		throw new IllegalArgumentException(value);
	}
}
