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
package com.michelin.cio.jenkins.plugin.requests.model;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.michelin.cio.jenkins.plugin.requests.action.RequestMailSender.DescriptorEmailImpl;

import hudson.model.Item;
import jenkins.model.Jenkins;

// @author John Flynn <john.trixmot.flynn@gmail.com>

public class UnlockRequest extends Request {

	private static final Logger LOGGER = Logger.getLogger(UnlockRequest.class.getName());

	public UnlockRequest(String requestType, String username, String project, String projectFullName, String buildNumber) {
		super(requestType, username, project, projectFullName, buildNumber);
	}

	@Override
	public String getMessage() {
		return Messages.UnlockRequest_message(buildNumber + " for " + project);
	}

	public boolean execute(Item item) {
		Jenkins jenkins = null;
		boolean success = false;
		String returnStatus = null;
		// Use the Admin user set in the global jenkins settings for the plugin:
		DescriptorEmailImpl descriptorEmailImpl = new DescriptorEmailImpl();
		final String adminUser = descriptorEmailImpl.getUnlockuser();

		try {
			jenkins = Jenkins.get();
			if (jenkins == null)
				throw new NullPointerException("Jenkins get() is null");

			String jenkinsURL = null;

			jenkinsURL = Jenkins.get().getRootUrl();
			if (jenkinsURL == null)
				throw new NullPointerException("Jenkins getRootUrl() is null");

			RequestsUtility requestsUtility = new RequestsUtility();
			// projectFullName = requestsUtility.encodeValue(projectFullName);
			projectFullName = projectFullName.replace(" ", "%20");
			String urlString = jenkinsURL + "job/" + projectFullName + "/" + buildNumber + "/toggleLogKeep";

			try {
				returnStatus = requestsUtility.runPostMethod(jenkinsURL, urlString);

			} catch (Exception e) {
				errorMessage = "Unable to Unlock the build: " + e.getMessage();
				LOGGER.log(Level.SEVERE, "Unable to Unlock the build " + projectFullName + ":" + buildNumber, e.getMessage().toString());

				return false;
			}

			if (returnStatus.equals("success")) {
				errorMessage = "Build number " + buildNumber + " has been properly Unlocked for " + projectFullName;
				LOGGER.log(Level.INFO, "Build {0} has been properly Unlocked", projectFullName + ":" + buildNumber);
				success = true;

			} else if (returnStatus.contains("Forbidden")) {
				errorMessage = "The Admin User " + username + " does not have permission to Unlock the Build";
				LOGGER.log(Level.SEVERE, "The Admin User {0} does not have permission to Unlock the Build", new Object[] { adminUser });
				success = false;

			} else {
				errorMessage = "UNLOCK Build request has failed for " + projectFullName + ":" + buildNumber + " : " + returnStatus.toString();
				LOGGER.log(Level.INFO, "UNLOCK Build call has failed: ", projectFullName + ":" + buildNumber + " : " + returnStatus.toString());

				success = false;
			}

		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "[ERROR] Exception executing Unlock Request: " + projectFullName + ":" + buildNumber, e.getMessage().toString());
			return false;
		}

		return success;
	}
}
