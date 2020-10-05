package com.strandls.naksha.dao;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.NoResultException;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import com.strandls.naksha.pojo.MetaLayer;
import com.strandls.naksha.service.MetaLayerService;

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

	public List<Object> executeQueryForSingleResult(String queryStr) {
		Session session = sessionFactory.openSession();
		Query query = session.createNativeQuery(queryStr);
		List<Object> entity;
		try {
			entity = (List<Object>) query.getResultList();
		} catch(NoResultException e) {
			throw e;
		}
		session.close();
		return entity;
	}
}
