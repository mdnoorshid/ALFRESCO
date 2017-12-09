package com.reactiveworks.learning.alfresco.activity;

import java.io.IOException;

import org.apache.chemistry.opencmis.commons.data.RepositoryInfo;

import com.reactiveworks.learning.alfresco.util.FileNotExistError;

/**
 * Interface defining the activity for alfresco
 * 
 * @author Md Noorshid
 *
 */
public interface IAlfrescoActivity {
	RepositoryInfo readRepo() throws FileNotFound;

	void createFolder(String folderName) throws FileNotFound, FolderAlreadyExist;

	void createSubFolder(String mainFolderName, String subFolderName) throws FileNotFound, FolderAlreadyExist;

	void uploadDocument(String folderPath, String fileType, String filePath)
			throws FileNotFound, DocumentAlreadyExist, FolderAlreadyExist;

	void createDocument(String folderPath, String fileName, String contents, String fileType, String version)
			throws FileNotFound, FolderAlreadyExist;

	void deleteDocument(String sourceFolderPath, String documentName) throws FileNotFound, DocumentNotExist;

	void deleteFolder(String targetFolderPath) throws FileNotFound;

	void updateVersionOfDocument(String folderPath, String filePath, String versioningStateType)
			throws FileNotFound, VersionStateMissMatched;

	void downloadLatestDocument(String sourceFolderPath, String documentName, String targetFolderPath)
			throws FileNotFound, ContentException, IOException;

	void downloadDocumentWithVersionType(String sourceFolderPath, String documentName, String version,
			String targetFolderPath) throws FileNotExistError, FileNotFound, ContentException;

	void createLinkForFolder(String sourceFolderPath, String destinationFolderName, String linkName,
			String linkDescription) throws FileNotFound;

	void createLinkForDocument(String sourceFolderPath, String documentName, String destinationFolderPath,
			String linkName, String linkDescription) throws FileNotFound;

	void createUserBasedPermissionForDocument(String sourcePath,String documentName ,String userName, String permissionType) throws FileNotFound;
    
	void createUserBasedPermissionForFolder(String sourceFolderPath,String userName,String permissionType) throws FileNotFound;

}
