package com.strandls.naksha.pojo.response;

public class ObservationLocationInfoBBP extends ObservationLocationInfo {
	private String dzongkhag;
	private String geog;

	public ObservationLocationInfoBBP(String soil, String temp, String rainfall, String tahsil, String forestType,
			String dzongkhag, String geog) {
		super(soil, temp, rainfall, tahsil, forestType);
		this.dzongkhag = dzongkhag;
		this.geog = geog;
	}

	public String getDzongkhag() {
		return dzongkhag;
	}

	public void setDzongkhag(String dzongkhag) {
		this.dzongkhag = dzongkhag;
	}

	public String getGeog() {
		return geog;
	}

	public void setGeog(String geog) {
		this.geog = geog;
	}

}