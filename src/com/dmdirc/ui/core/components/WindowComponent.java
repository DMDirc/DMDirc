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

package com.dmdirc.ui.core.components;

/**
 * An enumeration of common window components.
 */
public enum WindowComponent {

    /** A small bar to show the current topic/subject and allow editing. */
    TOPICBAR("com.dmdirc.ui.components.topicbar"),
    /** A panel to show a list of users or contacts. */
    USERLIST("com.dmdirc.ui.components.userlist"),
    /** A large text area for generic output. */
    TEXTAREA("com.dmdirc.ui.components.textarea"),
    /** An input field for user input or commands. */
    INPUTFIELD("com.dmdirc.ui.components.inputfield"),
    /** A component to show details of or problems with a certificate. */
    CERTIFICATE_VIEWER("com.dmdirc.ui.components.certificateviewer"),
    /** An indicator that a remote party is typing. */
    TYPING_INDICATOR("com.dmdirc.ui.components.typing");
    /** The identifier for this window component. */
    private final String identifier;

    /**
     * Creates a new WindowComponent.
     *
     * @param identifier The identifier for this window component
     */
    WindowComponent(final String identifier) {
        this.identifier = identifier;
    }

    /**
     * Gets the identifier for this component.
     *
     * @return This component's identifier
     */
    public String getIdentifier() {
        return identifier;
    }

}
