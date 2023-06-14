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

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

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
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import com.michelin.cio.jenkins.plugin.requests.action.RequestMailSender.DescriptorEmailImpl;

import hudson.util.Secret;

// @author John Flynn <john.trixmot.flynn@gmail.com>

public class RequestsUtility {

	private static final Logger LOGGER = Logger.getLogger(RequestsUtility.class.getName());

	public String runPostMethod(String jenkinsURL, String urlString) throws ClientProtocolException, IOException {
		LOGGER.info("[INFO] urlString: " + urlString);
		String returnStatus;

		try {
			DescriptorEmailImpl descriptorEmailImpl = new DescriptorEmailImpl();
			String username = descriptorEmailImpl.getUnlockuser();
			// The password must be a Jenkins User Token:
			Secret password = descriptorEmailImpl.getUnlockpassword();
			URI uri = URI.create(urlString);
			HttpHost host = new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme());
			CredentialsProvider credsProvider = new BasicCredentialsProvider();
			credsProvider.setCredentials(new AuthScope(uri.getHost(), uri.getPort()), new UsernamePasswordCredentials(username, password.getPlainText()));
			AuthCache authCache = new BasicAuthCache();
			BasicScheme basicAuth = new BasicScheme();
			authCache.put(host, basicAuth);
			CloseableHttpClient httpClient = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build();

			HttpPost httpPost = new HttpPost(uri);

			// Add AuthCache to the execution context
			HttpClientContext localContext = HttpClientContext.create();
			localContext.setAuthCache(authCache);

			HttpResponse response = httpClient.execute(host, httpPost, localContext);
			int responseCode = response.getStatusLine().getStatusCode();
			LOGGER.info("[INFO] responseCode: " + responseCode);

			if ((responseCode > 199) && (responseCode < 400)) {
				returnStatus = "success";
			} else {
				LOGGER.info("[ERROR] httpClient getReasonPhrase: " + response.getStatusLine().getReasonPhrase());
				returnStatus = response.getStatusLine().getReasonPhrase();
			}

		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Unable to Delete the build " + e.getMessage().toString());

			returnStatus = e.getMessage().toString();
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

		LOGGER.info("[INFO] FULL FOLDER PROJECT NAME: " + stringBuffer.toString());

		return stringBuffer.toString();
	}

	// Encode project name for url using `UTF-8` encoding scheme:
	public String encodeValue(String value) {
		try {
			return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
		} catch (UnsupportedEncodingException ex) {
			throw new RuntimeException(ex.getCause());
		}
	}

	public boolean verifyJobType(String strJobName) {
		// Used by *ActionFactory.java to verify that the job is a MultiBranch pipeline
		// jobType:
		if (strJobName != null) {
			Document document = createDocumentFromXMLString(strJobName);
			if (document != null) {
				Node node = document.getDocumentElement();
				String root = node.getNodeName();
				String rootElementString = "org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject";

				if (rootElementString.equalsIgnoreCase(root)) {
					return true;
				}
			}
		}

		return false;
	}

	public Document createDocumentFromXMLString(String xmlString) {
		Document document = null;
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			document = builder.parse(new InputSource(new StringReader(xmlString)));
			document.getDocumentElement().normalize();

		} catch (Exception e) {
			LOGGER.warning("ERROR: CreateDocumentFromXMLString(): " + xmlString + " - Exception error: " + e.getMessage());
			return null;
		}

		return document;
	}

}
