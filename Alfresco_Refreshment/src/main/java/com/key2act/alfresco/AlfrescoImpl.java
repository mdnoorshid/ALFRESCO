package com.key2act.alfresco;

import static com.key2act.alfresco.util.AlfrescoConstant.FOLDER_OBJECT_TYPE_ID;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.ObjectFactory;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.Ace;
import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.Action;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.apache.log4j.Logger;

import com.google.api.client.http.GenericUrl;
import com.key2act.alfresco.util.AlfrescoConnection;
import com.key2act.alfresco.util.FileNotFoundException;
import javax.activation.MimetypesFileTypeMap;
import static com.key2act.alfresco.util.AlfrescoConstant.*;

/**
 * This is the implementation class
 * @author bizruntime
 *
 */
public class AlfrescoImpl implements IAlfresco {
  static Logger logger=Logger.getLogger(AlfrescoImpl.class);
	 /**
	   * This method is to read the repo of ALfresco
	   * @throws AlfrescoException 
	   */
	public void readRepo() throws AlfrescoException {
		logger.debug(".inside readRepo method.... ");	
		try {
		 Session session = AlfrescoConnection.getSession();
		Folder rootFolder=session.getRootFolder();
		logger.debug("Root Folder Id, and name:::"+rootFolder.getId()+" , "+rootFolder.getName());
	     ItemIterable<CmisObject>children=rootFolder.getChildren();
	     logger.info("----------------------------Find following folders in the Repository----------------------------------------");
	     for(CmisObject cmisObject:children){
	    	 logger.debug(cmisObject.getName()+"-- Which is of type --"+cmisObject.getType().getDisplayName());  
	     }
		} catch (FileNotFoundException e) {
			throw new AlfrescoException("There is no file exception",e);
		}
		}//end of method readRepo

	  /**
	   * This method is to create folder in the Repo
	   *@param String folderName :This is the folder name by which the folder will create in the repo
	   *@throws AlfrescoException 
	   */
	public void createFolder(String folderName) throws AlfrescoException {
		 try {
				Session session=AlfrescoConnection.getSession();
				Folder folder;
				Folder rootFolder=session.getRootFolder();
				logger.debug("Root Folder Id,Name, and Path is:  "+rootFolder.getId()+" , "+rootFolder.getName()+" , "+rootFolder.getPath());
				//Defining Properties of Folder
				Map<String,Object>folderPropersties=new HashMap<String, Object>();
				folderPropersties.put(PropertyIds.OBJECT_TYPE_ID,FOLDER_OBJECT_TYPE_ID);
				folderPropersties.put(PropertyIds.NAME, folderName);
				folder=rootFolder.createFolder(folderPropersties);
				logger.debug("Folder created successfully.....");
			} catch (FileNotFoundException e) {
				throw new AlfrescoException("There is file not found exception!!",e);
			}
		
	}//end of method createFolder
     
	 /**
	    * This method is to create subfolder inside the folder
	    * @param folderName:Name of the Main Folder
	    * @param subFolderName: Name of the Sub Folder Name
	    *@throws AlfrescoException 
	    */
	public void createSubFolder(String folderName,String subFolderName) throws AlfrescoException {
		   try {
				 Folder folder; 
				 Folder subFolder;
				Session session=AlfrescoConnection.getSession();
				Folder rootFolder=session.getRootFolder();
				Map<String,Object>folderProperties=new HashMap<String, Object>();
				folder=(Folder) session.getObjectByPath(rootFolder.getPath()+folderName);
				folderProperties.put(PropertyIds.OBJECT_TYPE_ID, FOLDER_OBJECT_TYPE_ID);
				folderProperties.put(PropertyIds.NAME, subFolderName);
				subFolder=folder.createFolder(folderProperties);
				logger.debug("Sub Folder Created succesfully");
			} catch (FileNotFoundException e) {
				throw new AlfrescoException("There is file not found Exception",e);
			}
		
	}//end of method createSubFolder
    
