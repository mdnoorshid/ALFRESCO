package com.reactiveworks.learning.alfresco.operation;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.impl.json.JSONObject;
import org.apache.james.mime4j.dom.Disposable;
import org.apache.log4j.Logger;

public class AlfrescoRestCallOperation extends BaseOnPrem {
	static Logger logger = Logger.getLogger(AlfrescoRestCallOperation.class);

	/**
	 * This method is to create comment on document
	 * 
	 * @param parentFolderPath
	 * @param documentName
	 * @param comment
	 * @throws CreateCommentException
	 * @throws RootFolderException
	 */
	public void createComment(String parentFolderPath, String documentName, String comment)
			throws CreateCommentException, RootFolderException {
		try {
			// Find the root Folder of our target site
			String rootFolderId = getRootFolderId(getSite());
			logger.debug("rootFolderId:: " + rootFolderId);
			Folder rootFoder = (Folder) getCmisSession().getObject(rootFolderId);
			logger.debug("rootFoder:: " + rootFoder);
			Folder parentFolder = (Folder) getCmisSession().getObjectByPath(rootFoder.getPath() + parentFolderPath);
			Document document = (Document) getCmisSession()
					.getObjectByPath(parentFolder.getPath() + "/" + documentName);
			comment(document.getVersionSeriesId(), comment);
			logger.info("COMMENTED SUCCESSFULLY:: " + comment);
		} catch (IOException e) {
			logger.error("Error:: " + e.getMessage());
			throw new CreateCommentException("Comment unsuccessful due to Error", e);
		}

	}

	/**
	 * Method to create folder using Rest URL
	 * 
	 * @param folderName
	 * @throws CreateFolderException
	 * @throws RootFolderException
	 */
	public void createFolderRest(String folderName) throws CreateFolderException, RootFolderException {
		try {
			int statusCode = createFolderUsingRest(folderName);
			if (statusCode == 201) {
				logger.info("=====FOLDER " + folderName + "CREATED SUCCESSFULLY=====");
			}
		} catch (FolderCreationException e) {
			throw new CreateFolderException("Folder creation failed", e);
		}
	}

	/**
	 * Method to create sub folder using Rest URL
	 * 
	 * @param parentFolderPath
	 * @param subFolderName
	 * @throws CreateDocumentException
	 * @throws RootFolderException
	 * @throws FolderException
	 * @throws CreateFolderException
	 */
	public void createSubFolderRest(String parentFolderPath, String subFolderName)
			throws RootFolderException, FolderException, CreateFolderException {
		try {
			int statusCode = createSubFolderUsingRest(parentFolderPath, subFolderName);
			if (statusCode == 201) {
				logger.info("=====FOLDER " + subFolderName + "CREATED SUCCESSFULLY=====");
			}
		} catch (RootFolderException e) {
			throw new RootFolderException("There is Error in creating root folder " + e);
		} catch (FolderException e) {
			throw new FolderException("There is Error in creating root folder " + e);
		}
	}

	/**
	 * This method will retrieve the all child info within it such folders or
	 * document
	 * 
	 * @param parentFolderPath
	 * @throws FolderException
	 * @throws RootFolderException
	 */
	public void getFolderChildInfo(String parentFolderPath) throws FolderException, RootFolderException {
		Session session = getCmisSession();
		String path = parentFolderPath;
		logger.debug("path:: " + path);
		Folder parentFolder = (Folder) session.getObjectByPath("/" + path);
		String parentFolderId = parentFolder.getId();
		logger.debug("parentFolderId:: " + parentFolderId);
		try {
			String folderChildInfo = getFolderChildRest(parentFolderId);
			logger.info("=============FolderChildInfo===============");
			logger.info(folderChildInfo);
		} catch (FolderException e) {
			throw new FolderException("Unable to retrieve the information");
		}

	}

	/**
	 * Method to delete the folder
	 * 
	 * @param targetFolderPath
	 *            : Path of the Target Folder which need to ne delete
	 * @throws DeleteNodeException
	 */
	public void deleteFolder(String targetFolderPath) throws DeleteNodeException {
		Session session = getCmisSession();
		String path = targetFolderPath;
		logger.debug("path:: " + path);
		Folder targetFolder = (Folder) session.getObjectByPath("/" + path);
		String tagetFolderName = targetFolder.getName();
		String targetFolderId = targetFolder.getId();
		logger.debug("targetFolderId:: " + targetFolderId);
		try {
			int responseStatus = deleteNodeRest(targetFolderId);
			if (responseStatus == 204) {
				logger.info("=====FOLDER " + tagetFolderName + " DELETED SUCCESSFULLY=====");
			}
		} catch (DeleteNodeException e) {
			throw new DeleteNodeException("Unable to delete node", e);
		}

	}

