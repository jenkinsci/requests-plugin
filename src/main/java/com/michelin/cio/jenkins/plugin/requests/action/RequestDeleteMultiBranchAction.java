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

package com.michelin.cio.jenkins.plugin.requests.action;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.mail.MessagingException;
import jakarta.servlet.ServletException;

import org.apache.commons.lang3.StringUtils;
import org.kohsuke.stapler.HttpRedirect;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest2;
import org.kohsuke.stapler.StaplerResponse2;
import org.kohsuke.stapler.verb.POST;

import com.michelin.cio.jenkins.plugin.requests.RequestsPlugin;
import com.michelin.cio.jenkins.plugin.requests.model.DeleteMultiBranchRequest;
import com.michelin.cio.jenkins.plugin.requests.model.RequestsUtility;

import hudson.Extension;
import hudson.Functions;
import hudson.XmlFile;
import hudson.model.AbstractItem;
import hudson.model.Action;
import hudson.model.Item;
import jenkins.model.Jenkins;
import jenkins.model.TransientActionFactory;

// Represents the "Ask for deletion" action appearing on a given folder view page.

public class RequestDeleteMultiBranchAction implements Action {

	private AbstractItem project;

	public RequestDeleteMultiBranchAction(AbstractItem target) {
		project = target;
	}

	@POST
	public HttpResponse doCreateDeleteMultiBranchRequest(StaplerRequest2 staplerRequest, StaplerResponse2 response) throws IOException, MessagingException {
		String username = " ";

		try {
			if (isIconDisplayed()) {
				username = staplerRequest.getParameter("username");
				String jobName = project.getFullName();

				if (!StringUtils.isNotEmpty(jobName)) {
					RequestMailSender mailSender = new RequestMailSender("deleteMultiBranch", username, "REQUEST", "", "ERROR",
							"ERROR: Jenkins Project object is null.  Unable to submit the Delete MultiBranch Job Request.");
					mailSender.executeEmail();

					return null;
				}

				RequestsPlugin plugin = Jenkins.get().getPlugin(RequestsPlugin.class);
				if (plugin == null) {
					return null;
				}

				String fullJobURL = "";
				String jobNameSlash = jobName;
				;
				String jobNameJelly = "";
				String[] emailData = { jobName, username, "A Delete Multi Branch", project.getAbsoluteUrl() };

				if (jobName.contains("/")) {
					String[] projectnameList = jobName.split("/");
					int nameCount = projectnameList.length;
					jobName = projectnameList[nameCount - 1];
				}

				jobNameJelly = jobNameSlash;

				if (jobNameJelly.contains("%20")) {
					jobNameJelly = jobNameJelly.replace("%20", " ");
				}
				fullJobURL = project.getAbsoluteUrl();

				LOGGER.info("Delete MBP Job Request jobName:" + jobName + ", fullJobURL:" + fullJobURL);
				// LOGGER.info("[DEBUG] deleteMultiBranch: " + username + ":" + jobName + ":NA" + ":" + fullJobURL + ":" + jobNameSlash + ":" +
				// jobNameJelly + ":NA");

				plugin.addRequestPlusEmail(new DeleteMultiBranchRequest("deleteMultiBranch", username, jobName, "NA", fullJobURL, jobNameSlash, jobNameJelly, "NA"), emailData);
			}

		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "[ERROR] Exception: " + e.getMessage());
			RequestMailSender mailSender = new RequestMailSender("deleteMultiBranch", username, "REQUEST", "", "ERROR",
					"ERROR: Unable to submit the Delete MultiBranch Job Request. " + e.getMessage());
			mailSender.executeEmail();

			return null;
		}

		return new HttpRedirect(staplerRequest.getContextPath() + '/' + project.getUrl());
	}

	public String getDisplayName() {
		if (isIconDisplayed()) {
			return Messages.RequestDeleteMultiBranchAction_DisplayName();
		}
		return null;
	}

	@Override
	public String getIconFileName() {
		return null;
	}

	public String getIconClassName() {
		if (isIconDisplayed()) {
			return "icon-edit-delete";
		}
		return null;
	}

	public AbstractItem getProject() {
		return project;
	}

	public String getUrlName() {
		return "request-delete-multibranch";
	}

	private boolean isIconDisplayed() {
		boolean isDisplayed = false;
		try {
			isDisplayed = !hasDeletePermission();
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, "Impossible to know if the icon has to be displayed", e);
		}

		return isDisplayed;
	}

	private boolean hasDeletePermission() throws IOException, ServletException {
		return Functions.hasPermission(project, Item.DELETE);
	}

	private static final Logger LOGGER = Logger.getLogger(RequestDeleteMultiBranchAction.class.getName());

	// @Extension(optional = true)
	@Extension
	public static class TransientFolderActionFactoryImpl extends TransientActionFactory<AbstractItem> {

		@Override
		public Collection<? extends Action> createFor(@Nonnull AbstractItem target) {
			RequestMailSender.DescriptorEmailImpl descriptorEmailImpl = new RequestMailSender.DescriptorEmailImpl();
			List<Action> adminActions = new ArrayList<Action>();

			// Check if option is enabled in Global settings of the plugin:
			// Note: that a restart is required after making a change since this is
			// loaded at start up time:

			try {
				if (descriptorEmailImpl.isEnableDeleteMultiBranch()) {
					XmlFile objConfigXML = target.getConfigFile();
					String strConfigXML = null;
					RequestsUtility util = new RequestsUtility();

					if (objConfigXML != null) {
						strConfigXML = objConfigXML.asString();
						if (util.verifyJobType(strConfigXML)) {
							adminActions.add(new RequestDeleteMultiBranchAction((AbstractItem) target));
						}
					}
				}

			} catch (Exception e) {
				LOGGER.warning("ERROR: createFor(): IOException error: " + e.getMessage());
			}

			return adminActions;
		}

		@Override
		public Class<AbstractItem> type() {
			return AbstractItem.class;
		}
	}

}
