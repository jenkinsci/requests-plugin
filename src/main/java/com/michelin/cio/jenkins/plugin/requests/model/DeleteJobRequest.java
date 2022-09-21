/*
 * The MIT License
 *
 * Copyright (c) 2011-2012, Manufacture Francaise des Pneumatiques Michelin, Daniel Petisme
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

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.michelin.cio.jenkins.plugin.requests.action.RequestMailSender.DescriptorEmailImpl;

import hudson.model.Item;
import jenkins.model.Jenkins;

// Represents a deletion request sent by a user to Jenkins' administrator.
// @author Daniel Petisme <daniel.petisme@gmail.com> <http://danielpetisme.blogspot.com/>

public class DeleteJobRequest extends Request {

	private static final Logger LOGGER = Logger.getLogger(DeleteJobRequest.class.getName());

	public DeleteJobRequest(String requestType, String username, String project, String projectFullName, String buildNumber) {
		super(requestType, username, project, projectFullName, buildNumber);
	}

	@Override
	public String getMessage() {
		return Messages.DeleteJobRequest_message(project);
	}

	/*
	 * public boolean execute_backup(Item item) { boolean success = false;
	 * 
	 * try { if (Jenkins.get().hasPermission(Item.DELETE)) { try { item.delete();
	 * success = true; errorMessage = "The Job " + item.getFullName() +
	 * " has been properly Deleted"; LOGGER.log(Level.INFO,
	 * "The job {0} has been properly deleted", item.getFullName()); } catch
	 * (Exception e) { errorMessage = e.getMessage().toString();
	 * LOGGER.log(Level.SEVERE, "Unable to delete the job " + item.getFullName(),
	 * e); } } else { errorMessage = "The current user " + username +
	 * " does not have permission to delete the job"; LOGGER.log(Level.FINE,
	 * "The current user {0} does not have permission to DELETE the job", new
	 * Object[] { username }); }
	 * 
	 * } catch (Exception e) { errorMessage = e.getMessage().toString();
	 * LOGGER.log(Level.SEVERE, "Unable to Delete the job " + projectFullName + ":"
	 * + buildNumber, e.getMessage().toString());
	 * 
	 * return false; }
	 * 
	 * return success; }
	 */

	public boolean execute(Item item) {
		boolean success = false;
		String returnStatus;
		StringBuffer stringBuffer = new StringBuffer();
		String[] projectList = null;
		// The Admin user set in the global jenkins settings for the plugin:
		DescriptorEmailImpl descriptorEmailImpl = new DescriptorEmailImpl();
		final String adminUser = descriptorEmailImpl.getUnlockuser();

		try {
			String jenkinsURL = Jenkins.get().getRootUrl();
			if (jenkinsURL == null) {
				LOGGER.log(Level.SEVERE, "Jenkins instance is null: ");

				return false;
			}

			if (!projectFullName.contains("/job/") && projectFullName.contains("/")) {
				projectList = projectFullName.split("/");

				// Need to add '/job/' in between all names:
				int nameCount = projectList.length;
				stringBuffer.append(projectList[0]);
				for (int i = 1; i < nameCount; i++) {
					stringBuffer.append("/job/");
					stringBuffer.append(projectList[i]);
				}
				projectFullName = stringBuffer.toString();
				// LOGGER.info("[INFO] FOLDER Found: " + projectFullName);
			}

			RequestsUtility requestsUtility = new RequestsUtility();
			// if (!projectFullName.contains("/job/")) {
			// projectFullName = requestsUtility.encodeValue(projectFullName);
			projectFullName = projectFullName.replace(" ", "%20");
			// }

			String urlString = jenkinsURL + "job/" + projectFullName + "/doDelete";
			LOGGER.info("[INFO] Delete Build urlString: " + urlString);

			try {
				returnStatus = requestsUtility.runPostMethod(jenkinsURL, urlString);

			} catch (IOException e) {
				errorMessage = e.getMessage();
				LOGGER.log(Level.SEVERE, "Unable to DELETE the Build " + projectFullName + ":" + buildNumber, e.getMessage().toString());

				return false;
			}

			if (returnStatus.equals("success")) {
				errorMessage = "Job : " + projectFullName + " has been properly Deleted";
				LOGGER.log(Level.INFO, "Job {0} has been properly Deleted", projectFullName);
				success = true;

			} else if (returnStatus.contains("Forbidden")) {
				errorMessage = "The Admin User " + adminUser + " does not have permission to DELETE the Job";
				LOGGER.log(Level.SEVERE, "The Admin User {0} does not have permission to DELETE the Job", new Object[] { adminUser });
				success = false;

			} else {
				errorMessage = "DELETE Job request has failed for " + projectFullName + " : " + returnStatus.toString();
				LOGGER.log(Level.INFO, "DELETE Job request has failed: ", projectFullName + " : " + returnStatus.toString());
				success = false;
			}

		} catch (Exception e) {
			errorMessage = e.getMessage().toString();
			LOGGER.log(Level.SEVERE, "Jenkins.get Error: Unable to Delete the Job " + projectFullName + " : " + e.getMessage().toString());

			success = false;
		}

		return success;
	}

}
