package se325.example12.parolee.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * Singleton class that manages an EntityManagerFactory. When a
 * PersistenceManager is instantiated, it creates an EntityManagerFactory. An
 * EntityManagerFactory is required to create an EntityManager, which represents
 * a persistence context (session with a database). 
 * 
 * When a Web service application component (e.g. a resource object) requires a 
 * persistence context, it should call the PersistentManager's 
 * createEntityManager() method to acquire one.
 * 
 */
public class PersistenceManager {
	private static PersistenceManager instance = null;
	
	private EntityManagerFactory entityManagerFactory;
	private static final Logger LOGGER = LoggerFactory.getLogger(PersistenceManager.class);
	
	protected PersistenceManager() {
		try {
			entityManagerFactory = Persistence.createEntityManagerFactory("se325.parolee");
		} catch (Exception ex) {
			LOGGER.error("Failed to create persistence manager", ex);
			throw ex;
		}
	}
	
	public EntityManager createEntityManager() {
		return entityManagerFactory.createEntityManager();
	}
	
	public static PersistenceManager instance() {
		if(instance == null) {
			instance = new PersistenceManager();
		}
		return instance;
	}

	/**
	 * Wipes the database.
	 */
	public void reset() {
		entityManagerFactory.close();
		entityManagerFactory = Persistence.createEntityManagerFactory("se325.parolee");
	}

}
