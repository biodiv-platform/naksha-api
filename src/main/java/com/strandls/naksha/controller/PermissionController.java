package com.strandls.naksha.controller;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

/**
 * 
 * 
 */

public interface PermissionController {
	
	public Response assignPersmissions(HttpServletRequest request, String jsonString);
	
}