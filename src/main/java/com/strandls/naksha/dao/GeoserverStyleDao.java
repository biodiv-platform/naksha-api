package com.strandls.naksha.dao;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.NoResultException;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

public class GeoserverStyleDao {

	@Inject
	private SessionFactory sessionFactory;
	
	private static final String TABLE_NAME = "tableName";

	@Inject
	public GeoserverStyleDao() {
		// default constructor
	}
	
	@SuppressWarnings("unchecked")
	public List<Object[]> getColumnTypes(String tableName) {
		Session session = sessionFactory.openSession();
		String queryStr = "select column_name, data_type from information_schema.columns where table_name =:tableName";
		Query<Object[]> query = session.createNativeQuery(queryStr);
		query.setParameter(TABLE_NAME, tableName);
		List<Object[]> entity = query.getResultList();
		session.close();
		return entity;
	}

	@SuppressWarnings("unchecked")
	public List<Object[]> getColumnNames(String tableName) {
		
		Session session = sessionFactory.openSession();
		String queryStr = "SELECT c.column_name, pgd.description, c.data_type "
				+ "from pg_catalog.pg_statio_all_tables as st "
				+ "inner join pg_catalog.pg_description pgd on (pgd.objoid=st.relid) "
				+ "right outer join information_schema.columns c on (pgd.objsubid=c.ordinal_position and  c.table_schema=st.schemaname and c.table_name=st.relname) "
				+ "where table_schema = 'public' and table_name = :tableName and pgd.description is not null";
		Query<Object[]> query = session.createNativeQuery(queryStr);
		query.setParameter(TABLE_NAME, tableName);
		List<Object[]> entity = query.getResultList();
		session.close();
		return entity;
		
	}

	@SuppressWarnings("unchecked")
	public String getColumnType(String tableName, String columnName) {
		String queryStr = "select data_type from information_schema.columns where table_name = :tableName and column_name = :columnName";
		Session session = sessionFactory.openSession();
		Query<Object> query = session.createNativeQuery(queryStr);
		query.setParameter(TABLE_NAME, tableName);
		query.setParameter("columnName", columnName);
		String entity;
		try {
			entity = (String) query.getSingleResult();
		} catch (NoResultException e) {
			e.printStackTrace();
			throw e;
		}
		session.close();
		return entity;
	}

	public List<Object[]> getDistinctValues(String tableName, String columnName) {
		String queryStr = "select distinct(\"" + columnName + "\") from " + tableName;
		return executeQuery(queryStr);
	}

	public List<Object[]> getMinMaxValues(String tableName, String columnName) {
		String queryStr = "select min(\"" + columnName + "\"), max(\"" + columnName + "\") from " + tableName;
		return executeQuery(queryStr);
	}

	@SuppressWarnings("unchecked")
	private List<Object[]> executeQuery(String queryStr) {
		Session session = sessionFactory.openSession();
		Query<Object[]> query = session.createNativeQuery(queryStr);
		List<Object[]> entity;
		entity = query.getResultList();
		session.close();
		return entity;
	}
}
