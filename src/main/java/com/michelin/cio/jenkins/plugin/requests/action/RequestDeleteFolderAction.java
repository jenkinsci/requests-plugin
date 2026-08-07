/*
 * The MIT License
 *
 * Copyright 2020 Lexmark
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.MessagingException;
import jakarta.servlet.ServletException;

import org.kohsuke.stapler.HttpRedirect;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest2;
import org.kohsuke.stapler.StaplerResponse2;
import org.kohsuke.stapler.verb.POST;

import com.cloudbees.hudson.plugins.folder.Folder;
import com.cloudbees.hudson.plugins.folder.FolderProperty;
import com.michelin.cio.jenkins.plugin.requests.RequestsPlugin;
import com.michelin.cio.jenkins.plugin.requests.model.DeleteFolderRequest;

import hudson.Extension;
import hudson.Functions;
import hudson.model.Action;
import hudson.model.Item;
import jenkins.model.Jenkins;
import jenkins.model.TransientActionFactory;

// Represents the "Ask for deletion" action appearing on a given folder view page.

public class RequestDeleteFolderAction extends FolderProperty<Folder> implements Action {

	private Folder project;
	private Folder project2;

	public RequestDeleteFolderAction(Folder target) {
		project2 = (Folder) target.getTarget();
		this.project = project2;
	}

	@POST
	public HttpResponse doCreateDeleteFolderRequest(StaplerRequest2 request, StaplerResponse2 response) throws IOException, ServletException, MessagingException {

		try {
			if (isIconDisplayed()) {
				final String username = request.getParameter("username");
				RequestsPlugin plugin = Jenkins.get().getPlugin(RequestsPlugin.class);
				if (plugin == null) {
					return null;
				}
				String jobName = project.getFullName();
				String fullJobURL = "";
				String jobNameSlash = jobName.toString();
				;
				String jobNameJelly = "";
				String[] emailData = { project.getName(), username, "A Delete Folder", project.getAbsoluteUrl() };

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

				fullJobURL = project.getAbsoluteUrl();
				LOGGER.info("Delete Folder Request fullJobURL: " + fullJobURL);
				plugin.addRequestPlusEmail(new DeleteFolderRequest("deleteFolder", username, jobName, "NA", fullJobURL, jobNameSlash, jobNameJelly, "NA"), emailData);
			}

		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "[ERROR] Exception: " + e.getMessage());

			return null;
		}

		return new HttpRedirect(request.getContextPath() + '/' + project.getUrl());
	}

	public String getDisplayName() {
		if (isIconDisplayed()) {
			return Messages.RequestDeleteFolderAction_DisplayName();
		}
		return null;
	}

	@Override
	public String getIconFileName() {
		return null;
	}

	public String getIconClassName() {
		if (isIconDisplayed()) {
			return "icon-edit-delete";
		}
		return null;
	}

	public Folder getProject() {
		Folder project2a = (Folder) project2.getTarget();
		return project2a;
	}

	public String getUrlName() {
		return "request-delete-folder";
	}

	/*
	 * Permission computing 1: The user has the permission 0: The user has not the permission
	 *
	 * Create | 1 | 0 | Delete | 0 | 1 | Configure | 0 | 0 |
	 *
	 * So, the action has to be enabled when: Create AND !Delete AND !Configure OR Delete AND !Create AND !Configure
	 */
	private boolean isIconDisplayed() {
		boolean isDisplayed = false;
		try {
			isDisplayed = !hasDeletePermission();
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, "Impossible to know if the icon has to be displayed", e);
		}

		return isDisplayed;
	}

	// private boolean hasConfigurePermission()
	// throws IOException, ServletException {
	// return Functions.hasPermission(project, Item.CONFIGURE);
	// }

	private boolean hasDeletePermission() throws IOException, ServletException {
		return Functions.hasPermission(project, Item.DELETE);
	}

	private static final Logger LOGGER = Logger.getLogger(RequestDeleteFolderAction.class.getName());

	// @Extension(optional = true)
	@Extension
	public static class TransientFolderActionFactoryImpl extends TransientActionFactory<Folder> {

		@Override
		public Collection<? extends Action> createFor(Folder target) {
			RequestMailSender.DescriptorEmailImpl descriptorEmailImpl = new RequestMailSender.DescriptorEmailImpl();
			List<Action> adminActions = new ArrayList<Action>();

			// Check if option is enabled in Global settings of the plugin:
			// Note: that a restart is required after making a change since this is
			// loaded at start up time:
			if (descriptorEmailImpl.isEnableDeleteFolder()) {
				adminActions.add(new RequestDeleteFolderAction(target));
			}

			return adminActions;
		}

		@Override
		public Class<Folder> type() {
			return Folder.class;
		}
	}

}