	/**
	 * This method is to upload the document in a specific folder
	 * @param folderPath:Give the folder path to where the document should upload
	 * (In case of sub folder ,give path from parent folder else simply pass the name of Folder)
	 * @param fileType:Provide the file type
	 * @param filePath:Provide the filepath to which have to upload
	 */
	public void uploadDocument(String folderPath,String fileType,String filePath) {
		try {
			Session session=AlfrescoConnection.getSession();
			Folder rootFolder=session.getRootFolder();
		    Folder parentFolder=(Folder) session.getObjectByPath(rootFolder.getPath()+folderPath);
			File file=new File(filePath);
			String fileTyp=fileType;
			Map<String,Object>props=null;
			
			String fileName=file.getName();
			//create a map of properties if one was not passed in
			if(props==null){
			  props=new HashMap<String, Object>();		
			}
			
			//Add the Object Id if it was not added already
			if(props.get("cmis:objectTypeId")==null){
				props.put("cmis:objectTypeId", DOCUMENT_OBJECT_TYPE_ID);
			}
			//Add the name if it was not added already
			if(props.get("cmis:name")==null){
				props.put("cmis:name", fileName);
			}
			//Setting up a ContentStream object
			ContentStream contentStream=session.getObjectFactory().createContentStream(fileName, file.length(), fileTyp, new FileInputStream(file));
			Document document=parentFolder.createDocument(props, contentStream, null);
			logger.info("document Uploaded successfully");
		} catch (FileNotFoundException e) {
			new AlfrescoException("File not found Exception",e);
		} catch (java.io.FileNotFoundException e) {
			new AlfrescoException("File not found Exception",e);
		}
	}//end of method uploaddocument
     
	/**
	 * This method is to update the version of a document
	 * @param folderPath:Path of the folder where the document is exist
	 * (In case of sub folder ,give path from parent folder else simply pass the name of Folder)
	 * @throws FileNotFoundException 
	 */
	public void updateVersionOfDocument(String folderPath, String filePath) throws FileNotFoundException {
		 /**
		  * Getting Repository Folder or Root Folder  
		  */
		Session session=AlfrescoConnection.getSession();
		 Folder rootFolder=session.getRootFolder();
		 logger.info("Root Folder Id, and name:::"+rootFolder.getId()+" , "+rootFolder.getName());
		 
		 /**
		  * Getting Parent Folder
		  */
		 Folder parentFolder=(Folder) session.getObjectByPath(rootFolder.getPath()+folderPath);
		 logger.info("Parent Folder Path: "+parentFolder.getPath());
		 
		 //File Name which has to change the version after uploading new version
		 //Passing File Parameters
		 File file=new File(filePath);
		 String fileName=file.getName();
		 logger.debug("fileName:: "+fileName);
		 String mimeType = new MimetypesFileTypeMap().getContentType(file);
		 Document doc=(Document) session.getObjectByPath(parentFolder.getPath()+"/"+fileName);
		 logger.info("document Path: "+doc.getPaths());
		 
		 logger.info("Changing Version of "+doc.getName()+" from "+doc.getVersionLabel());
		 
		 //Now update with a new Version
		 if(doc.getAllowableActions().getAllowableActions().contains(Action.CAN_CHECK_OUT))
		 {
			//refresh()--->Reloads this object from the repository. 
			doc.refresh(); 
			String fileName2=doc.getContentStream().getFileName();
			
			//checkOut()--->Checks out the document and returns the object ID of the PWC (private working copy).
			ObjectId idOfCheckedOutDocument=doc.checkOut();
			//Returns a CMIS object from the session cache. If the object is not in the cache or the cache is turned off per default OperationContext, 
			//it will load the object from the repository and puts it into the cache.
			Document pwc=(Document) session.getObject(idOfCheckedOutDocument);
			
			//Getting Content Stream
			ContentStream contentStream=pwc.getContentStream();
			
			
			 // String fileType="application/pdf";
			  Map<String,Object> props=null;
			  
			  //create a map of properties if one was not passed in
			  if(props==null){
			   props=new HashMap<String, Object>();	  
			  }
			  
			  //Add the object ID if it was not already added
			  if(props.get("cmis:objectTypeId")==null){
				 props.put("cmis:objectTypeId","cmis:document"); 
			  }
			  
			  //Add the name if it was not already added
			  if(props.get("cmis:name")==null){
				 props.put("cmis:name",fileName); 
			  }
			
		      //Setting up a ContentStream object
			  try {
				contentStream=session.getObjectFactory().createContentStream(pwc.getName(),file.length(),mimeType,new FileInputStream(file));
			} catch (java.io.FileNotFoundException e) {
			}
			  ObjectId objectId=pwc.checkIn(true, props, contentStream,"Version Changed after uploading again the same folder");
			  doc=(Document) session.getObject(objectId);
			  logger.info("Version label is now:" + doc.getVersionLabel());
		}
	}//end of method updateVersionOfdocument
   /**
    * This method is to download the document from the folder
    * @param sourceFolderPath: Path to the Folder where document exists
    * (In case of sub folder ,give path from parent folder else simply pass the name of Folder)
    * @param dcoumentName: Name of the Document
    * @param targetFolderPath : Path of folder where the document should be download 
    * @throws AlfrescoException 
    */
	public void dowloadTheDocument(String SourceFolderPath,String documentName,String targetFolderPath) throws AlfrescoException {
        try {
			Session session=AlfrescoConnection.getSession();
			Folder rootFolder=session.getRootFolder();
			Document document=(Document) session.getObjectByPath(rootFolder.getPath()+SourceFolderPath+"/"+documentName);
			logger.debug("Document Name:: "+document.getName());
			logger.debug("Document Id:: "+document.getId());
			ContentStream downloadedDoc=document.getContentStream();
			InputStream inputStream=downloadedDoc.getStream();
			Path path=Paths.get(targetFolderPath+"\\"+documentName);
			Files.copy(inputStream, path);
			logger.debug("Document downloaded successfully...");
			
		} catch (FileNotFoundException e) {
			throw new AlfrescoException("There is no file not found exception",e);
		} catch (IOException e) {
			throw new AlfrescoException("Unable to copy the contents to the taget folder",e);
		}	
	}//end of method dowloadTheDocument

