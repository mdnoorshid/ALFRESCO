package com.reactiveworks.learning.alfresco.operation;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.reactiveworks.learning.alfresco.model.ContainerEntry;
import com.reactiveworks.learning.alfresco.model.ContainerList;
import com.reactiveworks.learning.alfresco.model.NetworkEntry;
import com.reactiveworks.learning.alfresco.model.NetworkList;
import com.reactiveworks.learning.alfresco.util.Config;

/**
 * This class contains constants and methods that are common across the Alfresco
 * Public API regardless of where the target repository is hosted.
 *
 * @author Md Noorshid
 *
 */
abstract public class BasePublicAPI {
	static Logger logger = Logger.getLogger(BasePublicAPI.class);
	/*
	 * public static final String SITES_URL = "/public/cmis/versions/1.1/atom";
	 */
	public static final String SITES_URL = "/public/alfresco/versions/1/sites/";
	public static final String NODES_URL = "/public/alfresco/versions/1/nodes/";
	public static final String DOWNLOADS_URL = "/public/alfresco/versions/1/downloads/";
	public static final String PEOPLE_URL = "/public/alfresco/versions/1/people";
	public static final String GROUP_URL = "/public/alfresco/versions/1/groups/";
	public static final String SITE_URL = "/public/alfresco/versions/1/sites";
	public static final String QUERIES = "/public/alfresco/versions/1/queries/";

	private String homeNetwork;

	/**
	 * Method to get home network
	 * 
	 * @return
	 * @throws IOException
	 */
	public String getHomeNetwork() throws IOException {
		if (this.homeNetwork == null) {
			GenericUrl url = new GenericUrl(getAlfrescoAPIUrl());

			HttpRequest request = getRequestFactory().buildGetRequest(url);

			NetworkList networkList = request.execute().parseAs(NetworkList.class);
			logger.info("Found ::" + networkList.list.pagination.totalItems + " networks.");
			for (NetworkEntry networkEntry : networkList.list.entries) {
				if (networkEntry.entry.homeNetwork) {
					this.homeNetwork = networkEntry.entry.id;

				}
			}

			if (this.homeNetwork == null) {
				this.homeNetwork = "-default-";
			}

			logger.info("Your home network appears to be: " + homeNetwork);
		}
		return this.homeNetwork;
	}

	/**
	 * Use the REST API to find the documentLibrary folder for the target site
	 * 
	 * @return String
	 * @author jpotts
	 * @throws RootFolderException
	 */
	public String getRootFolderId(String site) throws RootFolderException {

		GenericUrl containersUrl;
		String rootFolderId = null;
		try {
			containersUrl = new GenericUrl(getAlfrescoAPIUrl() + getHomeNetwork() + SITES_URL + site + "/containers");
			logger.info("containersUrl: " + containersUrl);
			HttpRequest request = getRequestFactory().buildGetRequest(containersUrl);
			logger.debug("request:: " + request.toString());
			ContainerList containerList = request.execute().parseAs(ContainerList.class);
			logger.debug("container list:: " + containerList.toString());
			for (ContainerEntry containerEntry : containerList.list.entries) {
				if (containerEntry.entry.folderId.equals("documentLibrary")) {
					logger.debug(".inside if condition...");
					rootFolderId = containerEntry.entry.id;
					break;
				}
			}
		} catch (IOException e) {
			throw new RootFolderException("There is in getting RootFolderId:: " + e);
		}
		logger.debug("rootFolderId:: " + rootFolderId);
		return rootFolderId;
	}

