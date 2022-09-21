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

// Represents a build deletion request sent by a user to Jenkins' administrator.
// @author John Flynn <john.trixmot.flynn@gmail.com>

public class DeleteBuildRequest extends Request {

	private static final Logger LOGGER = Logger.getLogger(DeleteBuildRequest.class.getName());

	public DeleteBuildRequest(String requestType, String username, String project, String projectFullName, String buildNumber) {
		super(requestType, username, project, projectFullName, buildNumber);
	}

	@Override
	public String getMessage() {
		return Messages.DeleteBuildRequest_message(buildNumber + " for " + project);
	}

	public boolean execute(Item item) {
		boolean success = false;
		String returnStatus;
		// The Admin user set in the global jenkins settings for the plugin:
		DescriptorEmailImpl descriptorEmailImpl = new DescriptorEmailImpl();
		final String adminUser = descriptorEmailImpl.getUnlockuser();

		try {
			Jenkins jenkins = Jenkins.get();
			LOGGER.info("[DEBUG] DeleteBuildRequest triggered - projectFullName: " + projectFullName);
			String jenkinsURL = Jenkins.get().getRootUrl();

			if (jenkinsURL == null) {
				LOGGER.log(Level.SEVERE, "Jenkins instance is null: ");

				return false;
			}

			RequestsUtility requestsUtility = new RequestsUtility();
			// projectFullName = requestsUtility.encodeValue(projectFullName);
			projectFullName = projectFullName.replace(" ", "%20");
			String urlString = jenkinsURL + "job/" + projectFullName + "/" + buildNumber + "/doDelete";
			LOGGER.info("[INFO] Delete Build urlString: " + urlString);

			try {
				returnStatus = requestsUtility.runPostMethod(jenkinsURL, urlString);

			} catch (Exception e) {
				errorMessage = e.getMessage().toString();
				LOGGER.log(Level.SEVERE, "runPostMethod Error: Unable to Delete the build " + projectFullName + " : " + buildNumber + " : "
						+ e.getMessage().toString());

				return false;
			}

			if (returnStatus.equals("success")) {
				errorMessage = "Build number " + buildNumber + " has been properly Deleted for " + projectFullName;
				LOGGER.log(Level.INFO, "Build {0} has been properly Deleted", projectFullName + ":" + buildNumber);
				success = true;

			} else if (returnStatus.contains("Forbidden")) {
				errorMessage = "The Admin User " + adminUser + " does not have permission to DELETE the Build";
				LOGGER.log(Level.SEVERE, "The Admin User {0} does not have permission to DELETE the Build", new Object[] { adminUser });
				success = false;

			} else if (returnStatus.contains("Bad Request")) {
				errorMessage = "The DELETE Build request has failed. The Build trying to be deleted may be locked.";
				LOGGER.log(Level.SEVERE,
						"The Delete Build request has failed. The Build trying to be deleted may be locked. " + projectFullName + ":" + buildNumber);
				success = false;

			} else {
				errorMessage = "DELETE Build request has failed for " + projectFullName + ":" + buildNumber + " : " + returnStatus;
				LOGGER.log(Level.SEVERE, "DELETE Build request has failed: ", projectFullName + ":" + buildNumber + " : " + returnStatus);
				success = false;
			}

		} catch (Exception e) {
			errorMessage = e.getMessage().toString();
			LOGGER.log(Level.SEVERE,
					"Jenkins.get Error: Unable to Delete the build " + projectFullName + " : " + buildNumber + " : " + e.getMessage().toString());

			success = false;
		}

		return success;
	}

}
