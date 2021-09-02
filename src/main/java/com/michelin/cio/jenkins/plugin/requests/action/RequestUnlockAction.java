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

import hudson.Functions;
import hudson.model.Action;
import hudson.model.Run;
import jenkins.model.Jenkins;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.MessagingException;
import javax.servlet.ServletException;
import org.kohsuke.stapler.HttpRedirect;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.interceptor.RequirePOST;
import org.kohsuke.stapler.verb.POST;

import com.michelin.cio.jenkins.plugin.requests.RequestsPlugin;
import com.michelin.cio.jenkins.plugin.requests.model.RequestsUtility;
import com.michelin.cio.jenkins.plugin.requests.model.UnlockRequest;

import static java.util.logging.Level.FINE;

// @author John Flynn <john.trixmot.flynn@gmail.com>

public class RequestUnlockAction implements Action {
	public static final Logger LOGGER = Logger.getLogger(RequestUnlockAction.class.getName());
	private transient List<String> errors = new ArrayList<String>();
	private Run<?, ?> build;

	public RequestUnlockAction(Run<?, ?> target) {
		this.build = target;
	}

	public List<String> getErrors() {
		return errors;
	}

	public void setErrors(String errorString) {
		errors.clear();
		errors.add(errorString);
	}

	@POST
	public HttpResponse doCreateUnlockRequest(StaplerRequest request, StaplerResponse response) throws IOException, ServletException, MessagingException {

		try {
			if (isIconDisplayed()) {
				errors.clear();
				final String username = request.getParameter("username");
				RequestsPlugin plugin = Jenkins.get().getPlugin(RequestsPlugin.class);
				String buildName = build.getDisplayName();
				String projectFullName = null;
				String projectName = null;
				int buildNumber = build.getNumber();
				String fullDisplayName = build.getFullDisplayName();
				StringBuffer stringBuffer = new StringBuffer();

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
				String buildUrl = jenkinsUrl + build.getUrl();
				String[] emailData = { buildName, username, "An Unlock Build", buildUrl };

				plugin.addRequestPlusEmail(new UnlockRequest("unlockBuild", username, projectName, projectFullName, Integer.toString(buildNumber)), emailData);
			}

		} catch (NullPointerException e) {
			LOGGER.log(Level.SEVERE, "[ERROR] Exception: " + e.getMessage());

			return null;
		}

		return new HttpRedirect(request.getContextPath() + '/' + build.getUrl());
	}

	public String getDisplayName() {
		if (isIconDisplayed()) {
			return Messages.RequestUnlockAction_DisplayName();
		}
		return null;
	}

	public String getIconFileName() {
		if (isIconDisplayed()) {
			return "/images/24x24/lock.png";
		}
		return null;
	}

	public Run<?, ?> getBuild() {
		return build;
	}

	public String getUrlName() {
		return "request-unlock";
	}

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
