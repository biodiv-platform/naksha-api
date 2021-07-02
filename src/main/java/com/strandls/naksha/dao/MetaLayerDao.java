package com.strandls.naksha.dao;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.NoResultException;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import com.strandls.naksha.pojo.MetaLayer;
import com.strandls.naksha.pojo.enumtype.LayerStatus;
import com.strandls.naksha.service.MetaLayerService;

public class MetaLayerDao extends AbstractDao<MetaLayer, Long> {

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
			e.printStackTrace();
			throw e;
		} finally {
			session.close();
		}
		return entity;
	}
	
	@SuppressWarnings("unchecked")
	public String isTableAvailable(String layerTableName) {
		String queryStr = "select table_name from information_schema.tables where "
				+ "table_name = :layerTableName";
		
		Session session = sessionFactory.openSession();		
		try {
			Query<String> query = session.createNativeQuery(queryStr);
			query.setParameter("layerTableName", layerTableName);
			return query.getSingleResult();
		} catch (NoResultException e) {
			return null;
		} finally {
			session.close();
		}		

	}

	public MetaLayer findByLayerTableName(String layerTableName) {
		String queryStr = "from " + daoType.getSimpleName() + " t where layerTableName = :layerTableName";
		Session session = sessionFactory.openSession();
		Query<MetaLayer> query = session.createQuery(queryStr, MetaLayer.class);
		query.setParameter("layerTableName", layerTableName);
		MetaLayer resultList = query.getSingleResult();
		session.close();
		return resultList;
	}

	public List<MetaLayer> getAllInactiveLayer() {
		String queryStr = "from " + daoType.getSimpleName() + " t " + "where t.layerStatus = :value order by id";
		Session session = sessionFactory.openSession();
		Query<MetaLayer> query = session.createQuery(queryStr, MetaLayer.class);
		query.setParameter("value", LayerStatus.INACTIVE);

		try {
			List<MetaLayer> resultList = new ArrayList<>();
			resultList = query.getResultList();
			return resultList;
		} catch (NoResultException e) {
			e.printStackTrace();
			throw e;
		} finally {
			session.close();
		}
	}

	@Override
	public List<MetaLayer> findAll(int limit, int offset) {
		String queryStr = "" + "from " + daoType.getSimpleName() + " t " + "where t.layerStatus != :value order by id";
		Session session = sessionFactory.openSession();
		Query<MetaLayer> query = session.createQuery(queryStr, MetaLayer.class);
		query.setParameter("value", LayerStatus.INACTIVE);

		try {
			List<MetaLayer> resultList = new ArrayList<>();
			if (limit > 0 && offset >= 0)
				query = query.setFirstResult(offset).setMaxResults(limit);
			resultList = query.getResultList();
			return resultList;
		} catch (NoResultException e) {
			e.printStackTrace();
			throw e;
		} finally {
			session.close();
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List<Object> executeQueryForSingleResult(String attribute, String layerName, String lon, String lat) {
		Double x = Double.parseDouble(lon);
		Double y = Double.parseDouble(lat);
		String queryStr = "SELECT " + attribute + " from " + layerName + " where st_contains" + "(" + layerName + "."
				+ MetaLayerService.GEOMETRY_COLUMN_NAME + ", ST_GeomFromText('POINT(" + x + " " + y + ")',0))";
		Session session = sessionFactory.openSession();
		Query query = session.createNativeQuery(queryStr);
		List<Object> entity;
		try {
			entity = query.getResultList();
		} catch (NoResultException e) {
			e.printStackTrace();
			throw e;
		} finally {
			session.close();
		}
		return entity;
	}

	@SuppressWarnings("rawtypes")
	public String getBoundingBox(String layerTableName) {
		Session session = sessionFactory.openSession();
		String queryStr = "select ST_ASTEXT(ST_Extent(wkb_geometry)) BBOX from " + layerTableName;
		Query query = session.createNativeQuery(queryStr);
		String bboxWkt;
		try {
			bboxWkt = (String) query.getSingleResult();
		} catch (NoResultException e) {
			e.printStackTrace();
			throw e;
		}
		session.close();
		return bboxWkt;
	}

	@SuppressWarnings({ "rawtypes" })
	public void dropTable(String layerName) {
		Session session = sessionFactory.openSession();
		Transaction tx = session.beginTransaction();
		Query query = session.createNativeQuery("drop table " + layerName);
		query.executeUpdate();
		tx.commit();
		session.close();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List<Object[]> executeQueryForLocationInfo(String lat, String lon) {
		Double x = Double.parseDouble(lon);
		Double y = Double.parseDouble(lat);
		String queryStr = "SELECT state,district,tahsil from " + MetaLayerService.INDIA_TAHSIL + " where st_contains"
				+ "(" + MetaLayerService.INDIA_TAHSIL + "." + MetaLayerService.GEOMETRY_COLUMN_NAME
				+ ", ST_GeomFromText('POINT(" + x + " " + y + ")',0))";

		Session session = sessionFactory.openSession();
		Query query = session.createNativeQuery(queryStr);
		List<Object[]> entity;
		try {
			entity = query.getResultList();
		} catch (NoResultException e) {
			e.printStackTrace();
			throw e;
		}
		session.close();
		return entity;
	}


}