	/**
	 * This method is to get child nodes
	 * 
	 * @param nodeId
	 *            :Node Id of target folder
	 * @throws FolderException
	 */
	public String getFolderChildRest(String nodeId) throws FolderException {
		HttpRequest request;
		String homeNetwork;
		String rawResponse;
		try {
			homeNetwork = getHomeNetwork();
			logger.debug("homeNetwork:: " + homeNetwork);
			String url = getAlfrescoAPIUrl() + homeNetwork + NODES_URL + nodeId + "/children";
			logger.debug("url:: " + url);
			GenericUrl folderChildUrl = new GenericUrl(url);
			request = getRequestFactory().buildGetRequest(folderChildUrl);
			rawResponse = request.execute().parseAsString();
			logger.debug("rawResponse:: " + rawResponse);
		} catch (IOException e) {
			throw new FolderException("Unable to retrieve the information");
		}
		return rawResponse;

	}

	/**
	 * This method is to create Folder using Rest API
	 * 
	 * @param cmisSession
	 * @param parentFolderId
	 * @param folderName
	 * @return Folder
	 * @author Md Noorshid
	 * @throws FolderCreationException
	 * @throws RootFolderException
	 *
	 */
	public int createFolderUsingRest(String folderName) throws FolderCreationException, RootFolderException {
		int statusCode;
		try {
			String homeNetwork = getHomeNetwork();
			logger.debug("homeNetwork:: " + homeNetwork);
			String url = getAlfrescoAPIUrl() + getHomeNetwork() + NODES_URL + getRootFolderId(getSite()) + "/children";
			logger.debug("url:: " + url);
			GenericUrl createFolderUrl = new GenericUrl(url);
			JSONObject jsonBody = new JSONObject();
			jsonBody.put("name", folderName);
			jsonBody.put("nodeType", "cm:folder");
			HttpContent contentBody = new ByteArrayContent("application/json", jsonBody.toString().getBytes());
			HttpRequest request = getRequestFactory().buildPostRequest(createFolderUrl, contentBody);
			HttpResponse response = request.execute();
			statusCode = response.getStatusCode();
		} catch (IOException e) {
			throw new FolderCreationException("There is exception in creating folder: ", e);
		}
		return statusCode;
	}

	/**
	 * Method to create sub folder using Alfresco Rest API
	 * 
	 * @param parentFolderPath
	 * @param subFolderName
	 * @throws CreateDocumentException
	 * @throws RootFolderException
	 * @throws FolderException
	 * @throws CreateFolderException
	 */
	public int createSubFolderUsingRest(String parentFolderPath, String subFolderName)
			throws RootFolderException, FolderException, CreateFolderException {
		String homeNetwork;
		int statusCode;
		try {
			homeNetwork = getHomeNetwork();
			logger.debug("homeNetwork:: " + homeNetwork);
			String url = getAlfrescoAPIUrl() + getHomeNetwork() + NODES_URL + getRootFolderId(getSite()) + "/children";
			logger.debug("url:: " + url);
			GenericUrl createSubFolderUrl = new GenericUrl(url);
			JSONObject jsonBody = new JSONObject();
			jsonBody.put("name", subFolderName);
			jsonBody.put("nodeType", "cm:folder");
			String relativePath = "/" + parentFolderPath; // Path Pattern: X/Y/Z
			logger.debug("relativePath:: " + relativePath);
			jsonBody.put("relativePath", relativePath);
			HttpContent contentBody = new ByteArrayContent("application/json", jsonBody.toString().getBytes());
			HttpRequest request = getRequestFactory().buildPostRequest(createSubFolderUrl, contentBody);
			HttpResponse response = request.execute();
			statusCode = response.getStatusCode();
			logger.info("statusCode:::::::: " + statusCode);
			logger.debug("===CHILD FOLDER CREATED SUCESSFULLY===");
		} catch (IOException e) {
			throw new CreateFolderException("Document creation Error:: " + e);
		}
		return statusCode;
	}

