package com.key2act.alfresco;

import com.key2act.alfresco.util.FileNotFoundException;

/**
 * This interface contains all methods for alfresco
 * @author deepalisingh
 *
 */
public interface IAlfresco {
   //Method have to implement for reading the Repo of alfresco
	void readRepo() throws AlfrescoException;
       //Method have to implement for create the folder	
	void createFolder(String folderName) throws AlfrescoException;
     //Method have to implement for create sub folder	
	void createSubFolder(String folderName,String subFolderName) throws AlfrescoException;
    //Method have to implement for upload the document	
	void uploadDocument(String folderPath,String fileType,String filePath) throws AlfrescoException;
   //Method have to implement for update the document	
	void updateVersionOfDocument(String folderPath, String filePath) throws FileNotFoundException;
  //Method have to implement for download the Document
	void dowloadTheDocument(String SourceFolderPath,String documentName,String targetFolderPath) throws AlfrescoException ;
	//Method have to implement for download all versions of Document
	void downloadAllVersionsOfDocument(String sourceFolderPath,String documentName,String version,String targetFolderPath) throws AlfrescoException;
	//Method have to implement for deleting the Document
	void deleteTheDocument(String sourceFolderPath,String documentName) throws AlfrescoException;
	//Method have to implement for deleting the Folder
	void deleteFolder(String targetFolderPath) throws AlfrescoException;
	//Method have to implement for Creating link for folder
	void createLinkForFolder(String sourceFolderPath,String destinationFolderName,String linkName,String linkDescription) throws AlfrescoException;
	//Method have to implement for creating link for document
	void createLinkForDocument(String sourceFolderPath,String documentName,String destinationFolderName,String linkName,String linkDescription) throws AlfrescoException;
}
