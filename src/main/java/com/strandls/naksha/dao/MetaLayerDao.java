package com.strandls.naksha.dao;

import javax.inject.Inject;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.strandls.naksha.pojo.MetaLayer;

public class MetaLayerDao extends AbstractDao<MetaLayer, Long>{

	@Inject
	protected MetaLayerDao(SessionFactory sessionFactory) {
		super(sessionFactory);
	}

	@Override
	public MetaLayer findById(Long id) {
		Session session = sessionFactory.openSession();
		MetaLayer entity = null;
		try {
			entity = session.get(MetaLayer.class, id);
		} catch (Exception e) {
			throw e;
		} finally {
			session.close();
		}
		return entity;
	}
}
