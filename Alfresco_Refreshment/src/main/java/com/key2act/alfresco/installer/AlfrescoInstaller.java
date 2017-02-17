package com.key2act.alfresco.installer;


import com.key2act.alfresco.AlfrescoException;
import com.key2act.alfresco.AlfrescoImpl;
import com.key2act.alfresco.util.FileNotFoundException;

/**
 * This is the installer class
 */
public class AlfrescoInstaller {
/**
 * Main method to call all alfresco methods
 * @param args
 * @throws AlfrescoException 
 * @throws FileNotFoundException 
 */
public static void main(String[] args) throws AlfrescoException, FileNotFoundException {
	AlfrescoImpl alfresco=new AlfrescoImpl();
	alfresco.readRepo();
	//alfresco.createSubFolder("Noorshid", "SAC-APPLICATION");
  // alfresco.uploadDocument("Noorshid/SAC-APPLICATION","applicatio/pdf","C:\\Users\\deepalisingh\\Desktop\\sac.pdf");
 // alfresco.updateVersionOfDocument("Noorshid/SAC-APPLICATION","C:\\Users\\deepalisingh\\Desktop\\Alfresco.pdf");
	                            //source path              //file name //target path
  //alfresco.dowloadTheDocument("Noorshid/SAC-APPLICATION","Test.pdf","D:\\alfrescotest");	
//	alfresco.downloadAllVersionsOfDocument("Noorshid/SAC-APPLICATION", "Alfresco.pdf","2.0", "D:\\alfrescotest");
//alfresco.deleteTheDocument("Noorshid/SAC-APPLICATION", "Test.pdf");	
//alfresco.deleteFolder("Test Alfresco");		
//alfresco.ceateLinkForFolder("Noorshid","Test","linkTest","This is the test for link");
//	alfresco.createLinkForDocument("Noorshid/SAC-APPLICATION", "Alfresco.pdf", "Test","DocumentLink","This is the document link");
	alfresco.createUserBasedPermission("Noorshid/SAC-APPLICATION/Alfresco.pdf","document", "noorshid","all");
}
}
