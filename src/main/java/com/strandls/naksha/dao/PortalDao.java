package com.strandls.naksha.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.strandls.naksha.pojo.Portal;

public class PortalDao extends AbstractDao<Portal, Long> {

	protected PortalDao(SessionFactory sessionFactory) {
		super(sessionFactory);
	}

	@Override
	public Portal findById(Long id) {
		Session session = sessionFactory.openSession();
		Portal entity = null;
		try {
			entity = session.get(Portal.class, id);
		} finally {
			session.close();
		}
		return entity;
	}

}