	/**
	 * This method is to create document
	 * 
	 * @param parentFolderPath
	 * @param documentName
	 * @param contentsOfDocument
	 * @throws CreateDocumentException
	 * @throws RootFolderException
	 */
	public int createDocumentRest(String parentFolderPath, String documentName, String contentsOfDocument)
			throws CreateDocumentException, RootFolderException {
		HttpContent contentBody = null;
		HttpRequest request = null;
		HttpResponse response = null;
		JSONObject jsonBody = null;
		int finalResponseStatus;
		try {
			String homeNetwork = getHomeNetwork();
			logger.debug("homeNetwork:: " + homeNetwork);
			String url = getAlfrescoAPIUrl() + getHomeNetwork() + NODES_URL + getRootFolderId(getSite()) + "/children";
			logger.debug("url:: " + url);
			GenericUrl createDocumentUrl = new GenericUrl(url);
			jsonBody = new JSONObject();
			jsonBody.put("name", documentName);
			jsonBody.put("nodeType", "cm:content");
			String relativePath = "/" + parentFolderPath; // Path Pattern: X/Y/Z
			logger.debug("relativePath:: " + relativePath);
			jsonBody.put("relativePath", relativePath);
			contentBody = new ByteArrayContent("application/json", jsonBody.toString().getBytes());
			request = getRequestFactory().buildPostRequest(createDocumentUrl, contentBody);
			response = request.execute();
			String rawResponse = response.parseAsString();
			logger.debug("rawResponse:: " + rawResponse);
			JSONObject responseJsonObj = new JSONObject(rawResponse);
			GenericUrl updateUrl = new GenericUrl(getAlfrescoAPIUrl() + getHomeNetwork() + NODES_URL
					+ responseJsonObj.getJSONObject("entry").getString("id") + "/content");
			logger.info("Document BY NAME " + documentName + "CREATED SUCCESSFULLY,NOW WRITING CONTENTS ON IT......");
			contentBody = new ByteArrayContent("text/plain", contentsOfDocument.getBytes());
			request = getRequestFactory().buildPutRequest(updateUrl, contentBody);
			response = request.execute();
			finalResponseStatus = response.getStatusCode();
			logger.debug("Final Response:: " + response.parseAsString());
			logger.info("===CONTENTS WRITTEN SUCCESSFULLY===");
		} catch (IOException e) {
			throw new CreateDocumentException("Document creation Error:: " + e);
		}
		return finalResponseStatus;
	}

	/**
	 * This method is to download the docuement using rest api
	 * 
	 * @param objectId
	 * @throws DownLoadDocException
	 * @throws PathAlreadyExist
	 */
	public void downloadDocumentUsingRest(String objectId, String documentName, String targetFolderPath,
			String sourcePath) throws DownLoadDocException, PathAlreadyExist {
		HttpRequest request;
		HttpResponse response = null;
		try {
			String homeNotework = getHomeNetwork();
			logger.debug("homeNetwork:: " + homeNetwork);
			String encodedDownloadURL = getAlfrescoAPIUrl() + homeNotework + NODES_URL + objectId + "/content";
			logger.debug("encodedDownloadURL:: " + encodedDownloadURL);
			GenericUrl downloadUrl = new GenericUrl(encodedDownloadURL);
			request = getRequestFactory().buildGetRequest(downloadUrl);
			response = request.execute();
			String rawResponse = response.parseAsString();
			logger.debug("rawResponse::: " + rawResponse);
			Files.write(Paths.get(targetFolderPath + "\\" + documentName + ".txt"), rawResponse.getBytes());
			logger.debug("Response status id:: " + response.getStatusCode());
			logger.info("DOWNLOAD SUCCESSFUL");
		} catch (IOException e) {
			throw new DownLoadDocException("There is exception in downloading::  " + e);
		}
	}

