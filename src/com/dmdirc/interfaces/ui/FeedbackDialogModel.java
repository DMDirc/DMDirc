/*
 * Copyright (c) 2006-2015 DMDirc Developers
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
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.dmdirc.interfaces.ui;

import com.dmdirc.util.validators.Validator;

import java.util.Optional;

/**
 * Dialog to connect to send feedback to the developers.
 */
public interface FeedbackDialogModel {

    /**
     * Gets the name in the model.
     *
     * @return Optional string containing the name
     */
    Optional<String> getName();

    /**
     * Sets the name in the model.
     *
     * @param name Optional string containing the name
     */
    void setName(Optional<String> name);

    /**
     * Is the name in the model valid?
     *
     * @return true or false
     */
    boolean isNameValid();

    /**
     * Gets the name validator in the model.
     *
     * @return Name validator
     */
    Validator<String> getNameValidator();

    /**
     * Gets the email in the model.
     *
     * @return Optional string containing the email
     */
    Optional<String> getEmail();

    /**
     * Sets the email in the model.
     *
     * @param email Optional string containing the email
     */
    void setEmail(Optional<String> email);

    /**
     * Is the email in the model valid?
     *
     * @return true or false
     */
    boolean isEmailValid();

    /**
     * Gets the validator for the email address
     *
     * @return Email validator
     */
    Validator<String> getEmailValidator();

    /**
     * Gets the feedback in the model.
     *
     * @return Optional string containing the feedback
     */
    Optional<String> getFeedback();

    /**
     * Sets the feedback in the model.
     *
     * @param feedback Optional string containing the feedback
     */
    void setFeedback(Optional<String> feedback);

    /**
     * Is the feedback in the model valid?
     *
     * @return true or false
     */
    boolean isFeedbackValid();

    /**
     * Gets the validator for the feedback.
     *
     * @return Feedback validator
     */
    Validator<String> getFeedbackValidator();

    /**
     * Should we include the server info with the feedback?
     *
     * @return true or false
     */
    boolean getIncludeServerInfo();

    /**
     * Sets whether we should include the server info with the feedback.
     *
     * @param includeServerInfo true or false
     */
    void setIncludeServerInfo(boolean includeServerInfo);

    /**
     * Should we include information about DMDirc with the feedback?
     *
     * @return true or false
     */
    boolean getIncludeDMDircInfo();

    /**
     * Sets whether we should include information about DMDirc with the feedback.
     *
     * @param includeDMDircInfo true or false
     */
    void setIncludeDMDircInfo(boolean includeDMDircInfo);

    /**
     * Are we allowed to save the dialog?
     *
     * @return true or false
     */
    boolean isSaveAllowed();

    /**
     * Saves the state of the dialog, sending specified feedback.
     */
    void save();

    /**
     * Adds a listener to this model.
     *
     * @param listener Listener to add
     */
    void addListener(FeedbackDialogModelListener listener);

    /**
     * Removes a listener from this model.
     *
     * @param listener Listener to remove
     */
    void removeListener(FeedbackDialogModelListener listener);

}
