package com.strandls.naksha.dao;

import java.util.List;

import javax.persistence.NoResultException;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import com.google.inject.Inject;

public class GeoserverStyleDao {
	
	@Inject
	private SessionFactory sessionFactory;
	
	public List<Object[]> getColumnTypes(String tableName) {
		String queryStr = "select column_name, data_type from information_schema.columns where table_name = '"
				+ tableName + "'";
		return executeQuery(queryStr);
	}
	
	public String getColumnType(String tableName, String columnName) {
		String queryStr = "select data_type from information_schema.columns where table_name = '"
				+ tableName + "' and column_name = '" + columnName + "'";
		Session session = sessionFactory.openSession();
		Query query = session.createNativeQuery(queryStr);
		String entity;
		try {
			entity = (String) query.getSingleResult();
		} catch(NoResultException e) {
			throw e;
		}
		session.close();
		return entity;
	}

	public List<Object[]> getDistinctValues(String tableName, String columnName) {
		String queryStr = "select distinct(" + columnName + ") from " + tableName;
		return executeQuery(queryStr);
	}

	public List<Object[]> getMinMaxValues(String tableName, String columnName) {
		String queryStr = "select min(" + columnName + "), max(" + columnName + ") from " + tableName;
		return executeQuery(queryStr);
	}
	
	private List<Object[]> executeQuery(String queryStr) {
		Session session = sessionFactory.openSession();
		Query query = session.createNativeQuery(queryStr);
		List<Object[]> entity;
		try {
			entity = (List<Object[]>) query.getResultList();
		} catch(NoResultException e) {
			throw e;
		}
		session.close();
		return entity;
	}
}
