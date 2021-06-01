/*
 * The MIT License
 *
 * Copyright 2019 Lexmark
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.michelin.cio.jenkins.plugin.requests.model;

import com.michelin.cio.jenkins.plugin.requests.action.RequestMailSender.DescriptorEmailImpl;

import hudson.util.Secret;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;



// @author John Flynn <john.trixmot.flynn@gmail.com>

public class RequestsUtility {

	private static final Logger LOGGER = Logger.getLogger(RequestsUtility.class.getName());
	
	public String runPostMethod(String jenkinsURL, String urlString)
			throws ClientProtocolException, IOException {
		LOGGER.info("[INFO] urlString: " + urlString);
		String returnStatus;
		DescriptorEmailImpl descriptorEmailImpl = new DescriptorEmailImpl();
		String username = descriptorEmailImpl.getUnlockuser();
		// The password must be a Jenkins User Token:
		Secret password = descriptorEmailImpl.getUnlockpassword();
		URI uri = URI.create(urlString);
		HttpHost host = new HttpHost(uri.getHost(), uri.getPort(),
				uri.getScheme());
		CredentialsProvider credsProvider = new BasicCredentialsProvider();
		credsProvider.setCredentials(
				new AuthScope(uri.getHost(), uri.getPort()),
				new UsernamePasswordCredentials(username,
						password.getPlainText()));
		AuthCache authCache = new BasicAuthCache();
		BasicScheme basicAuth = new BasicScheme();
		authCache.put(host, basicAuth);
		CloseableHttpClient httpClient = HttpClients.custom()
				.setDefaultCredentialsProvider(credsProvider).build();
		HttpPost httpPost = new HttpPost(uri);

		// Add AuthCache to the execution context
		HttpClientContext localContext = HttpClientContext.create();
		localContext.setAuthCache(authCache);

		HttpResponse response = httpClient.execute(host, httpPost,
				localContext);
		int responseCode = response.getStatusLine().getStatusCode();
		LOGGER.info("[INFO] responseCode: " + responseCode);

		if ((responseCode > 199) && (responseCode < 400)) {
			returnStatus = "success";
		} else {
			LOGGER.info("[ERROR] httpClient getReasonPhrase: "
					+ response.getStatusLine().getReasonPhrase());
			returnStatus = response.getStatusLine().getReasonPhrase();
		}
		return returnStatus;
	}
	
	public String constructFolderJobName(String inputName) {
		String[] projectNameList = null;
		StringBuffer stringBuffer = new StringBuffer();
		projectNameList = inputName.split("/");
		
		// Need to add '/job/' in between all names:
		int nameCount = projectNameList.length;
		stringBuffer.append(projectNameList[0]);
		for (int i = 1; i < nameCount; i++) {
			stringBuffer.append("/job/");
			stringBuffer.append(projectNameList[i]);
		}

		//LOGGER.info("[INFO] FULL FOLDER PROJECT NAME: " + stringBuffer.toString());
		
		return stringBuffer.toString();
	}
	
	public String[] constructFolderJobNameAndFull(String buildURL) {
		String[] project_and_BuildList = null;
		String[] returnNameList = new String[2];
		//Use StringBuffer instead of concatenate strings:
		StringBuffer stringBuffer = new StringBuffer(); 
		String projectName = "";	

		LOGGER.log(Level.INFO,"[INFO] constructFolderJobNameAndFull inputName: " + buildURL);
		
		buildURL = buildURL.replace("%20", " ");
		
		//view/JOHN/job/Utilities/job/FolderLayer_2/job/folderlayerjob/1/
		//Split on "/job/" ignore index 0 and last:
		project_and_BuildList = buildURL.split("/job/");	
		int nameCount = project_and_BuildList.length;
		LOGGER.log(Level.INFO,"[INFO] constructFolderJobNameAndFull nameCount: " + nameCount);
		String firstFolderName = project_and_BuildList[1];
		stringBuffer.append(firstFolderName);
		
		//Skip first index /job/ and the buildname from the end:
		for (int i = 2; i < nameCount; i++) {
			stringBuffer.append("/job/");
			if (i == nameCount -1) {
				String[] projectBuild = project_and_BuildList[i].split("/");
				stringBuffer.append(projectBuild[0]);
				projectName = projectBuild[0];
			} else
			{
				stringBuffer.append(project_and_BuildList[i]);
			}						
		}
		
		returnNameList[0] = projectName; 				//projectName
		returnNameList[1] = stringBuffer.toString(); 	//projectFullName == foldername1/job/foldername2/job/projectname
		
		return returnNameList;
	}
	
	// Encode project name for url using `UTF-8` encoding scheme:
    public String encodeValue (String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex.getCause());
        }
    }

}
