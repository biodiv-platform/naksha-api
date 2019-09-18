/**
 * 
 */
package com.strandls.naksha.controller;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

/**
 * @author Abhishek Rudra
 *
 */
public class NakshaControllerModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(GeoserverController.class).in(Scopes.SINGLETON);
		bind(LayerController.class).in(Scopes.SINGLETON);
	}
}
