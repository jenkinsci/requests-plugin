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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;



 // @author Daniel Petisme <daniel.petisme@gmail.com> <http://danielpetisme.blogspot.com/>

public abstract class Request {
	
	//private static final Logger LOGGER = Logger.getLogger(Request.class.getName());

    protected static final SimpleDateFormat DATE_FORMATER = new SimpleDateFormat(Messages.Request_dateFormat());
    protected String requestType;
    protected String username;
    protected String project;
    protected String projectFullName;
    protected String buildNumber;
    protected String errorMessage;
    private String creationDate;


    public Request(String requestType, String username, String project, String projectFullName, String buildNumber) {
        this.requestType = requestType;
    	this.username = username;
        this.project = project;
        this.projectFullName = projectFullName;
        this.buildNumber = buildNumber;
        this.creationDate = DATE_FORMATER.format(new Date());
    }

    public String getProject() {       
        return project;
    }
    
    public String getProjectFullName() {
    	String[] projectList = null;
        if (!projectFullName.contains("/job/") && projectFullName.contains("/")) {
        	projectList = projectFullName.split("/");
        	projectFullName = projectList[0] + "/job/" + projectList[1];
        } 

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
        String[] projectNameList = null;
        String searchName = projectFullName;
        
        // Check if a folder job type:
        if (searchName.contains("/job/")) {
        	projectNameList = searchName.split("/job/");
        	searchName = projectNameList[0] + "/" + projectNameList[1];
        }
        
        Item item = Jenkins.getInstance().getItemByFullName(searchName);

        if (item != null) {
            success = execute(item);
        } else {
        	if (requestType.equals("deleteJob")) {
        		errorMessage = "The job " + projectFullName + " doesn't exist";
        	} else {
        		errorMessage = "The build for " + projectFullName + " doesn't exist";
        	}            
        }

        return success;
    }

    public abstract boolean execute(Item item);

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Request other = (Request) obj;
        if ((this.username == null) ? (other.username != null) : !this.username.equals(other.username)) {
            return false;
        }
        if ((this.projectFullName == null) ? (other.projectFullName != null) : !this.projectFullName.equals(other.projectFullName)) {
            return false;
        }
        return true;
    }
}
