package com.strandls.naksha.controller.impl;

import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.strandls.naksha.ApiConstants;
import com.strandls.naksha.controller.PermissionController;
import com.strandls.naksha.service.PermissionService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api("Permissions Service")
@Path(ApiConstants.PERMISSION)
public class PermissionControllerImpl implements PermissionController {

	@Inject
	private PermissionService permissionService;
	
	@Inject
	public PermissionControllerImpl() {
		// Default constructor
	}
	
	@Override
	@POST
	@Path("assign")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Assign permission to users", notes = "Returns succuess or failure", response = String.class)
	@ApiResponses(value = { @ApiResponse(code = 400, message = "Invalid permissions", response = String.class),
			@ApiResponse(code = 500, message = "ERROR", response = String.class) })
	//@ValidateUser
	public Response assignPersmissions(@Context HttpServletRequest request, String jsonString) {
		try {
			Map<String, String> result = permissionService.assignPermissions(request, jsonString);
			return Response.ok().entity(result).build();
		} catch (Exception e) {
			throw new WebApplicationException(
					Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
		}
	}

}
