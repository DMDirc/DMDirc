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

package com.dmdirc.commandparser.commands.global;

import com.dmdirc.FrameContainer;
import com.dmdirc.Server;
import com.dmdirc.commandparser.BaseCommandInfo;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.CommandInfo;
import com.dmdirc.commandparser.CommandType;
import com.dmdirc.commandparser.commands.Command;
import com.dmdirc.commandparser.commands.IntelligentCommand;
import com.dmdirc.commandparser.commands.context.CommandContext;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.plugins.PluginManager;
import com.dmdirc.plugins.Service;
import com.dmdirc.ui.input.AdditionalTabTargets;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The new server command allows users to open a new server window.
 */
public class NewServer extends Command implements IntelligentCommand {

    /** A command info object for this command. */
    public static final CommandInfo INFO = new BaseCommandInfo("newserver",
            "newserver <host[:[+]port]> [password] - connect to a new server",
            CommandType.TYPE_GLOBAL);

    /** {@inheritDoc} */
    @Override
    public void execute(final FrameContainer origin,
            final CommandArguments args, final CommandContext context) {
        URI address = null;

        if (args.getArguments().length == 0) {
            showUsage(origin, args.isSilent(), "newserver",
                    "<host[:[+]port]> [password]");
            return;
        } else if (args.getArguments().length == 1
                && args.getArgumentsAsString().contains("://")) {
            try {
                address = getURI(args.getArgumentsAsString());
            } catch (URISyntaxException ex) {
                origin.addLine(FORMAT_ERROR, "URI specified was invalid.");
                return;
            }
        }
        if (address == null) {
            address = parseInput(origin, args.isSilent(), args);
        }

        if (address == null) {
            return;
        }

        final Server server = new Server(address, IdentityManager
                .getCustomIdentities("profile").get(0));
        server.connect();
    }

    /**
     * Get a URI from the given string.
     * This method allows for +port in the uri (eg irc://server.host:+6668/)
     *
     * @param address Address to parse
     * @return URI from address.
     * @throws URISyntaxException If the string is not parseable.
     */
    public static URI getURI(final String address) throws URISyntaxException {
        final URI uri = new URI(address);
        final int port = uri.getPort();

        // Either no port specified, or a +port was used, lets try to find one.
        // Otherwise just return the URI.
        if (port == -1) {
            final Matcher m = Pattern.compile(".*://[^/:]+:\\+([0-9]+).*").matcher(address);
            // If no port is found, then again just return the uri above, the
            // Parser can use its default.
            if (m.matches()) {
                m.find();
                int newPort = -1;
                try {
                    newPort = Integer.parseInt(m.group(2));
                } catch (final NumberFormatException nfe) { }

                // Add 's' to scheme if not already there.
                String scheme = uri.getScheme();
                if (scheme.charAt(scheme.length() - 1) != 's') {
                    scheme += "s";
                }
                return new URI(scheme, uri.getUserInfo(), uri.getHost(), newPort, uri.getPath(), uri.getQuery(), uri.getFragment());
            }
        }

        // If a port already existed, or we were unable to find a port, just
        // use the default one we had to start with.
        return uri;
    }

    /**
     * Parses an input string and attempts to create a URI from it.
     *
     * @param origin origin input window
     * @param isSilent is this a silent command
     * @param args command arguments
     *
     * @return URI is input was valid
     */
    public static URI parseInput(final FrameContainer origin, final boolean isSilent,
            final CommandArguments args) {

        boolean ssl = false;
        String host = "";
        String pass = null;
        int port = 6667;
        int offset = 0;

        // Check for SSL
        if (args.getArguments()[offset].equalsIgnoreCase("--ssl")) {
            Logger.userError(ErrorLevel.LOW,
                    "Using /newserver --ssl is deprecated, and may be removed in the future."
                    + " Use /newserver <host>:+<port> instead.");

            ssl = true;
            offset++;
        }

        // Check for port
        if (args.getArguments()[offset].indexOf(':') > -1) {
            final String[] parts = args.getArguments()[offset].split(":");

            if (parts.length < 2) {
                if (origin != null) {
                    origin.addLine(FORMAT_ERROR, "Invalid port specified");
                } else {
                    Logger.userError(ErrorLevel.LOW, "Invalid port specified "
                            + "in newserver command");
                }
                return null;
            }
            host = parts[0];

            if (parts[1].length() > 0 && parts[1].charAt(0) == '+') {
                ssl = true;
                parts[1] = parts[1].substring(1);
            }

            try {
                port = Integer.parseInt(parts[1]);
            } catch (NumberFormatException ex) {
                if (origin != null) {
                    origin.addLine(FORMAT_ERROR, "Invalid port specified");
                } else {
                    Logger.userError(ErrorLevel.LOW, "Invalid port specified "
                            + "in newserver command");
                }
                return null;
            }

            if (port <= 0 || port > 65535) {
                if (origin == null) {
                    Logger.userError(ErrorLevel.LOW, "Port must be between 1 "
                            + "and 65535 in newserver command");
                } else if (!args.isSilent()) {
                    origin.addLine(FORMAT_ERROR, "Port must be between 1 and 65535");
                }

                return null;
            }
        } else {
            host = args.getArguments()[offset];
        }

        // Check for password
        if (args.getArguments().length > ++offset) {
            pass = args.getArgumentsAsString(offset);
        }

        try {
            return new URI("irc" + (ssl ? "s" : ""), pass, host, port, null, null, null);
        } catch (URISyntaxException ex) {
            if (origin == null) {
                Logger.userError(ErrorLevel.LOW, "Invalid address provided to "
                        + "newserver command. Host: " + host + ", Port: " + port, ex);
            } else if (!args.isSilent()) {
                origin.addLine(FORMAT_ERROR, "Invalid address specified.");
            }

            return null;
        }
    }

    /** {@inheritDoc} */
    @Override
    public AdditionalTabTargets getSuggestions(final int arg,
            final IntelligentCommandContext context) {
        final AdditionalTabTargets res = new AdditionalTabTargets();

        if (arg == 0) {
            for (Service parserType : PluginManager.getPluginManager().getServicesByType("parser")) {
                res.add(parserType.getName()+"://");
            }
        }
        res.excludeAll();
        return res;
    }
}
