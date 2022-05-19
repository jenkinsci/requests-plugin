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

package com.michelin.cio.jenkins.plugin.requests.action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import hudson.Extension;
import hudson.model.Action;
import hudson.model.Run;
import jenkins.model.TransientActionFactory;

// @author John Flynn <john.trixmot.flynn@gmail.com>

@Extension
public class RequestBuildActionFactory extends TransientActionFactory<Run> {

	public static final Logger LOGGER = Logger.getLogger(RequestBuildActionFactory.class.getName());

	@Override
	public Class<Run> type() {
		return Run.class;
	}

	public Collection<? extends Action> createFor(Run target) {
		RequestMailSender.DescriptorEmailImpl descriptorEmailImpl = new RequestMailSender.DescriptorEmailImpl();
		List<Action> adminActions = new ArrayList<Action>();

		if (descriptorEmailImpl.isEnableDeleteBuild()) {
			// LOGGER.info("ActionFactory Delete Build Request: ");
			adminActions.add(new RequestDeleteBuildAction(target));
		}

		if (descriptorEmailImpl.isEnableUnlockBuild()) {
			// LOGGER.info("ActionFactory Unlock Build Request: ");
			adminActions.add(new RequestUnlockAction(target));
		}

		return adminActions;
	}

}
