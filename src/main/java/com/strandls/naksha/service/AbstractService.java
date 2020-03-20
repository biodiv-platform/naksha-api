package com.strandls.naksha.service;

import java.lang.reflect.ParameterizedType;
import java.util.List;

import com.strandls.naksha.dao.AbstractDao;


public abstract class  AbstractService<T> {

	public Class<T> entityClass;
	private  AbstractDao<T, Long> dao;
	
	public AbstractService(AbstractDao<T, Long> dao) {
		System.out.println("\nAbstractService constructor");
		this.dao = dao;
		entityClass = ((Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0]);
	}

	public T save(T entity) {
		try {
			this.dao.save(entity);
			return entity;
		} catch (RuntimeException re) {
			throw re;
		}
	}

	public T update(T entity)  {
		try {
			this.dao.update(entity);
			return entity;
		} catch (RuntimeException re) {
			throw re;
		}

	}

	public T delete(Long id) {
		try {
			T entity = (T) this.dao.findById(id);
			this.dao.delete(entity);
			return entity;
		} catch (RuntimeException re) {
			throw re;
		}
	}

	public T findById(Long id) {
		try {
			T entity = (T) this.dao.findById(id);
			return entity;
		} catch (RuntimeException re) {
			throw re;
		}
	}
	
	public List<T> findAll() {
		
		try {
			List<T> entities = this.dao.findAll();
			return entities;
		} catch (RuntimeException re) {
			throw re;
		}
	}
	
	public T findByPropertyWithCondtion(String property, String value, String condition) {
		return dao.findByPropertyWithCondition(property, value, condition);
	}
	
	public List<T> getByPropertyWithCondtion(String property, Object value, String condition, int limit, int offset) {
		return dao.getByPropertyWithCondtion(property, value, condition, limit, offset);
	}
}
