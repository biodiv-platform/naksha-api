package com.strandls.naksha.dao;

import javax.inject.Inject;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.strandls.naksha.pojo.MetaLayer;
import com.strandls.naksha.pojo.Portal;
import com.strandls.naksha.pojo.layerPortalMapping;

public class LayerPortalDao extends AbstractDao<layerPortalMapping, Long> {

	@Inject
	protected LayerPortalDao(SessionFactory sessionFactory) {
		super(sessionFactory);
	}

	@Override
	public layerPortalMapping findById(Long id) {
		Session session = sessionFactory.openSession();
		layerPortalMapping entity = null;
		try {
			entity = session.get(layerPortalMapping.class, id);
		} finally {
			session.close();
		}
		return entity;
	}

}
