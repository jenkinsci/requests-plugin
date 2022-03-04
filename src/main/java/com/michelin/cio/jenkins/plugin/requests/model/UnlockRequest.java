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

import hudson.model.Item;
import hudson.model.Run;
import jenkins.model.Jenkins;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

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

		try {
			jenkins = Jenkins.get();
			if (jenkins == null)
				throw new NullPointerException("Jenkins instance is null");

			if (Jenkins.get() != null && Jenkins.get().hasPermission(Run.DELETE)) {
				try {
					String jenkinsURL = null;

					try {
						jenkinsURL = Jenkins.get().getRootUrl();
						if (jenkinsURL == null)
							throw new NullPointerException("Jenkins instance is null");
					} catch (Exception npe) {
						LOGGER.log(Level.SEVERE, "[ERROR] Exception: " + npe.getMessage().toString());
						return false;
					}

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
						errorMessage = "The current user " + username + " does not have permission to Unlock the Build";
						LOGGER.log(Level.SEVERE, "The current user {0} does not have permission to Unlock the Build", new Object[] { username });
						success = false;
						
					} else {
						errorMessage = "UNLOCK Build request has failed for " + projectFullName + ":" + buildNumber + " : " + returnStatus.toString();
						LOGGER.log(Level.INFO, "UNLOCK Build call has failed: ", projectFullName + ":" + buildNumber + " : " + returnStatus.toString());

						success = false;
					}

				} catch (Exception e) {
					errorMessage = "Unable to UNLOCK the Build: " + e.getMessage();
					LOGGER.log(Level.SEVERE, "Unable to UNLOCK the Build " + projectFullName + ":" + buildNumber, e.getMessage().toString());
				}

			} else {
				errorMessage = "The current user " + username + " does not have permission to UNLOCK the Build";
				LOGGER.log(Level.FINE, "The current user {0} does not have permission to UNLOCK the Build", new Object[] { username });
			}

		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "[ERROR] Exception: " + e.getMessage().toString());
			return false;
		}

		return success;
	}

}
