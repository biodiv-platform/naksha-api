package com.strandls.naksha.dao;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class DaoModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(MetaLayerDao.class).in(Scopes.SINGLETON);
	}
}