	/**
	 * This method is to download document based on Versions
	 * 
	 * @param nodeId
	 *            : Object Id of the target folder
	 * @param documentName:
	 *            Name of the target folder
	 * @param versionId
	 *            : Version Id of the target Document
	 * @param targetFolderPath
	 *            : Target path where need to download the document
	 * @param sourcePath
	 *            : Source path where the document exists
	 * @throws DownLoadDocException
	 */
	public int downloadDocumentBasedOnVersions(String nodeId, String documentName, String versionId,
			String targetFolderPath, String sourcePath) throws DownLoadDocException {

		HttpRequest request;
		HttpResponse response = null;
		int statusId;
		try {
			String homeNotework = getHomeNetwork();
			logger.debug("homeNetwork:: " + homeNetwork);
			String encodedDownloadURL = getAlfrescoAPIUrl() + homeNotework + NODES_URL + nodeId + "/versions/"
					+ versionId + "/content";
			logger.debug("encodedDownloadURL:: " + encodedDownloadURL);
			GenericUrl downloadUrl = new GenericUrl(encodedDownloadURL);
			request = getRequestFactory().buildGetRequest(downloadUrl);
			response = request.execute();
			String rawResponse = response.parseAsString();
			logger.debug("rawResponse::: " + rawResponse);
			Files.write(Paths.get(targetFolderPath + "\\" + documentName + versionId + ".txt"), rawResponse.getBytes());
			statusId = response.getStatusCode();
			logger.debug("Response status id:: " + response.getStatusCode());
			logger.info("DOWNLOAD SUCCESSFUL");
		} catch (IOException e) {
			throw new DownLoadDocException("There is exception in downloading::  " + e);
		}
		return statusId;

	}

	/**
	 * This method is to create user using RestApi
	 * 
	 * @param id
	 * @param firstName
	 * @param lastName
	 * @param email
	 * @param password
	 * @param customProperties
	 * @throws UserCreationException
	 */
	public int createUserRest(String id, String firstName, String lastName, String email, String password)
			throws UserCreationException {
		HttpContent contentBody = null;
		HttpRequest request = null;
		HttpResponse response = null;
		JSONObject jsonBody = null;
		int responseStatusCode;
		try {
			String homeNetwork = getHomeNetwork();
			logger.debug("homeNetwork:: " + homeNetwork);
			String encodedPeopleURL = getAlfrescoAPIUrl() + homeNetwork + PEOPLE_URL;
			logger.debug("encodedPeopleURL:: " + encodedPeopleURL);
			jsonBody = new JSONObject();
			jsonBody.put("id", id);
			jsonBody.put("firstName", firstName);
			jsonBody.put("lastName", lastName);
			jsonBody.put("email", email);
			jsonBody.put("password", password);
			GenericUrl createPeropleUrl = new GenericUrl(encodedPeopleURL);
			contentBody = new ByteArrayContent("application/json", jsonBody.toString().getBytes());
			request = getRequestFactory().buildPostRequest(createPeropleUrl, contentBody);
			response = request.execute();
			responseStatusCode = response.getStatusCode();
		} catch (IOException e) {
			e.getStackTrace();
			throw new UserCreationException("User Creation Failed ", e);
		}
		return responseStatusCode;
	}

	/**
	 * This method is to make favorite for any site
	 * 
	 * @param siteId
	 *            : SiteId which need to make favorite
	 * @param personId:
	 *            Person Id for which site has to make favorite
	 * @throws UnableToCreateFavoriteException
	 */
	public int createFavouriteSite(String siteId, String personId) throws UnableToCreateFavoriteException {
		HttpRequest request;
		HttpResponse response;
		HttpContent contentBody;
		GenericUrl genericUrl;
		JSONObject requestBodyObj1;
		int statusCode;
		try {
			String homeNetwork = getHomeNetwork();
			String URL = getAlfrescoAPIUrl() + homeNetwork + PEOPLE_URL + "/" + personId + "/favorite-sites";
			genericUrl = new GenericUrl(URL);
			requestBodyObj1 = new JSONObject();
			requestBodyObj1.put("id", siteId);
			JSONArray finalBodyRequest = new JSONArray().put(requestBodyObj1);
			logger.debug("finalBodyRequest:: " + finalBodyRequest);
			contentBody = new ByteArrayContent("application/json", finalBodyRequest.toString().getBytes());
			request = getRequestFactory().buildPostRequest(genericUrl, contentBody);
			statusCode = request.execute().getStatusCode();

		} catch (IOException e) {
			throw new UnableToCreateFavoriteException("Unable to make favorite ", e);
		}
		return statusCode;

	}

