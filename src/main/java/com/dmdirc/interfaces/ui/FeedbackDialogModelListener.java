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

package com.dmdirc.interfaces.ui;

import java.util.Optional;

/**
 * Listener for various events in a feedback dialog.
 */
public interface FeedbackDialogModelListener {

    /**
     * Called when the name is changed.
     *
     * @param name New name
     */
    void nameChanged(Optional<String> name);

    /**
     * Called when the email is changed.
     *
     * @param email New email
     */
    void emailChanged(Optional<String> email);

    /**
     * Called when the feedback is changed.
     *
     * @param feedback New feedback
     */
    void feedbackChanged(Optional<String> feedback);

    /**
     * Called when the server info checkbox is changed.
     *
     * @param includeServerInfo New state
     */
    void includeServerInfoChanged(boolean includeServerInfo);

    /**
     * Called when the DMDirc info checkbox is changed.
     *
     * @param includeDMDircInfo New state
     */
    void includeDMDircInfoChanged(boolean includeDMDircInfo);

}
