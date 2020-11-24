package com.strandls.naksha.pojo.enumtype;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "editAccess")
@XmlEnum
public enum EditAccess {

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
	
	EditAccess(String value) {
		this.value = value;
	}
	
	public static EditAccess fromValue(String value) {
		for(EditAccess editAccess : EditAccess.values()) {
			if(editAccess.value.equals(value))
				return editAccess;
		}
		throw new IllegalArgumentException(value);
	}
}
