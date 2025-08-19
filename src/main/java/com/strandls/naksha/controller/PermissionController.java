package com.strandls.naksha.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.Response;

/** */
public interface PermissionController {

	public Response assignPersmissions(HttpServletRequest request, String jsonString);
}
