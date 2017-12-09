package com.reactiveworks.learning.alfresco.activity;

import static com.reactiveworks.learning.alfresco.util.AlfrescoConstant.CMIS_OBJECT_NAME;
import static com.reactiveworks.learning.alfresco.util.AlfrescoConstant.CMIS_PERMISSION_ALL;
import static com.reactiveworks.learning.alfresco.util.AlfrescoConstant.CMIS_PERMISSION_READ;
import static com.reactiveworks.learning.alfresco.util.AlfrescoConstant.CMIS_PERMISSION_WRITE;
import static com.reactiveworks.learning.alfresco.util.AlfrescoConstant.DOCUMENT_OBJECT_TYPE_ID;
import static com.reactiveworks.learning.alfresco.util.AlfrescoConstant.FILE_LINK;
import static com.reactiveworks.learning.alfresco.util.AlfrescoConstant.FOLDER_OBJECT_TYPE_ID;
import static com.reactiveworks.learning.alfresco.util.AlfrescoConstant.LINK_DESTINATION_OBJECT;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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

import javax.activation.MimetypesFileTypeMap;

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
import org.apache.chemistry.opencmis.commons.data.RepositoryCapabilities;
import org.apache.chemistry.opencmis.commons.data.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.enums.Action;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.UnfileObject;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisContentAlreadyExistsException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.apache.log4j.Logger;

import com.reactiveworks.learning.alfresco.util.AlfrescoConnection;
import com.reactiveworks.learning.alfresco.util.FileNotExistError;

/**
 * Impl Class for Alfresco Activity
 * 
 * @author Md Noorshid
 *
 */
public class AlfrescoActivityImpl implements IAlfrescoActivity {
	static Logger logger = Logger.getLogger(AlfrescoActivityImpl.class);

	/**
	 * Method to read repo of Alfresco
	 * 
	 * @throws FileNotFound
	 */
	public RepositoryInfo readRepo() throws FileNotFound {
		Session session = null;
		try {
			session = AlfrescoConnection.getSession();
			Folder rootFolder = session.getRootFolder();
			logger.debug("Root Folder Id, and name:::" + rootFolder.getId() + " , " + rootFolder.getName());
			ItemIterable<CmisObject> children = rootFolder.getChildren();
			logger.info(
					"----------------------------Find following folders in the Repository----------------------------------------");
			for (CmisObject cmisObject : children) {
				logger.debug(cmisObject.getName() + "-- Which is of type --" + cmisObject.getType().getDisplayName());
			}

			RepositoryInfo info = session.getRepositoryInfo();
			logger.info(
					"----------------------------------------------------------------------------------------------------------------------");
			logger.info("Name:: " + info.getName());
			logger.info("ID:: " + info.getId());
			logger.info("Product name:: " + info.getProductName());
			logger.info("Product Version:: " + info.getProductVersion());
			logger.info("Version Supported:: " + info.getCmisVersionSupported());
			logger.info(
					"----------------------------------------------------------------------------------------------------------------------");
			RepositoryCapabilities caps = session.getRepositoryInfo().getCapabilities();
			logger.info("Brief Capabilities Report:::");
			logger.info("Query:: " + caps.getQueryCapability());
			logger.info("GetDecendants:: " + caps.isGetDescendantsSupported());
			logger.info("GetFolderTree:: " + caps.isGetFolderTreeSupported());
			return info;
		} catch (FileNotExistError e) {
			throw new FileNotFound("File not found error!!", e);
		} finally {
			session.clear();
			logger.info("CACHE DATA CLEARED!!");
		}

	}

