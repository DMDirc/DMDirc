/*
 * Copyright (c) 2006-2017 DMDirc Developers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.dmdirc.ui.core.feedback;

import com.dmdirc.interfaces.ui.FeedbackDialogModel;
import com.dmdirc.interfaces.ui.FeedbackDialogModelListener;
import com.dmdirc.util.collections.ListenerList;
import com.dmdirc.util.validators.NotEmptyValidator;
import com.dmdirc.util.validators.OptionalEmailAddressValidator;
import com.dmdirc.util.validators.PermissiveValidator;
import com.dmdirc.util.validators.Validator;

import java.util.Optional;

import javax.inject.Inject;

/**
 * Default implementation of a feedback dialog module.
 */
public class CoreFeedbackDialogModel implements FeedbackDialogModel {

    private final FeedbackSenderFactory feedbackSenderFactory;
    private final ListenerList listeners;
    private final FeedbackHelper feedbackHelper;
    private Optional<String> name;
    private Optional<String> email;
    private Optional<String> feedback;
    private boolean includeServerInfo;
    private boolean includeDMDircInfo;

    @Inject
    public CoreFeedbackDialogModel(final FeedbackSenderFactory feedbackSenderFactory,
            final FeedbackHelper feedbackHelper) {
        this.feedbackSenderFactory = feedbackSenderFactory;
        this.feedbackHelper = feedbackHelper;
        this.listeners = new ListenerList();
        name = Optional.empty();
        email = Optional.empty();
        feedback = Optional.empty();
        includeServerInfo = false;
        includeDMDircInfo = false;
    }

    @Override
    public Optional<String> getName() {
        return name;
    }

    @Override
    public void setName(final Optional<String> name) {
        this.name = name;
        listeners.getCallable(FeedbackDialogModelListener.class).nameChanged(name);
    }

    @Override
    public boolean isNameValid() {
        return !getNameValidator().validate(name.orElse("")).isFailure();
    }

    @Override
    public Validator<String> getNameValidator() {
        return new PermissiveValidator<>();
    }

    @Override
    public Optional<String> getEmail() {
        return email;
    }

    @Override
    public void setEmail(final Optional<String> email) {
        this.email = email;
        listeners.getCallable(FeedbackDialogModelListener.class).emailChanged(email);
    }

    @Override
    public boolean isEmailValid() {
        return !getEmailValidator().validate(email.orElse("")).isFailure();
    }

    @Override
    public Validator<String> getEmailValidator() {
        return new OptionalEmailAddressValidator();
    }

    @Override
    public Optional<String> getFeedback() {
        return feedback;
    }

    @Override
    public void setFeedback(final Optional<String> feedback) {
        this.feedback = feedback;
        listeners.getCallable(FeedbackDialogModelListener.class).feedbackChanged(feedback);
    }

    @Override
    public boolean isFeedbackValid() {
        return !getFeedbackValidator().validate(feedback.orElse("")).isFailure();
    }

    @Override
    public Validator<String> getFeedbackValidator() {
        return new NotEmptyValidator();
    }

    @Override
    public boolean getIncludeServerInfo() {
        return includeServerInfo;
    }

    @Override
    public void setIncludeServerInfo(final boolean includeServerInfo) {
        this.includeServerInfo = includeServerInfo;
        listeners.getCallable(FeedbackDialogModelListener.class).includeServerInfoChanged(
                includeServerInfo);
    }

    @Override
    public boolean getIncludeDMDircInfo() {
        return includeDMDircInfo;
    }

    @Override
    public void setIncludeDMDircInfo(final boolean includeDMDircInfo) {
        this.includeDMDircInfo = includeDMDircInfo;
        listeners.getCallable(FeedbackDialogModelListener.class).includeDMDircInfoChanged(
                includeDMDircInfo);
    }

    @Override
    public boolean isSaveAllowed() {
        return isNameValid() && isEmailValid() && isFeedbackValid();
    }

    @Override
    public void save() {
        final String serverInfo;
        if (getIncludeServerInfo()) {
            serverInfo = feedbackHelper.getServerInfo();
        } else {
            serverInfo = "";
        }
        final String dmdircInfo;
        if (getIncludeDMDircInfo()) {
            dmdircInfo = feedbackHelper.getDMDircInfo();
        } else {
            dmdircInfo = "";
        }
        final FeedbackSender sender = feedbackSenderFactory.getFeedbackSender(
                name.orElse(""), email.orElse(""), feedback.orElse(""),
                feedbackHelper.getVersion(), serverInfo, dmdircInfo);
        new Thread(sender, "Feedback Sender").start();
    }

    @Override
    public void addListener(final FeedbackDialogModelListener listener) {
        listeners.add(FeedbackDialogModelListener.class, listener);
    }

    @Override
    public void removeListener(final FeedbackDialogModelListener listener) {
        listeners.remove(FeedbackDialogModelListener.class, listener);
    }

}
