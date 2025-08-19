package com.strandls.naksha;

import com.strandls.user.controller.UserServiceApi;

import jakarta.ws.rs.core.HttpHeaders;

public class Headers {

	public UserServiceApi addUserHeaders(UserServiceApi userService, String authHeader) {
		userService.getApiClient().addDefaultHeader(HttpHeaders.AUTHORIZATION, authHeader);
		return userService;
	}
}