	/**
	 * Method to create Folder
	 * 
	 * @throws FileNotFound
	 * @throws FolderAlreadyExist
	 */
	public void createFolder(String folderName) throws FileNotFound, FolderAlreadyExist {
		Session session = null;
		try {
			session = AlfrescoConnection.getSession();
			Folder rootFolder = session.getRootFolder();
			logger.debug("Root Folder Id,Name, and Path is:  " + rootFolder.getId() + " , " + rootFolder.getName()
					+ " , " + rootFolder.getPath());
			// Defining properties of file
			Map<String, Object> folderProperties = new HashMap<String, Object>();
			folderProperties.put(PropertyIds.OBJECT_TYPE_ID, FOLDER_OBJECT_TYPE_ID);
			folderProperties.put(PropertyIds.NAME, folderName);
			rootFolder.createFolder(folderProperties);
			logger.info("FOLDER CREATED SUCCESSFULLY");
		} catch (FileNotExistError e) {
			throw new FileNotFound("File not found error!!", e);
		} catch (CmisContentAlreadyExistsException e) {
			throw new FolderAlreadyExist("Folder already Exist, please give other name!!", e);
		} finally {
			session.clear();
			logger.info("CACHE DATA CLEARED!!");
		}
	}// end of method createFolder

	/**
	 * Method to create sub Folder
	 * 
	 * @param mainFolderName:Name
	 *            of the folder inside where to create folder
	 * @param subFolderName:
	 *            Name of the sub folder
	 * @throws FolderAlreadyExist
	 */
	public void createSubFolder(String mainFolderName, String subFolderName) throws FileNotFound, FolderAlreadyExist {
		Folder mainFolder;
		Folder subFolder;
		Session session = null;
		try {
			session = AlfrescoConnection.getSession();
			Folder rootFolder = session.getRootFolder();
			Map<String, Object> subFolderProperties = new HashMap<String, Object>();
			mainFolder = (Folder) session.getObjectByPath(rootFolder.getPath() + mainFolderName);
			subFolderProperties.put(PropertyIds.OBJECT_TYPE_ID, FOLDER_OBJECT_TYPE_ID);
			subFolderProperties.put(PropertyIds.NAME, subFolderName);
			subFolder = mainFolder.createFolder(subFolderProperties);
			logger.info("SUB FOLDER CREATED SUCCESSFULLY UNDER FOLDER:::::: " + subFolder.getFolderParent().getName());
		} catch (FileNotExistError e) {
			throw new FileNotFound("File not found error!!", e);
		} catch (CmisContentAlreadyExistsException e) {
			throw new FolderAlreadyExist("Folder already Exist, please give other name!!", e);
		} finally {
			session.clear();
			logger.info("CACHE DATA CLEARED!!");
		}

	}

	/**
	 * Method to upload a document
	 * 
	 * @param folderPath
	 *            : Path of folder where document should be stored
	 * @param fileType:
	 *            Type of file
	 * @param filePath:
	 *            Path of document
	 * @throws FileNotFound
	 * @throws DocumentAlreadyExist
	 * 
	 */
	public void uploadDocument(String folderPath, String fileType, String filePath)
			throws FileNotFound, FolderAlreadyExist, DocumentAlreadyExist {
		Session session = null;
		try {
			session = AlfrescoConnection.getSession();

			Folder rootFolder = session.getRootFolder();
			Folder parentFolder = (Folder) session.getObjectByPath(rootFolder.getPath() + folderPath);

			File file = new File(filePath);
			Map<String, Object> props = null;

			String fileName = file.getName();

			// Creating Map object
			if (props == null) {
				props = new HashMap<String, Object>();
			}

			// Putting CMIS Object type ID
			if (props.get("cmis:objectTypeId") == null) {
				props.put("cmis:objectTypeId", DOCUMENT_OBJECT_TYPE_ID);
			}

			// Putting cmis:name in map
			if (props.get("cmis:name") == null) {
				props.put("cmis:name", fileName);
			}

			// Setting content Stream
			ContentStream contentStream = session.getObjectFactory().createContentStream(fileName, file.length(),
					fileType, new FileInputStream(file));

			Document document = parentFolder.createDocument(props, contentStream, null);

			logger.info("DOCUMENT " + document.getName() + " UPLOADED SUCCESSFULLY");

		} catch (FileNotExistError e) {
			throw new FileNotFound("File not found error!!", e);
		} catch (FileNotFoundException e) {
			throw new FileNotFound("File not found error!!", e);
		} catch (CmisContentAlreadyExistsException e) {
			throw new DocumentAlreadyExist("Document already Exist!!", e);
		} finally {
			session.clear();
			logger.info("CACHE DATA CLEARED!!");
		}
	}