	 /**
	    * This method is to download the document from the folder
	    * @param SourceFolderPath: Path to the Folder where document exists
	    * (In case of sub folder ,give path from parent folder else simply pass the name of Folder)
	    * @param dcoumentName: Name of the Document
	    * @param version : Version of Folder to be download
	    * @param targetFolderPath : Path of folder where the document should be download 
	    * @throws AlfrescoException 
	    */
public void downloadAllVersionsOfDocument(String sourceFolderPath,String documentName,String version,String targetFolderPath) throws AlfrescoException {
	   try {
		Session session=AlfrescoConnection.getSession();
		Document document=(Document) session.getObjectByPath(session.getRootFolder().getPath()+sourceFolderPath+"/"+documentName);
		List<Document>documents=document.getAllVersions();
		for(Document doc:documents){
			logger.debug("document versions:: "+doc.getVersionLabel());
			if(doc.getVersionLabel().equals(version.trim())){
				document=doc;
				break;
			}
		}
		ContentStream downloadedDoc=document.getContentStream();
		InputStream inputStream=downloadedDoc.getStream();
		String[] docArr=documentName.split("\\.");
		Path path=Paths.get(targetFolderPath+"\\"+docArr[0]+"("+version+")."+docArr[1]);
		Files.copy(inputStream, path);
		logger.debug("Document downloaded successfully...");
	} catch (FileNotFoundException e) {
		throw new AlfrescoException("There is no file not found exception",e);  
	} catch (IOException e) {
		throw new AlfrescoException("Unable to copy the contents to the taget folder",e);
	}
}//end of method downloadAllVersionsOfDocument

/**
 * This method is to delete the document from the folder
 * @param sourceFolderPath: Path to the Folder where document exists
 * (In case of sub folder ,give path from parent folder else simply pass the name of Folder)
 * @param documentName: Name of the Document
 * @throws AlfrescoException 
 */
	public void deleteTheDocument(String sourceFolderPath, String documentName) throws AlfrescoException {
		try {
			Session session=AlfrescoConnection.getSession();
			Folder rootFolder=session.getRootFolder();
			logger.info("Root Folder Path,Id,Name: "+rootFolder.getPath()+" , "+rootFolder.getId()+" , "+rootFolder.getName());
			Folder parentFolder=(Folder) session.getObjectByPath(rootFolder.getPath()+sourceFolderPath);
			Document document=(Document) session.getObjectByPath(parentFolder.getPath()+"/"+documentName);
				document.delete();
				logger.debug("Document Deleted by Name:: "+documentName);
				throw new AlfrescoException("Document not Exist by Name:: "+documentName);
		} catch (FileNotFoundException e) {
			throw new AlfrescoException("There is no file not found exception",e);  
		}
	}
/**
 * This method is to delete the target Folder
 * @param targetFolderPath:Path of Folder to delete(In case of sub folder ,give path from parent folder else simply pass the name of Folder)
 * @throws AlfrescoException 
 */
public void deleteFolder(String targetFolderPath) throws AlfrescoException {
	try {
		Session session=AlfrescoConnection.getSession();
		Folder rootFolder=session.getRootFolder();
		Folder targetFolder=(Folder) session.getObjectByPath(rootFolder.getPath()+targetFolderPath);
		String name=targetFolder.getName();
		targetFolder.delete();
		logger.debug("Folder deleted by Name:: "+name);
	} catch (FileNotFoundException e) {
	throw new AlfrescoException("There is no file not found exception",e);  
	}
}
/**
 * This method is to create link of Folder to the 
 * @param sourceFolderPath: Path of sourceFolder for which the link should create
 * @param destinationFolderName: Name of destination folder where to create the link
 * @param linkName: Name of the link
 * @param linkDescription: Description for the link
 * @throws AlfrescoException 
 */
public void createLinkForFolder(String sourceFolderPath,String destinationFolderName,String linkName,String linkDescription) throws AlfrescoException {
	try {
		Session session=AlfrescoConnection.getSession();
		Folder sourceFolder=(Folder) session.getObjectByPath(session.getRootFolder().getPath()+sourceFolderPath);
		logger.debug("sourceFolder:: "+sourceFolder.getName());
		String objectId=sourceFolder.getId();
		logger.debug("objectId:: "+objectId);
		String sourceFolderNodeRef="workspace://SpacesStore/"+objectId;
		logger.debug("sourceFolderNodeRef:: "+sourceFolderNodeRef);
        Map<String,Object>properties=new HashMap<String, Object>();
        properties.put(PropertyIds.BASE_TYPE_ID,BaseTypeId.CMIS_ITEM.value());
        //Define name and description for the link
        properties.put(PropertyIds.NAME, linkName);
        properties.put(LINK_DESCRIPTION_OBJECT, linkDescription);
        properties.put(PropertyIds.OBJECT_TYPE_ID, FILE_LINK);
        properties.put(LINK_DESTINATION_OBJECT, sourceFolderNodeRef);
        
        //Target folder where the link to be create
        Folder rootFolder=session.getRootFolder();
        Folder targetFolder=(Folder) session.getObjectByPath(rootFolder.getPath()+destinationFolderName);
        session.createItem(properties, targetFolder);
        logger.debug("Link Created for Folder successfully.....");
	} catch (FileNotFoundException e) {
		throw new AlfrescoException("There is no file not found exception",e);  
	}
}//end of ceateLinkForFolder

/**
 * This method is to create the link for the Document
 * @param sourceFolderPath: Path of sourceFolder for which the link should create
 * @param DocumentName:Name of the Document to create the link
 * @param destinationFolderName: Name of destination folder where to create the link
 * @param linkName: Name of the link
 * @param linkDescription: Description for the link
 * @throws AlfrescoException 
 */
public void createLinkForDocument(String sourceFolderPath, String documentName, String destinationFolderName,
		String linkName, String linkDescription) throws AlfrescoException {
	try {
		Session session=AlfrescoConnection.getSession();
		Folder sourceFolder=(Folder) session.getObjectByPath(session.getRootFolder().getPath()+sourceFolderPath);
		Document document=(Document) session.getObjectByPath(sourceFolder.getPath()+"/"+documentName);
		String objectId=document.getId();
		logger.debug("objectId:: "+objectId);
		String sourceDocumentNodeRef="workspace://SpacesStore/"+objectId;
		 Map<String,Object>properties=new HashMap<String, Object>();
	        properties.put(PropertyIds.BASE_TYPE_ID,BaseTypeId.CMIS_ITEM.value());
	        //Define name and description for the link
	        properties.put(PropertyIds.NAME, linkName);
	        properties.put(LINK_DESCRIPTION_OBJECT, linkDescription);
	        properties.put(PropertyIds.OBJECT_TYPE_ID, FILE_LINK);
	        properties.put(LINK_DESTINATION_OBJECT, sourceDocumentNodeRef);
	        
	        //Target folder where the link to be create
	        Folder rootFolder=session.getRootFolder();
	        Folder targetFolder=(Folder) session.getObjectByPath(rootFolder.getPath()+destinationFolderName);
	        session.createItem(properties, targetFolder);
	        logger.debug("Link Created for Document successfully.....");
	    
	} catch (FileNotFoundException e) {
		throw new AlfrescoException("There is  file not found exception",e); 
	}
}//end of createLinkForDocument method
/**
 * This method is to create user based permission
 * @param sourcePath: The source path of cmis object either may be document or folder for which the permission has to set
 * @param objectType: Type of Object either may be Document or Folder
 * @param userName: User Name for which the permission has to set
 * @param permissionType: Setting type of Permission for the object like all,reading,writing
 * @throws AlfrescoException 
 */
public void createUserBasedPermission(String sourcePath,String objectType,String userName,String permissionType) throws AlfrescoException {
	try {
		Session session=AlfrescoConnection.getSession();
		Folder rootFolder=session.getRootFolder();
		Folder sourceFolder=null;
		Document document=null;
		logger.info("Root Folder id and Path: "+rootFolder.getId()+", "+rootFolder.getPath());
		CmisObject cmisObject=null;
		if(objectType.equalsIgnoreCase("folder")){
		sourceFolder=(Folder) session.getObjectByPath(rootFolder.getPath()+sourcePath);
		 logger.info("sourceFolder ID and Path "+sourceFolder.getId()+" , "+sourceFolder.getPath());
		 cmisObject=session.getObject(sourceFolder.getId());
		}else if(objectType.equalsIgnoreCase("document")){
			document=(Document) session.getObjectByPath(rootFolder.getPath()+sourcePath);
			 cmisObject=session.getObject(document.getId());
		}
		 //Creating cmis Object
		 logger.debug("cmis object has been created succesfully:: "+cmisObject.toString());;
		
		 //Creating Object Factory
		 ObjectFactory objectFactory=session.getObjectFactory();
		//Defining principal(User) to whom the acl should be applied
		 String principal=userName;
		 //Creating ace --->access control entries for particular principal
		 Ace ace=null;
		 if(permissionType.equalsIgnoreCase("read")){
		    ace=objectFactory.createAce(principal, Collections.singletonList(CMIS_PERMISSION_READ));
		 }else if(permissionType.equalsIgnoreCase("write")){
		    ace=objectFactory.createAce(principal, Collections.singletonList(CMIS_PERMISSION_WRITE));	 
		 }else if(permissionType.equalsIgnoreCase("all")){
			 ace=objectFactory.createAce(principal, Collections.singletonList(CMIS_PERMISSION_ALL));	  
		 }
		 //Adding the ace to the list
		 List<Ace>addAces=new ArrayList<Ace>();
		 addAces.add(ace);
		 
		 //Setting Acl
		  cmisObject.setAcl(addAces);
		  logger.info("Acl applied successfully.....for principal--->User "+principal);
		   
		  Acl acl=session.getAcl(cmisObject, true);
		  logger.debug("acl:: "+acl);
		
	} catch (FileNotFoundException e) {
		throw new AlfrescoException("There is  file not found exception",e); 
	}
	
}//end of createUserBasedPermission method
}
