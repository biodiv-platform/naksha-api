package com.strandls.naksha.service.impl;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import com.strandls.naksha.dao.NakshaPermissionDao;
import com.strandls.naksha.pojo.NakshaPermission;
import com.strandls.naksha.pojo.enumtype.PermissionType;
import com.strandls.naksha.service.AbstractService;
import com.strandls.naksha.service.PermissionService;

public class PermissionServiceImpl extends AbstractService<NakshaPermission> implements PermissionService {

	@Inject
	public PermissionServiceImpl(NakshaPermissionDao dao) {
		super(dao);
	}

	@Override
	public Map<String, String> assignPermissions(HttpServletRequest request, String jsonString) {
		JSONObject jsonObject = new JSONObject(jsonString);

		JSONArray userIdArray = jsonObject.getJSONArray("userIds");
		JSONArray permissionArray = jsonObject.getJSONArray("permissions");
		String resourceType = jsonObject.getString("resourceType");
		BigInteger resourceId = jsonObject.getBigInteger("resourceId");
		String type = "biodiv";

		// TODO : validate the users and permissions

		Map<String, String> permissionMatrix = new HashMap<String, String>();

		userIdArray.forEach(userId -> permissionArray.forEach(permission -> {
			NakshaPermission nakshaPermission = new NakshaPermission();
			nakshaPermission.setPermission(PermissionType.fromValue(permission.toString()));
			nakshaPermission.setResourceType(resourceType);
			nakshaPermission.setResourceId(resourceId.longValue());
			nakshaPermission.setUserId(Long.parseLong(userId.toString()));
			nakshaPermission.setType(type);
			save(nakshaPermission);
			permissionMatrix.put(userId.toString(), permission.toString());
		}));

		return permissionMatrix;
	}

}
