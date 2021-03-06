/**
 * 
 */
package com.xsmp.espm;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.apache.olingo.odata2.jpa.processor.api.ODataJPAContext;
import org.apache.olingo.odata2.jpa.processor.api.ODataJPAServiceFactory;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPARuntimeException;

import com.xsmp.espm.data.DataLoader;
import com.xsmp.espm.util.Utility;

/**
 * @author prodemo
 *
 */
public class ESPMServiceFactory extends ODataJPAServiceFactory {
	
	private static final String PERSISTENCE_UNIT_NAME = "ESPM";
	
	private static final String CONFIG = "Configuration";
	private static final String SHOW_DETAIL_ERROR = "showdetailerror";
	
	private static boolean first_invocation = true;
	
	/*
	 * Default destination configuration for push notification trigger
	 * 
	 * @see com.xsmp.espm.data.DataLoader
	 * @see com.xsmp.espm.util.ProductPushNotificationTrigger
	 */
	private static final String user = "no-default-user";
	private static final String password = "no-default-password";
	private static final String hostname = "hcpms-XXXXXXXtrial.hanatrial.ondemand.com";
	private static final String application_name = "no.default.application";
	private static Integer adminHTTPPort = 443;
	private static Integer adminUseHTTPS = 1;
	
	 @Override
	  public ODataJPAContext initializeODataJPAContext()
	      throws ODataJPARuntimeException {
		 
		//LocalEdmExtensions ext = new LocalEdmExtensions();
	 
	    ODataJPAContext oDatJPAContext = this.getODataJPAContext();
	    try {
	 
	      EntityManagerFactory emf = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
	      
	      // Load seed data
	      if (first_invocation) {
	    	  DataLoader m = new DataLoader( emf );
	    	  Utility.setEntityManagerFactory( emf );
	    	  m.loadData();
	    	  first_invocation = false;
	      }
	      
	      oDatJPAContext.setEntityManagerFactory(emf);
	      oDatJPAContext.setPersistenceUnitName(PERSISTENCE_UNIT_NAME);
	      
	      // This file must be located in the same folder as "WEB-INF" in the WAR archive
	      oDatJPAContext.setJPAEdmMappingModel("map-tuning.xml");
	      
	      //oDatJPAContext.setJPAEdmExtension(ext);
	      
	      setDetailErrors(true);
	      
	      //setErrorLevel();
	      
	      return oDatJPAContext;
	 
	    } catch (Exception e) {
	    	e.printStackTrace();
	    	throw new RuntimeException(e);
	    }
	 
	  }
	 
	 private void setErrorLevel()
	 { 
		 ResourceBundle config = ResourceBundle.getBundle(CONFIG); 
		 boolean error = Boolean.parseBoolean((String) config.getObject(SHOW_DETAIL_ERROR)); 
		 setDetailErrors(error); 
	 }

	public static String getUser() {
		return user;
	}

	public static String getPassword() {
		return password;
	}

	public static String getHostname() {
		return hostname;
	}

	public static String getApplicationName() {
		return application_name;
	}

	public static Integer getAdminHttpPort() {
		return adminHTTPPort;
	}

	public static Integer getAdminUseHttps() {
		return adminUseHTTPS;
	}


}
