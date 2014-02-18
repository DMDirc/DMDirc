/*
 * Copyright (c) 2006-2014 DMDirc Developers
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

package com.dmdirc.ui.core.util;

import com.dmdirc.ServerManager;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.interfaces.ui.UIController;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.ui.StatusMessage;
import com.dmdirc.ui.core.components.StatusBarManager;
import com.dmdirc.util.CommandUtils;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;

/** Handles URLs. */
public class URLHandler {

    /** The time a browser was last launched. */
    private static Date lastLaunch;
    /** The UI Controller that owns this handler. */
    private final UIController controller;
    /** Config manager. */
    private final AggregateConfigProvider config;
    /** Server manager to use to connect to servers. */
    private final ServerManager serverManager;
    /** Status bar manager to use to show messages. */
    private final StatusBarManager statusBarManager;
    /** Desktop handler. */
    private final Desktop desktop;

    /**
     * Instantiates a new URL Handler.
     *
     * @param controller       The UI controller to show dialogs etc on
     * @param globalConfig     Config to retrieve settings from
     * @param serverManager    Server manager to connect to servers
     * @param statusBarManager Status bar manager used to show messages
     */
    public URLHandler(
            final UIController controller,
            final AggregateConfigProvider globalConfig,
            final ServerManager serverManager,
            final StatusBarManager statusBarManager) {
        this.controller = controller;
        this.config = globalConfig;
        this.serverManager = serverManager;
        this.statusBarManager = statusBarManager;
        this.desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
    }

    /**
     * Launches an application for a given url.
     *
     * @param urlString URL to parse
     */
    public void launchApp(final String urlString) {
        final String sanitisedString = getSanitisedString(urlString);

        URI uri;
        try {
            uri = new URI(sanitisedString);
            if (uri.getScheme() == null) {
                uri = new URI("http://" + sanitisedString);
            }
        } catch (URISyntaxException ex) {
            Logger.userError(ErrorLevel.LOW, "Invalid URI: " + ex.getMessage(), ex);
            return;
        }

        launchApp(uri);
    }

    /**
     * Sanitises the specified string so that it may be used as a {@link URI}. Sanitisation consists
     * of:
     * <ul>
     * <li>replacing any pipe character with its hex escape</li>
     * <li>replacing any hash character in the fragment with its hex escape</li>
     * </ul>
     *
     * @since 0.6.5
     * @param urlString The string to be sanitised
     *
     * @return A sanitised version of the specified string.
     */
    protected static String getSanitisedString(final String urlString) {
        String sanitisedString = urlString.replace("|", "%7C");

        final int index = sanitisedString.indexOf('#');
        if (sanitisedString.lastIndexOf('#') > index) {
            sanitisedString = sanitisedString.substring(0, index + 1)
                    + sanitisedString.substring(index + 1).replace("#", "%23");
        }

        return sanitisedString;
    }

    /**
     * Launches an application for a given url.
     *
     * @param url URL to parse
     */
    public void launchApp(final URL url) {
        URI uri;
        try {
            uri = url.toURI();
            if (uri.getScheme() == null) {
                uri = new URI("http://" + url.toString());
            }
        } catch (URISyntaxException ex) {
            Logger.userError(ErrorLevel.LOW, "Invalid URL: " + ex.getMessage(), ex);
            return;
        }

        launchApp(uri);
    }

    /**
     * Launches an application for a given url.
     *
     * @param uri URL to parse
     */
    public void launchApp(final URI uri) {
        final Date oldLaunch = lastLaunch;
        lastLaunch = new Date();

        if (config.getOptionBool("browser", "uselaunchdelay") && oldLaunch != null) {
            final Long diff = lastLaunch.getTime() - oldLaunch.getTime();

            if (diff < config.getOptionInt("browser", "launchdelay")) {
                return;
            }
        }

        if (!config.hasOptionString("protocol", uri.getScheme().toLowerCase())) {
            controller.showURLDialog(uri);
            return;
        }

        final String command = config.getOption("protocol", uri.getScheme().toLowerCase());
        switch (command) {
            case "DMDIRC":
                statusBarManager.setMessage(
                        new StatusMessage("Connecting to: " + uri.toString(),
                        config));
                serverManager.connectToAddress(uri);
                break;
            case "BROWSER":
                statusBarManager.setMessage(
                        new StatusMessage("Opening: " + uri.toString(),
                        config));
                execBrowser(uri);
                break;
            case "MAIL":
                execMail(uri);
                break;
            default:
                statusBarManager.setMessage(
                        new StatusMessage("Opening: " + uri.toString(),
                        config));
                execApp(substituteParams(uri, command));
                break;
        }
    }

