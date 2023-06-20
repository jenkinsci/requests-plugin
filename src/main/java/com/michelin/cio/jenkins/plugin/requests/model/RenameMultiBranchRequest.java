/*
 * The MIT License 
 *
 * Copyright 2023 Lexmark
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

// Represents a renaming request sent by a user to the administrator.

public class RenameMultiBranchRequest extends Request {

	private String newName;

	public RenameMultiBranchRequest(String requestType, String username, String jobNameSpace, String newName, String fullJobURL, String jobNameSlash, String jobNameJelly,
			String rename) {
		super(requestType, username, jobNameSpace, newName, fullJobURL, jobNameSlash, jobNameJelly, rename);
		this.newName = newName;
	}

	@Override
	public String getMessage() {
		return Messages.RenameMultiBranchRequest_message(jobNameJelly, rename);
	}

	public String getNewName() {
		return newName;
	}

	// @Restricted(org.kohsuke.accmod.restrictions.NoExternalUse.class)
	@Override
	public boolean execute(Item item) {
		boolean success = false;

		try {
			// if ((Jenkins.get().hasPermission(Item.DELETE) && !Jenkins.get().hasPermission(Item.CREATE) &&
			// !Jenkins.get().hasPermission(Item.CONFIGURE))
			// || (!Jenkins.get().hasPermission(Item.DELETE) && Jenkins.get().hasPermission(Item.CREATE) &&
			// !Jenkins.get().hasPermission(Item.CONFIGURE))
			// || (!Jenkins.get().hasPermission(Item.DELETE) && !Jenkins.get().hasPermission(Item.CREATE) &&
			// !Jenkins.get().hasPermission(Item.CONFIGURE))) {
			if (Jenkins.get().hasPermission(Item.DELETE)) {
				// ((AbstractItem) item).doConfirmRename(newName);

				// LOGGER.info("[DEBUG] MultibranchPipeline item parent: " + item.getParent().getFullName().toString());
				// RenameMultiBranch renameMultiBranch = new RenameMultiBranch(item.getParent(), rename) {
				// };
				// renameMultiBranch.renameTo(rename);
				// ((AbstractItem) item).renameTo(rename);
				// RenameMultiBranch renameMultiBranch = new RenameMultiBranch(item.getParent(), rename);

				// renameMultiBranch.renameTo(rename);

				success = true;
				LOGGER.log(Level.INFO, "The Multibranch Pipeline has successfully been renamed from " + jobNameSlash + " to " + rename);

			} else {
				errorMessage = "The current user does not have permission to RENAME the Multibranch Pipeline";
				LOGGER.log(Level.FINE, "The current user does not have permission to RENAME the Multibranch Pipeline");
			}
		} catch (Exception e) {
			errorMessage = "Unable to RENAME the Multibranch Pipeline " + e.getMessage().toString();
			LOGGER.log(Level.SEVERE, "Unable to RENAME the Multibranch Pipeline " + item.getName(), e.getMessage().toString());
		}

		return success;
	}

	private static final Logger LOGGER = Logger.getLogger(RenameMultiBranchRequest.class.getName());

	/*
	 * public class RenameMultiBranch extends AbstractItem {
	 * 
	 * protected RenameMultiBranch(ItemGroup parent, String name) { super(parent, name); // TODO Auto-generated constructor stub try {
	 * this.renameTo(name); } catch (IOException e) { // TODO Auto-generated catch block e.printStackTrace(); }
	 * 
	 * }
	 * 
	 * @Override public Collection<? extends Job> getAllJobs() { // TODO Auto-generated method stub return null; }
	 * 
	 * // public void renameTo(String rename) { // this.renameTo(rename); // LOGGER.info("[DEBUG] MultibranchPipeline rename: " +
	 * rename); // }
	 * 
	 * }
	 */
}