	/**
	 * Method to create document
	 * 
	 * @param folderPath:
	 *            Path of Folder where to create document
	 * @param fileName:
	 *            Name of File
	 * @param content:
	 *            Contents of file
	 * @param fileType:
	 *            Type of File
	 * @param version:
	 *            Version State MAJOR OR MINOR
	 * @throws FileNotFound
	 * @throws FolderAlreadyExist
	 */
	public void createDocument(String folderPath, String fileName, String contents, String fileType, String version)
			throws FileNotFound, FolderAlreadyExist {
		Session session = null;
		try {
			session = AlfrescoConnection.getSession();
			Folder rootFolder = session.getRootFolder();
			Folder parentFolder = (Folder) session.getObjectByPath(rootFolder.getPath() + folderPath);
			VersioningState versionState = null;
			Map<String, Object> properties = null;
			if (properties == null) {
				properties = new HashMap<String, Object>();
			}
			if (properties.get(DOCUMENT_OBJECT_TYPE_ID) == null) {
				properties.put(PropertyIds.OBJECT_TYPE_ID, DOCUMENT_OBJECT_TYPE_ID);
			}
			if (properties.get(CMIS_OBJECT_NAME) == null) {
				properties.put(PropertyIds.NAME, fileName);
			}
			if (contents == null) {
				contents = "";
			}
			byte[] content = contents.getBytes();
			InputStream inputStream = new ByteArrayInputStream(content);
			ContentStream contentStream = new ContentStreamImpl(fileName, BigInteger.valueOf(content.length), fileType,
					inputStream);
			if (version.equalsIgnoreCase("MAJOR")) {
				versionState = VersioningState.MAJOR;
			} else if (version.equalsIgnoreCase("MINOR")) {
				versionState = VersioningState.MINOR;
			}

			Document document = parentFolder.createDocument(properties, contentStream, versionState);
			logger.info("DOCUMENT " + document.getName() + " CREATED SUCCESSFULLY");

		} catch (FileNotExistError e) {
			throw new FileNotFound("File not found error!!", e);
		} catch (CmisContentAlreadyExistsException e) {
			throw new FolderAlreadyExist("Docuemnt " + fileName + " already Exist, please give other name!!", e);
		} finally {
			session.clear();
			logger.info("CACHE DATA CLEARED!!");
		}

	}

	/**
	 * Method to delete the document
	 * 
	 * @param sourceFolderPath:
	 *            Source folder path where document exist
	 * @param documentName:
	 *            Name of document which has to delete
	 */
	public void deleteDocument(String sourceFolderPath, String documentName) throws FileNotFound, DocumentNotExist {
		Session session = null;
		try {
			session = AlfrescoConnection.getSession();
			Folder rootFolder = session.getRootFolder();
			Document document;
			Folder parentFolder = (Folder) session.getObjectByPath(rootFolder.getPath() + sourceFolderPath);
			document = (Document) session.getObjectByPath(parentFolder.getPath() + "/" + documentName);
			document.delete();
			logger.info("DOCUMENT " + documentName + " DELETED SUCCESSFULLY");
		} catch (FileNotExistError e) {
			throw new FileNotFound("File not found error!!", e);
		} catch (CmisObjectNotFoundException e) {
			throw new FileNotFound("DOCUMENT " + documentName + " DOES NOT EXIST!!", e);
		} finally {
			session.clear();
			logger.info("CACHE DATA CLEARED!!");
		}

	}

	/**
	 * Method to delete Folder
	 * 
	 * @param targetFolderPath:
	 *            Path to delete target folder
	 * @throws FileNotFound
	 */
	public void deleteFolder(String targetFolderPath) throws FileNotFound {
		String folderName = null;
		Session session = null;
		try {
			session = AlfrescoConnection.getSession();
			Folder rootFolder = session.getRootFolder();
			Folder targetFolder = (Folder) session.getObjectByPath(rootFolder.getPath() + targetFolderPath);
			folderName = targetFolder.getName();
			targetFolder.deleteTree(true, UnfileObject.DELETE, true);
			logger.info("FOLDER " + folderName + " DELETED SUCCESSFULLY");
		} catch (FileNotExistError e) {
			throw new FileNotFound("File not found error!!", e);
		} catch (CmisObjectNotFoundException e) {
			throw new FileNotFound("Folder DOES NOT EXIST!!", e);
		} finally {
			session.clear();
			logger.info("CACHE DATA CLEARED!!");
		}

	}

