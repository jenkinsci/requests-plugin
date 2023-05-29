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
import com.michelin.cio.jenkins.plugin.requests.model.DeleteBuildRequest;

import hudson.Functions;
import hudson.model.Action;
import hudson.model.Run;
import jenkins.model.Jenkins;

// Represents the "Request for build deletion" action appearing on a given build's page.
// @author John Flynn <john.trixmot.flynn1@gmail.com>

public class RequestDeleteBuildAction implements Action {
	public static final Logger LOGGER = Logger.getLogger(RequestDeleteBuildAction.class.getName());
	private String buildName;
	private int buildNumber;
	private String fullDisplayName;
	private String build_Url;

	public RequestDeleteBuildAction(Run<?, ?> target) {
		buildName = target.getDisplayName();
		buildNumber = target.getNumber();
		fullDisplayName = target.getFullDisplayName();
		build_Url = target.getUrl();
	}

	@POST
	public HttpResponse doCreateDeleteBuildRequest(StaplerRequest request, StaplerResponse response)
			throws IOException, ServletException, MessagingException {

		if (isIconDisplayed()) {
			final String username = request.getParameter("username");
			RequestsPlugin plugin = Jenkins.get().getPlugin(RequestsPlugin.class);

			if (plugin == null) {
				return null;
			}

			String projectFullName = "";
			String projectName = "";

			LOGGER.info("Delete Build Action: fullDisplayName: " + fullDisplayName);
			LOGGER.info("Delete Build Action: build_Url: " + build_Url);
			LOGGER.info("Delete Build Action: buildName: " + buildName);
			LOGGER.info("Delete Build Action: buildNumber: " + buildNumber);

			// Need to extract the folder name(s) and the job name:
			if (fullDisplayName.contains(" » ")) {
				// Split off the build number from the build_Url:
				// Then Split off the next item which is the project name:
				// Then split off the next item which should be job:
				// Then Split off the next item which is the Folder name and not display name:
				String[] New_String_Names = null;
				New_String_Names = build_Url.split("/");
				int stringFolderCount = New_String_Names.length;
				LOGGER.info("Delete Build Action: Folder build number: " + New_String_Names[stringFolderCount - 1]);
				LOGGER.info("Delete Build Action: Folder project name: " + New_String_Names[stringFolderCount - 2]);
				LOGGER.info("Delete Build Action: Folder folder name: " + New_String_Names[stringFolderCount - 4]);

				projectName = New_String_Names[stringFolderCount - 4] + " " + New_String_Names[stringFolderCount - 2];
				projectFullName = New_String_Names[stringFolderCount - 4] + "/job/" + New_String_Names[stringFolderCount - 2];
				LOGGER.info("Delete Build Action: Folder projectName: " + projectName);
				LOGGER.info("Delete Build Action: Folder projectFullName: " + projectFullName);

				// Need to extract the job name:
			} else {
				String[] projectNameList = fullDisplayName.split(buildName);
				projectName = projectNameList[0].trim();
				projectFullName = projectName;
				LOGGER.info("Delete Build Action: project name: " + projectName);
				LOGGER.info("Delete Build Action: projectFullName: " + projectFullName);
			}

			String jenkinsUrl = Jenkins.get().getRootUrl();
			String buildUrl = jenkinsUrl + build_Url;
			LOGGER.info("Delete Build Action: buildUrl: " + buildUrl);
			String[] emailData = { buildName, username, "A Delete Build", buildUrl };
			// LOGGER.info("[INFO] doCreateDeleteBuildRequest:");
			plugin.addRequestPlusEmail(new DeleteBuildRequest("deleteBuild", username, projectName, projectFullName, Integer.toString(buildNumber)),
					emailData);
		}

		return new HttpRedirect(request.getContextPath() + '/' + build_Url);
	}

	public String getDisplayName() {
		if (isIconDisplayed()) {
			return Messages.RequestDeleteBuildAction_DisplayName();
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

	// public Run<?, ?> getBuild() {
	// return build;
	// }

	public String getUrlName() {
		return "request-delete-build";
	}

	/**
	 * Displays the icon when the user can not Delete a build.
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

	private boolean hasDeletePermission() throws IOException, ServletException {
		return Functions.hasPermission(Run.DELETE);
	}

}
