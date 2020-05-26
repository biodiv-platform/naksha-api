package com.strandls.naksha.dao;

import com.google.inject.Singleton;
import com.google.inject.servlet.ServletModule;

public class DAOModule extends ServletModule {
	
	@Override
	protected void configureServlets() {
		bind(LayerDAO.class).to(LayerDAOJDBC.class).in(Singleton.class);
	}
}