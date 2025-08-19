package com.strandls.naksha.controller.impl;

import java.util.Map;

import com.strandls.naksha.ApiConstants;
import com.strandls.naksha.controller.PermissionController;
import com.strandls.naksha.service.PermissionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Tag(name = "Permissions Service")
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
	@Operation(summary = "Assign permission to users", description = "Returns success or failure. Accepts a JSON request body representing assignments.", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Permission assignment request as JSON (key-value map or object)", required = true, content = @Content(schema = @Schema(type = "string", format = "json"))), responses = {
			@ApiResponse(responseCode = "200", description = "Permissions assigned successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = Map.class))),
			@ApiResponse(responseCode = "400", description = "Invalid permissions request", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = String.class))) })
	// @ValidateUser
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
