package com.strandls.naksha.pojo.response;

public class ObservationLocationInfoPA extends ObservationLocationInfo {

	private String protectedAreaName;
	private String province;
	private String district;

	public ObservationLocationInfoPA(String soil, String temp, String rainfall, String tahsil, String forestType,
			String protectedAreaName, String province, String district) {
		super(soil, temp, rainfall, tahsil, forestType);

		this.protectedAreaName = protectedAreaName;
		this.province = province;
		this.district = district;
	}

	public String getProtectedAreaName() {
		return protectedAreaName;
	}

	public void setProtectedAreaName(String protectedAreaName) {
		this.protectedAreaName = protectedAreaName;
	}

	public String getProvince() {
		return province;
	}

	public void setProvince(String province) {
		this.province = province;
	}

	public String getDistrict() {
		return district;
	}

	public void setDistrict(String district) {
		this.district = district;
	}
}