	/**
	 * This method is to delete the document
	 * 
	 * @param parentFolderPath
	 * @param documentName
	 * @throws DeleteNodeException
	 */
	public void deleteDocument(String parentFolderPath, String documentName) throws DeleteNodeException {
		Session session = getCmisSession();
		Document targetDocument = (Document) session
				.getObjectByPath(session.getRootFolder().getPath() + parentFolderPath + "/" + documentName);
		String documentId = targetDocument.getId();
		logger.debug("targetDocument:: " + targetDocument);
		try {
			int responseStatus = deleteNodeRest(documentId);
			if (responseStatus == 204) {
				logger.info("=====FILE " + documentName + " DELETED SUCCESSFULLY=====");
			}
		} catch (DeleteNodeException e) {
			throw new DeleteNodeException("Unable to delete node", e);
		}
	}

	/**
	 * This method is to remove member from group
	 * 
	 * @param groupId:
	 *            Target group id
	 * @param groupMemberId
	 *            :Target group member id
	 * @throws RemoveMemberException
	 */
	public void removeMemberFromGroup(String groupId, String memberId) throws RemoveMemberException {
		try {
			int responseStatus = deleteMemberFromGroup(groupId, memberId);
			if (responseStatus == 204) {
				logger.info("MEMBER " + memberId + " REMOVED SUCCESSFULLY FROM " + groupId);
			}
		} catch (RemoveMemberException e) {
			throw new RemoveMemberException("Unable to remove member", e);
		}
	}

	/**
	 * Method to create document
	 * 
	 * @param parentFolderPath
	 * @param documentName
	 * @param contentsOfDocument
	 * @throws CreateDocumentException
	 * @throws RootFolderException
	 */
	public void createDocumentUsingRest(String parentFolderPath, String documentName, String contentsOfDocument)
			throws CreateDocumentException, RootFolderException {
		int responseStatus = createDocumentRest(parentFolderPath, documentName, contentsOfDocument);
		if (responseStatus == 200) {
			logger.info("=====DOCUEMENT CREATION COMPLETED====");
		}
	}

	/**
	 * This method is to like a document
	 * 
	 * @param parentFolderPath
	 * @param documentName
	 * @throws LikeDocumentException
	 * @throws RootFolderException
	 */
	public void likeDocument(String parentFolderPath, String documentName)
			throws LikeDocumentException, RootFolderException {
		String rootFolderId = null;
		try {
			rootFolderId = getRootFolderId(getSite());
			logger.debug("rootFolderId:: " + rootFolderId);
			Folder rootFolder = (Folder) getCmisSession().getObject(rootFolderId);
			Folder parentFolder = (Folder) getCmisSession().getObjectByPath(rootFolder.getPath() + parentFolderPath);
			Document document = (Document) getCmisSession()
					.getObjectByPath(parentFolder.getPath() + "/" + documentName);
			String documentId = document.getId();
			int statusCode = like(documentId);
			if (statusCode == 201)
				logger.info("LIKED SUCCESSFULLY:: " + document.getName());
		} catch (IOException e) {
			logger.error("Error:: " + e.getMessage());
			throw new LikeDocumentException("Comment unsuccessful due to Error", e);
		}
	}

	/**
	 * Method to download the document
	 * 
	 * @param documentName
	 *            : Name of document
	 * @param targetFolderPath
	 *            : Download Target Folder Path
	 * @param sourcePath
	 *            : Source Path
	 * @throws DownLoadDocException
	 * @throws PathAlreadyExist
	 */
	public void downloadDocument(String documentName, String targetFolderPath, String sourcePath)
			throws DownLoadDocException, PathAlreadyExist {
		String rootFolderId;
		try {
			rootFolderId = getRootFolderId(getSite());
			logger.debug("rootFolderId:: " + rootFolderId);
			Folder rootFolder = (Folder) getCmisSession().getObject(rootFolderId);
			Folder parentFolder = (Folder) getCmisSession().getObjectByPath(rootFolder.getPath() + sourcePath);
			Document document = (Document) getCmisSession()
					.getObjectByPath(parentFolder.getPath() + "/" + documentName);
			String documentId = document.getId().split("\\;")[0];
			logger.debug("documentId:: " + documentId);
			downloadDocumentUsingRest(documentId, documentName, targetFolderPath, sourcePath);
			logger.info("======DOWNLOAD COMPLETED=======");
		} catch (RootFolderException e) {
			throw new DownLoadDocException("There is Exception in Downloading DOC:: ", e);
		} catch (PathAlreadyExist e) {
			throw new PathAlreadyExist("Path Already Exist", e);
		}
	}

	public void downloadDocBasedOnVersions(String documentName, String versionId, String targetFolderPath,
			String sourcePath) throws DownLoadDocException {
		String rootFolderId;
		try {
			rootFolderId = getRootFolderId(getSite());
			logger.debug("rootFolderId:: " + rootFolderId);
			Folder rootFolder = (Folder) getCmisSession().getObject(rootFolderId);
			Folder parentFolder = (Folder) getCmisSession().getObjectByPath(rootFolder.getPath() + sourcePath);
			Document document = (Document) getCmisSession()
					.getObjectByPath(parentFolder.getPath() + "/" + documentName);
			String documentId = document.getId().split("\\;")[0];
			logger.debug("documentId:: " + documentId);
			int statusVersion = downloadDocumentBasedOnVersions(documentId, documentName, versionId, targetFolderPath,
					sourcePath);
			if (statusVersion == 200) {
				logger.info("======DOWNLOAD COMPLETED=======");
			}
		} catch (RootFolderException e) {
			throw new DownLoadDocException("There is Exception in Downloading DOC:: ", e);
		}
	}

