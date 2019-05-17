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
 *
 * ----------------------------------------------------------------------------
 */

package com.michelin.cio.jenkins.plugin.requests.action;


import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import javax.mail.*;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import net.sf.json.JSONObject;

import hudson.tasks.Builder;
import hudson.util.FormValidation;
import hudson.util.Secret;


// Option to email an admin user/group for delete request:
// @author John Flynn <john.trixmot.flynn@gmail.com>

public class RequestMailSender extends Builder{ 

	private final String itemName;
	private final String userName;
	private final String requestType;
	private final String projectURL;
	private String requestadminemail;
	private String requestmaildomain;
	private String requestemailserver;


	private static final Logger LOGGER = Logger.getLogger(RequestMailSender.class.getName());    

	@DataBoundConstructor
	public RequestMailSender(String itemName, String userName, String requestType, String projectURL) {
		this.itemName = itemName;
		this.userName = userName;
		this.requestType = requestType; 
		this.projectURL = projectURL;
	}

	// Check if request admin value exits before trying to create the email:
	private boolean checkAddress() {
		boolean checkStatus = false;

		// Check for null, or white space:
		if (requestadminemail != null && StringUtils.isNotBlank(requestadminemail)) {	   		
			// Check if address is in the form of "Jenkins Daemon <foo@acme.org>":
			if (StringUtils.contains(requestadminemail, '<')) {
				String[] tokens = requestadminemail.split("<");
				String tempStr = tokens[1];
				tokens = tempStr.split(">");
				requestadminemail = tokens[0]; 
			}
			checkStatus = true;
		} 

		return checkStatus;
	}

	public String getProjectURL() {
		return projectURL;
	}

	public void executeEmail() throws MessagingException, UnknownHostException {  
		// Check if email option is set in the global configuration:
		if (!getDescriptor().isEnableEmails()) {
			LOGGER.log(Level.INFO, "[INFO] The Request email option is not enabled so an email will not be sent");
			return;
		}
		requestadminemail = getDescriptor().getRequestadminemail();
		requestmaildomain = getDescriptor().getRequestmaildomain();
		requestemailserver = getDescriptor().getRequestemailserver();
		boolean failedStatus = false;

		if (requestemailserver == null || requestemailserver.equals("")) {   		
			LOGGER.log(Level.WARNING, "[ERROR] The Requests email server value is missing so no email will not be sent");
			failedStatus = true;
		}

		if (requestmaildomain == null || requestmaildomain.equals("")) {   		
			LOGGER.log(Level.WARNING, "[ERROR] The Requests email domain is missing so mo email will not be sent");
			failedStatus = true;
		}

		if (requestadminemail == null || requestadminemail.equals("")) {   		
			LOGGER.log(Level.WARNING, "[ERROR] The Requests email address is missing so no email will not be sent");
			failedStatus = true;
		}

		if (!checkAddress()) {
			LOGGER.log(Level.WARNING, "[ERROR] The Requests email address is invalid so no email will not be sent");
			failedStatus = true;
		}    	

		if (failedStatus) {
			return;
		}

		MimeMessage mail = createMail(itemName, userName, requestType);

		if (mail != null) {
			Transport.send(mail);
			LOGGER.log(Level.INFO, "[INFO] A Request email has been sent to " + requestadminemail + " from " + userName);
		}

		return;
	}

	private MimeMessage createMail(String itemName, String userName, String requestType) throws MessagingException, UnknownHostException {
		String jenkinsURL = getProjectURL();
		MimeMessage msg = createEmptyMail();
		StringBuffer buf = new StringBuffer();
		String[] jenkinsURLArray = null;

		if (msg != null) { 	
			String jenkinsHostName = InetAddress.getLocalHost().getHostName();

			// Check to see if the hostname is an ip address:
			if (Character.isLetter(jenkinsHostName.charAt(0))){
				String[] nameArray = jenkinsHostName.split("\\.");
				jenkinsHostName = nameArray[0];
				jenkinsHostName = jenkinsHostName.toUpperCase(); 
			}

			// Get Jenkins URL from the projectURL:   		
			if (projectURL.contains("/view/")){
				jenkinsURLArray = jenkinsURL.split("/view/");
			} else if (jenkinsURL.contains("/job/")) {
				jenkinsURLArray = jenkinsURL.split("/job/");
			} 

			String pendingRequestsLink = jenkinsURLArray[0] +  "/plugin/requests/";

			// Email Subject line:
			msg.setSubject(String.format(jenkinsHostName + ": " + " Request has been submitted"));

			// Email page content:
			buf.append(".......................................................................................................................\n\n");	    	
			buf.append(requestType + " request has been submitted for '" + itemName + "'.\n\n");
			buf.append(pendingRequestsLink + "\n\n\n");
			buf.append(getProjectURL() + "\n");
			buf.append(".......................................................................................................................\n");
			msg.setText(buf.toString());
		}

		return msg;
	}

