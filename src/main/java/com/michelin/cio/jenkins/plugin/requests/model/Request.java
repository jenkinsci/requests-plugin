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

import hudson.model.Item;
import jenkins.model.Jenkins;

import java.util.Calendar;
import org.apache.commons.lang.time.FastDateFormat;

//import java.util.logging.Logger;

// @author Daniel Petisme <daniel.petisme@gmail.com> <http://danielpetisme.blogspot.com/>

public abstract class Request {

	protected String requestType;
	protected String username;
	protected String project;
	protected String projectFullName;
	protected String buildNumber;
	protected String errorMessage;
	private String creationDate;

	private static String dateFormat = "yyyy-MM-dd HH:mm:ss";
	private static final FastDateFormat yyyymmdd = FastDateFormat.getInstance(dateFormat);
	//private static final Logger LOGGER = Logger.getLogger(RequestsUtility.class.getName());

	public Request(String requestType, String username, String project, String projectFullName, String buildNumber) {
		this.requestType = requestType;
		this.username = username;
		this.project = project;
		this.projectFullName = projectFullName;
		this.buildNumber = buildNumber;
		this.creationDate = yyyymmdd.format(Calendar.getInstance().getTime());
	}

	public String getProject() {
		if (projectFullName.contains("/")) {
			String[] projectList = projectFullName.split("/");
			int nameCount = projectList.length;
			project = projectList[nameCount - 1];
		}
		return project;
	}

	public String getProjectNameWithoutJobSeparator() {
		String[] projectList = null;
		String projectFullNameWithoutJobSeparator;
		StringBuffer stringBuffer = new StringBuffer();
		
		if (projectFullName.contains("/job/")) {
			projectList = projectFullName.split("/job/");
			int nameCount = projectList.length;
			stringBuffer.append(projectList[0]);
			for (int i = 1; i < nameCount; i++) {
				stringBuffer.append("/" + projectList[i]);
			}
			
			projectFullNameWithoutJobSeparator = stringBuffer.toString();
			
		} else {
			projectFullNameWithoutJobSeparator = projectFullName;
		}
		
		return projectFullNameWithoutJobSeparator;
	}

	public String getProjectFullName() {
		return projectFullName;
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

	public boolean process(String requestType) {
		boolean success = false;
		String projectName = getProjectNameWithoutJobSeparator();

		try {

			Item item = Jenkins.get().getItemByFullName(projectName);

			if (item != null) {
				success = execute(item);
			} else {
				if (requestType.equals("deleteJob") || requestType.equals("renameJob")) {
					errorMessage = "The job " + projectName + " doesn't exist";
				}
				if (requestType.equals("deleteFolder") || requestType.equals("renameFolder")) {
					errorMessage = "The folder " + projectName + " doesn't exist";
				} else {
					errorMessage = "The build for " + projectName + " doesn't exist";
				}
			}

		} catch (NullPointerException e) {
			errorMessage = e.getMessage();

			return false;
		}

		return success;
	}

	public abstract boolean execute(Item item);

}