    /**
     * Substitutes variables into a command.
     *
     * @param url     data url
     * @param command command to be substituted
     *
     * @return Substituted command
     */
    public static String substituteParams(final URI url, final String command) {
        final String userInfo = url.getUserInfo();
        String fragment = "";
        String host = "";
        String path = "";
        String protocol = "";
        String query = "";
        String username = "";
        String password = "";
        String port = "";
        String newCommand = command;

        if (url.getFragment() != null) {
            fragment = url.getFragment();
        }

        if (url.getHost() != null) {
            host = url.getHost();
        }

        if (url.getPath() != null) {
            path = url.getPath();
        }

        if (url.getScheme() != null) {
            protocol = url.getScheme();
        }

        if (url.getQuery() != null) {
            query = url.getQuery();
        }

        if (url.getPort() > 0) {
            port = String.valueOf(url.getPort());
        }

        if (userInfo != null && !userInfo.isEmpty()) {
            if (userInfo.indexOf(':') == -1) {
                username = userInfo;
            } else {
                final int pos = userInfo.indexOf(':');
                username = userInfo.substring(0, pos);
                password = userInfo.substring(pos + 1);
            }
        }

        newCommand = newCommand.replace("$url", url.toString());
        newCommand = newCommand.replace("$fragment", fragment);
        newCommand = newCommand.replace("$host", host);
        newCommand = newCommand.replace("$path", path);
        newCommand = newCommand.replace("$port", port);
        newCommand = newCommand.replace("$query", query);
        newCommand = newCommand.replace("$protocol", protocol);
        newCommand = newCommand.replace("$username", username);
        newCommand = newCommand.replace("$password", password);

        return newCommand;
    }

    /**
     * Launches an application.
     *
     * @param command Application and arguments
     */
    private void execApp(final String command) {
        try {
            Runtime.getRuntime().exec(CommandUtils.parseArguments(command));
        } catch (IOException ex) {
            Logger.userError(ErrorLevel.LOW, "Unable to run application: "
                    + ex.getMessage(), ex);
        }
    }

    /**
     * Opens the specified URL in the users browser.
     *
     * @param url URL to open
     */
    private void execBrowser(final URI url) {
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
            try {
                desktop.browse(url);
            } catch (IOException ex) {
                Logger.userError(ErrorLevel.LOW,
                        "Unable to open URL: " + ex.getMessage(), ex);
            }
        } else {
            Logger.userError(ErrorLevel.LOW,
                    "Unable to open your browser: Your desktop enviroment is "
                    + "not supported, please go to the URL Handlers section of "
                    + "the preferences dialog and set the path to your browser "
                    + "manually");
        }
    }

    /**
     * Opens the specified URL in the users mail client.
     *
     * @param url URL to open
     */
    private void execMail(final URI url) {
        if (desktop != null && desktop.isSupported(Desktop.Action.MAIL)) {
            try {
                desktop.mail(url);
            } catch (IOException ex) {
                Logger.userError(ErrorLevel.LOW,
                        "Unable to open URL: " + ex.getMessage(), ex);
            }
        } else {
            Logger.userError(ErrorLevel.LOW,
                    "Unable to open your mail client: Your desktop enviroment is "
                    + "not supported, please go to the URL Handlers section of "
                    + "the preferences dialog and set the path to your browser "
                    + "manually");
        }
    }

}
