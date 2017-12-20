package com.reactiveworks.learning.alfresco.operation;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.Repository;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.enums.BindingType;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson.JacksonFactory;
import com.reactiveworks.learning.alfresco.util.Config;

/**
 * This class contains only the logic that is specific to using the Public API
 * against an Alfresco repository running on-premise (4.2.d or later).
 *
 * @author Md Noorshid
 */
public class BaseOnPrem   extends BasePublicAPI {
	public static final String CMIS_URL="/public/cmis/versions/1.1/atom";
	public static final HttpTransport HTTP_TRANSPORT=new NetHttpTransport();
	public static final JsonFactory JSON_FACTORY=new JacksonFactory();
	
    private HttpRequestFactory requestFactory;
    private Session cmisSession;
	
    public String getAtomPubURL(HttpRequestFactory httpRequestFactory){
    String alfrescoAPIUrl=getAlfrescoAPIUrl();
    String atomPubURL=null;
    try{
    atomPubURL=alfrescoAPIUrl+getHomeNetwork()+CMIS_URL;
    logger.debug("atomPubURL:: "+atomPubURL);    
    }catch(IOException e){
      logger.error("Warning: Couldn't determine home network, defaulting to -default-");	
      atomPubURL = alfrescoAPIUrl + "-default-" + CMIS_URL;
    }
    return atomPubURL;
    }
    
    
	
	@Override
	public String getAlfrescoAPIUrl() {
		logger.debug(".inside getAlfrescoAPIUrl() method.....");
        String host = Config.getConfig().getProperty("host"); //http://localhost:8080/alfresco
        logger.debug("host:: "+host);
        return host + "/api/";
    }
    
	/**
     * Gets a CMIS Session by connecting to the local Alfresco server.
     *
     * @return Session
     */
	@Override
	public Session getCmisSession() {
        if (cmisSession == null) {
            // default factory implementation
            SessionFactory factory = SessionFactoryImpl.newInstance();
            Map<String, String> parameter = new HashMap<String, String>();

            // connection settings
            parameter.put(SessionParameter.ATOMPUB_URL, getAtomPubURL(getRequestFactory()));
            parameter.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());
            parameter.put(SessionParameter.AUTH_HTTP_BASIC, "true");
            parameter.put(SessionParameter.USER, getUsername());
            parameter.put(SessionParameter.PASSWORD, getPassword());
            parameter.put(SessionParameter.OBJECT_FACTORY_CLASS, "org.alfresco.cmis.client.impl.AlfrescoObjectFactoryImpl");
            logger.debug("Parameters all set");

            List<Repository> repositories = factory.getRepositories(parameter);

            cmisSession = repositories.get(0).createSession();
            logger.debug("session build...");
        }
        return this.cmisSession;
    }

	@Override
	public HttpRequestFactory getRequestFactory() {
        if (this.requestFactory == null) {
            this.requestFactory = HTTP_TRANSPORT.createRequestFactory(new HttpRequestInitializer() {
                @Override
                public void initialize(HttpRequest request) throws IOException {
                    request.setParser(new JsonObjectParser(new JacksonFactory()));
                    request.getHeaders().setBasicAuthentication(getUsername(), getPassword());
                }
            });
        }
        return this.requestFactory;
    }
	
	 public String getUsername() {
	        return Config.getConfig().getProperty("username");
	    }

	    public String getPassword() {
	        return Config.getConfig().getProperty("password");
	    }

}
