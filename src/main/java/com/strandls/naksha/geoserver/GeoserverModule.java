package com.strandls.naksha.geoserver;

import com.google.inject.Scopes;
import com.google.inject.servlet.ServletModule;

public class GeoserverModule extends ServletModule {

	@Override
	protected void configureServlets() {
		bind(GeoServerIntegrationService.class).in(Scopes.SINGLETON);
		bind(GeoserverService.class).in(Scopes.SINGLETON);
	}
}