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

package com.dmdirc.ui.core.util;

import com.dmdirc.ServerManager;
import com.dmdirc.config.ConfigManager;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.ui.StatusMessage;
import com.dmdirc.ui.core.components.StatusBarManager;
import com.dmdirc.ui.interfaces.UIController;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/** Handles URLs. */
public class URLHandler {

    /** The UI Controller that owns this handler. */
    private final UIController controller;
    /** Config manager. */
    private final ConfigManager config;
    /** Desktop handler. */
    private final Desktop desktop;
    /** The time a browser was last launched. */
    private static Date lastLaunch;

    /**
     * Instantiates a new URL Handler.
     *
     * @param controller The UI controller to show dialogs etc on
     */
    public URLHandler(final UIController controller) {
        this.controller = controller;
        this.config = IdentityManager.getGlobalConfig();
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
     * Sanitises the specified string so that it may be used as a {@link URI}.
     * Sanitisation consists of:
     * <ul>
     * <li>replacing any pipe character with its hex escape</li>
     * <li>replacing any hash character in the fragment with its hex escape</li>
     * </ul>
     *
     * @since 0.6.5
     * @param urlString The string to be sanitised
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

        if (IdentityManager.getGlobalConfig().getOptionBool("browser",
                "uselaunchdelay") && oldLaunch != null) {
            final Long diff = lastLaunch.getTime() - oldLaunch.getTime();

            if (diff < IdentityManager.getGlobalConfig().getOptionInt("browser",
                    "launchdelay")) {
                return;
            }
        }

        if (!config.hasOptionString("protocol", uri.getScheme().toLowerCase())) {
            controller.showURLDialog(uri);
            return;
        }

        final String command = config.getOption("protocol", uri.getScheme().toLowerCase());

        if ("DMDIRC".equals(command)) {
            StatusBarManager.getStatusBarManager().setMessage(
                    new StatusMessage("Connecting to: " + uri.toString(),
                    IdentityManager.getGlobalConfig()));
            ServerManager.getServerManager().connectToAddress(uri);
        } else if ("BROWSER".equals(command)) {
            StatusBarManager.getStatusBarManager().setMessage(
                    new StatusMessage("Opening: " + uri.toString(),
                    IdentityManager.getGlobalConfig()));
            execBrowser(uri);
        } else if ("MAIL".equals(command)) {
            execMail(uri);
        } else {
            StatusBarManager.getStatusBarManager().setMessage(
                    new StatusMessage("Opening: " + uri.toString(),
                    IdentityManager.getGlobalConfig()));
            execApp(substituteParams(uri, command));
        }
    }

    /**
     * Substitutes variables into a command.
     *
     * @param url data url
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
            Runtime.getRuntime().exec(parseArguments(command));
        } catch (IOException ex) {
            Logger.userError(ErrorLevel.LOW, "Unable to run application: "
                    + ex.getMessage(), ex);
        }
    }

    /**
     * Parses the specified command into an array of arguments. Arguments are
     * separated by spaces. Multi-word arguments may be specified by starting
     * the argument with a quote (") and finishing it with a quote (").
     *
     * @param command The command to parse
     * @return An array of arguments corresponding to the command
     */
    protected static String[] parseArguments(final String command) {
        final List<String> args = new ArrayList<String>();
        final StringBuilder builder = new StringBuilder();
        boolean inquote = false;

        for (String word : command.split(" ")) {
            if (word.endsWith("\"") && inquote) {
                args.add(builder.toString() + ' ' + word.substring(0, word.length() - 1));
                builder.delete(0, builder.length());
                inquote = false;
            } else if (inquote) {
                if (builder.length() > 0) {
                    builder.append(' ');
                }

                builder.append(word);
            } else if (word.startsWith("\"") && !word.endsWith("\"")) {
                inquote = true;
                builder.append(word.substring(1));
            } else if (word.startsWith("\"") && word.endsWith("\"")) {
                if (word.length() == 1) {
                    inquote = true;
                } else {
                    args.add(word.substring(1, word.length() - 1));
                }
            } else {
                args.add(word);
            }
        }

        return args.toArray(new String[args.size()]);
    }

    /**
     * Opens the specified URL in the users browser.
     *
     * @param url URL to open
     */
    private void execBrowser(final URI url) {
        if (desktop != null &&
                desktop.isSupported(Desktop.Action.BROWSE)) {
            try {
                desktop.browse(url);
            } catch (IOException ex) {
                Logger.userError(ErrorLevel.LOW,
                        "Unable to open URL: " + ex.getMessage(), ex);
            }
        } else {
            Logger.userError(ErrorLevel.LOW,
                    "Unable to open your browser: Your desktop enviroment is " +
                    "not supported, please go to the URL Handlers section of " +
                    "the preferences dialog and set the path to your browser " +
                    "manually");
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
                    "Unable to open your mail client: Your desktop enviroment is " +
                    "not supported, please go to the URL Handlers section of " +
                    "the preferences dialog and set the path to your browser " +
                    "manually");
        }
    }
}
