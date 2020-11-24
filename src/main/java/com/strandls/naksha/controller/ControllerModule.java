/**
 * 
 */
package com.strandls.naksha.controller;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.strandls.naksha.controller.impl.GeoserverControllerImpl;
import com.strandls.naksha.controller.impl.LayerControllerImpl;
import com.strandls.naksha.controller.impl.ObservationControllerImpl;
import com.strandls.naksha.controller.impl.PermissionControllerImpl;

/**
 * 
 * @author vilay
 *
 */
public class ControllerModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(LayerController.class).to(LayerControllerImpl.class).in(Scopes.SINGLETON);
		bind(GeoserverController.class).to(GeoserverControllerImpl.class).in(Scopes.SINGLETON);
		bind(ObservationController.class).to(ObservationControllerImpl.class).in(Scopes.SINGLETON);
		bind(PermissionController.class).to(PermissionControllerImpl.class).in(Scopes.SINGLETON);
	}
}