	/**
	 * Method to create user using REST
	 * 
	 * @param id
	 * @param firstName
	 * @param lastName
	 * @param email
	 * @param password
	 * @throws UserCreationException
	 */
	public void createUser(String id, String firstName, String lastName, String email, String password)
			throws UserCreationException {
		try {
			int responseCode = createUserRest(id, firstName, lastName, email, password);
			if (responseCode == 201) {
				logger.info("====USER " + firstName + " " + lastName + " CREATED SUCCESSFULLY===");
			}
		} catch (UserCreationException e) {
			throw new UserCreationException("User Creation Failed ", e);
		}
	}

	/**
	 * Method to create group
	 * 
	 * @param id:
	 *            Id of group ,the group id must start with "GROUP_". If this is
	 *            omitted it will be added automatically.
	 * @param displayName:
	 *            Group Name which should be display to UI
	 * @throws GroupCreationException
	 */
	public void createGroup(String id, String displayName) throws GroupCreationException {
		try {
			int statusCode = createGroupRest(id, displayName);
			if (statusCode == 201)
				logger.info("====GROUP " + displayName + " CREATED SUCCESSFULLY====");
		} catch (GroupCreationException e) {
			throw new GroupCreationException("User Creation Failed ", e);
		}
	}

	/**
	 * This method used to add members in existing Group
	 * 
	 * @param groupId
	 *            : Group Id for which need to add members
	 * @param userId
	 *            : User Id to add in Group
	 * @param memberType
	 *            : It will be type PERSON if user already exist otherwise GROUP
	 *            for non existing USER
	 * @throws AddMemberCreationException
	 */
	public void addMemberGroup(String groupId, String userId, String memberType) throws AddMemberCreationException {
		try {
			int statusCode = addMembersInGroup(groupId, userId, memberType);
			if (statusCode == 201)
				logger.info("====MEMBER " + userId + " ADDED SUCCESSFULLY TO " + groupId + " ====");
		} catch (AddMemberCreationException e) {
			throw new AddMemberCreationException("Adding members failed", e);
		}
	}

	/**
	 * Method to create site
	 * 
	 * @param title
	 *            : Title of Site
	 * @param visibilty
	 *            : Access of the site, value may be PUBLIC
	 * @throws SiteCreationException
	 */
	public void createSite(String title, String visibility) throws SiteCreationException {
		try {
			int statusCode = createSiteRest(title, visibility);
			if (statusCode == 201) {
				logger.info("=====SITE " + title + " CREATED SUCCESSFULLY=====");
			}
		} catch (SiteCreationException e) {
			throw new SiteCreationException("There is exception in creating site ", e);
		}
	}

	/**
	 * Method to create membership request to a site
	 * 
	 * @param personId:
	 *            Id of requesting Person
	 * @param message
	 *            :request message by user
	 * @param id
	 *            : site id
	 * @param title
	 *            : Title for request message
	 * @throws MemberShipRequestException
	 */
	public void memberRequestSite(String personId, String message, String id, String title)
			throws MemberShipRequestException {
		try {
			membershipRequestInSite(personId, message, id, title);
		} catch (MemberShipRequestException e) {
			throw new MemberShipRequestException("There is Exception in creating membership request for Site", e);
		}
	}

	/**
	 * This method is to make favorite for any site
	 * 
	 * @param siteId
	 *            : SiteId which need to make favorite
	 * @throws UnableToCreateFavoriteException
	 */
	public void makeFavoriteSite(String siteId,String personId) throws UnableToCreateFavoriteException {
		try {
			int createFavouriteSiteStatusCode = createFavouriteSite(siteId,personId);
			if(createFavouriteSiteStatusCode==201)
				logger.info("SITE "+siteId+" MADE FAVORITE SUCCESSFULLY BY "+personId);
		} catch (UnableToCreateFavoriteException e) {
			throw new UnableToCreateFavoriteException("Unable to make favorite ", e);
		}
	}
	
	/**
	 * Method is to get Activities of current user
	 * @throws ActiviesException 
	 */
	public void getActiviesOfCurrentUser() throws ActiviesException{
		try {
			String activitiesResponse = getActivities();
			logger.info("====ACTIVITIES====");
			logger.info(activitiesResponse);
		} catch (ActiviesException e) {
			throw new ActiviesException("There is exception in getting the activities",e);
		}
	}
	
	public void getNodeQuery(String searcherm,String rootFolder) throws QueryExceptio{
		try {
			String response = getNodesQueries(searcherm,rootFolder);
            logger.info("====ACTIVITIES====");
            logger.info(response);
		} catch (QueryExceptio e) {
			throw new QueryExceptio("There is Exception in getting Node",e);
		}
	}

}
