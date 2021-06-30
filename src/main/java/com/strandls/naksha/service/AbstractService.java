package com.strandls.naksha.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.strandls.naksha.dao.AbstractDao;

public abstract class AbstractService<T> {

	private AbstractDao<T, Long> dao;
	private final Logger logger = LoggerFactory.getLogger(AbstractService.class);

	protected AbstractService(AbstractDao<T, Long> dao) {
		logger.info("\nAbstractService constructor");
		this.dao = dao;
	}

	public T save(T entity) {
		this.dao.save(entity);
		return entity;
	}

	public T update(T entity) {
		this.dao.update(entity);
		return entity;
	}

	public T delete(Long id) {
		T entity = this.dao.findById(id);
		this.dao.delete(entity);
		return entity;
	}

	public T findById(Long id) {
		return this.dao.findById(id);
	}

	public List<T> findAll() {
		return this.dao.findAll();
	}

	public List<T> findAll(int limit, int offset) {
		return  this.dao.findAll(limit, offset);
	}

	public T findByPropertyWithCondtion(String property, String value, String condition) {
		return dao.findByPropertyWithCondition(property, value, condition);
	}

	public List<T> getByPropertyWithCondtion(String property, Object value, String condition, int limit, int offset) {
		return dao.getByPropertyWithCondtion(property, value, condition, limit, offset);
	}
}