	private MimeMessage createEmptyMail() throws MessagingException {
		String emailHost = getDescriptor().getRequestemailhost();
		if (emailHost == null || requestemailserver.equals("")) {
			emailHost = "localhost";
		}
		Properties properties = System.getProperties();       
		properties.setProperty(requestemailserver, emailHost);
		Session session = Session.getDefaultInstance(properties);
		MimeMessage msg = null;

		try{	
			msg = new MimeMessage(session);
			msg.setContent("", "text/html");	        
			msg.setFrom(new InternetAddress(userName + requestmaildomain));
			msg.setSentDate(new Date());

			Address[] addresss_TO = new Address[1];
			Address[] addresss_CC = new Address[1];

			addresss_TO[0] = new InternetAddress(requestadminemail);
			addresss_CC[0] = new InternetAddress(userName + requestmaildomain);

			msg.addRecipients(Message.RecipientType.TO, addresss_TO);
			msg.addRecipients(Message.RecipientType.CC, addresss_CC);

		}catch (MessagingException me) {
			LOGGER.log(Level.WARNING, "Unable to create email message! ",  me.getMessage());
		}

		return msg;
	}

	public DescriptorEmailImpl getDescriptor() {
		return (DescriptorEmailImpl) super.getDescriptor();
	}

	@Extension()
	public static class DescriptorEmailImpl extends BuildStepDescriptor<Builder> {

		private String requestadminemail;
		private String requestemailserver;
		private String requestmaildomain;
		private String requestemailhost;
		private String unlockuser;
		private Secret unlockpassword;
		private boolean enableDeleteJob;
		private boolean enableDeleteBuild;
		private boolean enableUnlockBuild;
		private boolean enableEmails;
		private String testEmailAddress;

		public DescriptorEmailImpl() {
			super(RequestMailSender.class);
			load();
		}

		public String getRequestemailserver() {
			return requestemailserver;
		}
		
		public String getRequestemailhost() {
			return requestemailhost;
		}

		public String getRequestmaildomain() {
			return requestmaildomain;
		}

		public String getRequestadminemail() {
			return requestadminemail;
		}

		public String getUnlockuser() {
			return unlockuser;
		}
		
		public Secret getUnlockpassword() {
			return unlockpassword;
		}
		
		public String getTestEmailAddress() {
			return testEmailAddress;
		}

		public void setRequestemailserver(String requestemailserver) {
			this.requestemailserver = requestemailserver;
		}

		public void setRequestmaildomain(String requestmaildomain) {
			this.requestmaildomain = requestmaildomain;
		}

		public void setRequestadminemail(String requestadminemail) {
			this.requestadminemail = requestadminemail;
		}

		public void setRequestemailhost(String requestemailhost) {
			this.requestemailhost = requestemailhost;
		}
		
		public void setUnlockuser(String unlockuser) {
			this.unlockuser = unlockuser;
		}
		
		public void setUnlockpassword(Secret unlockpassword) {
			this.unlockpassword = unlockpassword;
		}
		
		public boolean isEnableDeleteJob() {
			return enableDeleteJob;
		}
		
		public boolean isEnableDeleteBuild() {
			return enableDeleteBuild;
		}
		
		public boolean isEnableUnlockBuild() {
			return enableUnlockBuild;
		}
		
		public boolean isEnableEmails(){
			return enableEmails;
		}
		
		public void setEnableDeleteJob(boolean enableDeleteJob) {
			this.enableDeleteJob = enableDeleteJob;
		}
		
		public void setEnableDeleteBuild(boolean enableDeleteBuild) {
			this.enableDeleteBuild = enableDeleteBuild;
		}
		
		public void setEnableUnlockBuild(boolean enableUnlockBuild) {
			this.enableUnlockBuild = enableUnlockBuild;
		}

		public void setEnableEmails(boolean enableEmails){
			this.enableEmails = enableEmails;
		}
		
		// Runs when the global settings are submitted to save configuration changes:
		@Override
		public boolean configure(StaplerRequest req, JSONObject json) throws FormException{
			req.bindJSON(this, json.getJSONObject("globalRequests"));
			save();
			return true;
		}

		@Override
		public boolean isApplicable(Class<? extends AbstractProject> arg0) {
			return true;
		}

		@Override
		public String getDisplayName() {
			return "Requests";
		}
		
		public FormValidation doTestEmail(@QueryParameter("testEmailAddress") final String testEmailAddress) throws MessagingException, UnknownHostException {
			String emailHost = getRequestemailhost();
			String returnMessage = "Unable to create email message";
			if (emailHost == null || requestemailserver.equals("")) {
				emailHost = "localhost";
			}
			Properties properties = System.getProperties();       
			properties.setProperty(requestemailserver, emailHost);
			Session session = Session.getDefaultInstance(properties);
			MimeMessage msg = null;

			try{	
				msg = new MimeMessage(session);
				msg.setContent("", "text/html");	        
				msg.setFrom(new InternetAddress(requestadminemail));
				msg.setSentDate(new Date());

				Address[] addresss_TO = new Address[1];
				addresss_TO[0] = new InternetAddress(testEmailAddress);
				msg.addRecipients(Message.RecipientType.TO, addresss_TO);

			} catch (MessagingException me) {
				LOGGER.log(Level.WARNING, "Unable to create email message! ",  me.getMessage());
				returnMessage = "Unable to create email message"; 	
	  			return FormValidation.error(returnMessage);
			}
			
			StringBuffer buf = new StringBuffer();

			// Email Subject line:
			msg.setSubject(String.format("Test Email - Jenkins Request Plugin"));

			// Email page content:
			buf.append(".......................................................................................................................\n");	    	
			buf.append("This is a test email from the Jenkins Requests Plugin\n");
			buf.append(".......................................................................................................................\n");
			msg.setText(buf.toString());
			
			
			if (msg != null) {
				Transport.send(msg);
				returnMessage = "Email sent successfully";
			}
					
			return FormValidation.ok(returnMessage);
		}

	}

}


