package com.key2act.alfresco.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.log4j.Logger;
import static com.key2act.alfresco.util.AlfrescoConstant.*;

/**
 * This class is to get the connection for Alfresco
 * @author bizruntime
 *
 */
public class AlfrescoConnection {
	static Logger logger=Logger.getLogger(AlfrescoConnection.class);

	/**
	 * This method is to get the session object
	 * @return
	 * @throws FileNotFoundException 
	 */
	public static Session getSession() throws FileNotFoundException{
		Config config=new Config();
		//Passing connection Parameters
		Map<String, String>sessionParamter=new HashMap<String, String>();
		sessionParamter.put(SessionParameter.USER,config.getPropValues("USER"));
		sessionParamter.put(SessionParameter.PASSWORD,config.getPropValues("USER"));
		sessionParamter.put(SessionParameter.ATOMPUB_URL, ATOMPUB_URL);
		sessionParamter.put(SessionParameter.BINDING_TYPE,BindingType.ATOMPUB.value());
		
		//Creating Session
		SessionFactory sessionFactory=SessionFactoryImpl.newInstance();
		Session session=sessionFactory.getRepositories(sessionParamter).get(0).createSession();
		logger.info("session created successfully.....");
		return session;
	}
	
}
