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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.MessagingException;
import jakarta.servlet.ServletException;

import org.kohsuke.stapler.HttpRedirect;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest2;
import org.kohsuke.stapler.StaplerResponse2;
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
	private String shortBuildUrl;

	public RequestUnlockAction(Run<?, ?> target) {
		buildName = target.getDisplayName();
		buildNumber = target.getNumber();
		shortBuildUrl = target.getUrl();
	}

	@POST
	public HttpResponse doCreateUnlockRequest(StaplerRequest2 request, StaplerResponse2 response) throws IOException, ServletException, MessagingException {

		if (isIconDisplayed()) {
			final String username = request.getParameter("username");
			RequestsPlugin plugin = Jenkins.get().getPlugin(RequestsPlugin.class);

			if (plugin == null) {
				return null;
			}

			String projectFullName = null;
			String jobNameSpace = null;
			String fullJobURL = "";
			String jobNameSlash = "";
			String jobNameJelly = "";

			// LOGGER.info("Unlock Build Action: shortBuildUrl: " + shortBuildUrl + "buildName: " + buildName + "buildNumber: " +
			// buildNumber);

			// NOTE: Folders and Multibranch pipelines can set a Display name. The real names can only be obtained from the build_Url:

			StringBuilder stringBuilder1 = new StringBuilder();
			StringBuilder stringBuilder3 = new StringBuilder();
			String[] nameArray = shortBuildUrl.split("/");
			ArrayList<String> nameList = new ArrayList<>();
			nameList.addAll(Arrays.asList(nameArray));

			// If build_Url starts with view, then remove first 2 elements (view and view_name):
			// If build_Url starts with job, then remove first element:
			// Ex: build_Url = "view/JOHN/job/FolderTest22/job/TestFolderJob1/5/";
			if (nameList.get(0).equalsIgnoreCase("view")) {
				nameList.remove(0);
				nameList.remove(0);
			}

			if (nameList.get(0).equalsIgnoreCase("job")) {
				nameList.remove(0);
			}

			int nameCount = nameList.size();

			// Cat together folder names without "job":
			for (int i = 0; i < nameCount - 1; i++) {
				if (!nameList.get(i).equalsIgnoreCase("job")) {
					if (i == (nameCount - 2)) {
						stringBuilder1.append(nameList.get(i));
						stringBuilder3.append(nameList.get(i));
					} else {
						stringBuilder1.append(nameList.get(i) + " ");
						stringBuilder3.append(nameList.get(i) + "/");
					}
				}
			}

			jobNameSpace = stringBuilder1.toString();
			jobNameSlash = stringBuilder3.toString();
			jobNameJelly = jobNameSlash;

			if (jobNameJelly.contains("%20")) {
				jobNameJelly = jobNameJelly.replace("%20", " ");
			}

			String jenkinsUrl = Jenkins.get().getRootUrl();
			fullJobURL = jenkinsUrl + shortBuildUrl;

			LOGGER.info("Unlock Build Action: jobNameSpace - fullJobURL: " + jobNameSpace + " - " + fullJobURL);

			String[] emailData = { buildName, username, "An Unlock Build", fullJobURL };
			plugin.addRequestPlusEmail(new UnlockRequest("unlockBuild", username, jobNameSpace, Integer.toString(buildNumber), fullJobURL, jobNameSlash, jobNameJelly, "NA"),
					emailData);
		}

		return new HttpRedirect(request.getContextPath() + '/' + shortBuildUrl);
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
