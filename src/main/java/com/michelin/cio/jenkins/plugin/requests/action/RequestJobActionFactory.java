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

package com.michelin.cio.jenkins.plugin.requests.action;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.Job;
import hudson.model.TransientProjectActionFactory;
import jenkins.model.TransientActionFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

//
// Adds custom {@link Action}s to {@link AbstractProject}s through
// {@link TransientProjectActionFactory}.
//
// @author Daniel Petisme <daniel.petisme@gmail.com> <http://danielpetisme.blogspot.com/>

@Extension
public class RequestJobActionFactory extends TransientActionFactory<Job> {

	@Override
	public Collection<? extends Action> createFor(Job target) {
		RequestMailSender.DescriptorEmailImpl descriptorEmailImpl = new RequestMailSender.DescriptorEmailImpl();
		List<Action> adminActions = new ArrayList<Action>();

		// Note: that a restart is required after making a change since this is
		// loaded at start up time:
		if (descriptorEmailImpl.getEnableDeleteJob()) {
			adminActions.add(new RequestDeleteJobAction(target));
		}

		// Note: that a restart is required after making a change since this is
		// loaded at start up time:
		if (descriptorEmailImpl.isEnableRenameJob()) {
			adminActions.add(new RequestRenameJobAction(target));
		}

		return adminActions;
	}

	@Override
	public Class<Job> type() {
		return Job.class;
	}

}
