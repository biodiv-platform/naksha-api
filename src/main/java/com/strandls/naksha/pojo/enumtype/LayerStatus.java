package com.strandls.naksha.pojo.enumtype;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "layerStatus")
@XmlEnum
public enum LayerStatus {

	@XmlEnumValue("Pending")
	PENDING("PENDING"),
	@XmlEnumValue("InActive")
	INACTIVE("INACTIVE"),
	@XmlEnumValue("Active")
	ACTIVE("ACTIVE");
	
	private String value;
	
	LayerStatus(String value) {
		this.value = value;
	}
	
	public static LayerStatus fromValue(String value) {
		for(LayerStatus layerStatus : LayerStatus.values()) {
			if(layerStatus.value.equals(value))
				return layerStatus;
		}
		throw new IllegalArgumentException(value);
	}
}
