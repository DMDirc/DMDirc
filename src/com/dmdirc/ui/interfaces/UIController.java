/*
 * Copyright (c) 2006-2011 Chris Smith, Shane Mc Cormack, Gregory Holmes
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
import com.dmdirc.Server;
import com.dmdirc.config.prefs.PreferencesInterface;
import com.dmdirc.ui.core.dialogs.sslcertificate.SSLCertificateDialogModel;
import com.dmdirc.updater.Update;

import java.net.URI;
import java.util.List;

/**
 * Defines the methods that should be implemented by UI controllers. Controllers
 * handle the various aspects of a UI implementation.
 *
 * @author Chris
 */
public interface UIController {

    /**
     * Retrieves the main window used by this UI.
     *
     * @return This UI's main window
     */
    MainWindow getMainWindow();

    /**
     * Returns an updater dialog for the specified updates.
     *
     * @param updates Updates available
     *
     * @return UpdaterDialog
     */
    UpdaterDialog getUpdaterDialog(List<Update> updates);

    /**
     * Shows the first run wizard for the ui.
     */
    void showFirstRunWizard();

    /**
     * Shows the SSL certificate information dialog.
     *
     * @param model The dialog model to use
     */
    void showSSLCertificateDialog(SSLCertificateDialogModel model);

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
     * Initialises any settings required by this UI (this is always called
     * before any aspect of the UI is instansiated).
     */
    void initUISettings();

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

    /**
     * Requests user input.
     *
     * @param prompt The prompt to display
     * @return The user-inputted string
     */
    String getUserInput(final String prompt);

    /**
     * Retrieves the object used to display the plugin preferences panel.
     *
     * @return The plugin preferences panel
     */
    PreferencesInterface getPluginPrefsPanel();

    /**
     * Retrieves the object used to display the updates preferences panel.
     *
     * @return The updates preferences panel
     */
    PreferencesInterface getUpdatesPrefsPanel();

    /**
     * Retrieves the object used to display the URL handlers preferences panel.
     *
     * @return The url handlers preferences panel
     */
    PreferencesInterface getUrlHandlersPrefsPanel();

    /**
     * Retrieves the object used to display the themes preferences panel.
     *
     * @return The themes preferences panel
     */
    PreferencesInterface getThemesPrefsPanel();

}
