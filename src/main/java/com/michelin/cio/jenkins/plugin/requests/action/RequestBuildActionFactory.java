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

import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.Action;
import jenkins.model.TransientActionFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


// @author John Flynn <john.trixmot.flynn@gmail.com>

@Extension
public class RequestBuildActionFactory extends TransientActionFactory<AbstractBuild> {

	
    @Override public Class<AbstractBuild> type() {
        return AbstractBuild.class;
    }

    public Collection<? extends Action> createFor(AbstractBuild target) {
    	RequestMailSender.DescriptorEmailImpl descriptorEmailImpl = new RequestMailSender.DescriptorEmailImpl();
        List<Action> adminActions = new ArrayList<Action>();

        if (descriptorEmailImpl.isEnableDeleteBuild()) {
        	adminActions.add(new RequestDeleteBuildAction(target));
        }
        
        if (descriptorEmailImpl.isEnableUnlockBuild()) {
        	adminActions.add(new RequestUnlockAction(target));
        }
        
        return adminActions;
    }

}
