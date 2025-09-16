package com.strandls.naksha.dao;

import jakarta.inject.Inject;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.strandls.naksha.pojo.MetaLayer;
import com.strandls.naksha.pojo.Portal;
import com.strandls.naksha.pojo.LayerPortalMapping;

public class LayerPortalDao extends AbstractDao<LayerPortalMapping, Long> {

	@Inject
	protected LayerPortalDao(SessionFactory sessionFactory) {
		super(sessionFactory);
	}

	@Override
	public LayerPortalMapping findById(Long id) {
		Session session = sessionFactory.openSession();
		LayerPortalMapping entity = null;
		try {
			entity = session.get(LayerPortalMapping.class, id);
		} finally {
			session.close();
		}
		return entity;
	}

}
