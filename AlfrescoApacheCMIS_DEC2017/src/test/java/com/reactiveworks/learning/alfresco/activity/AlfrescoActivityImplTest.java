package com.reactiveworks.learning.alfresco.activity;

import static org.junit.Assert.*;

import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.data.RepositoryInfo;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.*;

import com.reactiveworks.learning.alfresco.util.AlfrescoConnection;
import com.reactiveworks.learning.alfresco.util.FileNotExistError;


public class AlfrescoActivityImplTest {
	static Logger logger=Logger.getLogger(AlfrescoActivityImplTest.class);

	static Session session;
	static AlfrescoActivityImpl alfrescoActivityImpl;
	

	@BeforeClass
	public static void getSession() throws FileNotExistError {
		session = AlfrescoConnection.getSession();
		logger.info("SESSION STARTED");
	}

	@AfterClass
	public static void closeSession() throws FileNotExistError {
		session.clear();
		logger.info("SESSION CLEARED!!");	
	}
	
	@Before
	public  void getActivityObject(){
		alfrescoActivityImpl=new AlfrescoActivityImpl();
	}

	@Test
	public void createFolderTest() throws FileNotFound, FolderAlreadyExist {
		alfrescoActivityImpl.createFolder("TESTJUNIT");
		RepositoryInfo readRepo = alfrescoActivityImpl.readRepo();
		assertEquals("TESTJUNIT", readRepo.getName());
	}

}
