package com.strandls.naksha.pojo.enumtype;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlType;

@XmlType(name = "downloadAccess")
@XmlEnum
public enum DownloadAccess {

	// Access to only contributor.
	@XmlEnumValue("Private")
	PRIVATE("PRIVATE"),
	// Access to group of people
	@XmlEnumValue("Group")
	GROUP("GROUP"),
	// Access to everyone
	@XmlEnumValue("All")
	ALL("ALL");

	private String value;

	DownloadAccess(String value) {
		this.value = value;
	}

	public static DownloadAccess fromValue(String value) {
		for (DownloadAccess downloadAccess : DownloadAccess.values()) {
			if (downloadAccess.value.equals(value))
				return downloadAccess;
		}
		throw new IllegalArgumentException(value);
	}
}
