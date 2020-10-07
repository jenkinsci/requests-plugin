/*
 * The MIT License
 *
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
import hudson.model.Run;
import jenkins.model.Jenkins;
import hudson.model.Action;
import org.kohsuke.stapler.HttpRedirect;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.interceptor.RequirePOST;
import org.kohsuke.stapler.verb.POST;

import com.michelin.cio.jenkins.plugin.requests.RequestsPlugin;
import com.michelin.cio.jenkins.plugin.requests.model.DeleteBuildRequest;
import com.michelin.cio.jenkins.plugin.requests.model.RequestsUtility;

import javax.mail.MessagingException;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.logging.Logger;

import static java.util.logging.Level.FINE;

// Represents the "Request for build deletion" action appearing on a given build's page.
// @author John Flynn <john.trixmot.flynn1@gmail.com>

public class RequestDeleteBuildAction implements Action {

	private Run<?, ?> build;
	private transient List<String> errors = new ArrayList<String>();
	private static final Logger LOGGER = Logger
			.getLogger(RequestDeleteBuildAction.class.getName());

	public RequestDeleteBuildAction(Run<?, ?> target) {
		this.build = target;
	}

	public List<String> getErrors() {
		return errors;
	}

	public void setErrors(String errorString) {
		errors.clear();
		errors.add(errorString);
	}

	@RequirePOST
	public HttpResponse doCreateDeleteBuildRequest(StaplerRequest request,
			StaplerResponse response)
			throws IOException, ServletException, MessagingException {
		try {
			if (isIconDisplayed()) {
				LOGGER.log(FINE, "Delete Build request");
				errors.clear();
				final String username = request.getParameter("username");
				RequestsPlugin plugin = Jenkins.get().getPlugin(RequestsPlugin.class);
				String[] nameList = null;
				String buildName = build.getDisplayName();
				String projectFullName;
				String projectName = "";
				int buildNumber = build.getNumber();
				String fullDisplayName = build.getFullDisplayName();
				
				// Need to extract the job name:
				if (fullDisplayName.contains(" » ")) {
					RequestsUtility requestsUtility = new RequestsUtility();
					nameList = requestsUtility.constructFolderJobNameAndFull(fullDisplayName);
					projectName = nameList[0];
					projectFullName = nameList[1];

				} else {
					projectFullName = fullDisplayName.split(" #")[0];
					projectName = projectFullName;
				}

				//LOGGER.info("[INFO] Delete Build projectName: " + projectName);
				//LOGGER.info("[INFO] Delete Build projectFullName: " + projectFullName);

				String jenkinsUrl = Jenkins.get().getRootUrl();
				String buildUrl = jenkinsUrl + build.getUrl();
				String[] emailData = {buildName, username, "A Delete Build", buildUrl};
			
				plugin.addRequestPlusEmail(new DeleteBuildRequest("deleteBuild", username, projectName, projectFullName, Integer.toString(buildNumber)), emailData);
			}
		} catch (NullPointerException e) {

			LOGGER.log(Level.SEVERE, "Exception: " + e.getMessage());

			return null;
		}

		return new HttpRedirect(
				request.getContextPath() + '/' + build.getUrl());
	}

	public String getDisplayName() {
		if (isIconDisplayed()) {
			return Messages.RequestDeleteBuildAction_DisplayName();
		}
		return null;
	}

	public String getIconFileName() {
		if (isIconDisplayed()) {
			return "/images/24x24/edit-delete.png";
		}
		return null;
	}

	public Run<?, ?> getBuild() {
		return build;
	}

	public String getUrlName() {
		return "request-delete-build";
	}

	/**
	 * Displays the icon when the user can configure and !delete.
	 */
	private boolean isIconDisplayed() {
		boolean isDisplayed = false;
		try {
			isDisplayed = !hasDeletePermission();
		} catch (Exception e) {
			LOGGER.log(Level.WARNING,
					"Impossible to know if the icon has to be displayed", e);
		}

		return isDisplayed;
	}

	private boolean hasDeletePermission() throws IOException, ServletException {
		return Functions.hasPermission(Run.DELETE);
	}

}
