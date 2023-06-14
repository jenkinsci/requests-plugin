/*
 * The MIT License
 *
 * Copyright (c) 2011-2012, Manufacture Francaise des Pneumatiques Michelin, Daniel Petisme
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

import java.util.Calendar;
import java.util.logging.Logger;

import org.apache.commons.lang.time.FastDateFormat;

import hudson.model.Item;
import jenkins.model.Jenkins;

//import java.util.logging.Logger;

// @author Daniel Petisme <daniel.petisme@gmail.com> <http://danielpetisme.blogspot.com/>

public abstract class Request {

	protected String requestType;
	protected String username;
	protected String jobNameSpace;
	protected String buildNumber;
	protected String fullJobURL;
	protected String jobNameSlash;
	protected String jobNameJelly;
	protected String rename;
	protected String errorMessage;
	private String creationDate;

	private static String dateFormat = "yyyy-MM-dd HH:mm:ss";
	private static final FastDateFormat yyyymmdd = FastDateFormat.getInstance(dateFormat);
	private static final Logger LOGGER = Logger.getLogger(Request.class.getName());

	public Request(String requestType, String username, String jobNameSpaces, String buildNumber, String fullJobURL, String jobNameSlash, String jobNameJelly, String rename) {
		this.requestType = requestType;
		this.username = username;
		this.jobNameSpace = jobNameSpaces;
		this.buildNumber = buildNumber;
		this.fullJobURL = fullJobURL;
		this.jobNameSlash = jobNameSlash;
		this.jobNameJelly = jobNameJelly;
		this.rename = rename;
		this.creationDate = yyyymmdd.format(Calendar.getInstance().getTime());
	}

	public String getJobNameSpace() {
		return jobNameSpace;
	}

	public String getJobNameSlash() {
		return jobNameSlash;
	}

	public String getJobNameJelly() {
		return jobNameJelly;
	}

	public String getRename() {
		return rename;
	}

	public String getFullJobURL() {
		return fullJobURL;
	}

	public String getRequestType() {
		return requestType;
	}

	public String getUsername() {
		return username;
	}

	public String getBuildNumber() {
		return buildNumber;
	}

	public String getCreationDate() {
		return creationDate;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public abstract String getMessage();

	// Called from RequestsPlugin.java from the Pending Requests web page:
	public boolean process(String requestType) {
		boolean success = false;
		String jobNameSlash = getJobNameSlash();
		LOGGER.info("Request process(): " + jobNameSlash);

		try {

			Item item = Jenkins.get().getItemByFullName(jobNameSlash);

			if (requestType.equalsIgnoreCase("unlockBuild") || requestType.equalsIgnoreCase("deleteBuild")) {
				// Unlock and Delete Build Requests do not use the item object since they will use a URL in POST method:
				success = execute(item);

			} else {

				if (item != null) {
					// This is called on the {requestType}Request.java file:
					success = execute(item);
				} else {
					// If the item object is null, no sense in trying to call the Request:
					if (requestType.equals("deleteJob") || requestType.equals("renameJob")) {
						errorMessage = "Unable to find the job " + jobNameSlash;
					}
					if (requestType.equals("deleteMultiBranch")) {
						errorMessage = "Unable to find the MultiBranch Pipeline folder " + jobNameSlash;
					}
					if (requestType.equals("deleteFolder") || requestType.equals("renameFolder")) {
						errorMessage = "Unable to find the folder " + jobNameSlash;
					}
				}
			}

		} catch (Exception e) {
			errorMessage = e.getMessage().toString();

			return false;
		}

		return success;
	}

	public abstract boolean execute(Item item);

}
