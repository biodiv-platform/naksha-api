package com.strandls.naksha.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.google.inject.Inject;
import com.strandls.naksha.pojo.NakshaPermission;

public class NakshaPermissionDao extends AbstractDao<NakshaPermission, Long>{

	@Inject
	public NakshaPermissionDao(SessionFactory sessionFactory) {
		super(sessionFactory);
	}
	
	@Override
	public NakshaPermission findById(Long id) {
		Session session = sessionFactory.openSession();
		NakshaPermission entity = null;
		try {
			entity = session.get(NakshaPermission.class, id);
		} catch (Exception e) {
			throw e;
		} finally {
			session.close();
		}
		return entity;
	}

}
