/*
 * The MIT License
 *
 * Copyright (c) 2011-2012, Manufacture Francaise des Pneumatiques Michelin, Daniel Petisme
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
import jenkins.model.Jenkins;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;


 // Represents a deletion request sent by a user to Jenkins' administrator.
 // @author Daniel Petisme <daniel.petisme@gmail.com> <http://danielpetisme.blogspot.com/>

public class DeleteJobRequest extends Request {

	private static final Logger LOGGER = Logger.getLogger(DeleteJobRequest.class.getName());
	
    public DeleteJobRequest(String requestType, String username, String project, String projectFullName, String buildNumber) {
        super(requestType, username, project, projectFullName, buildNumber);
    }

    @Override
    public String getMessage() {
        return Messages.DeleteJobRequest_message(projectFullName).toString();
    }

    public boolean execute(Item item) {
        boolean success = false;

        if (Jenkins.getInstance().hasPermission(Item.DELETE)) {            
            try {
            	item.delete();
                success = true;
                LOGGER.log(Level.INFO, "The job {0} has been properly deleted", item.getFullName());
            } catch (IOException e) {
                errorMessage = e.getMessage();
                LOGGER.log(Level.SEVERE, "Unable to delete the job " + item.getFullName(), e);
            } catch (InterruptedException e) {
                errorMessage = e.getMessage();
                LOGGER.log(Level.SEVERE, "Unable to delete the job " + item.getFullName(), e);
            }
        } else {
            errorMessage = "The current user " + username + " does not have permission to delete the job";            
            LOGGER.log(Level.FINE, "The current user {0} does not have permission to DELETE the job", new Object[]{username});
        }

        return success;
    }
    
}
