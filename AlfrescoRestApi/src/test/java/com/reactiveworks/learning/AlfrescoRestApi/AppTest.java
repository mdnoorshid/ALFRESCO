package com.reactiveworks.learning.AlfrescoRestApi;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.apache.ApacheHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson.JacksonFactory;

/**
 * Unit test for simple App.
 */
public class AppTest {
	static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AppTest.class);
	public static HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
	static HttpRequestFactory requestFactory;

	@BeforeClass
	public static void getRequestFactory() {
		if (requestFactory == null) {
            requestFactory=HTTP_TRANSPORT.createRequestFactory(new HttpRequestInitializer() {
				
				@Override
				public void initialize(HttpRequest request) throws IOException {
                 request.setParser(new JsonObjectParser(new JacksonFactory()));
                 request.getHeaders().setBasicAuthentication("admin", "admin");
					
				}
			});
		}

	}

	@Test
	public void testHttp() throws IOException {

		String URL = "http://api-explorer.alfresco.com/alfresco/api/-default-/public/alfresco/versions/1/sites";
		HttpTransport transport = new ApacheHttpTransport();
		HttpRequestFactory requestFactory = transport.createRequestFactory();
		GenericUrl genericUrl = new GenericUrl(URL);
		logger.debug("requestFactory::::: "+requestFactory.toString());
		HttpRequest request = requestFactory.buildGetRequest(genericUrl);
		HttpResponse response = request.execute();
		logger.debug("response status:: " + response.getStatusCode());

	}

}
