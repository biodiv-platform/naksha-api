package com.strandls.naksha.pojo.enumtype;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlType;

@XmlType(name = "layerType")
@XmlEnum
public enum LayerType {
	@XmlEnumValue("RASTER")
	RASTER("RASTER"), @XmlEnumValue("MULTIPOLYGON")
	MULTIPOLYGON("MULTIPOLYGON"), @XmlEnumValue("POINT")
	POINT("POINT"), @XmlEnumValue("MULTILINESTRING")
	MULTILINESTRING("MULTILINESTRING"), @XmlEnumValue("MULTIPOINT")
	MULTIPOINT("MULTIPOINT");

	private String value;

	LayerType(String value) {
		this.value = value;
	}

	public static LayerType fromValue(String value) {
		for (LayerType layerStatus : LayerType.values()) {
			if (layerStatus.value.equals(value))
				return layerStatus;
		}
		throw new IllegalArgumentException(value);
	}
}
