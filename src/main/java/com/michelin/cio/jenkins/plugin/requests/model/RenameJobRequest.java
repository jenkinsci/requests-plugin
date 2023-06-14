/*
 * The MIT License
 *
 * Copyright (c) 2011-2012, Manufacture Francaise des Pneumatiques Michelin, Daniel Petisme
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
import hudson.model.Job;
import jenkins.model.Jenkins;

// Represents a renaming request sent by a user to the administrator.
//
// @author Daniel Petisme <daniel.petisme@gmail.com> <http://danielpetisme.blogspot.com/>
//
public class RenameJobRequest extends Request {

	private String newName;

	public RenameJobRequest(String requestType, String username, String jobNameSpace, String newName, String fullJobURL, String jobNameSlash, String jobNameJelly, String rename) {
		super(requestType, username, jobNameSpace, newName, fullJobURL, jobNameSlash, jobNameJelly, rename);
		this.newName = newName;
	}

	@Override
	public String getMessage() {
		return Messages.RenameJobRequest_message(jobNameJelly, rename);
	}

	public String getNewName() {
		return newName;
	}

	@Override
	public boolean execute(Item item) {
		boolean success = false;

		try {
			if ((Jenkins.get().hasPermission(Item.DELETE) && !Jenkins.get().hasPermission(Item.CREATE) && Jenkins.get().hasPermission(Item.CONFIGURE))
					|| (!Jenkins.get().hasPermission(Item.DELETE) && Jenkins.get().hasPermission(Item.CREATE) && Jenkins.get().hasPermission(Item.CONFIGURE))
					|| (Jenkins.get().hasPermission(Item.DELETE) && Jenkins.get().hasPermission(Item.CREATE) && Jenkins.get().hasPermission(Item.CONFIGURE))) {
				((Job) item).renameTo(newName);
				success = true;
				LOGGER.log(Level.INFO, "The Job has successfully been renamed from " + jobNameSlash + " to " + rename);

			} else {
				errorMessage = "The current user does not have permission to RENAME the Job";
				LOGGER.log(Level.FINE, "The current user does not have permission to RENAME the Job");
			}
		} catch (Exception e) {
			errorMessage = "Unable to RENAME the Job " + e.getMessage().toString();
			LOGGER.log(Level.SEVERE, "Unable to RENAME the Job " + item.getName(), e.getMessage().toString());
		}

		return success;
	}

	private static final Logger LOGGER = Logger.getLogger(RenameJobRequest.class.getName());

}
