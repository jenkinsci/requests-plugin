/*
 * The MIT License
 *
 * Copyright 2020 Lexmark
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

import hudson.model.AbstractItem;
import hudson.model.Hudson;
import hudson.model.Item;
import hudson.model.Job;
import jenkins.model.Jenkins;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

// Represents a folder deletion request sent by a user to the administrator.

public class DeleteFolderRequest extends Request {

	private static final Logger LOGGER = Logger.getLogger(DeleteFolderRequest.class.getName());
	
	public DeleteFolderRequest(String requestType, String username, String project, String projectFullName, String buildNumber) {
		super(requestType, username, project, projectFullName, buildNumber);
	}

	@Override
	public String getMessage() {
		return Messages.DeleteFolderRequest_message(project);
	}

	@Override
	public boolean execute(Item item) {
		boolean success = false;
		
		try {
			if (Jenkins.getInstance().hasPermission(Item.DELETE)) {
				try {
					item.delete();
					success = true;
					errorMessage = "The Folder " + item.getFullName()
							+ " has been properly Deleted";
					LOGGER.log(Level.INFO,
							"The folder {0} has been properly deleted",
							item.getFullName());
				} catch (IOException e) {
					errorMessage = e.getMessage();
					LOGGER.log(Level.SEVERE,
							"Unable to delete the folder " + item.getFullName(),
							e);
				} catch (InterruptedException e) {
					errorMessage = e.getMessage();
					LOGGER.log(Level.SEVERE,
							"Unable to delete the folder " + item.getFullName(),
							e);
				}
			} else {
				errorMessage = "The current user " + username
						+ " does not have permission to delete the folder";
				LOGGER.log(Level.FINE,
						"The current user {0} does not have permission to DELETE the folder",
						new Object[] { username });
			}

		} catch (NullPointerException e) {
			errorMessage = e.getMessage();
			LOGGER.log(Level.SEVERE, "Unable to Delete the folder "
					+ projectFullName + ":" + buildNumber, e.getMessage());

			return false;
		}

		return success;
	}

}