	/**
	 * This method is to create group
	 * 
	 * @param id
	 * @param displayName
	 * @throws GroupCreationException
	 */
	public int createGroupRest(String id, String displayName) throws GroupCreationException {
		HttpContent contentBody;
		HttpRequest request;
		HttpResponse response;
		JSONObject jsonBody;
		int statusCode;
		try {
			String homeNewteork = getHomeNetwork();
			logger.debug("homeNewteork:: " + homeNewteork);
			String url = getAlfrescoAPIUrl() + homeNewteork + GROUP_URL;
			logger.debug("url:: " + url);
			GenericUrl groupUrl = new GenericUrl(url);
			jsonBody = new JSONObject();
			jsonBody.put("id", id);
			jsonBody.put("displayName", displayName);
			contentBody = new ByteArrayContent("application/json", jsonBody.toString().getBytes());
			request = getRequestFactory().buildPostRequest(groupUrl, contentBody);
			response = request.execute();
			statusCode = response.getStatusCode();
			logger.info("====GROUP CREATED SUCCESSFULLY====");

		} catch (IOException e) {
			throw new GroupCreationException("User Creation Failed ", e);
		}
		return statusCode;

	}

	/**
	 * This method used to add members in existing Group
	 * 
	 * @param groupId
	 *            : It will be type PERSON if user already exist otherwise GROUP
	 *            for non existing USER
	 * @param userId
	 *            : User Id to add in Group
	 * @param memberType
	 *            : It will be type GROUP
	 * @throws AddMemberCreationException
	 */
	public int addMembersInGroup(String groupId, String userId, String memberType) throws AddMemberCreationException {
		HttpRequest request;
		HttpResponse response;
		HttpContent contentBody;
		JSONObject jsonBody;
		int statusResponse;
		try {
			String homeNetwork = getHomeNetwork();
			logger.debug("homeNetwork:: " + homeNetwork);
			String url = getAlfrescoAPIUrl() + homeNetwork + GROUP_URL + "/" + groupId + "/members";
			logger.debug("url:: " + url);
			GenericUrl addMemberUrl = new GenericUrl(url);
			jsonBody = new JSONObject();
			jsonBody.put("id", userId);
			jsonBody.put("memberType", memberType);
			contentBody = new ByteArrayContent("application/json", jsonBody.toString().getBytes());
			request = getRequestFactory().buildPostRequest(addMemberUrl, contentBody);
			response = request.execute();
			statusResponse = response.getStatusCode();
			logger.debug("statusResponse:: " + statusResponse);
		} catch (IOException e) {
			throw new AddMemberCreationException("Adding members failed", e);
		}
		return statusResponse;
	}