	/**
	 * This method is to update version of document
	 * 
	 * @param folderPath:
	 *            Path of folder where the document is stored in Alfresco Repo
	 * @param filePath:
	 *            Local Path of the document which need to be upload
	 * @throws FileNotFound
	 * @throws VersionStateMissMatched
	 */
	public void updateVersionOfDocument(String folderPath, String filePath, String versioningStateType)
			throws FileNotFound, VersionStateMissMatched {
		Session session = null;
		try {
			session = AlfrescoConnection.getSession();
			Folder rootFolder = session.getRootFolder();
			Folder parentFolder = (Folder) session.getObjectByPath(rootFolder.getPath() + folderPath);
			// update version state label of document
			boolean isMajor;
			if (versioningStateType.equalsIgnoreCase("MAJOR")) {
				isMajor = true;
			} else if (versioningStateType.equalsIgnoreCase("MINOR")) {
				isMajor = false;
			} else {
				throw new VersionStateMissMatched(
						"PLEASE PROVIDE THE CORRECT VERSION STATE, VALID STATES ARE (MAJOR,MINOR)");
			}

			// Getting the File which need to be changed
			File file = new File(filePath);
			String fileName = file.getName();
			String mimeType = new MimetypesFileTypeMap().getContentType(file);
			Document doc = (Document) session.getObjectByPath(parentFolder.getPath() + "/" + fileName);
			logger.debug("document path:: " + doc.getPaths());

			for (Document version : doc.getAllVersions()) {
				logger.info("ALL VERSION LABEL:: " + version.getVersionLabel());
			}

			logger.info("Changing version of " + doc.getName() + " from " + doc.getVersionLabel());

			// Now update with a new version
			if (doc.getAllowableActions().getAllowableActions().contains(Action.CAN_CHECK_OUT)) {

				doc.refresh(); // Reload the object from the repository

				// Checks out the document and returns the object ID of the PWC
				// (private working copy)
				ObjectId idOfCheckedOutDocument = doc.checkOut();

				// Returns a CMIS object from the session cache. If the object
				// is not in the cache or the cache is turned off per default
				// OperationContext,
				// it will load the object from the repository and puts it into
				// the cache.

				Document pwc = (Document) session.getObject(idOfCheckedOutDocument);

				ContentStream contentStream = pwc.getContentStream();

				Map<String, Object> props = null;
				if (props == null) {
					props = new HashMap<String, Object>();
				}

				if (props.get("cmis:name") == null) {
					props.put("cmis:name", fileName);
				}

				// Setting Content Stream
				contentStream = session.getObjectFactory().createContentStream(pwc.getName(), file.length(), mimeType,
						new FileInputStream(file));
				ObjectId objectId = pwc.checkIn(isMajor, props, contentStream,
						"Version Changed after uploading the same document");
				Document document = (Document) session.getObject(objectId);
				logger.info("DOCUMENT UPLOADED AND VERSION LABEL HAS BEEN CHANGED TO " + document.getVersionLabel());
			}
		} catch (FileNotExistError e) {
			throw new FileNotFound("File not found error!!", e);
		} catch (FileNotFoundException e) {
			throw new FileNotFound("File not found error!!", e);
		} finally {
			session.clear();
			logger.info("CACHE DATA CLEARED!!");
		}

	}

