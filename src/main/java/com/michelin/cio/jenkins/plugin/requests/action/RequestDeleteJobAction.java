/*
 * The MIT License
 *
 * Copyright (c) 2011-2012, Manufacture Francaise des Pneumatiques Michelin, Daniel Petisme, Romain Seguy
 * Copyright 2019 Lexmark
 *
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
package com.michelin.cio.jenkins.plugin.requests.action;

import hudson.Functions;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import hudson.model.Item;
import hudson.model.Job;
import jenkins.model.Jenkins;

import hudson.model.Action;
import org.kohsuke.stapler.HttpRedirect;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.interceptor.RequirePOST;
import org.kohsuke.stapler.verb.POST;

import com.michelin.cio.jenkins.plugin.requests.RequestsPlugin;
import com.michelin.cio.jenkins.plugin.requests.model.DeleteJobRequest;
import com.michelin.cio.jenkins.plugin.requests.model.RequestsUtility;

import javax.mail.MessagingException;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.logging.Logger;

import static java.util.logging.Level.FINE;

// Represents the "Request for deletion" action appearing on a given job's page.
// @author Daniel Petisme <daniel.petisme@gmail.com> <http://danielpetisme.blogspot.com/>

public class RequestDeleteJobAction implements Action {

	private Job<?, ?> project;
	private transient List<String> errors = new ArrayList<String>();
	private static final Logger LOGGER = Logger
			.getLogger(RequestDeleteJobAction.class.getName());

	public RequestDeleteJobAction(Job<?, ?> target) {
		this.project = target;
	}

	public List<String> getErrors() {
		return errors;
	}

	public void setErrors(String errorString) {
		errors.clear();
		errors.add(errorString);
	}

	@POST
	public HttpResponse doCreateDeleteJobRequest(StaplerRequest request,
			StaplerResponse response)
			throws IOException, ServletException, MessagingException {

		try {
			if (isIconDisplayed()) {
				errors.clear();
				final String username = request.getParameter("username");
				RequestsPlugin plugin = Jenkins.get().getPlugin(RequestsPlugin.class);
				String projectName = project.getFullName();
				String projectFullName = project.getFullName();

				// Check if a folder job type and if multiple layers of folders:
				if (!projectFullName.contains("/job/") && projectFullName.contains("/")) {
					RequestsUtility requestsUtility = new RequestsUtility();
					projectFullName = requestsUtility.constructFolderJobName(projectFullName);
				}
				
				String[] emailData = {project.getName(), username, "A Delete Job", project.getAbsoluteUrl()};

				if (projectName.contains("/")) {
					String [] projectnameList = projectName.split("/");
					int nameCount = projectnameList.length;
					projectName = projectnameList[nameCount-1];
				}
				LOGGER.info("Delete Job Request: " + projectName + " - " + projectFullName);
				plugin.addRequestPlusEmail(new DeleteJobRequest("deleteJob", username, projectName, projectFullName, ""), emailData);
			}

		} catch (NullPointerException e) {
			LOGGER.log(Level.SEVERE, "[ERROR] Exception: " + e.getMessage());

			return null;
		}

		return new HttpRedirect(
				request.getContextPath() + '/' + project.getUrl());
	}

	public String getDisplayName() {
		if (isIconDisplayed()) {
			return Messages.RequestDeleteJobAction_DisplayName();
		}
		return null;
	}

	public String getIconFileName() {
		if (isIconDisplayed()) {
			return "/images/24x24/edit-delete.png";
		}
		return null;
	}

	public Job<?, ?> getProject() {
		return project;
	}

	public String getUrlName() {
		return "request-delete-job";
	}

	/**
	 * Displays the icon when the user can configure and !delete.
	 */
	private boolean isIconDisplayed() {
		boolean isDisplayed = false;
		try {
			isDisplayed = hasConfigurePermission() && !hasDeletePermission();
		} catch (Exception e) {
			LOGGER.log(Level.WARNING,
					"Impossible to know if the icon has to be displayed", e);
		}

		return isDisplayed;
	}

	private boolean hasConfigurePermission()
			throws IOException, ServletException {
		return Functions.hasPermission(project, Item.CONFIGURE);
	}

	private boolean hasDeletePermission() throws IOException, ServletException {
		return Functions.hasPermission(project, Item.DELETE);
	}

}
