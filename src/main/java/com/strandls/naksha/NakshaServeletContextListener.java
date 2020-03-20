package com.strandls.naksha;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContextEvent;

import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Scopes;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;
import com.strandls.esmodule.controllers.GeoServiceApi;
import com.strandls.naksha.controller.NakshaControllerModule;
import com.strandls.naksha.dao.DaoModule;
import com.strandls.naksha.geoserver.GeoserverModule;
import com.strandls.naksha.service.ServiceModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;

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
				//try {

					/*
					 * PoolingHttpClientConnectionManager manager = new
					 * PoolingHttpClientConnectionManager();
					 * manager.setDefaultMaxPerRoute(MAX_CONNECTIONS_PER_ROUTE);
					 * bind(PoolingHttpClientConnectionManager.class).toInstance(manager);
					 * Class.forName("org.postgresql.Driver"); DAOFactory daoFactory =
					 * DAOFactory.getInstance(); Connection connection = daoFactory.getConnection();
					 * bind(Connection.class).toInstance(connection);
					 * bind(ObjectMapper.class).in(Scopes.SINGLETON);
					 */
					
					
					Configuration configuration = new Configuration();

					try {
						for (Class<?> cls : getEntityClassesFromPackage("com")) {
							configuration.addAnnotatedClass(cls);
						}
					} catch (ClassNotFoundException | IOException | URISyntaxException e) {
						e.printStackTrace();
						logger.error(e.getMessage());
					}

					configuration = configuration.configure();
					SessionFactory sessionFactory = configuration.buildSessionFactory();

					ObjectMapper objectMapper = new ObjectMapper();
					bind(ObjectMapper.class).toInstance(objectMapper);
					
					Map<String, String> props = new HashMap<String, String>();
					props.put("javax.ws.rs.Application", ApplicationConfig.class.getName());
					props.put("jersey.config.server.wadl.disableWadl", "true");

					bind(GeoServiceApi.class).in(Scopes.SINGLETON);
					bind(SessionFactory.class).toInstance(sessionFactory);
					serve("/api/*").with(GuiceContainer.class, props);
					

				/*} catch (ClassNotFoundException e) {
					logger.error("Error finding postgresql driver.", e);
				} catch (SQLException e) {
					logger.error("Error getting database connection.", e);
				}*/

				// ------------------------ End Geoserver related configurations
			}

		},new NakshaControllerModule(), new GeoserverModule(), new DaoModule(), new ServiceModule());

	}
	
	protected List<Class<?>> getEntityClassesFromPackage(String packageName)
			throws URISyntaxException, IOException, ClassNotFoundException {

		List<String> classNames = getClassNamesFromPackage(packageName);
		List<Class<?>> classes = new ArrayList<Class<?>>();
		for (String className : classNames) {
			// logger.info(className);
			Class<?> cls = Class.forName(className);
			Annotation[] annotations = cls.getAnnotations();

			for (Annotation annotation : annotations) {
				if (annotation instanceof javax.persistence.Entity) {
					System.out.println("Mapping entity :" + cls.getCanonicalName());
					classes.add(cls);
				}
			}
		}

		return classes;
	}

	private static ArrayList<String> getClassNamesFromPackage(final String packageName)
			throws URISyntaxException, IOException {

		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		ArrayList<String> names = new ArrayList<String>();
		URL packageURL = classLoader.getResource(packageName);

		URI uri = new URI(packageURL.toString());
		File folder = new File(uri.getPath());

		Files.find(Paths.get(folder.getAbsolutePath()), 999, (p, bfa) -> bfa.isRegularFile()).forEach(file -> {
			String name = file.toFile().getAbsolutePath().replaceAll(folder.getAbsolutePath() + File.separatorChar, "")
					.replace(File.separatorChar, '.');
			if (name.indexOf('.') != -1) {
				name = packageName + '.' + name.substring(0, name.lastIndexOf('.'));
				names.add(name);
			}
		});

		return names;
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
