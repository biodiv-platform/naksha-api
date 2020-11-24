package com.strandls.naksha.service;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

public interface PermissionService {

	public Map<String, String> assignPermissions(HttpServletRequest request, String jsonString);
}
