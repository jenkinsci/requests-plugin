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


// Represents a build deletion request sent by a user to Jenkins' administrator.
// @author John Flynn <john.trixmot.flynn@gmail.com>

public class DeleteBuildRequest extends Request {

	private static final Logger LOGGER = Logger.getLogger(DeleteBuildRequest.class.getName());
	
    public DeleteBuildRequest(String requestType, String username, String project, String projectFullName, String buildNumber) {
        super(requestType, username, project, projectFullName, buildNumber);
    }

    @Override
    public String getMessage() {
        return Messages.DeleteBuildRequest_message(buildNumber + " for " + projectFullName).toString();
    }

    public boolean execute(Item item) {
        boolean success = false;

        if (Jenkins.getInstance().hasPermission(Run.DELETE)) {            
            try {
            	String jenkinsURL = Jenkins.getInstance().getRootUrl();
            	String urlString = jenkinsURL + "job/" + projectFullName + "/" + buildNumber + "/doDelete";
            	RequestsUtility requestsUtility = new RequestsUtility();
            	success = requestsUtility.runPostMethod(jenkinsURL, urlString);

            } catch (IOException e) {
                errorMessage = e.getMessage();
                LOGGER.log(Level.SEVERE, "Unable to Delete the build " + projectFullName + ":" + buildNumber, e.getMessage());
            } 

            if (success) {
				errorMessage = "Build number " + buildNumber + " has been properly Deleted for " + projectFullName;
				LOGGER.log(Level.INFO, "Build {0} has been properly Deleted", projectFullName + ":" + buildNumber);
			} else {
				errorMessage = "Delete Build call has failed for " + projectFullName + ":" + buildNumber + " Make sure the build is not locked.";
				LOGGER.log(Level.INFO, "Delete Build call has failed: ", projectFullName + ":" + buildNumber + " Make sure the build is not locked.");
			}
            
        } else {
            errorMessage = "The current user " + username + " does not have permission to delete the build";            
            LOGGER.log(Level.FINE, "The current user {0} does not have permission to DELETE the build", new Object[]{username});LOGGER.log(Level.FINE, "The current user does not have the DELETE permission");
        }

        return success;
    }
    
}