	/**
	 * Method to download latest document
	 * 
	 * @param sourceFolderPath:
	 *            Path of source folder in Alfresco Repo from where document
	 *            need to be download
	 * @param documentName:
	 *            Name of document which need to be downloaded, resides under
	 *            sourceFolderPath
	 * @param targetFolderPath:
	 *            Local Path of machine where the document should downloaded
	 * @throws FileNotFound
	 * @throws ContentException
	 * @throws IOException
	 */
	public void downloadLatestDocument(String sourceFolderPath, String documentName, String targetFolderPath)
			throws FileNotFound, ContentException, IOException {
		int num = 0;
		String requiredName = null;
		String fileType = null;
		InputStream inputStream = null;
		Path path;
		Session session = null;
		try {
			session = AlfrescoConnection.getSession();
			Folder rootFolder = session.getRootFolder();
			Folder parentFolder = (Folder) session.getObjectByPath(rootFolder.getPath() + sourceFolderPath);
			Document document = (Document) session.getObjectByPath(parentFolder.getPath() + "/" + documentName);
			String docNameInRepo = document.getName();
			logger.debug("docNameInRepo:: " + docNameInRepo);
			String[] docNameArr = docNameInRepo.split("\\.");
			requiredName = docNameArr[0];
			logger.debug("requiredName:: " + requiredName);
			fileType = docNameArr[1];
			logger.debug("fileType:: " + fileType);

			ContentStream downloadedStream = document.getContentStream();

			inputStream = downloadedStream.getStream();
			path = Paths.get(targetFolderPath + "\\" + documentName);
			if (!Files.exists(path)) {
				logger.debug("inside if condition.....");
				Files.copy(inputStream, path);
				logger.info(document.getName() + document.getVersionLabel() + " DOWNLOADED SUCCESSFULLY");
			} else {
				logger.debug("inside else condition.....");
				num++;
				path = Paths.get(targetFolderPath + "\\" + requiredName + num + "." + fileType);
				Files.copy(inputStream, path);
			}
		} catch (FileNotExistError e) {
			throw new FileNotFound("File not found error!!", e);
		} catch (FileAlreadyExistsException e) {
			logger.debug("inside FileAlreadyExistsException.....");
			num = 2;
			path = Paths.get(targetFolderPath + "\\" + requiredName + num + "." + fileType);
			Files.copy(inputStream, path);
			logger.info(requiredName + num + "." + fileType + " DOWNLOADED SUCCESSFULLY");
			num++;
		} catch (IOException e) {
			throw new ContentException("Download could not happended, there is some exception ", e);
		} finally {
			session.clear();
			logger.info("CACHE DATA CLEARED!!");
		}
	}

	/**
	 * Method to download the document filtered by Versions
	 * 
	 * @param sourceFolderPath:
	 *            Source Folder Path where document is resides
	 * @param documentName:
	 *            Name of document which need to be download
	 * @param version:
	 *            Specific version of document which need to be download
	 */
	public void downloadDocumentWithVersionType(String sourceFolderPath, String documentName, String version,
			String targetFolderPath) throws FileNotFound, ContentException {
		Session session = null;
		try {
			session = AlfrescoConnection.getSession();
			Folder rootFolder = session.getRootFolder();
			Folder parentFolder = (Folder) session.getObjectByPath(rootFolder.getPath() + sourceFolderPath);
			Document document = (Document) session.getObjectByPath(parentFolder.getPath() + "/" + documentName);
			List<Document> allDocVersions = document.getAllVersions();
			for (Document doc : allDocVersions) {
				if (doc.getVersionLabel().equals(version.trim())) {
					document = doc;
					break;
				}
			}

			ContentStream contentStream = document.getContentStream();
			InputStream inputStream = contentStream.getStream();
			String[] docArr = documentName.split("\\.");
			Path path = Paths.get(targetFolderPath + "\\" + docArr[0] + "(" + version + ")." + docArr[1]);
			Files.copy(inputStream, path);
			logger.info(document.getName() + document.getVersionLabel() + " DOWNLOADED SUCCESSFULLY");
		} catch (FileNotExistError e) {
			throw new FileNotFound("File not found error!!", e);
		} catch (IOException e) {
			throw new ContentException("Download could not happended, there is some exception ", e);
		} finally {
			session.clear();
			logger.info("CACHE DATA CLEARED!!");
		}

	}

