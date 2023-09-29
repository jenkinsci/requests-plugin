/*
 * The MIT License
 *
 * Copyright (c) 2011-2012, Manufacture Francaise des Pneumatiques Michelin, Daniel Petisme, Romain Seguy
 * Portions Copyright 2019 Lexmark
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

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.MessagingException;
import javax.servlet.ServletException;

import org.kohsuke.stapler.HttpRedirect;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.verb.POST;

import com.michelin.cio.jenkins.plugin.requests.RequestsPlugin;
import com.michelin.cio.jenkins.plugin.requests.model.RenameJobRequest;

import hudson.Functions;
import hudson.model.Action;
import hudson.model.Item;
import hudson.model.Job;
import jenkins.model.Jenkins;

// Represents the "Ask for renaming" action appearing on a given project's page.
//
// @author Daniel Petisme <daniel.petisme@gmail.com> <http://danielpetisme.blogspot.com/>
//
public class RequestRenameJobAction implements Action {

	private Job<?, ?> project;
	private Job<?, ?> project2;

	public RequestRenameJobAction(Job<?, ?> target) {
		project2 = (Job<?, ?>) target.getTarget();
		this.project = project2;
	}

	@POST
	public HttpResponse doCreateRenameJobRequest(StaplerRequest request, StaplerResponse response) throws IOException, ServletException, MessagingException {

		try {
			if (isIconDisplayed()) {
				final String newName = request.getParameter("new-name");
				final String username = request.getParameter("username");
				String rename = "";

				RequestsPlugin plugin = Jenkins.get().getPlugin(RequestsPlugin.class);
				if (plugin == null) {
					return null;
				}
				String jobName = project.getFullName();
				String fullJobURL = "";
				String jobNameSlash = jobName.toString();
				;
				String jobNameJelly = "";

				if (jobName.contains("/")) {
					String[] projectnameList = jobName.split("/");
					int nameCount = projectnameList.length;
					jobName = projectnameList[nameCount - 1];
				}

				jobNameJelly = jobNameSlash.toString();
				;
				if (jobNameJelly.contains("%20")) {
					jobNameJelly = jobNameJelly.replace("%20", " ");
				}

				// Get path to current job if multiple"
				if (jobNameSlash.contains("/")) {
					String[] nameArray = jobNameSlash.split("/");
					int nameCount = nameArray.length;
					StringBuilder stringBuilder1 = new StringBuilder();
					for (int i = 0; i < nameCount - 1; i++) {
						stringBuilder1.append(nameArray[i] + "/");
					}
					stringBuilder1.append(newName);
					rename = stringBuilder1.toString();
				} else {
					rename = newName.toString();
					;
				}

				String[] emailData = { jobName + " -> " + newName, username, "A Rename Job", project.getAbsoluteUrl() };
				fullJobURL = project.getAbsoluteUrl();

				plugin.addRequestPlusEmail(new RenameJobRequest("renameJob", username, jobName, newName, fullJobURL, jobNameSlash, jobNameJelly, rename), emailData);
				LOGGER.log(Level.INFO, "The request to rename the job {0} to {1} has been sent to the administrator", new Object[] { project.getName(), newName });
			}
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "[ERROR] Exception: " + e.getMessage());

			return null;
		}

		return new HttpRedirect(request.getContextPath() + '/' + project.getUrl());
	}

	public String getDisplayName() {
		if (isIconDisplayed()) {
			return Messages.RequestRenameJobAction_DisplayName();
		}
		return null;
	}

	@Override
	public String getIconFileName() {
		return null;
	}

	public String getIconClassName() {
		if (isIconDisplayed()) {
			return "icon-setting";
		}
		return null;
	}

	public Job<?, ?> getProject() {
		Job<?, ?> project2a;
		project2a = (Job<?, ?>) project2.getTarget();
		return project2a;
	}

	public String getUrlName() {
		return "request-rename-job";
	}

	/*
	 * Permission computing 1: The user has the permission ---- 0: The user has not the permission
	 *
	 * Create | 1 | 0 | Delete | 0 | 1 | Configure | 0 | 0 |
	 *
	 * So, the action has to be enabled when: Create AND !Delete AND !Configure OR Delete AND !Create AND !Configure
	 */
	private boolean isIconDisplayed() {
		boolean isDisplayed = false;
		try {
			isDisplayed = (!hasDeletePermission() && !hasConfigurePermission() && hasCreatePermission())
					|| (!hasCreatePermission() && !hasConfigurePermission() && hasDeletePermission())
					|| (!hasDeletePermission() && !hasCreatePermission() && !hasConfigurePermission());

		} catch (IOException | ServletException e) {
			LOGGER.log(Level.WARNING, "Impossible to know if the icon has to be displayed", e);
		}

		return isDisplayed;
	}

	private boolean hasConfigurePermission() throws IOException, ServletException {
		return Functions.hasPermission(project, Item.CONFIGURE);
	}

	private boolean hasCreatePermission() throws IOException, ServletException {
		return Functions.hasPermission(project, Item.CREATE);
	}

	private boolean hasDeletePermission() throws IOException, ServletException {
		return Functions.hasPermission(project, Item.DELETE);
	}

	private static final Logger LOGGER = Logger.getLogger(RequestRenameJobAction.class.getName());

}
