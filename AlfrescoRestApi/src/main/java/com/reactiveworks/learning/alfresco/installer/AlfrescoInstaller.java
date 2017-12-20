package com.reactiveworks.learning.alfresco.installer;

import org.json.JSONException;
import org.json.JSONObject;

import com.reactiveworks.learning.alfresco.operation.ActiviesException;
import com.reactiveworks.learning.alfresco.operation.AddMemberCreationException;
import com.reactiveworks.learning.alfresco.operation.AlfrescoRestCallOperation;
import com.reactiveworks.learning.alfresco.operation.CreateCommentException;
import com.reactiveworks.learning.alfresco.operation.CreateDocumentException;
import com.reactiveworks.learning.alfresco.operation.CreateFolderException;
import com.reactiveworks.learning.alfresco.operation.DeleteNodeException;
import com.reactiveworks.learning.alfresco.operation.DownLoadDocException;
import com.reactiveworks.learning.alfresco.operation.FolderException;
import com.reactiveworks.learning.alfresco.operation.GroupCreationException;
import com.reactiveworks.learning.alfresco.operation.LikeDocumentException;
import com.reactiveworks.learning.alfresco.operation.MemberShipRequestException;
import com.reactiveworks.learning.alfresco.operation.PathAlreadyExist;
import com.reactiveworks.learning.alfresco.operation.QueryExceptio;
import com.reactiveworks.learning.alfresco.operation.RemoveMemberException;
import com.reactiveworks.learning.alfresco.operation.RootFolderException;
import com.reactiveworks.learning.alfresco.operation.SiteCreationException;
import com.reactiveworks.learning.alfresco.operation.UnableToCreateFavoriteException;
import com.reactiveworks.learning.alfresco.operation.UserCreationException;
import com.reactiveworks.learning.alfresco.util.Config;

/**
 * Alfresco Installer Class
 *
 */
public class AlfrescoInstaller {
	public static void main(String[] args) throws CreateCommentException, CreateFolderException, LikeDocumentException,
			DownLoadDocException, CreateDocumentException, RootFolderException, FolderException, PathAlreadyExist,
			JSONException, UserCreationException, GroupCreationException, AddMemberCreationException,
			SiteCreationException, MemberShipRequestException, DeleteNodeException, RemoveMemberException,
			UnableToCreateFavoriteException, ActiviesException, QueryExceptio {
		System.out.println("PROGRAM STARTED...");
		AlfrescoRestCallOperation alfrescoRestCallOperation = new AlfrescoRestCallOperation();
		// alfrescoRestCallOperation.createComment("/TestFolder", "TestDoc", "This is rescently liked by admin"); //SUCCESSFUL
		// alfrescoRestCallOperation.likeDocument("/TestFolder", "TestDoc");//SUCCESSFUL
		// alfrescoRestCallOperation.downloadDocument("DocTest","C:\\Users\\Niyamat\\Desktop\\DROPBOX","/RestFolder"); //SUCCESSFUL
	  //alfrescoRestCallOperation.createFolderRest("Curt Hamatys");
		//alfrescoRestCallOperation.createSubFolderRest("RestFolder","RestSubFolder"); //SUCCESSFUL
		// alfrescoRestCallOperation.createDocumentUsingRest("RestFolder/Rest SubFolder", "TestRestDOC5", "This is the test document using REST API");//SUCCESSFUL
		//alfrescoRestCallOperation.createUser("mdnoorshid","Md", "Noorshid", "mdnoorshid@gmail.com","9955924034");//SUCCESSFUL
		// alfrescoRestCallOperation.createGroup("GROUP_REACTIVEWORKS","REACTIVEWORKS");//SUCCESSFUL

		// alfrescoRestCallOperation.addMemberGroup("GROUP_REACTIVEWORKS","cheta","PERSON");//SUCCESSFUL

       // alfrescoRestCallOperation.createSite("ACT", "PUBLIC");//SUCCESSFUL

		//alfrescoRestCallOperation.memberRequestSite("curt", "Please addme", "ACT", "Request For Adding");//PROBLEM
		// ITSELF FROM ALFRESCO

		// alfrescoRestCallOperation.getFolderChildInfo("Sites/alfresco-api-demo/documentLibrary/RestFolder");//SUCCESSFUL

		// alfrescoRestCallOperation.deleteFolder("Sites/alfresco-api-demo/documentLibrary/TestFolder");//SUCCESSFUL

		// alfrescoRestCallOperation.deleteDocument("Sites/alfresco-api-demo/documentLibrary/RestFolder","DocTest");//SUCCESSFUL

		// alfrescoRestCallOperation.removeMemberFromGroup("GROUP_REACTIVEWORKS","rajaD");//SUCCESSFUL
		// alfrescoRestCallOperation.downloadDocBasedOnVersions("DocTest","1.2","C:\\Users\\Niyamat\\Desktop\\DROPBOX", "/RestFolder");//SUCCESSFUL

		// alfrescoRestCallOperation.makeFavoriteSite("alfresco-api-demo",
		// "mdnoorshid");//SUCCESSFUL
		// alfrescoRestCallOperation.getActiviesOfCurrentUser();//SUCCESSFUL
		// alfrescoRestCallOperation.getNodeQuery("node","Repository"); //Not
		// properly explained in Alfresco API
		//alfrescoRestCallOperation.createSiteMembershipByAdmin("act","curt@arista.com","SiteContributor");//SUCCESSFUL
		

	}
}
