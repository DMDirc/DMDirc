/*
 * Copyright (c) 2006-2011 DMDirc Developers
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

package com.dmdirc.ui.interfaces;

import com.dmdirc.Channel;
import com.dmdirc.FrameContainer;
import com.dmdirc.Server;

import java.net.URI;

/**
 * Defines the methods that should be implemented by UI controllers. Controllers
 * handle the various aspects of a UI implementation.
 */
public interface UIController {

    /**
     * Requests that the specified window be bought to focus in this UI.
     *
     * @param window The window to be focused
     */
    void requestWindowFocus(Window window);

    /**
     * Requests that the specified container's window be bought to focus in
     * this UI.
     *
     * @param container The framecontainer whose window is to be focused
     */
    void requestWindowFocus(FrameContainer container);

    /**
     * Shows the first run wizard for the ui.
     */
    void showFirstRunWizard();

    /**
     * Shows a channel settigns dialog for specified channel.
     *
     * @param channel Channel to show the dialog for
     */
    void showChannelSettingsDialog(Channel channel);

    /**
     * Shows a server settigns dialog for specified server.
     *
     * @param server Server to show the dialog for
     */
    void showServerSettingsDialog(Server server);

    /**
     * Shows the unknown URL protocol handling dialog for a URL.
     *
     * @param url full url
     */
    void showURLDialog(final URI url);

    /**
     * Show feedback nag.
     */
    void showFeedbackNag();

    /**
     * Shows a message dialog to the user.
     *
     * @param title Dialog title
     * @param message Message to display
     */
    void showMessageDialog(final String title, final String message);
}