	/**
	 * Method to create Link for Folder in destinationFolderPath
	 * 
	 * @param sourceFolderPath:
	 *            Source Folder Path for which need to create link
	 * @param destinationFolderPath:
	 *            Destination Folder Path where need to create link
	 * @param linkName:
	 *            Name of link
	 * @param linkDescription:
	 *            Description for link
	 * @throws FileNotFound
	 */
	public void createLinkForFolder(String sourceFolderPath, String destinationFolderName, String linkName,
			String linkDescription) throws FileNotFound {
		Session session = null;
		try {
			session = AlfrescoConnection.getSession();
			Folder sourceFolder = (Folder) session
					.getObjectByPath(session.getRootFolder().getPath() + sourceFolderPath); // Source
																							// Folder
			logger.debug("sourceFolder:: " + sourceFolder.getName());

			String objectId = sourceFolder.getId(); // Object Id of Source
													// Folder
			logger.debug("objectId:: " + objectId);

			String sourceFolderNodeRef = "workspace://SpacesStore/" + objectId; // Node
																				// reference
																				// for
																				// Source
																				// Folder
			logger.debug("sourceFolderNodeRef:: " + sourceFolderNodeRef);

			Map<String, Object> properties = new HashMap<String, Object>();
			// Putting name and description for the link
			properties.put(PropertyIds.BASE_TYPE_ID, BaseTypeId.CMIS_ITEM.value());
			properties.put(PropertyIds.NAME, linkName);
			properties.put(PropertyIds.DESCRIPTION, linkDescription);
			properties.put(PropertyIds.OBJECT_TYPE_ID, FILE_LINK); // Object
																	// type id
			properties.put(LINK_DESTINATION_OBJECT, sourceFolderNodeRef);

			// Target folder where link to be create
			Folder rootFolder = session.getRootFolder();
			Folder targetFolder = (Folder) session.getObjectByPath(rootFolder.getPath() + destinationFolderName);
			session.createItem(properties, targetFolder);
			logger.info("LINK FOR FOLDER CREATED SUCCESSFULLY");
		} catch (FileNotExistError e) {
			throw new FileNotFound("File not found error!!", e);
		} finally {
			session.clear();
			logger.info("CACHE DATA CLEARED!!");
		}
	}

	/**
	 * Method to create link for document
	 * 
	 * @param sourceFolderPath:
	 *            Source Folder where document is resides
	 * @param documentName:
	 *            Name of Document which need to be create link
	 * @param destinationFolderName:
	 *            Destination Folder where document link should be created
	 * @param linkName:
	 *            Name of Link
	 * @param linkDescription:
	 *            Description of Link
	 */
	public void createLinkForDocument(String sourceFolderPath, String documentName, String destinationFolderPath,
			String linkName, String linkDescription) throws FileNotFound {
		Session session = null;
		try {

			session = AlfrescoConnection.getSession();
			Folder sourceFolder = (Folder) session
					.getObjectByPath(session.getRootFolder().getPath() + sourceFolderPath);
			Document document = (Document) session.getObjectByPath(sourceFolder.getPath() + "/" + documentName);
			logger.debug("Name of document:: " + document.getName());

			String objectId = document.getId();
			logger.debug("objectId:: " + objectId);
			
			/**
			 * Here splitting objectId since with node value it is appended with ; and version on it
			 * that's why while creating the link it is not able to hit the right path 
			 */
			
			String sourceDocumentNodeRef = "workspace://SpacesStore/" + objectId.split("\\;")[0];
			logger.debug("sourceDocumentNodeRef:: " + sourceDocumentNodeRef);
			Map<String, Object> properties = new HashMap<String, Object>();
			// Define name and description for the link
			properties.put(PropertyIds.BASE_TYPE_ID, BaseTypeId.CMIS_DOCUMENT.value());
			properties.put(PropertyIds.NAME, linkName);
			properties.put(PropertyIds.DESCRIPTION, linkDescription);
			properties.put(PropertyIds.OBJECT_TYPE_ID, FILE_LINK);
			properties.put(LINK_DESTINATION_OBJECT, sourceDocumentNodeRef);

			// Target folder where the link to be create
			Folder rootFolder = session.getRootFolder();
			Folder targetFolder = (Folder) session.getObjectByPath(rootFolder.getPath() + destinationFolderPath);
			session.createItem(properties, targetFolder);
			logger.debug("LINK FOR DOCUMENT CREATED SUCCESSFULLY");
		} catch (FileNotExistError e) {
			throw new FileNotFound("File not found error!!", e);
		} finally {
			session.clear();
		}
	}
    
