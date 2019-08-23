package com.strandls.naksha.layers;

import com.google.inject.Singleton;
import com.google.inject.servlet.ServletModule;
import com.strandls.naksha.layers.scripts.DBexec;
import com.strandls.naksha.layers.scripts.Import_data;

public class LayerUploadModule extends ServletModule {

	@Override
	protected void configureServlets() {
		bind(LayerUploadService.class).in(Singleton.class);
		bind(Import_data.class).in(Singleton.class);
		bind(DBexec.class).in(Singleton.class);
	}
}
