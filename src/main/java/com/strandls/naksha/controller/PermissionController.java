package com.strandls.naksha.controller;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.strandls.authentication_utility.filter.ValidateUser;
import com.strandls.naksha.ApiConstants;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * 
 * 
 */

@Api("Permissions Service")
@Path(ApiConstants.PERMISSION)
public interface PermissionController {

	@GET
	@Path("ping")
	@Produces(MediaType.TEXT_PLAIN)
	public String ping();
	
	@POST
	@Path("assign")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Assign permission to users", notes = "Returns succuess or failure", response = String.class)
	@ApiResponses(value = { @ApiResponse(code = 400, message = "Invalid permissions", response = String.class),
			@ApiResponse(code = 500, message = "ERROR", response = String.class) })
	@ValidateUser
	public Response assignPersmissions(@Context HttpServletRequest request, String jsonString);
}