	/**
	 * Method is to create user based permission for Document
	 * @param sourcePath: It is the source path where we need to apply permission
	 * @param objectType: Here need to provide object type such as Folder (Or) Document
	 * @param userName: UserName for which permission is granted
	 * @param permissionType: Type of permission passing i.e read,write,all
	 * @throws FileNotFound 
	 */
	public void createUserBasedPermissionForDocument(String sourcePath,String documentName ,String userName, String permissionType) throws FileNotFound {
        Session session=null;
        try {
			session=AlfrescoConnection.getSession();
			Folder rootFolder=session.getRootFolder();
			Document document=null;
			CmisObject cmisObject=null;
			
			   //Creating cmisObject based on object type i.e document
				document=(Document) session.getObjectByPath(rootFolder.getPath()+sourcePath+"/"+documentName);
				cmisObject=session.getObject(document.getId());
			
			logger.debug("cmisObject created successfully: "+cmisObject);
			
			//Creating Object Factory
			ObjectFactory objectFactory=session.getObjectFactory();
			
			//Defining principal(user) to whom the acl should be applied
			String principal=userName;
			
			//Creating ace ---Access Controll Entries
			Ace ace=null;
			if(permissionType.equalsIgnoreCase("read")){
				ace=objectFactory.createAce(principal, Collections.singletonList(CMIS_PERMISSION_READ));
			}else if(permissionType.equalsIgnoreCase("write")){
				ace=objectFactory.createAce(principal, Collections.singletonList(CMIS_PERMISSION_WRITE));
			}else if(permissionType.equalsIgnoreCase("all")){
				ace=objectFactory.createAce(principal, Collections.singletonList(CMIS_PERMISSION_ALL));
			}
			
			//Adding the ace to the list
			List<Ace> addAces=new ArrayList<Ace>();
			addAces.add(ace);
			
			//Setting ACL
			cmisObject.setAcl(addAces);
			
			logger.debug("ACL APPLIED SUCCESSFULLY FOR THE PRINCIPAL: "+principal);
			
			Acl acl=session.getAcl(cmisObject, true);
			logger.debug("ACL:: "+acl);
			
			
		} catch (FileNotExistError e) {
			throw new FileNotFound("File not found error!!", e);
		}finally{
			session.clear();
		}
		
	}
   
	/**
	 * Method to create user based permission for Folder
	 * @param sourceFolderPath: Source Folder Path for which need to be set permission
	 * @param userName: UserName for which permission is granted
	 */
	public void createUserBasedPermissionForFolder(String sourceFolderPath, String userName, String permissionType) throws FileNotFound {
    		Session session=null;
    		try {
				session=AlfrescoConnection.getSession();
				Folder rootFolder=session.getRootFolder();
				CmisObject cmisObject=null;
				
				//Creating cmisObject based on object type i.e Folder
				Folder sourceFolder=(Folder) session.getObjectByPath(rootFolder.getPath()+sourceFolderPath);
				cmisObject=session.getObject(sourceFolder.getId());
				logger.debug("cmisObject created successfully:: "+cmisObject);
				 
				//Creating Object Factory
				ObjectFactory objectFactory=session.getObjectFactory();
				
				//Defining principal(user) to whom the acl should be applied
				String principal=userName;
				
				//Creating ace ---Access Controll Entries
				Ace ace=null;
				
				if(permissionType.equalsIgnoreCase("read")){
					ace=objectFactory.createAce(principal, Collections.singletonList(CMIS_PERMISSION_READ));
				}else if(permissionType.equalsIgnoreCase("write")){
					ace=objectFactory.createAce(principal, Collections.singletonList(CMIS_PERMISSION_WRITE));
				}else if(permissionType.equalsIgnoreCase("all")){
					ace=objectFactory.createAce(principal, Collections.singletonList(CMIS_PERMISSION_ALL));
				}
				
				//Adding the ace to the list
				List<Ace> addAces=new ArrayList<Ace>();
				addAces.add(ace);
				
				//Setting ACL
				cmisObject.setAcl(addAces);
				
				logger.debug("ACL APPLIED SUCCESSFULLY FOR THE PRINCIPAL: "+principal);
				
				Acl acl=session.getAcl(cmisObject, true);
				logger.debug("ACL:: "+acl);
				
			} catch (FileNotExistError e) {
				throw new FileNotFound("File not found error!!", e);
			}
    		
	}

}
