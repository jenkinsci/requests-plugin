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
import com.michelin.cio.jenkins.plugin.requests.model.UnlockRequest;

import hudson.Functions;
import hudson.model.Action;
import hudson.model.Run;
import jenkins.model.Jenkins;

//Represents the "Request for build unlock" action appearing on a given build's page.
// @author John Flynn <john.trixmot.flynn@gmail.com>

public class RequestUnlockAction implements Action {
	public static final Logger LOGGER = Logger.getLogger(RequestUnlockAction.class.getName());
	private String buildName;
	private int buildNumber;
	private String fullDisplayName;
	private String build_Url;

	public RequestUnlockAction(Run<?, ?> target) {
		buildName = target.getDisplayName();
		buildNumber = target.getNumber();
		fullDisplayName = target.getFullDisplayName();
		build_Url = target.getUrl();
	}

	@POST
	public HttpResponse doCreateUnlockRequest(StaplerRequest request, StaplerResponse response)
			throws IOException, ServletException, MessagingException {

		if (isIconDisplayed()) {
			final String username = request.getParameter("username");
			RequestsPlugin plugin = Jenkins.get().getPlugin(RequestsPlugin.class);

			if (plugin == null) {
				return null;
			}

			String projectFullName = null;
			String projectName = null;
			StringBuffer stringBuffer = new StringBuffer();

			LOGGER.info("Unlock Build Action: fullDisplayName " + fullDisplayName);

			// Need to extract the folder name(s) and the job name:
			if (fullDisplayName.contains(" » ")) {
				String[] Folder_project_BuildList = null;
				Folder_project_BuildList = fullDisplayName.split(" » ");
				int folderCount = Folder_project_BuildList.length;

				// Cat together folder names with /job/ except for the last value:
				for (int i = 0; i < folderCount - 1; i++) {
					stringBuffer.append(Folder_project_BuildList[i] + "/job/");
				}

				projectName = Folder_project_BuildList[folderCount - 1].split(" #")[0];
				// Cat in the job name:
				stringBuffer.append(projectName);
				projectFullName = stringBuffer.toString();

				// Need to extract the job name:
			} else {
				if (fullDisplayName.contains(" #")) {
					projectFullName = fullDisplayName.split(" #")[0];
				} else if (fullDisplayName.contains(" ")) {
					projectFullName = fullDisplayName.split(" ")[0];
				}

				projectName = projectFullName;
			}

			LOGGER.info("Unlock Build Request: " + projectName + " - " + projectFullName);

			String jenkinsUrl = Jenkins.get().getRootUrl();
			String buildUrl = jenkinsUrl + build_Url;
			String[] emailData = { buildName, username, "An Unlock Build", buildUrl };
			plugin.addRequestPlusEmail(new UnlockRequest("unlockBuild", username, projectName, projectFullName, Integer.toString(buildNumber)),
					emailData);
		}

		return new HttpRedirect(request.getContextPath() + '/' + build_Url);
	}

	public String getDisplayName() {
		if (isIconDisplayed()) {
			return Messages.RequestUnlockAction_DisplayName();
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

	public String getUrlName() {
		return "request-unlock";
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
