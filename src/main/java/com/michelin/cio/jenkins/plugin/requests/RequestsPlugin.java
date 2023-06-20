/*
 * The MIT License
 *
 * Copyright (c) 2011-2012, Manufacture Francaise des Pneumatiques Michelin, Daniel Petisme
 * Portions Copyright 2019 Lexmark
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
package com.michelin.cio.jenkins.plugin.requests;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import javax.mail.MessagingException;
import javax.servlet.ServletException;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.HttpRedirect;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.interceptor.RequirePOST;

import com.michelin.cio.jenkins.plugin.requests.action.RequestMailSender;
import com.michelin.cio.jenkins.plugin.requests.model.DeleteBuildRequest;
import com.michelin.cio.jenkins.plugin.requests.model.DeleteFolderRequest;
import com.michelin.cio.jenkins.plugin.requests.model.DeleteJobRequest;
import com.michelin.cio.jenkins.plugin.requests.model.DeleteMultiBranchRequest;
import com.michelin.cio.jenkins.plugin.requests.model.RenameFolderRequest;
import com.michelin.cio.jenkins.plugin.requests.model.RenameJobRequest;
import com.michelin.cio.jenkins.plugin.requests.model.RenameMultiBranchRequest;
import com.michelin.cio.jenkins.plugin.requests.model.Request;
import com.michelin.cio.jenkins.plugin.requests.model.UnlockRequest;

import hudson.Extension;
import hudson.Plugin;
import hudson.model.Hudson;
import hudson.model.ManagementLink;
import jenkins.model.Jenkins;

//Manages pending requests.
// @author Daniel Petisme <daniel.petisme@gmail.com> <http://danielpetisme.blogspot.com/>, John Flynn <john.trixmot.flynn1@gmail.com>

public class RequestsPlugin extends Plugin {
	// The requests are unique (to avoid duplication problems like delete a job already deleted)
	private List<Request> requests = new ArrayList<Request>();
	private static final Logger LOGGER = Logger.getLogger(RequestsPlugin.class.getName());
	private transient List<String> errors = new ArrayList<String>();

	/*
	 * public void addRequest(final Request request) { boolean alreadyRequested = false;
	 * 
	 * for (int i = 0; i < requests.size(); i++) { String projectFullName = requests.get(i).getProjectFullName(); String buildNumber =
	 * requests.get(i).getBuildNumber(); String requestType = requests.get(i).getRequestType();
	 * 
	 * // Allows a delete project, delete build and unlock build for the // same build but will not submit duplicate of the same
	 * request: if (projectFullName.equals(request.getProjectFullName()) && buildNumber.equals(request.getBuildNumber()) &&
	 * requestType.equals(request.getRequestType())) { alreadyRequested = true; break; } }
	 * 
	 * if (!alreadyRequested) { requests.add(request); persistPendingRequests(); } }
	 */

	public void addRequestPlusEmail(final Request request, final String[] emailData) throws UnknownHostException, MessagingException {
		boolean alreadyRequested = false;

		for (int i = 0; i < requests.size(); i++) {
			// String projectFullName = requests.get(i).getProjectFullName();
			String jobName = requests.get(i).getJobNameSlash();
			String buildNumber = requests.get(i).getBuildNumber();
			String requestType = requests.get(i).getRequestType();

			// Allows a delete project, delete build and unlock build for the same build but will not submit duplicate of the same request:
			if (jobName.equals(request.getJobNameSlash()) && buildNumber.equals(request.getBuildNumber()) && requestType.equals(request.getRequestType())) {
				alreadyRequested = true;
				break;
			}
		}

		if (!alreadyRequested) {
			requests.add(request);
			final int element0 = 0;
			final int element1 = 1;
			final int element2 = 2;
			final int element3 = 3;
			RequestMailSender mailSender = new RequestMailSender(emailData[element0], emailData[element1], emailData[element2], emailData[element3]);
			// LOGGER.info("[INFO] mailSender 1:");
			mailSender.executeEmail();
			// LOGGER.info("[INFO] mailSender 2:");
			persistPendingRequests();
		}
	}

	@RequirePOST
	public HttpResponse doManageRequests(final StaplerRequest request, final StaplerResponse response) throws IOException, ServletException {
		Jenkins.get().checkPermission(Jenkins.ADMINISTER);
		errors.clear();
		String[] selectedRequests = request.getParameterValues("selected");
		ArrayList<Integer> selectedIndexs = new ArrayList<Integer>();
		// Store the request once they have been applied
		List<Request> requestsToRemove = new ArrayList<Request>();

		if (selectedRequests != null && selectedRequests.length > 0) {
			for (String sindex : selectedRequests) {
				if (StringUtils.isNotBlank(sindex)) {
					int index = Integer.parseInt(sindex);
					Request currentRequest = requests.get(index);
					selectedIndexs.add(index);

					// LOGGER.info("[DEBUG] Request parameter testing for apply: " + request.hasParameter("apply"));
					// LOGGER.info("[DEBUG] Request parameter testing for discard: " + request.hasParameter("discard"));

					// The apply/discard parameters comes from the RequestsPlugin - index.jelly file (Pending Requests page)
					if (request.hasParameter("apply")) {
						String requestType = currentRequest.getRequestType();

						if (currentRequest.process(requestType)) {
							// Success, store to remove:
							requestsToRemove.add(currentRequest);

						} else {
							// Failed, show error message:
							errors.add(currentRequest.getErrorMessage().toString());
							LOGGER.info("[WARNING] The request can not be processed: " + currentRequest.getMessage().toString());
						}
					} else if (request.hasParameter("discard")) {
						requestsToRemove.add(currentRequest);
						LOGGER.info("[INFO] The request has been discarded: " + currentRequest.getMessage().toString());
					} else {
						errors.add("The request paramater is not defined");
						LOGGER.info("[WARNING] The request parameter is not defined");
					}

				} else {
					errors.add("The request index is not defined");
					LOGGER.info("[WARNING] The request index is not defined");
				}
			}
		} else {
			errors.add("No Requests selected");
			LOGGER.info("[INFO] Nothing selected");
		}

		// Once it has done the work, it removes the applied requests
		if (!requestsToRemove.isEmpty()) {
			removeAllRequests(selectedIndexs);
		}

		return new HttpRedirect(".");
	}

	public List<Request> getRequests() {
		List<Request> requests2 = new ArrayList<Request>();
		requests2.addAll(requests);
		return requests2;
	}

	public List<String> getErrors() {
		List<String> errors2 = new ArrayList<String>();
		errors2.addAll(errors);
		return errors2;
	}

	public void setErrors(String errorString) {
		errors.clear();
		errors.add(errorString);
	}

	private void persistPendingRequests() {
		try {
			save();
		} catch (IOException e) {
			LOGGER.info("[WARNING] Failed to persist the pending requests");
		}
	}

	public void removeAllRequests(ArrayList<Integer> selectedIndexs) {
		// Remove index's from requests starting with highest index first:
		Collections.sort(selectedIndexs, Collections.reverseOrder());

		for (int i = 0; i < selectedIndexs.size(); i++) {
			int selectedIndex = selectedIndexs.get(i);
			requests.remove(selectedIndex);
		}

		persistPendingRequests();
	}

	@Override
	public void start() throws Exception {
		super.start();

		Hudson.XSTREAM.alias("UnlockRequest", UnlockRequest.class);
		Hudson.XSTREAM.alias("DeleteJobRequest", DeleteJobRequest.class);
		Hudson.XSTREAM.alias("DeleteBuildRequest", DeleteBuildRequest.class);
		Hudson.XSTREAM.alias("RenameJobRequest", RenameJobRequest.class);
		Hudson.XSTREAM.alias("RenameFolderRequest", RenameFolderRequest.class);
		Hudson.XSTREAM.alias("DeleteFolderRequest", DeleteFolderRequest.class);
		Hudson.XSTREAM.alias("DeleteMultiBranchRequest", DeleteMultiBranchRequest.class);
		Hudson.XSTREAM.alias("RenameMultiBranchRequest", RenameMultiBranchRequest.class);
		Hudson.XSTREAM.alias("RequestsPlugin", RequestsPlugin.class);

		load();
	}

	@Extension
	public static final class RequestManagementLink extends ManagementLink {

		@Override
		public String getDescription() {
			return Messages.RequestManagementLink_Description();
		}

		public String getCategoryName() {
			return "TOOLS";
		}

		@Override
		public String getIconFileName() {
			return "/images/48x48/clipboard.png";
		}

		// public String getIconClassName() {
		// return "icon-clipboard";
		// }

		public String getDisplayName() {
			return Messages.RequestManagementLink_DisplayName();
		}

		@Override
		public String getUrlName() {
			return "plugin/requests";
		}
	}

}
