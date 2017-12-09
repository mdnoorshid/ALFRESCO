package com.reactiveworks.learning.alfresco.installer;

import java.io.IOException;

import com.reactiveworks.learning.alfresco.activity.AlfrescoActivityImpl;
import com.reactiveworks.learning.alfresco.activity.ContentException;
import com.reactiveworks.learning.alfresco.activity.FileNotFound;
import com.reactiveworks.learning.alfresco.activity.FolderAlreadyExist;
import com.reactiveworks.learning.alfresco.activity.VersionStateMissMatched;

/**
 * Installer Class for Alfresco
 *
 */
public class AlfrescoInstaller {
	public static void main(String[] args)
			throws FileNotFound, VersionStateMissMatched, ContentException, IOException, FolderAlreadyExist {
		AlfrescoActivityImpl alfrescoActivityImpl = new AlfrescoActivityImpl();
		// alfrescoActivityImpl.createFolder("PermisssionTest");
		// alfrescoActivityImpl.createSubFolder("LinkTest","TargetFolder");
		// alfrescoActivityImpl.createSubFolder("LinkTest","SourceFolder");
		// alfrescoActivityImpl.uploadDocument("TEST","text/plain","C:\\Users\\Niyamat\\Desktop\\ConfigurationTable.txt");
		//   alfrescoActivityImpl.createDocument("PermisssionTest", "PermissinTest", "Testing document for permissin based on user", "text/plain", "MAJOR");
		 //  alfrescoActivityImpl.createDocument("ACT/WORK",
		// "FinalTest","text/plain", "MINOR");
		// alfrescoActivityImpl.deleteDocument("ACT/WORK", "FinalTest");
		// alfrescoActivityImpl.deleteFolder("Md Noorshid");
		// alfrescoActivityImpl.updateVersionOfDocument("TEST","C:\\Users\\Niyamat\\Desktop\\ConfigurationTable.txt","MAJOR");
		// alfrescoActivityImpl.readRepo();
		// alfrescoActivityImpl.downloadLatestDocument("TEST","ConfigurationTable.txt","C:\\Users\\Niyamat\\Desktop\\DROPBOX");
		// alfrescoActivityImpl.downloadDocumentWithVersionType("TEST","ConfigurationTable.txt","6.0","C:\\Users\\Niyamat\\Desktop\\DROPBOX");
		/*alfrescoActivityImpl.createLinkForFolder("LinkTest/SourceFolder", "LinkTest/TargetFolder", "LinkFolder6",
				"Testing Link of source Folder");*/
		//alfrescoActivityImpl.createLinkForDocument("LinkTest/SourceFolder", "TestDocumentLink", "LinkTest/TargetFolder", "LinkDocument4", "Link for Document testing");
		//alfrescoActivityImpl.createUserBasedPermissionForDocument("PermisssionTest", "PermissinTest", "mdnoorshid", "read");
		alfrescoActivityImpl.createUserBasedPermissionForFolder("PermisssionTest", "mdnoorshid", "all");
		alfrescoActivityImpl.readRepo();
		

	}
}
