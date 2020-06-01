package com.strandls.naksha;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContextEvent;

import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Scopes;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;
import com.strandls.esmodule.controllers.GeoServiceApi;
import com.strandls.naksha.controller.NakshaControllerModule;
import com.strandls.naksha.dao.DAOFactory;
import com.strandls.naksha.dao.DAOModule;
import com.strandls.naksha.geoserver.GeoserverModule;
import com.strandls.naksha.layers.LayerUploadModule;

public class NakshaServeletContextListener extends GuiceServletContextListener {

	private final Logger logger = LoggerFactory.getLogger(NakshaServeletContextListener.class);

	/**
	 * The maximum number of connections to maintain per route by the pooling client
	 * manager
	 */
	protected static final int MAX_CONNECTIONS_PER_ROUTE = 5;

	@Override
	protected Injector getInjector() {
		return Guice.createInjector(new ServletModule() {

			@Override
			protected void configureServlets() {

				// Start Geoserver related configurations --------------------------------------
				try {

					PoolingHttpClientConnectionManager manager = new PoolingHttpClientConnectionManager();
					manager.setDefaultMaxPerRoute(MAX_CONNECTIONS_PER_ROUTE);
					bind(PoolingHttpClientConnectionManager.class).toInstance(manager);
					Class.forName("org.postgresql.Driver");
					DAOFactory daoFactory = DAOFactory.getInstance();
					Connection connection = daoFactory.getConnection();
					bind(Connection.class).toInstance(connection);
					bind(GeoServiceApi.class).in(Scopes.SINGLETON);

				} catch (ClassNotFoundException e) {
					logger.error("Error finding postgresql driver.", e);
				} catch (SQLException e) {
					logger.error("Error getting database connection.", e);
				}

				// ------------------------ End Geoserver related configurations

				Map<String, String> props = new HashMap<String, String>();
				props.put("javax.ws.rs.Application", ApplicationConfig.class.getName());
				props.put("jersey.config.server.provider.packages", "com");
				props.put("jersey.config.server.wadl.disableWadl", "true");

				bind(ServletContainer.class).in(Scopes.SINGLETON);
				serve("/api/*").with(ServletContainer.class, props);

			}

		}, new NakshaControllerModule(), new GeoserverModule(), new LayerUploadModule(), new DAOModule());

	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		Injector injector = (Injector) sce.getServletContext().getAttribute(Injector.class.getName());

		PoolingHttpClientConnectionManager httpConnectionManger = injector
				.getInstance(PoolingHttpClientConnectionManager.class);
		if (httpConnectionManger != null) {
			httpConnectionManger.close();
		}

		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		Enumeration<Driver> drivers = DriverManager.getDrivers();
		while (drivers.hasMoreElements()) {
			Driver driver = drivers.nextElement();
			if (driver.getClass().getClassLoader() == cl) {
				try {
					logger.info("Deregistering JDBC driver {}", driver);
					DriverManager.deregisterDriver(driver);
				} catch (SQLException ex) {
					logger.error("Error deregistering JDBC driver {}", driver, ex);
				}
			} else {
				logger.trace("Not deregistering JDBC driver {} as it does not belong to this webapp's ClassLoader",
						driver);
			}
		}

		super.contextDestroyed(sce);
	}

}
