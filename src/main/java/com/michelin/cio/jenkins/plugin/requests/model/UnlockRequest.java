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
		return Messages.UnlockRequest_message(buildNumber + " for " + projectFullName);
	}

	public boolean execute(Item item) {
		Jenkins jenkins = null;
		boolean success = false;
		String returnStatus = null;

		try {
			jenkins = Jenkins.getInstance();
			if (jenkins == null) throw new NullPointerException("Jenkins instance is null");

			if (Jenkins.getInstance() != null && Jenkins.getInstance().hasPermission(Run.DELETE)) {            
				try {
					String jenkinsURL = null;

					try {
						jenkinsURL = Jenkins.getInstance().getRootUrl();
						if(jenkinsURL == null) throw new NullPointerException("Jenkins instance is null");
					} catch (NullPointerException npe) {
						LOGGER.log(Level.SEVERE, "[ERROR] Exception: " + npe);
						return false;
					}

					String urlString = jenkinsURL + "job/" + projectFullName + "/" + buildNumber + "/toggleLogKeep";
					RequestsUtility requestsUtility = new RequestsUtility();

					try {
						returnStatus = requestsUtility.runPostMethod(jenkinsURL, urlString);
						
						if (returnStatus.equals("success")) {
							errorMessage = "Build number " + buildNumber + " has been properly Unlocked for " + projectFullName;
							LOGGER.log(Level.INFO, "Build {0} has been properly Unlocked", projectFullName + ":" + buildNumber);
							success = true;
						} else {
							errorMessage = "Unlock Build call has failed for " + projectFullName + ":" + buildNumber + " : " + returnStatus;
							LOGGER.log(Level.INFO, "Unlock Build call has failed: ", projectFullName + ":" + buildNumber + " : " + returnStatus);
							
							return false;
						}
						
					} catch (IOException e) {
						errorMessage = e.getMessage();
						LOGGER.log(Level.SEVERE, "Unable to Unlock the build " + projectFullName + ":" + buildNumber, e.getMessage());

						return false;
					}

				} catch (Exception e) {
					errorMessage = e.getMessage();
					LOGGER.log(Level.SEVERE, "Unable to Unlock the build " + projectFullName + ":" + buildNumber, e.getMessage());
				}

			} else {
				errorMessage = "The current user " + username + " does not have permission to Unlock the job";            
				LOGGER.log(Level.FINE, "The current user {0} does not have permission to UNLOCK the job", new Object[]{username});
			}

		} catch (NullPointerException npe) {
			LOGGER.log(Level.SEVERE, "[ERROR] Exception: " + npe);
			return false;
		}

		return success;
	}

}
