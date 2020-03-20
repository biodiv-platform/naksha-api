package com.strandls.naksha.dao;

import com.google.inject.Scopes;
import com.google.inject.servlet.ServletModule;

public class DaoModule extends ServletModule {
	
	@Override
	protected void configureServlets() {
		bind(MetaLayerDao.class).in(Scopes.SINGLETON);
	}
}
