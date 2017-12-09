package com.reactiveworks.learning.alfresco.util;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;


import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.log4j.Logger;
import static com.reactiveworks.learning.alfresco.util.AlfrescoConstant.*;

/**
 * Connection Class of Alfresco
 * 
 * @author Md Noorshid
 *
 */
public class AlfrescoConnection {
	static Logger logger = Logger.getLogger(AlfrescoConnection.class);

	/**
	 * This method to get the session
	 * 
	 * @return
	 * @throws FileNotExistError 
	 */
	public static Session getSession() throws FileNotExistError {
		Config config = new Config();
		Map<String,String> sessionParameters=new HashMap<String, String>();
		Session session=null;
		try {
			//Passing Connection Properties
			sessionParameters.put(SessionParameter.USER, config.getPropValues("USER"));
			sessionParameters.put(SessionParameter.PASSWORD, config.getPropValues("PASSWORD"));
            sessionParameters.put(SessionParameter.ATOMPUB_URL,ATOMPUB_URL);			
			sessionParameters.put(SessionParameter.BINDING_TYPE,BindingType.ATOMPUB.value());
			
			//Creating Session
			SessionFactory sessionFactory=SessionFactoryImpl.newInstance();
			session=sessionFactory.getRepositories(sessionParameters).get(0).createSession();
			logger.info("SESSION CREATED SUCCESSFULLY");
			
		} catch (FileNotFoundException e) {
			throw new FileNotExistError("Properties file not found",e);
		}
		return session;
	}

}