	/**
	 * Method to create site
	 * 
	 * @param title
	 *            : Title of Site
	 * @param visibilty
	 *            : Access of the site, value may be PUBLIC,MODERETED,PRIVATE
	 * @throws SiteCreationException
	 */
	public int createSiteRest(String title, String visibility) throws SiteCreationException {
		HttpContent contentBody;
		HttpRequest request;
		HttpResponse response;
		JSONObject jsonBody;
		int statusCode;
		try {
			String homeNetwork = getHomeNetwork();
			String url = getAlfrescoAPIUrl() + homeNetwork + SITE_URL;
			GenericUrl siteUrl = new GenericUrl(url);
			logger.debug("url:: " + url);
			jsonBody = new JSONObject();
			jsonBody.put("title", title);
			jsonBody.put("visibility", visibility);
			contentBody = new ByteArrayContent("application/json", jsonBody.toString().getBytes());
			request = getRequestFactory().buildPostRequest(siteUrl, contentBody);
			response = request.execute();
			statusCode = response.getStatusCode();
			logger.debug("statusCode:: " + statusCode);
		} catch (IOException e) {
			throw new SiteCreationException("There is exception in creating site ", e);
		}
		return statusCode;
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
	public void membershipRequestInSite(String personId, String message, String id, String title)
			throws MemberShipRequestException {
		HttpContent contentBody;
		HttpRequest request;
		HttpResponse response;
		JSONObject jsonBody;
		try {
			String homeNetwork = getHomeNetwork();
			String url = getAlfrescoAPIUrl() + homeNetwork + PEOPLE_URL + "/" + personId + "/site-membership-requests";
			logger.debug("url:: " + url);
			GenericUrl membershipUrl = new GenericUrl(url);
			jsonBody = new JSONObject();
			jsonBody.put("message", message);
			jsonBody.put("id", id);
			jsonBody.put("title", title);
			JSONArray arrBody = new JSONArray();
			arrBody.put(jsonBody);
			contentBody = new ByteArrayContent("application/json", arrBody.toString().getBytes());
			request = getRequestFactory().buildPostRequest(membershipUrl, contentBody);
			response = request.execute();
			if (response.getStatusCode() == 201) {
				logger.info("====RESQUEST CREATED SUCCESSFULLY====");
			}
		} catch (IOException e) {
			throw new MemberShipRequestException("There is Exception in requesting membership request for Site", e);
		}
	}
    
	/**
	 * This method is to create site Membership By Admin
	 * @param siteId : Target site id
	 * @param personId : Target Person
	 * @param role  : Role Assigned for the target site SiteConsumer,SiteCollaborator,SiteContributor,SiteManager
	 * @return
	 * @throws MemberShipRequestException
	 */
	public int createSiteMembershipRequest(String siteId, String personId, String role)
			throws MemberShipRequestException {
		HttpRequest request;
		HttpResponse response;
		HttpContent content;
		JSONArray finalJsonArrBody;
		GenericUrl genericUrl;
		int requestStatus;
		try {
			String homeNetwork = getHomeNetwork();
			String URL = getAlfrescoAPIUrl() + homeNetwork + SITE_URL + "/" + siteId + "/members";
			logger.debug("URL:: "+URL);
			genericUrl = new GenericUrl(URL);
			JSONObject requestObj = new JSONObject();
			requestObj.put("id", personId);
			requestObj.put("role", role);
			finalJsonArrBody = new JSONArray().put(requestObj);
			content = new ByteArrayContent("application/json", finalJsonArrBody.toString().getBytes());
			request = getRequestFactory().buildPostRequest(genericUrl, content);
			requestStatus = request.execute().getStatusCode();

		} catch (IOException e) {
			throw new MemberShipRequestException("There is Exception in creating membership request for Site", e);
		}
		return requestStatus;

	}

	/**
	 * This method is to delete folder
	 * 
	 * @param nodeId
	 *            : target node id
	 * @throws DeleteNodeException
	 */
	public int deleteNodeRest(String nodeId) throws DeleteNodeException {
		HttpRequest request;
		HttpResponse response;
		GenericUrl deleteUrl;
		try {
			String homeNetwork = getHomeNetwork();
			String url = getAlfrescoAPIUrl() + homeNetwork + NODES_URL + nodeId;
			logger.debug("url:: " + url);
			deleteUrl = new GenericUrl(url);
			request = getRequestFactory().buildDeleteRequest(deleteUrl);
			response = request.execute();
		} catch (IOException e) {
			throw new DeleteNodeException("Unable to delete node", e);
		}
		return response.getStatusCode();
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
	public int deleteMemberFromGroup(String groupId, String groupMemberId) throws RemoveMemberException {
		HttpRequest request;
		HttpResponse response;
		GenericUrl deleteMemberUrl;
		try {
			String homeNetwork = getHomeNetwork();
			String url = getAlfrescoAPIUrl() + homeNetwork + GROUP_URL + groupId + "/members/" + groupMemberId;
			logger.debug("url:: " + url);
			deleteMemberUrl = new GenericUrl(url);
			request = getRequestFactory().buildDeleteRequest(deleteMemberUrl);
			response = request.execute();
		} catch (IOException e) {
			throw new RemoveMemberException("Unable to remove member", e);
		}
		return response.getStatusCode();
	}

	/**
	 * Method to get the activities of a User
	 * 
	 * @return
	 * @throws ActiviesException
	 */
	public String getActivities() throws ActiviesException {
		HttpRequest request;
		HttpResponse response;
		GenericUrl genericUrl;
		String responseBody;
		try {
			String homeNetwork = getHomeNetwork();
			String URL = getAlfrescoAPIUrl() + homeNetwork + PEOPLE_URL + "/-me-/" + "activities";
			genericUrl = new GenericUrl(URL);
			request = getRequestFactory().buildGetRequest(genericUrl);
			responseBody = request.execute().parseAsString();
		} catch (IOException e) {
			throw new ActiviesException("There is exception in getting the activities", e);
		}
		return responseBody;
	}

	/**
	 * Use the REST API to "like" an object
	 *
	 * @param requestFactory
	 * @param homeNetwork
	 * @param objectId
	 * @throws IOException
	 */
	public int like(String objectId) throws IOException {
		GenericUrl likeUrl = new GenericUrl(getAlfrescoAPIUrl() + getHomeNetwork() + NODES_URL + objectId + "/ratings");
		int responseStatus;
		HttpContent body = new ByteArrayContent("application/json",
				"{\"id\": \"likes\", \"myRating\": true}".getBytes());
		HttpRequest request = getRequestFactory().buildPostRequest(likeUrl, body);
		HttpResponse response = request.execute();
		responseStatus = response.getStatusCode();
		logger.info("You liked: " + objectId);
		return responseStatus;
	}

	/**
	 * Use the REST API to comment on an object
	 *
	 * @param requestFactory
	 * @param homeNetwork
	 * @param objectId
	 * @param comment
	 * @throws IOException
	 */
	public void comment(String objectId, String comment) throws IOException {
		String homeNetwork = getHomeNetwork();
		logger.debug("homeNetwork:: " + homeNetwork);
		logger.info("comment URL:: " + getAlfrescoAPIUrl() + homeNetwork + NODES_URL + objectId + "/comments");
		GenericUrl commentUrl = new GenericUrl(getAlfrescoAPIUrl() + homeNetwork + NODES_URL + objectId + "/comments");
		HttpContent body = new ByteArrayContent("application/json", ("{\"content\": \"" + comment + "\"}").getBytes());
		HttpRequest request = getRequestFactory().buildPostRequest(commentUrl, body);
		request.execute();
		logger.info("You commented on: " + objectId);
	}

	/**
	 * This method is to get the node queries
	 * 
	 * @throws QueryExceptio
	 */
	public String getNodesQueries(String searchString, String rootFolder) throws QueryExceptio {
		HttpRequest request;
		String responseBody;
		GenericUrl genericUrl;
		String homeNetwork;
		try {
			homeNetwork = getHomeNetwork();
			String URL = getAlfrescoAPIUrl() + homeNetwork + QUERIES + "nodes?term=" + searchString + "&rootNodeId="
					+ rootFolder;
			genericUrl = new GenericUrl(URL);
			request = getRequestFactory().buildGetRequest(genericUrl);
			responseBody = request.execute().parseAsString();
		} catch (IOException e) {
			throw new QueryExceptio("There is Exception in getting Node", e);
		}
		return responseBody;

	}

	public String getSite() {
		return Config.getConfig().getProperty("site");
	}

	public String getFolderName() {
		return Config.getConfig().getProperty("folder_name");
	}

	public File getLocalFile() {
		String filePath = Config.getConfig().getProperty("local_file_path");
		return new File(filePath);
	}

	public String getLocalFileType() {
		return Config.getConfig().getProperty("local_file_type");
	}

	abstract public String getAlfrescoAPIUrl();

	abstract public Session getCmisSession();

	abstract public HttpRequestFactory getRequestFactory();
}
