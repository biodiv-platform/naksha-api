package com.strandls.naksha.controller.impl;

import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import com.strandls.naksha.controller.PermissionController;
import com.strandls.naksha.service.PermissionService;

public class PermissionControllerImpl implements PermissionController {

	@Inject
	private PermissionService permissionService;
	
	@Inject
	public PermissionControllerImpl() {
	}
	
	@Override
	public String ping() {
		return "pong";
	}

	@Override
	public Response assignPersmissions(HttpServletRequest request, String jsonString) {
		try {
			Map<String, String> result = permissionService.assignPermissions(request, jsonString);
			return Response.ok().entity(result).build();
		} catch (Exception e) {
			throw new WebApplicationException(
					Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
		}
	}

}
