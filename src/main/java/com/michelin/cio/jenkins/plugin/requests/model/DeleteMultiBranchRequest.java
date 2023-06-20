/*
 * The MIT License
 *
 * Copyright 2022 Lexmark
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

import hudson.model.Item;
import jenkins.model.Jenkins;

// Represents a folder deletion request sent by a user to the administrator.

public class DeleteMultiBranchRequest extends Request {

	private static final Logger LOGGER = Logger.getLogger(DeleteMultiBranchRequest.class.getName());

	public DeleteMultiBranchRequest(String requestType, String username, String jobNameSpace, String buildNumber, String fullJobURL, String jobNameSlash, String jobNameJelly,
			String rename) {
		super(requestType, username, jobNameSpace, buildNumber, fullJobURL, jobNameSlash, jobNameJelly, rename);
	}

	@Override
	public String getMessage() {
		return Messages.DeleteMultiBranchRequest_message(jobNameJelly);
	}

	@Override
	public boolean execute(Item item) {
		boolean success = false;

		try {
			if (Jenkins.get().hasPermission(Item.DELETE)) {
				try {
					item.delete();
					success = true;
					errorMessage = "The MultiBranch Pipeline " + jobNameSlash + " has been properly Deleted";
					LOGGER.log(Level.INFO, "The MultiBranch Pipeline {0} has been properly deleted", jobNameSlash);

				} catch (Exception e) {
					errorMessage = e.getMessage().toString();
					LOGGER.log(Level.SEVERE, "Unable to DELETE the MultiBranch Pipeline " + jobNameSlash, e);
					success = false;
				}

			} else {
				errorMessage = "The current user does not have permission to DELETE the MultiBranch Pipeline";
				LOGGER.log(Level.FINE, "The current user does not have permission to DELETE the MultiBranch Pipeline");
				success = false;
			}

		} catch (Exception e) {
			errorMessage = e.getMessage().toString();
			LOGGER.log(Level.SEVERE, "Unable to DELETE the MultiBranch Pipeline " + jobNameSlash + ":" + buildNumber, e.getMessage().toString());

			success = false;
		}

		return success;
	}

}