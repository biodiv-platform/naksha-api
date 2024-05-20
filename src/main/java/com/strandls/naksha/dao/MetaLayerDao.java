package com.strandls.naksha.dao;

import static org.hibernate.type.StandardBasicTypes.LONG;

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

	@SuppressWarnings("unchecked")
	public Long getLayerCount() {
		String queryString = "select count(*) from \"Meta_Layer_Table\" t where t.layer_status != :layerStatus";

		Session session = sessionFactory.openSession();
		Query<Long> countQuery = session.createNativeQuery(queryString).addScalar("count", LONG);

		countQuery.setParameter("layerStatus", LayerStatus.INACTIVE.name());

		Long count = countQuery.getSingleResult();
		session.close();
		return count;
	}

	@Override
	public MetaLayer findById(Long id) {
		Session session = sessionFactory.openSession();
		MetaLayer entity = null;
		try {
			entity = session.get(MetaLayer.class, id);
		} finally {
			session.close();
		}
		return entity;
	}

	@SuppressWarnings("unchecked")
	public String isTableAvailable(String layerTableName) {
		String queryStr = "select table_name from information_schema.tables where " + "table_name = :layerTableName";

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
		String queryStr = "from MetaLayer t where layerTableName = :layerTableName";
		Session session = sessionFactory.openSession();
		Query<MetaLayer> query = session.createQuery(queryStr, MetaLayer.class);
		query.setParameter("layerTableName", layerTableName);
		MetaLayer resultList = query.getSingleResult();
		session.close();
		return resultList;
	}

	public List<MetaLayer> getAllInactiveLayer() {
		String queryStr = "from MetaLayer t where t.layerStatus = :value order by id";
		Session session = sessionFactory.openSession();
		Query<MetaLayer> query = session.createQuery(queryStr, MetaLayer.class);
		query.setParameter("value", LayerStatus.INACTIVE);

		try {
			List<MetaLayer> resultList = new ArrayList<>();
			resultList = query.getResultList();
			return resultList;
		} finally {
			session.close();
		}
	}

	// @Override
	public List<MetaLayer> findAll(int limit, int offset, Long portal) {
		String queryStr = "from MetaLayer t where t.layerStatus != :value and portalId = :portalId order by id";
		Session session = sessionFactory.openSession();
		Query<MetaLayer> query = session.createQuery(queryStr, MetaLayer.class);
		query.setParameter("value", LayerStatus.INACTIVE);
		query.setParameter("portalId", portal);

		try {
			List<MetaLayer> resultList = new ArrayList<>();
			if (limit > 0 && offset >= 0)
				query = query.setFirstResult(offset).setMaxResults(limit);
			resultList = query.getResultList();
			return resultList;
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
		} finally {
			session.close();
		}
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
		String queryStr = "SELECT " + MetaLayerService.STATE_COLUMN_NAME + ", " + MetaLayerService.DISTRICT_COLUMN_NAME
				+ ", " + MetaLayerService.TAHSIL_COLUMN_NAME + " from " + MetaLayerService.INDIA_TAHSIL
				+ " where st_contains" + "(" + MetaLayerService.INDIA_TAHSIL + "."
				+ MetaLayerService.GEOMETRY_COLUMN_NAME + ", ST_GeomFromText('POINT(" + x + " " + y + ")',0))";

		Session session = sessionFactory.openSession();
		Query query = session.createNativeQuery(queryStr);

		List<Object[]> entity;
		try {
			entity = query.getResultList();
		} finally {
			session.close();
		}
		return entity;
	}

	@SuppressWarnings("unchecked")
	public String findSRID(String layerTableName) {
		String queryStr = "select 'EPSG:' || Find_SRID(:schema, :table, :column) as srid";

		Session session = sessionFactory.openSession();
		Query<String> query = session.createNativeQuery(queryStr);
		query.setParameter("schema", "public");
		query.setParameter("table", layerTableName);
		query.setParameter("column", "wkb_geometry");
		try {
			String result = query.getSingleResult();
			return "EPSG:0".equals(result) ? null : result;
		} finally {
			session.close();
		}
	}
}
