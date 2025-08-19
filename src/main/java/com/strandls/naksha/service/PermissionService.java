package com.strandls.naksha.service;

import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

public interface PermissionService {

	public Map<String, String> assignPermissions(HttpServletRequest request, String jsonString);
}
