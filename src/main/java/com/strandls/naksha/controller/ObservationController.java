package com.strandls.naksha.controller;

import javax.ws.rs.core.Response;

public interface ObservationController {

	public Response fetchMap(String speciesId, Double top, Double left, Double bottom, Double right, Double width,
			Double height);

	public Response fetchESAggs(String index, String type, String geoField, Integer precision, Double top, Double left,
			Double bottom, Double right, Long speciesId);

	public Response fetchAggsMap(Integer precision, Double top, Double left, Double bottom, Double right, Double width,
			Double height);

}
