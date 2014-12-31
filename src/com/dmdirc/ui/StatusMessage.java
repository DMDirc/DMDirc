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

package com.dmdirc.ui;

import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.interfaces.ui.StatusMessageNotifier;

/**
 * Describes a status bar message to be presented to a user.
 */
public class StatusMessage {

    /** Icon type for the message. */
    private final String iconType;
    /** Message to display. */
    private final String message;
    /** Message notifier. */
    private final StatusMessageNotifier messageNotifier;
    /** Timeout when initially displayed. */
    private final int timeout;
    /** Config manager to get default timeout from. */
    private final AggregateConfigProvider configManager;

    /**
     * Creates a new statusbar message.
     *
     * @param iconType        Icon type to use for the message (can be null)
     * @param message         Message to show
     * @param messageNotifier Optional notifier (can be null)
     * @param timeout         message timeout (can be -1)
     * @param configManager   Config manager to get default timeout from
     */
    public StatusMessage(final String iconType, final String message,
            final StatusMessageNotifier messageNotifier, final int timeout,
            final AggregateConfigProvider configManager) {
        this.iconType = iconType;
        this.message = message;
        this.messageNotifier = messageNotifier;
        this.timeout = timeout;
        this.configManager = configManager;
    }

    /**
     * Creates a new statusbar message. This will show no icon, won't have a message notifier and
     * will time out in the default timeout.
     *
     * @param message Message to show
     * @param manager Config manager to get default timeout from
     */
    public StatusMessage(final String message, final AggregateConfigProvider manager) {
        this(null, message, null, -1, manager);
    }

    /**
     * Get the value of message
     *
     * @return the value of message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Get the value of iconType
     *
     * @return the value of iconType
     */
    public String getIconType() {
        return iconType;
    }

    /**
     * Get the value of messageNotifier
     *
     * @return the value of messageNotifier
     */
    public StatusMessageNotifier getMessageNotifier() {
        return messageNotifier;
    }

    /**
     * Get the value of timeout
     *
     * @return the value of timeout
     */
    public int getTimeout() {
        if (timeout == -1) {
            return configManager.getOptionInt("statusBar",
                    "messageDisplayLength");
        } else {
            return timeout;
        }
    }

}
